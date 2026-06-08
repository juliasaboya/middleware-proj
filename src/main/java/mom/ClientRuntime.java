package mom;

import jakarta.jms.Connection;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ClientRuntime {
    private static final DateTimeFormatter MESSAGE_FORMAT = DateTimeFormatter.ofPattern("dd/MM HH:mm:ss");

    private final Client client;
    private final ActiveMQConnectionFactory connectionFactory;
    private final DefaultListModel<String> messageModel = new DefaultListModel<>();
    private final Set<String> subscribedTopics = new LinkedHashSet<>();
    private final Map<String, MessageConsumer> consumersByTopic = new LinkedHashMap<>();

    private Connection connection;
    private Session session;
    private String lastStatusMessage = "Nenhum topico assinado.";
    private Runnable onRuntimeChanged = () -> {
    };

    public ClientRuntime(Client client, String brokerUrl) {
        this.client = client;
        this.connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        messageModel.addElement(timestamp() + " | Cliente criado");
    }

    public Client getClient() {
        return client;
    }

    public DefaultListModel<String> getMessageModel() {
        return messageModel;
    }

    public List<String> getSubscribedTopics() {
        return new ArrayList<>(subscribedTopics);
    }

    public int getSubscribedTopicCount() {
        return subscribedTopics.size();
    }

    public int getReceivedMessageCount() {
        return Math.max(0, messageModel.getSize() - 1);
    }

    public String getLastStatusMessage() {
        return lastStatusMessage;
    }

    public void setOnRuntimeChanged(Runnable onRuntimeChanged) {
        this.onRuntimeChanged = onRuntimeChanged == null ? () -> {
        } : onRuntimeChanged;
    }

    public void updateSubscriptions(List<String> topics) throws Exception {
        closeConsumers();
        subscribedTopics.clear();
        subscribedTopics.addAll(topics);

        if (subscribedTopics.isEmpty()) {
            lastStatusMessage = "Nenhum topico assinado.";
            appendMessage("Assinaturas removidas");
            SwingUtilities.invokeLater(this.onRuntimeChanged);
            return;
        }

        ensureSession();
        for (String topic : subscribedTopics) {
            MessageConsumer consumer = session.createConsumer(session.createTopic(topic));
            consumer.setMessageListener(message -> handleIncomingMessage(topic, message));
            consumersByTopic.put(topic, consumer);
        }

        lastStatusMessage = "Assinando " + subscribedTopics.size() + " topico(s).";
        appendMessage("Assinaturas atualizadas: " + String.join(", ", subscribedTopics));
        SwingUtilities.invokeLater(this.onRuntimeChanged);
    }

    public void close() {
        closeConsumers();
        closeSessionResources();
    }

    private void ensureSession() throws Exception {
        if (session != null) {
            return;
        }

        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        connection.start();
    }

    private void handleIncomingMessage(String topic, Message message) {
        String payload = message.toString();

        if (message instanceof TextMessage textMessage) {
            try {
                String sensorType = textMessage.getStringProperty("sensorType");
                String alertType = textMessage.getStringProperty("alertType");
                double currentValue = textMessage.getDoubleProperty("currentValue");
                payload = String.format(
                        Locale.US,
                        "%s | Tópico %s | leitura = %.2f | Está %s da faixa estabelecida para %s",
                        timestamp(),
                        topic,
                        currentValue,
                        resolveCondition(alertType),
                        resolveSensorType(sensorType)
                );
            } catch (Exception exception) {
                payload = timestamp() + " | " + topic + " | Falha ao ler mensagem: " + exception.getMessage();
            }
        } else {
            payload = timestamp() + " | " + topic + " | " + payload;
        }

        String finalPayload = payload;
        SwingUtilities.invokeLater(() -> {
            messageModel.add(0, finalPayload);
            lastStatusMessage = "Mensagens recebidas: " + getReceivedMessageCount();
            onRuntimeChanged.run();
        });
    }

    private void closeConsumers() {
        for (MessageConsumer consumer : consumersByTopic.values()) {
            try {
                consumer.close();
            } catch (Exception ignored) {
                // Ignore consumer shutdown failures.
            }
        }
        consumersByTopic.clear();
    }

    private void closeSessionResources() {
        if (session != null) {
            try {
                session.close();
            } catch (Exception ignored) {
                // Ignore session shutdown failures.
            }
            session = null;
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (Exception ignored) {
                // Ignore connection shutdown failures.
            }
            connection = null;
        }
    }

    private void appendMessage(String detail) {
        messageModel.add(0, timestamp() + " | " + detail);
    }

    private String timestamp() {
        return MESSAGE_FORMAT.format(LocalDateTime.now());
    }

    private String resolveCondition(String alertType) {
        if ("MAXIMUM_REACHED".equals(alertType)) {
            return "acima";
        }

        return "abaixo";
    }

    private String resolveSensorType(String sensorType) {
        try {
            return SensorType.valueOf(sensorType).getDisplayName();
        } catch (Exception exception) {
            return sensorType == null ? "sensor" : sensorType;
        }
    }
}

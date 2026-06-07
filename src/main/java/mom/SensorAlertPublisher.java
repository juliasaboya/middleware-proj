package mom;

import jakarta.jms.Connection;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.Locale;

public class SensorAlertPublisher {
    private final ActiveMQConnectionFactory connectionFactory;

    public SensorAlertPublisher(String brokerUrl) {
        this.connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
    }

    public String publishLimitAlert(Sensor sensor, String alertType) throws Exception {
        String topicName = topicNameFor(sensor);

        try (
                Connection connection = connectionFactory.createConnection();
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                MessageProducer producer = session.createProducer(session.createTopic(topicName))
        ) {
            TextMessage message = session.createTextMessage(buildBody(sensor, alertType));
            message.setStringProperty("sensorId", sensor.getId());
            message.setStringProperty("sensorType", sensor.getType().name());
            message.setStringProperty("alertType", alertType);
            message.setDoubleProperty("currentValue", sensor.getCurrentValue());
            message.setDoubleProperty("minimum", sensor.getMinimum());
            message.setDoubleProperty("maximum", sensor.getMaximum());
            producer.send(message);
        }

        return topicName;
    }

    public String topicNameFor(Sensor sensor) {
        return "sensor." + sensor.getId();
    }

    private String buildBody(Sensor sensor, String alertType) {
        String condition = "MAXIMUM_REACHED".equals(alertType) ? "acima do maximo" : "abaixo do minimo";
        return String.format(
                Locale.US,
                "Sensor %s (%s) registrou %.2f %s e esta %s. Faixa configurada: %.2f a %.2f.",
                sensor.getId(),
                sensor.getType().getDisplayName(),
                sensor.getCurrentValue(),
                sensor.getType().getUnit(),
                condition,
                sensor.getMinimum(),
                sensor.getMaximum()
        );
    }
}

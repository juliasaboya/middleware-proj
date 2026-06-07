package mom;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ClientMonitorFrame extends JFrame {
    private final ClientRuntime clientRuntime;
    private final Supplier<List<String>> availableTopicsSupplier;
    private final Runnable onClientUpdated;
    private final JLabel statusLabel = new JLabel();
    private final DefaultTopicListModel availableTopicsModel = new DefaultTopicListModel();
    private final JList<String> availableTopicsList = new JList<>(availableTopicsModel);

    public ClientMonitorFrame(ClientRuntime clientRuntime, Supplier<List<String>> availableTopicsSupplier, Runnable onClientUpdated) {
        super("Cliente " + clientRuntime.getClient().getId());
        this.clientRuntime = clientRuntime;
        this.availableTopicsSupplier = availableTopicsSupplier;
        this.onClientUpdated = onClientUpdated;
        this.clientRuntime.setOnRuntimeChanged(this::handleRuntimeUpdated);
        configureFrame();
        buildUi();
        refreshAvailableTopics();
        refreshStatus();
    }

    public void focusWindow() {
        refreshAvailableTopics();
        setVisible(true);
        toFront();
        requestFocus();
    }

    public void refreshAvailableTopics() {
        List<String> availableTopics = availableTopicsSupplier.get();
        availableTopicsModel.setTopics(availableTopics);
        restoreSelectedTopics();
    }

    private void configureFrame() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(680, 440);
        setLocationByPlatform(true);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel titleLabel = new JLabel("Monitor do cliente " + clientRuntime.getClient().getId());
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        availableTopicsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane topicsScrollPane = new JScrollPane(availableTopicsList);
        topicsScrollPane.setBorder(BorderFactory.createTitledBorder("Topicos disponiveis"));

        JList<String> messageList = new JList<>(clientRuntime.getMessageModel());
        JScrollPane messageScrollPane = new JScrollPane(messageList);
        messageScrollPane.setBorder(BorderFactory.createTitledBorder("Mensagens recebidas"));

        JButton refreshTopicsButton = new JButton("Atualizar topicos");
        JButton saveSubscriptionsButton = new JButton("Salvar assinaturas");
        refreshTopicsButton.addActionListener(event -> refreshAvailableTopics());
        saveSubscriptionsButton.addActionListener(event -> saveSubscriptions());

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.add(refreshTopicsButton);
        actionPanel.add(saveSubscriptionsButton);

        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(topicsScrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 8));
        bottomPanel.add(statusLabel, BorderLayout.NORTH);
        bottomPanel.add(actionPanel, BorderLayout.SOUTH);

        root.add(topPanel, BorderLayout.NORTH);
        root.add(messageScrollPane, BorderLayout.CENTER);
        root.add(bottomPanel, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void saveSubscriptions() {
        try {
            List<String> selectedTopics = availableTopicsList.getSelectedValuesList();
            clientRuntime.updateSubscriptions(selectedTopics);
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Nao foi possivel atualizar as assinaturas.\n" + exception.getMessage(),
                    "Erro de assinatura",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void restoreSelectedTopics() {
        List<String> subscribedTopics = clientRuntime.getSubscribedTopics();
        List<Integer> indexes = new ArrayList<>();
        for (int index = 0; index < availableTopicsModel.getSize(); index++) {
            if (subscribedTopics.contains(availableTopicsModel.getElementAt(index))) {
                indexes.add(index);
            }
        }

        int[] selection = indexes.stream().mapToInt(Integer::intValue).toArray();
        availableTopicsList.setSelectedIndices(selection);
    }

    private void refreshStatus() {
        statusLabel.setText(clientRuntime.getLastStatusMessage());
    }

    private void handleRuntimeUpdated() {
        refreshStatus();
        onClientUpdated.run();
    }
}

package mom;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SensorDashboardFrame extends JFrame {
    private final EmbeddedBrokerManager brokerManager = new EmbeddedBrokerManager();
    private final SensorFactory sensorFactory = new SensorFactory();
    private final ClientFactory clientFactory = new ClientFactory();

    private final SensorAlertPublisher alertPublisher = new SensorAlertPublisher(EmbeddedBrokerManager.BROKER_URL);
    private final JLabel statusLabel = new JLabel("Inicializando broker...");
    private final SensorTableModel tableModel = new SensorTableModel();
    private final ClientTableModel clientTableModel = new ClientTableModel();
    private final JButton createSensorButton = new JButton("Criar sensor");
    private final JButton editSensorButton = new JButton("Abrir sensor");
    private final JButton deleteSensorButton = new JButton("Excluir sensor");
    private final JButton createClientButton = new JButton("Criar cliente");
    private final JButton openClientButton = new JButton("Abrir cliente");
    private final JButton deleteClientButton = new JButton("Excluir cliente");
    private final JTable sensorTable = new JTable(tableModel);
    private final JTable clientTable = new JTable(clientTableModel);
    private final Map<String, SensorRuntime> sensorRuntimes = new LinkedHashMap<>();
    private final Map<String, SensorMonitorFrame> sensorWindows = new LinkedHashMap<>();
    private final Map<String, ClientRuntime> clientRuntimes = new LinkedHashMap<>();
    private final Map<String, ClientMonitorFrame> clientWindows = new LinkedHashMap<>();

    public SensorDashboardFrame() {
        super("Middleware Orientado a Mensagens");
        configureFrame();
        buildUi();
        initializeApplication();
    }

    private void configureFrame() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(920, 480);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                stopBroker();
            }
        });
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel titleLabel = new JLabel("Hub IoT");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));

        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

        sensorTable.setFillsViewportHeight(true);
        sensorTable.setRowHeight(24);
        sensorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sensorTable.getSelectionModel().addListSelectionListener(this::handleSensorSelectionChange);

        clientTable.setFillsViewportHeight(true);
        clientTable.setRowHeight(24);
        clientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientTable.getSelectionModel().addListSelectionListener(this::handleClientSelectionChange);

        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.add(titleLabel, BorderLayout.NORTH);
        header.add(statusLabel, BorderLayout.SOUTH);

        createSensorButton.addActionListener(event -> openCreateSensorDialog());
        editSensorButton.addActionListener(event -> openSensorWindow());
        deleteSensorButton.addActionListener(event -> deleteSelectedSensor());
        createClientButton.addActionListener(event -> openCreateClientDialog());
        openClientButton.addActionListener(event -> openClientWindow());
        deleteClientButton.addActionListener(event -> deleteSelectedClient());

        JPanel sensorPanel = new JPanel(new BorderLayout(8, 8));
        sensorPanel.setBorder(BorderFactory.createTitledBorder("Sensores"));
        JPanel sensorActionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        sensorActionsPanel.add(createSensorButton);
        sensorActionsPanel.add(editSensorButton);
        sensorActionsPanel.add(deleteSensorButton);
        sensorPanel.add(sensorActionsPanel, BorderLayout.NORTH);
        sensorPanel.add(new JScrollPane(sensorTable), BorderLayout.CENTER);

        JPanel clientPanel = new JPanel(new BorderLayout(8, 8));
        clientPanel.setBorder(BorderFactory.createTitledBorder("Clientes"));
        JPanel clientActionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        clientActionsPanel.add(createClientButton);
        clientActionsPanel.add(openClientButton);
        clientActionsPanel.add(deleteClientButton);
        clientPanel.add(clientActionsPanel, BorderLayout.NORTH);
        clientPanel.add(new JScrollPane(clientTable), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sensorPanel, clientPanel);
        splitPane.setResizeWeight(0.58);
        splitPane.setBorder(null);

        updateSensorActionButtons();
        updateClientActionButtons();

        root.add(header, BorderLayout.NORTH);
        root.add(splitPane, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void initializeApplication() {
        try {
            brokerManager.start();
            updateStatusLabel();
        } catch (Exception exception) {
            statusLabel.setText("Falha ao iniciar broker");
            createSensorButton.setEnabled(false);
            editSensorButton.setEnabled(false);
            deleteSensorButton.setEnabled(false);
            createClientButton.setEnabled(false);
            openClientButton.setEnabled(false);
            deleteClientButton.setEnabled(false);
            JOptionPane.showMessageDialog(
                    this,
                    "Nao foi possivel iniciar o broker ActiveMQ.\n" + exception.getMessage(),
                    "Erro de inicializacao",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void openCreateSensorDialog() {
        SensorFormDialog dialog = new SensorFormDialog(this, sensorFactory, tableModel.getSensors());
        dialog.setVisible(true);

        Sensor createdSensor = dialog.getCreatedSensor();
        if (createdSensor == null) {
            return;
        }

        tableModel.addSensor(createdSensor);
        SensorRuntime sensorRuntime = new SensorRuntime(createdSensor, alertPublisher);
        sensorRuntime.addStateListener(() -> tableModel.notifySensorUpdated(createdSensor));
        sensorRuntimes.put(createdSensor.getId(), sensorRuntime);
        showSensorWindow(sensorRuntime);
        refreshClientWindowsTopics();
        updateStatusLabel();
        updateSensorActionButtons();
    }

    private void openSensorWindow() {
        int selectedRow = sensorTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        SensorRuntime sensorRuntime = sensorRuntimes.get(tableModel.getSensorAt(selectedRow).getId());
        if (sensorRuntime == null) {
            return;
        }

        showSensorWindow(sensorRuntime);
    }

    private void deleteSelectedSensor() {
        int selectedRow = sensorTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        Sensor sensor = tableModel.getSensorAt(selectedRow);
        int option = JOptionPane.showConfirmDialog(
                this,
                "Excluir o sensor " + sensor.getId() + "?",
                "Confirmar exclusao",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        SensorMonitorFrame sensorWindow = sensorWindows.remove(sensor.getId());
        if (sensorWindow != null) {
            sensorWindow.dispose();
        }

        SensorRuntime sensorRuntime = sensorRuntimes.remove(sensor.getId());
        if (sensorRuntime != null) {
            sensorRuntime.stopSimulation();
        }
        tableModel.removeSensor(selectedRow);
        refreshClientWindowsTopics();
        updateStatusLabel();
        updateSensorActionButtons();
    }

    private void showSensorWindow(SensorRuntime sensorRuntime) {
        Sensor sensor = sensorRuntime.getSensor();
        SensorMonitorFrame sensorWindow = sensorWindows.get(sensor.getId());

        if (sensorWindow == null || !sensorWindow.isDisplayable()) {
            sensorWindow = new SensorMonitorFrame(sensorRuntime, () -> tableModel.notifySensorUpdated(sensor));
            sensorWindows.put(sensor.getId(), sensorWindow);
        }

        sensorWindow.focusWindow();
    }

    private void openCreateClientDialog() {
        ClientFormDialog dialog = new ClientFormDialog(this, clientFactory, clientTableModel.getClients());
        dialog.setVisible(true);

        Client createdClient = dialog.getCreatedClient();
        if (createdClient == null) {
            return;
        }

        clientTableModel.addClient(createdClient);
        ClientRuntime clientRuntime = new ClientRuntime(createdClient, EmbeddedBrokerManager.BROKER_URL);
        clientRuntimes.put(createdClient.getId(), clientRuntime);
        syncClientMetrics(clientRuntime);
        showClientWindow(clientRuntime);
        updateStatusLabel();
        updateClientActionButtons();
    }

    private void openClientWindow() {
        int selectedRow = clientTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        ClientRuntime clientRuntime = clientRuntimes.get(clientTableModel.getClientAt(selectedRow).getId());
        if (clientRuntime == null) {
            return;
        }

        showClientWindow(clientRuntime);
    }

    private void deleteSelectedClient() {
        int selectedRow = clientTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        Client client = clientTableModel.getClientAt(selectedRow);
        int option = JOptionPane.showConfirmDialog(
                this,
                "Excluir o cliente " + client.getId() + "?",
                "Confirmar exclusao",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        ClientMonitorFrame clientWindow = clientWindows.remove(client.getId());
        if (clientWindow != null) {
            clientWindow.dispose();
        }

        ClientRuntime clientRuntime = clientRuntimes.remove(client.getId());
        if (clientRuntime != null) {
            clientRuntime.close();
        }

        clientTableModel.removeClient(selectedRow);
        updateStatusLabel();
        updateClientActionButtons();
    }

    private void showClientWindow(ClientRuntime clientRuntime) {
        Client client = clientRuntime.getClient();
        ClientMonitorFrame clientWindow = clientWindows.get(client.getId());

        if (clientWindow == null || !clientWindow.isDisplayable()) {
            clientWindow = new ClientMonitorFrame(
                    clientRuntime,
                    this::listAvailableTopics,
                    () -> {
                        syncClientMetrics(clientRuntime);
                        clientTableModel.notifyClientUpdated(client);
                    }
            );
            clientWindows.put(client.getId(), clientWindow);
        }

        clientWindow.focusWindow();
    }

    private void syncClientMetrics(ClientRuntime clientRuntime) {
        Client client = clientRuntime.getClient();
        client.setSubscribedTopicCount(clientRuntime.getSubscribedTopicCount());
        client.setReceivedMessageCount(clientRuntime.getReceivedMessageCount());
    }

    private List<String> listAvailableTopics() {
        List<String> topics = new ArrayList<>();
        for (Sensor sensor : tableModel.getSensors()) {
            topics.add(alertPublisher.topicNameFor(sensor));
        }
        return topics;
    }

    private void refreshClientWindowsTopics() {
        for (ClientMonitorFrame clientWindow : clientWindows.values()) {
            if (clientWindow.isDisplayable()) {
                clientWindow.refreshAvailableTopics();
            }
        }
    }

    private void handleSensorSelectionChange(ListSelectionEvent event) {
        if (!event.getValueIsAdjusting()) {
            updateSensorActionButtons();
        }
    }

    private void handleClientSelectionChange(ListSelectionEvent event) {
        if (!event.getValueIsAdjusting()) {
            updateClientActionButtons();
        }
    }

    private void updateSensorActionButtons() {
        boolean hasSelection = sensorTable.getSelectedRow() >= 0;
        editSensorButton.setEnabled(hasSelection);
        deleteSensorButton.setEnabled(hasSelection);
    }

    private void updateClientActionButtons() {
        boolean hasSelection = clientTable.getSelectedRow() >= 0;
        openClientButton.setEnabled(hasSelection);
        deleteClientButton.setEnabled(hasSelection);
    }

    private void updateStatusLabel() {
        statusLabel.setText(
                "Broker iniciado em " + EmbeddedBrokerManager.BROKER_URL +
                        " | Sensores: " + tableModel.getRowCount() +
                        " | Clientes: " + clientTableModel.getRowCount()
        );
    }

    private void stopBroker() {
        for (SensorMonitorFrame sensorWindow : sensorWindows.values()) {
            sensorWindow.dispose();
        }
        sensorWindows.clear();

        for (SensorRuntime sensorRuntime : sensorRuntimes.values()) {
            sensorRuntime.stopSimulation();
        }
        sensorRuntimes.clear();

        for (ClientMonitorFrame clientWindow : clientWindows.values()) {
            clientWindow.dispose();
        }
        clientWindows.clear();

        for (ClientRuntime clientRuntime : clientRuntimes.values()) {
            clientRuntime.close();
        }
        clientRuntimes.clear();

        try {
            brokerManager.stop();
        } catch (Exception exception) {
            // Silent shutdown to avoid blocking window closing.
        }
    }
}

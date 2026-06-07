package mom;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class SensorDashboardFrame extends JFrame {
    private final EmbeddedBrokerManager brokerManager = new EmbeddedBrokerManager();
    private final SensorFactory sensorFactory = new SensorFactory();

    // instancia o publisher
    private final SensorAlertPublisher alertPublisher = new SensorAlertPublisher(EmbeddedBrokerManager.BROKER_URL);
    private final JLabel statusLabel = new JLabel("Inicializando broker...");
    private final SensorTableModel tableModel = new SensorTableModel();
    private final JButton createSensorButton = new JButton("Criar sensor");
    private final JButton editSensorButton = new JButton("Abrir sensor");
    private final JButton deleteSensorButton = new JButton("Excluir sensor");
    private final JTable sensorTable = new JTable(tableModel);
    private final Map<String, SensorRuntime> sensorRuntimes = new LinkedHashMap<>();
    private final Map<String, SensorMonitorFrame> sensorWindows = new LinkedHashMap<>();

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

        JLabel titleLabel = new JLabel("Hub de sensores");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));

        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

        sensorTable.setFillsViewportHeight(true);
        sensorTable.setRowHeight(24);
        sensorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sensorTable.getSelectionModel().addListSelectionListener(this::handleSelectionChange);

        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.add(titleLabel, BorderLayout.NORTH);
        header.add(statusLabel, BorderLayout.SOUTH);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionsPanel.add(createSensorButton);
        actionsPanel.add(editSensorButton);
        actionsPanel.add(deleteSensorButton);

        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.add(header, BorderLayout.NORTH);
        topPanel.add(actionsPanel, BorderLayout.SOUTH);

        createSensorButton.addActionListener(event -> openCreateSensorDialog());
        editSensorButton.addActionListener(event -> openSensorWindow());
        deleteSensorButton.addActionListener(event -> deleteSelectedSensor());

        updateActionButtons();

        root.add(topPanel, BorderLayout.NORTH);
        root.add(new JScrollPane(sensorTable), BorderLayout.CENTER);
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
        sensorRuntimes.put(createdSensor.getId(), sensorRuntime);
        showSensorWindow(sensorRuntime);
        updateStatusLabel();
        updateActionButtons();
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

        sensorRuntimes.remove(sensor.getId());
        tableModel.removeSensor(selectedRow);
        updateStatusLabel();
        updateActionButtons();
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

    private void handleSelectionChange(ListSelectionEvent event) {
        if (!event.getValueIsAdjusting()) {
            updateActionButtons();
        }
    }

    private void updateActionButtons() {
        boolean hasSelection = sensorTable.getSelectedRow() >= 0;
        editSensorButton.setEnabled(hasSelection);
        deleteSensorButton.setEnabled(hasSelection);
    }

    private void updateStatusLabel() {
        statusLabel.setText(
                "Broker iniciado em " + EmbeddedBrokerManager.BROKER_URL +
                        " | Sensores cadastrados: " + tableModel.getRowCount()
        );
    }

    private void stopBroker() {
        for (SensorMonitorFrame sensorWindow : sensorWindows.values()) {
            sensorWindow.dispose();
        }
        sensorWindows.clear();

        try {
            brokerManager.stop();
        } catch (Exception exception) {
            // Silent shutdown to avoid blocking window closing.
        }
    }
}

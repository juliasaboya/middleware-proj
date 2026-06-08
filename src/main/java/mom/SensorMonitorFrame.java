package mom;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Locale;

public class SensorMonitorFrame extends JFrame {
    private final SensorRuntime sensorRuntime;
    private final Runnable onSensorUpdated;
    private final Runnable runtimeListener = this::handleRuntimeUpdated;
    private final JLabel statusLabel = new JLabel();
    private final JLabel simulationLabel = new JLabel();
    private final JTextField currentValueField = new JTextField(14);
    private final JTextField minimumField = new JTextField(14);
    private final JTextField maximumField = new JTextField(14);
    private final JButton simulationToggleButton = new JButton();

    public SensorMonitorFrame(SensorRuntime sensorRuntime, Runnable onSensorUpdated) {
        super("Sensor " + sensorRuntime.getSensor().getId());
        this.sensorRuntime = sensorRuntime;
        this.onSensorUpdated = onSensorUpdated;
        this.sensorRuntime.addStateListener(runtimeListener);
        configureFrame();
        buildUi();
        refreshFields();
    }

    public void focusWindow() {
        setVisible(true);
        toFront();
        requestFocus();
    }

    private void configureFrame() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(560, 420);
        setLocationByPlatform(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent event) {
                sensorRuntime.removeStateListener(runtimeListener);
            }
        });
    }

    private void buildUi() {
        Sensor sensor = sensorRuntime.getSensor();

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel titleLabel = new JLabel("Monitor do sensor " + sensor.getId());
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 6, 6, 6);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;

        int row = 0;
        addField(infoPanel, constraints, row++, "Tipo", new JLabel(sensor.getType().getDisplayName()));
        addField(infoPanel, constraints, row++, "ID", new JLabel(sensor.getId()));
        addField(infoPanel, constraints, row++, "Topico", new JLabel("sensor." + sensor.getId()));
        addField(infoPanel, constraints, row++, "Valor atual", currentValueField);
        addField(infoPanel, constraints, row++, "Minimo", minimumField);
        addField(infoPanel, constraints, row++, "Maximo", maximumField);
        addField(infoPanel, constraints, row, "Unidade", new JLabel(sensor.getType().getUnit()));

        JList<String> historyList = new JList<>(sensorRuntime.getHistoryModel());
        JScrollPane historyScrollPane = new JScrollPane(historyList);
        historyScrollPane.setBorder(BorderFactory.createTitledBorder("Historico"));

        JButton saveButton = new JButton("Salvar leitura");
        saveButton.addActionListener(event -> saveChanges());
        simulationToggleButton.addActionListener(event -> toggleSimulation());

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.add(simulationToggleButton);
        actionPanel.add(saveButton);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 8));
        JPanel statusPanel = new JPanel(new BorderLayout(0, 4));
        statusPanel.add(simulationLabel, BorderLayout.NORTH);
        statusPanel.add(statusLabel, BorderLayout.SOUTH);
        bottomPanel.add(statusPanel, BorderLayout.NORTH);
        bottomPanel.add(actionPanel, BorderLayout.SOUTH);

        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(infoPanel, BorderLayout.CENTER);

        root.add(topPanel, BorderLayout.NORTH);
        root.add(historyScrollPane, BorderLayout.CENTER);
        root.add(bottomPanel, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void addField(JPanel panel, GridBagConstraints constraints, int row, String labelText, java.awt.Component component) {
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 0;
        panel.add(new JLabel(labelText), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        panel.add(component, constraints);
    }

    private void refreshFields() {
        Sensor sensor = sensorRuntime.getSensor();
        currentValueField.setText(format(sensor.getCurrentValue()));
        minimumField.setText(format(sensor.getMinimum()));
        maximumField.setText(format(sensor.getMaximum()));
        statusLabel.setText(sensorRuntime.getLastStatusMessage());
        simulationLabel.setText(buildSimulationLabel(sensor));
        simulationToggleButton.setText(sensorRuntime.isSimulationRunning() ? "Pausar simulacao" : "Retomar simulacao");
    }

    private void saveChanges() {
        try {
            double currentValue = parseNumber(currentValueField.getText());
            double minimum = parseNumber(minimumField.getText());
            double maximum = parseNumber(maximumField.getText());

            if (minimum > maximum) {
                showValidationError("O minimo nao pode ser maior que o maximo.");
                return;
            }

            String statusMessage = sensorRuntime.applyReadingUpdate(currentValue, minimum, maximum);
            statusLabel.setText(statusMessage);
            onSensorUpdated.run();
        } catch (NumberFormatException exception) {
            showValidationError("Use apenas numeros validos nos campos numericos.");
        }
    }

    private void toggleSimulation() {
        if (sensorRuntime.isSimulationRunning()) {
            sensorRuntime.stopSimulation();
        } else {
            sensorRuntime.startSimulation();
        }
    }

    private void handleRuntimeUpdated() {
        refreshFields();
        onSensorUpdated.run();
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validacao", JOptionPane.WARNING_MESSAGE);
    }

    private String format(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private double parseNumber(String value) {
        return Double.parseDouble(value.trim().replace(',', '.'));
    }

    private String buildSimulationLabel(Sensor sensor) {
        return "Simulacao: " +
                (sensorRuntime.isSimulationRunning() ? "ativa" : "pausada") +
                " | faixa automatica=[" +
                format(sensor.getType().getSimulatedMinimum()) +
                ", " +
                format(sensor.getType().getSimulatedMaximum()) +
                "]";
    }
}

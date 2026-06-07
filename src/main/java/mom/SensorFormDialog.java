package mom;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Locale;

public class SensorFormDialog extends JDialog {
    private final SensorFactory sensorFactory;
    private final List<Sensor> existingSensors;
    private final Sensor editingSensor;
    private final JComboBox<SensorType> typeComboBox = new JComboBox<>(SensorType.values());
    private final JTextField idField = new JTextField(16);
    private final JTextField currentValueField = new JTextField(16);
    private final JTextField minimumField = new JTextField(16);
    private final JTextField maximumField = new JTextField(16);
    private final JLabel unitValueLabel = new JLabel();

    private Sensor createdSensor;
    private String lastSuggestedId = "";
    private boolean updatingForm;

    public SensorFormDialog(JFrame owner, SensorFactory sensorFactory, List<Sensor> existingSensors) {
        this(owner, sensorFactory, existingSensors, null);
    }

    public SensorFormDialog(JFrame owner, SensorFactory sensorFactory, List<Sensor> existingSensors, Sensor editingSensor) {
        super(owner, editingSensor == null ? "Criar sensor" : "Editar sensor", true);
        this.sensorFactory = sensorFactory;
        this.existingSensors = existingSensors;
        this.editingSensor = editingSensor;
        buildUi();

        if (editingSensor == null) {
            applyDefaults((SensorType) typeComboBox.getSelectedItem());
        } else {
            populateForm(editingSensor);
        }

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    public Sensor getCreatedSensor() {
        return createdSensor;
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 6, 6, 6);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addField(formPanel, constraints, row++, "Tipo", typeComboBox);
        addField(formPanel, constraints, row++, "ID", idField);
        addField(formPanel, constraints, row++, "Valor atual", currentValueField);
        addField(formPanel, constraints, row++, "Minimo", minimumField);
        addField(formPanel, constraints, row++, "Maximo", maximumField);
        addField(formPanel, constraints, row, "Unidade", unitValueLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton cancelButton = new JButton("Cancelar");
        JButton saveButton = new JButton("Salvar");
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        typeComboBox.addActionListener(event -> {
            if (!updatingForm) {
                applyDefaults((SensorType) typeComboBox.getSelectedItem());
            }
        });
        cancelButton.addActionListener(event -> dispose());
        saveButton.addActionListener(event -> saveSensor());
        getRootPane().setDefaultButton(saveButton);

        root.add(formPanel, BorderLayout.CENTER);
        root.add(buttonPanel, BorderLayout.SOUTH);
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

    private void applyDefaults(SensorType type) {
        if (type == null) {
            return;
        }

        String suggestedId = sensorFactory.createSuggestedId(type, existingSensors);
        if (idField.getText().isBlank() || idField.getText().equals(lastSuggestedId)) {
            idField.setText(suggestedId);
        }
        lastSuggestedId = suggestedId;

        minimumField.setText(format(type.getDefaultMinimum()));
        maximumField.setText(format(type.getDefaultMaximum()));
        currentValueField.setText(format((type.getDefaultMinimum() + type.getDefaultMaximum()) / 2.0));
        unitValueLabel.setText(type.getUnit());
    }

    private void populateForm(Sensor sensor) {
        updatingForm = true;
        try {
            typeComboBox.setSelectedItem(sensor.getType());
            idField.setText(sensor.getId());
            currentValueField.setText(format(sensor.getCurrentValue()));
            minimumField.setText(format(sensor.getMinimum()));
            maximumField.setText(format(sensor.getMaximum()));
            unitValueLabel.setText(sensor.getType().getUnit());
            lastSuggestedId = sensor.getId();
        } finally {
            updatingForm = false;
        }
    }

    private void saveSensor() {
        SensorType type = (SensorType) typeComboBox.getSelectedItem();
        String id = idField.getText().trim();

        if (type == null) {
            showValidationError("Selecione um tipo de sensor.");
            return;
        }

        if (id.isEmpty()) {
            showValidationError("Informe um ID para o sensor.");
            return;
        }

        if (existingSensors.stream().anyMatch(sensor -> sensor != editingSensor && sensor.getId().equalsIgnoreCase(id))) {
            showValidationError("Ja existe um sensor com esse ID.");
            return;
        }

        try {
            double currentValue = parseNumber(currentValueField.getText());
            double minimum = parseNumber(minimumField.getText());
            double maximum = parseNumber(maximumField.getText());

            if (minimum > maximum) {
                showValidationError("O minimo nao pode ser maior que o maximo.");
                return;
            }

            if (currentValue < minimum || currentValue > maximum) {
                showValidationError("O valor atual deve estar entre o minimo e o maximo.");
                return;
            }

            createdSensor = new Sensor(type, id, currentValue, minimum, maximum);
            dispose();
        } catch (NumberFormatException exception) {
            showValidationError("Use apenas numeros validos nos campos numericos.");
        }
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
}

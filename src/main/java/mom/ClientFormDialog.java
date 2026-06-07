package mom;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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

public class ClientFormDialog extends JDialog {
    private final List<Client> existingClients;
    private final JTextField idField = new JTextField(16);

    private Client createdClient;

    public ClientFormDialog(JFrame owner, ClientFactory clientFactory, List<Client> existingClients) {
        super(owner, "Criar cliente", true);
        this.existingClients = existingClients;
        buildUi(clientFactory);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    public Client getCreatedClient() {
        return createdClient;
    }

    private void buildUi(ClientFactory clientFactory) {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 6, 6, 6);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0;
        formPanel.add(new JLabel("ID"), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        idField.setText(clientFactory.createSuggestedId(existingClients));
        formPanel.add(idField, constraints);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton cancelButton = new JButton("Cancelar");
        JButton saveButton = new JButton("Salvar");
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        cancelButton.addActionListener(event -> dispose());
        saveButton.addActionListener(event -> saveClient());
        getRootPane().setDefaultButton(saveButton);

        root.add(formPanel, BorderLayout.CENTER);
        root.add(buttonPanel, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void saveClient() {
        String id = idField.getText().trim();
        if (id.isEmpty()) {
            showValidationError("Informe um ID para o cliente.");
            return;
        }

        if (existingClients.stream().anyMatch(client -> client.getId().equalsIgnoreCase(id))) {
            showValidationError("Ja existe um cliente com esse ID.");
            return;
        }

        createdClient = new Client(id);
        dispose();
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validacao", JOptionPane.WARNING_MESSAGE);
    }
}

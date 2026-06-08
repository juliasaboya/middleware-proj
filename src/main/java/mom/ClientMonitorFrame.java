package mom;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class ClientMonitorFrame extends JFrame {
    private static final Color SUBSCRIBED_COLOR = new Color(0, 128, 0);

    private final ClientRuntime clientRuntime;
    private final Supplier<List<String>> availableTopicsSupplier;
    private final Runnable onClientUpdated;
    private final JLabel statusLabel = new JLabel();
    private final DefaultTopicListModel availableTopicsModel = new DefaultTopicListModel();
    private final JList<String> availableTopicsList = new JList<>(availableTopicsModel);
    private final Set<String> checkedTopics = new LinkedHashSet<>();
    private boolean checklistInitialized;

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
        checkedTopics.retainAll(new LinkedHashSet<>(availableTopics));

        if (!checklistInitialized) {
            checkedTopics.addAll(clientRuntime.getSubscribedTopics());
            checklistInitialized = true;
        }

        availableTopicsList.repaint();
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

        availableTopicsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        availableTopicsList.setCellRenderer(new TopicChecklistRenderer());
        availableTopicsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                toggleTopicAt(availableTopicsList.locationToIndex(event.getPoint()));
            }
        });
        availableTopicsList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_SPACE) {
                    toggleTopicAt(availableTopicsList.getSelectedIndex());
                    event.consume();
                }
            }
        });
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
            clientRuntime.updateSubscriptions(List.copyOf(checkedTopics));
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Nao foi possivel atualizar as assinaturas.\n" + exception.getMessage(),
                    "Erro de assinatura",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void toggleTopicAt(int index) {
        if (index < 0 || index >= availableTopicsModel.getSize()) {
            return;
        }

        String topic = availableTopicsModel.getElementAt(index);
        if (checkedTopics.contains(topic)) {
            checkedTopics.remove(topic);
        } else {
            checkedTopics.add(topic);
        }

        availableTopicsList.setSelectedIndex(index);
        availableTopicsList.repaint();
    }

    private void refreshStatus() {
        statusLabel.setText(clientRuntime.getLastStatusMessage());
    }

    private void handleRuntimeUpdated() {
        checkedTopics.clear();
        checkedTopics.addAll(clientRuntime.getSubscribedTopics());
        checkedTopics.retainAll(new LinkedHashSet<>(availableTopicsModel.getTopics()));
        checklistInitialized = true;
        availableTopicsList.repaint();
        refreshStatus();
        onClientUpdated.run();
    }

    private class TopicChecklistRenderer extends JCheckBox implements javax.swing.ListCellRenderer<String> {
        @Override
        public Component getListCellRendererComponent(
                JList<? extends String> list,
                String value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            boolean subscribed = clientRuntime.getSubscribedTopics().contains(value);

            setText(value);
            setSelected(checkedTopics.contains(value));
            setOpaque(true);
            setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            setForeground(subscribed ? SUBSCRIBED_COLOR : list.getForeground());
            setFont(list.getFont().deriveFont(subscribed ? Font.BOLD : Font.PLAIN));
            setBorderPainted(true);
            return this;
        }
    }
}

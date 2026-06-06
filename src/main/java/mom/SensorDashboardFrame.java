package mom;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class SensorDashboardFrame extends JFrame {
    private final EmbeddedBrokerManager brokerManager = new EmbeddedBrokerManager();
    private final SensorFactory sensorFactory = new SensorFactory();
    private final JLabel statusLabel = new JLabel("Inicializando broker...");
    private final SensorTableModel tableModel = new SensorTableModel();

    public SensorDashboardFrame() {
        super("Middleware Orientado a Mensagens");
        configureFrame();
        buildUi();
        initializeApplication();
    }

    private void configureFrame() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(860, 420);
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

        JLabel titleLabel = new JLabel("Broker local e sensores ativos");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));

        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JTable sensorTable = new JTable(tableModel);
        sensorTable.setFillsViewportHeight(true);
        sensorTable.setRowHeight(24);

        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.add(titleLabel, BorderLayout.NORTH);
        header.add(statusLabel, BorderLayout.SOUTH);

        root.add(header, BorderLayout.NORTH);
        root.add(new JScrollPane(sensorTable), BorderLayout.CENTER);
        setContentPane(root);
    }

    private void initializeApplication() {
        try {
            brokerManager.start();
            List<Sensor> sensors = sensorFactory.createRandomSensors(10);
            tableModel.setSensors(sensors);
            statusLabel.setText("Broker iniciado em " + EmbeddedBrokerManager.BROKER_URL + " | Sensores carregados: " + sensors.size());
        } catch (Exception exception) {
            statusLabel.setText("Falha ao iniciar broker");
            JOptionPane.showMessageDialog(
                    this,
                    "Nao foi possivel iniciar o broker ActiveMQ.\n" + exception.getMessage(),
                    "Erro de inicializacao",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void stopBroker() {
        try {
            brokerManager.stop();
        } catch (Exception exception) {
            // Silent shutdown to avoid blocking window closing.
        }
    }
}

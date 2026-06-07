package mom;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            // pra qu serve isso?
            configureLookAndFeel();

            // inicializa a UI do hub
            SensorDashboardFrame frame = new SensorDashboardFrame();
            frame.setVisible(true);
        });
    }

    private static void configureLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Keeps the application running even if the native look and feel is unavailable.
        }
    }
}

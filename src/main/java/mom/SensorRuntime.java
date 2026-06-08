package mom;

import javax.swing.DefaultListModel;
import javax.swing.Timer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class SensorRuntime {
    private static final DateTimeFormatter HISTORY_FORMAT = DateTimeFormatter.ofPattern("dd/MM HH:mm:ss");
    private static final int SIMULATION_INTERVAL_MS = 2000;

    private final Sensor sensor;
    private final SensorAlertPublisher alertPublisher;
    private final DefaultListModel<String> historyModel = new DefaultListModel<>();
    private final List<Runnable> stateListeners = new ArrayList<>();
    private final Timer simulationTimer;

    private String lastStatusMessage = "Leitura dentro da faixa configurada.";
    private String lastAlertType;

    public SensorRuntime(Sensor sensor, SensorAlertPublisher alertPublisher) {
        this.sensor = sensor;
        this.alertPublisher = alertPublisher;
        this.simulationTimer = new Timer(SIMULATION_INTERVAL_MS, event -> applySimulatedReading());
        addHistoryEntry("Sensor criado");
        lastStatusMessage = publishThresholdAlertIfNeeded();
        startSimulation();
    }

    public Sensor getSensor() {
        return sensor;
    }

    public DefaultListModel<String> getHistoryModel() {
        return historyModel;
    }

    public String getLastStatusMessage() {
        return lastStatusMessage;
    }

    public boolean isSimulationRunning() {
        return simulationTimer.isRunning();
    }

    public void startSimulation() {
        if (!simulationTimer.isRunning()) {
            simulationTimer.start();
            addHistoryDetail("Simulacao automatica iniciada");
            notifyStateListeners();
        }
    }

    public void stopSimulation() {
        if (simulationTimer.isRunning()) {
            simulationTimer.stop();
            addHistoryDetail("Simulacao automatica pausada");
            notifyStateListeners();
        }
    }

    public void addStateListener(Runnable listener) {
        stateListeners.add(listener);
    }

    public void removeStateListener(Runnable listener) {
        stateListeners.remove(listener);
    }

    public String applyReadingUpdate(double currentValue, double minimum, double maximum) {
        sensor.setCurrentValue(currentValue);
        sensor.setMinimum(minimum);
        sensor.setMaximum(maximum);

        addHistoryEntry("Parametros atualizados");
        lastStatusMessage = publishThresholdAlertIfNeeded();
        notifyStateListeners();
        return lastStatusMessage;
    }

    private String publishThresholdAlertIfNeeded() {
        String alertType = resolveAlertType();
        if (alertType == null) {
            lastAlertType = null;
            return "Leitura dentro da faixa configurada.";
        }

        if (alertType.equals(lastAlertType)) {
            return "Leitura fora da faixa configurada.";
        }

        try {
            String topicName = alertPublisher.publishLimitAlert(sensor, alertType);
            String detail = "MAXIMUM_REACHED".equals(alertType) ? "maximo atingido" : "minimo atingido";
            String statusMessage = "Alerta enviado para o topico " + topicName + ".";
            addHistoryDetail("Broker notificado: " + detail);
            lastAlertType = alertType;
            return statusMessage;
        } catch (Exception exception) {
            String statusMessage = "Falha ao publicar alerta: " + exception.getMessage();
            addHistoryDetail(statusMessage);
            lastAlertType = alertType;
            return statusMessage;
        }
    }

    private String resolveAlertType() {
        if (sensor.getCurrentValue() < sensor.getMinimum()) {
            return "MINIMUM_REACHED";
        }

        if (sensor.getCurrentValue() > sensor.getMaximum()) {
            return "MAXIMUM_REACHED";
        }

        return null;
    }

    private void addHistoryEntry(String action) {
        historyModel.add(0, String.format(
                Locale.US,
                "%s | %s | valor=%.2f %s | faixa=[%.2f, %.2f]",
                HISTORY_FORMAT.format(LocalDateTime.now()),
                action,
                sensor.getCurrentValue(),
                sensor.getType().getUnit(),
                sensor.getMinimum(),
                sensor.getMaximum()
        ));
    }

    private void addHistoryDetail(String detail) {
        historyModel.add(0, HISTORY_FORMAT.format(LocalDateTime.now()) + " | " + detail);
    }

    private void applySimulatedReading() {
        SensorType type = sensor.getType();
        double simulatedValue = round(ThreadLocalRandom.current().nextDouble(
                type.getSimulatedMinimum(),
                type.getSimulatedMaximum()
        ));

        sensor.setCurrentValue(simulatedValue);
        addHistoryEntry("Leitura automatica");
        lastStatusMessage = publishThresholdAlertIfNeeded();
        notifyStateListeners();
    }

    private void notifyStateListeners() {
        for (Runnable listener : List.copyOf(stateListeners)) {
            listener.run();
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

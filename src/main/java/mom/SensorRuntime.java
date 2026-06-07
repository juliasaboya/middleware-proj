package mom;

import javax.swing.DefaultListModel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class SensorRuntime {
    private static final DateTimeFormatter HISTORY_FORMAT = DateTimeFormatter.ofPattern("dd/MM HH:mm:ss");

    private final Sensor sensor;
    private final SensorAlertPublisher alertPublisher;
    private final DefaultListModel<String> historyModel = new DefaultListModel<>();

    private String lastStatusMessage = "Leitura dentro da faixa configurada.";

    public SensorRuntime(Sensor sensor, SensorAlertPublisher alertPublisher) {
        this.sensor = sensor;
        this.alertPublisher = alertPublisher;
        addHistoryEntry("Sensor criado");
        lastStatusMessage = publishThresholdAlertIfNeeded();
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

    public String applyReadingUpdate(double currentValue, double minimum, double maximum) {
        sensor.setCurrentValue(currentValue);
        sensor.setMinimum(minimum);
        sensor.setMaximum(maximum);

        addHistoryEntry("Parametros atualizados");
        lastStatusMessage = publishThresholdAlertIfNeeded();
        return lastStatusMessage;
    }

    private String publishThresholdAlertIfNeeded() {
        String alertType = resolveAlertType();
        if (alertType == null) {
            return "Leitura dentro da faixa configurada.";
        }

        try {
            String topicName = alertPublisher.publishLimitAlert(sensor, alertType);
            String detail = "MAXIMUM_REACHED".equals(alertType) ? "maximo atingido" : "minimo atingido";
            String statusMessage = "Alerta enviado para o topico " + topicName + ".";
            addHistoryDetail("Broker notificado: " + detail);
            return statusMessage;
        } catch (Exception exception) {
            String statusMessage = "Falha ao publicar alerta: " + exception.getMessage();
            addHistoryDetail(statusMessage);
            return statusMessage;
        }
    }

    private String resolveAlertType() {
        if (sensor.getCurrentValue() == sensor.getMinimum()) {
            return "MINIMUM_REACHED";
        }

        if (sensor.getCurrentValue() == sensor.getMaximum()) {
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
}

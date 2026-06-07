package mom;

import java.util.Collection;

public class SensorFactory {

    // reduz duplicação de regra p sugestão de ID com base nos ja criados. (serve p sensores de todos os tipos)
    public String createSuggestedId(SensorType type, Collection<Sensor> existingSensors) {
        int highestIndex = 0;

        for (Sensor sensor : existingSensors) {
            if (sensor.getType() != type) {
                continue;
            }

            String sensorId = sensor.getId();
            if (!sensorId.startsWith(type.getIdPrefix())) {
                continue;
            }

            String suffix = sensorId.substring(type.getIdPrefix().length());
            if (!suffix.matches("\\d+")) {
                continue;
            }

            highestIndex = Math.max(highestIndex, Integer.parseInt(suffix));
        }

        return type.getIdPrefix() + String.format("%03d", highestIndex + 1);
    }
}

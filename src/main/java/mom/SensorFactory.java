package mom;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class SensorFactory {
    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    public List<Sensor> createRandomSensors(int amount) {
        List<Sensor> sensors = new ArrayList<>(amount);
        Map<SensorType, Integer> counters = new EnumMap<>(SensorType.class);

        for (SensorType sensorType : SensorType.values()) {
            counters.put(sensorType, 0);
        }

        SensorType[] sensorTypes = SensorType.values();
        for (int i = 0; i < amount; i++) {
            SensorType type = sensorTypes[random.nextInt(sensorTypes.length)];
            int nextIndex = counters.merge(type, 1, Integer::sum);
            double value = random.nextDouble(type.getDefaultMinimum(), type.getDefaultMaximum());

            sensors.add(new Sensor(
                    type,
                    type.getIdPrefix() + String.format("%03d", nextIndex),
                    round(value),
                    type.getDefaultMinimum(),
                    type.getDefaultMaximum()
            ));
        }

        return sensors;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

package mom;

public class Sensor {
    private final SensorType type;
    private final String id;
    private double currentValue;
    private double minimum;
    private double maximum;

    public Sensor(SensorType type, String id, double currentValue, double minimum, double maximum) {
        this.type = type;
        this.id = id;
        this.currentValue = currentValue;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public SensorType getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public double getMinimum() {
        return minimum;
    }

    public void setMinimum(double minimum) {
        this.minimum = minimum;
    }

    public double getMaximum() {
        return maximum;
    }

    public void setMaximum(double maximum) {
        this.maximum = maximum;
    }
}

package mom;

public enum SensorType {
    TEMPERATURA("Temperatura", "temp", -10.0, 50.0, "C", -15.0, 60.0),
    UMIDADE("Umidade", "umid", 20.0, 95.0, "%", 15.0, 100.0),
    VELOCIDADE("Velocidade", "veloc", 0.0, 120.0, "km/h", 0.0, 130.0);

    private final String displayName;
    private final String idPrefix;
    private final double defaultMinimum;
    private final double defaultMaximum;
    private final String unit;
    private final double simulatedMinimum;
    private final double simulatedMaximum;

    // define o objeto SensorType
    SensorType(
            String displayName,
            String idPrefix,
            double defaultMinimum,
            double defaultMaximum,
            String unit,
            double simulatedMinimum,
            double simulatedMaximum
    ) {
        this.displayName = displayName;
        this.idPrefix = idPrefix;
        this.defaultMinimum = defaultMinimum;
        this.defaultMaximum = defaultMaximum;
        this.unit = unit;
        this.simulatedMinimum = simulatedMinimum;
        this.simulatedMaximum = simulatedMaximum;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIdPrefix() {
        return idPrefix;
    }

    public double getDefaultMinimum() {
        return defaultMinimum;
    }

    public double getDefaultMaximum() {
        return defaultMaximum;
    }

    public String getUnit() {
        return unit;
    }

    public double getSimulatedMinimum() {
        return simulatedMinimum;
    }

    public double getSimulatedMaximum() {
        return simulatedMaximum;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

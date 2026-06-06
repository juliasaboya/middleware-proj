package mom;

public enum SensorType {
    TEMPERATURA("Temperatura", "temp", -10.0, 50.0, "C"),
    UMIDADE("Umidade", "umid", 20.0, 95.0, "%"),
    VELOCIDADE("Velocidade", "veloc", 0.0, 120.0, "km/h");

    private final String displayName;
    private final String idPrefix;
    private final double defaultMinimum;
    private final double defaultMaximum;
    private final String unit;

    SensorType(String displayName, String idPrefix, double defaultMinimum, double defaultMaximum, String unit) {
        this.displayName = displayName;
        this.idPrefix = idPrefix;
        this.defaultMinimum = defaultMinimum;
        this.defaultMaximum = defaultMaximum;
        this.unit = unit;
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
}

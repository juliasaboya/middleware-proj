package mom;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// representa a base de dados dos sensores e é
// usada para alertar a UI quando dados mudarem.
public class SensorTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {"Tipo", "ID", "Valor Atual", "Minimo", "Maximo", "Unidade"};

    private List<Sensor> sensors = new ArrayList<>();

    public void setSensors(List<Sensor> sensors) {
        this.sensors = new ArrayList<>(sensors);
        fireTableDataChanged();
    }

    public void addSensor(Sensor sensor) {
        int rowIndex = sensors.size();
        sensors.add(sensor);
        fireTableRowsInserted(rowIndex, rowIndex);
    }

    public Sensor getSensorAt(int rowIndex) {
        return sensors.get(rowIndex);
    }

    public void updateSensor(int rowIndex, Sensor sensor) {
        sensors.set(rowIndex, sensor);
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    public void notifySensorUpdated(Sensor sensor) {
        int rowIndex = sensors.indexOf(sensor);
        if (rowIndex >= 0) {
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }

    public void removeSensor(int rowIndex) {
        sensors.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    public List<Sensor> getSensors() {
        return Collections.unmodifiableList(sensors);
    }

    @Override
    public int getRowCount() {
        return sensors.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Sensor sensor = sensors.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> sensor.getType().getDisplayName();
            case 1 -> sensor.getId();
            case 2 -> sensor.getCurrentValue();
            case 3 -> sensor.getMinimum();
            case 4 -> sensor.getMaximum();
            case 5 -> sensor.getType().getUnit();
            default -> "";
        };
    }
}

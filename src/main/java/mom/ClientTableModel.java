package mom;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {"ID", "Topicos Assinados", "Mensagens Recebidas"};

    private final List<Client> clients = new ArrayList<>();

    public void addClient(Client client) {
        int rowIndex = clients.size();
        clients.add(client);
        fireTableRowsInserted(rowIndex, rowIndex);
    }

    public Client getClientAt(int rowIndex) {
        return clients.get(rowIndex);
    }

    public void removeClient(int rowIndex) {
        clients.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    public void notifyClientUpdated(Client client) {
        int rowIndex = clients.indexOf(client);
        if (rowIndex >= 0) {
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }

    public List<Client> getClients() {
        return Collections.unmodifiableList(clients);
    }

    @Override
    public int getRowCount() {
        return clients.size();
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
        Client client = clients.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> client.getId();
            case 1 -> client.getSubscribedTopicCount();
            case 2 -> client.getReceivedMessageCount();
            default -> "";
        };
    }
}

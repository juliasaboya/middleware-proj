package mom;

import java.util.Collection;

public class ClientFactory {
    public String createSuggestedId(Collection<Client> existingClients) {
        int highestIndex = 0;

        for (Client client : existingClients) {
            String clientId = client.getId();
            if (!clientId.startsWith("client")) {
                continue;
            }

            String suffix = clientId.substring("client".length());
            if (!suffix.matches("\\d+")) {
                continue;
            }

            highestIndex = Math.max(highestIndex, Integer.parseInt(suffix));
        }

        return "client" + String.format("%03d", highestIndex + 1);
    }
}

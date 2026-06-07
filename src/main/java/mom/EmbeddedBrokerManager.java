package mom;

import org.apache.activemq.broker.BrokerService;

public class EmbeddedBrokerManager {
    public static final String BROKER_URL = "tcp://localhost:61616";

    private BrokerService brokerService;

    public synchronized void start() throws Exception {
        if (brokerService != null && brokerService.isStarted()) {
            return;
        }
        // cria o broker
        brokerService = new BrokerService();

        // configurando o broker
        brokerService.setBrokerName("sensor-broker");
        brokerService.setPersistent(false);
        brokerService.setUseJmx(false);
        brokerService.addConnector(BROKER_URL);

        // start no broker
        brokerService.start();
        brokerService.waitUntilStarted();
    }

    public synchronized void stop() throws Exception {
        if (brokerService == null) {
            return;
        }

        brokerService.stop();
        brokerService.waitUntilStopped();

        // matou o  broker
        brokerService = null;
    }

    public synchronized boolean isRunning() {
        return brokerService != null && brokerService.isStarted();
    }
}

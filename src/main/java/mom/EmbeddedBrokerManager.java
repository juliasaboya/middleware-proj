package mom;

import org.apache.activemq.broker.BrokerService;

public class EmbeddedBrokerManager {
    public static final String BROKER_URL = "tcp://localhost:61616";

    private BrokerService brokerService;

    public synchronized void start() throws Exception {
        if (brokerService != null && brokerService.isStarted()) {
            return;
        }

        brokerService = new BrokerService();
        brokerService.setBrokerName("sensor-broker");
        brokerService.setPersistent(false);
        brokerService.setUseJmx(false);
        brokerService.addConnector(BROKER_URL);
        brokerService.start();
        brokerService.waitUntilStarted();
    }

    public synchronized void stop() throws Exception {
        if (brokerService == null) {
            return;
        }

        brokerService.stop();
        brokerService.waitUntilStopped();
        brokerService = null;
    }

    public synchronized boolean isRunning() {
        return brokerService != null && brokerService.isStarted();
    }
}

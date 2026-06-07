package mom;

public class Client {
    private final String id;
    private int subscribedTopicCount;
    private int receivedMessageCount;

    public Client(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public int getSubscribedTopicCount() {
        return subscribedTopicCount;
    }

    public void setSubscribedTopicCount(int subscribedTopicCount) {
        this.subscribedTopicCount = subscribedTopicCount;
    }

    public int getReceivedMessageCount() {
        return receivedMessageCount;
    }

    public void setReceivedMessageCount(int receivedMessageCount) {
        this.receivedMessageCount = receivedMessageCount;
    }
}

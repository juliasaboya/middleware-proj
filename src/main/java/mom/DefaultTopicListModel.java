package mom;

import javax.swing.AbstractListModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultTopicListModel extends AbstractListModel<String> {
    private final List<String> topics = new ArrayList<>();

    public void setTopics(List<String> updatedTopics) {
        topics.clear();
        topics.addAll(updatedTopics);
        fireContentsChanged(this, 0, Math.max(0, topics.size() - 1));
    }

    @Override
    public int getSize() {
        return topics.size();
    }

    public List<String> getTopics() {
        return Collections.unmodifiableList(topics);
    }

    @Override
    public String getElementAt(int index) {
        return topics.get(index);
    }
}

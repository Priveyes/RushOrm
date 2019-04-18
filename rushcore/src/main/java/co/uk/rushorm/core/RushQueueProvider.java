package co.uk.rushorm.core;

/**
 * Created by Stuart on 10/12/14.
 */
public interface RushQueueProvider {

    interface RushQueCallback {
        void callback(RushQueue rushQueue);
    }

    RushQueue blockForNextQueue();
    void waitForNextQue(RushQueCallback rushQueCallback);
    void queComplete(RushQueue rushQueue);
}

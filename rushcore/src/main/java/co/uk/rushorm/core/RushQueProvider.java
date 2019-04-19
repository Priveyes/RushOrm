package co.uk.rushorm.core;

/**
 * Created by Stuart on 10/12/14.
 */
public interface RushQueProvider {

    interface RushQueCallback {
        void callback(RushQue rushQue);
    }

    RushQue blockForNextQue();
    void waitForNextQue(RushQueCallback rushQueCallback);
    void queComplete(RushQue que);
}

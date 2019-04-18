package co.uk.rushorm.android;

import java.util.ArrayList;
import java.util.List;

import co.uk.rushorm.core.RushQueue;
import co.uk.rushorm.core.RushQueueProvider;

/**
 * Created by stuartc on 11/12/14.
 */
public class AndroidRushQueueProvider implements RushQueueProvider {

    private List<RushQueue> rushQueues = new ArrayList<>();
    private final Object syncToken = new Object();

    public AndroidRushQueueProvider() {
        rushQueues.add(new AndroidRushQueue());
    }
    
    @Override
    public RushQueue blockForNextQueue() {
        synchronized (syncToken) {
            while (rushQueues.size() < 1) {
                try {
                    syncToken.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return rushQueues.remove(0);
        }
    }
    
    @Override
    public void waitForNextQue(final RushQueCallback rushQueCallback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                rushQueCallback.callback(blockForNextQueue());
            }
        }).start();
    }

    @Override
    public void queComplete(RushQueue rushQueue) {
        synchronized (syncToken) {
            rushQueues.add(rushQueue);
            syncToken.notify();
        }
    }
}

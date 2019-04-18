package co.uk.rushorm.core;

import java.util.List;

/**
 * Created by Stuart on 10/12/14.
 */
public interface RushStatementRunner {

    interface ValuesCallback {
        boolean hasNext();
        List<String> next();
        void close();
    }

    void runRaw(String statement, RushQueue rushQueue);
    ValuesCallback runGet(String sql, RushQueue rushQueue);
    void startTransition(RushQueue rushQueue);
    void endTransition(RushQueue rushQueue);
    
    boolean isFirstRun();
    void initializeComplete(long version);
    boolean requiresUpgrade(long version, RushQueue rushQueue);

}

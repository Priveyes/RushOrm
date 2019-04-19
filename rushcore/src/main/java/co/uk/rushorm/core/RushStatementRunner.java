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

    void runRaw(String statement, RushQue que);
    ValuesCallback runGet(String sql, RushQue que);
    void startTransition(RushQue que);
    void endTransition(RushQue que);
    
    boolean isFirstRun();
    void initializeComplete(long version);
    boolean requiresUpgrade(long version, RushQue que);

}

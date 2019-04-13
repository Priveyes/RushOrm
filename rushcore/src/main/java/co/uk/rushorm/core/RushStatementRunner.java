package co.uk.rushorm.core;

import java.util.List;

/**
 * Created by Stuart on 10/12/14.
 */
public interface RushStatementRunner {

    public interface ValuesCallback {
        public boolean hasNext();
        public List<String> next();
        public void close();
    }

    public void runRaw(String statement, RushQue que);
    public ValuesCallback runGet(String sql, RushQue que);
    public void startTransition(RushQue que);
    public void endTransition(RushQue que);
    
    public boolean isFirstRun();
    public void initializeComplete(long version);
    public boolean requiresUpgrade(long version, RushQue que);

}

package co.uk.rushorm.core;

/**
 * Created by Stuart on 11/12/14.
 */
public interface Logger {

    void log(String message);
    void logSql(String sql);
    void logError(String error);
}

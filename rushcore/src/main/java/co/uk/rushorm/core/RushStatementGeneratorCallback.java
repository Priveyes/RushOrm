package co.uk.rushorm.core;

/**
 * Created by Stuart on 16/02/15.
 */
public interface RushStatementGeneratorCallback {
    void deleteStatement(String sql);
    RushMetaData getMetaData(Rush rush);
}

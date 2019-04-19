package co.uk.rushorm.core;

/**
 * Created by Stuart on 17/02/15.
 */
public interface RushSaveStatementGeneratorCallback extends RushStatementGeneratorCallback {

    void addRush(Rush rush, RushMetaData rushMetaData);
    void createdOrUpdateStatement(String sql);

}

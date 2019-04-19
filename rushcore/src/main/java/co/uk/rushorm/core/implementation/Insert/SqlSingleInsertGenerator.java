package co.uk.rushorm.core.implementation.Insert;

import java.util.*;

import co.uk.rushorm.core.*;
import co.uk.rushorm.core.implementation.*;

/**
 * Created by Stuart on 03/04/15.
 */
public class SqlSingleInsertGenerator implements RushSqlInsertGenerator {

    private final RushConfig rushConfig;

    public SqlSingleInsertGenerator(RushConfig rushConfig) {
        this.rushConfig = rushConfig;
    }

    @Override
    public void createManyJoins(Map<String, List<BasicJoin>> joinValues, RushSaveStatementGeneratorCallback saveCallback) {
        for (Map.Entry<String, List<BasicJoin>> entry : joinValues.entrySet()) {
            List<BasicJoin> values = entry.getValue();

            for (BasicJoin join : values) {
                StringBuilder columnsString = new StringBuilder();
                columnsString.append("('")
                        .append(join.getParent().getId())
                        .append("','")
                        .append(join.getChild().getId())
                        .append("')");

                String sql = String.format(RushSqlUtils.MULTIPLE_INSERT_JOIN_TEMPLATE, entry.getKey(),
                        columnsString.toString());
                saveCallback.createdOrUpdateStatement(sql);
            }
        }
    }

    @Override
    public void createOrUpdateObjects(Map<Class<? extends Rush>, List<BasicUpdate>> valuesMap, Map<Class<? extends Rush>, List<String>> columnsMap, Map<Class<? extends Rush>, AnnotationCache> annotationCache, RushSaveStatementGeneratorCallback saveCallback) {
        for (Map.Entry<Class<? extends Rush>, List<BasicUpdate>> entry : valuesMap.entrySet()) {

            StringBuilder columnsBuilder = new StringBuilder();
            columnsBuilder.append(RushSqlUtils.RUSH_ID)
                    .append(",")
                    .append(RushSqlUtils.RUSH_CREATED)
                    .append(",")
                    .append(RushSqlUtils.RUSH_UPDATED)
                    .append(",")
                    .append(RushSqlUtils.RUSH_VERSION)
                    .append(commaSeparated(columnsMap.get(entry.getKey())));

            String columns = columnsBuilder.toString();
            List<BasicUpdate> creates = entry.getValue();

            for (BasicUpdate update : creates) {
                RushMetaData rushMetaData = update.rushMetaData;
                rushMetaData.save();

                StringBuilder valuesString = new StringBuilder();
                valuesString.append("('")
                        .append(rushMetaData.getId())
                        .append("',")
                        .append(rushMetaData.getCreated())
                        .append(",")
                        .append(rushMetaData.getUpdated())
                        .append(",")
                        .append(rushMetaData.getVersion())
                        .append(commaSeparated(update.values))
                        .append(")");

                String sql = String.format(/*rushConfig.usingMySql() ? RushSqlUtils.MULTIPLE_INSERT_UPDATE_TEMPLATE_MYSQL :*/ RushSqlUtils.MULTIPLE_INSERT_UPDATE_TEMPLATE_SQLITE,
                        annotationCache.get(entry.getKey()).getTableName(),
                        columns,
                        valuesString.toString());

                saveCallback.createdOrUpdateStatement(sql);
            }
        }
    }


    private String commaSeparated(List<String> values) {
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            string.append(",")
                    .append(values.get(i));
        }
        return string.toString();
    }

}

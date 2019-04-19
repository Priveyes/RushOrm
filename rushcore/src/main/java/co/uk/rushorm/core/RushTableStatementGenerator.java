package co.uk.rushorm.core;

import java.util.List;
import java.util.Map;

/**
 * Created by stuartc on 11/12/14.
 */
public interface RushTableStatementGenerator {

    interface StatementCallback {
        void statementCreated(String statement);
    }

    void generateStatements(List<Class<? extends Rush>> classes, RushColumns rushColumns, StatementCallback statementCallback, Map<Class<? extends Rush>, AnnotationCache> annotationCache);

}

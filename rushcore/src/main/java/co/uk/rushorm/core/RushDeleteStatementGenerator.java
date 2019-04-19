package co.uk.rushorm.core;

import java.util.List;
import java.util.Map;

/**
 * Created by Stuart on 16/02/15.
 */
public interface RushDeleteStatementGenerator {

    interface Callback extends RushStatementGeneratorCallback{
        void removeRush(Rush rush);
        void deleteStatement(String sql);
    }

    void generateDelete(List<? extends Rush> objects, Map<Class<? extends Rush>, AnnotationCache> annotationCache, Callback deleteCallback);
    void generateDeleteAll(Class<? extends Rush> clazz, Map<Class<? extends Rush>, AnnotationCache> annotationCache, Callback deleteCallback);

}

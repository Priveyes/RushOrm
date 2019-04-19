package co.uk.rushorm.core;

import java.util.List;
import java.util.Map;

/**
 * Created by Stuart on 14/12/14.
 */
public interface RushClassLoader {

    interface LoadCallback {
        RushStatementRunner.ValuesCallback runStatement(String string);
        void didLoadObject(Rush rush, RushMetaData rushMetaData);
    }

    <T extends Rush> List<T> loadClasses(Class<T> clazz, RushColumns rushColumns, Map<Class<? extends Rush>, AnnotationCache> annotationCache, RushStatementRunner.ValuesCallback valuesCallback, LoadCallback callback);

}

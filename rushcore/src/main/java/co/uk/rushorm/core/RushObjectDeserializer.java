package co.uk.rushorm.core;

import java.util.List;
import java.util.Map;

/**
 * Created by Stuart on 18/02/15.
 */
public interface RushObjectDeserializer {

    interface Callback {
        void addRush(Rush rush, RushMetaData rushMetaData);
    }
    <T extends Rush> List<T> deserialize(String string, String idName, String versionName, RushColumns rushColumns, Map<Class<? extends Rush>, AnnotationCache> annotationCache, Class<T> clazz, Callback callback);

}

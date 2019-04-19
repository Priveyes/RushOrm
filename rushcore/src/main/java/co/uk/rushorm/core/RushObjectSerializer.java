package co.uk.rushorm.core;

import java.util.List;
import java.util.Map;

/**
 * Created by Stuart on 18/02/15.
 */
public interface RushObjectSerializer {

    interface Callback {
        RushMetaData getMetaData(Rush rush);
    }

    String serialize(List<? extends Rush> objects, String idName, String versionName, RushColumns rushColumns, Map<Class<? extends Rush>, AnnotationCache> annotationCache, Callback callback);

}

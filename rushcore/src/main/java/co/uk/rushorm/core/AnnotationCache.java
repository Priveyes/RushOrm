package co.uk.rushorm.core;

import java.util.List;
import java.util.Map;

/**
 * Created by Stuart on 03/03/15.
 */
public interface AnnotationCache {
    List<String> getFieldToIgnore();
    List<String> getDisableAutoDelete();
    Map<String, Class<? extends Rush>> getListsClasses();
    Map<String, Class<? extends List>> getListsTypes();
    String getSerializationName();
    String getTableName();
    boolean prefixTable();
}

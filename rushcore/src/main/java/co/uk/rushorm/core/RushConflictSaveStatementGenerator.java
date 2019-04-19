package co.uk.rushorm.core;

import java.util.List;
import java.util.Map;

/**
 * Created by Stuart on 17/02/15.
 */
public interface RushConflictSaveStatementGenerator {

    interface Callback extends RushSaveStatementGeneratorCallback {
        void conflictFound(RushConflict conflict);
        <T extends Rush> T load(Class T, String sql);
    }

    void conflictsFromGenerateSaveOrUpdate(List<? extends Rush> objects, Map<Class<? extends Rush>, AnnotationCache> annotationCache, RushStringSanitizer rushStringSanitizer, RushColumns rushColumns, Callback saveCallback);

}

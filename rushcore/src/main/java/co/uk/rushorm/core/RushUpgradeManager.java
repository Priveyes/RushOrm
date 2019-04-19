package co.uk.rushorm.core;

import java.util.List;
import java.util.Map;

/**
 * Created by Stuart on 10/12/14.
 */
public interface RushUpgradeManager {

    interface UpgradeCallback {
        RushStatementRunner.ValuesCallback runStatement(String sql);
        void runRaw(String sql);
        void createClasses(List<Class<? extends Rush>> missingClasses);
    }

    void upgrade(List<Class<? extends Rush>> classList, UpgradeCallback callback, Map<Class<? extends Rush>, AnnotationCache> annotationCache);

}

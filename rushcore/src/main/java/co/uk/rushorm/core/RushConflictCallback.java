package co.uk.rushorm.core;

import java.util.List;

/**
 * Created by Stuart on 17/02/15.
 */
public interface RushConflictCallback {
    void complete(List<RushConflict> conflicts);
}

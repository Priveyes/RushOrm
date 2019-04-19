package co.uk.rushorm.core.search;

import androidx.annotation.*;

/**
 * Created by Stuart on 22/03/15.
 */
public class RushOrderBy {

    private final String field;
    private final String order;
    public RushOrderBy(String field, String order) {
        this.field = field;
        this.order = order;
    }

    public String getStatement(){
        return field + " " + order;
    }

    @NonNull
    @Override
    public String toString() {
        return "{\"field\":\"" + field + "\"," +
                "\"order\":\"" + order + "\"}";
    }

}

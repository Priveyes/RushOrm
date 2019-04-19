package co.uk.rushorm.core;

import java.lang.reflect.Field;

/**
 * Created by Stuart on 06/01/15.
 */
public interface RushColumn<T> {

    String sqlColumnType();
    String serialize(T object, RushStringSanitizer stringSanitizer);
    T deserialize(String value);
    Class[] classesColumnSupports();


}

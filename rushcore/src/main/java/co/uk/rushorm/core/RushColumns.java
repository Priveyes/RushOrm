package co.uk.rushorm.core;

import java.lang.reflect.Field;

/**
 * Created by Stuart on 06/01/15.
 */
public interface RushColumns {

    boolean supportsField(Field field);
    String sqlColumnType(Field field);
    String valueFromField(Rush rush, Field field, RushStringSanitizer stringSanitizer) throws IllegalAccessException;
    <T> void setField(T rush, Field field, String value) throws IllegalAccessException;

}

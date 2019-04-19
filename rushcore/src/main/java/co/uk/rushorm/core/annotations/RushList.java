package co.uk.rushorm.core.annotations;

import java.lang.annotation.*;
import java.util.*;

import co.uk.rushorm.core.*;

/**
 * Created by stuartc on 11/12/14.
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface RushList {
    Class<? extends Rush> classType();
    Class listType() default ArrayList.class;
}

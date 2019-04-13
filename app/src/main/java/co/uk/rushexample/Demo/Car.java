package co.uk.rushexample.Demo;

import java.util.*;

import co.uk.rushorm.core.*;
import co.uk.rushorm.core.annotations.*;

/**
 * Created by Stuart on 31/01/15.
 */
public class Car extends RushObject {

    public String color;
    public co.uk.rushexample.Demo.Engine engine;

    public String anotherField;

    @RushList(classType = co.uk.rushexample.Demo.Wheel.class)
    public List<co.uk.rushexample.Demo.Wheel> wheels;

    public Car(){
        /* Empty constructor required */
    }

    public Car(String color, co.uk.rushexample.Demo.Engine engine){
        this.color = color;
        this.engine = engine;
    }
}

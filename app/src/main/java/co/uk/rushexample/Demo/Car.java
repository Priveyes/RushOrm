package co.uk.rushexample.Demo;

import java.util.*;

import co.uk.rushorm.core.*;
import co.uk.rushorm.core.annotations.*;

/**
 * Created by Stuart on 31/01/15.
 */
public class Car extends RushObject {

    private String color;
    private Engine engine;

    private String anotherField;

    public List<Wheel> getWheels() {
        return wheels;
    }
    @RushList(classType = Wheel.class)
    public List<Wheel> wheels;
    @RushList(classType = Drivers.class)
    private List<Drivers> drivers;

//    private List<String> stringList;

    public Car(){
        /* Empty constructor required */
    }

    public Car(String color, Engine engine){
        this.color = color;
        this.engine = engine;
    }
}

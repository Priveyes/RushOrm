package co.uk.rushexample.Demo;

import co.uk.rushorm.core.RushObject;

/**
 * Created by Stuart on 31/01/15.
 */
public class Wheel extends RushObject {

    public Wheel(){
         /* Empty constructor required */
    }

    private String make;

    public Wheel(String make){
        this.make = make;
    }

}

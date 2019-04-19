package co.uk.rushexample;

import android.app.*;

import java.util.*;

import co.uk.rushexample.Demo.*;
import co.uk.rushorm.android.*;
import co.uk.rushorm.core.*;

/**
 * Created by stuartc on 11/12/14.
 */
public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        List<Class<? extends Rush>> classes = new ArrayList<>();
        classes.add(Car.class);
        classes.add(Engine.class);
        classes.add(Wheel.class);
        classes.add(Drivers.class);

        AndroidInitializeConfig androidInitializeConfig = new AndroidInitializeConfig(getApplicationContext(), classes);

        RushAndroid.initialize(androidInitializeConfig);
    }

}

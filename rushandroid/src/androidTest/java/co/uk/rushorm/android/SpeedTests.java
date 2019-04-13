package co.uk.rushorm.android;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.uk.rushorm.core.RushCore;
import co.uk.rushorm.core.RushSearch;
import co.uk.rushorm.android.testobjects.TestChildObject;
import co.uk.rushorm.android.testobjects.TestObject;

/**
 * Created by Stuart on 16/12/14.
 */
public class SpeedTests extends ApplicationTestCase<Application> {

    public SpeedTests() {
        super(Application.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Utils.setUp(getContext());
    }

    public void testSave1000ChildRows() throws Exception {

        Date date = new Date();

        TestObject testObject = new TestObject();
        testObject.children = new ArrayList<>();
        for(int i = 0; i < 1000; i ++){
            testObject.children.add(new TestChildObject());
        }
        testObject.save();

        double time = (new Date().getTime() - date.getTime()) / 1000.0;

        Log.i("SPEED_TEST", "Save 1001 - " + Double.toString(time));

        assertTrue("Save time of 1001 rows : " + Double.toString(time), time < 10);
    }

    public void testSave100RowsIndividually() throws Exception {

        Date date = new Date();

        for (int i = 0; i < 100; i++) {
            TestChildObject testObject = new TestChildObject();
            testObject.save();
        }

        double time = (new Date().getTime() - date.getTime()) / 1000.0;

        Log.i("SPEED_TEST", "Save 1000 children - " + Double.toString(time));

        assertTrue("Save Children time of 1000 rows : " + Double.toString(time), time < 20);
    }

    public void testSave1000ChildrenInTransitionRows() throws Exception {

        Date date = new Date();
        List<TestChildObject> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            list.add(new TestChildObject());
        }

        RushCore.getInstance().save(list);

        double time = (new Date().getTime() - date.getTime()) / 1000.0;

        Log.i("SPEED_TEST", "Save 1000 children in transaction - " + Double.toString(time));

        assertTrue("Save Children in transition time of 100 rows : " + Double.toString(time), time < 10);
    }

    public void testSave1000ObjectInTransitionRows() throws Exception {

        Date date = new Date();
        List<TestObject> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            list.add(new TestObject());
        }

        RushCore.getInstance().save(list);

        double time = (new Date().getTime() - date.getTime()) / 1000.0;

        Log.i("SPEED_TEST", "Save 1000 object in transaction - " + Double.toString(time));

        assertTrue("Save Object in transition time of 100 rows : " + Double.toString(time), time < 10);
    }

    public void testLoad1000Rows() throws Exception {

        TestObject testObject = new TestObject();
        testObject.children = new ArrayList<>();
        for(int i = 0; i < 1000; i ++){
            testObject.children.add(new TestChildObject());
        }
        testObject.save();
        String id = testObject.getId();


        Date date = new Date();
        TestObject loadedObject = new RushSearch().whereId(id).findSingle(TestObject.class);
        double time = (new Date().getTime() - date.getTime()) / 1000.0;

        Log.i("SPEED_TEST", "Load 1000 - " + Double.toString(time));

        assertTrue("Load time of 1001 rows : " + Double.toString(time), time < 10);
    }

    public void testDelete1000Rows() throws Exception {

        TestObject testObject = new TestObject();
        testObject.children = new ArrayList<>();
        for(int i = 0; i < 1000; i ++){
            testObject.children.add(new TestChildObject());
        }
        testObject.save();
        String id = testObject.getId();

        TestObject loadedObject = new RushSearch().whereId(id).findSingle(TestObject.class);
        Date date = new Date();
        loadedObject.delete();
        double time = (new Date().getTime() - date.getTime()) / 1000.0;

        Log.i("SPEED_TEST", "Delete 1000 - " + Double.toString(time));

        assertTrue("Delete time of 1001 rows : " + Double.toString(time), time < 10);
    }
}

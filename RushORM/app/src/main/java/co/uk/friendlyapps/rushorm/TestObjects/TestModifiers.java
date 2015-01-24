package co.uk.friendlyapps.rushorm.TestObjects;

import co.uk.rushorm.core.Rush;
import co.uk.rushorm.core.RushTable;
import co.uk.rushorm.core.annotations.RushTableAnnotation;

/**
 * Created by Stuart on 23/01/15.
 */
@RushTableAnnotation
public class TestModifiers extends RushTable {

    public String publicString;
    protected String protectedString;
    String string;
    private String privateString;

    public TestModifiers(String protectedString, String string, String privateString) {
        this.protectedString = protectedString;
        this.string = string;
        this.privateString = string;
    }

    public TestModifiers() {

    }

    public String getProtectedString() {
        return protectedString;
    }

    public String getString() {
        return string;
    }

    public String getPrivateString() {
        return privateString;
    }

}

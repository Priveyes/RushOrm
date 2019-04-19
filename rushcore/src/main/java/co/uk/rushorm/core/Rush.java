package co.uk.rushorm.core;

public interface Rush {

    void save();

    void save(RushCallback callback);

    void delete();

    void delete(RushCallback callback);

    String getId();

}


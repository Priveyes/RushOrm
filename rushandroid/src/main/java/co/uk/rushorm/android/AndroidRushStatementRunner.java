package co.uk.rushorm.android;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import co.uk.rushorm.core.RushConfig;
import co.uk.rushorm.core.RushQueue;
import co.uk.rushorm.core.RushStatementRunner;
import co.uk.rushorm.core.exceptions.RushSqlException;

/**
 * Created by stuartc on 11/12/14.
 */
public class AndroidRushStatementRunner extends SQLiteOpenHelper implements RushStatementRunner {

    private int lastRunVersion = -1;
    private RushConfig rushConfig;
    private final Context context;
    
    public AndroidRushStatementRunner(Context context, String name, RushConfig rushConfig) {
        super(context, name, null, rushConfig.dbVersion());
        lastRunVersion = rushConfig.dbVersion();
        this.rushConfig = rushConfig;
        this.context = context;
    }

    @Override
    public void runRaw(String statement, RushQueue rushQueue) {
        try {
            getWritableDatabase().execSQL(statement);
        } catch (SQLiteException exception) {
            if(rushConfig.inDebug()) {
                throw exception;
            } else {
                throw new RushSqlException();
            }
        }
    }

    @Override
    public ValuesCallback runGet(String sql, RushQueue rushQueue) {
        final Cursor cursor;
        try {
            cursor = getWritableDatabase().rawQuery(sql, null);
        } catch (SQLiteException exception) {
            if(rushConfig.inDebug()) {
                throw exception;
            } else {
                throw new RushSqlException();
            }
        }
        cursor.moveToFirst();
        return new ValuesCallback() {
            @Override
            public boolean hasNext() {
                return !cursor.isAfterLast();
            }
            @Override
            public List<String> next() {

                List<String> row = new ArrayList<>();
                for(int i = 0; i < cursor.getColumnCount(); i++){
                    row.add(cursor.getString(i));
                }
                cursor.moveToNext();
                return row;
            }
            @Override
            public void close() {
                cursor.close();
            }
        };
    }

    @Override
    public void startTransition(RushQueue rushQueue) {
        getWritableDatabase().beginTransaction();
    }

    @Override
    public void endTransition(RushQueue rushQueue) {
        getWritableDatabase().setTransactionSuccessful();
        getWritableDatabase().endTransaction();
    }

    @Override
    public boolean isFirstRun() {
        String[] databases = context.databaseList();
        for (String database : databases) {
            if(database.equals(rushConfig.dbName())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void initializeComplete(long version) {

    }

    @Override
    public boolean requiresUpgrade(long version, RushQueue rushQueue) {
        return getLastRunVersion() != version;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        lastRunVersion = oldVersion;
    }

    private int getLastRunVersion(){
        getReadableDatabase().getVersion();
        return lastRunVersion;
    }
}

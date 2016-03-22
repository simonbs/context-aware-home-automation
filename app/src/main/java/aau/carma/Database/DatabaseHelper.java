package aau.carma.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String TABLE_GESTURE_CONFIGURATIONS = "gestureConfigurations";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CONFIGURATION = "configuration";

    private static final String DATABASE_NAME = "carma.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_GESTURE_CONFIGURATIONS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_CONFIGURATION
            + " text not null);";

    private static DatabaseHelper instance;

    private DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_GESTURE_CONFIGURATIONS);
        onCreate(database);
    }
}

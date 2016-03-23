package aau.carma.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import aau.carma.GestureConfiguration;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "carma.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_GESTURE_CONFIGURATIONS = "gestureConfigurations";
    public static final String COLUMN_ID = "_id";

    /**
     * {@link GestureConfiguration} specific constants
     */
    public static final String COLUMN_CONFIGURATION = "configuration";
    private static final String[] ALL_CONFIGURATION_COLUMNS = {COLUMN_ID, COLUMN_CONFIGURATION};

    private static final String CREATE_GESTURE_CONFIGURATIONS_TABLE = "create table "
            + TABLE_GESTURE_CONFIGURATIONS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_CONFIGURATION
            + " text not null);";

    private static DatabaseHelper instance;
    private SQLiteDatabase database;

    private DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = getWritableDatabase();
    }

    public static DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_GESTURE_CONFIGURATIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_GESTURE_CONFIGURATIONS);
        onCreate(database);
    }

    /**
     * Takes a {@link GestureConfiguration} and stores it in the database
     * @param configuration The configuration to be saved
     * @return
     */
    public GestureConfiguration saveGestureConfiguration(GestureConfiguration configuration) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_CONFIGURATION, gestureConfigurationToString(configuration));
        long insertId =  database.insert(TABLE_GESTURE_CONFIGURATIONS, null, values);
        Cursor cursor = database.query(TABLE_GESTURE_CONFIGURATIONS,
                ALL_CONFIGURATION_COLUMNS,
                COLUMN_ID + " = " + insertId,
                null, null, null, null);
        cursor.moveToFirst();
        GestureConfiguration result = cursorToGestureConfiguration(cursor);
        cursor.close();
        return result;
    }

    /**
     * Queries the database for all {@Link GestureConfiguration}s
     * @return all {@Link GestureConfiguration}s stored in the database
     */
    public ArrayList<GestureConfiguration> getAllGestureConfiguration() {
        ArrayList<GestureConfiguration> gestureConfigurations = new ArrayList<>();
        Cursor cursor = database.query(TABLE_GESTURE_CONFIGURATIONS,
               ALL_CONFIGURATION_COLUMNS , null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            GestureConfiguration configuration = cursorToGestureConfiguration(cursor);
            gestureConfigurations.add(configuration);
            cursor.moveToNext();
        }
        cursor.close();

        return gestureConfigurations;
    }

    /**
     * Removes a {@Link GestureConfiguration} from the database
     * @param configuration The {@Link GestureConfiguration} to be removed
     * @return A {@Link boolean} indicating whether the row was removed
     */
    public boolean removeGestureConfiguration(GestureConfiguration configuration) {
        int deleteCount = database.delete(TABLE_GESTURE_CONFIGURATIONS,
                COLUMN_CONFIGURATION + " = \"" + gestureConfigurationToString(configuration) + "\"",
                null);
        return deleteCount != 0;
    }

    /**
     * Takes a {@link GestureConfiguration} and transforms it into a comma-separated {@link String}
     * @param configuration The {@link GestureConfiguration} to be transformed
     * @return A comma-separated {@link String} representation of the {@link GestureConfiguration}
     */
    private String gestureConfigurationToString(GestureConfiguration configuration) {
        return configuration.roomId + "," + configuration.actionId + "," + configuration.gestureId;
    }

    /**
     * Takes a comma-separated {@link String} and transforms it into a {@Link GestureConfiguration}
     * @param configuration A comma-separated {@Link String} representation of a GestureConfiguration
     * @return A new {@Link GestureConfiguration} object
     */
    private GestureConfiguration stringToGestureConfiguration(String configuration) {
        String[] conf = configuration.split(",");
        return new GestureConfiguration(conf[0], conf[1], conf[2]);
    }

    /**
     * Takes a {@Link Cursor} and extracts a {@Link GestureConfiguration} from it
     * @param cursor
     * @return
     */
    private GestureConfiguration cursorToGestureConfiguration(Cursor cursor) {
        return stringToGestureConfiguration(cursor.getString(1));
    }
}

package aau.carma.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import aau.carma.Library.Action;
import aau.carma.GestureConfiguration;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "carma.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_GESTURE_CONFIGURATIONS = "gestureConfigurations";
    public static final String TABLE_ACTIONS = "actions";
    public static final String COLUMN_ID = "_id";

    /**
     * {@link Action} specific constants
     */
    public static final String COLUMN_ITEM_NAME = "itemName";
    public static final String COLUMN_ITEM_LABEL = "itemLabel";
    public static final String COLUMN_NEW_STATE = "newState";
    private static final String[] ALL_ACTION_COLUMNS = {COLUMN_ID, COLUMN_ITEM_NAME, COLUMN_ITEM_LABEL, COLUMN_NEW_STATE};
    private static final String CREATE_ACTIONS_TABLE = "create table "
            + TABLE_ACTIONS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_ITEM_NAME
            + " text not null, " + COLUMN_ITEM_LABEL
            + " text not null, " + COLUMN_NEW_STATE
            + " text not null);";

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
        database.execSQL(CREATE_ACTIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_GESTURE_CONFIGURATIONS);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIONS);
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

    /**
     * Takes an {@link Action} and stores it in the database
     * @param action The {@link Action} to be saved
     * @return
     */
    public Action saveAction(Action action) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_NAME, action.itemName);
        values.put(COLUMN_ITEM_LABEL, action.itemLabel);
        values.put(COLUMN_NEW_STATE, action.newState);
        
        long insertId =  database.insert(TABLE_ACTIONS, null, values);
        Cursor cursor = database.query(TABLE_ACTIONS,
                ALL_ACTION_COLUMNS,
                COLUMN_ID + " = " + insertId,
                null, null, null, null);
        cursor.moveToFirst();
        Action result = cursorToAction(cursor);
        cursor.close();
        return result;
    }

    /**
     * Takes an {@link Action} and attempts to remove it from the database
     * @param action The {@link Action} to remove
     * @return Whether or not the removal was successful
     */
    public boolean removeAction(Action action) {
        int deleteCount = database.delete(TABLE_ACTIONS,
                COLUMN_ITEM_NAME + " = ? AND"
                        + COLUMN_ITEM_LABEL + " = ? AND"
                        + COLUMN_NEW_STATE + " = ?",
                new String[]{action.itemName, action.itemLabel, action.newState});
        return deleteCount != 0;
    }

    /**
     * Returns all {@link Action}s stored in the database
     * @return An {@link ArrayList} containing all {@link Action} objects in DB
     */
    public ArrayList<Action> getAllActions() {
        ArrayList<Action> actions = new ArrayList<>();
        Cursor cursor = database.query(TABLE_ACTIONS,
                ALL_ACTION_COLUMNS, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Action action= cursorToAction(cursor);
            actions.add(action);
            cursor.moveToNext();
        }
        cursor.close();

        return actions;
    }

    /**
     * Queries the database for an {@link Action} with the given identifier
     * @param id identifier for the {@link Action}
     * @return The {@link Action} if any was found, null otherwise
     */
    public Action getAction(int id) {
        Cursor cursor = database.query(TABLE_ACTIONS,
                ALL_ACTION_COLUMNS, COLUMN_ID + " = " + id,
                null,
                null, null, null);
        cursor.moveToFirst();
        if (cursor.getCount() == 0) {
            return null;
        }
        Action action = cursorToAction(cursor);
        cursor.close();
        return action;
    }

    /**
     * Takes a {@link Cursor} and returns an {@link Action}
     * @param cursor The {@link Cursor} to extract an {@link Action} from
     * @return
     */
    private Action cursorToAction(Cursor cursor) {
        return new Action(getStringFromColumnName(cursor, COLUMN_ITEM_NAME),
                          getStringFromColumnName(cursor, COLUMN_ITEM_LABEL),
                          getStringFromColumnName(cursor, COLUMN_NEW_STATE),
                          Integer.toString(getIntFromColumnName(cursor, COLUMN_ID)));
    }

    /**
     * Takes a {@link Cursor} and the name of a column and returns the {@link String} at the index of that column
     * @param cursor The {@link Cursor} to extract the {@link String} from
     * @param column The name of the column
     * @return The {@link String} from the given column
     */
    private String getStringFromColumnName(Cursor cursor, String column) {
        return cursor.getString(cursor.getColumnIndex(column));
    }

    /**
     * Takes a {@link Cursor} and the name of a column and returns the {@link int} at the index of that column
     * @param cursor The {@link Cursor} to extract the {@link int} from
     * @param column The name of the column
     * @return The {@link int} from the given column
     */
    private int getIntFromColumnName(Cursor cursor, String column) {
        return cursor.getInt(cursor.getColumnIndex(column));
    }
}

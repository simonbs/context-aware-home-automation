package aau.carma.ThreeDOneCentGestureRecognizer.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by kasperlindsorensen on 17/03/16.
 */
public class ThreeDSQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_TEMPLATES = "templates";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LABEL = "label";
    public static final String COLUMN_ODR = "odr";
    public static final String COLUMN_LABELED_STROKE = "labeledStroke";

    private static final String DATABASE_NAME = "templates.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_TEMPLATES + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_LABEL
            + " text not null, " + COLUMN_ODR
            + " text not null, " + COLUMN_LABELED_STROKE
            + " text not null);";

    public ThreeDSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_TEMPLATES);
        onCreate(database);
    }
}

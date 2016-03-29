package aau.carma.ThreeDOneCentGestureRecognizer.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import aau.carma.ThreeDOneCentGestureRecognizer.datatype.ThreeDNNRTemplate;
import aau.carma.ThreeDOneCentGestureRecognizer.datatype.ThreeDLabeledStroke;
import aau.carma.ThreeDOneCentGestureRecognizer.datatype.ThreeDPoint;

/**
 * Created by kasperlindsorensen on 17/03/16.
 */
public class ThreeDTemplatesDataSource {
    // Database fields
    private SQLiteDatabase database;
    private ThreeDSQLiteHelper dbHelper;
    private String[] allColumns = { ThreeDSQLiteHelper.COLUMN_ID,
            ThreeDSQLiteHelper.COLUMN_LABEL, ThreeDSQLiteHelper.COLUMN_ODR , ThreeDSQLiteHelper.COLUMN_LABELED_STROKE};

    public ThreeDTemplatesDataSource(Context context) {
        dbHelper = new ThreeDSQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public ThreeDNNRTemplate saveTemplate(String label, ThreeDOneDimensionalRepresentation odr, ThreeDLabeledStroke labeledStroke) throws JSONException {
        return saveTemplate(new ThreeDNNRTemplate(label, odr, labeledStroke));
    }

    public ThreeDNNRTemplate saveTemplate(ThreeDNNRTemplate template) throws JSONException {
        ContentValues values = new ContentValues();
        values.put(ThreeDSQLiteHelper.COLUMN_LABEL, template.getLabel());

        JSONObject odrJsonObject = new JSONObject();
        JSONArray odrJsonArray = new JSONArray(template.getOdr().getSeries());
        odrJsonObject.put("odr", odrJsonArray);
        values.put(ThreeDSQLiteHelper.COLUMN_ODR, odrJsonObject.toString());

        JSONObject labeledStrokeJsonObject = new JSONObject();
        JSONArray labeledStrokeJsonArray = new JSONArray(template.getLs().getPoints());
        labeledStrokeJsonObject.put("labeledStroke", labeledStrokeJsonArray);
        values.put(ThreeDSQLiteHelper.COLUMN_LABELED_STROKE, labeledStrokeJsonObject.toString());

        long insertId = database.insert(ThreeDSQLiteHelper.TABLE_TEMPLATES, null, values);
        Cursor cursor = database.query(ThreeDSQLiteHelper.TABLE_TEMPLATES,
                allColumns,
                ThreeDSQLiteHelper.COLUMN_ID + " = " + insertId,
                null, null, null, null);
        cursor.moveToFirst();
        ThreeDNNRTemplate newTemplate = cursorToTemplate(cursor);
        cursor.close();
        return newTemplate;
    }

    public ThreeDNNRTemplate cursorToTemplate(Cursor cursor) throws JSONException {
        String label = cursor.getString(1);
        JSONArray odrJsonArray = new JSONObject(cursor.getString(2)).optJSONArray("odr");
        ArrayList<Double> odr = new ArrayList<>();
        for (int i = 0; i < odrJsonArray.length(); i++){
            odr.add(odrJsonArray.getDouble(i));
        }

        JSONArray labeledStrokeJsonArray = new JSONObject(cursor.getString(3)).optJSONArray("labeledStroke");
        ArrayList<ThreeDPoint> labeledStrokePoints = new ArrayList<>();
        for (int i = 0; i < labeledStrokeJsonArray.length(); i++){
            JSONArray values = new JSONArray(labeledStrokeJsonArray.getString(i));
            labeledStrokePoints.add(new ThreeDPoint(values.getDouble(0), values.getDouble(1), values.getDouble(2), 0));
        }

        ThreeDNNRTemplate template = new ThreeDNNRTemplate(label, new ThreeDOneDimensionalRepresentation(odr), new ThreeDLabeledStroke(label, labeledStrokePoints));

        return template;
    }

    public ArrayList<ThreeDNNRTemplate> getAllTemplates() throws JSONException {
        ArrayList<ThreeDNNRTemplate> templates = new ArrayList<>();

        Cursor cursor = database.query(ThreeDSQLiteHelper.TABLE_TEMPLATES,
                allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ThreeDNNRTemplate template = cursorToTemplate(cursor);
            templates.add(template);
            cursor.moveToNext();
        }
        cursor.close();
        return templates;
    }
}

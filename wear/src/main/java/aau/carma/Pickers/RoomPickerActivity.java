package aau.carma.Pickers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import aau.carma.R;

/**
 * Activity for picking a room.
 */
public class RoomPickerActivity extends Activity implements RoomPickerFragment.OnRoomPickedListener {
    /**
     * Key for a picked room when the activity finishes with a result.
     */
    public static final String RESULT_ROOM = "room";

    /**
     * Fragment for picking the room.
     */
    private RoomPickerFragment pickerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_picker);
        pickerFragment = (RoomPickerFragment)getFragmentManager().findFragmentById(R.id.room_picker_fragment);
        pickerFragment.setOnRoomPickedListener(this);
    }

    @Override
    public void onPick(RoomPickerFragment.RoomPickerItem roomPickerItem) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(RESULT_ROOM, roomPickerItem.room);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
    }
}

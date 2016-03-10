package simonbs.whereami;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.estimote.sdk.SystemRequirementsChecker;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocalBroadcastManager.getInstance(this).registerReceiver(didEnterRoomReceiver, new IntentFilter(Notifications.DidEnterRoom));
        LocalBroadcastManager.getInstance(this).registerReceiver(didLeaveRoom, new IntentFilter(Notifications.DidLeaveRoom));
    }

    @Override
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }

    private BroadcastReceiver didEnterRoomReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Room room = intent.getParcelableExtra(Notifications.Extras.Room);
            TextView roomNameTextView = (TextView)findViewById(R.id.room_name);
            roomNameTextView.setText(room.name);
        }
    };

    private BroadcastReceiver didLeaveRoom = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TextView roomNameTextView = (TextView)findViewById(R.id.room_name);
            roomNameTextView.setText(null);
        }
    };
}

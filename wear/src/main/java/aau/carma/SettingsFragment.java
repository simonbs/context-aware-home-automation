package aau.carma;

import android.app.Fragment;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Fragment showing settings.
 */
public class SettingsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        WearableListView listView = (WearableListView)getView().findViewById(R.id.settings_list_view);
        ArrayList<WearableListItemAdapter.WearableListItem> settings = new ArrayList<>();
        settings.add(Setting.TRAIN_GESTURE);
        settings.add(Setting.CONFIGURE_ACTION);
        WearableListItemAdapter listAdapter = new WearableListItemAdapter(getActivity(), settings);
        listView.setAdapter(listAdapter);
    }

    /**
     * Settings list view.
     */
    private enum Setting implements WearableListItemAdapter.WearableListItem {
        TRAIN_GESTURE,
        CONFIGURE_ACTION;

        @Override
        public int getIconResource() {
            switch (this) {
                case TRAIN_GESTURE: return R.drawable.train_gesture;
                case CONFIGURE_ACTION: return R.drawable.configure_action;
            }

            return -1;
        }

        @Override
        public int getTitleResource() {
            switch (this) {
                case TRAIN_GESTURE: return R.string.settings_train_gesture;
                case CONFIGURE_ACTION: return R.string.settings_configure_action;
            }

            return -1;
        }
    }
}

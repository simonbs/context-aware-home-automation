package aau.carma.Picker;

import android.app.Fragment;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import aau.carma.R;
import aau.carmakit.Utilities.Optional;

/**
 * Fragment showing a picker allowing the user to select an item.
 */
public class PickerFragment extends Fragment implements WearableListView.ClickListener {
    /**
     * List view showing the items in the picker.
     */
    private WearableListView listView;

    /**
     * Listener to inform when an item is picked.d
     */
    private Optional<OnPickListener> onPickListener = new Optional<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_picker, container, false);
        listView = (WearableListView)view.findViewById(R.id.picker_list_view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setClickListener(this);
    }

    /**
     * Sets the listener to inform when an item is picked.
     * @param onPickListener Listener to inform when item is picked.
     */
    public void setOnPickListener(OnPickListener onPickListener) {
        this.onPickListener = new Optional<>(onPickListener);
    }

    /**
     * Reloads the items in the picker.
     * @param items Items to show in the picker.
     */
    public void reloadItems(ArrayList<WearableListItemAdapter.WearableListItem> items) {
        WearableListItemAdapter listAdapter = new WearableListItemAdapter(getActivity(), items);
        listView.setAdapter(listAdapter);
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        if (onPickListener.isPresent()) {
            onPickListener.value.onPick(viewHolder.getLayoutPosition(), viewHolder);
        }
    }

    @Override
    public void onTopEmptyRegionClick() { }

    /**
     * Interface implemented by objects interested in knowing when an item is picked.
     */
    public interface OnPickListener {
        void onPick(int position, WearableListView.ViewHolder viewHolder);
    }
}

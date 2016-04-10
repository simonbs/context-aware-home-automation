package aau.carma;

import android.app.Fragment;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import aau.carmakit.Utilities.Logger;

/**
 * Fragment showing a picker allowing the user to select an item.
 */
public class PickerFragment extends Fragment {
    /**
     * List view showing the items in the picker.
     */
    private WearableListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.picker_fragment, container, false);
        listView = (WearableListView)view.findViewById(R.id.picker_list_view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        listView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
    }

    /**
     * Reloads the items in the picker.
     * @param items Items to show in the picker.
     */
    public void reloadItems(ArrayList<WearableListItemAdapter.WearableListItem> items) {
        WearableListItemAdapter listAdapter = new WearableListItemAdapter(getActivity(), items);
        listView.setAdapter(listAdapter);
    }
}

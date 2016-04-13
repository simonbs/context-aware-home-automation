package aau.carma.Pickers;

import android.os.Parcel;
import android.os.Parcelable;

import aau.carma.Picker.WearableListItemAdapter;
import aau.carma.R;
import aau.carmakit.ContextEngine.ContextOutcome;
import aau.carmakit.Utilities.Optional;

/**
 * A context outcome to be displayed in the picker.
 */
public class ContextOutcomePickerItem implements WearableListItemAdapter.WearableListItem, Parcelable {
    /**w
     * Title shown in the cell.
     */
    private final String title;

    /**w
     * Subtitle shown in the cell.
     */
    private final String subtitle;

    /**
     * Encapsulated context outcome.
     */
    public final ContextOutcome contextOutcome;

    /**
     * Initializes a context outcome item for displaying in the picker.
     * @param title Title communicating the item the action of the context outcome is associated with.
     * @param subtitle Subtitle communicating the new state an item will be in after triggering the action.
     * @param contextOutcome Encapsulated context outcome.
     */
    ContextOutcomePickerItem(String title, String subtitle, ContextOutcome contextOutcome) {
        this.title = title;
        this.subtitle = subtitle;
        this.contextOutcome = contextOutcome;
    }

    protected ContextOutcomePickerItem(Parcel in) {
        title = in.readString();
        subtitle = in.readString();
        contextOutcome = in.readParcelable(ContextOutcome.class.getClassLoader());
    }

    public static final Creator<ContextOutcomePickerItem> CREATOR = new Creator<ContextOutcomePickerItem>() {
        @Override
        public ContextOutcomePickerItem createFromParcel(Parcel in) {
            return new ContextOutcomePickerItem(in);
        }

        @Override
        public ContextOutcomePickerItem[] newArray(int size) {
            return new ContextOutcomePickerItem[size];
        }
    };

    @Override
    public Optional<Integer> getIconResource() {
        return new Optional<>(R.drawable.action);
    }

    @Override
    public Optional<String> getTitle() {
        return new Optional<>(title);
    }

    @Override
    public Optional<String> getSubtitle() {
        return new Optional<>(subtitle);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(subtitle);
        dest.writeParcelable(contextOutcome, 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
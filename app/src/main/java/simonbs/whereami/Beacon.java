package simonbs.whereami;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by simonbs on 10/03/2016.
 */
public class Beacon implements Parcelable {
    /**
     * Namespace of the Eddystone beacon.
     * Identifies a group of beacons.
     */
    final String namespace;

    /**
     * Instance of the Eddystone beacon.
     * Identifies a specific beacon.
     */
    final String instance;

    /**
     * Creates an Eddystone beacon.
     * @param namespace Namespace of the Eddystone beacon. Identifies a group of beacons.
     * @param instance Instance of the Eddystone beacon. Identifies a specific beacon.
     */
    Beacon(String namespace, String instance) {
        this.namespace = namespace;
        this.instance = instance;
    }

    Beacon(Parcel in) {
        namespace = in.readString();
        instance = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(namespace);
        dest.writeString(instance);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Room createFromParcel(Parcel in) {
            return new Room(in);
        }

        public Room[] newArray(int size) {
            return new Room[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Beacon)) {
            return false;
        }

        Beacon other = (Beacon)o;
        return namespace.toLowerCase() == other.namespace.toLowerCase() &&
                instance.toLowerCase() == other.instance.toLowerCase();
    }

    @Override
    public int hashCode() {
        return (namespace.toLowerCase() + instance.toLowerCase()).hashCode();
    }
}

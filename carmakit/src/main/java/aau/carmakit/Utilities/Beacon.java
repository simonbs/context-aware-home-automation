package aau.carmakit.Utilities;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents a beacon registered in openHAB.
 */
public class Beacon implements Parcelable {
    /**
     * Namespace of the Eddystone beacon.
     * Identifies a group of beacons.
     */
    public final String namespace;

    /**
     * Instance of the Eddystone beacon.
     * Identifies a specific beacon.
     */
    public final String instance;

    /**
     * Creates an Eddystone beacon.
     * @param namespace Namespace of the Eddystone beacon. Identifies a group of beacons.
     * @param instance Instance of the Eddystone beacon. Identifies a specific beacon.
     */
    public Beacon(String namespace, String instance) {
        this.namespace = namespace;
        this.instance = instance;
    }

    protected Beacon(Parcel in) {
        namespace = in.readString();
        instance = in.readString();
    }

    public static final Creator<Beacon> CREATOR = new Creator<Beacon>() {
        @Override
        public Beacon createFromParcel(Parcel in) {
            return new Beacon(in);
        }

        @Override
        public Beacon[] newArray(int size) {
            return new Beacon[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(namespace);
        dest.writeString(instance);
    }
}

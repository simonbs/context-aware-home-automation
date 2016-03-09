package simonbs.whereami;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import com.estimote.sdk.Region;

import java.util.UUID;

/**
 * Created by simonbs on 09/03/2016.
 */
public class Room implements Parcelable {
    private final String identifier;
    private final String name;
    private final UUID uuid;
    private final Integer major;
    private final Integer minor;

    Room(String identifier, String name, UUID uuid, Integer major, Integer minor) {
        this.identifier = identifier;
        this.name = name;
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
    }

    String getIdentifier() {
        return identifier;
    }

    String getName() {
        return name;
    }

    UUID getUUID() {
        return uuid;
    }

    Integer getMajor() {
        return major;
    }

    Integer getMinor() {
        return minor;
    }

    Region toRegion() {
        return new Region(identifier, uuid, major, minor);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(identifier);
        dest.writeString(name);
        dest.writeString(uuid.toString());
        dest.writeInt(major);
        dest.writeInt(minor);
    }

    Room(Parcel in) {
        identifier = in.readString();
        name = in.readString();
        uuid = UUID.fromString(in.readString());
        major = in.readInt();
        minor = in.readInt();
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
}

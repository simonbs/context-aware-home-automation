package simonbs.whereami;

import com.estimote.sdk.Region;

import java.util.UUID;

/**
 * Created by simonbs on 09/03/2016.
 */
public class Room {
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
}

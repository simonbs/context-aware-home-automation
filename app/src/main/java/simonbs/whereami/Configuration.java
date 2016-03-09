package simonbs.whereami;

import java.util.UUID;

/**
 * Created by simonbs on 08/03/2016.
 */
public class Configuration {
    public static final String EstimoteAppId = "whereami-96k";
    public static final String EstimoteAppToken = "0c545e845da025fe91061318dea910ff";

    public static final UUID BeaconIce2UUID = UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D");
    public static final Integer BeaconIce2Major = 13299;
    public static final Integer BeaconIce2Minor = 53869;

    public static final UUID BeaconBlueberry3UUID = UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D");
    public static final Integer BeaconBlueberry3Major = 12677;
    public static final Integer BeaconBlueberry3Minor = 29613;

    public static final UUID BeaconMint3UUID = UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D");
    public static final Integer BeaconMint3Major = 31177;
    public static final Integer BeaconMint3Minor = 40430;

    public static final Room[] Rooms = {
            new Room("kitchen",
                    "Kitchen",
                    BeaconIce2UUID,
                    BeaconIce2Major,
                    BeaconIce2Minor),
            new Room("desk",
                    "Desk",
                    BeaconBlueberry3UUID,
                    BeaconBlueberry3Major,
                    BeaconBlueberry3Minor),
            new Room("living_room",
                    "Living Room",
                    BeaconMint3UUID,
                    BeaconMint3Major,
                    BeaconMint3Minor)
    };
}

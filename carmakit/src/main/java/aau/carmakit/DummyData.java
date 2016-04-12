package aau.carmakit;

import java.util.ArrayList;
import java.util.UUID;

import aau.carmakit.Configuration;
import aau.carmakit.Utilities.Beacon;
import aau.carmakit.Utilities.Room;

/**
 * Contains dummy data until we have our databases configured.
 */
public class DummyData {
    public static final UUID BeaconIce2UUID = UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D");
    public static final Integer BeaconIce2Major = 13299;
    public static final Integer BeaconIce2Minor = 53869;
    public static final String BeaconIce2Namespace = "EDD1EBEAC04E5DEFA017";
    public static final String BeaconIce2Instance = "D470D26D33F3";

    public static final UUID BeaconBlueberry3UUID = UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D");
    public static final Integer BeaconBlueberry3Major = 12677;
    public static final Integer BeaconBlueberry3Minor = 29613;
    public static final String BeaconBlueberry3Namespace = "EDD1EBEAC04E5DEFA017";
    public static final String BeaconBlueberry3Instance = "F13173AD3185";

    public static final UUID BeaconMint3UUID = UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D");
    public static final Integer BeaconMint3Major = 31177;
    public static final Integer BeaconMint3Minor = 40430;
    public static final String BeaconMint3Namespace = "EDD1EBEAC04E5DEFA017";
    public static final String BeaconMint3Instance = "E6D39DEE79C9";

    public static final UUID BeaconIce3UUID = UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D");
    public static final Integer BeaconIce3Major = 49349;
    public static final Integer BeaconIce3Minor = 36330;
    public static final String BeaconIce3Namespace = "EDD1EBEAC04E5DEFA017";
    public static final String BeaconIce3Instance = "DEC18DEAC0C5";

    private static String RoomIdentifierKitchen = "kitchen";
    private static String RoomIdentifierDesk = "desk";
    private static String RoomIdentifierLivingRoom = "living_room";
    private static String RoomIdentifierBathroom = "bathroom";

    private static String GestureIdentifierCircle = "circle";
    private static String GestureIdentifierV = "v";

    private static String ActionIdentifierNextSong = "next_song";
    private static String ActionIdentifierPreviousSong = "previous_song";
    private static String ActionIdentifierTurnLampOn = "turn_lamp_on";
    private static String ActionIdentifierTurnLampOff = "turn_lamp_off";

    public static ArrayList<Room> getAllRooms() {
        ArrayList<Room> rooms = new ArrayList<>();

        ArrayList<Beacon> kitchenBeacons = new ArrayList<>();
        kitchenBeacons.add(new Beacon(BeaconIce2Namespace, BeaconIce2Instance));

        ArrayList<Beacon> deskBeacons = new ArrayList<>();
        deskBeacons.add(new Beacon(BeaconBlueberry3Namespace, BeaconBlueberry3Instance));

        ArrayList<Beacon> livingRoomBeacons = new ArrayList<>();
        deskBeacons.add(new Beacon(BeaconMint3Namespace, BeaconMint3Instance));

        ArrayList<Beacon> bathroomBeacons = new ArrayList<>();
        deskBeacons.add(new Beacon(BeaconIce3Namespace, BeaconIce3Instance));

        rooms.add(new Room(RoomIdentifierKitchen, "Kitchen", kitchenBeacons));
        rooms.add(new Room(RoomIdentifierDesk, "Desk", deskBeacons));
        rooms.add(new Room(RoomIdentifierLivingRoom, "Living Room", livingRoomBeacons));
        rooms.add(new Room(RoomIdentifierBathroom,  "Bathroom", bathroomBeacons));

        return rooms;
    }

    public static ArrayList<String> getAllGestures() {
        ArrayList<String> gestures = new ArrayList<>();
        gestures.add(GestureIdentifierCircle);
        gestures.add(GestureIdentifierV);

        return gestures;
    }

    public static ArrayList<String> getAllActions() {
        ArrayList<String> actions = new ArrayList<>();
        actions.add(ActionIdentifierNextSong);
        actions.add(ActionIdentifierPreviousSong);
        actions.add(ActionIdentifierTurnLampOff);
        actions.add(ActionIdentifierTurnLampOn);

        return actions;
    }

    public static ArrayList<GestureConfiguration> getAllGestureConfigurations() {
        ArrayList<GestureConfiguration> configurations = new ArrayList<>();

        configurations.add(new GestureConfiguration(RoomIdentifierDesk, ActionIdentifierNextSong, GestureIdentifierCircle));
        configurations.add(new GestureConfiguration(RoomIdentifierDesk, ActionIdentifierPreviousSong, GestureIdentifierV));

        configurations.add(new GestureConfiguration(RoomIdentifierKitchen, ActionIdentifierTurnLampOn, GestureIdentifierCircle));
        configurations.add(new GestureConfiguration(RoomIdentifierKitchen, ActionIdentifierTurnLampOff, GestureIdentifierV));

        return configurations;
    }

    public static ArrayList<GestureConfiguration> gestureConfigurationsForRoomWithIdentifier(String roomIdentifier) {
        ArrayList<GestureConfiguration> allConfigurations = getAllGestureConfigurations();
        ArrayList<GestureConfiguration> result = new ArrayList<>();
        for (GestureConfiguration configuration : allConfigurations) {
            if (configuration.roomId == roomIdentifier) {
                result.add(configuration);
            }
        }

        return result;
    }
}

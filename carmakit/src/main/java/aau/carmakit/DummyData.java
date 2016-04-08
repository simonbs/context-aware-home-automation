package aau.carmakit;

import java.util.ArrayList;

import aau.carmakit.Configuration;
import aau.carmakit.Utilities.Beacon;
import aau.carmakit.Utilities.Room;

/**
 * Contains dummy data until we have our databases configured.
 */
public class DummyData {
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
        kitchenBeacons.add(new Beacon(Configuration.BeaconIce2Namespace, Configuration.BeaconIce2Instance));

        ArrayList<Beacon> deskBeacons = new ArrayList<>();
        deskBeacons.add(new Beacon(Configuration.BeaconBlueberry3Namespace, Configuration.BeaconBlueberry3Instance));

        ArrayList<Beacon> livingRoomBeacons = new ArrayList<>();
        deskBeacons.add(new Beacon(Configuration.BeaconMint3Namespace, Configuration.BeaconMint3Instance));

        ArrayList<Beacon> bathroomBeacons = new ArrayList<>();
        deskBeacons.add(new Beacon(Configuration.BeaconIce3Namespace, Configuration.BeaconIce3Instance));

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

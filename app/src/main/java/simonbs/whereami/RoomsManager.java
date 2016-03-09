package simonbs.whereami;

/**
 * Created by simonbs on 09/03/2016.
 */
public class RoomsManager {
    private static RoomsManager ourInstance = new RoomsManager();
    public static RoomsManager getInstance() {
        return ourInstance;
    }

    private Room[] rooms;

    private Room currentRoom;

    private RoomsManager() {
        rooms = new Room[] {
                new Room("kitchen",
                        "Kitchen",
                        Configuration.BeaconIce2UUID,
                        Configuration.BeaconIce2Major,
                        Configuration.BeaconIce2Minor),
                new Room("desk",
                        "Desk",
                        Configuration.BeaconBlueberry3UUID,
                        Configuration.BeaconBlueberry3Major,
                        Configuration.BeaconBlueberry3Minor),
                new Room("living_room",
                        "Living Room",
                        Configuration.BeaconMint3UUID,
                        Configuration.BeaconMint3Major,
                        Configuration.BeaconMint3Minor)
        };
    }

    Room[] getRooms() {
        return rooms;
    }

    Room getRoomWithIdentifier(String identifier) {
        for (Room room : rooms) {
            if (room.getIdentifier() == identifier) {
                return room;
            }
        }

        return null;
    }

    Room getCurrentRoom() {
        return currentRoom;
    }

    void changeRoom(Room room) {
        currentRoom = room;
    }
}

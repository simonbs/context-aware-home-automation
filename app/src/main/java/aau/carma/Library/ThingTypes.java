package aau.carma.Library;

/**
 * Thing types in openHAB.
 */
public enum ThingTypes {
    /**
     * A room.
     */
    Room,

    /**
     * A beacon.
     */
    Beacon;

    /**
     * Retrieves the raw string value type, i.e.
     * the one used in openHAB.
     * @return Raw thing type.
     */
    public String rawValue() {
        switch (this) {
            case Room: return "carma:room";
            case Beacon: return "carma:beacon";
        }

        return null;
    }
}

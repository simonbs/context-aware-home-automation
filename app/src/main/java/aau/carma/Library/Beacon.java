package aau.carma.Library;

/**
 * Represents a beacon registered in openHAB.
 */
public class Beacon {
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

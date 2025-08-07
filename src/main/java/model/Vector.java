package model;

import java.util.Arrays;
import java.util.StringJoiner; // Import the StringJoiner class

public class Vector {

    private final float[] data;

    /**
     * Constructs a Vector from a float array.
     * @param data The vector's data. Cannot be null.
     */
    public Vector(float[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Vector data cannot be null.");
        }
        // Creates a defensive copy to ensure immutability.
        this.data = Arrays.copyOf(data, data.length);
    }

    /**
     * Gets the dimension of the vector.
     * @return The dimension of the vector.
     */
    public int getDimensions() {
        return data.length;
    }

    /**
     * Gets the data array of the vector.
     * @return A defensive copy of the vector's data.
     */
    public float[] getData() {
        // Returns a copy to protect the internal state from external modification.
        return Arrays.copyOf(data, data.length);
    }

    /**
     * Converts the vector to a string format recognizable by pgvector, e.g., "[1.0,2.5,3.0]".
     * This version uses StringJoiner for robust and efficient string construction.
     * @return The formatted string.
     */
    public String toSqlString() {
        // Use StringJoiner to specify the prefix "[", suffix "]", and delimiter ",".
        StringJoiner joiner = new StringJoiner(",", "[", "]");

        // Explicitly iterate over the float array. This has no ambiguity.
        for (float f : data) {
            joiner.add(String.valueOf(f));
        }

        return joiner.toString();
    }

    /**
     * Overrides the equals method to determine if two Vector objects are equal.
     * Two vectors are equal if and only if they have the same dimension and
     * their corresponding elements are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector vector = (Vector) o;
        return Arrays.equals(data, vector.data);
    }

    /**
     * Overrides the hashCode method to be consistent with equals.
     * This is crucial for using Vector objects in hash-based collections like HashMap or HashSet.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    /**
     * Overrides the toString method to provide a human-readable representation, useful for debugging.
     * Here, we reuse the logic of toSqlString.
     */
    @Override
    public String toString() {
        return toSqlString();
    }
}

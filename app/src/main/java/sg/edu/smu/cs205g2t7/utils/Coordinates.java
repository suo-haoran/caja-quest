package sg.edu.smu.cs205g2t7.utils;

import java.util.Objects;
/**
 * Coordinates class for location of user, end point, crates and obstacles
 * in a Cartesian 2D space
 */
public class Coordinates {
    /** x coordinate */
    public int x;
    /** y coordinate */
    public int y;
    /**
     * Instantiates the coordinates with x and y values
     * @param x x coordinate
     * @param y y coordinate
     */
    public Coordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }
    /**
     * Two coordinate objects are equal if their both x values are the same
     * and both y values are the same
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return x == that.x && y == that.y;
    }
    /**
     * Computes the hashcode from the values
     */
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
    /**
     * Makes a deep copy of the current coordinates
     * @param x 
     * @param y
     * @return a Coordinate object with variables x and y
     */
    public Coordinates clone(int x, int y) {
        return new Coordinates(x, y);
    }
}

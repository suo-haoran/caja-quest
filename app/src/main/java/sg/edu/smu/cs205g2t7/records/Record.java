package sg.edu.smu.cs205g2t7.records;
/**
 * Stores the attempt and the time taken by the user to play the game
 */
public class Record {
    /** Record identifier */
    private final int id;
    /** Timing of user to complete the game */
    private final Double timing;
    /**
     * Constructor
     * @param id - Integer for record id
     * @param timing - Timing from the start of game session
     */
    public Record(int id, Double timing) {
        this.id = id;
        this.timing = timing;
    }
    /**
     * Retrieves id of record
     * @return id - An integer corresponding to the record id
     */
    public int getId() {
        return id;
    }
    /**
     * Retrieves timing of record
     * @return timing - a double corresponding to the record timing
     */
    public Double getTiming() {
        return timing;
    }
}

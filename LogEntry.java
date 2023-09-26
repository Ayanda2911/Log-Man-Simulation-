import java.io.File;

public class LogEntry {

    private String raw;
    private int entryID;
    private String category;
    private long  timestamp;
    protected int sortedId;

    /**
     * Constructor for log entries with valid inputs
     * @param line
     */
    public  LogEntry(String line){
        // store original line
        raw = line;
        // convert it into a long that we can work with
        timestamp = convertTimeToLong(line);
        // category
        category = line.substring(line.indexOf("|") + 1, line.lastIndexOf("|")).toLowerCase();
    }

    /**
     * Creates a bogus log entry from timestamp
     * @param time
     */
    public  LogEntry(long time){
        timestamp = time;
    }


    /**
     * Sets the entry ID
     * @param entryID
     */
    public void setEntryID(int entryID) {
        this.entryID = entryID;
    }

    /**
     * Get's Entry ID
     * @return
     */
    public int getId() {
        return entryID;
    }

    /**
     * Retrieves the category
     * @return category
     */
    public String getCategory(){return category;}

    /**
     * Get's timestamo
     * @return timestamp
     */
    public long getTimestamp(){
        return timestamp;
    }

    /**
     * Convert's readin in timestamp to a long
     * @param time String of timestamp
     * @return timestamp in the form of a long
     */
    public static long convertTimeToLong(String time){
        // M M : D D : H H : m m  :  s  s
        // 0 1 2 3 4 5 6 7 8 9 10 11 12 13
        return (time.charAt(13) - '0') * 1L
                + (time.charAt(12) - '0') * 10L
                + (time.charAt(10) - '0') * 100L
                + (time.charAt(9) - '0') * 1000L
                + (time.charAt(7) - '0') * 10000L
                + (time.charAt(6) - '0') * 100000L
                + (time.charAt(4) - '0') * 1000000L
                + (time.charAt(3) - '0') * 10000000L
                + (time.charAt(1) - '0') * 100000000L
                + (time.charAt(0) - '0') * 1000000000L;

    }

    @Override
    public String toString() {
        return  raw + "\n";
    }

    public String getMessage() {
        return raw;
    }


}

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Logfile {
    // keep track of log entries
    ArrayList<LogEntry> masterList;
    // keeps track of indices
    private ArrayList<Integer> indexMap;
    // keeps track of sorted order
    private ArrayList<Integer> sortedMasterList;
    // stores categories and associated LogEntry ID
    private HashMap<String, ArrayList<Integer>> categoryMap;
    private HashMap<String, ArrayList<Integer>> keywordMap;
    // category/ keyword search result
    private ArrayList< Integer> hashSearchResults;
    // timestamp range
    private int startIdx, endIdx;
    // keeps track of the type of search that we are doing
    private LastSearch searchKind;
    // number of logentries
    private int numLogs;
    // excerpt list
    ArrayList<Integer> excerptList;

    public  Logfile(String fn){
        searchKind = LastSearch.None;
        startIdx = endIdx = -1;
        masterList = new ArrayList<>();
        excerptList = new ArrayList<>();
        numLogs = 0;

        try  {
            Scanner in = new Scanner(new File(fn));

            while(in.hasNextLine()){

                String line = in.nextLine();
                if(line.isBlank()){
                    //empty line
                    break;
                }
                // assign log entry to variable
                LogEntry latest = new LogEntry((line));
                masterList.add(latest);
                // set EntryID
                masterList.get(masterList.size() - 1).setEntryID(numLogs);
                numLogs++;
            }
        }
        catch (FileNotFoundException e){
            System.err.println(fn +" not found");
        }
        postProcess();
    }

    /**
     * This method does the processing for log entries
     * to prepare us for all of our user commands
     */
    public  void postProcess() {
        SortingComparator comp = new SortingComparator();
        Collections.sort(masterList, comp);

        // regenerate a mapping of the original index locations to support the append command
        indexMap = new ArrayList<>(masterList.size());
        sortedMasterList = new ArrayList<>(masterList.size());

        for(int i = 0; i < masterList.size(); i ++){

            sortedMasterList.add(0);
            // insert a dummy value
            indexMap.add(0);
        }

        // set the mapping for the original locations in the master logfile
        for( int i = 0; i< masterList.size(); i ++){
            LogEntry e = masterList.get(i);
            e.sortedId = i;
            //set id as I
            //msList is ordered
            // so the id at index i is the ordered that it is sorted
            sortedMasterList.set(e.sortedId, i);
            indexMap.set(e.getId(), i);
        }


        //prepare for categorySearch
        hashSearchResults = new ArrayList<>();
        prepareCategorySearch();
        prepKeywordSearch();

    }

    /**
     * Prepares Keyword search
     */
    private void prepKeywordSearch() {
        keywordMap = new HashMap<>();
        for(int i = 0; i < masterList.size(); i++){
            String[] kwds =  masterList.get(i).getMessage().substring(15).toLowerCase().split("[^a-zA-Z0-9]+");

            // make sure no duplicates
            for(String kwd: kwds){
                if(kwd.isBlank())
                    continue;

                //add the current index to the entry for the keyword
                if(!keywordMap.containsKey(kwd)){
                    // create a new keyword
                    keywordMap.put(kwd, new ArrayList<>());
                    keywordMap.get(kwd).add(i);
                }else{
                    //if its there add to the arraylist
                    keywordMap.get(kwd).add(i);
                }
            }
        }
    }

    /**
     * prepare for category search
     */
    private void prepareCategorySearch() {
        // initialize map
        categoryMap = new HashMap<>();
        for(int i = 0; i < masterList.size(); i++){
            // check to make sure that the category is in the mapping
            if(!categoryMap.containsKey(masterList.get(i).getCategory().toLowerCase())){
                categoryMap.put(masterList.get(i).getCategory().toLowerCase(), new ArrayList<>());
            }
            categoryMap.get(masterList.get(i).getCategory().toLowerCase()).add(i);
        }
    }

    /**
     * Performs timestamp search
     * @param start starting timestamp
     * @param end ending timestamp
     * @return [start, end] >> number of timetsamps in this interval
     */
    public int timeStampSearch(long start, long end){
        searchKind = LastSearch.Timestamp;
        hashSearchResults.clear();
        // find the index of the starting timestamp and save it to startIdx
        startIdx = Collections.binarySearch(masterList, new LogEntry(start), new LowerBound());
        startIdx = (startIdx + 1) * -1;

        // find the index of the ending timestamp and save it to endIdx
        endIdx = Collections.binarySearch(masterList, new LogEntry(end), new UpperBound());
        endIdx = (endIdx + 1) * -1;

        // add id to hashSearchresults
        for(int i = startIdx; i < endIdx; i++){
            hashSearchResults.add(masterList.get(i).sortedId);
            }

        return  endIdx - startIdx;
    }

    /**
     * Finds specified category
     * @param cat category to be found
     * @return the number of entries with matching categories
     */
    public int categorySearch(String cat){
        // set search kind
        searchKind = LastSearch.Category;
        // clear category
        hashSearchResults.clear();

        // if not found in Category map >> return 0
        if(!categoryMap.containsKey(cat.toLowerCase())){
            return 0;
        }
        // else add to results
        hashSearchResults.addAll(categoryMap.get(cat.toLowerCase()));

        return hashSearchResults.size();
    }


    public int keywordSearch(ArrayList<String> kwds){
        HashSet<Integer> temp = new HashSet<>();
        searchKind = LastSearch.Keyword;
        hashSearchResults.clear();

        // check that it isn't null
        if(!keywordMap.containsKey(kwds.get(0).toLowerCase())){
            return 0;
        }
        // if found in  add to temp
        temp.addAll(keywordMap.get(kwds.get(0).toLowerCase()));

        // if temp is empty return 0
        if(temp.isEmpty()){
            return 0;
        }

        // add the rest into temp
        for(int i = 1; i < kwds.size(); i ++){
            if(!keywordMap.containsKey(kwds.get(i).toLowerCase())){
                return 0;
            }
            else{
                temp.retainAll(keywordMap.get(kwds.get(i).toLowerCase()));
            }
        }

        hashSearchResults.addAll(temp);

        return hashSearchResults.size();

    }


    /**
     * Processes UserCommands
     * @param u UserCommands
     */
    public void Process(UserCommands u ){
        // ERROR CHECK : appropriate range in associated list
        if( u.command == 'b' || u.command == 'd' || u.command == 'e' ){
            checkAppropRange(excerptList, u.number);
        }

        // ERRORCHECK : MAKE SURE THAT PRIOR SEARCHES FOR G AND R HAVE OCCURRED
        if(u.command == 'g'|| u.command == 'r'){
            if(searchKind == LastSearch.None){
                System.err.print("No prior searches have occured");
                return;
            }
        }

        switch(u.command){
            // processing commands
            case 'a':
                if(u.number < 0 || u.number > masterList.size() - 1){
                    System.err.print("index not found in masterlist\n");
                    break;
                }
                int toBeAppended = getLogEntryInMasterListfromEntryID(u.number).sortedId;
                excerptList.add(toBeAppended);
                System.out.print("log entry " + u.number + " appended\n");
                break;
            case 'p':
                for(int i = 0; i < excerptList.size(); i ++){
                System.out.print(i + "|" +  getLogEntryInMasterList(excerptList.get(i)).getId() +  "|" + getLogEntryInMasterList(excerptList.get(i)));
                }
                break;
            case 'b':
                if(u.number < 0 ||u.number >= excerptList.size()){
                    System.out.print("");
                }else{
                int front = excerptList.get(u.number);
                // shift it all up
                for( int i = u.number - 1; i >= 0; i--){
                    excerptList.set(i + 1, excerptList.get(i));
                }
                excerptList.set(0, front);
                System.out.print("Moved excerpt list entry " + u.number + "\n");}
                break;
            case 'd':
                if(u.number < 0 ||u.number >= excerptList.size()){
                    System.out.print("");
                }else{
                    excerptList.remove(u.number);
                    System.out.print("Deleted excerpt list entry " + u.number + "\n");
                }
                break;
            case 'e':
                if(u.number < 0 ||u.number >= excerptList.size()){
                    System.out.print("");
                }else{
                    int back = excerptList.get(u.number);
                    excerptList.add(back);
                    excerptList.remove(u.number);
                    System.out.print("Moved excerpt list entry " + u.number + "\n");
                }
                break;
            case 's':
                // what to do if its empty
                if(excerptList.isEmpty()){
                    System.out.print("excerpt list sorted\n");
                    System.out.print("(previously empty)\n");
                }else{
                System.out.print("excerpt list sorted\n");
                System.out.print("previous ordering:\n");
                printFirstAndLast();
                Collections.sort(excerptList);
                System.out.print("new ordering:\n");
                printFirstAndLast();
                }
                break;
            case 'l':
                if(excerptList.isEmpty()){
                    System.out.print("excerpt list cleared\n" + "(previously empty)\n");
                }else{
                    System.out.print("excerpt list cleared\n");
                    System.out.print("previous contents:\n");
                   printFirstAndLast();
                    excerptList.clear();}
                break;
            case 't':
                String[] time = u.argument.split("\\|", 2);
                if (time[0].length() != 14 ||time[1].length() != 14 ) {
                    System.err.print("invalid timestamp \n");
                    break;
                }else{
                System.out.print("Timestamps search: "
                        + timeStampSearch(LogEntry.convertTimeToLong(time[0]), LogEntry.convertTimeToLong(time[1]))
                        + " entries found\n");}
                break;
            case 'c':
                System.out.print("Category search: " + categorySearch(u.argument) + " entries found\n");
                break;
            case 'g':
                Collections.sort(hashSearchResults);
                for(int i = 0; i < hashSearchResults.size(); i ++){
                    System.out.print(masterList.get(hashSearchResults.get(i)).getId() + "|" + masterList.get(hashSearchResults.get(i)));
                }
                break;
            case 'm':
                hashSearchResults.clear();
                LogEntry m = new LogEntry(LogEntry.convertTimeToLong(u.argument));
                int count = 0;
                for( LogEntry l : masterList){
                    if( m.getTimestamp() == l.getTimestamp()){
                        hashSearchResults.add(l.sortedId);
                        count ++;
                    }
                }
                System.out.print("Timestamp search: " + count + " entries found\n");
                break;
            case 'k':
                String[] kwds_split = u.argument.split("[^a-zA-Z0-9]+");
                ArrayList<String> kwds = new ArrayList<>();
                for(String s : kwds_split){
                    if(!s.isBlank()){
                        kwds.add(s);
                    }
                }
                System.out.print("Keyword search: " + keywordSearch(kwds) + " entries found\n");
                break;
            case 'r':
                Collections.sort(hashSearchResults);
                for(int i = 0; i < hashSearchResults.size(); i ++){
                    excerptList.add(masterList.get(hashSearchResults.get(i)).sortedId);
                }
                System.out.print(hashSearchResults.size() + " log entries appended\n");
                break;
            case 'q':
                System.exit(0);
                break;
            case '#':
                break;
        }
    }

    /**
     * Prints excerpt summary
     */
    private void printFirstAndLast() {
        System.out.print( 0 + "|" + getLogEntryInMasterList(excerptList.get(0)).getId() + "|" + getLogEntryInMasterList(excerptList.get(0)));
        System.out.print("...\n");
        System.out.print((excerptList.size() -1) + "|" + getLogEntryInMasterList(excerptList.get(excerptList.size() -1)).getId()
                + "|" + getLogEntryInMasterList(excerptList.get(excerptList.size()-1)));
    }

    private void checkAppropRange(ArrayList a, int i ){
        if(i < 0 || i > a.size() - 1){
            System.err.print("index not found\n");
        }
    }

    /**
     * Finds the masterlist size
     */
    public  int size(){
        return masterList.size();
    }


    /**
     * get's log entry from sorted masterlist
     * @param num entry ID
     * @return Log Entry asociated with Entry ID
     */
    private LogEntry getLogEntryInMasterList(int num){

        return masterList.get(sortedMasterList.get(num));

    }

    /**
     * Finds loge entry in masterlist from entry ID
     * @param num entry ID
     * @return Log Entry asociated with Entry ID
     */
    private LogEntry getLogEntryInMasterListfromEntryID(int num){

        return masterList.get(indexMap.get(num));

    }

    private enum LastSearch {
        None,
        Timestamp,
        Category,
        Keyword
    }

    private static class LowerBound implements Comparator<LogEntry> {

        @Override
        public int compare(LogEntry o1, LogEntry o2) {
            if(o1.getTimestamp() < o2.getTimestamp()){
                return -1;
            }
            else{
                // return "greater than" for == & >
                return 1;
            }
        }
    }

    private static class UpperBound implements Comparator<LogEntry>{

        @Override
        public int compare(LogEntry o1, LogEntry o2) {
            if(o1.getTimestamp() <= o2.getTimestamp()){
                // return < for == & <
                return -1;
            }
            else{
                return 1;
            }
        }
    }




}

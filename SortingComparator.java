import java.util.Comparator;

public class SortingComparator implements Comparator<LogEntry> {
    @Override
    public int compare(LogEntry o1, LogEntry o2) {
        if(o1.getTimestamp() < o2.getTimestamp()){
            return  -1;
        }
        else if(o1.getTimestamp() > o2.getTimestamp()){
            return 1;
        }
        else{
            if(o1.getCategory().compareToIgnoreCase(o2.getCategory()) < 0){
                return -1;
            }
            else if(o1.getCategory().compareToIgnoreCase(o2.getCategory()) > 0){
                return  1;
            }else{
                if(o1.getId() < o2.getId()){
                    return -1;
                }else{
                    return 1;
                }
            }
        }

    }


}

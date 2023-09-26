import java.util.Scanner;

public class Main {
    // processes arguments

    /**
     *This prints usage information
     */
    public static void printHelp(){
        System.out.print("Usage: Logman LOGFILE | -h | --help" + "\n"
        + "-help (-h) : prints out useful help information\n" +
                "LOGFILE : \n");
    }
    public static void main(String[] args) {
        if (args[0].equals("-h")|| args[0].equals("--help")) {
            printHelp();
            System.exit(0);
        }

        Logfile lf = new Logfile(args[0]);
        // read out the number of entries
        System.out.print(lf.size() + " entries read\n");

       // User Commands
        Scanner scn = new Scanner(System.in);
        UserCommands US  = new UserCommands();

        System.out.print("% ");
        while(scn.hasNextLine()){
            String line = scn.nextLine();
            US.UserCommandsProcessor(line);
            // start reading user commands and processing
            lf.Process(US);
            System.out.print("% ");
        }


    }
}

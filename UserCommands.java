import java.util.Objects;

public class UserCommands {
    // this filed handles user commands
    protected char command;
    protected String argument;
    protected int number;
    protected String readline ;

    public UserCommands(){
    }

    public void UserCommandsProcessor(String line){
        //entire commandline
        readline = line;

        // the character command
        command = readline.charAt(0);

        switch (command){
            // integer arguments
            case 'a':
            case 'b':
            case 'd':
            case 'e':
                argument = readline.substring(2);
                number = Integer.parseInt(argument);
                break;
            case 't':
            case 'k':
            case 'c':
            case 'm':
                argument = readline.substring(2);
                break;
            case 's':
                break;
            case 'l':
                break;
            case'g':
                break;
            case 'r':
                break;
            case 'p':
                break;
            case 'q':
                System.exit(0);
                break;
            case '#':
                break;
            default:
                System.err.print("Invalid Command\n");
                System.out.print("% ");
        }// switch
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserCommands that = (UserCommands) o;
        return command == that.command && Objects.equals(argument, that.argument);
    }

    @Override
    public int hashCode() {
        return Objects.hash(command, argument);
    }



}

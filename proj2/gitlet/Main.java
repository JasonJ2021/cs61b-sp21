package gitlet;

import java.util.ArrayList;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author TODO
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                // TODO: handle the `init` command
                Repository.init();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                String filename = args[1];
                Repository.add(filename);
                break;
            // TODO: FILL THE REST IN
            case "commit":
                // TODO: java gitlet.Main commit [message]
                String message = args[1];
                Repository.commit(message);
                break;
            case "rm":
                // TODO: java gitlet.Main rm [file name]
                Repository.rm(args[1]);
                break;
            case "log":
                // TODO: java gitlet.Main log
                Repository.log();
                break;
            case "checkout":
                if (args.length == 3) {
                    Repository.checkout1(args[2]);
                } else if (args.length == 4) {
                    Repository.checkout2(args[1], args[3]);
                }
                break;
        }
    }
}

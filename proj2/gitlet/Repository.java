package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import static gitlet.Utils.*;

// TODO: any imports you need here

/**
 * Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /*The stage area file*/

    /* TODO: fill in the rest of this class. */
    public static void init() {
        if (GITLET_DIR.exists()) {
            throw new GitletException("A Gitlet version-control system already exists in the current directory.");
        }
        GITLET_DIR.mkdir();
        File head = join(GITLET_DIR, "HEAD");
        File index = join(GITLET_DIR, "index");
        File refs = join(GITLET_DIR, "refs");
        File objects = join(GITLET_DIR, "objects");
        File heads = join(GITLET_DIR, "refs", "heads");
        /*create dir objects and refs
         * create file index and HEAD
         * */
        try {
            index.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            head.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        refs.mkdir();
        objects.mkdir();
        heads.mkdir();

        /*Create stage index*/
        Index stage = new Index();
        Utils.writeObject(index, stage);

        /*1.Create initial head
         * 2.put commit object into objects directory
         *
         * */
        Commit initial = new Commit();
        String sha1_initialCommit = saveObject(initial);

        /*Create Head & master  and point to initial*/
        File master = join(heads, "master");
        try {
            master.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.writeContents(master, sha1_initialCommit);
        Utils.writeContents(head, master.getAbsolutePath());
    }

    public static void add(String filename) {
        File file = join(CWD, filename);
        if (!file.exists()) {
            throw new GitletException("File does not exist.");
        }
        File index = join(GITLET_DIR, "index");
        Index stage = readObject(index, Index.class);
        stage.addFile(filename, file);
        writeObject(index, stage);
    }

    /***
     * By default a commit has the same file contents as its parent.
     * Files staged for addition and removal are the updates to the commit.
     * Of course, the date (and likely the mesage) will also different from the parent.
     */
    public static void commit(String message) {
        if (message == null) {
            throw new GitletException("Please enter a commit message.");
        }
        File index = join(GITLET_DIR, "index");
        Index stage = readObject(index, Index.class);
        if (stage.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        Commit newcommit = new Commit(message);
        //handle stage for addition
        for (String add : stage.getAddStage().keySet()) {
            String file = stage.getAddStage().get(add);
            String sha = saveByteOrString(file);
            newcommit.addFile(add, sha);
        }
        //handle stage for removal
        for (String removal : stage.getRemoveStage()) {
            newcommit.removeFile(removal);
        }
        //clear current index and save
        stage.clear();
        Utils.writeObject(index, stage);
        //save commit
        String newCommitsha = saveObject(newcommit);
        //move branch
        File head = join(GITLET_DIR, "HEAD");
        File branch = new File(Utils.readContentsAsString(head));
        Utils.writeContents(branch, newCommitsha);
    }

    /**
     * Unstage the file if it is currently staged for addition.
     * If the file is tracked in the current commit, stage it for removal and remove the file from the working directory
     * if the user has not already done so (do not remove it unless it is tracked in the current commit).
     */
    public static void rm(String filename) {
        File index = join(GITLET_DIR, "index");
        Index stage = readObject(index, Index.class);
        //unstage the file
        boolean flag = stage.unStage(filename);
        //If this file is tracked in the current commit , stage it for removal
        if (isTracked(filename)) {
            stage.removeFile(filename);
            Utils.restrictedDelete(join(CWD, filename));
            flag = true;
        }
        //save index
        Utils.writeObject(index, stage);
        if (flag == false) {
            throw new GitletException("No reason to remove the file.");
        }
    }


    /**
     * merge case hasn't implemented yet
     * print out the head commit
     */
    public static void log() {
        Commit point = getHeadCommit();
        while (true) {
            System.out.println("===");
            System.out.println("commit " + sha1(Utils.serialize(point)));
            System.out.println("Date: " + point.getDate());
            System.out.println(point.getMessage());
            System.out.println();
            if (point.getParent() == null) {
                break;
            } else {
                point = Utils.readObject(searchObject(point.getParent()), Commit.class);
            }
        }
    }

    /***
     * Usage: java gitlet.Main checkout -- [file name]
     * Takes the version of the file as it exists in the head commit and puts it in the working directory,
     * overwriting the version of the file that’s already there if there is one.
     * The new version of the file is not staged.
     * @param filename
     *
     */
    public static void checkout1(String filename) {

        Commit head = getHeadCommit();
        if (!head.containBlob(filename)) {
            throw new GitletException("File does not exist in that commit.");
        }
        File file = join(CWD, filename);
        File commitFile = searchObject(head.getFilesha(filename));
        String s = Utils.readContentsAsString(commitFile);
        Utils.writeContents(file, s);
    }

    /***Takes the version of the file as it exists in the commit with the given id,
     * and puts it in the working directory,
     * overwriting the version of the file that’s already there if there is one.
     * The new version of the file is not staged.
     *
     * @param commitId
     * @param filename
     */
    public static void checkout2(String commitId, String filename) {
        Commit head = Utils.readObject(searchWithPre(commitId), Commit.class);
        if (!head.containBlob(filename)) {
            throw new GitletException("File does not exist in that commit.");
        }
        File file = join(CWD, filename);
        File commitFile = searchObject(head.getFilesha(filename));
        String s = Utils.readContentsAsString(commitFile);
        Utils.writeContents(file, s);
    }

    /***
     * check whether a file is tracked by current commit
     * @param filename
     * @return
     */
    public static boolean isTracked(String filename) {
        Commit head = getHeadCommit();
        return head.fileTracked(filename);
    }

    public static String getHeadCommitSha() {
        File head = join(GITLET_DIR, "HEAD");
        File branch = new File(Utils.readContentsAsString(head));
        String commit = Utils.readContentsAsString(branch);
        return commit;
    }

    public static Commit getHeadCommit() {
        File head = join(GITLET_DIR, "HEAD");
        File branch = new File(Utils.readContentsAsString(head));
        String commit = Utils.readContentsAsString(branch);
        return Utils.readObject(searchObject(commit), Commit.class);
    }

    public static File searchWithPre(String sha) {
        String shaDir = sha.substring(0, 2);
        String prefix = sha.substring(3, sha.length());
        File dir = join(GITLET_DIR, "objects", shaDir);

        List<String> list = Utils.plainFilenamesIn(dir);
        String shafile = "";
        for (String s : list) {
            if (s.startsWith(prefix)) {
                shafile = s;
                break;
            }
        }
        File file = join(GITLET_DIR, "objects", shaDir, shafile);
        return file;
    }

    public static File searchObject(String sha) {
        String shaDir = sha.substring(0, 2);
        String shaFile = sha.substring(3, sha.length());
        File file = join(GITLET_DIR, "objects", shaDir, shaFile);
        if (!file.exists()) {
            throw new GitletException("File does not exist.");
        }
        return file;
    }

    /***
     * save object to objects directory
     *
     * @param object
     * @return sha-1 of object
     */
    public static String saveByteOrString(Object object) {
        String sha1 = sha1(object);
        String shaDir = sha1.substring(0, 2);
        String shaFile = sha1.substring(3, sha1.length());

        File dir = join(GITLET_DIR, "objects", shaDir);
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = join(dir, shaFile);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Utils.writeContents(file, object);
        return sha1;
    }

    public static String saveObject(Serializable object) {
        String sha1 = sha1(Utils.serialize(object));
        String shaDir = sha1.substring(0, 2);
        String shaFile = sha1.substring(3, sha1.length());

        File dir = join(GITLET_DIR, "objects", shaDir);
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = join(dir, shaFile);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Utils.writeObject(file, object);
        return sha1;
    }
}

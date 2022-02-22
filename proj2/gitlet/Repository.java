package gitlet;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

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

    public static void global_log() {

        File heads = join(GITLET_DIR, "refs", "heads");
        Map<String, Boolean> map = new HashMap<>();
        for (String branch : Utils.plainFilenamesIn(heads)) {
            File branchHead = join(heads, branch);
            String commitSha = Utils.readContentsAsString(branchHead);
            Commit point = readObject(searchObject(commitSha), Commit.class);
            while (true) {
                commitSha = sha1(Utils.serialize(point));
                if (map.containsKey(commitSha)) {
                    break;
                }
                System.out.println("===");
                map.put(commitSha, Boolean.TRUE);
                System.out.println("commit " + commitSha);
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
     * Takes all files in the commit at the head of the given branch,
     * and puts them in the working directory,
     * overwriting the versions of the files that are already there if they exist.
     * Also, at the end of this command, the given branch will now be considered the current branch (HEAD).
     * Any files that are tracked in the current branch but are not present in the checked-out branch are deleted.
     * The staging area is cleared, unless the checked-out branch is the current branch (see Failure cases below).
     * @param branchName
     */
    public static void checkout3(String branchName) {
        File heads = join(GITLET_DIR, "refs", "heads");
        List<String> headlist = plainFilenamesIn(heads);
        //If no branch with that name exists, print No such branch exists.
        if (!headlist.contains(branchName)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        File head = join(GITLET_DIR, "HEAD");
        File headbranch = new File(Utils.readContentsAsString(head));
        Commit currentHeadCommit = Utils.readObject(searchObject(Utils.readContentsAsString(headbranch)), Commit.class);
        //If that branch is the current branch, print No need to checkout the current branch.
        if (branchName.equals(headbranch.getName())) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        //If a working file is untracked in the current branch and would be overwritten by the checkout,
        // print There is an untracked file in the way; delete it, or add and commit it first.
        File newheadbranch = join(heads, branchName);
        String commitSha = Utils.readContentsAsString(newheadbranch);
        Commit newheadCommit = Utils.readObject(searchObject(commitSha), Commit.class);
        for (String s : Utils.plainFilenamesIn(CWD)) {
            if (!currentHeadCommit.fileTracked(s) && !newheadCommit.isSameBlob(s, getFileSha(join(CWD, s)))) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }

        //delete current CWD
        for (String s : Utils.plainFilenamesIn(CWD)) {
            Utils.restrictedDelete(join(CWD, s));
        }

        //retrive file in newHeadCommit
        for (String s : newheadCommit.getBlobs().keySet()) {
            checkout2(commitSha, s);
        }

        //clear the stage
        File index = join(GITLET_DIR, "index");
        Index stage = Utils.readObject(index, Index.class);
        stage.clear();
        Utils.writeObject(index, stage);

        //write back to Head
        Utils.writeContents(head, newheadbranch.getAbsolutePath());
    }

    /***
     * Creates a new branch with the given name,
     * and points it at the current head commit.
     * A branch is nothing more than a name for a reference (a SHA-1 identifier) to a commit node.
     * This command does NOT immediately switch to the newly created branch (just as in real Git).
     * Before you ever call branch, your code should be running with a default branch called “master”.
     * @param branchName
     */
    public static void branch(String branchName) {
        String headCommitSha = getHeadCommitSha();
        File refs = join(GITLET_DIR, "refs", "heads", branchName);
        if (refs.exists()) {
            throw new GitletException("A branch with that name already exists.");
        }
        try {
            refs.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.writeContents(refs, headCommitSha);
    }

    /***
     * Displays what branches currently exist, and marks the current branch with a *.
     * Also displays what files have been staged for addition or removal.
     * An example of the exact format it should follow is as follows.
     * === Branches ===
     * *master
     * other-branch
     *
     * === Staged Files ===
     * wug.txt
     * wug2.txt
     *
     * === Removed Files ===
     * goodbye.txt
     *
     * === Modifications Not Staged For Commit ===
     * junk.txt (deleted)
     * wug3.txt (modified)
     *
     * === Untracked Files ===
     * random.stuff
     */
    public static void status() {
        //print all branch
        File head = join(GITLET_DIR, "HEAD");
        File branch = new File(Utils.readContentsAsString(head));
        String commit = Utils.readContentsAsString(branch);
        Commit headCommit = Utils.readObject(searchObject(commit), Commit.class);

        String headBranchName = branch.getName();
        File heads = join(GITLET_DIR, "refs", "heads");
        System.out.println("=== Branches ===");
        System.out.println("*" + headBranchName);
        for (String s : Utils.plainFilenamesIn(heads)) {
            if (s.equals(headBranchName)) {
                continue;
            }
            System.out.println(s);
        }
        System.out.println();

        //print Staged Files
        File index = join(GITLET_DIR, "index");
        Index stage = Utils.readObject(index, Index.class);
        System.out.println("=== Staged Files ===");
        for (String s : stage.getAddStage().keySet()) {
            System.out.println(s);
        }
        System.out.println();

        //print Removed Files
        System.out.println("=== Removed Files ===");
        for (String s : stage.getRemoveStage()) {
            System.out.println(s);
        }
        System.out.println();

        //print Modifications Not Staged For Commit
        /*A file in the working directory is “modified but not staged”
        if it is one of the following cases
        * Tracked in the current commit, changed in the working directory, but not staged
        * Staged for addition, but with different contents than in the working directory
        * Staged for addition, but deleted in the working directory
        * Not staged for removal, but tracked in the current commit and deleted from the working directory
        * */

        System.out.println("=== Modifications Not Staged For Commit ===");
        List<String> strings = plainFilenamesIn(CWD);
        for (String s : strings) {
            File file = join(CWD, s);
            String fileSha = getFileSha(file);
            if (isTracked(s) && !headCommit.isSameBlob(s, fileSha) && !stage.isStaged(s)) {
                System.out.println(s + " (modified)");
            } else if (stage.isOnAddStage(s) && !fileSha.equals(Utils.sha1(stage.getFile(s)))) {
                System.out.println(s + " (modified)");
            }
        }
        for (String s : stage.getAddStage().keySet()) {
            if (!strings.contains(s)) {
                System.out.println(s + " (deleted)");
            }
        }
        for (String s : headCommit.getBlobs().keySet()) {
            if (!strings.contains(s) && !stage.isOnRemoveStage(s)) {
                System.out.println(s + " (deleted)");
            }
        }
        System.out.println();

        System.out.println("=== Untracked Files ===");
        for (String s : strings) {
            if (!headCommit.containBlob(s) && !stage.isStaged(s)) {
                System.out.println(s);
            }
        }
        System.out.println();

    }


    public static void find(String message) {
        File heads = join(GITLET_DIR, "refs", "heads");
        Map<String, Boolean> map = new HashMap<>();
        for (String branch : Utils.plainFilenamesIn(heads)) {
            File branchHead = join(heads, branch);
            String commitSha = Utils.readContentsAsString(branchHead);
            Commit point = readObject(searchObject(commitSha), Commit.class);
            while (true) {
                commitSha = sha1(Utils.serialize(point));
                if (map.containsKey(commitSha)) {
                    break;
                }
                if (point.getMessage().equals(message)) {
                    System.out.println(commitSha);
                }
                if (point.getParent() == null) {
                    break;
                } else {
                    point = Utils.readObject(searchObject(point.getParent()), Commit.class);
                }
            }
        }
    }

    /***
     * Deletes the branch with the given name.
     * This only means to delete the pointer associated with the branch;
     * it does not mean to delete all commits that were created under the branch, or anything like that.
     * @param branchName
     */
    public static void rm_branch(String branchName) {
        File head = join(GITLET_DIR, "HEAD");
        File head_branch = new File(Utils.readContentsAsString(head));
        if (head_branch.getName().equals(branchName)) {
            throw new GitletException("Cannot remove the current branch.");
        }
        File branch = join(GITLET_DIR, "refs", "heads", branchName);
        if (!branch.exists()) {
            throw new GitletException("A branch with that name does not exist.");
        }
        branch.delete();
//        String rm_commit_sha = Utils.readContentsAsString(branch);
//        Commit rm_commit = Utils.readObject(searchObject(rm_commit_sha) , Commit.class);
//        String parent_commit = rm_commit.getParent();

    }


    public static void merge(String branchName) {
        File index = join(GITLET_DIR, "index");
        Index stage = readObject(index, Index.class); // get stage
        File head = join(GITLET_DIR, "HEAD");
        File headbranch = new File(Utils.readContentsAsString(head));   //get headbranchFile contains headCommit
        String curBranchName = headbranch.getName();        //headBranch Name , e.g. master.
        /*======================check Failure cases=================*/
        if (curBranchName.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        if (!stage.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        Commit curBranch = getHeadCommit();         //get headCommit
        File branch = join(GITLET_DIR, "refs", "heads", branchName);
        if (!branch.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        for (String s : Utils.plainFilenamesIn(CWD)) {
            if (!curBranch.containBlob(s) && !stage.isStaged(s)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        /*=============================================================*/


        Commit givenBranch = readObject(searchObject(Utils.readContentsAsString(branch)), Commit.class);
        Commit splitCommit = findSplit(curBranch, givenBranch);
        String curSha = sha1(Utils.serialize(curBranch));
        String givenSha = sha1(Utils.serialize(givenBranch));
        String splitSha = sha1(Utils.serialize(splitCommit));
        if (splitSha.equals(givenSha)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        } else if (splitSha.equals(curSha)) {
            checkout3(branchName);
            Utils.writeContents(headbranch, givenSha);
            Utils.writeContents(head , headbranch.getAbsolutePath());
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }

        TreeSet<String> fileSet = new TreeSet<>();
        for (String s : curBranch.getBlobs().keySet()) {
            fileSet.add(s);
        }
        for (String s : givenBranch.getBlobs().keySet()) {
            fileSet.add(s);
        }
        for (String s : splitCommit.getBlobs().keySet()) {
            fileSet.add(s);
        }

        for (String s : fileSet) {
            if (splitCommit.containBlob(s) && curBranch.containBlob(s)) {
                if (splitCommit.getFilesha(s).equals(curBranch.getFilesha(s))) {
                    if (givenBranch.containBlob(s)) {
                        stage.addFile(s, searchObject(givenBranch.getFilesha(s)));
                    } else {
                        stage.removeFile(s);
                    }
                } else if (givenBranch.containBlob(s) && !splitCommit.getFilesha(s).equals(givenBranch.getFilesha(s))) {
                    if (!givenBranch.getFilesha(s).equals(curBranch.getFilesha(s))) {
                        //conflict;
                        //<<<<<<< HEAD
                        //contents of file in current branch
                        //=======
                        //contents of file in given branch
                        //>>>>>>>
                        String newString = "<<<<<<< HEAD\n";
                        newString += readContentsAsString(searchObject(curBranch.getFilesha(s))) + "\n";
                        newString += "=======\n";
                        newString += readContentsAsString(searchObject(givenBranch.getFilesha(s))) + "\n";
                        newString += ">>>>>>>";
                        File file = join(CWD, s);
                        stage.addFile(s, file);
                    }
                }
            } else if (!splitCommit.containBlob(s) && !curBranch.containBlob(s)) {
                stage.addFile(s, searchObject(givenBranch.getFilesha(s)));
            }
        }

        //merge COmmit

        if (stage.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        Commit newcommit = new Commit(curSha, givenSha, branchName, curBranchName);
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
        Utils.writeContents(headbranch, newCommitsha);
        checkout3(curBranchName);
    }

    /***
     * find a SplitCommit for curBranch
     * @param curBranch
     * @param givenBranch
     * @return
     */
    public static Commit findSplit(Commit curBranch, Commit givenBranch) {
        Map<String, Boolean> map = new HashMap<>();
        Commit point = curBranch;
        while (true) {
            map.put(sha1(serialize(point)), Boolean.TRUE);
            if (point.getParent() == null) {
                break;
            } else {
                point = Utils.readObject(searchObject(point.getParent()), Commit.class);
            }
        }
        point = givenBranch;
        while (true) {
            if (map.containsKey(sha1(serialize(point)))) {
                break;
            }
            if (point.getParent() == null) {
                break;
            } else {
                point = Utils.readObject(searchObject(point.getParent()), Commit.class);
            }
        }
        return point;
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

    public static String getObjectSha(Serializable object) {
        return Utils.sha1(serialize(object));
    }

    public static String getFileSha(File file) {
        String sha = Utils.sha1(Utils.readContentsAsString(file));
        return sha;
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

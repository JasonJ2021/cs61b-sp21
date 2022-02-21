package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.TreeMap;
import java.util.TreeSet;

//Index -> StageArea
public class Index implements Serializable {
    private TreeMap<String, String> addStage;
    private TreeSet<String> removeStage;

    public Index() {
        addStage = new TreeMap<>();
        removeStage = new TreeSet<>();
    }
    public TreeMap<String, String > getAddStage(){
        return addStage;
    }
    public TreeSet<String> getRemoveStage(){
        return removeStage;
    }
    /*if this file is same as the curent commit version
    * don't stage it , and remove it from the stage area if it's already there
    *
    * */
    public void addFile(String filename , File file) {
        Commit headCommit = Repository.getHeadCommit();
        if(headCommit.isSameBlob(filename , Utils.sha1(Utils.serialize(file)))){
            if(addStage.containsKey(filename)){
                addStage.remove(filename);
            }
            return;
        }
        addStage.put(filename, Utils.readContentsAsString(file));
    }

    /***
     *
     * @param filename
     * @return True if do unstage a file
     */
    public boolean unStage(String filename){
        boolean flag = false;
        if(addStage.containsKey(filename)){
            flag = true;
            addStage.remove(filename);
        }
        return flag;
    }
    public boolean isStaged(String filename){
        return addStage.containsKey(filename) || removeStage.contains(filename);
    }
    public boolean isOnAddStage(String filename){
        return addStage.containsKey(filename);
    }
    public boolean isOnRemoveStage(String filename){
        return removeStage.contains(filename);
    }
    public String getFile(String s){
        return addStage.get(s);
    }
    public void removeFile(String filename) {
        removeStage.add(filename);
    }
    public void clear(){
        this.addStage.clear();
        this.removeStage.clear();
    }
    public boolean isEmpty(){
        return addStage.isEmpty() && removeStage.isEmpty();
    }

}

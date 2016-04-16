package irproject20162;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MergeRun implements Runnable {
    private List<File> files = new ArrayList<>();
    private int i;

    MergeRun(List<File> files, int i) {
        this.files=files;
        this.i=i;
    }
    
    @Override
    public void run() {
        try {
            ExternalMergeSort.mergeSortedFiles(files, new File("newSorted"+i+".txt"));
        } catch (IOException ex) {
            Logger.getLogger(MergeRun.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

package irproject20162;

import static irproject20162.IRProject20162.countFiles;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QueryTask implements Runnable {

    private int query;
    QueryTask(int query){
        this.query = query;
    }
    
    @Override
    public void run() {
        int countFiles=0;
        try {
            countFiles = countFiles(new File("1.txt"), 1);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(QueryTask.class.getName()).log(Level.SEVERE, null, ex);
        }
        Query qe = new Query();
        try {
            qe.readTerms(new File("query.txt"), query);
        } catch (IOException ex) {
            Logger.getLogger(QueryTask.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            qe.saveL(countFiles);
        } catch (IOException ex) {
            Logger.getLogger(QueryTask.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            qe.topK(new File("index.txt"), countFiles-1);
        } catch (IOException ex) {
            Logger.getLogger(QueryTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

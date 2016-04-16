/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irproject20162;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kon
 */
public class SortRun implements Runnable{

    private int file1;
    
    SortRun(int file1) {
        this.file1=file1;
    }
    
    @Override
    public void run() {
        try {
            ExternalMergeSort ems = new ExternalMergeSort("seg"+file1+".txt", "sorted"+file1+".txt");
        } catch (IOException ex) {
            Logger.getLogger(SortRun.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

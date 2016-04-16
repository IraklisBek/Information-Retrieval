package irproject20162;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TokenRun implements Runnable{

    private int file1;
    private int file2;
    
    TokenRun(int file1, int file2) {
        this.file1=file1;
        this.file2=file2;
    }
    
    @Override
    public void run() {
        InvertedIndex ii = new InvertedIndex();
        try {
            ii.tokenization(new File(file1+".txt"), file1, new File("seg"+file2+".txt"));
        } catch (IOException ex) {
            Logger.getLogger(TokenRun.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
}

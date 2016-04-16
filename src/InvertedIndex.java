package irproject20162;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class InvertedIndex {
    //Map με key έναν όρο και value την συχνότητα όρου σε αρχείο
    private Map<String, HashMap<Integer, Integer>> td;
    //Map όπου key έχει έναν όρο και value μια λίστα με τα απο HashMaps 
    //που έχουν κλειδί κάποιο doc που υπάρχει ο όρος και value τη συχνότητα
    private Map<String, ArrayList<HashMap<String, String>>> index;

    public InvertedIndex(){
        td = new HashMap<>();
        index = new HashMap<>();
    }
    /**
     * Διαβάζει ένα αρχείο file και για κάθε λέξη του αρχείου αυτού
     * γραφει πόσες φορές υπάρχει η λέξη σε μορφή (word, doc, f)
     * στο αρχείο segFile
     * @param file      το αρχείο που διαβάζει    
     * @param file_num  ο αριθμός του αρχείου
     * @param segFile   το αρχείο που γράφει τα ζευγάρια
     */
    public void tokenization(File file, int file_num, File segFile) throws FileNotFoundException, IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line = null;
        while( (line = br.readLine())!= null ){
            String [] tokens = line.split("\\s+");           
            for(int i=0; i<tokens.length; i++){//Για κάθε όρο του αρχείου
                if(td.containsKey(tokens[i])){//αν υπάρχει ο όρος στο Map
                    HashMap get_count = td.get(tokens[i]);
                    Integer count = (Integer) get_count.get(file_num);
                    get_count.put(file_num, ++count);//κάνε update την συχότητα του όρου 
                    td.put(tokens[i], get_count);//βάλε την καινούργια συχνότητα στο Map
                }else{//αλλιώς
                    HashMap<Integer, Integer> ftd = new HashMap<>();
                    ftd.put(file_num, 1);
                    td.put(tokens[i], ftd);//βάλε τον όρο στο Map με συχνότητα 1
                }
            }
            try {
                FileWriter fw = new FileWriter(segFile, true);
                PrintWriter pw = new PrintWriter(fw);      
                
                /*Γράψε το td Map σε αρχείο segFile*/
                for(String word : td.keySet()){
                    for(Integer doc : td.get(word).keySet()){
                        Integer f = td.get(word).get(doc);
                        pw.write(word+" "+doc+" "+f+" ");
                        pw.println(); 
                    }   
                }
                pw.println();             
                pw.close();
                fw.close();
            } catch (IOException ex) {
            }           
        }
    }
    
    /**
     * Συνάρτηση που δημιουργεί τον κατάλογο αφού έχει προηγηθεί η τεχνική ταξινόμησης.
     * @param file      Το ταξινομημένο αρχείο κατά όρους μαζί με τα ζευγάρια (t, doc, ftdoc)
     * @param indexFile O τελικός κατάλογος
     */
    public void creatInvertedIndex(File file, File indexFile) throws IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line = null;
        String temp=null;
        //Χρήσιμη μεταβλητή για να ξέρουμε σε πιο ζευγάρι κάποιου όρου βρισκόμαστε στην γραμμή 94
        int lines=0;
        /*
        tokens[0] = όρος
        tokens[1] = doc
        tokens[2] = ftdoc
        */
        while( (line = br.readLine())!= null ){//Διαβάζει μία μία την γραμμή του ταξινομημένου αρχείου
            String [] tokens = line.split("\\s+");
            if(tokens.length<2)
                continue;
            //(βλέπε πρώτα, σχόλια στο else)
            if(tokens[0].equals(temp)){
                ArrayList<HashMap<String, String>> all = index.get(temp);//παίρνει το προηγούμενο list
                HashMap<String, String> hmm = all.get(lines-1);//παίρνει το προηγούμενο map
                hmm.put(tokens[1], tokens[2]);//και προσθέτει καινούργιο ζευγάρι
                all.add(hmm);
                index.put(temp, all);//Κάνει update το list του όρου                 
            }else{                
                temp = tokens[0];//αποθηκεύει σε μεταβλητή τον όρο
                try {
                    FileWriter fw = new FileWriter(indexFile, true);
                    PrintWriter pw = new PrintWriter(fw);
                    for(String token : index.keySet()){//Για κάποιον όρο
                        ArrayList<HashMap<String, String>> all = index.get(token);
                        pw.write(token+" ");
                        HashMap<String, String> mh = all.get(all.size()-1);
                        for(String docid : mh.keySet()){
                            pw.write(docid+" "+mh.get(docid)+" ");//αποθήκευσε στον κατάλογο
                        }
                    }
                    pw.println();             
                    pw.close();
                    fw.close();
                } catch (IOException ex) {

                }  
                index=null;//Αν έχει δημιουργηθεί πιο πριν κάποιο index το διαγράφει διότι δεν χρειάζεται ποια,
                         //αφού ο όρος αν ξαναβρεθεί θα τρέξει ο κώδικας μέσα στην παραπάνω if. 
                         //Επίσης έτσι γλιτώνουμε και θέματα ανεπάρκειας μνήμης.
                index=new HashMap<>();
                lines=0;
                ArrayList<HashMap<String, String>> al = new ArrayList<>();
                HashMap<String, String> hm = new HashMap<>();
                hm.put(tokens[1], tokens[2]);
                al.add(hm);
                index.put(temp, al);//βάζει στο Index τον όρο μαζί με ένα ζευγάρι doc, ftdoc
                
            }            
            lines++;           
        }          
    }

    
    public void createTxts(File file) throws FileNotFoundException, IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line = null;
        int txt=1;
        while( (line = br.readLine())!= null ){
            String [] tokens = line.split("\\s+");
            
            try {
                FileWriter fw = new FileWriter(txt+".txt", true);
                PrintWriter pw = new PrintWriter(fw);
                for(int i=0; i<tokens.length; i++){
                    if(i!=0){
                        pw.write(tokens[i]+" ");
                    }
                }
                pw.write("\n");                
                pw.close();
                fw.close();
            } catch (IOException ex) {

            }
            txt++;
        }
    }
}

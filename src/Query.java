package irproject20162;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Query {
    //ArrayList that holds the terms of the query
    private ArrayList<String> terms;
    int topk;
    int queryNum;
    
    
    public Query(){
        terms = new ArrayList<>();
        topk=0;
        queryNum=0;
    }
    /**
     * Διαβάζει τους όρους ενώς Query και τους αποθηκεύει στο terms. 
     * Επίσης αποθηκεύει σε μια μεταβλητή τον αριθμό των σχετικών documents που θα επιστραφούν
     * @param query Το αρχείο των Queries
     */
    public void readTerms(File query, int queryNum) throws FileNotFoundException, IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(query)));
        String line = null;
        int l=0;
        int lineNum=0;
        while( (line = br.readLine())!= null ){
            if(l==0){
                l++;
                continue;
            }
            lineNum++;
            if(lineNum!=queryNum)
                continue;
            String [] tokens = line.split("\\s+");
            this.queryNum=Integer.parseInt(tokens[0]);
            topk=Integer.parseInt(tokens[1]);
            for(int i=2; i<tokens.length; i++){
                terms.add(tokens[i]);
            }
        }     
    }
    /**
     * Διαβάζει τα documents ένα ένα και αποθηκεύει σε ένα αρχείο το Ld τους.
     * Η πρώτη γραμμή του αρχείου είναι το Ld του 1.txt η δεύτερη το Ld του 2.txt κ.ο.κ.
     * Το Ld του κάθε αρχείου είναι η ρίζα του πλήθους των μοναδικών όρων που υπάρχουν στο έγγραφο.
     * @param files ο αριθμός των αρχείων για τα οποία θα αποθηκευτεί το Ld
     */
    public void saveL(int files) throws FileNotFoundException, IOException{
        for(int i=1; i<files;i++){
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(i+".txt")));
            String line = null;
            line = br.readLine();
            String [] tokens = line.split("\\s+"); 
            HashMap<String, Integer> t = new HashMap<>();
            try {
                FileWriter fw = new FileWriter("L.txt", true);
                PrintWriter pw = new PrintWriter(fw); 
                /*for(int j=0; j<tokens.length; j++){
                    sumW.put(tokens[j], 1);                        
                    for(String token : sumW.keySet()){ 
                        if(token.equals(tokens[j])){
                            sumW.put(tokens[j], sumW.get(token)+1);
                        }
                    }               
                }
                double ld=0.0;
                for(String token : sumW.keySet()){
                    ld+=Math.log(1 + sumW.get(token));  //idf ??
                }*/
                for(int j=0; j<tokens.length; j++){
                    t.put(tokens[j], 1);
                }
                double a = Math.sqrt(t.size());
                pw.printf("%s",Double.toString(a));
                pw.println();               
                pw.close();
                fw.close();
            } catch (IOException ex) {
            }
        }
           
    }  
    
    /**
     * Τυπώνει τα top-k σχετικά έγγραφα ένα query.
     * @param index     ο κατάλογος
     * @param N         ο αριθμός των αρχείων
     */
    public void topK(File index, double N) throws IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(index)));
        String line = null;
        //Αρχεία στα οποία υπάρχει ο όρος t
        double nt;
        //Συντελεστής για να βρούμε το βάρος ενός όρου
        double idf;
        //Συντελεστής για να βρούμε το βάρος ενός όρου
        double tf;
        //Βάρος για έναν όρο
        double w;
        //HashMap Συσσορευτών με κλειδί τον αριθμό ενός doc και value την τιμή του συσσορευτή
        Map<Integer, Double> S = new HashMap<>();
        while( (line = br.readLine())!= null ){
            String [] tokens = line.split("\\s+"); 
            if(terms.contains(tokens[0])){//Αν βρει έναν όρο του query που υπάρχει στον κατάλογο
                nt=(int) tokens.length/2;
                idf=Math.log(1+ N/nt);
                for(int i=1; i<tokens.length; i=i+2){//για κάθε string της γραμμής του καταλόγου εκτός από το πρώτο
                    int doc = Integer.parseInt(tokens[i]);
                    tf = 1 + Math.log(Double.parseDouble(tokens[i+1]));
                    w = tf*idf;//χρησιμοποίησε το για να βρεις το βάρος του όρου σύμφωνα με την τεχνική του δυανισματικού μοντέλου
                    double ss=0;
                    if(S.containsKey(doc)){//Αν το HashMap συσσορευτών περιέχει τον συσσορευτή
                        ss = S.get(doc);//πάρε την τιμή του
                    }
                    ss = ss + w;//η τιμή του καινούργιου συσσορευτή θα γίνει ίση με το βάρος του όρου + την τιμή του προηγούμενου συσσορευτή
                    S.put(doc, ss);
                }
            }
        }
        for(Integer doc : S.keySet()){//Για κάθε συσσορευτή
            BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream("L.txt")));
            String line2 = null;
            int i=0;
            String [] tokens2=null;
            while( (line2 = br2.readLine())!= null ){
                tokens2 = line2.split("\\s+"); 
                if(i==doc){//αν η γραμμή του L.txt είναι ίση με τον αριθμό του συσσορευτή
                    break;
                }
                i++;
            }   
            Double ss = S.get(doc);//πάρε την τιμή αυτού του συσσορευτή
            S.put(doc, ss/Double.parseDouble(tokens2[0]));// και κάντην update διερώντας την με το Ld του    
        }        
        //Ταξινόμησε σε φθίνουσα διάταξη τις τημές των συσσορευτών
        TreeMap<Double, Integer> sortedMap = new TreeMap<>(new Comparator<Double>() {
          public int compare(Double a, Double b) {
            return b.compareTo(a);
          }
        });
        for (Map.Entry entry : S.entrySet()) {
            sortedMap.put((Double) entry.getValue(), (Integer)entry.getKey());
        }
        int i=0;
        //Εκτύπωσε τα top-k αρχεία
        System.out.println(queryNum +" query top files\n********");
        for(Double token : sortedMap.keySet()){            
            if(i==topk)
                break;
            System.out.println(token + " " + sortedMap.get(token));
            i++;
        }
        System.out.println();

    }
    
    
}

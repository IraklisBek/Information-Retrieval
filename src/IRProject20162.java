package irproject20162;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class IRProject20162 {
    //Η countFiles μετραει τα αρχεια μορφης "number.txt"
    public static int countFiles(File file, int i) throws FileNotFoundException{        
        while(file.exists()){
            i++;
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            file= new File(i+".txt");            
        }
        return i;        
    }
    //Η countSortedFiles μετραει τα αρχεια μορφης "sortednumber.txt"
    public static int countSortedFiles(File file, int i) throws FileNotFoundException{       
        while(file.exists()){
            i++;
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            file= new File("sorted"+i+".txt");
            
        }
        return i;       
    }
    //Η checkExecutors ελεγχει αν εχουν τελειωσει ολα τα task που υπαρχουν στην λιστα tasks
    public static void checkExecutors(List<Future> tasks) throws InterruptedException, ExecutionException{        
        boolean done=false;
        while(done==false) {
            done=true;
            for (int i=0; i<tasks.size(); i++){
                //Παιρνει απο την λιστα ενα απο τα αντικειμενα
                Future future =tasks.get(i);
		//αν δεν ειναι null σημαινει οτι δεν εχει τελειωσει το task αρα σταματαει την επαναληψη κ ξαναελεγχει απο την αρχη
                if(future.get()!=null){
                    done=false;
                    break;
                }
            }
        }
    }
    
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        //InvertedIndex t = new InvertedIndex();
        //t.createTxts(new File("data1.txt"));

        InvertedIndex ii = new InvertedIndex();
		
        List<Future> tasks = new ArrayList<>();
	//Βρισκει τον αριθμο επεξεργαστων του συστηματος 
        int MAX_THREADS = Runtime.getRuntime().availableProcessors();
        System.out.println("Create Inverted index with " +MAX_THREADS+ " Threads:");
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        System.out.println(dateFormat.format(date)); 
	//Δημιουργει το executor με οσα threads εχει το συστημα
        ExecutorService executor1 = Executors.newFixedThreadPool(MAX_THREADS);
        int segi=1;
	//Μετραει τον αριθμο των αρχειων μορφης "αριθμος.txt"
        int countFiles=countFiles(new File("1.txt"), 1);
	//Για καθε αρχειο δημειουργει ενα tokentask που θα φτιαξει αρχειο seg με tokens μορφης "λεξη" "αριθμος εγγραφου" "αριθμος εμφανησεων"
        for(int i=1; i<countFiles; i++){
            Runnable tokentask = new TokenRun(i,segi);
            tasks.add(executor1.submit(tokentask));
            //Για καθε 2 αρχεια δημιουργειτε 1 seg για αυτο το seg ανεβαινει ανα δυο επαναληψεις
            if(i%2==0){
                segi++;
            }
        }
        //Ελεγχει αν ολα τα tasks εχουν τελειωσει πριν προχωρησει παρακατω
        checkExecutors(tasks);
	//Καθαριζει την λιστα  
        tasks.clear();
        ExecutorService executor2 = Executors.newFixedThreadPool(MAX_THREADS);
	//Διαιρει τον αριθμο των αρχειων για να βρει ποσα αρχει seg υπαρχουν
        if(countFiles%2==1){
            countFiles=countFiles/2;
        }else{
            countFiles=countFiles/2 - 1;           
        }
	//Για καθε seg δημειουργει και τρεχει παραλληλα ενα mergetask που ταξινομει αλφαβητικα τα tokens
        for(int i=1; i<countFiles; i++){
            Runnable sorttask = new SortRun(i);
            tasks.add(executor2.submit(sorttask));
        }       
        checkExecutors(tasks);
        
        tasks.clear();
	//Μετραει τα sorted αρχεια
        int count = countSortedFiles(new File("sorted1.txt"), 0);
        int numfiles = count/MAX_THREADS;
        ExecutorService executor3 = Executors.newFixedThreadPool(MAX_THREADS);
        int j=0;
	//Η μεταβλητη counter μετραει σε ποιο αρχειο εχουμε μεινει
        int counter=1;
		
        int end;
		
        for(int i=1; i<MAX_THREADS+1; i++){
            List<File> files = new ArrayList<>();
            //Υπολογιζει μεχρι πιο αρχειο θα παει αυτη η επαναληψη
            end=numfiles*i;
            for(j=counter; j<end; j++){
                counter++;
                files.add(new File("sorted"+j+".txt"));
            }
            //Δημιουργει ενα mergetask που κανει merge τα ταξινομημενα αρχεια που του δινονται
            Runnable mergetask = new MergeRun(files, i);
            tasks.add(executor3.submit(mergetask));
            
            
        }       
        checkExecutors(tasks);
        tasks.clear();
        List<File> files = new ArrayList<>();
        for(int i=1; i<MAX_THREADS+1; i++){
            files.add(new File("newSorted"+i+".txt"));
        }
        
	//Τα ενωμενα αρχεια που δημιουργηθικαν γινονται merge σε ενα τελευταιο αρχειο
        ExternalMergeSort.mergeSortedFiles(files, new File("finalSorted.txt"));
	//Δημιουργει τον αντιστραμμενο καταλαγο απο το αρχειο finalSorted που ειναι η λιστα εμφανησεων ολων των terms
        ii.creatInvertedIndex(new File("finalSorted.txt"), new File("index.txt"));
        System.out.println("Inverted index created at:");
        DateFormat dateFormat2 = new SimpleDateFormat("HH:mm:ss");
        Date date2 = new Date();
        System.out.println(dateFormat2.format(date2)); 
	//Κλεινει τα executors
        executor1.shutdown();
        executor2.shutdown();
        executor3.shutdown(); 
	//Τα ενδιαμεσα αρχεια διαγραφονται		
        for(int i=0; i<countFiles; i++){
            try {
                File file = new File("seg"+i+".txt");
                file.delete();
                File file2 = new File("sorted"+i+".txt");
                file2.delete();
            } catch (Exception x) {

            }            
        }
        //System.out.println("done");
	//Διαβαζεται το αρχειο query.txt
        System.out.println("\nReturn query results with " +MAX_THREADS+ " Threads:");
        DateFormat dateFormat3 = new SimpleDateFormat("HH:mm:ss");
        Date date3 = new Date();
        System.out.println(dateFormat3.format(date3)); 
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("query.txt")));
        String line = null;
        line = br.readLine();
        String [] tokens = line.split("\\s+");
	//Βρισκει τον αριθμο απο queries που πρεπει να γινουν
        int queriesNum = Integer.parseInt(tokens[0]);
        
        ExecutorService executor4 = Executors.newFixedThreadPool(MAX_THREADS);
	//Για καθε query αναθετει task για να βρεθει η απαντηση
        for(int i=0; i<queriesNum; i++){
            Runnable queryTask = new QueryTask(i+1);
            tasks.add(executor4.submit(queryTask));
        } 
        checkExecutors(tasks);

        System.out.println("Query results at:");
        DateFormat dateFormat4 = new SimpleDateFormat("HH:mm:ss");
        Date date4 = new Date();
        System.out.println(dateFormat4.format(date4));
        executor4.shutdown();
    }    
}
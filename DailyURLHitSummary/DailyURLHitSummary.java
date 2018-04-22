/* Problem:
 *
 * Question:
 * ========
 * You’re given an input file. Each line consists of a timestamp (unix epoch in seconds) and a url separated by ‘|’ (pipe operator).
 * The entries are not in any chronological order. 
 * Your task is to produce a daily summarized report on url hit count, organized daily (use GMT) with the earliest date appearing first. 
 * For each day, you should display the number of times each url is visited in the order of highest hit count to lowest count.
 * Your program should take in one command line argument: input file name.
 * The output should be printed to stdout. 
 *
 * You can assume that the cardinality (i.e. number of distinct values) of hit count values and 
 * the number of days are much smaller than the number of unique URLs. 
 * You may also assume that number of unique URLs can fit in memory, but not necessarily the entire file.
 *
 *
 * input.txt
 * ==========
 * 1407564301|www.nba.com
 * 1407478021|www.facebook.com
 * 1407478022|www.facebook.com
 * 1407481200|news.ycombinator.com
 * 1407478028|www.google.com
 * 1407564301|sports.yahoo.com
 * 1407564300|www.cnn.com
 * 1407564300|www.nba.com
 * 1407564300|www.nba.com
 * 1407564301|sports.yahoo.com
 * 1407478022|www.google.com
 * 1407648022|www.twitter.com
 *
 *
 * Expected Output
 * ================
 * 08/08/2014 GMT
 * www.facebook.com 2
 * www.google.com 2
 * news.ycombinator.com 1
 * 08/09/2014 GMT
 * www.nba.com 3
 * sports.yahoo.com 2
 * www.cnn.com 1
 * 08/10/2014 GMT
 * www.twitter.com 1
 * 
 * Correctness, efficiency (speed and memory) and code cleanliness will be evaluated. 
 * Please provide a complexity analysis in Big-O notation for your program along with your source. 
 */


/* Date-Specific imports */
import java.text.DateFormat;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.util.Date;

/* File-Specific imports */
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/* Data-Struture imports */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

public class DailyURLHitSummary {

    /*
     * Name : convertUnixToGMT
     * Description : Converts unix timestamp to GMT date format
     * Input parameters : Timestamp (long) in unix epoch
     * Output : Date formatted string
     */
    public static String convertUnixToGMT (long timestamp) {
        Date date = new Date((long) timestamp);
        DateFormat dateformat = new SimpleDateFormat("dd/MM/yyyy");
        dateformat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String dateString = dateformat.format(date);
        return dateString;
    }

    /* 
     * Name : generateURLMap
     * Description : Generate the expected output result
     * Input parameters : Array of input strings
     * Output : None.
     */
    public static void generateURLMap(String[] fileInputLines) {
        
        /* Since we are looking for a sorted list of URL hit count 
         * we can use a Treemap of HashMaps to generate result */
        /* TreeMap is sorted by keys and hence will provide us 
         * date in chronological order */

        Map<Long,Map<String,Integer>> urlMap = 
            new TreeMap<Long, Map<String,Integer>>();

        for(String line : fileInputLines) {
            
            /* Split the input line based on '|' delimiter into fragments */
            String[] fragments = line.split("\\|");
            
            long unixEpoch = Long.parseLong(fragments[0]) * 1000;
            unixEpoch = (long) Math.floor(unixEpoch/(24*60*60*1000));
            unixEpoch = unixEpoch * (24*60*60*1000);

            /* HashMap to store url frequency count */
            Map<String,Integer> frequencyCountMap = urlMap.get(unixEpoch);

            if(frequencyCountMap == null) {
                frequencyCountMap = new HashMap<String, Integer>();
            } 

            Integer count = frequencyCountMap.get(fragments[1]);
            if(count == null) {
                count = 0;
            }

            count++;
            frequencyCountMap.put(fragments[1],count);
            urlMap.put(unixEpoch, frequencyCountMap);
        }

        /* Iterate over the Treemap entry set which is sorted */
        for(Entry<Long,Map<String,Integer>> entry : urlMap.entrySet()){
            
            System.out.println(convertUnixToGMT(entry.getKey())+" GMT");
            
            /* For each entry key (timestamp), sort the corresponding hashmap*/
            Map<String,Integer> frequencyCountMap = entry.getValue();
            Entry<String,Integer>[] countURLEntries = 
                frequencyCountMap.entrySet().toArray(new Entry[frequencyCountMap.size()]);

            Arrays.sort(countURLEntries, new Comparator<Entry<String,Integer>>(){
                @Override
                public int compare(Entry<String, Integer> o1,
                    Entry<String, Integer> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });

            for(Entry<String,Integer> entries : countURLEntries){
                System.out.println(entries.getKey()+" "+entries.getValue());
            }
        }
    }

    /* Main function */
    public static void main(String[] args) {
        /*Scanner to scan system input */
        Scanner scan = new Scanner(System.in);

        /* Add a try-catch block to process input file */
        try {
            /* Ask user to input the file and process it*/
            System.out.println("Enter the daily url logs input file : ");
            String filename = scan.nextLine();
            File file = new File(filename);

            /* Check if a file exists or not */
            if(file.exists()){

                String[] fileInputLines = null;
                List<String> inputList = new ArrayList<String>();
                
                try {

                    /* Process the lines in the input file
                     * and trim them based on end of line 
                     * and store them in a list 
                     */
                    
                    FileInputStream filestream = new FileInputStream(filename);
                    DataInputStream data = new DataInputStream(filestream);
                    BufferedReader buffReader = new BufferedReader(new InputStreamReader(data));
                    String strLine;
                    
                    /* Scan through the buffered reader */
                    while((strLine = buffReader.readLine()) != null) {
                        strLine = strLine.trim();
                        if((strLine.length() != 0)) {
                            inputList.add(strLine);
                        }
                    }

                    /* Convert the list of input lines into array elements */
                    int size = inputList.size();
                    fileInputLines = (String[])inputList.toArray(new String[size]);
                } 
                catch (Exception e) 
                {
                    System.err.println("Input File Error: " + e.getMessage());
                }

                /* Once the input file is processed successfully and 
                 * converted into a input string array, invoke the
                 * generateURLMap function which generates the required output
                 */
                generateURLMap(fileInputLines);
                
            }
            else {
                System.out.println("Input file not present in provided path");
            }
        }
        finally 
        {
            /* Close the scanner */
            scan.close();
        }
        
    }
}

/* Actual Output :
 * ===============
 * $ javac DailyURLHitSummary.java 
 * $ java DailyURLHitSummary
 * Enter the daily url logs input file : 
 * input.txt
 * 08/08/2014 GMT
 * www.facebook.com 2
 * www.google.com 2
 * news.ycombinator.com 1
 * 09/08/2014 GMT
 * www.nba.com 3
 * sports.yahoo.com 2
 * www.cnn.com 1
 * 10/08/2014 GMT
 * www.twitter.com 1
 */

/* Efficiency :
 * ============
 * Time complexity : O(n log(n)) where n = number of input lines in the input file and log(n) is treemap put/get operation
 *                                          and hashmap operations are O(1) operations
 * Space complexity : O(n) space where n - number of input lines
 */

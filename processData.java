// Author: 2/C Cooper Guzzi
// 18 FEB 20
// Program used to take in IDS parameters from alert.java
// and process data appropriately for our K-Means program.
// Input: csv file created by alert.java file
// Output: processed data parameters to be used in Kmeans program.
import java.util.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.io.*;

public class processData {
  //code taken from https://mkyong.com/java/java-convert-ip-address-to-decimal-number/
  // to convert string ip to long
  public static double ipToDouble(String ipAddress) {
    String[] ipAddressInArray = ipAddress.split("\\.");

    double result = 0;
    for (int i = 0; i < ipAddressInArray.length; i++) {

      int power = 3 - i;
      int ip = Integer.parseInt(ipAddressInArray[i]);
      result += ip * Math.pow(256, power);

    }

    return result;
  }

  public static void main(String[] args) {
    if(args.length != 3) {
      System.out.println("Usage: java processData input.csv output.csv normalizedoutput.csv");
      System.exit(1);
    }
    double maxIP = ipToDouble("255.255.255.255");
    String strMaxTime = "Dec 31 2012 23:59:59.999 UTC";

    try {
      SimpleDateFormat dateformat = new SimpleDateFormat("MMM dd yyyy HH:mm:ss.SSS zzz");
      Date dateMaxTime = dateformat.parse(strMaxTime);
      double epochMaxTime = (double)(dateMaxTime.getTime());

      BufferedReader csvReader = null;
  		FileReader fr = null;
  		BufferedWriter csvWriter = null;
  		FileWriter fw = null;
      BufferedWriter normalizedWriter = null;
  		FileWriter nw = null;

      String filename = args[0];

      try {
        fr = new FileReader(args[0]);
        csvReader = new BufferedReader(fr);
        fw = new FileWriter(args[1]);
        csvWriter = new BufferedWriter(fw);
        nw = new FileWriter(args[2]);
        normalizedWriter = new BufferedWriter(nw);


        File f = new File(filename);
        String row = "";

        if(f.isFile()){
          try {
            csvReader = new BufferedReader(new FileReader(f));
            HashMap<String, Integer> hmap = new HashMap<String, Integer>();
            int counter = 1;
            while((row = csvReader.readLine()) != null){
              String [] data = row.split(";");
              //process the data
              //data[] contents: sid, rev, message, classification, priority, timestamp,
              //srcIP, srcPort, destIP, destPort
              //convert IPs

              //System.out.println(ipToLong(data[6]));
              double lSrcIP = ipToDouble(data[6]);
              String srcIP = String.valueOf(lSrcIP);
              double lDestIP = ipToDouble(data[8]);
              String destIP = String.valueOf(lDestIP);
              double dts = Double.parseDouble(data[5]);
              String ts = String.valueOf(data[5]);
              String srcPort = data[7];
              String destPort = data[9];
              String ruleID = data[0];
              String alertType = data[3];
              Integer alertTypeInt = null;

              //normalize the data
              //IPs: divide by int val of 255.255.255.255
              String normalizedSrcIP = String.valueOf(lSrcIP/maxIP);
              String normalizedDestIP = String.valueOf(lDestIP/maxIP);
              //Ports: leave as is
              //RuleID: leave as is, will check for matches in kMeans Program
              //time: divide by epoch time of 12/31/2012 23:59:59
              String normalizedts = String.valueOf(dts/epochMaxTime);
              //AlertType: use hashmap to convert String to int value.
              if(hmap.containsKey(alertType)) {
                alertTypeInt = hmap.get(alertType);
              } else {
                hmap.put(alertType, counter);
                alertTypeInt = counter;
                counter++;
              }
              //write data fields to output files
              //SrcIP; DestIP; ts(Timestamp); RuleID; SrcPort; DestPort; AlertType
              String toWrite = srcIP+";"+destIP+";"+ts+";"+ruleID+";"
                  +srcPort+";"+destPort+";"+alertType;
              csvWriter.write(toWrite);
              csvWriter.newLine();

              //normalizedSrcIP; normalizedDestIP; normalizedts(timestamp); RuleID; SrcPort; DestPort; alertTypeInt
              String normalizedtoWrite = normalizedSrcIP+";"+normalizedDestIP+";"+normalizedts+";"+ruleID+";"
                  +srcPort+";"+destPort+";"+alertTypeInt;
              normalizedWriter.write(normalizedtoWrite);
              normalizedWriter.newLine();
            }
          } catch (FileNotFoundException e){
            e.printStackTrace();
          } catch (IOException e){
            e.printStackTrace();
          } finally {
            if (csvReader != null) {
              try {
                csvReader.close();
                csvWriter.flush();
                csvWriter.close();
                normalizedWriter.flush();
                normalizedWriter.close();
              } catch (IOException e){
                e.printStackTrace();
              }
            }
          }
          System.out.println("Data written to \"" + args[1] + "\" and normalized data written to \"" + args[2] + "\".");
        }
        else{
          System.out.println("Error: File \"" + filename +"\" does not exist.");
          System.exit(1);
        }
      } catch (IOException e){
        e.printStackTrace();
      }
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }
}

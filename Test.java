import java.util.*;

public class Test {
  private static int[] makeShares(int toSplit, int numShares) {
    Random rand = new Random();
    int[] shares = new int[numShares];

    int lastShare = toSplit;
    int thisShare = 0;
    //make 1 less than number of shares by choosing random numbers less than the int to toSplit
    for(int i = 0; i < numShares-1; i++) {
      thisShare = rand.nextInt(lastShare);
      shares[i] = thisShare;
      lastShare = lastShare - thisShare;
    }

    shares[numShares-1] = lastShare;

    return shares;
  }

  private static ArrayList<Double> makeShares(double toSplit, int numShares) {
    Random rand = new Random();
    ArrayList<Double> shares = new ArrayList<Double>(numShares);

    double lastShare = toSplit;
    double thisShare = 0;
    //make 1 less than number of shares by choosing random numbers less than the int to toSplit
    for(int i = 0; i < numShares-1; i++) {
      thisShare = rand.nextDouble()*lastShare;
      shares.add(i, thisShare);
      lastShare = lastShare - thisShare;
    }

    shares.add(numShares-1, lastShare);

    return shares;
  }

  private static ArrayList<ArrayList<Double>> makeShares(ArrayList<Double> toSplit, int numShares) {
    ArrayList<ArrayList<Double>> shares = new ArrayList<ArrayList<Double>>();

    for(int i = 0; i < toSplit.size(); i++) {
        ArrayList<Double> inner = makeShares(toSplit.get(i), numShares);
        shares.add(inner);
    }
    return shares;
  }

  private static ArrayList<HashMap<Integer, Integer>> makeShares(HashMap<Integer, Integer> toSplit, int numShares) {
    ArrayList<HashMap<Integer, Integer>> shares = new ArrayList<HashMap<Integer, Integer>>();

    for(int i = 0; i < toSplit.size(); i++) {
      HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
      shares.add(hm);
    }

    //Create an iterator to iterate through the HashMap
    Iterator itr = toSplit.entrySet().iterator();
    //Iterate through the HashMap
    while(itr.hasNext()) {
      Map.Entry mapElement = (Map.Entry)itr.next();
      int[] parts = makeShares((int)mapElement.getValue(), numShares);
      //add one share of each Key-Value pair to one of each of the HashMaps in shares
      for(int i = 0; i < numShares; i++) {
        shares.get(i).put((Integer)mapElement.getKey(), parts[i]);
      }
    }
    return shares;
  }

  private static ArrayList<ArrayList<HashMap<Integer, Integer>>> makeHashMapShares(ArrayList<HashMap<Integer, Integer>> toSplit, int numShares) {
    ArrayList<ArrayList<HashMap<Integer, Integer>>> shares = new ArrayList<ArrayList<HashMap<Integer, Integer>>>();

    for(int i = 0; i < toSplit.size(); i++) {
      ArrayList<HashMap<Integer, Integer>> inner = makeShares(toSplit.get(i), numShares);
      shares.add(inner);
    }
    return shares;
  }

  //------------------------------------------------MAIN METHOD------------------------------------------------
  public static void main(String[] args) {
    //change test to test each different makeShares method
    int test = 4;

    if(test == 0) {
      int num = 10;
      int[] shares = makeShares(num, 5);
      System.out.println(num + " split into 5 shares is:");
      System.out.print("[ ");
      for(int i = 0; i < shares.length; i++){
        System.out.print(shares[i]);
        if(i != shares.length-1)
          System.out.print(", ");
      }
      System.out.println(" ]");
    } else if(test == 1) {
      double num = 0.42;
      double sum = 0.0;
      int sharesNum = 3;
      ArrayList<Double> shares = makeShares(num, sharesNum);
      System.out.println(num + " split into " + sharesNum + " shares is:");
      System.out.print("[ ");
      for(int i = 0; i < shares.size(); i++) {
          System.out.print(shares.get(i));
          sum += shares.get(i);
          if(i != shares.size()-1) {
            System.out.print(", ");
          }
      }
      System.out.print(" ]\t");
      System.out.println("Sum of Shares: " + sum);
    } else if(test == 2) {
      ArrayList<Double> toSplit = new ArrayList<Double>(3);
      toSplit.add(0.42);
      toSplit.add(0.69);
      toSplit.add(0.77);
      int sharesNum = 3;
      double total1 = 0.0;
      double total2 = 0.0;
      double total3 = 0.0;

      ArrayList<ArrayList<Double>> shares = makeShares(toSplit, sharesNum);

      for(int i=0; i<shares.size(); i++) {
        System.out.print("[ ");
        for(int j=0; j<shares.get(i).size(); j++) {
          System.out.print(shares.get(i).get(j));
          if(j != shares.get(i).size()-1) {
            System.out.print(", ");
          }
          if(i == 0) {
            total1 += shares.get(i).get(j);
          }else if (i == 1) {
            total2 += shares.get(i).get(j);
          }else if (i == 2) {
            total3 += shares.get(i).get(j);
          }
        }
        System.out.print(" ]\t");
        if(i == 0) {
          System.out.println("Sum of Shares: " + total1);
        }else if (i == 1) {
          System.out.println("Sum of Shares: " + total2);
        }else if (i == 2) {
          System.out.println("Sum of Shares: " + total3);
        }
      }
    } else if(test == 3) {
      HashMap<Integer, Integer> toSplit = new HashMap<Integer, Integer>();
      toSplit.put(2, 10);
      toSplit.put(4, 12);
      toSplit.put(6, 23);

      int sharesNum = 3;

      //Print initial HashMap
      for (Map.Entry mapElement : toSplit.entrySet()) {
        Integer key = (Integer)mapElement.getKey();
        Integer val = (Integer)mapElement.getValue();
        System.out.println(key + " : " + val);
      }
      System.out.println("\nMaking Shares!!!\n");

      //Call makeShares function
      ArrayList<HashMap<Integer, Integer>> shares = makeShares(toSplit, sharesNum);
      //Print each hashmap of shares in the ArrayList shares
      for(HashMap<Integer, Integer> hm : shares) {
        for (Integer key : hm.keySet()) {
          Integer val = hm.get(key);
          System.out.println(key + " : " + val);
        }
        System.out.println();
      }
    } else if(test == 4)  {
      ArrayList<HashMap<Integer, Integer>> toSplit = new ArrayList<HashMap<Integer, Integer>>();
      HashMap<Integer, Integer> hmap1 = new HashMap<Integer, Integer>();
      hmap1.put(2, 10);
      hmap1.put(4, 12);
      hmap1.put(6, 23);
      HashMap<Integer, Integer> hmap2 = new HashMap<Integer, Integer>();
      hmap2.put(8, 7);
      hmap2.put(10, 8);
      hmap2.put(12, 17);
      toSplit.add(hmap1);
      toSplit.add(hmap2);

      int sharesNum = 3;

      //Print initial HashMaps in ArrayList
      for(HashMap<Integer, Integer> hm : toSplit) {
        for (Integer key : hm.keySet()) {
          Integer val = hm.get(key);
          System.out.println(key + " : " + val);
        }
        System.out.println();
      }
      System.out.print("\nMaking Shares!!!\n");
      //Call makeHashMapShares function
      ArrayList<ArrayList<HashMap<Integer, Integer>>> shares = makeHashMapShares(toSplit, sharesNum);

      //Print each hashmap of shares in each ArrayList in the ArrayList shares
      for (ArrayList<HashMap<Integer, Integer>> list : shares) {
        for(HashMap<Integer, Integer> hm : list) {
          System.out.println();
          for (Integer key : hm.keySet()) {
            Integer val = hm.get(key);
            System.out.println(key + " : " + val);
          }
          System.out.println();
        }
      }
    }

  } //end of main

} //end of class

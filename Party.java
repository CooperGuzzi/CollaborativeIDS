import java.util.ArrayList;
import java.util.Random;
import java.util.HashMap;
import java.util.Arrays;
import java.util.*;

public class Party
{
  ArrayList<Entity> data;
  ArrayList<Party> otherParties;
  int qualCount;
  int catCount;

  //Secret Sharing holders
  int intermediateSum;
  int finalSum;
  SharingEntity intermediateEntity;
  SharingEntity finalEntity;

  long bytesShared;

  
  public Party(ArrayList<Entity> data)
  {
    this.data = data;
    this.qualCount = data.get(0).getQualityCount();
    this.catCount = data.get(0).getCategoryCount();

    this.intermediateSum = 0;
    this.finalSum = 0;
    this.intermediateEntity = new SharingEntity(qualCount, catCount);
    this.finalEntity = new SharingEntity(qualCount, catCount);

    this.bytesShared = 0;
  }

  public long getBytesShared()
  {
    return bytesShared;
  }
  
  public int getFinalSum()
  {
    return finalSum;
  }
  
  public void setOtherParties(ArrayList<Party> list)
  {
    otherParties = list;
  }
  		       
  public int getQualityCount()
  {
    return qualCount;
  }
  
  public int getCategoryCount()
  {
    return catCount;
  }

  public SharingEntity getCombinedPartyDataOnCluster()
  {
    return finalEntity;
  }
  
  public ArrayList<Double> getQualitySumByCluster(int clusterID)
  {
    ArrayList<Double> sum = new ArrayList<Double>();
    for(int i = 0 ;  i < qualCount; i++)
      sum.add(0.0);
    
    for(Entity e : data)
      {
	if(e.getAssignedCluster() == clusterID)
	  {
	    for(int i = 0 ; i < qualCount; i++)
	      {
		sum.set(i, sum.get(i) + e.getQual(i));
	      }
	  }
      }
    return sum;
  }


  public ArrayList<HashMap<Integer,Integer>> getCategorySumByCluster(int clusterId)
  {
    ArrayList<HashMap<Integer,Integer>> sum = new ArrayList<HashMap<Integer,Integer>>();
    for(int i = 0 ; i < catCount; i++)
      {
	sum.add(getCategoryHashMap(i, clusterId));
      }    
    return sum;
  }

  public HashMap<Integer,Integer> getCategoryHashMap(int cat, int clusterNum)
  {
    ArrayList<Integer> list = new ArrayList<Integer>();
    for(Entity e : data)
      {
	if(e.getAssignedCluster() == clusterNum)
	  {
	    list.add(e.getCat(cat));
	  }
      }
    return KMode.getCategoryHashMap(list);
  }

  public void resetIntShares()
  {
    intermediateSum = 0;
    finalSum = 0;
  }

  public void resetEntityShares()
  {
    intermediateEntity = new SharingEntity(qualCount, catCount);
    finalEntity = new SharingEntity(qualCount, catCount);
  }
  
  public void assignClusters(ArrayList<EntityCluster> clusters)
  {
    double max = Double.MAX_VALUE;
    double min = max; 
    int cluster = 0;                 
    double distance = 0.0; 
    
    for(Entity point : data)
      {

	min = max;
	for(int i = 0; i < clusters.size(); i++)
	  {
	    EntityCluster c = clusters.get(i);

	    distance = Entity.distanceEuclidean(point, c.getCentroid());
	    if(distance < min){
	      min = distance;
	      cluster = i;
	    }
	  }
	point.setCluster(cluster);
	clusters.get(cluster).addEntity(point);
      }
  }

  public void broadcastCountShares()
  {
    int[] share = makeShares(data.size(), otherParties.size());
    for(int i = 0 ; i < otherParties.size(); i++)
      {
	otherParties.get(i).sendIntShare(share[i]);
      }
  }

  public void broadcastCountShares(int cid)
  {
    int count = 0;
    for(Entity e: data)
      {
	if(e.getAssignedCluster() == cid)
	  count++;
      }
    int[] share = makeShares(count, otherParties.size());
    for(int i = 0 ; i < otherParties.size(); i++)
      {
	otherParties.get(i).sendIntShare(share[i]);
      }
  }

  public int broadcastCountSums()
  {
    finalSum += intermediateSum;
    int[] share = makeShares(data.size(), otherParties.size());
    for(int i = 0 ; i < otherParties.size(); i++)
      {
	otherParties.get(i).sendIntIntermediate(intermediateSum);
      }

        return finalSum;

  }

  public void sendIntShare(int addend)
  {
    intermediateSum += addend;
    bytesShared += 8;
  }

  public void sendIntIntermediate(int addend)
  {
    finalSum += addend;
    bytesShared +=  8;
  }

  public static int computeEntCount(ArrayList<Party> parties)
  {
    for(Party p : parties)
      {
	p.broadcastCountShares();
	//each party adds count share to IntermediateSum
      }
    int ret = 0;
    for(Party p : parties)
      {
	//each party sends its intermediate sums to,all parties they are added to final sum
	ret = p.broadcastCountSums();
      }
    for(Party p : parties)
      {
	//Reset 
	 p.resetIntShares();
      }
    //return final sum
    return ret;    
  }
  
  public static int computeEntCount(ArrayList<Party> parties, int cid)
  {
    for(Party p : parties)
      {
	p.broadcastCountShares(cid);
	//each party adds count share to IntermediateSum
      }
    int ret = 0;
    for(Party p : parties)
      {
	//each party sends its intermediate sums to,all parties they are added to final sum
	ret = p.broadcastCountSums();
      }
    for(Party p : parties)
      {
	//Reset 
	 p.resetIntShares();
      }
    //return final sum
    return ret;    
  }

  private static int[] makeShares(int toSplit, int numShares) {
    Random rand = new Random();
    int[] shares = new int[numShares];

    int lastShare = toSplit;
    int thisShare = 0;
    //make 1 less than number of shares by choosing random numbers less than the int to toSplit
    for(int i = 0; i < numShares-1; i++) {
      if(lastShare != 0)//last share resached zero
	{
	  thisShare = rand.nextInt(lastShare);
	  shares[i] = thisShare;
	  lastShare = lastShare - thisShare;
	}
      else
	{
	  shares[i] = 0;
	}
    }

    shares[numShares-1] = lastShare;
    return shares;
  }


  private static ArrayList<SharingEntity> makeShares(SharingEntity origin, int numShares)
  {
    Random rand = new Random();
    ArrayList<SharingEntity> shares = new ArrayList<SharingEntity>();
    int[] countShares = makeShares(origin.getCountShare(), numShares);
    ArrayList<ArrayList<HashMap<Integer,Integer>>> mapShares = makeHashMapShares(origin.getModeMap(), numShares);
    ArrayList<ArrayList<Double>> qualities = makeShares(origin.getQualities(), numShares);
    for(int i = 0 ;  i < numShares; i++)
      {
	SharingEntity s  = new SharingEntity(countShares[i], qualities.get(i),  mapShares.get(i));
	shares.add(s);
      }
    return shares;
  }

  public static SharingEntity computeClusterTotal(ArrayList<Party> parties, int cid)
  {
    for(Party p : parties)
      {
	p.broadcastEntitySharesByCluster(cid);
	//each party computes total share
      }
    for(Party p : parties)
      {
	//each party sends its intermediate sums to all parties they are added to final sum
	p.broadcastClusterIntermediateSums();
      }
    
    SharingEntity ret = parties.get(0).getCombinedPartyDataOnCluster();
     for(Party p : parties)
      {
	//Reset 
	 p.resetEntityShares();
      }
    //return final sum
    return ret;    
  }
  
  public void broadcastEntitySharesByCluster(int cid)
  {
    ArrayList<HashMap<Integer,Integer>> catMap = getCategorySumByCluster(cid);
    int entsInCluster = localEntityInClusterCount(cid);
    ArrayList<Double> qualsForCluster = getQualitySumByCluster(cid);
    SharingEntity combinedPartyData = new SharingEntity( entsInCluster,    qualsForCluster,    catMap  );
    ArrayList<SharingEntity> shares = makeShares(combinedPartyData, otherParties.size());
    for(int i = 0 ; i < otherParties.size(); i++)
      {
	otherParties.get(i).sendEntityShare(shares.get(i));
      }
  }

  public void broadcastClusterIntermediateSums()
  {
    finalEntity = SharingEntity.merge(finalEntity,intermediateEntity); 
    for(int i = 0 ; i < otherParties.size(); i++)
      {
	otherParties.get(i).sendEntityIntermediate(intermediateEntity);
      }
    
  }
  
  public int localEntityInClusterCount(int cid)
  {
    int ret = 0;
    for(Entity e : data)
      {
	if(e.getAssignedCluster() == cid)
	  ret++;
      }
    return ret;
  }
  
  public void sendEntityShare(SharingEntity addend)
  {
    intermediateEntity = SharingEntity.merge(intermediateEntity, addend);
    bytesShared += addend.estimatedSizeInBytes();
  }

  public void sendEntityIntermediate(SharingEntity addend)
  {
    finalEntity = SharingEntity.merge(finalEntity, addend);
    bytesShared += addend.estimatedSizeInBytes();
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
    for(int i = 0; i < numShares; i++)
      shares.add(new ArrayList<Double>());
    for(int i = 0; i < toSplit.size(); i++)//each quality
      {
        ArrayList<Double> inner = makeShares(toSplit.get(i), numShares);
	for(int j = 0; j < numShares; j++)//each new share
	  {
	    ArrayList<Double> dsha = shares.get(j);
	    dsha.add(inner.get(j));
	  }
      }

    return shares;
    /*
  for(int i = 0; i < toSplit.size(); i++)//each quality
      {
        ArrayList<Double> inner = makeShares(toSplit.get(i), numShares);
	for(int j = 0; j < numShares; j++)//each new share
	  {
	    shares.get(j).add(inner.get(j));
	  }
      }

*/
  }

    private static ArrayList<HashMap<Integer, Integer>> makeShares(HashMap<Integer, Integer> toSplit, int numShares) {
      ArrayList<HashMap<Integer, Integer>> shares = new ArrayList<HashMap<Integer, Integer>>();

    for(int i = 0; i < numShares; i++) {//cooper->numshares was og tosplit.size()
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
    for(int i = 0 ; i < numShares; i++)//added ;loop
      {
	shares.add(new ArrayList<HashMap<Integer,Integer>>());
      }
    for(int i = 0; i < toSplit.size(); i++)//each category
      {
	ArrayList<HashMap<Integer, Integer>> inner = makeShares(toSplit.get(i), numShares);//each category
	for(int j = 0; j < numShares; j++)//each new share
	  {
	    shares.get(j).add(inner.get(j));
	  }
      }
    return shares;
  }

  public static void main(String[] args)
  {
    ArrayList<Party> partyList = new ArrayList<Party>();
    ArrayList<Entity> a = Entity.createRandomEntities(1,0,5);
    Party A = new Party(a);
    ArrayList<Entity> b = Entity.createRandomEntities(1,0,5);
    Party B = new Party(b);
    ArrayList<Entity> c = Entity.createRandomEntities(1,0,5);
    Party C = new Party(c);
    ArrayList<Party> op = new ArrayList<Party>();
    ArrayList<Party> op1 = new ArrayList<Party>();
    ArrayList<Party> op2 = new ArrayList<Party>();
    op.add(B);
    op.add(C);
    A.setOtherParties(op);
    op1.add(A);
    op1.add(C);
    B.setOtherParties(op1);
    op2.add(A);
    op2.add(B);
    C.setOtherParties(op2);

    partyList.add(A);
    partyList.add(B);
    partyList.add(C);
    System.out.println(a.get(0) + " " + b.get(0) + " " + c.get(0));
    System.out.println(computeEntCount(partyList));

    
    
  }
  
}

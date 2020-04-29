import java.util.ArrayList;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.concurrent.TimeUnit;

public class DKMeans
{
  private int NUM_CLUSTERS;    
  //Number of Points
  private int NUM_ENTITIES;
  private int QUALITIES;
  private int CATEGORIES;
  

  private ArrayList<EntityCluster> clusters;
  private ArrayList<Party> parties;
  
  public DKMeans(int NUM_CLUSTERS, int Q, int C)
  {
    this.clusters = new ArrayList<EntityCluster>();
    this.parties = new ArrayList<Party>();
    this.NUM_CLUSTERS = NUM_CLUSTERS;
    this.NUM_ENTITIES = 0;
    this.QUALITIES =  Q;
    this.CATEGORIES = C;
  }

  public void setParties(ArrayList<Party> par)
  {
    this.parties = par;
    for(Party p: par)
      {
	ArrayList<Party> otherParties = new ArrayList<Party>();
	for(Party q: par)
	  {
	    if(p != q)
	      {
		otherParties.add(q);
	      }
	  }
	p.setOtherParties(otherParties);
      }
    NUM_ENTITIES = Party.computeEntCount(parties);// parties.get(0).computeTotalEntityCount();
  }

  public int ComputeEntCount()
  {
    for(Party p : parties)
      {
	p.broadcastCountShares();
	//each party adds count share to IntermediateSum
      }
    for(Party p : parties)
      {
	
	p.broadcastCountSums();
	//each party sends its intermediate sums to,all parties they are added to final sum
      }
    //return final sum
    return parties.get(0).getFinalSum();    
  }
  
  public void init()
  {
    //Create Clusters

    //Set RandomCentroids
    for (int i = 0; i < NUM_CLUSTERS; i++) {
      EntityCluster cluster = new EntityCluster(i);
      Entity centroid = Entity.createRandomEntity( QUALITIES, CATEGORIES);
      cluster.setCentroid(centroid);
      clusters.add(cluster);
    }
    
  }

  public void init(long seed)
  {
    //Create Clusters

    //Set RandomCentroids
    for (int i = 0; i < NUM_CLUSTERS; i++) {
      EntityCluster cluster = new EntityCluster(i);
      Entity centroid = Entity.createRandomEntity( QUALITIES, CATEGORIES, seed);
      cluster.setCentroid(centroid);
      clusters.add(cluster);
    }
    
  }


  private void plotClusters() {
    for (int i = 0; i < NUM_CLUSTERS; i++) {
      EntityCluster c = clusters.get(i);
      c.plotCluster();
    }
  }

  private String resultSummary() {
    String ret = "Cluster Summary\n";
    int count  = 0;
    int emptyClusters = 0;
    for (EntityCluster c : clusters)
      {
	if(c.isEmpty())
	  emptyClusters++;
	ret += "Cluster " + count + " ";
	ret += c.clusterSummaryVerbose();
	count++;
      }
    return ret + "\nEmpty Clusters: " + emptyClusters;
  }

  private int emptyClusterCount() {
    int ret  = 0;
    for (EntityCluster c : clusters)
      {
	if(c.isEmpty())
	  ret++;
      }
    return ret;
  }

  private void clearClusters() {
    for(EntityCluster cluster : clusters) {
      cluster.clear();
    }
  }

  public void calculate() {
    boolean finish = false;
    int iteration = 0;
    // Add in new data, one at a time, recalculating centroids with each new one. 
    while(!finish) {

      //Clear cluster state
      clearClusters();
      
      ArrayList<Entity> lastCentroids = getCentroids();
      
      //Assign points to the closer cluster
      assignCluster();
      
      //Calculate new centroids.
      calculateCentroids();
      
      iteration++;
      
      ArrayList<Entity> currentCentroids = getCentroids();
      
      //Calculates total distance between new and old Centroids
      double distance = 0;
      for(int i = 0; i < lastCentroids.size(); i++) {
	distance += Entity.distanceEuclidean(lastCentroids.get(i),currentCentroids.get(i));
      }

      if(distance == 0) {
	finish = true;
      }
          
    }
  }


  private ArrayList<Entity> getCentroids()
  {
    ArrayList<Entity> centroids = new ArrayList<Entity>(NUM_CLUSTERS);
    for(EntityCluster cluster : clusters) {
      Entity aux = cluster.getCentroid();
      Entity point = new Entity(aux.getQualities(),aux.getCategories());
      centroids.add(point);
    }
    return centroids;
  }

 private void assignCluster()
  { 
    for(Party p: parties)
      p.assignClusters(clusters);
  }

 
  private void calculateCentroids()
  {
    
    for(EntityCluster cluster : clusters)
      {
	int entCount = 0;
	SharingEntity compositeClusterSum = Party.computeClusterTotal(parties, cluster.getId());
	entCount = compositeClusterSum.getFinalCount();
	Entity newCentroid = compositeClusterSum.toEntity();
	Entity centroid = cluster.getCentroid();
	if(entCount > 0)
	  {	    
	    centroid.setQualities(newCentroid.getQualities());
	    centroid.setCategories(newCentroid.getCategories());
	  }
      }
    
  }

  public double averageMSE()
  {
    double ret = 0.0;
    int denom = NUM_CLUSTERS;
    for(EntityCluster c : clusters)
      {
	if(c.getEntityCount() != 0)
	  {
	    ret += c.MSE();
	  }
	else
	  {
	    denom--;
	  }
      }
    return ret/denom;
  }

  public void addParty(Party A)
  {
    parties.add(A);

 
  }
  
  public double minMSE()
  {
    double ret = -1;
    for(EntityCluster c : clusters)
      {
	if(c.MSE() <  ret || ret < 0)
	  {
	    ret = c.MSE();
	  }
      }
    return ret;
  }
  
  public double maxMSE()
  {
    double ret = 0.0;
    for(EntityCluster c : clusters)
      {
	if(c.MSE() > ret)
	  {
	    ret = c.MSE();
	  }
      }
    return ret;
  }

  public static ArrayList<Party> readPartiesFromSingleFile(String FileName, int partyCount)
  {
    ArrayList<Party> partyList = new ArrayList<Party>();

    ArrayList<ArrayList<Entity>> lists = new ArrayList<ArrayList<Entity>>();
    for(int i = 0 ; i < partyCount; i++)
      {
	lists.add(new ArrayList<Entity>());
      }
    BufferedReader source;
    try
      {
        source = new BufferedReader(new FileReader(FileName));
	String line = "";
	int i = 0;
	while(source.ready())
	  {
	    line = source.readLine();
	    Entity en = Entity.makeEntityFromString(line);
	    if(en != null)
	      {
		lists.get(i % partyCount).add(en);
	      }
	    i++;
	  }  
      }  
    catch(Exception e)
      {
	e.printStackTrace();
	System.exit(1);
      }

    for(ArrayList<Entity> li : lists)
      {
	partyList.add(new Party(li));
      }
  
    return partyList;
  }

  public static ArrayList<Party> readPartiesFromFiles(String FileName)
  {
    ArrayList<Party> partyList = new ArrayList<Party>();//MOVE TO PARTY FILE CLEAN INTO MAKE PARTIES FUNCTION
    int partyCount = 0;
    BufferedReader sourcesList;
    try
      {
        sourcesList = new BufferedReader(new FileReader(FileName));
	partyCount = Integer.parseInt(sourcesList.readLine());
	
	for(int i = 0 ; i  < partyCount; i++)
	  {
	    String line = "";
	    ArrayList<Entity> entries = new ArrayList<Entity>();
	    
	    String partyFile = sourcesList.readLine();
	    BufferedReader in = new BufferedReader(new FileReader(partyFile));      
	    int ct = 0;
	    while(in.ready())
	      {
		line = in.readLine();
		Entity en = Entity.makeEntityFromString(line);
		if(en != null)
		  {
		    entries.add(en);
		  }	    
	      }

	    partyList.add(new Party(entries));
	    
	  }
	

      }  
    catch(Exception e)
      {
	e.printStackTrace();
	System.exit(1);
      }
  
    return partyList;
  }

  public long totalBytesShared()
  {
    long ret = 0;
    for(Party p : parties)
      {
	ret += p.getBytesShared();
      }

    return ret;
  }
  
  public void writeOutput(String outputFile)
  {
    try
      {

	FileWriter out = new FileWriter(outputFile);
	out.write("entries size: " + NUM_ENTITIES + "\n");
	out.write("Clusters :" + NUM_CLUSTERS +"\n");
	out.write("average MSE per cluster: " + averageMSE() + "\n" );
	out.write("Highest MSE: " + maxMSE() + "\n" );
	out.write("Lowest MSE: " + minMSE() + "\n" );
	out.write("Total Bytes Shared " + totalBytesShared() + "\n");
	out.write(resultSummary());
	out.close();
      }
    catch(IOException e)
      {
	System.out.println("Output file error!\n" + e);
      }
  }
  
  public void writeOutputCSV(FileWriter out, long elapse, String outputFile)
  {
    try
      {
	//"Entries size: " +
	out.write( NUM_ENTITIES + ",");
	//"Parties size: " dd
	out.write( parties.size() + ",");
	//"Clusters :" +
	out.write( NUM_CLUSTERS +",");
	//empty cluster count
	out.write( emptyClusterCount() + ",");
	//	"average MSE per cluster: " +
	out.write( averageMSE() + "," );
	//"Highest MSE: " +
	out.write( maxMSE() + "," );
	//"Lowest MSE: " +
	out.write( minMSE() + "," );
	//"Total Bytes Shared " + 
	out.write(totalBytesShared() + ",");
	//"Time Elapsed " +
	out.write( elapse + "\n");
      }
    catch(IOException e)
      {
	System.out.println("Output file error!\n" + e);
      }
  }
 
  public static void run(String FileName, int NUM_CLUSTERS, int NUM_PARTIES, String outputFile)
  {
    ArrayList<Party> partyList = readPartiesFromSingleFile(FileName, NUM_PARTIES);
    int QUALITIES = partyList.get(0).getQualityCount();
    int CATEGORIES = partyList.get(0).getCategoryCount();

    DKMeans dkmeans = new DKMeans(NUM_CLUSTERS, QUALITIES, CATEGORIES);
    
    dkmeans.setParties(partyList);
    dkmeans.init(5);
    dkmeans.calculate();
    dkmeans.writeOutput(outputFile);
    
  }

  public static void repRun(String FileName, int NUM_CLUSTERS, int NUM_PARTIES, String outputFile) throws Exception
  {
    FileWriter out = new FileWriter(outputFile);
    for(int j = 1; j <= NUM_PARTIES; j++)
      {
      ArrayList<Party> partyList = readPartiesFromSingleFile(FileName,j);
      int QUALITIES = partyList.get(0).getQualityCount();
      int CATEGORIES = partyList.get(0).getCategoryCount();
      for(int i = 3; i <= NUM_CLUSTERS; i++)
	{
	  long tote  = 0;
	  for(int k = 0; k < 10; k++)
	    {
	      
	      System.out.println("Clusters:"+ i + " Parties:" + j);
	  
	      DKMeans dkmeans = new DKMeans(i, QUALITIES, CATEGORIES);
	      dkmeans.setParties(partyList);
	      long start = System.nanoTime();
	      dkmeans.init();
	      dkmeans.calculate();
	      long end = System.nanoTime();
	      long elapsed = end - start;
	      tote += elapsed;
	      dkmeans.writeOutputCSV( out, elapsed, "Hrzn_" + NUM_CLUSTERS  + "c" + NUM_PARTIES  + "p" + ".csv");
	    }
	  System.out.println(i + "clusters " + tote/10);
	}
      
    }
    out.close();
    
  }
 public static void main(String[] args)
  {
    
    try
      {
	String inputFile = args[0];
	int num_clusters = Integer.parseInt(args[1]);
	String outputFile = "out.txt";
	int num_parties = 3;
	if(args.length > 2)
	  {
	    num_parties = Integer.parseInt(args[2]);
	    if(args.length > 3 )
	      {
		num_parties = Integer.parseInt(args[2]);
		if(args.length > 4 )
		  {
		    outputFile = args[3];
		  }
	      }
	  }
	
	run(inputFile, num_clusters, num_parties, outputFile);

      }
    catch(ArrayIndexOutOfBoundsException e)
      {
	
	System.out.println("Argument Error!\nUsage: java DKMeans [inputFile] [numberOfClusters] [numberOfParties] [outputFile(optional])");
	e.printStackTrace();
      }
  }




}


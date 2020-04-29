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
  private int DIMENSIONS;
  private int CATEGORIES;

  private ArrayList<Entity> entities;
  private ArrayList<EntityCluster> clusters;
  
  public DKMeans(int NUM_CLUSTERS, int NUM_ENTITIES, int DIMENSIONS, int CATEGORIES) {
    this.entities = new ArrayList<Entity>();
    this.clusters = new ArrayList<EntityCluster>();

    this.NUM_CLUSTERS = NUM_CLUSTERS;
    this.NUM_ENTITIES = NUM_ENTITIES;
    this.DIMENSIONS = DIMENSIONS;
    this.CATEGORIES = CATEGORIES;
  }

  public void setEntities(ArrayList<Entity> e)
  {
    this.entities = e;
  }
  
  public void init() {
    //Create Points
    
    //Create Clusters
    //Set Random Centroids
    for (int i = 0; i < NUM_CLUSTERS; i++) {
      EntityCluster cluster = new EntityCluster(i);
      Entity centroid = Entity.createRandomEntity(DIMENSIONS, CATEGORIES);
      cluster.setCentroid(centroid);
      clusters.add(cluster);
    }
    
    //Print Initial state
  }

  public void init(long seed) {
    //Create Points
    
    //Create Clusters
    //Set Random Centroids
    for (int i = 0; i < NUM_CLUSTERS; i++) {
      EntityCluster cluster = new EntityCluster(i);
      Entity centroid = Entity.createRandomEntity(DIMENSIONS, CATEGORIES,seed);
      cluster.setCentroid(centroid);
      clusters.add(cluster);
    }
    
    //Print Initial state
  }


  private void plotClusters() {
    for (int i = 0; i < NUM_CLUSTERS; i++) {
      EntityCluster c = clusters.get(i);
      c.plotCluster();
    }
  }
  

  private void clearClusters() {
    for(EntityCluster cluster : clusters) {
      cluster.clear();
    }
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

  public void calculate() {
    boolean finish = false;
    int iteration = 0;
    
    // Add in new data, one at a time, recalculating centroids with each new one. 
    while(!finish) {
      //Clear cluster state
      clearClusters();
      
      ArrayList<Entity> lastCentroids = getCentroids();
      
      //Assign points to the closer cluster
      assignCluster();// <-----------------------------------MARK
      
      //Calculate new centroids.
      calculateCentroids();//<--------------------------------MARK
      
      iteration++;
      
      ArrayList<Entity> currentCentroids = getCentroids();//<---------------MARK
      
      //Calculates total distance between new and old Centroids
      double distance = 0;
      for(int i = 0; i < lastCentroids.size(); i++) {
	distance += Entity.distanceEuclidean(lastCentroids.get(i),currentCentroids.get(i));
      }
      /*   System.out.println("#################");
      System.out.println("Iteration: " + iteration);
      System.out.println("Centroid distances: " + distance);
      */
      if(distance == 0) {
	finish = true;
      }
    }
  }


   private ArrayList<Entity> getCentroids() {
    	ArrayList<Entity> centroids = new ArrayList<Entity>(NUM_CLUSTERS);
    	for(EntityCluster cluster : clusters) {
    		Entity aux = cluster.getCentroid();
    		Entity point = new Entity(aux.getQualities(),aux.getCategories());
    		centroids.add(point);
    	}
    	return centroids;
    }

 private void assignCluster() {
        double max = Double.MAX_VALUE;
        double min = max; 
        int cluster = 0;                 
        double distance = 0.0; 
        
        for(Entity point : entities) {
        	min = max;
            for(int i = 0; i < NUM_CLUSTERS; i++) {
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

  private void calculateCentroids()
  {
    for(EntityCluster cluster : clusters)
      {
	ArrayList<Entity> list = cluster.getEntities();
	int n_points = list.size();
	double[] sum = new double[DIMENSIONS];
	ArrayList<ArrayList<Integer>> catEntries = new ArrayList<ArrayList<Integer>>();
	for(int i = 0 ; i < CATEGORIES; i++)
	  {
	    catEntries.add(new ArrayList<Integer>());
	  }

	for(Entity point : list)
	  {
	    for(int i = 0 ; i < DIMENSIONS; i++)
	      sum[i] += point.getQualities().get(i);
	    for(int i = 0; i < CATEGORIES; i++)
	      {
		catEntries.get(i).add(point.getCat(i));
	      }
	  }
	ArrayList<Integer> cats = new ArrayList<Integer>();
	for(int i = 0; i < CATEGORIES; i++)
	  {
	    cats.add(KMode.mode(catEntries.get(i)));
	  }
	
        
	Entity centroid = cluster.getCentroid();
	if(n_points > 0)
	  {
	    ArrayList<Double> quals = new ArrayList<Double>();
	    for(int i = 0; i < DIMENSIONS; i++)
	      {
		quals.add(sum[i]/n_points);
	      }
	    centroid.setQualities(quals);
	    centroid.setCategories(cats);
	  }
      }
    
  }

  public double averageMSE()
  {
    double ret = 0.0;
    int count = 0;
    for(EntityCluster c : clusters)
      {
	ret += c.MSE();
	if(!c.isEmpty())
	  count++;
      }
    return ret/count;
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


   public static ArrayList<Entity> readEntities(String FileName)
  {
    ArrayList<Entity> ret = new ArrayList<Entity>();
    BufferedReader source;
    try
      {
        source = new BufferedReader(new FileReader(FileName));
	String line = "";
	while(source.ready())
	  {
	    line = source.readLine();
	    Entity en = Entity.makeEntityFromString(line);
	    if(en != null)
	      {
		ret.add(en);
	      }
	  }  
      }  
    catch(Exception e)
      {
	e.printStackTrace();
	System.exit(1);
      }
  
    return ret;
  }

  
  public void writeOutputCSV(FileWriter out, long elapse)
  {
    try
      {
	//"Entries size: " +
	out.write( NUM_ENTITIES + ",");
	//"Clusters :" +
	out.write( NUM_CLUSTERS +",");
	//emptu cluster cpuint
	out.write( emptyClusterCount() + ",");
	//	"average MSE per cluster: " +
	out.write( averageMSE() + "," );
	//"Highest MSE: " +
	out.write( maxMSE() + "," );
	//"Lowest MSE: " +
	out.write( minMSE() + "," );
	//"Time Elapsed " +
	out.write( elapse + "\n");
      }
    catch(IOException e)
      {
	System.out.println("Output file error!\n" + e);
      }
  }

  

  public static void repRun(String FileName, int NUM_CLUSTERS, String outputFile) throws Exception
  {
    FileWriter out = new FileWriter(outputFile);
    
    ArrayList<Entity> data =  readEntities(FileName);
    int NUM_ENTITIES = data.size();
    int DIMENSIONS = data.get(0).getQualityCount();  
    int CATEGORIES = data.get(0).getCategoryCount();
      for(int i = 3; i <= NUM_CLUSTERS; i++)
	{
	  for(int j = 0 ; j < 10; j++)
	    {
	      System.out.println("Clusters:"+ i );
	      DKMeans dkmeans = new DKMeans(i,NUM_ENTITIES, DIMENSIONS, CATEGORIES);
	      dkmeans.setEntities(data);
	      long start = System.nanoTime();
	      dkmeans.init(5);
	      dkmeans.calculate();
	      long end = System.nanoTime();
	      long elapsed = end - start;
	      dkmeans.writeOutputCSV( out, elapsed);
	    }
	}
      
      out.close();
      
  }
  
 public static void run(String FileName, int NUM_CLUSTERS, String outputFile) throws Exception
  {
    FileWriter out = new FileWriter(outputFile);
    
    ArrayList<Entity> data =  readEntities(FileName);
    int NUM_ENTITIES = data.size();
    int DIMENSIONS = data.get(0).getQualityCount();  
    int CATEGORIES = data.get(0).getCategoryCount();
    DKMeans dkmeans = new DKMeans(NUM_CLUSTERS,NUM_ENTITIES, DIMENSIONS, CATEGORIES);
    dkmeans.setEntities(data);
    long start = System.nanoTime();
    dkmeans.init(5);
    dkmeans.calculate();
    long end = System.nanoTime();
    long elapsed = end - start;
    dkmeans.writeOutputCSV( out, elapsed);
    
    
    out.close();
    
  }

  
  public static void main(String[] args)
  {
    try
      {
	String inputFile = args[0];
	int num_clusters = Integer.parseInt(args[1]);
	String outputFile = "out.txt";
	if(args.length > 2)
	  {

	    outputFile = args[2];
	  }
	try
	  {
	    run(inputFile, num_clusters, outputFile);
	  }
	catch(Exception e)
	  {
	    e.printStackTrace();
	  }
      }
    catch(ArrayIndexOutOfBoundsException e)
      {
	
	System.out.println("Argument Error!\nUsage: java DKMeans [inputFile] [numberOfClusters] [numberOfParties] [outputFile(optional])");
	e.printStackTrace();
      }
  }

  
}




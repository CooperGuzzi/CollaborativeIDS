import java.util.ArrayList;
import java.util.HashMap;

public class Party
{
  ArrayList<Entity> data;
  int qualCount;
  int catCount;
	long bytesShared;
  public Party(ArrayList<Entity> data)
  {
    this.data = data;
    this.qualCount = data.get(0).getQualityCount();
    this.catCount = data.get(0).getCategoryCount();
	this.bytesShared = 0;
  }

  public int getQualityCount()
  {
    return qualCount;
  }
  
  public int getCategoryCount()
  {
    return catCount;
  }
  
  public int getEntCount(int cid)
  {

    int ret = 0;
    for(Entity e : data)
      {
	if(e.getAssignedCluster() == cid)
	  {
	    ret++;
	  }
      }
    return ret;
  }
 public int getEntCount()
  {

    return data.size();
  }

  public ArrayList<Double> getQualitySum(int clusterID)
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
	bytesShared += 8 * sum.size();
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
	HashMap<Integer,Integer> map = KMode.getCategoryHashMap(list);
	bytesShared += (8 * map.size());
    return map;
  }

public long getBytesShared()
{
	return bytesShared;
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
  
}

import java.util.ArrayList;

public class EntityCluster
{
  public ArrayList<Entity> Entities;
  private Entity centroid;
  public int id;
  private int entityCount;
  

  public EntityCluster(int id)
  {
    this.id = id;
    this.Entities = new ArrayList<Entity>();
    this.centroid = null;
    entityCount = 0;
  }

  public ArrayList<Entity> getEntities() {
    return Entities;
  }

  public void addEntity(Entity e) {
    Entities.add(e);
    entityCount++;
  }

  public void setEntities(ArrayList<Entity> e) {
    this.Entities = e;
  }

  public Entity getCentroid() {
    return centroid;
  }

  public int getEntityCount() {
    return entityCount;
  }

  public boolean isEmpty()
  {
    if(Entities.size() ==0)
      return true;
    return false;
  }
  
  public void setCentroid(Entity centroid) {
    this.centroid = centroid;
  }
  

  public int getId() {
    return id;
  }
  
  public void clear() {
    Entities.clear();
    entityCount = 0;
  }
  
  public void plotCluster() {
    System.out.println("[Cluster: " + id+"]");
    System.out.println("[Centroid: " + centroid + "]");
    System.out.println("[MSE: " + MSE() + "]");
    System.out.println("[Entities: \n");
    
    for(Entity e : Entities) {
      System.out.println(e);
    }
    System.out.println("]");
  }

  public String clusterSummary()
  {
    return "[Centroid: " + centroid + "]\n[MSE: " + MSE() + "]\n Entity Count: " + Entities.size() + "\n";
    
  }
  
  public String clusterSummaryVerbose() {
    String ret = "[Centroid: " + centroid + "]\n[MSE: " + MSE() + "]\nEntity Count: " + Entities.size() + "\n[Entities: \n";

    int i = 0;
    for(Entity e : Entities)
      {
	ret += e.toString() + "\n";
	if(i >= 100)
	  break;
	i++;
      }
    ret += "]";
  
    return ret;
  }
  

  public double MSE()
  {
    /*
      REPLACE FUNCTION
      party p.sumMSE()<--SECRET SHARE
      party p.EntCount(cluster ID)<--SECRET SHARE
    */
    int n_points = Entities.size();	
    if(n_points == 0)
      {
	return 0.0;
      }
    double sum = 0.0;
    for(Entity e : Entities)
      {
	sum +=  Math.pow(Entity.distanceEuclidean(e,centroid),2);   
      }
    
    return sum/n_points;
  }

}


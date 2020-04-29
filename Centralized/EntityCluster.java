import java.util.ArrayList;

public class EntityCluster
{
  public ArrayList<Entity> Entities;
  public Entity centroid;
  public int id;

  public EntityCluster(int id)
  {
    this.id = id;
    this.Entities = new ArrayList<Entity>();
    this.centroid = null;
  }

  public ArrayList<Entity> getEntities() {
    return Entities;
  }

  public void addEntity(Entity e) {
    Entities.add(e);
  }

  public boolean isEmpty()
  {
    return Entities.size() == 0;
  }

  public void setEntities(ArrayList<Entity> e) {
    this.Entities = e;
  }

  public Entity getCentroid() {
    return centroid;
  }
  
  public void setCentroid(Entity centroid) {
    this.centroid = centroid;
  }
  

  public int getId() {
    return id;
  }
  
  public void clear() {
    Entities.clear();
  }
  
  public void plotCluster() {
    System.out.println("[Cluster: " + id+"]");
    System.out.println("[Centroid: " + centroid + "]");
    System.out.println("[Entities: \n");
    for(Entity e : Entities) {
      System.out.println(e);
    }
    System.out.println("]");
  }


  public double MSE()
  {
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


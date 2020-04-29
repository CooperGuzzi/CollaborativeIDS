import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Entity
{
  private ArrayList<Double> qualities;
  private ArrayList<Integer> categories;
  
  private int d;
  private int dCat;
  private int cluster_number = 0;

  public Entity(ArrayList<Double> qualities, ArrayList<Integer> categories)
  {
    this.d = qualities.size();
    this.qualities = qualities;
    this.dCat = categories.size();
    this.categories = categories;
  }

  public void setQualities(ArrayList<Double> q)
  {
    qualities = q;
  }

   public void setCategories(ArrayList<Integer> cat)
  {
    categories = cat;
  }

  
  public double getQual(int i)
  {
    return qualities.get(i);
  }
  
  public int getCat(int i)
  {
    return categories.get(i);
  }

  public ArrayList<Double> getQualities()
  {
    return qualities;
  }

  public ArrayList<Integer> getCategories()
  {
    return categories;
  }
  
  public int getQualityCount()
  {
    return d;
  }
  
  public int getCategoryCount()
  {
    return dCat;
  }

  public void setCluster(int n) {
    this.cluster_number = n;
  }
  
  public int getAssignedCluster() {
    return this.cluster_number;
  }
  
  public static Entity createRandomEntity(int d, int dCat)
  {
    Random r = new Random();
    ArrayList<Double> retQual = new ArrayList<Double>();
    for(int i  = 0 ; i  < d ; i ++)
      {
	retQual.add(r.nextDouble());
      }
    ArrayList<Integer> retCat = new ArrayList<Integer>();
    for(int i  = 0 ; i  < dCat ; i ++)
      {
	retCat.add(r.nextInt(5));
      }
    return new Entity(retQual, retCat);
  }

  public static Entity createRandomEntity(int d, int dCat, long seed)
  {
    Random r = new Random(seed);
    ArrayList<Double> retQual = new ArrayList<Double>();
    for(int i  = 0 ; i  < d ; i ++)
      {
	retQual.add(r.nextDouble());
      }
    ArrayList<Integer> retCat = new ArrayList<Integer>();
    for(int i  = 0 ; i  < dCat ; i ++)
      {
	retCat.add(r.nextInt(5));
      }
    return new Entity(retQual, retCat);
  }
  
  protected static ArrayList<Entity> createRandomEntities(int d, int dCat, int number)
  {
    ArrayList<Entity> ret = new ArrayList<Entity>(number);
    for(int i = 0; i < number; i++) {
      ret.add(createRandomEntity(d, dCat));
    }
    return ret;
  }

  protected static double distanceEuclidean(Entity a, Entity b)
  {
    double sum = 0;
    for(int i = 0; i  < a.getQualityCount(); i++ )
      {
	sum += Math.pow(a.getQual(i) - b.getQual(i),2);
      }
    for(int i = 0; i  < a.getCategoryCount(); i++)
      {
	if(a.getCat(i) != b.getCat(i))
	  sum+=1;
      }
    return Math.sqrt(sum);
    
  }

  public String toString()
  {
    String ret = "";
    if(d != 0)
      {
      for(int i = 0 ; i < d; i++)
	ret += qualities.get(i) + ",";
      }
    if(dCat != 0)
      {
	for(int i = 0 ; i < dCat-1; i++)
	  ret += categories.get(i) + ",";
	ret += categories.get(dCat-1);
      }
    return  ret ;
  }
  
  public static Entity makeEntityFromString(String normalizedData){
    if(normalizedData.contains("null"))
      return null;
    String[] data = normalizedData.split(";");
    //[srcIP, destIP, timestamp, ruleID, srcPort, destPort, alertType]
    double srcIP = Double.parseDouble(data[0]);
    double destIP = Double.parseDouble(data[1]);
    double timestamp = Double.parseDouble(data[2]);
    int ruleID = Integer.parseInt(data[3]);
    int srcPort = Integer.parseInt(data[4]);
    int destPort = Integer.parseInt(data[5]);
    int alertType = Integer.parseInt(data[6]);

    ArrayList<Double> dArr = new ArrayList<Double>();
    dArr.add(srcIP);
    dArr.add(destIP);
    dArr.add(timestamp);

    ArrayList<Integer> iArr = new ArrayList<Integer>();
    iArr.add(ruleID);
    iArr.add(srcPort);
    iArr.add(destPort);
    iArr.add(alertType);
    // System.out.println(iArr);
    return new Entity(dArr, iArr);
  }

  public static void main(String[] args)
  {
    Entity a = Entity.createRandomEntity(2,2);
    Entity b = Entity.createRandomEntity(2,2);
    System.out.println(a);
    System.out.println(b);
    System.out.println(Entity.distanceEuclidean(a,b));
     a = Entity.createRandomEntity(2,0);
     b = Entity.createRandomEntity(2,0);
    System.out.println(a);
    System.out.println(b);
    System.out.println(Entity.distanceEuclidean(a,b));
     a = Entity.createRandomEntity(0,0);
     b = Entity.createRandomEntity(0,0);
    System.out.println(a);
    System.out.println(b);
    System.out.println(Entity.distanceEuclidean(a,b));
     a = Entity.createRandomEntity(0,2);
    b = Entity.createRandomEntity(0,2);
    System.out.println(a);
    System.out.println(b);
    System.out.println(Entity.distanceEuclidean(a,b));
  }
}

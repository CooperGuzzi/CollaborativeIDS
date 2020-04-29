import java.util.ArrayList;
import java.util.HashMap;
import java.util.*;

public class KMode
{
  public static int mode(ArrayList<Integer> list)
  {
    HashMap<Integer,Integer> entries = new HashMap<Integer,Integer>();
    for(Integer i : list)
      {
	if(entries.containsKey(i))
	  {
	    entries.put(i, 1 + entries.get(i));
	  }
	else
	  entries.put(i,1);
      }

    Set<Map.Entry<Integer,Integer>> pairs = entries.entrySet();
    int mode = -1;
    int maxCount = 0;
    for(Map.Entry<Integer,Integer> e : pairs)
      {
	if(e.getValue() > maxCount)
	  {
	    mode = e.getKey();
	    maxCount = e.getValue();
	  }
      }
    return mode;
  }

  public static void main(String[] args)
  {
    ArrayList<Entity> test = Entity.createRandomEntities(2,2,5);

    for(int i = 0; i < test.get(0).getCategoryCount(); i++)
      {
	ArrayList<Integer> cat = new ArrayList<Integer>();
	for(Entity e : test)
	  {
	    System.out.println(e);
	    cat.add(e.getCat(i));
	  }
	System.out.println("category " + i + " mode is " + mode(cat));
      }
  }
}

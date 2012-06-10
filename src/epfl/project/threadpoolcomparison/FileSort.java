package epfl.project.threadpoolcomparison;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Sort the output of MapReduce
 * This class is used to compare the output of benchmark and the output of the mapreduce
 * @author Nicolas
 *
 */
public class FileSort {
	public static void main(String[] args) {
		//String input = "ForkJoin Result/Result.txt";
		//String input = "ThreadPool Result/Result.txt";
            
                
		String input = "Result/res.txt";
                
                
		String output = "sort";
		try {
			BufferedReader r = new BufferedReader(new FileReader(input));
			BufferedWriter w = new BufferedWriter(new FileWriter(output)); 
			ArrayList<Tuple> list = new ArrayList<Tuple>();
			String text = "";
			while ((text = r.readLine()) != null) {
				String[] splitTxt = text.split(" ");
				// tuple must be like ( a , b )
				if (splitTxt.length == 5) {
					list.add(new Tuple(splitTxt[1], splitTxt[3]));
				}
			}
			

			Collections.sort(list);
			
			for (Tuple t : list) {
				w.write(t.toString());
				w.write("\n");
			}
			w.close();
			r.close();
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

}
/**
 * Represent a key/value pair
 * @author Nicolas
 *
 */
class Tuple implements Comparable<Tuple> {
	private String a;
	private String b;
	public String getA() {
		return a;
	}
	public void setA(String a) {
		this.a = a;
	}
	public String getB() {
		return b;
	}
	public void setB(String b) {
		this.b = b;
	}
	
	Tuple(String a, String b){
		this.a = a;
		this.b = b;
	}
	@Override
	public int compareTo(Tuple o) {
		return a.compareTo(o.getA());
	}

	public String toString(){
		return "( "+ a + ", " + b + " )";
	}

	
}
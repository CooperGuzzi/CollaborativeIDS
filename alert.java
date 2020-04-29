import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

public class alert
{
	private int sid;
	private int rev;
	private String message;
	private String classification;
	private int priority;
	private LocalDateTime atime;
	private long epochTime; //added by 2/C Guzzi
	private String srcIP;
	private String destIP;
	private String srcPort;
	private String destPort;

	public alert()
	{sid = -1;}

	/* read a few lines of input and parse them input fields*/
	public alert(String[] input)
	{
		// find first line with [**] [1:2009358:5] ET SCAN Nmap Scripting Engine User-Agent Detected (Nmap Scripting Engine) [**]
		Pattern pattern = Pattern.compile("\\d+:\\d+:\\d+");
		Matcher matcher = pattern.matcher(input[0]);
		if (matcher.find())
		{
		    String str1=matcher.group();
		    String[] list1=str1.split(":");
		    sid = Integer.parseInt(list1[1]);
		    rev = Integer.parseInt(list1[2]);
		}
		// last position of rule id
		int pos1 = matcher.end();
		// final [**]
		int pos2 = input[0].indexOf("[**]",pos1);
		message = input[0].substring(pos1+1, pos2);
		// second line
		//[Classification: Web Application Attack] [Priority: 1]
		pos1 = input[1].indexOf("[Classification:");
		pos2 = input[1].indexOf("]",2);
		classification = input[1].substring(pos1+1, pos2);
		pos1 = input[1].indexOf("[Priority:");
		String s1="[Priority:";
		pos2 = input[1].indexOf("]",pos1+1);
		priority = Integer.parseInt(input[1].substring(pos1+s1.length()+1, pos2));
		// line 3:
		//03/16-07:30:00.000000 192.168.202.79:50465 -> 192.168.229.251:80
		String[] list2 = input[2].split("\\s");
		// parse localdate time
		DateTimeFormatter f = new DateTimeFormatterBuilder().appendPattern("MM/dd-HH:mm:ss.SSSSSS").parseDefaulting(ChronoField.YEAR, 2012).toFormatter();
		//DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd-HH:mm:ss.SSSSSS");
		// set year to 2012
		atime = LocalDateTime.parse(list2[0], f);
		//		atime = atime.withYear(2012);
		//establish time zone (use default for now)
		ZoneId zoneId = ZoneId.systemDefault();
		// convert time to Epoch Time
		epochTime = atime.atZone(zoneId).toEpochSecond();
		// extract source ip
		pos2=list2[1].lastIndexOf(":");
		if (pos2 >=0) {
		srcIP=list2[1].substring(0, pos2);
		String portstr1=list2[1].substring(pos2+1);

		srcPort=list2[1].substring(pos2+1);
		}
		// ip only
		else
			srcIP=list2[1];
		// extract dest ip
		pos2=list2[3].lastIndexOf(":");
		if (pos2 >=0) {
		destIP=list2[3].substring(0, pos2);
		destPort=list2[3].substring(pos2+1);
		}
		else
			destIP=list2[3];
		}



	public String genCSVOutput()
	{
		String str = String.valueOf(sid)+";"+String.valueOf(rev)+";"+"\""+message+"\""+";"+classification+";"
				+ String.valueOf(priority)+ ";"+ String.valueOf(epochTime)+ ";" + srcIP + ";" + String.valueOf(srcPort)
				+";" + destIP + ";" + String.valueOf(destPort)+"\n";
		return str;

	}
	/* read a snort full alert file, transfer it to a csv file */
	public static void main(String[] args) {

		BufferedReader br = null;
		FileReader fr = null;
		BufferedWriter bw = null;
		FileWriter fw = null;



		System.out.println("Done");

		try {

			//br = new BufferedReader(new FileReader(FILENAME));
			fr = new FileReader(args[0]);
			br = new BufferedReader(fr);
			fw = new FileWriter(args[1]);
			bw = new BufferedWriter(fw);


			String sCurrentLine;
			// assuming each block no more than 20 strings
			String[] textBlock=new String[20];
			int i = 0;
			while ((sCurrentLine = br.readLine()) != null) {
				//System.out.println(sCurrentLine);
				if (sCurrentLine.equals("")) {
					// end old block
					if (i > 0) {
						alert a=new alert(textBlock);
						// print the block out
						if (a != null) {
							String ostr= a.genCSVOutput();
							bw.write(ostr);
						}
						i=0;
					}
				}

				else {
					textBlock[i]=sCurrentLine;
					i++;
				}

			}

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				if (br != null)
					br.close();

				if (fr != null)
					fr.close();

				if (bw != null)
					bw.close();

				if (fw != null)
					fw.close();


			} catch (IOException ex) {

				ex.printStackTrace();

			}

		}

	}

}

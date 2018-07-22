package mechanics;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Timestamp;

public class ErrorLog {
	private static String fileName = "./Logs/ErrorLog";
	
	public static void initLog(){
		File f = new File(fileName);
		if(!f.exists()) { 
		    try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void writeError(String location, Exception e){
		try {
		    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    PrintStream ps = new PrintStream(baos);
		    e.printStackTrace(ps);
		    ps.close();
			FileWriter fileWriter = new FileWriter(fileName, true);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			printWriter.println(timestamp);
			printWriter.println(location+": "+baos.toString());
			printWriter.close();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}
}

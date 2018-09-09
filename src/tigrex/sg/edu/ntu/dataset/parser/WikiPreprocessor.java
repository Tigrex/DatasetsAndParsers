package tigrex.sg.edu.ntu.dataset.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * http://konect.uni-koblenz.de/networks/wikipedia-growth
 * 
 * @author Ni Peng
 *
 */
public class WikiPreprocessor {
	
	final private Logger logger = LoggerFactory.getLogger(WikiPreprocessor.class);
	
	public void processFile(String path) {
		
		this.logger.debug("+processFile({})", path);
		
		try {
			
			BufferedReader br = new BufferedReader(new FileReader(path));
			
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + ".teg"), "utf-8"));
			
		    String line;
		    int count = 0;
		    
		    while ((line = br.readLine()) != null) {
		    	
		    	if (line.startsWith("%")) {
		    		continue;
		    	}
		    	
		    	count++;
		    	
		    	String[] parts = line.split("[\\s]+"); 
		    	
		    	if (parts.length != 4) {
		    		this.logger.error("Number of elements is {}, expected 4.", parts.length);
		    		this.logger.error(line);
		    		System.exit(1);
		    	}
		    	
		    	if (count % 1000000 == 0) {
		    		this.logger.debug("Reading line {}.", count);
		    	}
		    	
		    	String source = parts[0];
		    	String target = parts[1];
		    	String date = parts[2];
		    	
		    	String timestamp = date.replaceAll("-", "");
		    	
		    	try {
		    		Integer.parseInt(timestamp);
		    	} catch (NumberFormatException e) {
		    		this.logger.error("Date format error: {}.", date);
		    		System.exit(1);
		    	}
		    	
		    	bw.write(source + "," + target + "," + timestamp);
		    	bw.newLine();
//		    	bw.write(target + "," + source + "," + timestamp);
//		    	bw.newLine();
		    }
		    
		    br.close();
		    bw.close();
		    
		    this.logger.debug("Total number of lines is {}.", count); 
			
			this.logger.debug("-processFile({})", path);

			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	
	public static void main(String[] args) {
	
		WikiPreprocessor parser = new WikiPreprocessor();
		String path = "raw/wikipedia-growth.txt";

		parser.processFile(path);

	}

	
	
}




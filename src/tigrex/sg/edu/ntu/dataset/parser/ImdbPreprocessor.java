package tigrex.sg.edu.ntu.dataset.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ImdbPreprocessor {
	
	private Set<String> vertices;
	
	private Map<String, Map<String, Set<String>>> snapshots;
	
	final private Logger logger = LoggerFactory.getLogger(ImdbPreprocessor.class);
	
	public void readRaw(String path) {
		
		this.logger.debug("+readRaw({})", path);
		
		this.vertices = new HashSet<String>();
		
		this.snapshots = new HashMap<String, Map<String, Set<String>>>();
		
		int actorCount = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
		    String line;
		    int count = 0;
		    
		    // Skip header
		    line = br.readLine();
		    
		    while ((line = br.readLine()) != null) {
		    	count++;
		    	
		    	String[] parts = parseLine(line);
		    	
		    	if (parts.length != 12) {
		    		this.logger.error("Number of elements is {}, expected 12.", parts.length);
		    		this.logger.error(line);
		    		System.exit(1);
		    	}
		    	
		    	String[] actors = parts[5].split(",");
		    	actorCount += actors.length;
		    	
		    	String year = parts[6];

		    	List<String> beings = new ArrayList<String>();
		    	
		    	// Update vertices
	    		for (String actor: actors) {
	    			actor = actor.trim();
		    		this.vertices.add(actor);
		    		beings.add(actor);
		    	}
		    	
		    	// Update edges
		    	if (this.snapshots.containsKey(year)) {
		    		Map<String, Set<String>> edges = this.snapshots.get(year);
		    		
		    		for (String being: beings) {
		    			if (edges.containsKey(being)) {
		    				Set<String> neighbors = edges.get(being);
		    				neighbors.addAll(beings);
		    			} else {
		    				Set<String> neighbors = new HashSet<String>();
		    				neighbors.addAll(beings);
		    				edges.put(being, neighbors);
		    			}
		    		}
		    		
		    	} else {
		    		Map<String, Set<String>> edges = new HashMap<String, Set<String>>();
		    		this.snapshots.put(year, edges);
		    		
		    		for (String being: beings) {
		    			Set<String> neighbors = new HashSet<String>();
	    				neighbors.addAll(beings);
	    				edges.put(being, neighbors);
		    		}
		    		
		    	}
		    	
		    }
		    
		    for (String year: this.snapshots.keySet()) {
		    	Map<String, Set<String>> edges = this.snapshots.get(year);
		    	
		    	int totalEdges = 0;
		    	for (String being: edges.keySet()) {
		    		edges.get(being).remove(being);
		    		totalEdges += edges.get(being).size();
		    	}
		    	
		    	this.logger.debug("Snapshot {} has {} edges.", year, totalEdges);
		    }
		    
		    
		    this.logger.debug("Total number of vertices is {}.", actorCount);
		    
		    this.logger.debug("Total number of unique vertices is {}.", this.vertices.size());
		    
		    this.logger.debug("Total number of lines is {}.", count); 
			
			this.logger.debug("-readRaw({})", path);

			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	

	
	public String[] parseLine(String line) {
		// Remove double quotes
		line = line.replaceAll("\"\"", "");
		
		
		List<String> parts = new ArrayList<String>();
		
		StringBuilder builder = new StringBuilder();
		boolean startQuoted = false;
		
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			
			if (c == '"') {
				if (!startQuoted) {
					startQuoted = true;
				} else {
					startQuoted = false;
				}
				
			} else if (c == ',') {
				
				if (startQuoted) {
					builder.append(c);
				} else {
					String part = builder.toString();
					parts.add(part);
					builder.setLength(0);
				}
			} else {
				builder.append(c);
			}
			
		}
		parts.add(builder.toString());
		
		String[] result = new String[parts.size()];
		result = parts.toArray(result);

		return result;
	}
	
	
	@SuppressWarnings("unused")
	private void testParser() {
		
		String[] cases = {"single", "double1,double2", "triple1,triple2,triple3", "p1,p2,\"p3\",p4", "p1,p2,\"p3,p4\",p5"};
		
		for (String s:cases) {
			String parts[] = parseLine(s);
			System.out.println("Original: " + s);
			System.out.print("Parsed string: ");
			for (String p: parts) {
				System.out.print("(" + p + ")");
			}
			System.out.println();
		}
		
		
	}
	
	public void writeToFile(String path) {
		
		this.logger.debug("+writeFile({})", path);
			
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(path), "utf-8"));
			
			
		   for (String year: this.snapshots.keySet()) {
		    	Map<String, Set<String>> edges = this.snapshots.get(year);
		    	
		    	for (String being: edges.keySet()) {
		    		
		    		for(String neighbor: edges.get(being)) {
		    			
		    			String line = being + "," + neighbor + "," + year;
						writer.write(line);
						writer.newLine();
		    			
		    		}
		    		
		    	}
		    	
		    }
			
			writer.close();
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		this.logger.debug("-writeFile({})", path);
		
	}
	
	
	public static void main(String[] args) {
	
		ImdbPreprocessor parser = new ImdbPreprocessor();
		String path = "raw/IMDB-Movie-Data.csv";
		parser.readRaw(path);
		parser.writeToFile(path + ".teg");
		
	}

	
	
}




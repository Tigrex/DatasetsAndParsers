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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreprocessorForInsertedDeletions {
	
	private int numVertices;
	private int numSnapshots;
	
	private List<List<TemporalOutgoingEdge>> condensedGraph;
	
	final private Logger logger = LoggerFactory.getLogger(PreprocessorForInsertedDeletions.class);
	
	public void process(String path) {
		this.constructGraph(path);
		this.generateDeletions(0);
		this.writeEdgesToFile(path + ".deletions");
	}
	
	private void constructGraph(String path) {

		logger.debug("+constructGraph({})", path);
		
		Set<Integer> vertices = new HashSet<Integer>();
		Set<Integer> timestamps = new HashSet<Integer>();
		
		Map<Integer, Map<Integer, Integer>> condensed = new HashMap<Integer, Map<Integer, Integer>>() ;
		
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
		    String line;
		    String[] parts;

		    int numLines = 0;
		    
		    while ((line = br.readLine()) != null) {
		    	
		    	numLines++;
		    	if (numLines % 1000000 == 0) {
					logger.debug("Reading line {}...", numLines);
				}
		    	
		    	parts = line.split(",");
		    	int source = Integer.valueOf(parts[0]);
		    	int target = Integer.valueOf(parts[1]);
		    	int timestamp = Integer.valueOf(parts[2]);
		    	
		    	vertices.add(source);
		    	vertices.add(target);
		    	timestamps.add(timestamp);
		    	
		    	// Update condensedGraph
		    	if (condensed.containsKey(source)) {
		    		
		    		Map<Integer, Integer> outgoingEdges = condensed.get(source);
		    		
		    		if (outgoingEdges.containsKey(target)) {
		    			logger.error("Duplicate edges found ({}, {}), with timestamps {}, {}.", source, target, timestamp, outgoingEdges.get(target));
		    			System.exit(1);
		    		} else {
		    			outgoingEdges.put(target, timestamp);
		    		}
		    		
		    	} else {
		    		Map<Integer, Integer> outgoingEdges = new HashMap<Integer, Integer>();
		    		outgoingEdges.put(target, timestamp);
		    		condensed.put(source, outgoingEdges);
		    	}
		    	
		    }
		    
			int minVertexId = Integer.MAX_VALUE;
			int maxVertexId = 0;
			int minTimestamp = Integer.MAX_VALUE;
			int maxTimestamp = 0;
			
			for (int v: vertices) {
				if (v < minVertexId) {
					minVertexId = v;
				}
				if (v > maxVertexId) {
					maxVertexId = v;
				}
			}
			
			for (int t: timestamps) {
				if (t < minTimestamp) {
					minTimestamp = t;
				}
				if (t > maxTimestamp) {
					maxTimestamp = t;
				}
			}
			
			logger.info("Number of edges is {}.", numLines);
			logger.info("Number of vertices is {}.", vertices.size());
			logger.info("Number of snapshots is {}.", timestamps.size());
			
			if (minVertexId != 0) {
				logger.error("Min vertex id is {}.", minVertexId);
			}
			
			if (minTimestamp != 0) {
				logger.error("Min timestamp is {}.", minTimestamp);
			}
			
			if (maxVertexId - minVertexId + 1 != vertices.size()) {
				logger.error("Vertex id normalization error. Max vertex id is {}.", maxVertexId);
				System.exit(1);
			}

			if (maxTimestamp - minTimestamp + 1 != timestamps.size()) {
				logger.error("Timestamp id normalization error. Max timestamp id is {}.", maxTimestamp);
				System.exit(1);
			}
			
			this.numVertices = vertices.size();
			this.numSnapshots = timestamps.size();
			
			// Build array-based condensed graph
			this.condensedGraph = new ArrayList<List<TemporalOutgoingEdge>>(condensed.size());
			for (int i = 0; i < condensed.size(); i++) {
				Map<Integer, Integer> outgoingEdgesMap = condensed.get(i);
				List<TemporalOutgoingEdge> outgoingEdgesList = new ArrayList<TemporalOutgoingEdge>(outgoingEdgesMap.size());
				for (Integer target: outgoingEdgesMap.keySet()) {
					int timestamp = outgoingEdgesMap.get(target);
					outgoingEdgesList.add(new TemporalOutgoingEdge(target, timestamp));
				}
				
				Collections.sort(outgoingEdgesList);
				this.condensedGraph.add(outgoingEdgesList);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		logger.debug("-constructGraph({})", path);

	}
	
	private void generateDeletions(int seed) {
		
		this.logger.info("+generateDeletions()");

		Random rand = new Random(seed);
		
		for (int source = 0; source < this.numVertices; source++) {
			List<TemporalOutgoingEdge> outgoingEdges = this.condensedGraph.get(source);
			
			for (TemporalOutgoingEdge e: outgoingEdges) {
				int endTime = e.getStartTime() + rand.nextInt(this.numSnapshots - e.getStartTime());
				e.setEndTime(endTime);
			}
		}
		
		this.logger.info("-generateDeletions()");

		
	}
	
	private void writeEdgesToFile(String path) {
		
		this.logger.info("+writeEdgesToFile()");

		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(path), "utf-8"));
			
			for (int source = 0; source < this.condensedGraph.size(); source++) {
				List<TemporalOutgoingEdge> outgoingEdges = this.condensedGraph.get(source);
				
				for (TemporalOutgoingEdge e: outgoingEdges) {
					
					writer.write(source + "," + e.getTarget() + "," + e.getStartTime() + "," + e.getEndTime());
					writer.newLine();
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
		
		this.logger.info("-writeEdgesToFile()");
	}
		
	public static void main(String[] args) {
		
		PreprocessorForInsertedDeletions processor = new PreprocessorForInsertedDeletions();
		
//		String path = "raw/dblp-2018-01-01.xml.teg.sim";
//		String path = "raw/IMDB-Movie-Data.csv.teg.sim";
//		String path = "raw/wikipedia-growth.txt.teg.sim";
		String path = "raw/youtube-d-growth.txt.teg.sim";

		processor.process(path);

	}
		
}


package tigrex.sg.edu.ntu.dataset.parser;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dblp.DblpInterface;
import org.dblp.mmdb.Person;
import org.dblp.mmdb.PersonName;
import org.dblp.mmdb.Publication;
import org.dblp.mmdb.datastructure.SimpleLazyCoauthorGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * http://dblp.org/xml/release/
 * dblp-2018-01-01.xml.gz
 * 
 * @author Ni Peng
 *
 */
public class DblpPreprocessor {
	
	private DblpInterface dblp;

	private Map<Integer, List<List<String>>> yearlyPublications;
	
	private Map<Integer, Map<String, Set<String>>> teg;
	
	final private Logger logger = LoggerFactory.getLogger(DblpPreprocessor.class);

	public void getParser(String path) {
		
		long start = System.currentTimeMillis();
		this.logger.info("+getParser({})", path);

		// we need to raise entityExpansionLimit because the dblp.xml has millions of entities
		System.setProperty("entityExpansionLimit", "10000000");

		this.logger.debug("Building the dblp main memory DB...");
		
		try {
			this.dblp = new SimpleLazyCoauthorGraph(path);
		} catch (final IOException ex) {
			this.logger.error("Cannot read dblp XML: {}.", ex.getMessage());
			return;
		} catch (final SAXException ex) {
			this.logger.error("cannot parse XML: {}.", ex.getMessage());
			return;
		}
		
		this.logger.info("MMDB ready: {} publications, {} persons.", this.dblp.numberOfPublications(), this.dblp.numberOfPersons());
		
		long end = System.currentTimeMillis();
		this.logger.info("Get parser time: {} seconds.", (end-start)*1.0/1000);
		this.logger.info("-getParser({})", path);

	}
	
	public void analyzeAuthors() {
		
		this.logger.info("+analyzeAuthors()");
		
		int totalCount = this.dblp.getPersons().size();
		int disambiguationCount = 0;
		for(Person person: this.dblp.getPersons()) {
			if (person.isDisambiguation()) {
				disambiguationCount++;
			}
			
		}
		
		this.logger.info("Total number of authors is {}, disambiguation count is {}.", totalCount, disambiguationCount);
		
		this.logger.info("-analyzeAuthors()");

	}

	public void getYearlyPublications() {
		
		this.logger.info("+getYearlyPublications()");

		this.yearlyPublications = new HashMap<Integer, List<List<String>>>();
		
		int numPublications = 0;
		
		for (Publication publication: dblp.getPublications()) {
			int year = publication.getYear();
			
			List<String> coauthors = new LinkedList<String>();
			
			for (PersonName name: publication.getNames()) {
				
				if (name.getPerson().isDisambiguation()) {
					continue;
				}
	
				coauthors.add(name.getName());
			}
			
			if (coauthors.size() > 0) {
				if (this.yearlyPublications.containsKey(year)) {
					this.yearlyPublications.get(year).add(coauthors);
				} else {
					List<List<String>> publications = new LinkedList<List<String>>();
					publications.add(coauthors);
					this.yearlyPublications.put(year, publications);
				}
				
				numPublications++;
			}
			
		}
		
		this.logger.info("Number of years is {}.", this.yearlyPublications.size());
		this.logger.info("Number of publications is {}.", numPublications);
		this.logger.info("-getYearlyPublications()");
		
	}
	
	public void generateTEG() {
		
		this.logger.info("+generateTEG()");
		
		this.teg = new HashMap<Integer, Map<String, Set<String>>>();
		
		for (Integer year: this.yearlyPublications.keySet()) {
			List<List<String>> publications = this.yearlyPublications.get(year);
			
			Map<String, Set<String>> graph = new HashMap<String, Set<String>>();
			teg.put(year, graph);
			
			for (List<String> authors: publications) {
				
				for (String author: authors) {
					
					if (graph.containsKey(author)) {
						graph.get(author).addAll(authors);
					} else {
						Set<String> coauthors = new HashSet<String>();
						coauthors.addAll(authors);
						graph.put(author, coauthors);
					}
					
				}
				
			}
			
			for (String author: graph.keySet()) {
				graph.get(author).remove(author);
			}
			
		}
		
		this.logger.info("-generateTEG()");

	}
	
	public void writeGraphToFile(String path) {
		
		this.logger.info("+writeGraphToFile()");
		
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(path), "utf-8"));
			
			for (int year: this.teg.keySet()) {
				
				Map<String, Set<String>> graph = teg.get(year);

				for (String source: graph.keySet()) {
					for (String target: graph.get(source)) {
						String line = source + "," + target + "," + year;
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
		
		this.logger.info("-writeGraphToFile()");
			
	}

    public static void main(String[] args) {
    	
    	DblpPreprocessor parser = new DblpPreprocessor();
    	
    	String path = "raw/dblp-2018-01-01.xml";
    	parser.getParser(path);
    	parser.analyzeAuthors();
    	
    	parser.getYearlyPublications();
    	parser.generateTEG();
    	
    	parser.writeGraphToFile(path + ".teg");
    }
    
}

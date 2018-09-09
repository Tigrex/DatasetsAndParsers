package tigrex.sg.edu.ntu.dataset.parser;

import tigrex.sg.edu.ntu.graph.preprocessor.GraphPreprocessor;

public class WikiParser {

	public static void main(String[] args) {
		
		GraphPreprocessor preprocessor = new GraphPreprocessor();
		preprocessor.readFile("raw/wikipedia-growth.txt.teg");		
		
	}
	
}

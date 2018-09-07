package tigrex.sg.edu.ntu.dataset.parser;

import tigrex.sg.edu.ntu.graph.preprocessor.GraphPreprocessor;

public class DblpParser {
	
	public static void main(String[] args) {
		
		GraphPreprocessor preprocessor = new GraphPreprocessor();
		preprocessor.readFile("raw/dblp-2018-01-01.xml.teg");
		
	}

}

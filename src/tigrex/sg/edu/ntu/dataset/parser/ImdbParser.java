package tigrex.sg.edu.ntu.dataset.parser;

import tigrex.sg.edu.ntu.graph.preprocessor.GraphPreprocessor;

public class ImdbParser {
	
	public static void main(String[] args) {
		
		GraphPreprocessor preprocessor = new GraphPreprocessor();
		preprocessor.readFile("raw/IMDB-Movie-Data.csv.teg");
		
	}

}

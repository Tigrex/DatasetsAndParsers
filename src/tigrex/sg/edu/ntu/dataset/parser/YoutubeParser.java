package tigrex.sg.edu.ntu.dataset.parser;

import tigrex.sg.edu.ntu.graph.preprocessor.GraphPreprocessor;

public class YoutubeParser {

	public static void main(String[] args) {
		
		GraphPreprocessor preprocessor = new GraphPreprocessor();
		preprocessor.readFile("raw/youtube-d-growth.txt.teg");
		
	}
	
}

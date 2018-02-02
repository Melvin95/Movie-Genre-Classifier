package demos;

import java.io.*;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.AttributeFactory;

public class StopWordRemover {

	public static void main(String[] args) throws IOException{
		
		String s = "Remove the stop words here lucene, stop words are really annoying, why do we even use stop words in language?"
				+ " There's a stop word here, a stop word there, a word everywhere. There was";
		//Tokenize the string
	 	AttributeFactory factory = AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY;
	 	StandardTokenizer tokenizer = new StandardTokenizer(factory);
	 	tokenizer.setReader(new StringReader(s.toLowerCase()));  
	 	
	 	CharArraySet stopWords = EnglishAnalyzer.getDefaultStopSet();	//Lucene's stop word set
	 	
	 	System.out.println("Lucene's stop word set:");
	 	System.out.println(stopWords.toString());
	 	
	    TokenStream tokenStream =  tokenizer; //StopFilter only works with a TokenStream, don't know, don't care why
	    tokenStream = new StopFilter(tokenStream, stopWords);
	    StringBuilder sb = new StringBuilder();
	    CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
	    tokenStream.reset();
	    while (tokenStream.incrementToken()) {
	        String term = charTermAttribute.toString();
	        sb.append(term + " ");
	    }
	    tokenizer.close();
	    System.out.println("\nText before processing:");
	    System.out.println(s);
	    System.out.println("\nText with stop words removed:");
		System.out.println(sb.toString());
		
	}
}


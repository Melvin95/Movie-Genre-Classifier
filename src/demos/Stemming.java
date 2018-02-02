package demos;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Scanner;

import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.AttributeFactory;
import org.tartarus.snowball.ext.PorterStemmer;

public class Stemming {

	public static void main(String[] args) throws IOException {
		
	 	StringBuilder sb = new StringBuilder();
	 	
	 	Scanner sc = new Scanner(new FileReader("demo.txt"));
	 	
	 	//Tokenize text
	 	AttributeFactory factory = AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY;
	 	StandardTokenizer tokenizer = new StandardTokenizer(factory);
	 	while(sc.hasNextLine()){
	 		String line = sc.nextLine();
		 	tokenizer.setReader(new StringReader(line));
		 	tokenizer.reset();
		 	CharTermAttribute attr = tokenizer.addAttribute(CharTermAttribute.class);
		 	
		 	//Stem each token
		 	PorterStemmer ps = new PorterStemmer();
		 	while(tokenizer.incrementToken()){
		 		String term = attr.toString();
		 		ps.setCurrent(term);
		 		ps.stem();
		 		sb.append(ps.getCurrent()+" ");
		 	}
		 	tokenizer.close();
		}
	 	sc.close();
	 	System.out.println(sb.toString());
	}

}

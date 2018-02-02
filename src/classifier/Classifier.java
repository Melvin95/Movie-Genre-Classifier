package classifier;


import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.AttributeFactory;
import org.tartarus.snowball.ext.PorterStemmer;

/*
 * Classify movie summaries by genres from test/evaluation set based on
 * the data gained from training set(Training Sets) and PriorProb
 * 
 * Note: Took the log of probabilities to avoid underflow, so the likelihood score
 * 		is calculated by adding the probabilities instead of multiplying
 *		log(ab) = log(a) + log(b)
 *	
 *p(C)*p(w1|C)*p(w2|C)*...*p(wn|C) = log(p(c)) + log(p(w1|C)) +...+...log(p(wn|C))
 */
public class Classifier {
	
	//Used as a check, correct classification of a movie summary
	protected static Map<Integer,String> movieID_Genre = new HashMap<>();
	
	protected static Map<Integer,String> movieID_Summary = new HashMap<>();
	
	protected static Map<String,Float> genre_prob = new HashMap<>();
		
	public static void main(String[] args) throws IOException {
		setData();
		
	}
	
	/*
	 * @Param is the movie summary after stop words removed, stemming done
	 * 
	 */
	public static Map<String,Float> classify(String summary){
		Map<String,Float> likelihoodMap = new HashMap<>();
		String [] words = summary.split(" ");
		
		for(int g = 0; g<12;g++){
			Map<String,Float> probs = new HashMap<>();
			if(g==0){
				probs = getFeatureVec("Training Sets/Action.txt");
				//float score = calcScore(probs);
				//likelihoodMap.put("Action", score);
			}
			else if(g==1)
				probs = getFeatureVec("Training Sets/Adventure.txt");
			else if(g==2)
				probs = getFeatureVec("Training Sets/Comedy.txt");
			else if(g==3)
				probs = getFeatureVec("Training Sets/Crime.txt");
			else if(g==4)
				probs = getFeatureVec("Training Sets/Documentary.txt");
			else if(g==5)
				probs = getFeatureVec("Training Sets/Drama.txt");
			else if(g==6)
				probs = getFeatureVec("Training Sets/Fantasy.txt");
			else if(g==7)
				probs = getFeatureVec("Training Sets/Horror.txt");
			else if(g==8)
				probs = getFeatureVec("Training Sets/Romance.txt");
			else if(g==9)
				probs = getFeatureVec("Training Sets/Science Fiction.txt");
			else if(g==10)
				probs = getFeatureVec("Training Sets/Thriller.txt");
			else
				probs = getFeatureVec("Training Sets/Western.txt");
		}

		return likelihoodMap;
	}
	
	public static float calcScore(Map<String,Float> probs){
		float score = 0;
		
		return score;
	}
	
	public static void setData() throws IOException{
		Scanner evalSet = new Scanner(new FileReader("EvaluationSet.txt"));
		
		while(evalSet.hasNextLine()){
			int movieID = Integer.parseInt(evalSet.next());
			String summary = RemoveStopAndStem(evalSet.nextLine());
			movieID_Summary.put(movieID, summary);
			movieID_Genre.put(movieID,"");
		}
		evalSet.close();
		
		Scanner metaFile = new Scanner(new FileReader("newMetaData.txt"));
		while(metaFile.hasNextLine()){
			int movieID = Integer.parseInt(metaFile.next());
			if(movieID_Genre.containsKey(movieID)){
				String genre = metaFile.nextLine();
				movieID_Genre.put(movieID, genre);
			}
			else
				metaFile.nextLine();
		}
		metaFile.close();
		
		Scanner genreProb = new Scanner(new FileReader("PriorProb.txt"));
		while(genreProb.hasNextLine()){
			String genre = genreProb.next();
			float prob = genreProb.nextFloat();
			System.out.println(genre+" "+prob);
			genre_prob.put(genre,prob);
			genreProb.nextLine();
		}
		genreProb.close();
	}
	
	public static Map<String,Float> getFeatureVec(String fileName){
		Scanner featureSet = new Scanner(fileName);
		Map<String,Float> featureVector = new HashMap<>();
		while(featureSet.hasNextLine()){
			featureVector.put(featureSet.next(),featureSet.nextFloat());
			featureSet.nextLine();
		}
		featureSet.close();
		
		return featureVector;
	}
	
	 public static String RemoveStopAndStem(String line) throws IOException{
		 
		 	AttributeFactory factory = AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY;
		 	StandardTokenizer tokenizer = new StandardTokenizer(factory);
		 	
		 	//Remove stop words
		 	tokenizer.setReader(new StringReader(line.toLowerCase()));
		 	CharArraySet stopWords = EnglishAnalyzer.getDefaultStopSet();
		 	TokenStream tokenStream = tokenizer;
		 	tokenStream = new StopFilter(tokenStream, stopWords);
		 	CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
		 	tokenStream.reset();
		 	StringBuilder stopWordsRemoved = new StringBuilder();
		 	while(tokenStream.incrementToken()){
		 		String term = charTermAttribute.toString();
		 		stopWordsRemoved.append(term+" ");
		 	}
		 	tokenStream.close();
		 	
		 	//Stemming
		 	tokenizer.setReader(new StringReader(stopWordsRemoved.toString()));
		 	tokenizer.reset();
		 	charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);
			//Stem each token
			PorterStemmer ps = new PorterStemmer();
			StringBuilder sb = new StringBuilder();
			while(tokenizer.incrementToken()){
				String term = charTermAttribute.toString();
			 	ps.setCurrent(term);
			 	ps.stem();
			 	sb.append(ps.getCurrent()+" ");
			 }
			tokenizer.close();
		 	return sb.toString();
	 }

}

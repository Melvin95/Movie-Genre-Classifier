package corpusStuff;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.AttributeFactory;
import org.tartarus.snowball.ext.PorterStemmer;


/*
 * Main goal here is to clean out the corpus so we only process movie summaries that we have meta-data for
 * Corpus contains 42 306 summaries, of which only 37 800 of them have meta-data(genre)
 */
public class CorpusClean {

	/*
	 * Restricting classifier to these 12 main genres for simplicity.
	 * Corpus contains a lot more genres but these are the most common, outside of these genre, most are subsets of these(so regular expression should catch those)
	 * From original 42 306 plot summaries, after restricting genres, we still end up with 37 800 plot summaries
	 */
	protected static String [] mainGenres = {"Action","Adventure","Comedy","Crime",
											 "Drama","Documentary","Thriller","Horror",
											 "Fantasy","Science Fiction","Western","Romance"};
	
	protected static Map<Integer,String> movieID_Genre = new HashMap<>();
	
	protected static int removed = 0;
	
	public static void main(String[] args) throws IOException {
		//No need to run again
		//cleanMetaCorpus();			
		//cleanSummaryCorpus();
		//CreateDocs();
	}
	
	
	/*
	 * Current meta-file contains 80 000+ meta-data for movies, our corpus only has 42 306 plot summaries
	 * Function reduces meta-file to only those plot summaries, also reduces meta-data, only genre needed
	 * End up with a meta-file in the form of movieID+GENRE(S)
	 */
	public static void cleanMetaCorpus() throws IOException{
		int movieID = 0;
		try {
			FileWriter newMetaFile = new FileWriter("newMetaData.txt");
			Scanner summaryFile = new Scanner(new FileReader("Original Corpus/MovieSummaries/plot_summaries.txt"));
			while(summaryFile.hasNextLine()){
				movieID = Integer.parseInt(summaryFile.next());
				movieID_Genre.put(movieID, "");
				summaryFile.nextLine();
			}
			summaryFile.close();
			
			Scanner metaFile	= new Scanner(new FileReader("Original Corpus/MovieSummaries/movie.metadata.tsv"));
			while(metaFile.hasNextLine()){
				movieID = Integer.parseInt(metaFile.next());
				String line = metaFile.nextLine();
				//Check if movieID has a plot summary, if not: do nothing
				if(movieID_Genre.containsKey(movieID)){
					StringBuilder sbGenre = new StringBuilder();
					for(int i = 0; i<mainGenres.length;i++){
						Pattern genrePattern = Pattern.compile(".*\"(\\w*\\s*"+mainGenres[i]+"\\s*\\w*)\".*");
						Matcher matcher = genrePattern.matcher(line);
						if(matcher.matches()){
							if(sbGenre.length()==0)
								sbGenre.append(mainGenres[i]);
							else
								sbGenre.append("+"+mainGenres[i]);
						}
					}
					//If movie doesn't have any genres extracted(some won't have because of restriction on genre) remove it
					if(sbGenre.length()!=0){
						movieID_Genre.put(movieID, sbGenre.toString());
						newMetaFile.append(movieID+" "+sbGenre.toString()+"\n");
					}
					else
						movieID_Genre.remove(movieID);
				}
				
			}
			metaFile.close();
			newMetaFile.close();
		} 
		catch (IOException e) { e.printStackTrace(); }
		
	}
	
	/*
	 * Only need the 37 800 plot summaries whose genre we recored
	 * Also splitting corpus into training and evaluation set here
	 * Evaluation Set= 12 600, Training Set= 25 200
	 */
	public static void cleanSummaryCorpus() throws IOException{
		
		if(movieID_Genre.size()!=0){
			
			Scanner summaryFile = new Scanner(new FileReader("Original Corpus/MovieSummaries/plot_summaries.txt"));
			FileWriter evalSet = new FileWriter("EvaluationSet.txt");
			FileWriter trainSet = new FileWriter("Training.txt");
			int count = 0;
			
			while(summaryFile.hasNextLine()){
				int movieID = Integer.parseInt(summaryFile.next());
				String line = summaryFile.nextLine();
				if(movieID_Genre.containsKey(movieID)){
					
					if(count<12600)	//Evaluation Set
						evalSet.append(movieID+" "+line+"\n");
					else			//Training Set
						trainSet.append(movieID+" "+line+"\n");
					
					count++;
				}
			}
			summaryFile.close();
			evalSet.close();
			trainSet.close();
		}
		
	}
	
	/*
	 * Splits the training set. Movies summaries are put in different files based on genres
	 * This is required to make using the TFIDF scoring system easier 
	 * A movie can have more than one genre so obviously a movie may be in more than one file
	 */
	public static void CreateDocs() throws IOException{
		
		//Reading from
		Scanner summaryFile = new Scanner(new FileReader("Training.txt"));
		//Writing into 
		FileWriter actionFile 		= new FileWriter("Genres/Action.txt");
		FileWriter adventureFile 	= new FileWriter("Genres/Adventure.txt");
		FileWriter comedyFile 		= new FileWriter("Genres/Comedy.txt");
		FileWriter crimeFile 		= new FileWriter("Genres/Crime.txt");
		FileWriter documentaryFile 	= new FileWriter("Genres/Documentary.txt");
		FileWriter dramaFile 		= new FileWriter("Genres/Drama.txt");
		FileWriter fantasyFile 		= new FileWriter("Genres/Fantasy.txt");
		FileWriter horrorFile 		= new FileWriter("Genres/Horror.txt");
		FileWriter romanceFile 		= new FileWriter("Genres/Romance.txt");
		FileWriter sciFiFile 		= new FileWriter("Genres/Science Fiction.txt");
		FileWriter thrillerFile 	= new FileWriter("Genres/Thriller.txt");
		FileWriter westernFile 		= new FileWriter("Genres/Western.txt");
		
		Pattern pattern = Pattern.compile("(Science Fiction|\\w+)");
		while(summaryFile.hasNextLine()){
			int movieID = Integer.parseInt(summaryFile.next());
			String line = RemoveStopAndStem(summaryFile.nextLine());
			Matcher matcher = pattern.matcher(movieID_Genre.get(movieID));
			while(matcher.find()){
				String genre = matcher.group(1);
				switch(genre){
				case("Action"):
					actionFile.append(line+"\n");
					break;
					
				case("Adventure"):
					adventureFile.append(line+"\n");
					break;
					
				case("Comedy"):
					comedyFile.append(line+"\n");
					break;
					
				case("Crime"):
					crimeFile.append(line+"\n");
					break;
					
				case("Drama"):
					dramaFile.append(line+"\n");
					break;
					
				case("Documentary"):
					documentaryFile.append(line+"\n");
					break;
					
				case("Fantasy"):
					fantasyFile.append(line+"\n");
					break;
					
				case("Horror"):
					horrorFile.append(line+"\n");
					break;
					
				case("Thriller"):
					thrillerFile.append(line+"\n");
					break;
					
				case("Science Fiction"):
					sciFiFile.append(line+"\n");
					break;
					
				case("Romance"):
					romanceFile.append(line+"\n");
					break;
					
				case("Western"):
					westernFile.append(line+"\n");
					break;	
					
				}
			}
		}
		summaryFile.close();	actionFile.close();		adventureFile.close();		comedyFile.close();
		crimeFile.close();		dramaFile.close();		documentaryFile.close();	horrorFile.close();
		thrillerFile.close();	fantasyFile.close();	westernFile.close();		romanceFile.close();
		sciFiFile.close();
	}
	
	/*
	 * Using Lucene 6.5.0 with Snowball
	 * Takes a line and stems each word in that line based on the Porter Stemmer algorithm
	 * Stemming will give a more true TF-IDF weighting
	 * We wouldn't want to get low counts because of words that basically have the same meaning are in different forms
	 * Example: Don't want to get seperate counts on words like kill, killed, killing, kills
	 * Low counts = low TF-IDF score
	 * Removing stop words first and then stemming because the Porter Stemmer algorithm
	 */
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

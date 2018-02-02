package corpusStuff;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.io.*;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;

/*
 * Aim is to get the TF-IDF weighting on words from documents to remove words that aren't "unique" to that document
 * Each document is a distinct genre containing movie summaries of that genre
 * TF: 	number of times a certain word appeared in a particular document(genre)
 * IDF:	number of times a certain word appeared across all documents(genres(
 * W = TF * IDF
 * Using TFIDF weighting very loosely, probably more interested in the IDF for now
 */
public class LuceneTFIDF {
	
	public static void main(String[] args) throws IOException {
		StandardAnalyzer analyzer  = new StandardAnalyzer();	//Analyzer used in tokenizing text and indexing/searching
		Directory index = new RAMDirectory();					//Create the index 
		IndexWriterConfig config = new IndexWriterConfig(analyzer);		
		IndexWriter w = new IndexWriter(index,config);
		//Add genre documents to index
		addDoc(w,"Genres/Action.txt");			//0		
		addDoc(w,"Genres/Adventure.txt");		//1	
		addDoc(w,"Genres/Comedy.txt");			//2
		addDoc(w,"Genres/Crime.txt");			//3
		addDoc(w,"Genres/Documentary.txt");		//4
		addDoc(w,"Genres/Drama.txt");			//5	
		addDoc(w,"Genres/Fantasy.txt");			//6 
		addDoc(w,"Genres/Thriller.txt");		//7	
		addDoc(w,"Genres/Horror.txt");			//8
		addDoc(w,"Genres/Science Fiction.txt");	//9	
		addDoc(w,"Genres/Western.txt");			//10	  
		addDoc(w,"Genres/Romance.txt");			//11
		//addDoc(w,"Demo.txt");
		//addDoc(w,"Demo2.txt");
		w.close();
		
		
		IndexReader reader = DirectoryReader.open(index);	//Used to access documents once they've been added to index
		featureVec(reader);
		write(reader,"Summary");
		//readFeatureVec();
		reader.close();   //Only close when no need to access documents anymore
	}
	
	public static void addDoc(IndexWriter w,String filename)throws IOException{
		Scanner file = new Scanner(new FileReader(filename));
		Document doc = new Document();
		FieldType f = new FieldType();
		f.setStored(true);
		f.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
		f.setStoreTermVectors(true);
		f.setStoreTermVectorPositions(true);
		f.setTokenized(true);
		f.freeze();
		String summary = "";
		while(file.hasNextLine()){
			summary += file.nextLine() +" ";
		}
		doc.add(new Field("Summary",summary,f));	//TextField tokenizes the value in this field
		w.addDocument(doc);
		file.close();
	}
	
	//Returns a map of terms and their corresponding frequencies for a particular genre(docID)
	public static Map<String,Long> getTF(IndexReader reader,String field, int docID)throws IOException{
		Terms termVector = reader.getTermVector(docID, field);
		TermsEnum termsEnum = termVector.iterator();	//Iterate through terms(each word of summary)
		BytesRef  term = null;
		Map<String,Long> termFreq = new HashMap<>();
		while( (term = termsEnum.next()) != null ){
			termFreq.put(term.utf8ToString(), termsEnum.totalTermFreq());
		}
		return termFreq;
	}
	//Compute and return IDF for a given term 
	public static double getIDF(IndexReader reader, String field,String term) throws IOException{
		return Math.log( reader.numDocs()/(double)(reader.docFreq(new Term(field,term))) );
	}
	
	//Returns a map of words with their corresponding frequencies for a particular genre BUT..
	//Only considering words with a TFIDF weighting>0, that is idf!=0
	public static Map<String,Long> getTFIDF(IndexReader reader,String field,int docID)throws IOException{
		Map<String,Long> tfidf = new HashMap<>();
		
		Terms termVector = reader.getTermVector(docID, field);
		TermsEnum termsEnum = termVector.iterator();
		BytesRef term = null;
		while( (term=termsEnum.next())!=null ){
			double idf = getIDF(reader,field,term.utf8ToString());
			if( idf*(double)termsEnum.totalTermFreq()>1 )
				tfidf.put(term.utf8ToString(), termsEnum.totalTermFreq());			
		}
		return tfidf;
	}
	
	public static LinkedList<String> getFeatureVector(IndexReader reader,String field) throws IOException{
		LinkedList<String> li = new LinkedList<>();
		for(int docID=0;docID<reader.numDocs();docID++){
			Terms termVector = reader.getTermVector(docID, field);
			TermsEnum termsEnum = termVector.iterator();
			BytesRef term = null;
			while( (term=termsEnum.next())!=null ){
				double idf = getIDF(reader,field,term.utf8ToString());
				if( (idf*(double)termsEnum.totalTermFreq()>8) && termsEnum.totalTermFreq()>1 && !li.contains(term.utf8ToString()))
					li.addLast(term.utf8ToString());
			}
		}	//Do this for all documents(genres)
		return li;
	}
	
	//Getting feature set,writes words to a text file
	public static void featureVec(IndexReader reader) throws IOException{
		FileWriter file = new FileWriter("FeatureSet.txt");
		LinkedList<String> featureVector = getFeatureVector(reader,"Summary");
		System.out.println(featureVector.size());
		while(featureVector.size()!=0){
			file.append(featureVector.removeFirst()+"\n");
		}
		file.close();
	}
	
	public static LinkedList<String> readFeatureVec() throws FileNotFoundException{
		Scanner file = new Scanner(new FileReader("FeatureSet.txt"));
		LinkedList<String> list= new LinkedList<>();
		int c = 0;
		while(file.hasNextLine()){
			list.addLast(file.nextLine());
			c++;
		}
		System.out.println(c);
		
		file.close();
		return list;
	}
	
	//Calculate probability of word given the genre, P(w|G)
	//Returns a map of words and their probabilities
	public static Map<String,Double> getProbWord(Map<String,Long> termFreq,LinkedList<String> vocab) throws FileNotFoundException{
		
		Map<String,Double> probWord = new HashMap<>();
		
		int n = 0;
		//Get # of words in document(genre) that are in the reduced vocabulary
		for(Map.Entry<String, Long> freqEntry:termFreq.entrySet()){
			if(vocab.contains(freqEntry.getKey()) )
					n += freqEntry.getValue();
		}
		
		//Calculate probabilities of word|genre
		//Laplace smoothing considering vocabulary that doesn't appear in the genre
		for(int i=0; i<vocab.size();i++){
			double p = Math.log10( (double)(termFreq.getOrDefault(vocab.get(i), (long) 0)+1)/(double)(n+vocab.size()));
			probWord.put(vocab.get(i), p);
		}
		return probWord;	
	}
	
	//Writing a word and the probability of that word occurring in it's genre
	public static void write(IndexReader reader,String field) throws IOException{
		
		LinkedList<String> vocab = readFeatureVec();
		
		//Iterate documents
		for(int doc=0; doc<reader.numDocs();doc++){
			Map<String,Long> termFreq = getTFIDF(reader,field,doc);
			Map<String,Double> probWord = getProbWord(termFreq,vocab);
			
			//Write to text files...
			switch(doc){
			case 0:
				helpWrite("Training Sets/Action.txt",probWord);
				break;
			case 1:
				helpWrite("Training Sets/Adventure.txt",probWord);
				break;
			case 2:
				helpWrite("Training Sets/Comedy.txt",probWord);
				break;
			case 3:
				helpWrite("Training Sets/Crime.txt",probWord);
				break;
			case 4:
				helpWrite("Training Sets/Documentary.txt",probWord);
				break;
			case 5:
				helpWrite("Training Sets/Drama.txt",probWord);
				break;
			case 6:
				helpWrite("Training Sets/Fantasy.txt",probWord);
				break;
			case 7:
				helpWrite("Training Sets/Thriller.txt",probWord);
				break;
			case 8:
				helpWrite("Training Sets/Horror.txt",probWord);
				break;
			case 9:
				helpWrite("Training Sets/Science Fiction.txt",probWord);
				break;
			case 10:
				helpWrite("Training Sets/Western.txt",probWord);
				break;	
			case 11:
				helpWrite("Training Sets/Romance.txt",probWord);
				break;	
			default:
				System.out.println("NOTHING TO WRITE INTO");
			}	
		}
	}
	
	public static void helpWrite(String filename,Map<String,Double> wordProbs) throws IOException{
		FileWriter file = new FileWriter(filename);
		for(Map.Entry<String, Double> entry:wordProbs.entrySet()){
			file.append(entry.getKey()+" "+entry.getValue()+"\n");
		}
		file.close();
	}	
}

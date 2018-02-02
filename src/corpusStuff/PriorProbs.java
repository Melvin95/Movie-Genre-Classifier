package corpusStuff;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PriorProbs {

	static Map<Integer,String> metaData = new HashMap<>();
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		fillMetaMap();
		
		Scanner trainingFile = new Scanner(new FileReader("Training.txt"));
	
		int genreCount = 0;
		int actionCount = 0;
		int adventureCount = 0;
		int documentaryCount = 0;
		int crimeCount = 0;
		int comedyCount = 0;
		int dramaCount = 0;
		int thrillerCount = 0;
		int horrorCount = 0;
		int romanceCount = 0;
		int scifiCount = 0;
		int fantasyCount = 0;
		int westernCount = 0;
		
		Pattern pattern = Pattern.compile("(Science Fiction|\\w+)");
		while(trainingFile.hasNextLine()){
			int movieID = Integer.parseInt(trainingFile.next());
			if(metaData.containsKey(movieID)){
				Matcher matcher = pattern.matcher(metaData.get(movieID));
				trainingFile.nextLine();
				while(matcher.find()){
					String genre = matcher.group(1);
					switch(genre){
					case("Action"):
						genreCount++;
						actionCount++;
						break;
						
					case("Adventure"):
						genreCount++;
						adventureCount++;
						break;
						
					case("Comedy"):
						genreCount++;
						comedyCount++;
						break;
						
					case("Crime"):
						genreCount++;
						crimeCount++;
						break;
						
					case("Drama"):
						genreCount++;
						dramaCount++;
						break;
						
					case("Documentary"):
						genreCount++;
						documentaryCount++;
						break;
						
					case("Fantasy"):
						genreCount++;
						fantasyCount++;
						break;
						
					case("Horror"):
						genreCount++;
						horrorCount++;
						break;
						
					case("Thriller"):
						genreCount++;
						thrillerCount++;
						break;
						
					case("Science Fiction"):
						genreCount++;
						scifiCount++;
						break;
						
					case("Romance"):
						genreCount++;
						romanceCount++;
						break;
						
					case("Western"):
						genreCount++;
						westernCount++;
						break;		
					}
				}
			}
			else
				trainingFile.nextLine();
		}
		trainingFile.close();
		
		FileWriter priorProbFile = new FileWriter("PriorProb.txt");
		
		priorProbFile.append("Action "+ Math.log10(((double)actionCount/(double)genreCount)) +"\n");
		priorProbFile.append("Adventure "+ Math.log10(((double)adventureCount/(double)genreCount)) +"\n");
		priorProbFile.append("Crime "+ Math.log10(((double)crimeCount/(double)genreCount)) +"\n");
		priorProbFile.append("Comedy "+ Math.log10(((double)comedyCount/(double)genreCount)) +"\n");
		priorProbFile.append("Documentary "+ Math.log10(((double)documentaryCount/(double)genreCount)) +"\n");
		priorProbFile.append("Fantasy "+ Math.log10(((double)fantasyCount/(double)genreCount)) +"\n");
		priorProbFile.append("Horror "+ Math.log10(((double)horrorCount/(double)genreCount)) +"\n");
		priorProbFile.append("Thriller "+ Math.log10(((double)thrillerCount/(double)genreCount)) +"\n");
		priorProbFile.append("Scifi "+ Math.log10(((double)scifiCount/(double)genreCount)) +"\n");
		priorProbFile.append("Western "+ Math.log10(((double)westernCount/(double)genreCount)) +"\n");
		priorProbFile.append("Romance "+ Math.log10(((double)romanceCount/(double)genreCount)) +"\n");
		priorProbFile.append("Drama "+ Math.log10(((double)dramaCount/(double)genreCount)) +"\n");
		priorProbFile.close();

	}
	
	public static void fillMetaMap() throws FileNotFoundException{
		Scanner metaFile = new Scanner(new FileReader("newMetaData.txt"));
		
		while(metaFile.hasNextLine()){
			int movieID = Integer.parseInt(metaFile.next());
			String line = metaFile.nextLine();
			metaData.put(movieID,line);
		}
		metaFile.close();
		
		System.out.println(metaData.size());
	}

}

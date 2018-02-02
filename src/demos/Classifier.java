package demos;

import java.util.ArrayList;
import java.util.Arrays;

public class Classifier {


		/**
		 * @param args
		 */

		public static void main(String[] args) {
			// TODO Auto-generated method stub
			
			//Data
			String[] c = {"Chinese", "Beijing", "Chinese", "Chinese", "Chinese", "Shanghai", "Chinese", "Macao"};
			String[] j =  {"Tokyo", "Japan", "Chinese"};
			String[] test = {"Chinese", "Chinese", "Chinese", "Tokyo", "Japan"};
			
			//Category counts
			double pc = 0.75;
			double pj = 0.25;
			
			ArrayList<Double> pcalc = new ArrayList<Double>();
			pcalc.add(pc);
			pcalc.add(pj);
			
			//2-D array which holds all arrays
			String[][] allArrays = {c, j};//, test, c2};
			
			int vocab = vocabSize(allArrays);
			
			for (String[] array : allArrays) {
			    System.out.println("\nNew Array : " + Arrays.toString(array)+"\n");
			    Double count = pcalc.get(0);
			    for (int i = 0; i < test.length; i++){
					System.out.println(test[i] + " = " + (getCount(test[i], array)+1) + " / " + (array.length+vocab));
					double num = (getCount(test[i], array) + 1);
					double den = (array.length + vocab);
					count = count * (num / den);
				}
			    pcalc.remove(0);
			    
			    //"count" variable represents final probability for the current class / genre
			    System.out.println(count);
			}

		}
		
		static int vocabSize(String[][] allArrays){
			ArrayList<String> vocab = new ArrayList<String>();
			for (String[] array : allArrays) {
				for (int i = 0; i < array.length; i++){
					if(!vocab.contains(array[i])){
						vocab.add(array[i]);
					}
				}
			}
			return vocab.size();
			
		}
		
		static int getCount(String s, String[] words){
			int count = 0;
			for (int i = 0; i < words.length; i++){
				if(s.equals(words[i])){
					count++;
				}
			}
			return count;
		}

	}

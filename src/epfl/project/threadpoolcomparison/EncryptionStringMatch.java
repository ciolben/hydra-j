package epfl.project.threadpoolcomparison;

import java.io.*;

/**
 * Use for testing the StringMatch benchmark. This class encrypt a text file
 * @author Nicolas
 *
 */
public class EncryptionStringMatch {
	public static void main(String[] args) {

                String input;
                if (args.length == 2) {
                     input = args[1];
                } else {
                     input = "word_10MB.txt";
                }
                
		String output = "encryption.txt";
			
		try {
			BufferedReader r = new BufferedReader(new FileReader(input));
			BufferedWriter w = new BufferedWriter(new FileWriter(output)); 
			String text = "";
			while ((text = r.readLine()) != null) {
				String[] splitTxt = text.split(" ");
				for(String word : splitTxt) {
					w.write(encryption(word) + "\n");
				}
			}
			r.close();
			w.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String encryption(String word) {
		String encryptedWord = "";
        for (int i = 0; i < word.length(); i++) {
            encryptedWord += (char) (((int) word.charAt(i)) + 1);
        }
        return encryptedWord;
	}
}

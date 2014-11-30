package nl.et4it;

public class Tokenizer {
	public static String[] tokenize(String message) {
		String cleanedMessage = message;
		// Links
		cleanedMessage = cleanedMessage.replaceAll("(http:|ftp:|https:|www.)[^ ]+[ |\r|\n|\t]", " ").replaceAll("(http:|ftp:|https:|www.).*", "");
		// Newlines
		cleanedMessage = cleanedMessage.replaceAll("\n", " ").replaceAll("\r", "");
    	// Non-latin characters
		cleanedMessage = cleanedMessage.replaceAll("#[0-9a-zA-z]+", " ").replaceAll("@[0-9a-zA-z]+", " ");
    	
		String[] replacements = {"\\.", ",", ":", ";", "\\?", "!", "'", "\"",
    			"\\[", "\\]", "\\(", "\\)", "#", "@", "\\+", "\\*", "=", "\\^", "%", "\\$", "&", "\\%", "/"};
    	// Surround special characters by space
    	for (int i = 0; i < replacements.length; i++) {
    		String replacement = replacements[i];
    		cleanedMessage = cleanedMessage.replaceAll(replacement, " " + replacement + " ");
    	}
    	// Multiple whitespaces
    	cleanedMessage = cleanedMessage.replaceAll("[ |\t]+", " ");
    	// De-capitalization
    	String[] tokens = cleanedMessage.split(" ");
    	for (int i = 0; i < tokens.length; i++) {
    		if (tokens[i].length() > 0) {
	    		// First char remains the same, the others become lower case
	    		tokens[i] = tokens[i].charAt(0) + tokens[i].substring(1).toLowerCase();
    		}
    	}
    	
    	return tokens;
	}
}

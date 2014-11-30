package nl.et4it.examples;

import nl.et4it.LIGA;
import nl.et4it.Tokenizer;

public class LIGAExample {
	private static String joiner(String[] toJoin, String separator) {
		StringBuilder builder = new StringBuilder();
		for(String s : toJoin) {
		    builder.append(s + separator);
		}
		return builder.toString();
	}
	
	public static void main(String[] args) {
		// Create LIGA instance
        LIGA liga = new LIGA();
        // Load LIGA model
        liga.loadModel();
        // Classify some messages
        System.out.println(joiner(Tokenizer.tokenize("Bankers are the assassins of hope."), " "));
        System.out.println(liga.classify(joiner(Tokenizer.tokenize("Bankers are the assassins of hope."), " "))); // en_UK
        System.out.println(liga.classify(joiner(Tokenizer.tokenize("Dit is ook een test"), " "))); // nl_NL
        System.out.println(liga.classify(joiner(Tokenizer.tokenize("Und diese war auch eine Teste"), " "))); // de_DE
        System.out.println(liga.classify(joiner(Tokenizer.tokenize("Wir haben wegen # prism Anzeige gegen Unbekannt gestellt , damit mal nach unseren Grundrechten gefahndet wird :"), " "))); // de_DE
        System.out.println(liga.classify(joiner(Tokenizer.tokenize("Cezaevinde anneleriyle kalan 0-6 yaş TUTUKSUZ TUTUKLU 35 BEBEK için pabuç ve biberon gerekiyor, destek olmak isteyen?"), " "))); // tr_TR
        
        System.out.println(liga.classify(joiner(Tokenizer.tokenize("Ganu arep di bojo ora gelem yemm RT FitRia_Maniez : Masi berkutat dgn pekerjaan . . workaholic kpn mbojone . . ! !"), " "))); // tl_TL
        System.out.println(liga.classify(joiner(Tokenizer.tokenize("Hehehe , , , iyhaaa , wah kpn ktmu kwe mneh yo lin , sue tenan og : ( LiendaSulisty : holohh koe ki iyo ajek ngunukui te pangling"), " "))); // tl_TL
	}
}

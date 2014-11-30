<?php

/**
 * LanguageIdentifier.php - Description
 *
 * Description
 *
 *
 * @author     Erik Tromp
 * @copyright
 *
 * @version
 * @link
 * @since
 * @filesource
 */

/**
 * LanguageIdentifier - Description
 *
 *
 * @copyright  
 * @author     Erik Tromp
 * @version    
 * @since      
 */
class LanguageIdentifier {

    public static $model = array();
    public static $counter = array();
    public static $threshold = 0.00125; // Magic threshold
    const UNKNOWN_LANGUAGE = 'UNKNOWN';

    /**
     * Add a document to the model
     * @param string $document The document to add
     * @param string $language The language the document is in
     */
    public static function addDocument($document, $language, $pre = false) {
    	$document = strtolower($document);
		if ($pre) {
			$document = self::preProcess($document);
		}
    	
    	// Language counters for edges and nodes
        if	(!isset(self::$counter[$language]))
        	self::$counter[$language] = array(
        		'nodes' => 0,
        		'edges' => 0
        	);

        // Keep track of previous node
        $prevNode = null;
        // Go through the string creating 3-grams
        $n = strlen($document);
        $ch1 = '';
        $ch2 = '';
        $ch3 = '';
        for ($i = 2; $i < $n; $i++) {
        	// Get the 3 characters comprising the trigram
        	$ch1 = $document[$i - 2];
        	$ch2 = $document[$i - 1];
        	$ch3 = $document[$i];
        	// Make trigram
        	$trigram = $ch1.$ch2.$ch3;
        	
        	// Add node to graph
            Graph::addNode($trigram, $language);
            self::$counter[$language]['nodes'] += 1;
            
        	// Add edge if possible
            if (isset($prevNode)) {
                Graph::addEdge($prevNode, $trigram, $language);
                self::$counter[$language]['edges'] += 1;
            }
            
            // Update previous node
            $prevNode = $trigram;
        }
        
        self::$model = Graph::$graph;
    }

    /**
      * Stores the learned model. Loading it in later prevents time-consuming training
      */
    public static function storeModel() {
        // Serialize the model
        $serialModel = serialize(self::$model);
        // Store model
        file_put_contents('Cache/identifyModel', $serialModel);
        // Serialize the counters
        $serialCounter = serialize(self::$counter);
        // Store counters
        file_put_contents('Cache/identifyCounter', $serialCounter);
    }

    /**
      * Loads the learned model.
      */
    public static function loadModel() {
        // Get model from cache
        $serialModel = file_get_contents('Cache/identifyModel');
        // Unserialize to real data
        self::$model = unserialize($serialModel);
        // Get counters from cache
        $serialCounter = file_get_contents('Cache/identifyCounter');
        // Unserialize to real data
        self::$counter = unserialize($serialCounter);
		Graph::$graph = self::$model;
		Graph::$counters = self::$counter;
    }
    
    /**
     * Sets the threshold to determine unknown languages
     * @param real $threshold The threshold
     */
    public static function setThreshold($threshold) {
    	self::$threshold = $threshold;
    }

    /**
     * Identifies the language of given document when the model is loaded
     * @param string $document The document to determine the language of
     * @return string The language the document is in
     */
    public static function Identify($document) {
    	// Clean document
    	$document = strtolower($document);
    	$document = self::preProcess($document);
    	
        // Get the ngrams
        $ngrams = self::getNgrams($document);

        // Match the path
        Graph::$counters = self::$counter;
        $scores = Graph::pathMatching(self::$model, $ngrams, array(), 0, 80);

        // Find the maximum scoring language
        $maxScore = -1;
        $maxLanguage = "";
        foreach ($scores as $language => $score) {
            // Check if the score is the max so far
            if ($score > $maxScore) {
                // Update maximum score/language
                $maxScore = $score;
                $maxLanguage = $language;
            }
        }

        // Determine threshold
        $threshold = $maxScore / strlen($document);
        if ($threshold < self::$threshold) {
        	$maxLanguage = self::UNKNOWN_LANGUAGE;
        }

        // Return maximum scoring language
        return $maxLanguage;
    }

    /**
     * Creates the ngrams of a given word
     *
     * @param string $match                    The string to create NGrams of
     * @param int $n                           Default 3, the length of the NGrams
     * @return array                           The NGrams
     */
    private static function getNgrams($match, $n = 3) {
        $ngrams = array();
        $len = strlen($match);
        // Go through the string char by char
        for ($i = 0; $i < $len; $i++) {
            // Check if we can actually get a long enough NGram
            if ($i > ($n - 2)) {
                // Get NGram
                $ng = '';
                for ($j = $n - 1; $j >= 0; $j--)
                    $ng .= $match[$i - $j];
                // Add NGram
                $ngrams[] = $ng;
            }
        }
        // Return NGrams
        return $ngrams;
    }
    
    /**
     * Removes garnage from a document
     * @param string $document The document to preprocess
     * @return string The cleaned document
     */
    private static function preProcess($document) {
    	// Tokenize
    	$words = explode(" ", $document);
    	// Go through all words
    	$newWords = array();
    	foreach ($words as $word) {
	    	// Check for unwanted prefixes, matches, affixes etc
			if (!($word[0] == "@" ||
				substr($word, 0, 4) == "http" ||
				substr($word, 0, 3) == "www" ||
				$word == "rt" ||
				substr($word, 0, 3) == "rt@" ||
				$word == "fb")) {

				//$word = preg_replace("/[^a-zA-Z0-9\säüïöëáàúùíìóòéèãõñâôûîßê]/", "", $word);
				$newWords[] = $word;
			}
    	}
    	
    	return implode(" ", $newWords);
    }
    
    /**
     * Resets the model, useful for cross-validation
     */
    public static function resetModel() {
    	self::$model = array();
    	self::$counter = array();
    	Graph::$graph = array();
    }

}

?>
package nl.et4it;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.MutablePair;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

public class LIGA {
	double threshold = 0.0125;
	// This is our model
	HashMap<String, HashMap<String, Integer>> nodes = new HashMap<String, HashMap<String,Integer>>();
	HashMap<String, HashMap<String, HashMap<String, Integer>>> edges = new HashMap<String, HashMap<String, HashMap<String, Integer>>>();
	HashMap<String, MutablePair<Integer, Integer>> counter = new HashMap<String, MutablePair<Integer, Integer>>();

	/**
	 * Adds a node (N-gram) to the model
	 * @param node
	 * @param language
	 */
	private void addNode(String node, String language) {
		// Check if this node exists
        if (!nodes.containsKey(node)) {
            // Add the node as it doesn't exist yet
        	nodes.put(node, new HashMap<String, Integer>());
        	edges.put(node, new HashMap<String, HashMap<String, Integer>>());
        }
        // Initialize the count for this node for this language
        if (!nodes.get(node).containsKey(language))
            nodes.get(node).put(language, 0);        
        // Increase the counter for this language
        nodes.get(node).put(language, nodes.get(node).get(language) + 1);
        
        // Update the total counter
        if (!counter.containsKey(language))
            counter.put(language, new MutablePair<Integer, Integer>(0, 0));
        counter.get(language).setLeft(counter.get(language).getLeft() + 1);
    }
	
	/**
	 * Adds an edge between two existing nodes
	 * @param source
	 * @param target
	 * @param language
	 */
	private void addEdge (String source, String target, String language) {
		// Check if there is already an edge
		if (!edges.get(source).containsKey(target))
			edges.get(source).put(target, new HashMap<String, Integer>());

		// Check if there is an edge for this language
		if (!edges.get(source).get(target).containsKey(language))
			edges.get(source).get(target).put(language, 0);
		
        // Increase the count
        edges.get(source).get(target).put(language, edges.get(source).get(target).get(language) + 1);
        
        // Update the total counter, source node and target node should exist for this language
        counter.get(language).setRight(counter.get(language).getRight() + 1);
    }
	
	/**
	 * Recursive path matching function
	 * @param path
	 * @param currentScores
	 * @param depth
	 * @param maxDepth
	 * @return
	 */
	private HashMap<String, Double> recPathMatching(List<String> path, HashMap<String, Double> currentScores, Integer depth, Integer maxDepth) {
		if (depth > maxDepth || path.size() == 0)
			return currentScores; // Done traversing, return accumulator
        else if (path.size() == 1) {
        	String ngram = path.get(0);
    	    // There is just one node left, just count node and disregard edges
    	    if (nodes.containsKey(ngram)) {
    	        // Add up scores
    	    	for (Entry<String, Integer> langCounts : nodes.get(ngram).entrySet()) {
    	    		String language = langCounts.getKey();
    	    		Double cnt = (double) langCounts.getValue();
    	    		// Update score
    	    		if (!currentScores.containsKey(language))
    	    			currentScores.put(language, 0.0);
    	    		currentScores.put(language, currentScores.get(language) + (cnt / counter.get(language).getRight()));
    	    	}
    	    }
    	    return currentScores;
	    }
        else {
        	// Get source and target
        	String source = path.get(0);
        	String target = path.get(1);
        	
        	// First we update scores for the source node
        	if (nodes.containsKey(source)) {
        		// Add up scores
    	    	for (Entry<String, Integer> langCounts : nodes.get(source).entrySet()) {
    	    		String language = langCounts.getKey();
    	    		Double cnt = (double) langCounts.getValue();
    	    		// Update score
    	    		if (!currentScores.containsKey(language))
    	    			currentScores.put(language, 0.0);
    	    		currentScores.put(language, currentScores.get(language) + (cnt / counter.get(language).getRight()));
    	    	}
    	    	
    	    	// Now process edges
    	    	if (edges.containsKey(source)) {
    	    		if (edges.get(source).containsKey(target)) {
    	    			// Add up scores
    	    	    	for (Entry<String, Integer> langCounts : edges.get(source).get(target).entrySet()) {
    	    	    		String language = langCounts.getKey();
    	    	    		Double cnt = (double) langCounts.getValue();
    	    	    		// Update score
    	    	    		if (!currentScores.containsKey(language))
    	    	    			currentScores.put(language, 0.0);
    	    	    		currentScores.put(language, currentScores.get(language) + (cnt / counter.get(language).getLeft()));
    	    	    	}
    	    		}
    	    	}
        	}
	        	    
    	    // Recurse with the trailing path
        	path.remove(0);
    	    return recPathMatching(path, currentScores, depth + 1, maxDepth);
        }
    }
	
	/**
	 * The actual path matching is done here
	 * @param trigrams
	 * @return
	 */
	private HashMap<String, Double> pathMatching(List<String> trigrams) {
        // Invoke our helper function
        return recPathMatching(trigrams, new HashMap<String, Double>(), 0, 1000);
    }
	
	/**
	 * Adds a document to the model
	 * @param doc
	 * @param language
	 */
	public void addDocument(String doc, String language) {
        // Minor pre-processing
        doc = doc.replaceAll(" +", " ").replaceAll("\\n", " ").toLowerCase();
        
        // Initialize counter for language
        if (!counter.containsKey(language))
            counter.put(language, new MutablePair<Integer, Integer>(0, 0));
        
        // Get all character trigrams and add them to the model
        String previousTrigram = null;
        for (int i = 2; i < doc.length(); i++) {
        	String trigram = "" + doc.charAt(i - 2) + doc.charAt(i - 1) + doc.charAt(i);
        	// Add node
        	addNode(trigram, language);
        	
        	// See if we have to add an edge
        	if (previousTrigram != null) {
        		// Add edge
        		addEdge(previousTrigram, trigram, language);
        	}
        	
        	previousTrigram = trigram;
        }
    }
	
	/**
	 * Classifies a message and returns the most probable language
	 * @param doc
	 * @return
	 */
	public String classify(String doc) {
        // Minor pre-processing
        doc = doc.replaceAll(" +", " ").toLowerCase();
        
        // Get all N-grams into a list
        List<String> trigrams = new LinkedList<String>();
        for (int i = 2; i < doc.length(); i++) {
        	String trigram = "" + doc.charAt(i - 2) + doc.charAt(i - 1) + doc.charAt(i);
        	trigrams.add(trigram);
        }
        
        // Get scores
        HashMap<String, Double> scores = pathMatching(trigrams);
        
        // Get the best score or return unknown
        Double bestScore = -1.0;
        String bestLang = "UNKNOWN";
        for (Entry<String, Double> score : scores.entrySet()) {
        	if (score.getValue() > bestScore && score.getValue() > threshold) {
        		bestScore = score.getValue();
        		bestLang = score.getKey();
        	}
        }
        
        // Return best scoring language
        return bestLang;
    }
	
	/**
	 * Loads a LIGA model
	 */
	public void loadModel() {
        try {
            // Stream JSON since loading it into memory is too memory-intensive
            JsonFactory jFactory = new JsonFactory();
        	JsonParser jParser = jFactory.createJsonParser(this.getClass()
					.getResourceAsStream("/model.liga"));
        	// Continue until we find the end object
        	while (!jParser.nextToken().equals(JsonToken.END_OBJECT)) {
        	    // Get the field name
        	    String fieldname = jParser.getCurrentName();
        	    
        	    if (fieldname != null) {
	        	    // We look for graph and counter field names
	        	    if (fieldname.equals("graph")) {
	        	        jParser.nextToken();
	        	        
	        	        while (!jParser.nextToken().equals(JsonToken.END_OBJECT)) {
	        	        	String trigram = jParser.getCurrentName();
	        	        	
	        	            // Next we again have a JSON object
	        	            while (!jParser.nextToken().equals(JsonToken.END_OBJECT)) {
	        	                // We can have 2 fields: nodes and edges
	        	                String nodeOrEdge = jParser.getCurrentName();
	        	                
	        	                if (nodeOrEdge.equals("nodes")) {
	        	                    jParser.nextToken();
	        	                    
	        	                    // Now we have an object containing languages and counts
	        	                    while (!jParser.nextToken().equals(JsonToken.END_OBJECT)) {
	        	                        String language = jParser.getCurrentName();
		    	                        jParser.nextToken();
		    	                        Integer count = jParser.getIntValue();
		    	                        
		    	                        // Add the trigram if required
		    	                        if (!nodes.containsKey(trigram)) {
		    	                        	nodes.put(trigram, new HashMap<String, Integer>());
		    	                        	edges.put(trigram, new HashMap<String, HashMap<String, Integer>>());
						        	    }
	        	                    	if (!nodes.get(trigram).containsValue(language))
	        	                    		nodes.get(trigram).put(language, 0);
	        	                        // Add the counts
	        	                    	nodes.get(trigram).put(language, count);
	        	                    }
	        	                }
	        	                else if (nodeOrEdge.equals("edges")) {
	        	                    jParser.nextToken();
	        	                    // Edges contain more trigrams
	        	                    while (!jParser.nextToken().equals(JsonToken.END_OBJECT)) {
	        	                        String target = jParser.getCurrentName();
		        	                    jParser.nextToken();
		        	                    // More JSON Objects, containing languages and counts
		        	                    while (!jParser.nextToken().equals(JsonToken.END_OBJECT)) {
		        	                        String language = jParser.getCurrentName();
			        	                    jParser.nextToken();
			        	                    Integer count = jParser.getIntValue();
			        	                    
			        	                    // Add the trigram if required
			    	                        if (!nodes.containsKey(trigram)) {
			    	                        	nodes.put(trigram, new HashMap<String, Integer>());
			    	                        	edges.put(trigram, new HashMap<String, HashMap<String, Integer>>());
							        	    }
		        	                    	if (!nodes.get(trigram).containsValue(language))
		        	                    		nodes.get(trigram).put(language, 0);
		        	                    	
		        	                    	// Add the target if required
			    	                        if (!nodes.containsKey(target)) {
			    	                        	nodes.put(target, new HashMap<String, Integer>());
			    	                        	edges.put(target, new HashMap<String, HashMap<String, Integer>>());
							        	    }
		        	                    	if (!nodes.get(target).containsValue(language))
		        	                    		nodes.get(target).put(language, 0);
		        	                    	
		        	                    	// Add the edge
		        	                    	if (!edges.containsKey(trigram))
		        	                    		edges.put(trigram, new HashMap<String, HashMap<String, Integer>>());
		        	                    	if (!edges.get(trigram).containsKey(target))
		        	                    		edges.get(trigram).put(target, new HashMap<String, Integer>());
		        	                    	if (!edges.get(trigram).get(target).containsKey(language))
		        	                    		edges.get(trigram).get(target).put(language, 0);
		        	                    	edges.get(trigram).get(target).put(language, count);
		        	                    }
	        	                    }
	        	                }
	        	            }
	        	        }
	        	    }
	        	    else if (fieldname.equals("counter")) {
	        	        jParser.nextToken();
	        	        // We have language that contain objects which are counters for nodes and edges
	        	        while (!jParser.nextToken().equals(JsonToken.END_OBJECT)) {
	        	            String language = jParser.getCurrentName();
	        	            jParser.nextToken();
	        	            
	        	            // Get the nodes/edges object
	        	            while (!jParser.nextToken().equals(JsonToken.END_OBJECT)) {
	        	            	String nodeEdge = jParser.getCurrentName();
	        	            	
	        	            	// Check if its nodes or edges
	        	            	if (nodeEdge.equals("nodes")) {
	        	            	    jParser.nextToken();
	        	            	    Integer count = jParser.getIntValue();
	        	            	    
	        	            	    // Add the language if it didn't exist yet
	        	            	    if (!counter.containsKey(language))
	        	            	    	counter.put(language, new MutablePair<Integer, Integer>(0, 0));
	        	            	    
	        	            	    // Add to the mapping
	        	            	    counter.get(language).setLeft(count);
	        	            	    
	        	            	}
	        	            	else if (nodeEdge.equals("edges")) {
	        	            	    jParser.nextToken();
	        	            	    Integer count = jParser.getIntValue();
	        	            	    
	        	            	    // Add the language if it didn't exist yet
	        	            	    if (!counter.containsKey(language))
	        	            	    	counter.put(language, new MutablePair<Integer, Integer>(0, 0));
	
	        	            	    // Add it
	        	            	    counter.get(language).setRight(count);
	        	            	}
	        	            }
	        	        }
	        	    }
        	    }
        	}
            jParser.close();
        }
        catch (Exception e) {
        	System.out.println("Unable to load model");
			e.printStackTrace();
        }
	}
	
	/**
	 * Stores the model to persistency
	 */
	
}

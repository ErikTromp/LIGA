<?php
/**
 * graph.php - Description
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
 * graph - Description
 *
 *
 * @copyright  
 * @author     Erik Tromp
 * @version    
 * @since      
 */
class Graph {
    
    public static $graph = array();
    public static $counters = array();

    public static function addNode($name, $language) {
        // Check if node exists
        if (isset(self::$graph[$name])) {
            // Check if the the node already exists for the given language
            if (isset(self::$graph[$name]['Occurrences'][$language])) {
                // Increase count by 1
                self::$graph[$name]['Occurrences'][$language] += 1;
            }
            else {
                // Mark language to have this node
                self::$graph[$name]['Occurrences'][$language] = 1;
            }
        }
        else {
            // Create node
            self::$graph[$name] = array();
            // Start count for this language
            self::$graph[$name]['Occurrences'] = array($language => 1);
            // Initialize edges
            self::$graph[$name]['Edges'] = array();
        }
    }

    public static function addEdge($n1, $n2, $language) {
        // Check if edge exists
        if (isset(self::$graph[$n1]['Edges'][$n2])) {
            // Check if this language occurs
            if (isset(self::$graph[$n1]['Edges'][$n2][$language])) {
                // Increase edge weight
                self::$graph[$n1]['Edges'][$n2][$language] += 1;
            }
            else {
                // Initialize edge language weight
                self::$graph[$n1]['Edges'][$n2][$language] = 1;
            }
        }
        else {
            // Initialize edge
            self::$graph[$n1]['Edges'][$n2] = array();
            // Set weight for this language
            self::$graph[$n1]['Edges'][$n2][$language] = 1;
        }
    }

    public static function pathMatching($graph, $path, $scores, $depth, $maxDepth) {
        if (count($path) == 0 || $depth > $maxDepth) {
            // Path is empty, return scores
            return $scores;
        }
        else {
            // Path contains only 1 node, no need to check for edges
            if (isset($graph[$path[0]])) {
                // Node exists in the graph, account for all languages
                foreach ($graph[$path[0]]['Occurrences'] as $language => $occurrences) {
                    // Check if the language already has a score
                    if (!isset($scores[$language]))
                        $scores[$language] = 0;
                    // Add _relative_ occurrence count to the language's score
                    $scores[$language] += $occurrences / self::$counters[$language]['nodes'];
                }
            }

            // Check if the path contains more nodes
            if (count($path) > 1) {
                // Check if the edge between the first two nodes in our path
                // also exists in the graph
                if (isset($graph[$path[0]]['Edges'][$path[1]])) {
                    // Edge exists, account for the weights of the edges
                    foreach ($graph[$path[0]]['Edges'][$path[1]] as $language => $weights) {
                        // Check if the language already exists
                        if (!isset($scores[$language]))
                            $scores[$language] = 0;
                        // Add _relative_ weight count to the language's score
                        $scores[$language] += $weights / self::$counters[$language]['edges'];
                    }
                }

                // Recurse down the path
                return self::pathMatching($graph, array_slice($path, 1), $scores, $depth + 1, $maxDepth);
            }
            else {
                // No need to recurse further
                return $scores;
            }
        }
    }

}
?>

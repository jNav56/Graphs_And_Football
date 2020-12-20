/*  Student information for assignment:
 *
 *  On my honor, Juan Nava, this programming assignment is my own work
 *  and I have not provided this code to any other student.
 *
 *  UTEID:jcn842
 *  email address:nava.juan1012@gmail.com
 *  Grader name:Ethan
 *  Number of slip days I am using:0
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.TreeSet;

/**
 * Models a directed graph. Edges can be weighted.
 * To model an undirected graph add an edge from vertex A
 * to vertex B and another edge from vertex B to vertex A.
 * To model an unweighted graph set all edge costs to the
 * same value, typically 1 or 0.
 * @author scottm, based on the Graph class from
 * Weiss Algorithms and Data Structures.
 */
public class Graph {

    // used to indicate a vertex has not been visited and
    // that no path exists between current start vertex.
    private static final double INFINITY = Double.MAX_VALUE;

    // The vertices in the graph. Every vertex must have a unique String label.
    private Map<String, Vertex> vertices;

    // Used to store the path between the two most distant vertices.
    // In other words the vertices with the LONGEST shortest
    // path of all the shortest paths in the graph.
    private Path longest;

    // flag to ensure all paths have been found
    // Must set to true when the allPaths method is called.
    private boolean allPathsFound;

    private String currentStartVertexName;

    /**
     * Create an empty graph.
     */
    public Graph() {
        vertices = new HashMap<>();
    }

    /**
     * Add an edge to this graph.
     * If an edge from source to dest already exists
     * then this edge replaces the old edge.
     * Creates vertices for source and / or dest if
     * they are not already present.
     * <br>pre: source != null, dest != null, cost > 0
     * @param source cannot be null
     * @param dest cannot be null
     * @param cost must be > 0
     * @return return true if an edge existed from source to dest
     * prior to this method call, false otherwise.
     */
    public boolean addEdge(String source, String dest, double cost) {
        if (source == null || dest == null) {
            throw new IllegalArgumentException("Violation of precondition. " +
                            "Vertex names may not be null.");
        }
        if (cost <= 0) {
            throw new IllegalArgumentException("Violation of precondition. " +
                            "edge costs must be > 0." + cost);
        }
        Vertex s = getVertex(source);
        Vertex d = getVertex(dest);
        return s.addEdge(d, cost);
    }

    /**
     * Returns true if an edge (not a path, just an edge)
     * exists from the source vertex to the destination
     * vertex.
     * @param source the start vertex
     * @param dest the ending vertex (or destination)
     * @return true if an edge exists from source to dest, false otherwise
     */
    public boolean edgeExists(String source, String dest) {
        boolean result = false;
        Vertex src = vertices.get(source);
        if (src != null) {
            result = src.getEdgeWithName(dest) == null ? false : true;
        }
        return result;
    }

    /**
     * Add a vertex with the given name to this Graph.
     * The new vertex has no edges initially.
     * <br>pre: name != null
     * @param name The name of the new vertex.
     */
    public void addVertex(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Violation of precondition. " +
                            "Vertex name may not be null.");
        }
        getVertex(name);
    }

    /**
     * Find all unweighted shortest paths from the Vertex with startName
     * to all other vertices in this Graph.
     * <br>
     * After this method is called,
     * call the printPath(String) method to get the path from startName
     * to any other vertex in this Graph.
     *
     * <br>pre: startName != null, containsVertex(startName) == true
     *
     * @param startName The starting vertex. This method will find all the
     * unweighted shortest paths from the give vertex to all other vertices
     * in the graph.
     */
    public void findUnweightedShortestPath(String startName) {
        // CS314 STUDENTS - I RECOMMEND YOU LEAVE THIS METHOD AS IS.
        // DO NOT ALTER IT. Mike
        handleFUWSPPrecons(startName);

        Queue<Vertex> q = prepForFindingUnweightedShortestPaths(startName);

        while(!q.isEmpty()) {
            Vertex current = q.remove();
            for(Edge e : current.adjacent) {
                Vertex neighberNode = e.dest;
                // Is this the first time I have seen this vertex?
                if(neighberNode.weightedCostFromStartVertex == INFINITY) {
                    // In an unweighted graph we treat
                    // the weighted cost and number of edges from start
                    // as the same.
                    neighberNode.weightedCostFromStartVertex = current.weightedCostFromStartVertex + 1;
                    neighberNode.numEdgesFromStartVertex = current.numEdgesFromStartVertex + 1;
                    neighberNode.prev = current;
                    q.add(neighberNode);
                }
            }
        }
    }


    // Set up queue and instance variables to find
    // unweighted shortest path algorithm from startName vertex
    // to every other Vertex we can reach.
    private Queue<Vertex> prepForFindingUnweightedShortestPaths(String startName) {
        currentStartVertexName = startName;
        clearAll();
        Vertex start = vertices.get(startName);
        start.weightedCostFromStartVertex = 0;
        start.numEdgesFromStartVertex = 0;
        Queue<Vertex> result= new LinkedList<>();
        result.add(start);
        return result;

    }

    // check preconditions for findUnweightedShortestPath
    private void handleFUWSPPrecons(String startName) {
        if (startName == null) {
            throw new IllegalArgumentException("Violation of precondition. " +
                            "Vertex name may not be null.");
        }
        if (!containsVertex(startName)) {
            throw new NoSuchElementException("No Verex named " + startName + " exists in this Graph");
        }

    }

    /**
     * Find all weighted shortest paths from the Vertex startName
     * to all other vertices in this Graph using Dijkstra's algorithm.
     *
     * After this method is called, call
     * the printPath(String) method to get the path from startNode
     * to any other vertex in this Graph.
     *
     * <br>pre: startName != null, containsVertex(startName) == true
     * @param startName The starting vertex. This method will find all the
     * weighted shortest paths from the given vertex to all other vertices
     * in the graph.
     */
    public void dijkstra(String startName) {
        if (startName == null) {
            throw new IllegalArgumentException("Violation of precondition. " +
                            "Vertex name may not be null.");
        }
        if (!containsVertex(startName)) {
            throw new NoSuchElementException("No Vertex named " + startName + " exists in this Graph");
        }

        currentStartVertexName = startName;

        // CS314 STUDENTS - Complete this method
        // Use a PriorityQueue of Path objects (not Vertex objects)
        // It is acceptable to use the built in Java PriorityQueue class.

        // Resets the weighted cost, number of edges to start vertex, and the node it comes 
        // after for every vertex
        for(String name: vertices.keySet()) {
        	vertices.get(name).reset();
        }
        // Change the weight of the start vertex to 0
        vertices.get(currentStartVertexName).weightedCostFromStartVertex = 0;
        
        // Create queue for paths
        PriorityQueue<Path> que = new PriorityQueue<Path>();
        
        // Add the starting vertex to the queue with it's default path
        que.add(new Path(vertices.get(currentStartVertexName), 0));
        
        // Check every unvisited vertex/path in the queue
        while(!que.isEmpty()) {
        	// Take out first path in queue and its vertex
        	Path cur = que .remove();
        	Vertex v = cur.dest;
        	
        	// If the vertex has never been visited, enter and evaluate its neighbors
        	// Variable scratch indicates if vertex has been visited; 0 means unvisited
        	if(v.scratch == 0) {
        		// Change scratch to 1 to indicate it has been visited
	        	v.scratch = 1;
	        	
	        	// Get list of this current vertex's neighbors
	        	List<Edge> adj = v.adjacent;
	        	
	        	// Loop through each of the vertex's edges
	        	for(Edge e: adj) {
	        		// Get the edge's destination Vertex and it's weight 
	        		Vertex dest = e.dest;
	        		double weight = e.cost + v.weightedCostFromStartVertex;
		        	
	        		// If the new possible weight of the destination vertex is less than its
	        		// current weight, then update the destination vertex
	        		if(weight < dest.weightedCostFromStartVertex) {
	        			
	        			// Update the number of edges in the path and weight from the starting vertex
	        			dest.numEdgesFromStartVertex = v.numEdgesFromStartVertex + 1;
	        			dest.weightedCostFromStartVertex = weight;
	        			// Update the vertex the destination vertex is referenced by
	        			dest.prev = v;
	        			
	        			// Add path of destination vertex to queue
	        			que.add(new Path(dest, dest.weightedCostFromStartVertex));
	        		}
	        	}
	        	
        	}
        }
    }
    
    /**
     * Find all shortest paths between all pairs of vertices in this Graph
     *
     * <br><br>After this method is done, each vertex shall store:
     * <br>1. The total number of vertices (not including itself)
     * it is connect to.
     * <br>2. The sum of the number of edges in the all
     * the shortest paths from the vertex to the vertices it
     * connects to. (Unweighted path lengths.)
     * <br>3. The sum of the weighted path lengths in the all
     * the shortest paths from the vertex to the vertices it
     * connects to.
     * <br><br>Note, if the parameter weighted is false, 2 and 3 shall
     * equal each other.
     *
     * <br><br>This method also determines the shortest path with the
     * greatest distance.
     * The two most distant vertices in the Graph are those with the
     * Longest shortest path between them. The longest path is based on
     * the number of edges in the path if weighted == false and the unweighted
     * shortest path algorithm is being used. The longest path is based on the
     * highest cost shortest path if weighted == true and Dijkstra is used.
     *
     * <br><br>if weighted == true, use dijkstra, otherwise, use unweighted
     * shortest path
     *
     * <br><br>
     * After this method is called the getAllPaths, getDiamter, and
     * get longest path methods may be called.
     *
     * @param weighted If weighted == true use dijkstra's algorithm
     * otherwise use the unweighted shortest path algorithm. (Ignore any
     * weights for edges. All edge weights considered to be 1.)
     */
    public void findAllPaths(boolean weighted) {
        allPathsFound = true;

        // CS314 Students - complete this method
        // Instantiate longest variable
        longest = new Path();
        
        // Resets the number of vertices a vertex is connected to, the sum of all 
        // unweighted paths, and sum of all weighted paths for each vertex in the graph  
        for(String name: vertices.keySet()) {
        	vertices.get(name).clearPathInfo();
        }
        
        // Loop through every vertex in the graph and get i
        for(String name: vertices.keySet()) {
        	Vertex v = vertices.get(name);
        	
        	// weighted variable determines which method between dijkstra and 
        	// findUnweightedShortestPath will be called
        	if(weighted) {
        		this.dijkstra(v.name);
        	} else {
        		this.findUnweightedShortestPath(v.name);
        	}
        	
        	// Get path to each vertex from current vertex
        	for(String dest: vertices.keySet()) {
        		Path temp = this.getPath(dest);
        		
        		// If path from current vertex to destination vertex is longer than
        		// longest, then update longest
        		if(temp.getNumEdgesInPath() > longest.getNumEdgesInPath()) {
        			longest = temp;
        		}
        		
        		// If path from current vertex to destination vertex exists,
        		// then increment number of vertices current vertex is connected to
        		if(temp.getNumEdgesInPath() > 0) {
        			v.numVertexConnected += 1;
        		}
        		
        		// Get weighted and unweighted values from current vertex to destination vertex
        		double weight = getWeightedCostFromStart(dest);
        		double unweighted = getNumEdgesFromStart(dest);
        		
        		// Update sum of weighted and unweighted values if path exists
        		if(weight != -1) {
            		v.totalWeightedPathLength += weight;
        		}
        		if(unweighted != -1) {
        			v.totalUnweightedPathLength += unweighted;
        		}
        	}
        }
    }


    // helper to get path from current start vertex to dest vertex
    private Path getPath(String dest) {
        Path result = new Path();
        Vertex end = vertices.get(dest);
        result.weightedCostOfPath = end.weightedCostFromStartVertex;
        if (end.weightedCostFromStartVertex != INFINITY) {
            getPath(result, end);
        }
        return result;
    }

    // helper to get path
    private void getPath(Path result, Vertex end) {
        if (end.prev != null) {
            getPath(result, end.prev);
        }
        result.add(end);
    }

    /**
     * Get the number of edges of the shortest path
     * from the current start vertex to the given destination vertex.
     * If there is no path from the current start vertex to the given
     * destination vertex, returns -1.
     * <br><br>
     * pre: findUnweightedShortestPath or dijkstra called.
     * <tt>containsVertex(dest) == true</tt>
     * @param dest the destination vertex.
     * @return the number of edges from the current start vertex
     * to the destination vertex. returns -1 if no path
     * exists.
     */
    public int getNumEdgesFromStart(String dest) {
        if (currentStartVertexName == null) {
            throw new IllegalStateException("method " +
                            "findUnweigthedShortesPath or dijkstra must be " +
                            "called before calling this method.");
        } else if(!containsVertex(dest)) {
            throw new NoSuchElementException("No Vertex named "
                            + dest + " exists in this Graph");
        }

        int result = vertices.get(dest).numEdgesFromStartVertex;
        return (result == Integer.MAX_VALUE) ? -1 : result;
    }

    /**
     * Get the total weighted cost of the shortest path
     * from the current start
     * vertex to the given destination vertex. If there is
     * no path from the current start vertex to the given
     * destination vertex -1 is returned.
     * <br><br>
     * pre: findUnweightedShortestPath or dijkstra called.
     * <tt>containsVertex(dest) == true</tt>
     * @param dest
     * @return the total cost of the shortest path
     * from the current start vertex
     * to the destination vertex. returns -1 if no path
     * exists.
     */
    public double getWeightedCostFromStart(String dest) {
        if (currentStartVertexName == null) {
            throw new IllegalStateException("method " +
                            "findUnweigthedShortesPath or dijkstra must be " +
                            "called before calling this method.");
        } else if(!containsVertex(dest)) {
            throw new NoSuchElementException("No Vertex named "
                            + dest + " exists in this Graph");
        }

        double result = vertices.get(dest).weightedCostFromStartVertex;
        if (result == INFINITY) {
            result = -1;
        }
        return result;
    }


    /**
     * Get all path statistics for all vertices in this graph that are
     * connected to one or more other vertices.
     * <br>pre: findAllPaths has been called.
     * @return A TreeSet with AllPathsInfo for the vertices in this Graph.
     */
    public TreeSet<AllPathsInfo> getAllPaths() {
        if (!allPathsFound) {
            throw new IllegalStateException("The method findAllPaths must be called before calling this method. ");
        }

        TreeSet<AllPathsInfo> result = new TreeSet<>();
        for (Vertex v : vertices.values()) {
            if (v.numVertexConnected > 0) {
                AllPathsInfo temp = new AllPathsInfo(v.name, v.numVertexConnected, v.totalWeightedPathLength );
                boolean added = result.add(temp);
                assert added : "Did not add path info for " + v +". Why not?";
            }
        }
        return result;
    }


    /**
     * Check if a vertex with the given name is present in this graph.
     * <br>pre: name != null
     * @param name The name of the vertex to check.
     * @return true if a vertex with name is present in this Graph, false otherwise.
     */
    public boolean containsVertex(String name) {
        return vertices.containsKey(name);
    }


    /**
     * Return the name of the current start vertex.
     * <br><br>
     * pre: findUnweightedShortestPath or dijkstra called.
     * @return the label of the current starting vertex.
     */
    public String getCurrentStartVertex() {
        if (currentStartVertexName == null) {
            throw new IllegalStateException("method findUnweigthedShortesPath or dijkstra must be " +
                            "called before calling this method.");
        }
        return this.currentStartVertexName;
    }


    /**
     * Alternative to printPath that returns an List containing the path from the
     * current start vertex to the vertex named destName. If no path exists
     * an empty List is returned.
     * <br>pre: destName != null, containsVertex(destName) == true, the startNode has
     * been set by calling findUnweigthedShortesPath or dijkstra.
     * @param destName The destination vertex
     * @return A list with the path from the current start vertex to the vertex with destName.
     * start vertex will be at index 0 and destName will be at index list.size() - 1 unless there
     * is no path from the start vertex to destName in which case an emtpy list is returned.
     */
    public List<String> findPath(String destName) {
        if (currentStartVertexName == null) {
            throw new IllegalStateException("method findUnweigthedShortesPath or dijkstra must be " +
                            "called before calling this method.");
        }
        if (destName == null) {
            throw new IllegalArgumentException("Violation of precondition. " +
                            "Vertex name may not be null.");
        }
        if (!containsVertex(destName)) {
            throw new NoSuchElementException("No Vertex named " + destName + " exists in this Graph");
        }

        List<String> result = new LinkedList<>();
        Vertex end = vertices.get(destName);

        if (end.weightedCostFromStartVertex != INFINITY) {
            findPath(result, end);
        }
        return result;
    }


    // helper method to find path.
    private void findPath(List<String> result, Vertex end) {
        if (end.prev != null) {
            findPath(result, end.prev);
        }
        result.add(end.name);
    }

    /**
     * Print the path from the current start vertex to the vertex with name destName
     * <br>pre: destName != null, containsVertex(destName) == true, the startNode has
     * been set by calling findUnweigthedShortesPath or dijkstra.
     * @param destName The destination vertex.
     */
    public void printPath(String destName) {
        Vertex end = vertices.get(destName);
        if (end == null) {
            throw new NoSuchElementException("No Node named " + destName + " exists in this Graph");
        } else if(end.weightedCostFromStartVertex == INFINITY) {
            System.out.println("no path to " + destName);
        } else {
            System.out.println("Cost is " + end.weightedCostFromStartVertex);
            printPath(end);
            System.out.println();
        }
    }

    // helper to print path
    private void printPath(Vertex dest) {
        if (dest.prev != null) {
            printPath(dest.prev);
        }
        System.out.println(dest.name);
    }

    /**
     * Return the number of edges in the longest shortest path
     * in this Graph.
     * <br>pre: findAllPaths has been called.
     * @return the diameter of this graph in terms of number of edges.
     */
    public int getDiameter() {
        if (!allPathsFound) {
            throw new IllegalStateException("The method findAllPaths must "
                            + "be called before calling this method. ");
        }
        return longest.getNumEdgesInPath();
    }

    /**
     * Return the cost of the longest shortest path in this
     * Graph. Note, the cost of the longest shortest path
     * may differ than the number of edges in the path. (returned by getDiameter)
     * <br>pre: findAllPaths has been called.
     * @return the diameter of this graph.
     */
    public double costOfLongestShortestPath() {
        if (!allPathsFound) {
            throw new IllegalStateException("The method findAllPaths must be "
                            + "called before calling this method. ");
        }
        return longest.weightedCostOfPath;
    }

    /**
     * Return a path equal to the diameter of this graph.
     * <br>pre: findAllPaths has been called.
     * @return the names of the vertices in a path equal to the diamter in this graph. If there
     * is more than one longest path one is arbitrarily chosen.
     */
    public String getLongestPath() {
        return longest.toString();
    }


    // helper. If name not present create new vertex.
    // return vertex with given name
    private Vertex getVertex(String name) {
        Vertex v = vertices.get(name);
        if (v == null) {
            v = new Vertex(name);
            vertices.put(name, v);
        }
        return v;
    }


    // reset all vertices to set new start vertex and find all paths
    // from new start
    private void clearAll() {
        for (Vertex v : vertices.values()) {
            v.reset();
        }
    }

    /**
     * Prints out the name of all vertices in the Graph.
     * Not expected to use on A12
     */
    public void showAll() {
        for (Vertex v : vertices.values()) {
            System.out.println(v);
        }
    }


    /**
     * Get the name of all vertices the Vertex with name
     * is connected to.
     * Not expected to use this on assignment 12.
     * @param name The name of the start Vertex
     * @return a List with the names of the vertices
     * connect to the Vertex name.
     */
    public List<String> getConnections(String name) {
        List<String> result = new ArrayList<>();
        Vertex v = vertices.get(name);
        if (v != null) {
            for(Edge e : v.adjacent) {
                result.add(e.dest.name);
            }
        }
        return result;
    }



    // models edge between two vertices
    private static class Edge {
        private Vertex dest;
        private double cost;

        public Edge(Vertex d, double c) {
            dest = d;
            cost = c;
        }

        public String toString() {
            return "(" + dest.name + " " + cost + ")";
        }
    }

    // model a vertex. uses list of edges from this vertex to others
    private static class Vertex {

        private String name;
        private List<Edge> adjacent;

        // Number of other vertices this vertex is connected to.
        // This variable should be updated in the findAllPaths method.
        private int numVertexConnected;

        // Sum of all unweighted shortest paths from this vertex to other vertices.
        // This variable should be updated in the findAllPaths method.
        private double totalUnweightedPathLength;

        // Sum of all weighted shortest paths from this vertex to other vertices.
        // This variable should be updated in the findAllPaths method.
        private double totalWeightedPathLength;

        // for graph algorithms
        private double weightedCostFromStartVertex;
        private int numEdgesFromStartVertex;
        private Vertex prev;
        private int scratch;

        public Vertex(String n) {
            name = n;
            adjacent = new LinkedList<>();
            reset();
        }

        // called when looking for new shortest path info
        public void reset() {
            weightedCostFromStartVertex = INFINITY;
            numEdgesFromStartVertex = Integer.MAX_VALUE;
            prev = null;
            scratch = 0;
        }

        // zero out the sum of paths
        public void clearPathInfo() {
            numVertexConnected = 0;
            totalUnweightedPathLength = 0;
            totalWeightedPathLength = 0;
        }

        public String toString() {
            return "{" + name + ", connected to: " + numVertexConnected + "adjacent: " + adjacent + "}";
        }

        // Add an edge from this vertex to dest.
        // If an edge already exists replace the previous
        // cost with the new cost.
        // Return true if an edge existed from this
        // vertex to dest prior to the method call.
        public boolean addEdge(Vertex dest, double cost) {
            Edge e = getEdgeWithName(dest.name);
            if (e == null) {
                adjacent.add(new Edge(dest, cost));
            } else {
                e.cost = cost;
            }
            return e != null;
        }

        public boolean equals(Object other) {
            boolean result = other instanceof Vertex;
            if (result){
                result = name.equals(((Vertex) other).name);
            }
            return result;
        }

        public int hashCode() {
            return name.hashCode();
        }

        // Gets the edge from this Vertex to
        // dest if one exists. If one does not
        // exist return null.
        public Edge getEdgeWithName(String dest) {
            for (Edge e : adjacent) {
                if (e.dest.name.equals(dest)) {
                    return e;
                }
            }
            return null;
        }
    } // end of the Vertex class


    // Models a path between vertices.
    // Best not to try and store all paths for any but small graphs.
    // Used in the Dijkstra method and to track the longest shortest path
    // in a graph when get all paths is called.
    // We store Path objects in the PriorityQueue instead of
    // Vertex objects because a given Vertex may have several paths
    // to it and altering the Vertex may disrupt the PriorityQueue that
    // stores them.
    private static class Path implements Comparable<Path> {

        private List<Vertex> verticesInPath; // not necessary for temporary paths in Dijkstra's
        private double weightedCostOfPath;
        private Vertex dest;

        public Path() {
            verticesInPath = new LinkedList<>();
        }

        public Path(Vertex v, double c) {
            dest = v;
            weightedCostOfPath = c;
            verticesInPath = new LinkedList<Vertex>();
        }

        public void add(Vertex v) {
            verticesInPath.add(v);
        }

        // return number of vertices in this Path
        public int getNumVerticesInPath() {
            return verticesInPath.size();
        }

        // return the number of edges in this Path
        public int getNumEdgesInPath() {
            return verticesInPath.size() - 1;
        }

        // return weighted cost of path
        public double weightedCost() {
            return weightedCostOfPath;
        }

        public String toString() {
            StringBuilder result = new StringBuilder();
            result.append("[");
            for (Vertex v : verticesInPath) {
                result.append(v.name);
                result.append(", ");
            }
            if (verticesInPath.size() > 0) {
                result.delete(result.length() - 2, result.length());
            }
            result.append("]");
            if (verticesInPath.size() > 0) {
                result.append(" cost: ");
                result.append(weightedCostOfPath);
            }
            return result.toString();
        }

        public int compareTo(Path other) {
            return (weightedCostOfPath < other.weightedCostOfPath)
                            ? -1 : (weightedCostOfPath > other.weightedCostOfPath) ? 1 :0;
        }
    }
}
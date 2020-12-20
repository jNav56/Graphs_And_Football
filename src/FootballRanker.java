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


import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

/**
 * Rank college football teams for a given season based on wins
 * and margin of victory. Stores a directed, weighted grapg.
 * Teams stored in vertices. An edge exists between a team that
 * beat another team. Weight of edge is proportional to margin of
 * victory. Calculate ranks of teams based on centrality
 * in graph three ways:<br>
 * 1. average unweighted shortest path length<br>
 * 2. average weighted shortest path length<br>
 * 3. average weighted shortest path length adjusted
 * by team's win percentage.
 * @author scottm
 *
 */

public class FootballRanker {

    // only include teams in final rankings with this many wins
    private static final int MIN_WINS = 7;

    // only rank teams with a significant number of transitive wins.
    private static final int MIN_TRANSITIVE_WINS = 100;

    // for output
    private static final int PADDING = 20;

    // to adjust output
    private static final DecimalFormat ourFormatter = new DecimalFormat("0.0000");

    // used to adjust scores to number scores winning team won by,
    private static final int TD_PLUS = 8;

    // fields in games results data files
    private static final int HOME_TEAM = 1;
    private static final int HOME_SCORE = 2;
    private static final int AWAY_TEAM = 3;
    private static final int AWAY_SCORE = 4;

    private Graph teamsAndResults;
    private Map<String, FootballRecord> records;
    private List<String> actualRanks;


    /**
     * Create a new FootballRanker based on the given files.
     * Format of file is:
     * <br>Date,Home Team Name,Home Team Points,Away Team Name,Away Team Points,Location if neutral.
     * <br> results that are ties are ignored
     * <br>pre: gameResults != null, actualRanks != null, files with the given names
     * are in the local directory.
     * @param gameResults The name of the file with the results from games.
     * @param actualRanks The name of the file with the final end of season rankings.
     */
    public FootballRanker(String gameResults, String actualRanks) {
        if(gameResults == null || actualRanks == null)
            throw new IllegalArgumentException("Violation of precondition. " +
                    "File names may not be null.");

        records = new HashMap<String, FootballRecord>();
        teamsAndResults = buildGraph(gameResults);
        this.actualRanks = getActual(actualRanks);


    }


    /**
     * Calculate top teams based solely on average path length of unweighted shortest paths.
     * @param showResults if this is true output results to standard output
     * @return the root mean square error of the top 25 teams based on this calculation
     * rounded to 1 decimal place.
     */
    public double doUnweighted(boolean showResults) {
        if(showResults)
            System.out.print("\n\n ***** RESULTS BASED ON UNWEIGHTED WINS *****");
        teamsAndResults.findAllPaths(false);
        TreeSet<AllPathsInfo> paths = teamsAndResults.getAllPaths();
        paths = rankByScore(paths);
        if(showResults)
            showResults(paths);
        return printRootMeanSquareError(actualRanks, paths, showResults);
    }


    /**
     * Calculate top teams based on average path length of weighted shortest paths.
     * This takes into account how easily teams won their games.
     * @param showResults if this is true output results to standard output
     * @return the root mean square error of the top 25 teams based on this calculation
     * rounded to 1 decimal place.
     */
    public double doWeighted(boolean showResults) {
        if(showResults)
            System.out.print("\n\n ***** RESULTS BASED ON WEIGHTED WINS *****");
        teamsAndResults.findAllPaths(true);
        TreeSet<AllPathsInfo> paths = teamsAndResults.getAllPaths();
        paths = rankByScore(paths);
        if(showResults)
            showResults(paths);
        return printRootMeanSquareError(actualRanks, paths, showResults);
    }

    /**
     * Calculate top teams based on average path length of weighted shortest paths
     * with an adjustment for the teams winning percentage.
     * @param showResults if this is true output results to standard output
     * @return the root mean square error of the top 25 teams based on this calculation
     * rounded to 1 decimal place.
     */
    public double doWeightedAndWinPercentAdjusted(boolean showResults) {
        if(showResults)
            System.out.print("\n\n ***** RESULTS BASED ON WEIGHTED WINS ADJUSTED BY WIN PERCENTAGE *****");
        teamsAndResults.findAllPaths(true);
        TreeSet<AllPathsInfo> paths = teamsAndResults.getAllPaths();
        paths = rankByScoreAdjustWinPercentage(paths);
        if(showResults)
            showResults(paths);
        return printRootMeanSquareError(actualRanks, paths, showResults);
    }

    // show results of calculations
    private void showResults(Set<AllPathsInfo> paths) {
        System.out.println("\n\n***** TOP TEAMS *****");
        int rank = 1;
        for(AllPathsInfo team : paths)
                System.out.println("predicted rank: " + (rank++) + ", " + team);
    }


    /*
     * Calculate difference between predicted results based on graph and actual results of
     * poll data. return the root mean square error of difference between acutal poll results and
     * predicted results from graph.
     * If showResults is true print results to standard output.
     */
    private double printRootMeanSquareError(List<String> humanRanks,
                    TreeSet<AllPathsInfo> paths, boolean showResults) {
        if(showResults)
            System.out.println("\n\n ***** PREDICTED VS. ACTUAL RESULTS *****");

        // CS314 STUDENTS - COMPLETE THIS METHOD
        // Keeps track of the sum of all the differences of the AP and predicted ranks squared
        double sumSquare = 0;
	    
        // Keeps track of AP ranks
        int index = 1;
        
        // Traverses through the teams from the AP ranks 
        for(String str: humanRanks) {
			        	
        	int predictedRank = getPredictedRank(paths, str);
        	
        	// Print results of AP and predicted ranks depending on showResults
        	if(showResults) {
        		System.out.printf("%-" + PADDING + "s - ", str);
        		System.out.print("actual rank: " + index);
        		System.out.print(" predicted rank: " + predictedRank);
        		System.out.println();
        	}
        	
        	// Update sumSquare with difference of AP and predicted rank squared
        	sumSquare += Math.pow(index - predictedRank, 2);
        	index++;
        }
        
        // Get square root of the sum of the differences squared divided by the number of teams
        double squareRoot = Math.sqrt(sumSquare / (index - 1));
        
        // Print square root error value depending on showResults
        if(showResults){
        	System.out.printf("Root Mean Square Error: " + ourFormatter.format(squareRoot));
        	System.out.println();
        }
        
        // Return the root mean square error to the nearest tenth
        return Math.round(squareRoot * 10) / 10.0;
    }

    /**
     *  Returns the index (predicted rank) of the team in the TreeSet containing paths info
     *  @param paths is the TreeSet containing all the paths and their info
     *  @param str is the team name whose rank we are searching for 
     */
    private int getPredictedRank(TreeSet<AllPathsInfo> paths, String str) {
    	// Is the position or rank in the predicted set
    	int index = 0;
    	// Determines whether we have found the team name in the contained set
    	boolean found = false;
    	
    	Iterator<AllPathsInfo> it = paths.iterator();
    	
    	// Traverse through TreeSet checking each path and respective vertex
    	while(it.hasNext() && !found) {
    		found = str.equals(it.next().getName());
    		index++;
    	}
		return index;
	}


	public void processRequests() {
        Scanner in = new Scanner(System.in);
        do {
            System.out.print("\nEnter starting team: ");
            String start = in.nextLine().trim();
            if(teamsAndResults.containsVertex(start)) {
                teamsAndResults.findUnweightedShortestPath(start);
                do {
                    System.out.println("Finding paths from " + start);
                    System.out.print("\nEnter ending team: ");
                    String end = in.nextLine().trim();
                    if(teamsAndResults.containsVertex(end)) {
                        teamsAndResults.printPath(end);
                    }
                    else
                        System.out.println("\n" + end + " is not a team");
                } while(another(in, "Do you want to find another path from " + start + "?"));
            }
            else
                System.out.println("\n" + start + " is not a team");
        } while(another(in, "Do you want to look for more paths between teams?"));
    }

    /**
     * Show the longest path in the graph based on games played.
     */
    public void showLongest() {
        System.out.println();
        System.out.println(teamsAndResults.getLongestPath());
    }



    private boolean another(Scanner in, String prompt) {
        System.out.println(prompt);
        System.out.print("Enter y for yes, anything else for no: ");
        String answer = in.nextLine().trim().toLowerCase();
        System.out.println();
        return answer != null && answer.length() > 0 && answer.charAt(0) == 'y';
    }


    private List<String> getActual(String filename) {
        List<String> result = new ArrayList<String>();
        try{
            Scanner sc = new Scanner(new File(filename));
            while(sc.hasNext()) {
                String temp = sc.nextLine().trim();
                result.add(temp);

            }
            sc.close();
        }
        catch(IOException e) {
            System.out.println("Error reading from file: " + e);
            System.out.println("Returning null");
            result = null;
        }
        return result;
    }


    private TreeSet<AllPathsInfo> rankByScoreAdjustWinPercentage(Set<AllPathsInfo> paths) {

        TreeSet<AllPathsInfo> result = new TreeSet<AllPathsInfo>(new AveCostComparator());
        for(AllPathsInfo teamPaths : paths) {
            FootballRecord team = records.get(teamPaths.getName());
            if(team.getWins() > MIN_WINS && teamPaths.getNumPaths() > MIN_TRANSITIVE_WINS) {
                double winPercent = team.winPercent();
                AllPathsInfo copy = AllPathsInfo.makeCopy(teamPaths);
                copy.adjustAveCost(1.0 / winPercent);
                result.add(copy);
            }
        }
        return result;
    }


    private TreeSet<AllPathsInfo> rankByScore(Set<AllPathsInfo> paths) {

        TreeSet<AllPathsInfo> result = new TreeSet<AllPathsInfo>(new AveCostComparator());
        for(AllPathsInfo teamPaths : paths) {
            FootballRecord team = records.get(teamPaths.getName());
            if(team.getWins() > MIN_WINS && teamPaths.getNumPaths() > MIN_TRANSITIVE_WINS) {
                boolean added = result.add(teamPaths);
                assert added : "Shoudl have added this team. " + teamPaths;
            }
        }
        return result;
    }

    private Graph buildGraph(String gameResults) {
        Graph g = new Graph();
        try {
            Scanner sc = new Scanner(new File(gameResults));
            while(sc.hasNext()) {
                String temp = sc.nextLine();
                String[] line = temp.trim().split(",");
                int scoreDiff = Integer.parseInt(line[HOME_SCORE]) - Integer.parseInt(line[AWAY_SCORE]);
                if(scoreDiff != 0) {
                    addTeam(records, line[1]);
                    addTeam(records, line[3]);
                    addEdge(g, line, records, scoreDiff);
                }
                else {
                    //prints out ties
                    //System.out.println(Arrays.toString(line));
                }
            }
            sc.close();
        }
        catch(IOException e) {
            System.out.println("Error reading from file: " + e);
            System.out.println("Returning null");
            g = null;
        }
        return g;
    }


    private void addEdge(Graph g, String[] data, Map<String, FootballRecord> records, int scoreDiff) {
        int scoresWonBy = (scoreDiff / TD_PLUS);
        if(scoresWonBy == 0)
            scoresWonBy = 1;
        double cost = Math.abs(1.0 / scoresWonBy);

        if(scoreDiff > 0) {
            g.addEdge(data[HOME_TEAM], data[AWAY_TEAM], cost);
            records.get(data[HOME_TEAM]).win();
            records.get(data[AWAY_TEAM]).lose();
        }
        else {
            g.addEdge(data[3], data[HOME_TEAM], cost);
            records.get(data[AWAY_TEAM]).win();
            records.get(data[HOME_TEAM]).lose();
        }
    }


    private void addTeam(Map<String, FootballRecord> records, String name) {
        if(!records.containsKey(name))
            records.put(name, new FootballRecord(name));
    }


    private static class AveCostComparator implements Comparator<AllPathsInfo>{

        public int compare(AllPathsInfo a, AllPathsInfo b) {
            int result = (a.getAveCost() < b.getAveCost()) ? -1 : (a.getAveCost() == b.getAveCost()) ? 0 : 1;
            if(result == 0)
                result = a.getName().compareTo(b.getName());
            return result;
        }
    }

    public void showAll() {
        teamsAndResults.showAll();
    }
}

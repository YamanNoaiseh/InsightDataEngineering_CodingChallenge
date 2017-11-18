import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.HashSet;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 * This class represents the graph of Venmo payments
 * 
 * @author Yaman Noaiseh
 * @date July 10, 2016
 *
 */
public class VenmoGraph {
	
	private Date maxTime;
	private double medianDegree;
	private Map<Payment, Date> edges;
	private Map<String, Integer> vertices;
	
	/**
	 * Initiates a graph object
	 */
	public VenmoGraph() {
		maxTime = null;
		medianDegree = 0.0;
		vertices = new HashMap<String, Integer>(100, 0.75F);
		edges = new HashMap<Payment, Date>(100, 0.75F);
	}
	
	/**
	 * The main method of the program. Creates a graph object and processes it
	 * 
	 * @param args not used
	 */
	public static void main(String[] args) {
		VenmoGraph venmo = new VenmoGraph();
		processInput(venmo);
	}
	
	/**
	 * Handles an input file, and creates and writes in the output file
	 * 
	 * @param graph object to build
	 */
	public static void processInput(VenmoGraph graph) {
		String inputFile = "./../venmo_input/venmo-trans.txt";
		String outputFile = "./../venmo_output/output.txt";
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		int bufferSize = 8*1024;
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile), bufferSize);
				BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile), bufferSize)) {
			String jsonStr;
			// Reading the input file
			while ((jsonStr = bufferedReader.readLine()) != null) {
				// if statement to ignore blank lines in the input file
				if(jsonStr.length() > 0) {
					JsonObject jsonObject = getJsonFromString(jsonStr);
					Date timestamp = formatter.parse(jsonObject.getString("created_time"));
					// If the graph is empty, start it
					if (graph.edges.size() == 0) {
						graph.resetGraph(jsonObject);
					} else {
						long timeDiff = timeDifference(timestamp, graph.maxTime);
						// If the maximum time is more than 60 seconds from the incoming payment, reset the graph
						if (timeDiff >= 60) {
							graph.edges.clear();
							graph.vertices.clear();
							graph.resetGraph(jsonObject);
							bufferedWriter.newLine();
						} else if (timeDiff > 0) { // In-order payment
							graph.maxTime = timestamp;
							Payment payment = new Payment(jsonObject.getString("actor"), jsonObject.getString("target"));
							// If an edge between the two users exists, just update the time stamp
							if (graph.edges.containsKey(payment)) {
								graph.edges.put(payment, timestamp);
							} else {
								graph.addNewEdge(payment, timestamp);
							}
							// edges that should be evicted will be first stored in this set
							Set<Payment> edgesToEvict = new HashSet<Payment>();
							for (Payment key: graph.edges.keySet()) {
								if (timeDifference(graph.maxTime, graph.edges.get(key)) >= 60) {
									edgesToEvict.add(key);
								}
							}
							// Evict edges
							for (Payment edge : edgesToEvict) {
								graph.edges.remove(edge);
								graph.decreaseUserDegree(edge.user1);
								graph.decreaseUserDegree(edge.user2);
							}
							// Update the median degree
							graph.medianDegree = graph.getUpdatedMedian();
							bufferedWriter.newLine();
						} else if (timeDiff > -60) {
							// Out of order, but valid payment
							Payment payment = new Payment(jsonObject.getString("actor"), jsonObject.getString("target"));
							if (graph.edges.containsKey(payment)) {
								if (timeDifference(timestamp, graph.edges.get(payment)) > 0) {
									graph.edges.put(payment, timestamp);
								}
							} else {
								graph.addNewEdge(payment, timestamp);
							}
							graph.medianDegree = graph.getUpdatedMedian();
							bufferedWriter.newLine();
						} else { // Out of order payment, and out of the 60 seconds window
							bufferedWriter.newLine();
						}
					}
					// Write the current median to the output file
					bufferedWriter.write(new DecimalFormat("0.00").format(graph.truncateMedianDegree()));
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Gets JSON data from a string
	 * 
	 * @param jsonStr String representation of JSON data
	 * @return JSON object
	 */
	private static JsonObject getJsonFromString(String jsonStr) {
	    JsonReader reader = Json.createReader(new StringReader(jsonStr));
	    JsonObject jsonObject = reader.readObject();
	    reader.close();
	    return jsonObject;
	}
	
	/**
	 * Initiates a blank graph with one edge and two vertices from the JSON object's data
	 * 
	 * @param jsonObject from which the data will be extracted and set into the graph
	 */
	private void resetGraph(JsonObject jsonObject) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		try {
			Date timestamp = formatter.parse(jsonObject.getString("created_time"));
			String actor = jsonObject.getString("actor");
			String target = jsonObject.getString("target");
			Payment edge = new Payment(actor, target);
			edges.put(edge, timestamp);
			maxTime = timestamp;
			vertices.put(jsonObject.getString("actor"), 1);
			vertices.put(jsonObject.getString("target"), 1);
			medianDegree = 1;
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds a new, not-existing, edge to the graph,
	 * and increases the degrees of the two users involved in the payment
	 * 
	 * @param edge a payment object giving the names of the two users involved in the payment 
	 * @param timestamp the time stamp of the payment
	 */
	private void addNewEdge(Payment edge, Date timestamp) {
		edges.put(edge, timestamp);
		increaseUserDegree(edge.user1);
		increaseUserDegree(edge.user2);
	}
	
	/**
	 * Increases the number of neighbors of a user, if exists,
	 * or adds it to the graph and sets the number of neighbors to 1 
	 * 
	 * @param user the name of the user
	 */
	private void increaseUserDegree(String user) {
		if (vertices.get(user) != null) {
			vertices.put(user, vertices.get(user) + 1);
		} else {
			vertices.put(user, 1);
		}
	}
	
	/**
	 * Reduces the number of neighbors of a user by 1 if it has more than one neighbor,
	 * or removes the user from the graph if it has only one neighbor 
	 * 
	 * @param user the name of the user
	 */
	private void decreaseUserDegree(String user) {
		if (vertices.get(user) == 1) {
			vertices.remove(user);
		} else {
			vertices.put(user, vertices.get(user) - 1);
		}
	}
	
	/**
	 * Finds and returns the median degree of all vertices in the graph
	 * 
	 * @return the current median degree
	 */
	// To be improved later; Use min and max heaps and implement the running median algorithm
	// to get the current median
	private double getUpdatedMedian() {
		PriorityQueue<Integer> queue = new PriorityQueue<Integer>(vertices.values());
		int halfSize = queue.size() / 2;
		if (queue.size() % 2 == 1) {
			for (int i = 0; i < halfSize; i++) {
				queue.poll();
			}
			return queue.poll();
		} else {
			int degree = 0;
			for (int i = 0; i < halfSize; i++) {
				degree = queue.poll();
			}
			return (degree + queue.poll()) / 2.0;
		}
	}
	
	/**
	 * Fines and returns the time difference between two dates in seconds
	 * 
	 * @param d1 the first date
	 * @param d2 the second date
	 * @return the time difference between the two date objects in seconds
	 */
	private static long timeDifference(Date d1, Date d2) {
		long timeDiff = d1.getTime() - d2.getTime();
		return TimeUnit.MILLISECONDS.toSeconds(timeDiff);
	}
	
	/**
	 * Truncates the graph's median degree with the precision of two digits after the decimal place
	 * 
	 * @return the truncated median degree
	 */
	private double truncateMedianDegree() {
		medianDegree *= 100;
		medianDegree -= medianDegree % 1;
		medianDegree /= 100;
		return medianDegree;
	}
	
}


/**
 * This class represents a payment between two Venmo users
 * 
 * @author Yaman Noaiseh
 * @date July 10, 2016
 */
class Payment {
	
	String user1;
	String user2;
	
	/**
	* Creates a payment object using two user names
	* 
	* @param user1 the name of the first user involved in a transaction
	* @param user2 the name of the second user involved in a transaction
	*/
	Payment(String user1, String user2){
		this.user1 = user1;
		this.user2 = user2;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof Payment) {
			Payment payment = (Payment) other;
			// Because this is an indirected graph,
			// two payments are equal if they hold the same two user names
			if ((user1.equals(payment.user1) && user2.equals(payment.user2)) ||
					(user1.equals(payment.user2) && user2.equals(payment.user1))) {
				return true;
			}
			return false;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return user1.hashCode() ^ user2.hashCode();
	}
	
}

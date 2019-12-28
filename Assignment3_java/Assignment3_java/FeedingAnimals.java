package Assignment3_java;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;


/**
 * Assignment No:3
 * Member 1: Chilin Ma,991386717
 * Member 2:Janica Sivani Balaji Ramathaal,991536141
 * Submission date:December 6,2019
 * Instructor's name:Syed Tanbeer
 */
public class FeedingAnimals {
	//Thread Variables
	private static FoodStorage foodStorage = new FoodStorage();
	private static ArrayList<Animal> animalList = new ArrayList<>();
	private static int runtime = 20;
	private static int countRun = 0; //This index serve the purpose of stopping deposit when animal has been fed runtime times 
	
	//DBMS Variables
	private static Connection conn;
	private static Statement stmt;
	private static ResultSet rs;
	private static PreparedStatement pStmt;
	private static String username = "root";
	private static String password = "1234";
	private static String url = "jdbc:mysql://localhost/ZooDB";
	
	//result helping variables
	private static int totalFood =0;
	private static ArrayList<String> maxConsumeAnimal = new ArrayList<>();
	private static ArrayList<String> maxHungryAnimal = new ArrayList<>();
	
	public static void main(String[] args) {
		
		//create and add all the animals into the list
		Animal elephant = new  Animal("Elephant", 15);
		Animal giraffe = new Animal("Giraffe", 9);
		Animal horse = new Animal ("Horse", 5);
		Animal zebra = new Animal ("Zebra", 5);
		Animal deer = new Animal("Deer", 3);
		animalList.add(elephant);
		animalList.add(giraffe);
		animalList.add(horse);
		animalList.add(zebra);
		animalList.add(deer);
		
		//start to run the threads
		System.out.printf("%-18s%-50s%10s\n", "Deposit Food", "Feed Animals", "Stock(kg)");
		ExecutorService ex = Executors.newCachedThreadPool();
		
		ex.execute(new FeedAnimalFromStorage());
		ex.execute(new AddFoodToStorage());
			
		ex.shutdown();
		//prevent main method keep going while the feeding and adding food thread are not finished
		while(!ex.isTerminated()) {}
		
		System.out.println("----------------Finished Feeding---------------");
		
		//connect to database
		createConnectionAndStatement();
		//create the table and write the information into the table
		createTable();
		for(Animal animal:animalList) {
			insertData(animal.getName(), animal.getFeedingCount(), animal.getHungryCount());
		}
		
		//output the data from database
		System.out.printf("%-12s%-14s%-12s\n", "AnimalName", "FeedingCount", "HungerCount");
		getData();
		
		//output highest food consume animal
		System.out.print("Highest amount of food consumed by: ");
		for(String name: maxConsumeAnimal) {
			System.out.print(name + " ");
		}
		System.out.println();
		
		//output most hungry count animal
		System.out.print("The most hungry animal: ");
		for(String name: maxHungryAnimal) {
			System.out.print(name + " ");
		}
		System.out.println();
		
		//output total good consume
		System.out.println("Total food consumed by all animals: " + totalFood + "kg");

	}
	//DBMS OPERATIONS-----------------------------------------------------
	
	//connect and create statement
		public static void createConnectionAndStatement() {
			try {
				conn = DriverManager.getConnection(url, username, password);
				System.out.println("connection created----");
				stmt = conn.createStatement();
				System.out.println("statement created------");
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	
	//create Feeding Data table in SQL database
	public static void createTable() {	
		try {
			//check weather the table has already exist in the database
			//if exist delete the table
			DatabaseMetaData metadata = conn.getMetaData();
			ResultSet tables = metadata.getTables(null, null, "FeedingData", null);
			if(tables.next()) {
				String dropTableQuery = "DROP TABLE FeedingData";
				stmt.executeUpdate(dropTableQuery);
			}
			//create table
			String createTableQuery = " CREATE table FeedingData(animalName varchar(225) primary key , FeedingCount int, HungryCount int);";
			stmt.executeUpdate(createTableQuery);
			System.out.println("Table created------");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//insert data into FeedingData Table
	public static void insertData(String animalName, int feedingCount, int hungryCount) {
		String prepareQuery = "Insert into FeedingData value(?, ?, ?)";
		try {
			 pStmt = conn.prepareStatement(prepareQuery);
			 //set the input values into the ?
			 pStmt.setString(1, animalName);
			 pStmt.setInt(2, feedingCount);
			 pStmt.setInt(3, hungryCount);
			 //execute query
			 pStmt.executeUpdate();
			 //display success message
			 System.out.println("Animal: " + animalName + " inserted successfully------");
		} catch (SQLException e) {
			e.printStackTrace();	
		}
	}
	
	//method to get the data from the Exercise table
	public static void getData() {
		String query = "Select * from FeedingData";
		try {
			rs = stmt.executeQuery(query);
			
			//store record for animal names and the total consume amount for each animal
			ArrayList<String> animalNameList = new ArrayList<>();
			ArrayList<Integer> consumeList = new ArrayList<>();
			//track max hungry count;
			int maxHungry = 0; 
			//display the result
			while(rs.next()) {
				//output the result
				String animalName = rs.getString("animalName");
				int feedingCount  = rs.getInt("FeedingCount");
				int hungryCount = rs.getInt("HungryCount");
				System.out.printf("%-12s%-14s%-12s\n", animalName, feedingCount, hungryCount);
				animalNameList.add(animalName);
				
				//counting the store the consume amount of each animal
				for(Animal animal: animalList) {
					if(animal.getName().equalsIgnoreCase(animalName)) {
						int amount = feedingCount*animal.getRequiredAmount();
						consumeList.add(amount);
						totalFood += amount;
						break;
					}
				}
				
				//compare to find max hungry
				if(hungryCount > maxHungry) {
					maxHungryAnimal.clear();
					maxHungry = hungryCount;
					maxHungryAnimal.add(animalName);
				}else if(hungryCount == maxHungry) {
					maxHungryAnimal.add(animalName);
				}
			}
			
			//get the max amount consume and get hungry
			int maxAmount = consumeList.get(0);
			maxConsumeAnimal.add(animalNameList.get(0));
			for(int i = 1; i < consumeList.size(); i++) {
				if(consumeList.get(i) > maxAmount) {
					maxConsumeAnimal.clear();
					maxAmount = consumeList.get(i);
					maxConsumeAnimal.add(animalNameList.get(i));
				}else if(consumeList.get(i) == maxAmount){
					maxConsumeAnimal.add(animalNameList.get(i));
				}
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	

	//MultiThread Class---------------------------------------------------
	//class for add food into storage
	static class AddFoodToStorage implements Runnable{
		@Override
		public void run() {
			try {	
				while(countRun!=runtime) {
					//randomly generate amount to add into storage
					int amount = (int)(Math.random()*20)+1;
					foodStorage.addFood(amount);
					Thread.sleep(1000);
				}
			}catch(Exception e) {
				e.printStackTrace();
			}	
		}	
	}
	
	//class for feed the animals
	static class FeedAnimalFromStorage implements Runnable{
		@Override
		public void run() {
			//loop through runtime
			for(int i=0; i<runtime; i++) {
				//randomly generate index for animal in the list
				int index = (int)(Math.random()*animalList.size());
				foodStorage.feedAnimal(animalList.get(index));
				countRun++;
				
			}			
		}
	}
	
}

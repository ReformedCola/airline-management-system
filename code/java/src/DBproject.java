import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class DBproject{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

	public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("\033[1;32m"); // bold green
		System.out.print("Connecting to database...");
		System.out.print("\033[0m"); // reset color
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.print("\033[1;32m"); // bold green
			System.out.println ("Connection URL: " + url + "\n");
			System.out.print("\033[0m"); // reset color

			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
					System.out.print("\033[1;32m"); // bold green
	        System.out.println("Done");
					System.out.print("\033[0m"); // reset color
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}

	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 *
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException {
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 *
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;

		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 *
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 * obtains the metadata object for the returned result set.  The metadata
		 * contains row and column info.
		*/
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;

		//iterates through the result set and saves the data returned by the query.
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>();
		while (rs.next()){
			List<String> record = new ArrayList<String>();
			for (int i=1; i<=numCol; ++i)
				record.add(rs.getString (i));
			result.add(record);
		}//end while
		stmt.close ();
		return result;
	}//end executeQueryAndReturnResult

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 *
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}

	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current
	 * value of sequence used for autogenerated keys
	 *
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */

	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();

		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 *
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + DBproject.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if

		DBproject esql = null;

		try{
			System.out.print("\033[1;32m"); // bold green
			System.out.println("(1)");
			System.out.print("\033[0m"); // reset color

			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			System.out.print("\033[1;32m"); // bold green
			System.out.println("(2)");
			System.out.print("\033[0m"); // reset color
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];

			esql = new DBproject (dbname, dbport, user, "");

			boolean keepon = true;
			while(keepon){
				System.out.print("\033[1;31m"); // bold red
				System.out.println("WELCOME TO OUR AIRLINE DATABASE SYSTEM.");
				System.out.println("---------");
				System.out.println("1. Add Plane");
				System.out.println("2. Add Pilot");
				System.out.println("3. Add Flight");
				System.out.println("4. Add Technician");
				System.out.println("5. Book Flight");
				System.out.println("6. List number of available seats for a given flight.");
				System.out.println("7. List total number of repairs per plane in descending order");
				System.out.println("8. List total number of repairs per year in ascending order");
				System.out.println("9. Find total number of passengers with a given status");
				System.out.println("10. < EXIT");
				System.out.print("\033[0m"); // reset color

				switch (readChoice()){
					case 1: AddPlane(esql); break;
					case 2: AddPilot(esql); break;
					case 3: AddFlight(esql); break;
					case 4: AddTechnician(esql); break;
					case 5: BookFlight(esql); break;
					case 6: ListNumberOfAvailableSeats(esql); break;
					case 7: ListsTotalNumberOfRepairsPerPlane(esql); break;
					case 8: ListTotalNumberOfRepairsPerYear(esql); break;
					case 9: FindPassengersCountWithStatus(esql); break;
					case 10: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("\033[1;32m"); // bold green
					System.out.print("Disconnecting from database...");
					System.out.print("\033[0m"); // reset color
					esql.cleanup ();
					System.out.print("\033[1;32m"); // bold green
					System.out.println("Done\n\nBye !");
					System.out.print("\033[0m"); // reset color
				}//end if
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("\033[1;31m"); // bold red
			System.out.print("Please make your choice: ");
			System.out.print("\033[0m"); // reset color
			try { // read the integer, parse it and break.
				System.out.print("\033[1;36m"); // bold cyan
				input = Integer.parseInt(in.readLine());
				System.out.print("\033[0m"); // reset color
				break;
			}catch (Exception e) {
				System.out.print("\033[101m"); // red background
				System.out.print("\033[1;37m"); // bold white
				System.out.print("Your input is invalid!");
				System.out.println("\033[0m"); // reset color
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	public static void AddPlane(DBproject esql) {//1
		String make;
		String model;
		int age;
		int seats;
		int id;

		// prompt user for make of the plane.
		do
		{
			System.out.print("\033[1;31m"); // bold red
			System.out.print("\tPlease enter the make of the plane: ");
			System.out.print("\033[0m"); // reset color
			try
			{
				System.out.print("\033[1;36m"); // bold cyan
				make = in.readLine();
				System.out.print("\033[0m"); // reset color
				break;
			}
			catch(Exception e)
			{
				System.out.print("\033[101m"); // red background
				System.out.print("\033[1;37m"); // bold white
				System.out.print("\tSorry, your input is invalid.");
				System.out.println("\033[0m"); // reset color
				continue;
			}
		} while(true);

		// prompt user for model of the plane.
		do
		{
			System.out.print("\033[1;31m"); // bold red
			System.out.print("\tPlease enter the model of the plane: ");
			System.out.print("\033[0m"); // reset color
			try
			{
				System.out.print("\033[1;36m"); // bold cyan
				model = in.readLine();
				System.out.print("\033[0m"); // reset color
				break;
			}
			catch(Exception e)
			{
				System.out.print("\033[101m"); // red background
				System.out.print("\033[1;37m"); // bold white
				System.out.print("\tSorry, your input is invalid.");
				System.out.println("\033[0m"); // reset color
				continue;
			}
		} while(true);

		// prompt user for age of the plane.
		do
		{
			System.out.print("\033[1;31m"); // bold red
			System.out.print("\tPlease enter the age of the plane: ");
			System.out.print("\033[0m"); // reset color
			try
			{
				System.out.print("\033[1;36m"); // bold cyan
				age = Integer.parseInt(in.readLine());
				System.out.print("\033[0m"); // reset color
				if(Integer.valueOf(age) <= 0)
				{
					System.out.print("\033[101m"); // red background
					System.out.print("\033[1;37m"); // bold white
					System.out.print("\tInvalid age, please try again.");
					System.out.println("\033[0m"); // reset color
				}
				else
				{
					break;
				}
			}
			catch(Exception e)
			{
				System.out.print("\033[101m"); // red background
				System.out.print("\033[1;37m"); // bold white
				System.out.print("\tSorry, your input is invalid.");
				System.out.println("\033[0m"); // reset color
				continue;
			}
		} while(true);

		// prompt user for seats of the plane.
		do
		{
			System.out.print("\033[1;31m"); // bold red
			System.out.print("\tPlease enter the number of seats on the plane(Maximum Seats = 499): ");
			System.out.print("\033[0m"); // reset color
			try
			{
				System.out.print("\033[1;36m"); // bold cyan
				seats = Integer.parseInt(in.readLine());
				System.out.print("\033[0m"); // reset color
				if(Integer.valueOf(seats) < 0 || Integer.valueOf(seats) > 499)
				{
					System.out.print("\033[101m"); // red background
					System.out.print("\033[1;37m"); // bold white
					System.out.print("\tInvalid seats number, please try again.");
					System.out.println("\033[0m"); // reset color
				}
				else
				{
					break;
				}
			}
			catch(Exception e)
			{
				System.out.print("\033[101m"); // red background
				System.out.print("\033[1;37m"); // bold white
				System.out.print("\tSorry, your input is invalid.");
				System.out.println("\033[0m"); // reset color
				continue;
			}
		} while(true);

		System.out.print("\033[4;36m"); // cyan underline
		System.out.print("\033[1;36m"); // bold cyan
		System.out.println("\tMake: " + make + ", Model: " + model + ", Age: " + age + ", Seats: " + seats);
		System.out.print("\033[0m"); // reset color

		// add plane.
		try
		{
			String queryA = "SELECT id FROM Plane";
			List<List<String>> plane_id = esql.executeQueryAndReturnResult(queryA);
			id = plane_id.size();
			String queryB = "INSERT INTO Plane VALUES(" + id + ", \'" + make + "\', \'" + model + "\', " + age + ", " + seats + ");";
			esql.executeUpdate(queryB);
			System.out.print("\033[1;36m"); // bold cyan
			System.out.println("\tThe plane is added successfully!");
			System.out.print("\033[0m"); // reset color
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
		}

	}

	public static void AddPilot(DBproject esql) {//2
		String fullname;
		String nationality;
		int id;

		// prompt user for full name of the pilot.
		do
		{
			System.out.print("\033[1;31m"); // bold red
			System.out.print("\tPlease enter the full name of the pilot: ");
			System.out.print("\033[0m"); // reset color
			try
			{
				System.out.print("\033[1;36m"); // bold cyan
				fullname = in.readLine();
				System.out.print("\033[0m"); // reset color
				break;
			}
			catch(Exception e)
			{
				System.out.print("\033[101m"); // red background
				System.out.print("\033[1;37m"); // bold white
				System.out.print("\tSorry, your input is invalid.");
				System.out.println("\033[0m"); // reset color
				continue;
			}
		} while(true);

		// prompt user for nationality of the pilot.
		do
		{
			System.out.print("\033[1;31m"); // bold red
			System.out.print("\tPlease enter the nationality of the pilot: ");
			System.out.print("\033[0m"); // reset color
			try
			{
				System.out.print("\033[1;36m"); // bold cyan
				nationality = in.readLine();
				System.out.print("\033[0m"); // reset color
				break;
			}
			catch(Exception e)
			{
				System.out.print("\033[101m"); // red background
				System.out.print("\033[1;37m"); // bold white
				System.out.print("\tSorry, your input is invalid.");
				System.out.println("\033[0m"); // reset color
				continue;
			}
		} while(true);

		System.out.print("\033[4;36m"); // cyan underline
		System.out.print("\033[1;36m"); // bold cyan
		System.out.println("\tFull Name: " + fullname + ", Nationality: " + nationality);
		System.out.print("\033[0m"); // reset color

		// add pilot.
		try
		{
			String queryA = "SELECT id FROM Pilot";
			List<List<String>> pilot_id = esql.executeQueryAndReturnResult(queryA);
			id = pilot_id.size();
			String queryB = "INSERT INTO Pilot VALUES(" + id + ", \'" + fullname + "\', \'" + nationality + "\');";
			esql.executeUpdate(queryB);
			System.out.print("\033[1;36m"); // bold cyan
			System.out.println("\tThe pilot is added successfully!");
			System.out.print("\033[0m"); // reset color
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
		}

	}

	public static void AddFlight(DBproject esql) {//3
		// Given a pilot, plane and flight, adds a flight in the DB
		int cost;
		int num_sold;
		int num_stops;
		String departure_date;
		String arrival_date;
		String departure_airport;
		String arrival_airport;
		int id;

		// prompt user for cost of the ticket.
		do
		{
			System.out.print("\033[1;31m"); // bold red
			System.out.print("\tPlease enter the cost of the ticket for this flight: ");
			System.out.print("\033[0m"); // reset color
			try
			{
				System.out.print("\033[1;36m"); // bold cyan
				cost = Integer.parseInt(in.readLine());
				System.out.print("\033[0m"); // reset color
				if(Integer.valueOf(cost) <= 0)
				{
					System.out.print("\033[101m"); // red background
					System.out.print("\033[1;37m"); // bold white
					System.out.print("\tInvalid cost, please enter the cost that is greater than 0.");
					System.out.println("\033[0m"); // reset color
				}
				else
				{
					break;
				}
			}
			catch(Exception e)
			{
				System.out.print("\033[101m"); // red background
				System.out.print("\033[1;37m"); // bold white
				System.out.print("\tSorry, your input is invalid.");
				System.out.println("\033[0m"); // reset color
				continue;
			}
		} while(true);

		// prompt user for number of tickets sold.
		do
		{
			System.out.print("\033[1;31m"); // bold red
			System.out.print("\tPlease enter the number of tickets sold for this flight: ");
			System.out.print("\033[0m"); // reset color
			try
			{
				System.out.print("\033[1;36m"); // bold cyan
				num_sold = Integer.parseInt(in.readLine());
				System.out.print("\033[0m"); // reset color
				if(Integer.valueOf(num_sold) < 0)
				{
					System.out.print("\033[101m"); // red background
					System.out.print("\033[1;37m"); // bold white
					System.out.print("\tInvalid number of tickets sold, please enter the number that is greater than or equals to 0.");
					System.out.println("\033[0m"); // reset color
				}
				else
				{
					break;
				}
			}
			catch(Exception e)
			{
				System.out.print("\033[101m"); // red background
				System.out.print("\033[1;37m"); // bold white
				System.out.print("\tSorry, your input is invalid.");
				System.out.println("\033[0m"); // reset color
				continue;
			}
		} while(true);

		// prompt user for number of stops.
		do
		{
			System.out.print("\033[1;31m"); // bold red
			System.out.print("\tPlease enter the number of stops for this flight: ");
			System.out.print("\033[0m"); // reset color
			try
			{
				System.out.print("\033[1;36m"); // bold cyan
				num_stops = Integer.parseInt(in.readLine());
				System.out.print("\033[0m"); // reset color
				if(Integer.valueOf(num_stops) <= 0)
				{
					System.out.print("\033[101m"); // red background
					System.out.print("\033[1;37m"); // bold white
					System.out.print("\tInvalid number of stops, please enter the number that is greater than or equals to 0.");
					System.out.println("\033[0m"); // reset color
				}
				else
				{
					break;
				}
			}
			catch(Exception e)
			{
				System.out.print("\033[101m"); // red background
				System.out.print("\033[1;37m"); // bold white
				System.out.print("\tSorry, your input is invalid.");
				System.out.println("\033[0m"); // reset color
				continue;
			}
		} while(true);

		// prompt user for departure date.
		do
		{
			System.out.print("\033[1;31m"); // bold red
			System.out.print("\tPlease enter the departure date for this flight(YYYY-MM-DD): ");
			System.out.print("\033[0m"); // reset color
			try
			{
				System.out.print("\033[1;36m"); // bold cyan
				departure_date = in.readLine();
				System.out.print("\033[0m"); // reset color
				break;
			}
			catch(Exception e)
			{
				System.out.print("\033[101m"); // red background
				System.out.print("\033[1;37m"); // bold white
				System.out.print("\tSorry, your input is invalid.");
				System.out.println("\033[0m"); // reset color
				continue;
			}
		} while(true);

		// prompt user for arrival date.
		do
		{
			System.out.print("\033[1;31m"); // bold red
			System.out.print("\tPlease enter the arrival date for this flight(YYYY-MM-DD): ");
			System.out.print("\033[0m"); // reset color
			try
			{
				System.out.print("\033[1;36m"); // bold cyan
				arrival_date = in.readLine();
				System.out.print("\033[0m"); // reset color
				break;
			}
			catch(Exception e)
			{
				System.out.print("\033[101m"); // red background
				System.out.print("\033[1;37m"); // bold white
				System.out.print("\tSorry, your input is invalid.");
				System.out.println("\033[0m"); // reset color
				continue;
			}
		} while(true);

		// prompt user for departure airport.
		do
		{
			System.out.print("\033[1;31m"); // bold red
			System.out.print("\tPlease enter the departure airport for this flight(Airport Code): ");
			System.out.print("\033[0m"); // reset color
			try
			{
				System.out.print("\033[1;36m"); // bold cyan
				departure_airport = in.readLine();
				System.out.print("\033[0m"); // reset color
				if(departure_airport.length() < 5 || departure_airport.length() > 5)
				{
					System.out.print("\033[101m"); // red background
					System.out.print("\033[1;37m"); // bold white
					System.out.print("\tInvalid airport code, please enter 5 character airport code.");
					System.out.println("\033[0m"); // reset color
				}
				else
				{
					break;
				}
			}
			catch(Exception e)
			{
				System.out.print("\033[101m"); // red background
				System.out.print("\033[1;37m"); // bold white
				System.out.print("\tSorry, your input is invalid.");
				System.out.println("\033[0m"); // reset color
				continue;
			}
		} while(true);

		// prompt user for arrival airport.
		do
		{
			System.out.print("\033[1;31m"); // bold red
			System.out.print("\tPlease enter the arrival airport for this flight(Airport Code): ");
			System.out.print("\033[0m"); // reset color
			try
			{
				System.out.print("\033[1;36m"); // bold cyan
				arrival_airport = in.readLine();
				System.out.print("\033[0m"); // reset color
				if(arrival_airport.length() < 5 || arrival_airport.length() > 5)
				{
					System.out.print("\033[101m"); // red background
					System.out.print("\033[1;37m"); // bold white
					System.out.print("\tInvalid airport code, please enter 5 character airport code.");
					System.out.println("\033[0m"); // reset color
				}
				else
				{
					break;
				}
			}
			catch(Exception e)
			{
				System.out.print("\033[101m"); // red background
				System.out.print("\033[1;37m"); // bold white
				System.out.print("\tSorry, your input is invalid.");
				System.out.println("\033[0m"); // reset color
				continue;
			}
		} while(true);

		System.out.print("\033[4;36m"); // cyan underline
		System.out.print("\033[1;36m"); // bold cyan
		System.out.println("\tCost: " + cost + ", Tickets Sold: " + num_sold + ", Stops: " + num_stops + ", Departure Date: " + departure_date + ", Arrival Date: " + arrival_date + ", Departure Airport: " + departure_airport + ", Arrival Airport: " + arrival_airport);
		System.out.print("\033[0m"); // reset color

		// add flight.
		try
		{
			String queryA = "SELECT fnum FROM Flight";
			List<List<String>> flight_id = esql.executeQueryAndReturnResult(queryA);
			id = flight_id.size();
			String queryB = "INSERT INTO Flight VALUES(" + id + ", " + cost + ", " + num_sold + ", " + num_stops + ", \'" + departure_date + "\', \'" + arrival_date + "\', \'" + arrival_airport + "\', \'" + departure_airport + "\');";
			esql.executeUpdate(queryB);
			System.out.print("\033[1;36m"); // bold cyan
			System.out.println("\tThe flight is added successfully!");
			System.out.print("\033[0m"); // reset color
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
		}

	}

	public static void AddTechnician(DBproject esql) {//4
		String full_name;
		int id;

		// prompt user for full name of the technician.
		do
		{
			System.out.print("\033[1;31m"); // bold red
			System.out.print("\tPlease enter the full name of the technician: ");
			System.out.print("\033[0m"); // reset color

			try
			{
				System.out.print("\033[1;36m"); // bold cyan
				full_name = in.readLine();
				System.out.print("\033[0m"); // reset color
				break;
			}
			catch(Exception e)
			{
				System.out.print("\033[101m"); // red background
				System.out.print("\033[1;37m"); // bold white
				System.out.print("\tSorry, your input is invalid.");
				System.out.println("\033[0m"); // reset color
				continue;
			}
		} while(true);

		System.out.print("\033[4;36m"); // cyan underline
		System.out.print("\033[1;36m"); // bold cyan
		System.out.println("\tFull Name: " + full_name);
		System.out.print("\033[0m"); // reset color

		// add technician.
		try
		{
			String queryA = "SELECT id FROM Technician";
			List<List<String>> technician_id = esql.executeQueryAndReturnResult(queryA);
			id = technician_id.size();
			String queryB = "INSERT INTO Technician VALUES(" + id + ", \'" + full_name + "\');";
			esql.executeUpdate(queryB);
			System.out.print("\033[1;36m"); // bold cyan
			System.out.println("\tThe technician is added successfully!");
			System.out.print("\033[0m"); // reset color
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
		}

	}

	public static void BookFlight(DBproject esql) {//5
		// Given a customer and a flight that he/she wants to book, add a reservation to the DB
		int cid;
		int fnum;
		String userInput;
		String upperuserInput;
		//int seatsSold;

		// prompt user for id of the customer.
		do
		{
			System.out.print("\033[1;31m"); // bold red
			System.out.print("\tPlease enter the id of the customer: ");
			System.out.print("\033[0m"); // reset color
			try
			{
				System.out.print("\033[1;36m"); // bold cyan
				cid = Integer.parseInt(in.readLine());
				System.out.print("\033[0m"); // reset color
				break;
			}
			catch(Exception e)
			{
				System.out.print("\033[101m"); // red background
				System.out.print("\033[1;37m"); // bold white
				System.out.print("\tSorry, your input is invalid.");
				System.out.println("\033[0m"); // reset color
				continue;
			}
		} while(true);

		// prompt user for flight number.
		do
		{
			System.out.print("\033[1;31m"); // bold red
			System.out.print("\tPlease enter the number of flight you want to book: ");
			System.out.print("\033[0m"); // reset color
			try
			{
				System.out.print("\033[1;36m"); // bold cyan
				fnum = Integer.parseInt(in.readLine());
				System.out.print("\033[0m"); // reset color
				break;
			}
			catch(Exception e)
			{
				System.out.print("\033[101m"); // red background
				System.out.print("\033[1;37m"); // bold white
				System.out.print("\tSorry, your input is invalid.");
				System.out.println("\033[0m"); // reset color
				continue;
			}
		} while(true);

		try
		{
			// get reserveration number.
			String queryA = "SELECT rnum FROM Reservation";
			List<List<String>> reserveNum = esql.executeQueryAndReturnResult(queryA);
			int rnum = reserveNum.size();

			// check if there are available seats.
			String seatsQuery = "SELECT P.seats - F.num_sold FROM Flight F, Plane P, FlightInfo I WHERE F.fnum = I.flight_id AND I.flight_id = P.id AND F.fnum = " + fnum;
			List<List<String>> seatsResult = esql.executeQueryAndReturnResult(seatsQuery);
			int seatsLeft = Integer.parseInt(seatsResult.get(0).get(0));
			if(seatsLeft == 0)
			{
				System.out.print("\033[1;33m"); // bold yellow
				System.out.print("\tSorry, the flight you want to book is sold out. Would you like to be waitlisted?(Y/N): ");
				System.out.print("\033[0m"); // reset color
			}
			while(seatsLeft == 0)
			{
				System.out.print("\033[1;36m"); // bold cyan
				userInput = in.readLine();
				System.out.print("\033[0m"); // reset color
				upperuserInput = userInput.toUpperCase();

				// add to the waitlist.
				if(upperuserInput.equals("Y"))
				{
					String waitlistQuery = "INSERT INTO Reservation VALUES(" + rnum + ", " + cid + ", " + fnum + "," + "'W');";
					esql.executeUpdate(waitlistQuery);
					System.out.print("\033[1;36m"); // bold cyan
					System.out.println("\tCongratulations, you have been added to the waitlist!");
					System.out.print("\033[0m"); // reset color
					break;
				}
				else if(upperuserInput.equals("N"))
				{
					System.out.print("\033[1;36m"); // bold cyan
					System.out.println("\tGoodbye! Have a good day!");
					System.out.print("\033[0m"); // reset color
					break;
				}
				else
				{
					System.out.print("\033[101m"); // red background
					System.out.print("\033[1;37m"); // bold white
					System.out.print("\tSorry, your input is invalid.");
					System.out.println("\033[0m"); // reset color
				}

			}

			// add to the reservation.
			if(seatsLeft > 0)
			{
				String reserveQuery = "INSERT INTO Reservation VALUES(" + rnum + 1 + ", " + cid + ", " + fnum + "," + "'R');";
				esql.executeUpdate(reserveQuery);
				System.out.print("\033[1;36m"); // bold cyan
				System.out.println("\tCongratulations, your seat for this flight has been reserved!");
				System.out.print("\033[0m"); // reset color

				// update seats sold for this flight.
				String soldQuery = "SELECT num_sold FROM Flight WHERE Flight.fnum = " + fnum;
				List<List<String>> soldNum = esql.executeQueryAndReturnResult(soldQuery);
				int seatsSold = Integer.parseInt(soldNum.get(0).get(0));
				seatsSold = seatsSold + 1;
				esql.executeUpdate("UPDATE Flight SET num_sold = " + seatsSold + " WHERE fnum = " + fnum + ";");
				System.out.print("\033[1;36m"); // bold cyan
				System.out.println("\tCongratulations, your reservation has been confirmed!");
				System.out.print("\033[0m"); // reset color
			}
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
		}
	}



	public static void ListNumberOfAvailableSeats(DBproject esql) {//6
		// For flight number and date, find the number of availalbe seats (i.e. total plane capacity minus booked seats )
		// Don't need departure date.
		int userflightNum;

		// prompt user for the fight number.
		do
		{
			System.out.print("\033[1;31m"); // bold red
			System.out.print("\tPlease enter the flight number: ");
			System.out.print("\033[0m"); // reset color
			try
			{
				System.out.print("\033[1;36m"); // bold cyan
				userflightNum = Integer.parseInt(in.readLine());
				System.out.print("\033[0m"); // reset color
				break;
			}
			catch(Exception e)
			{
				System.out.print("\033[101m"); // red background
				System.out.print("\033[1;37m"); // bold white
				System.out.print("\tSorry, your input is invalid.");
				System.out.println("\033[0m"); // reset color
				continue;
			}
		} while(true);

		try
		{
			String seatsQuery = "SELECT P.seats - F.num_sold FROM Flight F, Plane P, FlightInfo I WHERE F.fnum = I.flight_id AND I.flight_id = P.id AND F.fnum = " + userflightNum;
			List<List<String>> seatsResult = esql.executeQueryAndReturnResult(seatsQuery);
			int seatsLeft = Integer.parseInt(seatsResult.get(0).get(0));
			System.out.print("\033[1;36m"); // bold cyan
			System.out.println("\tThere are " + seatsLeft + " seates available for this flight.");
			System.out.print("\033[0m"); // reset color
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
		}

	}

	public static void ListsTotalNumberOfRepairsPerPlane(DBproject esql) {//7
		// Count number of repairs per planes and list them in descending order
		try
		{
			String query = "SELECT plane.id, (SELECT COUNT(*) FROM Repairs R WHERE R.plane_id = plane.id) AS RepairCount FROM Plane ORDER BY RepairCount DESC;";
			System.out.print("\033[1;36m"); // bold cyan
			esql.executeQueryAndPrintResult(query);
			System.out.print("\033[0m"); // reset color
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
		}

	}

	public static void ListTotalNumberOfRepairsPerYear(DBproject esql) {//8
		// Count repairs per year and list them in ascending order
		try
		{
			String query = "SELECT EXTRACT(year FROM repair_date) AS Year, COUNT(*) FROM Repairs GROUP BY EXTRACT(year FROM repair_date) ORDER BY count ASC;";
			System.out.print("\033[1;36m"); // bold cyan
			esql.executeQueryAndPrintResult(query);
			System.out.print("\033[0m"); // reset color
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
		}
	}

	public static void FindPassengersCountWithStatus(DBproject esql) {//9
		// Find how many passengers there are with a status (i.e. W,C,R) and list that number.
		// Only consider status W and R.
		int userflightNum;
		String status;
		int passNum;

		// prompt user for the flight number.
		do
		{
			System.out.print("\033[1;31m"); // bold red
			System.out.print("Please enter the flight number: ");
			System.out.print("\033[0m"); // reset color
			try
			{
				System.out.print("\033[1;36m"); // bold cyan
				userflightNum = Integer.parseInt(in.readLine());
				System.out.print("\033[0m"); // reset color
				break;
			}
			catch(Exception e)
			{
				System.err.println(e.getMessage());
			}
		} while(true);

		// prompt user for the status.
		do
		{
			System.out.print("\033[1;31m"); // bold red
			System.out.print("Please enter the status you wish to check(R = Reserved, W = Waitlisted, C = Confirmed): ");
			System.out.print("\033[0m"); // reset color
			System.out.print("\033[1;36m"); // bold cyan
			Scanner sc = new Scanner(System.in);
			System.out.print("\033[0m"); // reset color
			status = sc.nextLine();
			System.out.print("\033[1;36m"); // bold cyan
			System.out.println("Status: " + status);
			System.out.print("\033[0m"); // reset color
			if((!status.equals("R")) && (!status.equals("W")) && (!status.equals("C")))
			{
				System.out.print("\033[101m"); // red background
				System.out.print("\033[1;37m"); // bold white
				System.out.print("Sorry, your input is invalid.");
				System.out.println("\033[0m"); // reset color
			}
			else
			{
				break;
			}
		} while(true);

		// get the number of passengers with status.
		try
		{
			// get the number of passenger with status Reserved.
			if(status.equals("R"))
			{
				System.out.print("\033[1;36m"); // bold cyan
				System.out.println("Number of Reservations: ");
			  System.out.print("\033[0m"); // reset color
				String queryA = "SELECT COUNT(*) ReservationCount FROM Reservation WHERE Reservation.fid = " + userflightNum + "AND Reservation.status = 'R'";
				System.out.print("\033[1;36m"); // bold cyan
				esql.executeQueryAndPrintResult(queryA);
				System.out.print("\033[0m"); // reset color
			}
			// get the number of passenger with status Confirmed.
			else if(status.equals("C"))
			{
				System.out.print("\033[1;36m"); // bold cyan
				System.out.println("Number of Confirmation: ");
				System.out.print("\033[0m"); // reset color
				String queryB = "SELECT COUNT(*) ConfirmationCount FROM Reservation WHERE Reservation.fid = " + userflightNum + "AND Reservation.status = 'C'";
				System.out.print("\033[1;36m"); // bold cyan
				esql.executeQueryAndPrintResult(queryB);
				System.out.print("\033[0m"); // reset color
			}
			// get the number of passenger with status Waitlisted.
			else if(status.equals("W"))
			{
				System.out.print("\033[1;36m"); // bold cyan
				System.out.println("Number of Waitlisted: ");
				System.out.print("\033[0m"); // reset color
				String queryC = "SELECT COUNT(*) WaitlistCount FROM Reservation WHERE Reservation.fid = " + userflightNum + "AND Reservation.status = 'W'";
				System.out.print("\033[1;36m"); // bold cyan
				esql.executeQueryAndPrintResult(queryC);
				System.out.print("\033[0m"); // reset color
			}
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
		}
	}
}

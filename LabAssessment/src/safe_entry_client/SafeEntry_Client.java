package safe_entry_client;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import safe_entry_server.*;

public class SafeEntry_Client extends java.rmi.server.UnicastRemoteObject implements RMIClientIntf{
	private static Scanner scan = new Scanner(System.in);
	static String user_dir=System.getProperty("user.dir");
	static Path records_dir=Paths.get(user_dir,"src","safe_entry_client","client_records");
	static JSONArray user_details=new JSONArray();
	static safe_entry se;
	public SafeEntry_Client() throws RemoteException {

	}

	public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException {
		// TODO Auto-generated method stub
		
		// Connection to server
		
		String reg_host = "localhost";
		int reg_port = 1099;
		if (args.length == 1) {
			reg_port = Integer.parseInt(args[0]);
		} else if (args.length == 2) {
			reg_host = args[0];
			reg_port = Integer.parseInt(args[1]);
		}
		try {
			SafeEntry_Client sc = new SafeEntry_Client();
			se=(safe_entry)Naming.lookup("rmi://" + reg_host + ":" + reg_port + "/SafeEntryService");
			System.out.println("Connected to server");
		JSONObject user=new JSONObject();
		JSONObject user_record=new JSONObject();

			
		for (;;) {
			System.out.println("***************************************");
			System.out.println("Trace Together Contact Tracing");
			System.out.println("***************************************");
			System.out.println();
			System.out.println("Are you a user or an officer?");
			System.out.println("1. User");
			System.out.println("2. Officer");
			int user_type=getChoice();
			if(user_type==1) {
				System.out.println("How do you want to check in / check out today?");
				System.out.println("1. Individually");
				System.out.println("2. Group");
				System.out.println("3. View History");
				
			
				user_details=readFile();
				int choice = getChoice();
				if (choice == 1) {

					System.out.println("Enter your name: ");
					String input_name = scan.nextLine();
					System.out.println(input_name);
					System.out.println("Enter your NRIC: ");
					String input_nric = scan.nextLine();
					System.out.println("Enter the location name: ");
					String input_location = scan.nextLine();
					System.out.println("Enter '1' to check in or '2' to check-out: ");
					int checkin_checkout =Integer.parseInt(scan.nextLine());
					// Write in JSON file
					JSONObject entry_details = new JSONObject();
					entry_details.put("name", input_name);
					entry_details.put("nric", input_nric);
					se.informUsers(sc, input_nric);
					entry_details.put("location", input_location);
					if (checkin_checkout == 1) {
						entry_details.put("check_in", LocalDateTime.now().toString());
						entry_details.put("check_out", "null");
						se.checkIn(entry_details);
						user_details.add(entry_details);
						
						
						if(writeFile()) {
							System.out.println("Check in  information saved");
						}
					} else {
						String check_out_datetime=LocalDateTime.now().toString();
						entry_details.put("check_out",check_out_datetime);
						se.checkOut(entry_details);
						user_details=readFile();
						for(int i=user_details.size()-1;i>0;i--) {
							user_record=(JSONObject)user_details.get(i) ;

							if(user_record.get("nric").equals(input_nric) && user_record.get("check_out").equals("null") && user_record.get("location").equals(input_location)) {
								user_record.put("check_out", check_out_datetime);
								System.out.println("User "+user_record.get("name")+" is checking out...");
								break;
							}
						}
						
						if(writeFile()) {
							System.out.println("Check out information saved");
						}
					}
					
	
				} else if (choice==2){
					JSONArray user_info=new JSONArray();

					System.out.println("Enter number of people you want to check-in / check-out with: ");
					int number = Integer.parseInt(scan.nextLine());
					for (int i = 0; i < number; i++) {
						JSONObject entry_details = new JSONObject();
						System.out.println("Enter your name " + i+1 + ": ");
						String input_name = scan.nextLine();
						System.out.println("Enter your NRIC " + i+1 + ": ");
						String input_nric = scan.nextLine();
						entry_details.put("name", input_name);
						entry_details.put("nric", input_nric);
						user_info.add(entry_details);
						System.out.println(entry_details.get("name"));
						se.informUsers(sc, input_nric);
						
					}
					System.out.println("Enter the location name: ");
					String input_location = scan.nextLine();
					System.out.println("Enter '1' to check in or '2' to check-out: ");
					int checkin_checkout =Integer.parseInt(scan.nextLine());
					
					// Write in JSON file
					if (checkin_checkout == 1) {
						for(Object o:user_info) {
							user=(JSONObject) o;
							user.put("location", input_location);
							user.put("check_in", LocalDateTime.now().toString());
							user.put("check_out", "null");
							se.checkIn(user);
							user_details.add(user);
						}
						if(writeFile()) {
							System.out.println("Check in information saved");
						}
					}
					else if (checkin_checkout==2){
						for(Object o:user_info) {
							user=(JSONObject) o;
							user.put("location", input_location);
							String check_out_time=LocalDateTime.now().toString();
							for(int i=user_details.size()-1;i>0;i--) {
								user_record=(JSONObject) user_details.get(i);
								if(user.get("nric").equals(user_record.get("nric")) && user_record.get("location").equals(input_location) && user_record.get("check_out").equals("null")) {
									System.out.println("Found user");
									user_record.put("check_out",check_out_time);
									System.out.println("User "+user.get("name")+" is checking out...");
									break;
								}
								
							}
							user.put("check_out", check_out_time);
							
							se.checkOut(user);
							
						}
						if(writeFile()) {
							System.out.println("Check out information saved");
						}
					}
					
					
				}
				else if (choice==3) {
					
					System.out.println("Enter your nric:");
					String nric=scan.nextLine();
//					updateFile(nric);
					JSONArray local_data=readFile();
		
					JSONArray user_data=getUserRecords(nric,local_data);
					System.out.println(user_data);
					
					
				}
			}
			else if (user_type==2) {
				System.out.print("Enter username:");
				String user_name=scan.nextLine();
				
				System.out.print("Enter password:");
				String password=scan.nextLine();
				if(se.checkLogin(user_name, password)) {
					System.out.print("Enter location name for covid alert:");
					String location=scan.nextLine();
					System.out.print("Enter date of covid alert (yyyy-mm-dd):");
					String covid_alert_date=scan.nextLine();
					System.out.print("Enter time of covid alert (HH:mm):");
					String covid_alert_time=scan.nextLine();
					String covid_alert_dateTime=covid_alert_date+" "+covid_alert_time;
					boolean result=se.declareLocationUnsafe(location, covid_alert_dateTime);
					if(result) {
						System.out.println("Covid alert successfully added to records!");
					}
		
				}
				else {
					System.out.println("Login failed!");
				}
				
				
			}
		}
		} catch (MalformedURLException murle) {
			System.out.println();
			System.out.println("MalformedURLException");
			System.out.println(murle);
		} catch (RemoteException re) {
			System.out.println();
			System.out.println("RemoteException");
			System.out.println(re);
		} catch (NotBoundException nbe) {
			System.out.println();
			System.out.println("NotBoundException");
			System.out.println(nbe);
		}
	}
	public static boolean updateFile(String nric) {
		try {
			JSONArray records=se.getInfo(nric);
			JSONArray user_local_data=readFile();
			JSONArray add_records=new JSONArray();
			for(Object o:user_local_data) {
				JSONObject jo=(JSONObject) o;
				for(Object c:records) {
					JSONObject jor=(JSONObject) c;
					
					if(!jo.get("check_out").toString().contains(jor.get("check_in").toString().substring(0, 18))) {
						add_records.add(jor);
						
					}
				}
			}
			user_local_data.addAll(add_records);

			FileWriter file = new FileWriter(Paths.get(records_dir.toString(),"Data.json").toString());
				// We can write any JSONArray or JSONObject instance to the file

			file.write(user_local_data.toString());
			file.flush();
			file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return false;
		
	}
	public static JSONArray getUserRecords(String nric,JSONArray records) {
		JSONArray users=new JSONArray();
		for(Object o:records) {
			JSONObject jobj=(JSONObject) o;
			if(jobj.get("nric").equals(nric)) {
				users.add(jobj);
				
			}
		}
		return users;
	}
	
	public static JSONArray readFile() {
		JSONParser jparser=new JSONParser();
		JSONArray users=new JSONArray();
		File file_data=new File(Paths.get(records_dir.toString(),"Data.json").toString());
		if (file_data.length() != 0) {
			try {
				FileReader fr = new FileReader(Paths.get(records_dir.toString(),"Data.json").toString());
				users = (JSONArray) jparser.parse(fr);
			} catch (IOException | ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return users;
	}
	public static boolean writeFile() {
		boolean written=false;
		// Write JSON file
		try (FileWriter file = new FileWriter(Paths.get(records_dir.toString(),"Data.json").toString())) {
			// We can write any JSONArray or JSONObject instance to the file
				file.write(user_details.toJSONString());
			file.flush();
			written=true;
		} catch (IOException e) {
			System.out.println("Unable to write into json file");
			e.printStackTrace();
		}
		return written;
	}

	public static int getChoice() {
		try {
			System.out.print("Enter choice:");
			int input = Integer.parseInt(scan.nextLine());
			if (input < 1 || input > 3)
				throw new Exception();
			return input;
		} catch (Exception e) {
			System.out.print("ERROR: Please input a number from 1 to 2");
			
		}
		return 0;
	}

	public static int checkin_checkout() {
		try {
			System.out.print("Enter choice:");
			int input = Integer.parseInt(scan.nextLine());
			if (input < 1 || input > 2)
				throw new Exception();
			return input;
		} catch (Exception e) {
			System.out.print("ERROR: Please input a number from 1 to 2");
			
		}
		return 0;
	}

	@Override
	public void callBack(JSONArray users, String latest_declared_time) throws RemoteException {
		JSONParser parser=new JSONParser();
		JSONObject user=new JSONObject();
		try {
			FileReader fr= new FileReader(Paths.get(records_dir.toString(),"Data.json").toString());
			if(!fr.equals(null)) {
				JSONArray user_records=(JSONArray) parser.parse(fr);
				for(Object o:user_records) {
					for(Object u:users) {
						JSONObject user_record=(JSONObject) o;
						JSONObject user_affected=(JSONObject) u;
						if(user_affected.get("nric").equals(user_record.get("nric"))){
							System.out.println("User " + user_record.get("nric") + " who visited "+ user_record.get("location") 
							+ " at time " + latest_declared_time+ " was affected.");
							
						}
					}
				}
			}
				
			
			} catch (IOException | ParseException e) {
				e.printStackTrace();
			}
			
		}
	}

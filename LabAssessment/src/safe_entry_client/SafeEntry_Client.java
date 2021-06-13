package safe_entry_client;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
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
			safe_entry se=(safe_entry)Naming.lookup("rmi://" + reg_host + ":" + reg_port + "/SafeEntryService");
			System.out.println("Connected to server");

			
		for (;;) {
			System.out.println("***************************************");
			System.out.println("Trace Together Contact Tracing");
			System.out.println("***************************************");
			System.out.println();
			System.out.println("How do you want to check in / check out today?");
			System.out.println("1. Individually");
			System.out.println("2. Group");
			JSONParser jparser=new JSONParser();
			File file_data=new File(Paths.get(records_dir.toString(),"Data.json").toString());
			if(file_data.length()!=0) {
				try(FileReader file = new FileReader(Paths.get(records_dir.toString(),"Data.json").toString())){
						user_details=(JSONArray) jparser.parse(file);
					
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
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

				} else {
					entry_details.put("check_out",LocalDateTime.now().toString());
					se.checkOut(entry_details);
				}
				// Write JSON file
				try (FileWriter file = new FileWriter(Paths.get(records_dir.toString(),"Data.json").toString())) {
					// We can write any JSONArray or JSONObject instance to the file
					user_details.add(entry_details);
					file.write(user_details.toJSONString());
					file.flush();
				} catch (IOException e) {
					System.out.println("Unable to write into json file");
					e.printStackTrace();
				}

			} else {
				JSONArray user_info=new JSONArray();
				JSONObject entry_details = new JSONObject();
				System.out.println("Enter number of people you want to check-in / check-out with: ");
				int number = Integer.parseInt(scan.nextLine());
				for (int i = 0; i < number; i++) {
					System.out.println("Enter your name " + i+1 + ": ");
					String input_name = scan.nextLine();
					System.out.println("Enter your NRIC " + i+1 + ": ");
					String input_nric = scan.nextLine();
					user_info.add(entry_details.put("name", input_name));
					user_info.add(entry_details.put("nric", input_nric));
					se.informUsers(sc, input_nric);
					
				}
				System.out.println("Enter the location name: ");
				String input_location = scan.nextLine();
				System.out.println("Enter '1' to check in or '2' to check-out: ");
				int checkin_checkout =Integer.parseInt(scan.nextLine());
				// Write in JSON file
				for(Object o:user_info) {
					JSONObject user=(JSONObject) o;
					user.put("location", input_location);
					if (checkin_checkout == 1) {
						user.put("check_in", LocalDateTime.now());
						user.put("check_out", "null");
						se.checkIn(user);

					} else {
						user.put("check_out", LocalDateTime.now());
						se.checkOut(user);
					}
				}

				// Write JSON file
				try (FileWriter file = new FileWriter(Paths.get(records_dir.toString(),"Data.json").toString())) {
					// We can write any JSONArray or JSONObject instance to the file
					user_details.add(user_info);
					file.write(user_details.toJSONString());
					file.flush();
				} catch (IOException e) {
					System.out.println("Unable to write into json file");
					e.printStackTrace();
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

	private static Object getTime() {
		// TODO Auto-generated method stub
		
		
		return null;
	}

	public static int getChoice() {
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

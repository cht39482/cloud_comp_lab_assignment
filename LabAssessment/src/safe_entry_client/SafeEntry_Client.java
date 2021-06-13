package safe_entry_client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.json.simple.JSONObject;

import safe_entry_server.JSONArray;
import safe_entry_server.JSONParser;
import safe_entry_server.ParseException;
import safe_entry_server.RMIClientIntf;
import safe_entry_server.safe_entry;
import safe_entry_server.safe_entry_impl;

public class SafeEntry_Client extends java.rmi.server.UnicastRemoteObject implements RMIClientIntf {
	private static Scanner scan = null;

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
			safe_entry_impl se=(safe_entry_impl)Naming.lookup("rmi://" + reg_host + ":" + reg_port + "/SafeEntryService");
			System.out.println("Connected to server");
			
		for (;;) {
			System.out.println("***************************************");
			System.out.println("Trace Together Contact Tracing");
			System.out.println("***************************************");
			System.out.println();
			System.out.println("How do you want to check in / check out today?");
			System.out.println("1. Individually");
			System.out.println("2. Group");
			
			int choice = getChoice();
			if (choice == 1) {
				System.out.println("Enter your name: ");
				String input_name = scan.toString();
				System.out.println("Enter your NRIC: ");
				String input_nric = scan.toString();
				System.out.println("Enter the location name: ");
				String input_location = scan.toString();
				System.out.println("Enter '1' to check in or '2' to check-out: ");
				int checkin_checkout = scan.nextInt();
				// Write in JSON file
				JSONObject entry_details = new JSONObject();
				entry_details.put("name", input_name);
				entry_details.put("nric", input_nric);
				se.trackCovid(sc, input_nric);
				entry_details.put("location", input_location);
				if (checkin_checkout == 1) {
					
					entry_details.put("check_in", getTime());
					entry_details.put("check_out", "null");
					se.checkIn(entry_details);
					

				} else {
					entry_details.put("check_out", getTime());
					se.checkOut(entry_details);
				}
				// Write JSON file
				try (FileWriter file = new FileWriter("Data.json")) {
					// We can write any JSONArray or JSONObject instance to the file
					file.write(entry_details.toJSONString());
					file.flush();
				} catch (IOException e) {
					System.out.println("Unable to write into json file");
					e.printStackTrace();
				}

			} else {
				System.out.println("Enter number of people you want to check-in / check-out with: ");
				int number = scan.nextInt();
				for (int i = 0; i < number; i++) {
					System.out.println("Enter your name" + number + ": ");
					String input_name = scan.toString();
					System.out.println("Enter your NRIC" + number + ": ");
					String input_nric = scan.toString();
					System.out.println("Enter the location name: ");
					String input_location = scan.toString();
					System.out.println("Enter '1' to check in or '2' to check-out: ");
					int checkin_checkout = scan.nextInt();
					// Write in JSON file
					JSONObject entry_details = new JSONObject();
					entry_details.put("name", input_name);
					entry_details.put("nric", input_nric);
					se.trackCovid(sc, input_nric);
					entry_details.put("location", input_location);
					if (checkin_checkout == 1) {
						entry_details.put("check_in", getTime());

					} else {
						entry_details.put("check_out", getTime());
					}
					// Write JSON file
					try (FileWriter file = new FileWriter("Data.json")) {
						// We can write any JSONArray or JSONObject instance to the file
						file.write(entry_details.toJSONString());
						file.flush();
					} catch (IOException e) {
						System.out.println("Unable to write into json file");
						e.printStackTrace();
					}
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
			int input = scan.nextInt();
			if (input < 1 || input > 2)
				throw new Exception();
			return input;
		} catch (Exception e) {
			System.out.print("ERROR: Please input a number from 1 to 2");
			return getChoice();
		}
	}

	public static int checkin_checkout() {
		try {
			System.out.print("Enter choice:");
			int input = scan.nextInt();
			if (input < 1 || input > 2)
				throw new Exception();
			return input;
		} catch (Exception e) {
			System.out.print("ERROR: Please input a number from 1 to 2");
			return getChoice();
		}
	}

	@Override
	public void callBack(String nric) throws RemoteException {
		JSONParser parser=new JSONParser();
		JSONArray users=new JSONArray();
		JSONObject user=new JSONObject();
		String filename="Data.json";
		if(!nric.equals("null")) {
			try {
				FileReader fr= new FileReader(filename);
				if(!fr.equals(null)) {
					users=(JSONArray) parser.parse(fr);
					for(Object o:users) {
						JSONObject user_record=(JSONObject) o;
						if(nric.equals(user_record.get("nric"))){
							user=user_record;
							
						}
					}
				}
			
			} catch (IOException | ParseException e) {
				e.printStackTrace();
			}
			System.out.println("User " + user.get("name") + " with nric "+ user.get("nric") + " has been affected");
		}
	}
}
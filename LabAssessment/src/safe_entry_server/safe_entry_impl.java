package safe_entry_server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class safe_entry_impl extends java.rmi.server.UnicastRemoteObject implements safe_entry{
	private RMIClientIntf c;
	
	protected safe_entry_impl() throws RemoteException {
		super();
	}
	ReadWriteLock file_lock=new ReentrantReadWriteLock();
	ReadWriteLock declared_covid_locations_lock=new ReentrantReadWriteLock();
	Lock write_lock=file_lock.writeLock();
	Lock read_lock=file_lock.readLock();
	Lock declared_covid_write=declared_covid_locations_lock.writeLock();
	Lock declared_covid_read=declared_covid_locations_lock.readLock();
	boolean write_success=true;
	static String user_dir=System.getProperty("user.dir");
	static Path records_dir=Paths.get(user_dir,"src","safe_entry_server","server_records");
	boolean watch_file=true;
	static Long lastRead = 0L;
	
//	String latest_declared_location="";
//	String latest_declared_datetime="";
//	JSONArray locations_declared=new JSONArray();
//	boolean file_change=false;

	@Override
	public boolean checkIn(JSONObject user) throws RemoteException {
		JSONParser parser=new JSONParser();
		JSONArray users=new JSONArray();
		String filename=Paths.get(records_dir.toString(),"safe_entry_history.json").toString();
		System.out.println("Writing to " + filename);
		
		try {
			write_lock.lock();
			write_success=false;
			FileReader fr= new FileReader(filename);
			if(!fr.equals(null)) {
				users=(JSONArray) parser.parse(fr);
				
			}
			System.out.println("Reached here");
			users.add(user);
			
//			System.out.print(users);
			FileWriter file = new FileWriter(filename);
			file.write(users.toJSONString());
			file.flush();
			file.close();
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			write_lock.unlock();
			return false;
		}
		finally {
			write_success=true;
			write_lock.unlock();
		}
		return true;
	}

	@Override
	public boolean checkOut(JSONObject user) throws RemoteException {
		// TODO Auto-generated method stub
		JSONParser parser=new JSONParser();
		JSONArray users=new JSONArray();
		boolean foundUser=false;
		String filename=Paths.get(records_dir.toString(),"safe_entry_history.json").toString();
	
		if(!user.get("check_out").equals("null")) {
			try {
				write_lock.lock();
				FileReader fr= new FileReader(filename);
				if(!fr.equals(null)) {
					users=(JSONArray) parser.parse(fr);
					System.out.print(users.get(users.size()-1));
					for(int i=users.size()-1;i>0;i--) {
						
						JSONObject user_record=(JSONObject)users.get(i);
						if(user_record.get("nric").equals(user.get("nric"))&& user_record.get("location").equals(user.get("location"))) {
							System.out.println(user_record.get("nric"));
							
							user_record.put("check_out",java.time.LocalDateTime.now().toString());
							foundUser=true;
							break;
						}
					}
	//				System.out.print(users);
					FileWriter file = new FileWriter(filename);
					file.write(users.toJSONString());
					file.flush();
					file.close();
				}
							
			} catch (IOException | ParseException e) {
				write_lock.unlock();
				e.printStackTrace();
				return false;
			}
			finally {
				write_lock.unlock();
			}
		}
		else {
			System.out.print("There's no check out time");
		}
		return foundUser;
	}



	@Override
	public boolean checkIn(JSONArray users) throws RemoteException {
		// TODO Auto-generated method stub
		JSONParser parser=new JSONParser();
		JSONArray user_records=new JSONArray();
		boolean updated_file=false;
		String filename=Paths.get(records_dir.toString(),"safe_entry_history.json").toString();
	
		if(!users.equals(null)) {
			try {
				write_lock.lock();
				FileReader fr= new FileReader(filename);
				user_records=(JSONArray) parser.parse(fr);
				user_records.addAll(users);
//				System.out.print(user_records);
				FileWriter file = new FileWriter(filename);
				file.write(user_records.toJSONString());
				file.flush();
				file.close();
				updated_file=true;
			} catch (IOException | ParseException e) {
				write_lock.unlock();
				e.printStackTrace();
				return false;
			}
			finally {
				write_lock.unlock();
			}
		}
		else {
			System.out.print("There's no users");
			return false;
		}
		return updated_file;
	}

	@Override
	public boolean checkOut(JSONArray users) throws RemoteException {
		JSONParser parser=new JSONParser();
		JSONArray user_records=new JSONArray();
		boolean foundUser=false;
//		String filename="C:\\Users\\cht47\\OneDrive - Singapore Institute Of Technology\\Lessons\\Year 2 Sem 3\\Cloud Computing\\Lab_Assessment\\safe_entry_history.json";
		String filename=Paths.get(records_dir.toString(),"safe_entry_history.json").toString();
		if(!users.equals(null)) {
			try {
				write_lock.lock();
				FileReader fr= new FileReader(filename);
				if(!fr.equals(null)) {
					user_records=(JSONArray) parser.parse(fr);    
					System.out.print(users.get(users.size()-1));
					for(Object o:users) {
						for(int i=user_records.size()-1;i>0;i--) {
							JSONObject user=(JSONObject) o;
							JSONObject user_record=(JSONObject) user_records.get(i);
							if(user_record.get("nric").equals((user.get("nric")))) {
								user_record.put("check_out",user.get("check_out"));
								foundUser=true;
							}
						}
					}
				
//				System.out.print(users);
				FileWriter file = new FileWriter(filename);
				file.write(user_records.toJSONString());
				file.flush();
				file.close();
				}
			} catch (IOException | ParseException e) {
				write_lock.unlock();
				e.printStackTrace();
				return false;
			}
			finally {
				write_lock.unlock();
			}
		}
		else {
			System.out.print("There's no users");
			return false;
		}
		return foundUser;
	}




	@Override
	public JSONArray getInfo() throws RemoteException {
		
		// TODO Auto-generated method stub
		JSONParser parser=new JSONParser();
		JSONArray users=new JSONArray();
		String filename="C:\\Users\\cht47\\OneDrive - Singapore Institute Of Technology\\Lessons\\Year 2 Sem 3\\Cloud Computing\\Lab_Assessment\\safe_entry_history.json";
		try {
			read_lock.lock();
			FileReader fr= new FileReader(filename);
			if(!fr.equals(null)) {
				users=(JSONArray) parser.parse(fr);
			}
		} catch (IOException | ParseException e) {
			write_lock.unlock();
			e.printStackTrace();
			return null;
		}
		finally {
			read_lock.unlock();
		}
		
		return users;
	}

	@Override
	public JSONArray getInfo(String nric) throws RemoteException {
		JSONParser parser=new JSONParser();
		JSONArray users=new JSONArray();
		String filename="C:\\Users\\cht47\\OneDrive - Singapore Institute Of Technology\\Lessons\\Year 2 Sem 3\\Cloud Computing\\Lab_Assessment\\safe_entry_history.json";
		JSONArray records=new JSONArray();
		if(!nric.equals("null")) {
			try {
				read_lock.lock();
				FileReader fr= new FileReader(filename);
				if(!fr.equals(null)) {
					users=(JSONArray) parser.parse(fr);
					for(Object o:users) {
						JSONObject user_record=(JSONObject) o;
						if(nric.equals(user_record.get("nric"))){
							records.add(user_record);
						}
					}
				}
			
			} catch (IOException | ParseException e) {
				write_lock.unlock();
				e.printStackTrace();
				return null;
			}
			finally {
				read_lock.unlock();
			}
		}
		return records;
			
	}

	@Override
	public boolean checkLogin(String name, String password) throws RemoteException {
		JSONParser parser=new JSONParser();
		JSONArray officers=new JSONArray();
		String filename=Paths.get(records_dir.toString(),"user_officers.json").toString();
		boolean loggedIn=false;
		try {
			FileReader fr= new FileReader(filename);
				officers=(JSONArray) parser.parse(fr);
				for(Object o:officers) {
					JSONObject officer=(JSONObject) o;
					if(officer.get("name").equals(name) && officer.get("password").equals(password)) {
						System.out.print("Officer account found! Login success");
						loggedIn=true;
						break;
					}
				}
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return loggedIn;
	}

	@Override
	public boolean declareLocationUnsafe(String locationName, String dateTime) throws RemoteException  {
		JSONArray covid_records=new JSONArray();
		JSONParser parser=new JSONParser();
		JSONObject covid_record=new JSONObject();
		covid_record.put("location", locationName);
		covid_record.put("date_time", dateTime);
		String filename=Paths.get(records_dir.toString(),"declared_covid_locations.json").toString();
		boolean added_covid_record=false;
		try {
			
			FileReader fr= new FileReader(filename);
			
				covid_records=(JSONArray) parser.parse(fr);
			
			covid_records.add(covid_record);
			declared_covid_write.lock();
			FileWriter file = new FileWriter(filename);
			file.write(covid_records.toJSONString());
			file.flush();
			file.close();
			
			added_covid_record=true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			declared_covid_write.unlock();
		}
		return added_covid_record;
	}
	
//	public boolean trackCovid(RMIClientIntf client, String nric) throws RemoteException {
//		
//		JSONArray affected_users=getListAffectedUsers();
//		boolean covid_pos=false;
//		for(Object o:affected_users) {
//			JSONObject user=(JSONObject)o;
//			if(user.get("nric").equals(nric)) {
//				System.out.print("User has been detected as being affected.");
//				covid_pos=true;
//			}
//		}
//		return covid_pos;
//	}
	public boolean check_affected_date(String datetime_user, String datetime_declared) {
		LocalDateTime odt_user = LocalDateTime.parse(datetime_user);
		LocalDateTime odt_officer = LocalDateTime.parse(datetime_declared);
		return Duration.between( odt_officer, odt_user).toDays()<=14;
	}
	public JSONArray getListAffectedUsers(String latest_declared_location, String latest_declared_datetime) {
		
		JSONArray users=new JSONArray();
		JSONArray users_affected=new JSONArray();
		JSONParser parser=new JSONParser();
//		String filename="C:\\Users\\cht47\\OneDrive - Singapore Institute Of Technology\\Lessons\\Year 2 Sem 3\\Cloud Computing\\Lab_Assessment\\safe_entry_history.json";
		try {
			String filename=Paths.get(records_dir.toString(),"safe_entry_history.json").toString();
			FileReader fr=  new FileReader(filename);
			users=(JSONArray) parser.parse(fr);
			for(Object o:users) {
				JSONObject user=(JSONObject) o;
				if(user.get("location").equals(latest_declared_location) && check_affected_date((String)user.get("check_in"), latest_declared_datetime)) {
					System.out.print("Found user is being affected.");
					users_affected.add(user);
					
				}
			}
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return users_affected;
	}
	public void stopWatcher() {watch_file=false;}
	public void informUsers(RMIClientIntf client, String nric) throws RemoteException{
		c=client;
		
		Thread thread=new Thread(new Runnable(){
			
			@Override
			public void run() {
				Random rg = new Random();
				int timer = rg.nextInt(1000);
			    try {
					WatchService watcher=(WatchService) FileSystems.getDefault().newWatchService();
					records_dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
					JSONArray covid_records=new JSONArray();
					JSONObject covid_record=new JSONObject();
					JSONParser parser=new JSONParser();
					String filename=Paths.get(records_dir.toString(),"declared_covid_locations.json").toString();
					while(watch_file) {
						WatchKey key=null;
						try{
							key=watcher.take();
							System.out.println("Client registered with watcher");
						}
						catch(InterruptedException ie) {
							System.out.println("Thread interrupted; yielding");
							Thread.yield();
							continue;
						}
						for(WatchEvent<?> event : key.pollEvents()) {
							 WatchEvent<Path> event_path = (WatchEvent<Path>) event;
							 Path files=event_path.context();
							 if(files.endsWith("declared_covid_locations.json")) {
								FileReader fr;
								try {
									Long lastWriteTime = new File(Paths.get(records_dir.toString(),"declared_covid_locations.json").toString()).lastModified();
									System.out.println(lastWriteTime);
									System.out.println(lastRead);
								    if (!Objects.equals(lastWriteTime, lastRead))
								    {
										fr = new FileReader(filename);
										covid_records=(JSONArray) parser.parse(fr);
										covid_record=(JSONObject) covid_records.get(covid_records.size()-1);
										String latest_declared_datetime=(String) covid_record.get("date_time");
										String latest_declared_location=(String) covid_record.get("location");
										System.out.println(latest_declared_datetime);
//										System.out.println(lastWriteTime);
										System.out.print("Detected new records entered.");
										JSONArray affected_users=getListAffectedUsers(latest_declared_location,latest_declared_datetime);
										System.out.println(affected_users);
								        lastRead = lastWriteTime;
								        c.callBack(affected_users,latest_declared_datetime);
								    }

										
									
								} catch (IOException | ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} 

							 }
							
						}
						if(!key.reset()) {
							System.out.print("File cannot be found");
							break;
						}
					}
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
				
		});
		thread.start();
		return;

	}
	public void watchFile() throws RemoteException{
		Thread thread=new Thread(new Runnable(){
			
			@Override
			public void run() {
				Random rg = new Random();
				int timer = rg.nextInt(1000);
			    try {
			    	String filename=Paths.get(records_dir.toString(),"declared_covid_locations.json").toString();
					WatchService watcher=(WatchService) FileSystems.getDefault().newWatchService();
					records_dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);

					while(watch_file) {
						WatchKey key=null;
						try{key=watcher.take();
						System.out.println("Watching file");
						}
						
						catch(InterruptedException ie) {
							Thread.yield();
							System.out.println("Thread interrupted");
							continue;
						}
						for(WatchEvent<?> event : key.pollEvents()) {
							 WatchEvent<Path> event_path = (WatchEvent<Path>) event;
							 Path files=event_path.context();
							 if(files.endsWith("declared_covid_locations.json")) {
								FileReader fr;
								fr = new FileReader(Paths.get(records_dir.toString(),"declared_covid_locations.json").toString());
								System.out.println(filename);
									try {
										
											JSONArray covid_records=new JSONArray();
											JSONObject covid_record=new JSONObject();
											JSONParser parser=new JSONParser();
											covid_records=(JSONArray) parser.parse(fr);
										
										
									} catch (ParseException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
//									covid_record=(JSONObject) covid_records.get(1);
////									System.out.println( covid_record.get("nric"));
//									String latest_declared_datetime=(String) covid_record.get("date_time");
//									String latest_declared_location=(String) covid_record.get("location");
//									System.out.print("Detected new records entered.");
									//JSONArray affected_users=getListAffectedUsers(latest_declared_location,latest_declared_datetime);
//									Thread.sleep(timer);
								

								 System.out.println("File changed");
							 }
							 System.out.println(files);
							
						}
						if(!key.reset()) {
							System.out.print("File cannot be found");
							break;
						}
					}
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
				
		});
		thread.start();
		return;

	}
	public static void main(String args[]) {
		JSONParser jparser=new JSONParser();
		JSONObject user=new JSONObject();
//		JSONArray user1=new JSONArray();
		String userInfo="[{\"check_out\":\"null\",\"check_in\":\"2016-04-12T13:37:27+00:00\",\"name\":\"Jon\",\"location\":\"Hell\",\"nric\":\"S992222R\"},{\"check_out\":\"null\",\"check_in\":\"2016-04-12T13:37:27+00:00\",\"name\":\"Jonel\",\"location\":\"Hell\",\"nric\":\"S9922333R\"},{\"check_out\":\"null\",\"check_in\":\"2016-04-12T13:37:27+00:00\",\"name\":\"Jonel\",\"location\":\"Hell\",\"nric\":\"S9922333R\"},{\"check_out\":\"2021-06-10T14:03:34.343506900\",\"check_in\":\"2016-04-12T13:37:27+00:00\",\"name\":\"David\",\"location\":\"Canyon\",\"nric\":\"S9922535R\"}]";
		String userInfo1="[{\"check_out\":\"2016-04-12T13:37:27+00:00\",\"check_in\":\"2016-04-12T13:37:27+00:00\",\"name\":\"Jon\",\"location\":\"Hell\",\"nric\":\"S992222R\"},{\"check_out\":\"2016-04-12T13:37:27+00:00\",\"check_in\":\"2016-04-12T13:37:27+00:00\",\"name\":\"Jonel\",\"location\":\"Hell\",\"nric\":\"S9922333R\"},{\"check_out\":\"2016-04-12T13:37:27+00:00\",\"check_in\":\"2016-04-12T13:37:27+00:00\",\"name\":\"Jonel\",\"location\":\"Hell\",\"nric\":\"S9922333R\"},{\"check_out\":\"2021-06-10T14:03:34.343506900\",\"check_in\":\"2016-04-12T13:37:27+00:00\",\"name\":\"David\",\"location\":\"Canyon\",\"nric\":\"S9922535R\"}]";

				user.put("nric", "S9922535R");
		user.put("location","Canyon");
		user.put("name", "David");
		user.put("check_in", "2016-04-12T13:37:27+00:00");
		user.put("check_out", "null");
//		user1.put("location","Canyon");
//		user1.put("nric", "S9922535R");
//		user1.put("name", "David");
//		user1.put("check_in", "null");
//		user1.put("check_out", "2016-04-12T17:37:27+00:00");
		safe_entry_impl si;
		try {
//			user=(JSONObject) jparser.parse(user);
//			user1=(JSONArray) jparser.parse(userInfo1);
//			FileReader fr = new FileReader(Paths.get(records_dir.toString(),"declared_covid_locations.json").toString());
//			JSONArray covid_records=new JSONArray();
//			JSONObject covid_record=new JSONObject();
//			JSONParser parser=new JSONParser();
//			covid_records=(JSONArray) parser.parse(fr);
//			System.out.println(covid_records);
//			System.out.println(filename);
//			JSONObj1=(JSONArray)jparser.parse(fr);
//			System.out.print(JSONObj);
			si = new safe_entry_impl();
//			DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
//	                .ofPattern("yyyy-MM-ddTHH:mm:ss");
			
			LocalDateTime odt_user = LocalDateTime.parse(LocalDateTime.now().toString());
			System.out.println(LocalDateTime.now());
			System.out.println(records_dir.toString());
//			si.watchFile();
			si.checkIn(user);
//			si.checkOut(user1);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}


}

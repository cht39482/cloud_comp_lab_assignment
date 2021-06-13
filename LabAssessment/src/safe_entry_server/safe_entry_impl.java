package safe_entry_server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.rmi.RemoteException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
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
	boolean watch_file=true;
	String latest_declared_location="";
	String latest_declared_datetime="";
//	JSONArray locations_declared=new JSONArray();
//	boolean file_change=false;

	@Override
	public boolean checkIn(JSONObject user) throws RemoteException {
		JSONParser parser=new JSONParser();
		JSONArray users=new JSONArray();
		String filename="C:\\Users\\cht47\\OneDrive - Singapore Institute Of Technology\\Lessons\\Year 2 Sem 3\\Cloud Computing\\Lab_Assessment\\safe_entry_history.json";
		
		try {
			write_lock.lock();
			write_success=false;
			FileReader fr= new FileReader(filename);
			if(!fr.equals(null)) {
				users=(JSONArray) parser.parse(fr);
				
			}	
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
		String filename="C:\\Users\\cht47\\OneDrive - Singapore Institute Of Technology\\Lessons\\Year 2 Sem 3\\Cloud Computing\\Lab_Assessment\\safe_entry_history.json";
	
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
		String filename="C:\\Users\\cht47\\OneDrive - Singapore Institute Of Technology\\Lessons\\Year 2 Sem 3\\Cloud Computing\\Lab_Assessment\\safe_entry_history.json";
	
		if(!users.equals(null)) {
			try {
				write_lock.lock();
				FileReader fr= new FileReader(filename);
				if(fr.read()==0) {
					user_records=(JSONArray) parser.parse(fr);
					
				}	
				user_records.addAll(users);
//				System.out.print(user_records);
				FileWriter file = new FileWriter(filename);
				file.write(users.toJSONString());
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
		String filename="C:\\Users\\cht47\\OneDrive - Singapore Institute Of Technology\\Lessons\\Year 2 Sem 3\\Cloud Computing\\Lab_Assessment\\safe_entry_history.json";
	
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
		String filename="C:\\Users\\cht47\\OneDrive - Singapore Institute Of Technology\\Lessons\\Year 2 Sem 3\\Cloud Computing\\Lab_Assessment\\declared_covid_locations.json";
		boolean loggedIn=false;
		try {
			FileReader fr= new FileReader(filename);
			if(fr.read()!=0) {
				officers=(JSONArray) parser.parse(fr);
				for(Object o:officers) {
					JSONObject officer=(JSONObject) o;
					if(officer.get("name").equals(name) && officer.get("password").equals(password)) {
						System.out.print("Officer account found! Login success");
						loggedIn=true;
						break;
					}
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
		String filename="C:\\Users\\cht47\\OneDrive - Singapore Institute Of Technology\\Lessons\\Year 2 Sem 3\\Cloud Computing\\Lab_Assessment\\declared_covid_locations.json";
		boolean added_covid_record=false;
		try {
			
			FileReader fr= new FileReader(filename);
			if(fr.read()!=0) {
				covid_records=(JSONArray) parser.parse(fr);
			}	
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
	
	public boolean trackCovid(RMIClientIntf client, String nric) throws RemoteException {
		
		JSONArray affected_users=getListAffectedUsers();
		boolean covid_pos=false;
		for(Object o:affected_users) {
			JSONObject user=(JSONObject)o;
			if(user.get("nric").equals(nric)) {
				System.out.print("User has been detected as being affected.");
				covid_pos=true;
			}
		}
		return covid_pos;
	}
	public boolean check_affected_date(String datetime_user, String datetime_declared) {
		DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
                .ofPattern("dd/MM/uuuu'T'HH:mm:ss:SSSXXXXX");
		LocalDateTime odt_user = OffsetDateTime.parse(datetime_user, DATE_TIME_FORMATTER).toLocalDateTime();
		LocalDateTime odt_officer = OffsetDateTime.parse(datetime_declared, DATE_TIME_FORMATTER).toLocalDateTime();
		return Duration.between( odt_officer, odt_user).toDays()<=14;
	}
	public JSONArray getListAffectedUsers() {
		
		JSONArray users=new JSONArray();
		JSONArray users_affected=new JSONArray();
		JSONParser parser=new JSONParser();
		String filename="C:\\Users\\cht47\\OneDrive - Singapore Institute Of Technology\\Lessons\\Year 2 Sem 3\\Cloud Computing\\Lab_Assessment\\safe_entry_history.json";
		try {
			FileReader fr= new FileReader(filename);
			if(fr.read()!=0) {
				users=(JSONArray) parser.parse(fr);
				for(Object o:users) {
					JSONObject user=(JSONObject) o;
					if(user.get("location").equals(latest_declared_location) && check_affected_date((String)user.get("check_in"), latest_declared_datetime)) {
						System.out.print("Found user is being affected.");
						users_affected.add(user);
						
					}
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
			    	String filename="declared_covid_locations.json";
					WatchService watcher=(WatchService) FileSystems.getDefault().newWatchService();
					File file=new File(filename);
					Path parent = file.toPath().getParent();
					parent.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
					JSONArray covid_records=new JSONArray();
					JSONObject covid_record=new JSONObject();
					JSONParser parser=new JSONParser();
					while(watch_file) {
						WatchKey key=null;
						try{key=watcher.take();}
						catch(InterruptedException ie) {
							Thread.yield();
							continue;
						}
						for(WatchEvent<?> event : key.pollEvents()) {
							 WatchEvent<Path> event_path = (WatchEvent<Path>) event;
							 Path files=event_path.context();
							 if(files.endsWith("declared_covid_locations.json")) {
								FileReader fr;
								try {
									fr = new FileReader(filename);
									if(fr.read()!=0) {
										covid_records=(JSONArray) parser.parse(fr);
										covid_record=(JSONObject) covid_records.get(covid_records.size()-1);
										latest_declared_datetime=(String) covid_record.get("date_time");
										latest_declared_location=(String) covid_record.get("location");
										System.out.print("Detected file change");
										boolean covid_affected=trackCovid(c, nric);
										Thread.sleep(timer);
										if(covid_affected) {
											c.callBack(nric);
										}
										
									}	
								} catch (IOException | ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (InterruptedException e) {
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
	public static void main(String args[]) {
		JSONObject user=new JSONObject();
		JSONObject user1=new JSONObject();
		String userInfo="[{\"check_out\":\"null\",\"check_in\":\"2016-04-12T13:37:27+00:00\",\"name\":\"Jon\",\"location\":\"Hell\",\"nric\":\"S992222R\"},{\"check_out\":\"null\",\"check_in\":\"2016-04-12T13:37:27+00:00\",\"name\":\"Jonel\",\"location\":\"Hell\",\"nric\":\"S9922333R\"},{\"check_out\":\"null\",\"check_in\":\"2016-04-12T13:37:27+00:00\",\"name\":\"Jonel\",\"location\":\"Hell\",\"nric\":\"S9922333R\"},{\"check_out\":\"2021-06-10T14:03:34.343506900\",\"check_in\":\"2016-04-12T13:37:27+00:00\",\"name\":\"David\",\"location\":\"Canyon\",\"nric\":\"S9922535R\"}]";
		String userInfo1="[{\"check_out\":\"2016-04-12T13:37:27+00:00\",\"check_in\":\"2016-04-12T13:37:27+00:00\",\"name\":\"Jon\",\"location\":\"Hell\",\"nric\":\"S992222R\"},{\"check_out\":\"2016-04-12T13:37:27+00:00\",\"check_in\":\"2016-04-12T13:37:27+00:00\",\"name\":\"Jonel\",\"location\":\"Hell\",\"nric\":\"S9922333R\"},{\"check_out\":\"2016-04-12T13:37:27+00:00\",\"check_in\":\"2016-04-12T13:37:27+00:00\",\"name\":\"Jonel\",\"location\":\"Hell\",\"nric\":\"S9922333R\"},{\"check_out\":\"2021-06-10T14:03:34.343506900\",\"check_in\":\"2016-04-12T13:37:27+00:00\",\"name\":\"David\",\"location\":\"Canyon\",\"nric\":\"S9922535R\"}]";
//		user.put("nric", "S9922535R");
//		user.put("location","Canyon");
//		user.put("name", "David");
//		user.put("check_in", "2016-04-12T13:37:27+00:00");
//		user.put("check_out", "null");
//		user1.put("location","Canyon");
//		user1.put("nric", "S9922535R");
//		user1.put("name", "David");
//		user1.put("check_in", "null");
//		user1.put("check_out", "2016-04-12T17:37:27+00:00");
		safe_entry_impl si;
		try {
			JSONParser parse=new JSONParser();
			JSONArray JSONObj = (JSONArray)parse.parse(userInfo);
			JSONArray JSONObj1 = (JSONArray)parse.parse(userInfo1);
//			System.out.print(JSONObj);
			si = new safe_entry_impl();
			si.checkIn(JSONObj);
			si.checkOut(JSONObj1);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


}

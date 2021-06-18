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
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class safe_entry_impl extends java.rmi.server.UnicastRemoteObject implements safe_entry {
	private RMIClientIntf c;

	public safe_entry_impl() throws RemoteException {
		super();
	}
	boolean is_writing = false;
	static String user_dir = System.getProperty("user.dir");
	static Path records_dir = Paths.get(user_dir, "src", "safe_entry_server", "server_records");
	boolean watch_file = true;
	static Long lastRead = 0L;
	boolean checkedIn = false;
	static int count = 0;
	static Object obj_safe_entry_history_lock = new Object();
	static Object obj_covid_file_lock = new Object();
	boolean isReadWriting_history = false;
	boolean isReadWriting_locations = false;

	@Override
	public boolean checkIn(JSONObject user) throws RemoteException {

		String filename = Paths.get(records_dir.toString(), "safe_entry_history.json").toString();

		try {
			synchronized (obj_safe_entry_history_lock) {

				while (isReadWriting_history) {

					obj_safe_entry_history_lock.wait();

				}
				isReadWriting_history = true;
				JSONParser parser = new JSONParser();
				JSONArray users = new JSONArray();
				users = readFile(filename);
				user.put("count", count++);
				users.add(user);
				FileWriter file_writer = new FileWriter(filename);
				file_writer.write(users.toJSONString());
				file_writer.flush();
				file_writer.close();
				isReadWriting_history = false;
				obj_safe_entry_history_lock.notifyAll();
				checkedIn = true;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Thread.interrupted();
			e.printStackTrace();
		} finally {
			is_writing = false;

		}

		return checkedIn;
	}

	@Override
	public boolean checkOut(JSONObject user) throws RemoteException {
		boolean foundUser = false;
		if (!user.get("check_out").equals("null")) {
			try {

				synchronized (obj_safe_entry_history_lock) {

					while (isReadWriting_history) {
						try {
							obj_safe_entry_history_lock.wait();
						} catch (InterruptedException e) {
							Thread.interrupted();
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
					isReadWriting_history = true;
					obj_safe_entry_history_lock.notifyAll();
					JSONParser parser = new JSONParser();
					JSONArray users = new JSONArray();
					String filename = Paths.get(records_dir.toString(), "safe_entry_history.json").toString();
					users = readFile(filename);
					for (int i = users.size() - 1; i > 0; i--) {

						JSONObject user_record = (JSONObject) users.get(i);
						if (user_record.get("nric").equals(user.get("nric"))
								&& user_record.get("location").equals(user.get("location"))
								&& user_record.get("check_out").equals("null")) {
							System.out.println(user_record.get("nric"));

							user_record.put("check_out", java.time.LocalDateTime.now().toString());
							foundUser = true;
							break;
						}
					}
					// System.out.print(users);
					FileWriter file_writer = new FileWriter(filename);
					file_writer.write(users.toJSONString());
					file_writer.flush();
					file_writer.close();
					isReadWriting_history = false;
					obj_safe_entry_history_lock.notifyAll();
				}

			} catch (IOException e) {

				e.printStackTrace();
			}
		} else {
			System.out.print("There's no check out time");
		}
		// TODO Auto-generated method stub

		return foundUser;
	}

	@Override
	public JSONArray getInfo(String nric) throws RemoteException {
		String filename = Paths.get(records_dir.toString(), "safe_entry_history.json").toString();
		JSONArray records = new JSONArray();
		if (!nric.equals("null")) {
			try {
				synchronized (obj_safe_entry_history_lock) {

					while (isReadWriting_history) {

						obj_safe_entry_history_lock.wait();

					}
					isReadWriting_history = true;
					JSONArray users = readFile(filename);

					for (Object o : users) {
						JSONObject user_record = (JSONObject) o;
						if (nric.equals(user_record.get("nric"))) {
							records.add(user_record);
						}
					}

					isReadWriting_history = false;
					obj_safe_entry_history_lock.notifyAll();
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				Thread.interrupted();
				e.printStackTrace();
			}
		}
		return records;

	}

	public JSONArray readFile(String filename) {
		JSONArray records = new JSONArray();
		JSONParser parser = new JSONParser();
		JSONObject record = new JSONObject();

		FileReader fr;
		try {
			fr = new FileReader(filename);
			File file = new File(filename);
			if (file.length() != 0) {
				records = (JSONArray) parser.parse(fr);
			}
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return records;
	}

	@Override
	public boolean checkLogin(String name, String password) throws RemoteException {
		String filename = Paths.get(records_dir.toString(), "user_officers.json").toString();
		boolean loggedIn = false;
		JSONArray officers = readFile(filename);
		for (Object o : officers) {
			JSONObject officer = (JSONObject) o;
			if (officer.get("name").equals(name) && officer.get("password").equals(password)) {
				System.out.print("Officer account found! Login success");
				loggedIn = true;
				break;
			}
		}

		return loggedIn;
	}

	@Override
	public boolean declareLocationUnsafe(String locationName, String dateTime) throws RemoteException {
		boolean added_covid_record = false;
		try {

			synchronized (obj_covid_file_lock) {

				while (isReadWriting_locations) {
					try {
						obj_covid_file_lock.wait();
					} catch (InterruptedException e) {
						Thread.interrupted();
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				isReadWriting_locations = true;
				JSONArray covid_records = new JSONArray();
				JSONParser parser = new JSONParser();
				JSONObject covid_record = new JSONObject();
				covid_record.put("location", locationName);
				DateTimeFormatter dt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
				LocalDateTime ldt = LocalDateTime.parse(dateTime, dt);
				covid_record.put("date_time", ldt.toString());
				String filename = Paths.get(records_dir.toString(), "declared_covid_locations.json").toString();
				covid_records = readFile(filename);
				FileWriter file_writer = new FileWriter(filename);
				file_writer.write(covid_records.toJSONString());
				file_writer.flush();
				file_writer.close();
				isReadWriting_locations = false;
				obj_covid_file_lock.notifyAll();
				added_covid_record = true;
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return added_covid_record;
	}

	public boolean check_affected_date(String datetime_user, String datetime_declared) {
		LocalDateTime odt_user = LocalDateTime.parse(datetime_user);
		DateTimeFormatter dt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		LocalDateTime odt_officer = LocalDateTime.parse(datetime_declared);
		return Duration.between(odt_officer, odt_user).toDays() <= 14;
	}

	public JSONArray getListAffectedUsers(String latest_declared_location, String latest_declared_datetime) {

		JSONArray users = new JSONArray();
		JSONArray users_affected = new JSONArray();
		JSONParser parser = new JSONParser();

		String filename = Paths.get(records_dir.toString(), "safe_entry_history.json").toString();
		users = readFile(filename);
		for (Object o : users) {
			JSONObject user = (JSONObject) o;
			if (user.get("location").equals(latest_declared_location)
					&& check_affected_date((String) user.get("check_in"), latest_declared_datetime)) {
				System.out.print("Found user is being affected.");
				users_affected.add(user);

			}
		}

		return users_affected;
	}

	public void stopWatcher() {
		watch_file = false;
	}

	public void informUsers(RMIClientIntf client, String nric) throws RemoteException {
		c = client;

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				Random rg = new Random();
				int timer = rg.nextInt(1000);
				try {
					WatchService watcher = (WatchService) FileSystems.getDefault().newWatchService();
					records_dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
					JSONArray covid_records = new JSONArray();
					JSONObject covid_record = new JSONObject();
					JSONParser parser = new JSONParser();
					String filename = Paths.get(records_dir.toString(), "declared_covid_locations.json").toString();
					while (watch_file) {
						WatchKey key = null;
						try {
							key = watcher.take();
						} catch (InterruptedException ie) {
							System.out.println("Thread interrupted; yielding");
							Thread.yield();
							continue;
						}
						for (WatchEvent<?> event : key.pollEvents()) {
							WatchEvent<Path> event_path = (WatchEvent<Path>) event;
							Path files = event_path.context();
							if (files.endsWith("declared_covid_locations.json")) {
								FileReader fr;
								try {
									Long lastWriteTime = new File(Paths
											.get(records_dir.toString(), "declared_covid_locations.json").toString())
													.lastModified();
									if (!Objects.equals(lastWriteTime, lastRead)) {
										covid_records = readFile(filename);
										covid_record = (JSONObject) covid_records.get(covid_records.size() - 1);
										String latest_declared_datetime = (String) covid_record.get("date_time");
										String latest_declared_location = (String) covid_record.get("location");
										System.out.print("Detected new records entered.");
										JSONArray affected_users = getListAffectedUsers(latest_declared_location,
												latest_declared_datetime);
										System.out.println(affected_users);
										lastRead = lastWriteTime;
										c.callBack(affected_users, latest_declared_datetime);
									}

								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}

						}
						if (!key.reset()) {
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

	

}

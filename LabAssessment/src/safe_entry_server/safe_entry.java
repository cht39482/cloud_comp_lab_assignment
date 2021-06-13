package safe_entry_server;
import java.rmi.RemoteException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import safe_entry_client.SafeEntry_Client;

public interface safe_entry extends java.rmi.Remote {
	public boolean checkIn(JSONObject user) throws RemoteException;
	public boolean checkIn(JSONArray users) throws RemoteException;
	public boolean checkOut(JSONObject user) throws RemoteException;
	public boolean checkOut(JSONArray users) throws RemoteException;
	public boolean checkLogin(String name, String password) throws RemoteException;
	public boolean declareLocationUnsafe(String locationName, String dateTime) throws RemoteException;
	public boolean trackCovid(RMIClientIntf client, String nric) throws RemoteException;
	public void informUsers(RMIClientIntf client, String nric) throws RemoteException;
	public JSONArray getInfo() throws RemoteException;
	public JSONArray getInfo(String name) throws RemoteException;
}

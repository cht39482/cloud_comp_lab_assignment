package safe_entry_server;

import java.rmi.Remote;

import org.json.simple.JSONArray;

public interface RMIClientIntf extends Remote {
	public void callBack(JSONArray users, String latest_declared_time) throws java.rmi.RemoteException;
}

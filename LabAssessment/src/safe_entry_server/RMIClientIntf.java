package safe_entry_server;

import java.rmi.Remote;

public interface RMIClientIntf extends Remote {
	public void callBack(String nric) throws java.rmi.RemoteException;
}

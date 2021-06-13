package safe_entry_server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class safe_entry_server {
	static int port=1099;
	   public safe_entry_server() {
		     

		     try {
		    	 
		       	safe_entry se = new safe_entry_impl();
		       	LocateRegistry.createRegistry(port);
		       	Naming.rebind("rmi://localhost:" + port + "/SafeEntryService", se);
		       	System.out.print("Server is running");
		     } 
		     catch (Exception e) {
		       System.out.println("Server Error: " + e);
		     }
		   }

		   public static void main(String args[]) {
		     	//Create the new Calculator server
			if (args.length == 1)
				port = Integer.parseInt(args[0]);
			
			new safe_entry_server();
		   }

}

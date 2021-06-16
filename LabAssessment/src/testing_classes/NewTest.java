package testing_classes;

import org.testng.annotations.Test;

import safe_entry_server.safe_entry_impl;

import org.testng.annotations.BeforeMethod;

import static org.testng.Assert.assertEquals;

import java.rmi.RemoteException;
import java.time.LocalDateTime;

import org.json.simple.JSONObject;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.AfterTest;

public class NewTest {
	
	JSONObject user=new JSONObject();
  @Test(threadPoolSize=5,invocationCount=15,timeOut=1000)
  public void f() {
	  try {
		  Long id = Thread.currentThread().getId();
//	        System.out.println("Test method executing on thread with id: " + id);
			safe_entry_impl si=new safe_entry_impl();
			si.declareLocationUnsafe("Samsung", "2020-06-09 13:12");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  }
  @BeforeMethod
  public void beforeMethod() {
	 
  }

  @AfterMethod
  public void afterMethod() {
  }

  @BeforeTest
  public void beforeTest() {
	  user.put("name","Same");
	  user.put("nric", "s1234567b");
	  user.put("location", "Home");
	  user.put("check_in",LocalDateTime.now().toString());
	  user.put("check_out", LocalDateTime.now().toString());
	  System.out.println(user);
	  
  }

  @AfterTest
  public void afterTest() {
  }

}

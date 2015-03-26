import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;



public class View {
	public static final String DOMAIN = "project1";
    private AmazonSimpleDB sdb;	
	// map of server IDs -> (status, time)
	public TreeMap<String, String[]> viewMap;
	
	public View(String serverId){
        AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
        sdb = new AmazonSimpleDBClient(credentialsProvider);
		this.viewMap = new TreeMap<String, String[]>();

		if(!sdb.listDomains().getDomainNames().contains(DOMAIN)){
		     sdb.createDomain(new CreateDomainRequest(DOMAIN));
		}
		System.out.println("Your SimpleDB domains: ");
		for(String d : sdb.listDomains().getDomainNames()){
			System.out.println("- " + d);
		}
		
		// bootstrap self
	}
	
	/*** 
	 * Serializes the view's viewMap into a String of the format 
	 * svrID_status_time|svrID_status_time.  Use of TreeMap ensures
	 * that two equivalent viewMaps will have the same serialized value. */
	public static String serializeViewMap(HashMap<String, String[]> viewMap){
		String viewMapString = "";
		Iterator<String> svrIDs = viewMap.keySet().iterator();
		while(svrIDs.hasNext()){
			String svrID = svrIDs.next();
			viewMapString += svrID + "_";
			viewMapString += viewMap.get(svrID)[0] + "_";
			viewMapString += viewMap.get(svrID)[1];
			viewMapString += "|";
		}
		return viewMapString;
	}
	
	/***
	 * Unserializes a viewMapString into a new viewMap.
	 */
	public static TreeMap<String, String[]> unserializeViewMap(String viewMapString){
		TreeMap<String, String[]> newViewMap = new TreeMap<String, String[]>();
		String[] servers = viewMapString.split("|");
		for(int i = 0; i < servers.length; i++){
			String[] status_time = new String[2];
			status_time[0] = servers[i].split("_")[1]; // up/down
			status_time[1] = servers[i].split("_")[2]; // time
			newViewMap.put(servers[i].split("_")[0], status_time);
		}
		return newViewMap;
	}
	
	public void updateStatus(String serverId, String status){
		String now =  Long.toString(System.currentTimeMillis());
		viewMap.put(serverId, new String[]{status, now});
	}
	
	public void exchange(String viewMapString){
		TreeMap<String, String[]> otherViewMap = unserializeViewMap(viewMapString);
		// perform exchange
	}
	
	public void gossip(){
		// choose random server ID from view or SimpleDB
		
		// exchange views with that server/DB via RPC call
	}

}

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;

import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;



public class View {
	private static final String DOMAIN = "project1";
    private AmazonSimpleDB sdb;	
	// map of server IDs -> (status, time)
	private TreeMap<String, String[]> viewMap;
	private String localIP;
	
	/***
	 * Unserializes a viewMapString into a new viewMap.
	 */
	private static TreeMap<String, String[]> unserialize(String viewMapString){
		TreeMap<String, String[]> newViewMap = new TreeMap<String, String[]>();
		String[] servers = viewMapString.split("&");
		for(int i = 0; i < servers.length; i++){
			String status = servers[i].split("_")[1];
			String time = servers[i].split("_")[2]; 
			newViewMap.put(servers[i].split("_")[0], new String[]{status, time});
		}
		return newViewMap;
	}
	
	public View(String localIP){
        AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
        sdb = new AmazonSimpleDBClient(credentialsProvider);
		this.viewMap = new TreeMap<String, String[]>();
		this.localIP = localIP;

		if(!sdb.listDomains().getDomainNames().contains(DOMAIN)){
		     sdb.createDomain(new CreateDomainRequest(DOMAIN));
		}
		System.out.println("Your SimpleDB domains: ");
		for(String d : sdb.listDomains().getDomainNames()){
			System.out.println("- " + d);
		}
		
		// bootstrap self using SimpleDB
	}
	
	/*** 
	 * Serializes this view's viewMap into a String of the format 
	 * svrID_status_time|svrID_status_time.  Use of TreeMap ensures
	 * that two equivalent viewMaps will have the same serialized value. */
	public String serialize(){
		String viewMapString = "";
		Iterator<String> svrIDs = viewMap.keySet().iterator();
		while(svrIDs.hasNext()){
			String svrID = svrIDs.next();
			// localIP time is always now, up
			String status = svrID == localIP ? "up" : viewMap.get(svrID)[0];
			String time = svrID == localIP ? Long.toString(System.currentTimeMillis()) : viewMap.get(svrID)[1];
			viewMapString += svrID + "_" + status + "_" + time + "&";
		}
		return viewMapString;
	}
	
	public int getViewSize(){
		return viewMap.size();
	}
	
	public String getRandomIP(){
		Random random = new Random();
		ArrayList<String> keys = new ArrayList<String>(viewMap.keySet());
		return keys.get(random.nextInt(keys.size()));
	}
	
	public void updateStatus(String serverId, String status){
		String now =  Long.toString(System.currentTimeMillis());
		viewMap.put(serverId, new String[]{status, now});
	}
	
	public void merge(String viewMapString){
		TreeMap<String, String[]> otherViewMap = unserialize(viewMapString);
		// set viewMap to the merge of viewMap and otherViewMap
	}
	
	public void mergeWithSimpleDB(){
		// exchange views with SimpleDB
		
	}

}

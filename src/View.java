import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.HashMap;

import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;



public class View {
	private static final String DOMAIN = "project1";
	private static final String ITEM_NAME = "view";
	private static final String ATTR_NAME = "serialized_view";

    private AmazonSimpleDB sdb;	
	private HashMap<String, String[]> viewMap; 	// map of server IDs -> (status, time)
	private String localIP;
	
	/***
	 * Unserializes a viewMapString into a new viewMap.
	 */
	private static HashMap<String, String[]> unserialize(String viewMapString){
		HashMap<String, String[]> newViewMap = new HashMap<String, String[]>();
		String[] servers = viewMapString.split("\\&");
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
		this.viewMap = new HashMap<String, String[]>();
		this.localIP = localIP;
		viewMap.put(localIP, new String[]{"up", Long.toString(System.currentTimeMillis())});
		
		if(!sdb.listDomains().getDomainNames().contains(DOMAIN)){
		     sdb.createDomain(new CreateDomainRequest(DOMAIN));
		}
		
		mergeWithSimpleDB();
	}
	
	/*** 
	 * Serializes this view's viewMap into a String of the format 
	 * svrID_status_time|svrID_status_time.   */
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
		HashMap<String, String[]> otherViewMap = unserialize(viewMapString);
		// set viewMap to the merge of viewMap and otherViewMap
		for(String svrID : otherViewMap.keySet()){
			if(!viewMap.containsKey(svrID)) viewMap.put(svrID, otherViewMap.get(svrID));
			if(Long.parseLong(otherViewMap.get(svrID)[1]) > Long.parseLong(viewMap.get(svrID)[1])){
				viewMap.put(svrID, otherViewMap.get(svrID));
			}
		}
	}
	
	public void mergeWithSimpleDB(){
		// exchange views with SimpleDB
		GetAttributesRequest getReq = new GetAttributesRequest();
		getReq.setDomainName(DOMAIN);
		getReq.setItemName(ITEM_NAME);
		ArrayList<String> attribute = new ArrayList<String>();
		attribute.add(ATTR_NAME);
		getReq.setAttributeNames(attribute);
		GetAttributesResult getRes = sdb.getAttributes(getReq);
		
		if(getRes.getAttributes().size() == 1){
			String dbViewString = getRes.getAttributes().get(0).getValue();
			this.merge(dbViewString);
		}
		
		PutAttributesRequest putReq = new PutAttributesRequest();
		ReplaceableAttribute attr = new ReplaceableAttribute();
		attr.setName(ATTR_NAME);
		attr.setValue(this.serialize());
		putReq.setDomainName(DOMAIN);
		putReq.setItemName(ITEM_NAME);
		ArrayList<ReplaceableAttribute> attributes = new ArrayList<ReplaceableAttribute>();
		attributes.add(attr);
		putReq.setAttributes(attributes);
		sdb.putAttributes(putReq);
	}

}

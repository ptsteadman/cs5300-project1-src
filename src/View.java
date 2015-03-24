import java.util.ArrayList;
import java.util.HashMap;

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
	public HashMap<String, ArrayList<Attribute>> viewMap;
	
	public View(String serverId){
        AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
        sdb = new AmazonSimpleDBClient(credentialsProvider);
		this.viewMap = new HashMap<String, ArrayList<Attribute>>();

		if(!sdb.listDomains().getDomainNames().contains(DOMAIN)){
		     sdb.createDomain(new CreateDomainRequest(DOMAIN));
		}
		System.out.println("Your SimpleDB domains: ");
		for(String d : sdb.listDomains().getDomainNames()){
			System.out.println("- " + d);
		}
		
		// bootstrap self
	}
	
	public void update(String serverId){
		// set to (up, now) in map
	}
	
	public void exchange(View otherView){
		// perform exchange
	}

}

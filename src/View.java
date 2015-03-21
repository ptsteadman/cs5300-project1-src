import java.util.ArrayList;
import java.util.HashMap;

import com.amazonaws.services.simpledb.model.Attribute;


public class View {
	// map of server IDs -> (status, time)
	public HashMap<String, ArrayList<Attribute>> viewMap;
	
	public View(){
		this.viewMap = new HashMap<String, ArrayList<Attribute>>();
	}
	
	public void update(String serverId){
		// set to (up, now) in map
	}
	
	public void exchange(View otherView){
		// perform exchange
	}

}

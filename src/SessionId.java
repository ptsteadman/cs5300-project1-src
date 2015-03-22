
public class SessionId {
	private Integer sessionId;
	private String serverIP = "0.0.0.0";
	
	public SessionId() {}
	
	public SessionId(int sid, String IP) {
		sessionId = sid;
		serverIP = IP;
	}
	
	public SessionId(String sid, String IP) {
		sessionId = new Integer(sid);
		serverIP = IP;
	}
	
	public SessionId(String serialized) {
		String[] tokens = serialized.split("_");
		assert(tokens.length == 2);
		sessionId = new Integer(tokens[0]);
		String[] frags = tokens[1].split(".");
		assert(frags.length == 4);
		serverIP = tokens[1];
	}

	public Integer getServerId() {
		return sessionId;
	}

	public void setServerId(int serverId) {
		this.sessionId = serverId;
	}

	public String getServerIP() {
		return serverIP;
	}

	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}
	
	public String serialize() {
		String[] tokens = serverIP.split(".");
		assert(tokens.length == 4);
		return "" + sessionId + "_" + serverIP;
	}

}

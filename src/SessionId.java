
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
	
	public SessionId(String serialized) {}

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

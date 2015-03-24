
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

	public Integer getSessionId() {
		return sessionId;
	}

	public void getSessionId(int serverId) {
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
	
	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + sessionId;
		result = 31 * result + serverIP.hashCode();
		return result;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof SessionId) {
			SessionId sid = (SessionId)o;
			return (serverIP.equals(sid.getServerIP()) && sessionId == sid.getSessionId());
		} else {
			return false;
		}
	}

}

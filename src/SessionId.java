
public class SessionId {
	private int serverId;
	private String serverIP;
	
	public SessionId() {}
	
	public SessionId(String serialized) {}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public String getServerIP() {
		return serverIP;
	}

	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}
	
	public String serialize() {
		return "";
	}

}


public class SessionState {
	private SessionId sessionID;
	private long version;
	private String message;
	private long timeout;
	// metadata fields
	private int maxLength;

	public SessionState() {}

	public SessionState(SessionId sessionID, String message) {
		this.sessionID = sessionID;
		this.message = message;
		this.version = 1;
		this.timeout = System.currentTimeMillis() + 1000 * 60;
		this.maxLength = 512 - 2 * 8 - 2 * (8 + (sessionID.getServerIP().length() * 8));
	}
	
	public SessionState(String serialized) {}

	public SessionId getSessionID() {
		return sessionID;
	}

	public void setSessionID(SessionId sessionID) {
		this.sessionID = sessionID;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	/**
	 * Refresh and increment version
	 */
	public void refresh() {
		timeout = System.currentTimeMillis() + 1000 * 60;
		this.version++;
	}

	/**
	 * Replace old session message with the new one. limits session state to
	 * 512 bytes.
	 * @param newMessage -- new session state message to be displayed
	 */
	public void replace(String newMessage) {
		if (newMessage.length() > maxLength) {
			this.message = newMessage.substring(0, maxLength);
		} else {
			this.message = newMessage;
		}
		this.version++;
		this.timeout = System.currentTimeMillis() + 1000 * 60;
	}
}

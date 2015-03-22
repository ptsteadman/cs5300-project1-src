
public class SessionState {
	private SessionId sessionID;
	private long version;
	private String message;
	private long timeout;
	// metadata fields
	private static final int maxLength =  512 - 2 * 8 - 2 * (8 + (15 * 8));

	public SessionState() {}

	public SessionState(SessionId sessionID, String message) {
		this.sessionID = sessionID;
		this.version = 1;
		this.timeout = System.currentTimeMillis() + 1000 * 60 + 1000 * 5;
		if (message.length() > maxLength) {
			this.message = message.substring(0, maxLength);
		} else {
			this.message = message;
		}
	}
	
	public SessionState(String serialized) {
		String[] tokens = serialized.split("_");
		assert(tokens.length == 5);
		SessionId sid = new SessionId(tokens[0], tokens[1]);
		sessionID = sid;
		version = new Long(tokens[2]);
		timeout = new Long(tokens[4]);
		String msg = tokens[3];
		assert(msg.charAt(0) == '(' && msg.charAt(msg.length()-1) == ')');
		msg = msg.substring(1, msg.length()-1);
		message = msg;
	}

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
		timeout = System.currentTimeMillis() + 1000 * 60 + 1000 * 5;
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
		this.timeout = System.currentTimeMillis() + 1000 * 60 + 1000 * 5;
	}
	
	public String serialize() {
		return sessionID.serialize() + "_" + version + "_" + "(" + message + ")" + "_" + timeout;
	}
}

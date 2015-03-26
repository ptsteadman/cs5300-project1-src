
public interface RPCUser {
	public SessionState sessionRead(String sessId);
	public int sessionWrite(SessionState ss);
	public String receiveExchangeViews(String viewMapString); // XXX fix
	public String getHost();
}
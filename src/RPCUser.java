
public interface RPCUser {
	public SessionState sessionRead(SessionId sessId);
	public int sessionWrite(SessionState ss);
	public void exchangeViews(); // XXX fix
	public String getHost();
}
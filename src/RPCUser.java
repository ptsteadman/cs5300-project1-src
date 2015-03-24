
public interface RPCUser {
	public SessionState sessionRead(String sessId);
	public int sessionWrite(SessionState ss);
	public void exchangeViews(); // XXX fix
	public String getHost();
}
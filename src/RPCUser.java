
public interface RPCUser {
	public SessionState sessionRead(SessionId sessId);
	public int sessionWrite(SessionId sessId, long version, String sessData, long discardTime);
	public void exchangeViews(); // XXX fix
}
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.lang.Runtime;
import java.net.DatagramPacket;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet implementation class SessionServlet
 */
@WebServlet(value="/SessionServlet", loadOnStartup=1)
public class SessionServlet extends HttpServlet implements RPCUser {
	private static final long serialVersionUID = 1L;
	private HashMap<String, SessionState> sessionTable;
	private Hashtable<String, String> lockTable;
	private View groupView;
	private static String initialString = "Hello World!";
	private static final String cookieName = "CS5300P1ASESSION";
	private String IPAddr = "0.0.0.0";
	private Integer sessNum = 1;
	private RPCServer rpcServer;
	private RPCClient rpcClient;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SessionServlet() {
		super();
		// untested code for getting IP address
		try {
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec("/opt/aws/bin/ec2-metadata --public-ipv4");
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String output = stdIn.readLine();
			if(output != null && output != "") this.IPAddr = output.substring(13);
		} catch (Throwable t){
			System.out.println("Problem getting ec2 IP address.");
			t.printStackTrace();
		}
		groupView = new View(this.IPAddr);  // create view and add self to it		
		sessionTable = new HashMap<String, SessionState>();
		lockTable = new Hashtable<String, String>();
		rpcClient = new RPCClient(this);
		rpcServer = new RPCServer(this);
		rpcServer.setDaemon(true);
		rpcServer.start();
		GarbageCollector gc = new GarbageCollector();
		gc.setDaemon(true);
		gc.start();
		System.out.println("Session Servlet Started");
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Cookie[] cookies = request.getCookies();
		ArrayList<Cookie> validCookies = new ArrayList<Cookie>();

		// print cookies for sanity
		if (cookies != null) {
			for (Cookie c : cookies) {
				System.out.println("cookie name = " + c.getName());
				System.out.println("cookie value = " + c.getValue());
				if (c.getName().equals(cookieName)) {
					validCookies.add(c);
				}
			}
		}
		System.out.println("---");

		// no valid cookies --  first server request
		if (validCookies.isEmpty()) {
			SessionId sessid = null;
			synchronized (sessNum) {
				sessid = new SessionId(sessNum.toString(), IPAddr);
				sessNum++;
			}
			SessionState ss = new SessionState(sessid, initialString);
			String ip1 = "54.148.120.63";
			String ip2 = "52.10.147.176";
			String backup = "";

			if (IPAddr.equals(ip1)) {
				backup = ip2;
			} else {
				backup = ip1;
			}

			// SessionWrite
			int rc = sessionWrite(ss);
			if (rc != 1) {
				System.out.println("Write failed!");
			}

			ArrayList<String> dest_id = new ArrayList<String>();
			dest_id.add(backup);
			System.out.println("Writing to backup = " + backup);
			System.out.println("We are: " + IPAddr);
			DatagramPacket wPkt = rpcClient.sessionWriteClient(ss, dest_id); // Change code to get the backup ip correctly
			if (wPkt == null) {
				backup = "0.0.0.0";
				System.out.println("backup write failed");
				// XXX backup is down
			} else {
				// XXX backup is up
			}

			RequestDispatcher view = request.getRequestDispatcher("session.jsp");
			request.setAttribute("state", ss.getMessage());
			request.setAttribute("timeout", ss.getTimeout());
			Cookie sCookie = new Cookie(cookieName, cookieValue(ss, IPAddr, backup));
			sCookie.setMaxAge(60);
			response.addCookie(sCookie);
			view.forward(request, response);
		} else {
			if (validCookies.size() > 1) { // there shouldn't be more than one valid cookie in a single request
				throw new ServletException("Incorrect number of cookies in request");
			} else {
				// parse cookie
				Cookie rCookie = validCookies.get(0);
				String[] terms = rCookie.getValue().split("_");
				SessionId sid = new SessionId(terms[0], terms[1]);
				//				Integer version = new Integer(terms[2]);
				String primaryIp = terms[3];
				String backupIp = terms[4];

				RequestDispatcher view = request.getRequestDispatcher("session.jsp");
				if (request.getParameter("logout") != null) { // logout button press
					// logout button
					rCookie.setMaxAge(0);
					response.addCookie(rCookie);
					request.setAttribute("state", "Logged out!");
					request.setAttribute("timeout", System.currentTimeMillis());
					view.forward(request, response);
					return;
				} else {  // refresh button press
					SessionState ss = null;
					SessionState ss1 = null;
					SessionState ss2 = null;
					ss1 = sessionRead(sid.serialize());

					String backup = "";
					if (primaryIp.equals(IPAddr)) {
						backup = backupIp;
					} else {
						backup = primaryIp;
					}
					
					ArrayList<String> dest_id = new ArrayList<String>();
					dest_id.add(backup);
					DatagramPacket reply = rpcClient.sessionReadClient(sid, dest_id);
					if (reply == null) {
						backupIp = "0.0.0.0";
						ss = ss1;
						// XXX backup down
					} else {
						String reply_data = new String(reply.getData()).trim();
						String[] tok = reply_data.split("\\|");
						assert(tok.length == 2);
						ss2 = new SessionState(tok[1].trim());
						// XXX backup up
					}

					if (ss1.getSessionID().getSessionId() == -1 && ss2.getSessionID().getSessionId() == -1) {
						// we should never hit this
						System.out.println("Session timed out, cookie persisted longer than session.");
						rCookie.setMaxAge(0);
						response.addCookie(rCookie);
						request.setAttribute("state", "Session timed out!");
						request.setAttribute("timeout", System.currentTimeMillis());
						view.forward(request, response);
						return;
					} else if (ss2.getSessionID().getSessionId() == -1 || ss1.getVersion() > ss2.getVersion()) {
						ss = ss1;
					} else {
						ss = ss2;
					}
					request.setAttribute("state", ss.getMessage());
					request.setAttribute("timeout", ss.getTimeout());
					Cookie sCookie = new Cookie(cookieName, cookieValue(ss, IPAddr, backup));
					sCookie.setMaxAge(60);
					response.addCookie(sCookie);
					view.forward(request, response);
				}
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Cookie[] cookies = request.getCookies();
		ArrayList<Cookie> validCookies = new ArrayList<Cookie>();
		// print cookies for sanity
		for (Cookie c : cookies) {
			if (c.getName().equals(cookieName)) {
				System.out.println("cookie name = " + c.getName());
				System.out.println("cookie value = " + c.getValue());
				validCookies.add(c);
			}
		}
		System.out.println("---");

		if (validCookies.isEmpty()) { // first server request can't be a post
			throw new ServletException("Illegal HTTP method");
		} else {
			if (validCookies.size() > 1) { // shouldn't be > 1 valid cookie per request
				throw new ServletException("Incorrect number of cookies in post");
			} else {
				// parse cookie
				Cookie rCookie = validCookies.get(0);
				String[] terms = rCookie.getValue().split("_");
				SessionId sid = new SessionId(terms[0], terms[1]);
				Integer version = new Integer(terms[2]);
				String primaryIp = terms[3];
				String backupIp = terms[4];

				RequestDispatcher view = request.getRequestDispatcher("session.jsp");
				String newState = request.getParameter("replacetext");
				SessionState ss = new SessionState(sid, newState);
				ss.setVersion(version + 1);
				int rc = sessionWrite(ss);
				
				if (rc != 1) {
					// something bad happened
				}

				// XXX distributed write to backup
				String backup = "";
				if (primaryIp.equals(IPAddr)) {
					backup = backupIp;
				} else {
					backup = primaryIp;
				}
				ArrayList<String> dest_id = new ArrayList<String>();
				dest_id.add(backup);
				System.out.println("Writing to backup = " + backup);
				System.out.println("We are: " + IPAddr);
				DatagramPacket dp = rpcClient.sessionWriteClient(ss, dest_id);
				
				if (dp == null) {
					backup = "0.0.0.0";
					System.out.println("backup write failed");
					// XXX backup down
				} else {
					// XXX backup up
				}

				request.setAttribute("state", ss.getMessage());
				request.setAttribute("timeout", ss.getTimeout());
				Cookie sCookie = new Cookie(cookieName, cookieValue(ss, IPAddr, backup));
				sCookie.setMaxAge(60);
				response.addCookie(sCookie);
				view.forward(request, response);
			}
		}
	}

	/**
	 * 
	 * @param sData -- SessionState we want to cookify
	 * @return Cookie value for this SessionState
	 */
	private String cookieValue(SessionState sData, String primaryIp, String secondaryIp) {
		String[] tok1 = primaryIp.split(".");
		assert(tok1.length == 4);
		String[] tok2 = primaryIp.split(".");
		assert(tok2.length == 4);
		return "" + sData.getSessionID().serialize() + "_" + sData.getVersion() + "_" + primaryIp + "_" + secondaryIp;
	}

	/**
	 * Garbage collecting thread for state table. Run as daemon thread.
	 * Periodically runs (every 5 seconds) and checks for timed out Sessions.
	 */
	private class GarbageCollector extends Thread {

		@Override
		public void run() {
			while (true) {
				for (String sid : lockTable.keySet()) {
					String lock = lockTable.get(sid);
					synchronized (lock) {
						SessionState sData = sessionTable.get(sid);
						if (sData.getTimeout() < System.currentTimeMillis()) {
							// expired, remove from both tables.
							System.out.println("Removing expired session.");
							lockTable.remove(sid);
							sessionTable.remove(sid);
						}
					}
				}
				try {
					Thread.sleep(1000 * 5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public SessionState sessionRead(String sessId) {
		SessionState ss = new SessionState();
		if (!lockTable.containsKey(sessId)) {
			ss.setSessionID(new SessionId(-1, "0.0.0.0"));
			return ss;
		} else {
			String lock = lockTable.get(sessId);
			synchronized (lock) {
				ss = sessionTable.get(sessId);
				ss.refresh();
			}
		}
		return ss;
	}

	public int sessionWrite(SessionState ss) {
		SessionId sessid = ss.getSessionID();
		if (!lockTable.containsKey(sessid.serialize())) {
			lockTable.put(sessid.serialize(), sessid.serialize());
		}

		String lock = lockTable.get(sessid.serialize());
		synchronized (lock) {
			sessionTable.put(sessid.serialize(), ss);
		}
		return 1;
	}

	public void exchangeViews() {
		// TODO Auto-generated method stub
	}

	@Override
	public String getHost() {
		return this.IPAddr;
	}
}
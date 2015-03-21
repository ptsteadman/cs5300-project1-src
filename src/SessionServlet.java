import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet; XXX
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


// TODO list
// location metadata -- add in next project.

/**
 * Servlet implementation class SessionServlet
 */
//@WebServlet("/SessionServlet") XXX
public class SessionServlet extends HttpServlet implements RPCUser {
	private static final long serialVersionUID = 1L;
	private HashMap<SessionId, SessionState> sessionTable;
	private ConcurrentHashMap<SessionId, SessionId> lockTable;
	private static String initialString = "Hello World!";
	private static final String cookieName = "CS5300P1ASESSION";
	private String IPAddr = "0.0.0.0";
	private Integer sessNum = 1;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SessionServlet() {
		super();
		// XXX: get IP address from AWS
		sessionTable = new HashMap<SessionId, SessionState>();
		lockTable = new ConcurrentHashMap<SessionId, SessionId>();
		GarbageCollector gc = new GarbageCollector();
		gc.setDaemon(true);
		gc.start();
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
			// SessionWrite
			int rc = sessionWrite(ss);
			if (rc != 1) {
				System.out.println("fuck");
			}
			// distributed write to another server
			RequestDispatcher view = request.getRequestDispatcher("session.jsp");
			request.setAttribute("state", ss.getMessage());
			request.setAttribute("timeout", ss.getTimeout());
			Cookie sCookie = new Cookie(cookieName, cookieValue(ss, IPAddr, "0.0.0.0")); // XXX backup ip here
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
				Integer version = new Integer(terms[2]);
				String primaryIp = terms[3];
				String backupIp = terms[4];

				// check if we service this request
				// if yes, then just do the normal thing
				// if no, then send messages to 
				RequestDispatcher view = request.getRequestDispatcher("session.jsp");
				if (request.getParameter("refresh") != null) { // refresh button press
					// refresh button
					SessionState ss = null;
					if (primaryIp.equals(IPAddr) || backupIp.equals(IPAddr)) {
						ss = sessionRead(sid);
					} else {
						// distributed read
						// XXX failure conditions
					}
					request.setAttribute("state", ss.getMessage());
					request.setAttribute("timeout", ss.getTimeout());
					Cookie sCookie = new Cookie(cookieName, cookieValue(ss, primaryIp, backupIp));
					sCookie.setMaxAge(60);
					response.addCookie(sCookie);
					view.forward(request, response);
				} else if (request.getParameter("logout") != null) { // logout button press
					// logout button
					rCookie.setMaxAge(0);
					response.addCookie(rCookie);
					request.setAttribute("state", "Logged out!");
					request.setAttribute("timeout", System.currentTimeMillis());
					view.forward(request, response);
				} else { // f5 re-get
					// XXX wut
					//					request.setAttribute("state", sData.getMessage());
					//					request.setAttribute("timeout", sData.getTimeout());
					//					response.addCookie(rCookie);
					//					view.forward(request, response);
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
				SessionState ss = new SessionState(sid, newState); // XXX careful with cookie timeouts
				ss.setVersion(version + 1);
				if (primaryIp.equals(IPAddr) || backupIp.equals(IPAddr)) {
					sessionWrite(ss);
				} else {
					// distributed write
				}
				request.setAttribute("state", ss.getMessage());
				request.setAttribute("timeout", ss.getTimeout());
				Cookie sCookie = new Cookie(cookieName, cookieValue(ss, primaryIp, backupIp)); // XXX write failing
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
				for (SessionId sid : lockTable.keySet()) {
					SessionId lock = lockTable.get(sid);
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

	public SessionState sessionRead(SessionId sessId) {
		// TODO Auto-generated method stub
		SessionState ss = new SessionState();
		if (!lockTable.containsKey(sessId)) {
			ss.setSessionID(new SessionId(-1, "0.0.0.0"));
			return ss;
		} else {
			SessionId lock = lockTable.get(sessId);
			synchronized (lock) {
				ss = sessionTable.get(sessId);
				ss.refresh();
			}
		}
		return ss;
	}

	public int sessionWrite(SessionState ss) {
		// TODO Auto-generated method stub
		SessionId sessid = ss.getSessionID();
		if (!lockTable.containsKey(sessid)) {
			lockTable.put(sessid, sessid);
		}

		SessionId lock = lockTable.get(sessid);
		synchronized (lock) {
			sessionTable.put(sessid, ss);
		}
		return 1;
	}

	public void exchangeViews() {
		// TODO Auto-generated method stub
	}
}
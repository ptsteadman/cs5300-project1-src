import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.TreeMap;


public class RPCClient {
	private static final int portPROJ1BRPC = 5300;
	private Integer callID = 1;
	private static final String WRITE = "sessionWrite";
	private static final String READ = "sessionRead";
	private static final String VIEW = "exchangeView";
	private static final int UDP_PACKET_SIZE = 512;
	private RPCUser ru;

	
	public RPCClient() {}
	public RPCClient(RPCUser ru) {
		this.ru = ru;
	}
	
	public DatagramPacket sessionReadClient(SessionId sessid, ArrayList<String> destAddr) {
		DatagramSocket rpcSock = null;
		try {
			rpcSock = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		int cid = -1;
		synchronized (callID) {
			cid = callID;
		}
		byte[] outbuf = new byte[UDP_PACKET_SIZE];
		String outdata = "" + cid + "|" + READ + "|" + sessid.serialize();
		outbuf = outdata.getBytes();
		for (String ip : destAddr) {
			System.out.println("Ip received\t"+ip);
			if (ip == ru.getHost()) {
				continue;
			}
			assert(ip.split(".").length == 4);
			InetAddress addr = null;
			try {
				addr = InetAddress.getByName(ip);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			DatagramPacket sendPkt = new DatagramPacket(outbuf, outbuf.length, addr, portPROJ1BRPC);
			try {
				rpcSock.send(sendPkt);
				System.out.println("Packet Sent \t"+ip);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		byte[] inbuf = new byte[UDP_PACKET_SIZE];
		DatagramPacket recvPkt = new DatagramPacket(inbuf, inbuf.length);
		Integer recvCallID = -2;
		try {
			do {
				// handle garbage responses
				recvPkt.setLength(inbuf.length);
				rpcSock.receive(recvPkt);
				String inmsg = new String(recvPkt.getData());
				String[] tok = inmsg.split("\\|");
				assert(tok.length == 2);
				recvCallID = new Integer(tok[0]);
				SessionState ss = new SessionState(tok[1].trim());
				System.out.println("Packet recieved \t");

				if (ss.getVersion() == -1) { // handle garbage responses
					continue;
				}
			} while (recvCallID != cid);
		} catch(SocketTimeoutException e) {
			recvPkt = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		rpcSock.close();

		return recvPkt;
	}
	
	public DatagramPacket sessionWriteClient(SessionState ss, ArrayList<String> destAddr) {
		DatagramSocket rpcSock = null;
		try {
			rpcSock = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int cid = -1;
		synchronized (callID) {
			cid = callID;
		}
		byte[] outbuf = new byte[UDP_PACKET_SIZE];
		String outdata = "" + cid + "|" + WRITE + "|" + ss.serialize();
		outbuf = outdata.getBytes();
		for (String ip : destAddr) {
			assert(ip.split(".").length == 4);
			InetAddress addr = null;
			try {
				addr = InetAddress.getByName(ip);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			DatagramPacket sendPkt = new DatagramPacket(outbuf, outbuf.length, addr, portPROJ1BRPC);
			try {
				rpcSock.send(sendPkt);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		byte[] inbuf = new byte[UDP_PACKET_SIZE];
		DatagramPacket recvPkt = new DatagramPacket(inbuf, inbuf.length);
		Integer recvCallID = -2;
		try {
			do {
				// handle garbage responses
				recvPkt.setLength(inbuf.length);
				rpcSock.receive(recvPkt);
				String inmsg = new String(recvPkt.getData());
				String[] tok = inmsg.split("\\|");
				assert(tok.length == 2);
				recvCallID = new Integer(tok[0]);
				Integer rc = new Integer(tok[1].trim());
				if (rc != 1) { // handle garbage responses
					continue;
				}
			} while (recvCallID != cid);
		} catch(SocketTimeoutException e) {
			recvPkt = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		rpcSock.close();

		return recvPkt;
	}
	
	public void exchangeViews(View view, String ip) {
		DatagramSocket rpcSock = null;
		try {
			rpcSock = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int cid = -1;
		synchronized (callID) {
			cid = callID;
		}
		byte[] outbuf = new byte[UDP_PACKET_SIZE];
		String outdata = "" + cid + "|" + VIEW + "|" + ss.serialize();
		outbuf = outdata.getBytes();
		assert(ip.split(".").length == 4);
		InetAddress addr = null;
		try {
			addr = InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		DatagramPacket sendPkt = new DatagramPacket(outbuf, outbuf.length, addr, portPROJ1BRPC);
		try {
			rpcSock.send(sendPkt);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		byte[] inbuf = new byte[UDP_PACKET_SIZE];
		DatagramPacket recvPkt = new DatagramPacket(inbuf, inbuf.length);
		Integer recvCallID = -2;
		try {
			do {
				// handle garbage responses
				recvPkt.setLength(inbuf.length);
				rpcSock.receive(recvPkt);
				String inmsg = new String(recvPkt.getData());
				String[] tok = inmsg.split("\\|");
				assert(tok.length == 2);
				recvCallID = new Integer(tok[0]);
				Integer rc = new Integer(tok[1].trim());
				if (rc != 1) { // handle garbage responses
					continue;
				}
			} while (recvCallID != cid);
		} catch(SocketTimeoutException e) {
			recvPkt = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		rpcSock.close();

		return recvPkt;
	}
}
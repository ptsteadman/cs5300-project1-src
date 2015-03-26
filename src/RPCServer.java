import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


public class RPCServer extends Thread {
	private static final int portPROJ1BRPC = 5300;
	private RPCUser ru = null;
	private static final String WRITE = "sessionWrite";
	private static final String READ = "sessionRead";
	private static final String VIEW = "exchangeView";
	private static final int UDP_PACKET_SIZE = 512;
	
	public RPCServer() {}

	public RPCServer(RPCUser ru) {
		this.ru = ru;
	}
	
	@Override
	public void run() {
		DatagramSocket rpcSock = null;
		System.out.println("RPC Server online");
		try {
			rpcSock = new DatagramSocket(portPROJ1BRPC);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		while (true) {
			byte[] inbuf = new byte[UDP_PACKET_SIZE];
			DatagramPacket recvPkt = new DatagramPacket(inbuf, inbuf.length);
			try {
				System.out.println("RPC Server prepared to listen for requests");
				rpcSock.receive(recvPkt);
				System.out.println("RPC Server received request");
				InetAddress returnAddr = recvPkt.getAddress();
				int returnPort = recvPkt.getPort();
				byte[] outbuf = null;
				String inmsg = new String(recvPkt.getData());
				String[] msgTok = inmsg.split("\\|");
				assert(msgTok.length == 3);
				String callID = msgTok[0];
				String opcode = msgTok[1];
				String args = msgTok[2].trim();
				// we should probably set the view to "up" for this IP here, a centralized location?
				// parse and run correct RPCUser function based on opcode
				switch (opcode) {
				case WRITE:
					String wmsg = parseSessionWrite(args);
					wmsg = callID + "|" + wmsg;
					outbuf = wmsg.getBytes();
					DatagramPacket wsendPkt = new DatagramPacket(outbuf, outbuf.length, returnAddr, returnPort);
					rpcSock.send(wsendPkt);
					break;
				case READ:
					String rmsg = parseSessionRead(args);
					rmsg = callID + "|" + rmsg;
					outbuf = rmsg.getBytes();
					DatagramPacket rsendPkt = new DatagramPacket(outbuf, outbuf.length, returnAddr, returnPort);
					rpcSock.send(rsendPkt);
					break;
				case VIEW:
					String rview = ru.receiveExchangeViews(args);
					rmsg = callID + "|" + rview;
					outbuf = rmsg.getBytes();
					DatagramPacket rviewPkt = new DatagramPacket(outbuf, outbuf.length, returnAddr, returnPort);
					rpcSock.send(rviewPkt);
					break;
				default:
					throw new Exception("Illegal opcode");
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private String parseSessionRead(String args) {
		SessionId sid = new SessionId(args);
		SessionState ss = ru.sessionRead(sid.serialize());
		return ss.serialize();
	}
	
	private String parseSessionWrite(String args) {
		SessionState ss = new SessionState(args);
		int rc = ru.sessionWrite(ss);
		return "" + rc;
	}
	
	private void parseRecieveExchangeViews(String args) {
	}
	
}
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


public class RPCServer extends Thread {
	private static final int portPROJ1BRPC = 5300;
//	private RPCUser ru = null;
	
	public RPCServer() {}
	
//	public RPCServer(RPCUser ru) {} XXX: have servlet and SimpleDB implement RPCUser
	
	@Override
	public void run() {
		DatagramSocket rpcSock = null;
		try {
			rpcSock = new DatagramSocket(portPROJ1BRPC);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while (true) {
			byte[] inbuf = new byte[512];
			DatagramPacket recvPkt = new DatagramPacket(inbuf, inbuf.length);
			try {
				rpcSock.receive(recvPkt);
				InetAddress returnAddr = recvPkt.getAddress();
				int returnPort = recvPkt.getPort();
				byte[] outbuf = null;
				// parse and run correct RPCUser function based on opcode
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
}
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * @author Lauren Marsillo
 * @author Michael Gorokhovsky
 * @author Cassandra Ferworn
 * 
 *	HisCinema authoritative DNS
 */
public class HisDNS {

	static ArrayList<String> records = new ArrayList<String>();
	static String herIP;
	private static final int THISPORT = 6797;
	
	public static void main(String[] args) throws Exception {
		
		records.add("video.hiscinema.com herCDN.com V");
		records.add("herCDN.com NSherCDN.com NS");
		records.add("NSherCDN.com 127.0.0.1 A");
		
		DatagramSocket serverSocket = new DatagramSocket(THISPORT);
		byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];
        System.out.println("Waiting for message");
        while(true)
        {
        	//receiving message from the local DNS
           DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
           serverSocket.receive(receivePacket);
           String received = new String( receivePacket.getData());
           String type = received.substring(0, 1);
           String sentence =  received.substring(1, 30);
           System.out.println("RECEIVED: " + sentence);
           System.out.println("Of type: " + type);
           
           //finding the IP and returning it to the LocalDNS
           String name = sentence.substring(7, 26);
           findIP(name);
           sendData = herIP.getBytes();
           InetAddress IPAddress = receivePacket.getAddress();
           int port = receivePacket.getPort();
           DatagramPacket returnPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
           serverSocket.send(returnPacket);
        }
	}
	
	/*
	 * finds the IP associated with a name server
	 * @param name name of the record to search for
	 */
	public static void findIP(String name){
		for (String record : records){
			String [] recValues = record.split(" ");
			if (name.equals(recValues[0])){
				if (recValues[2].equals("A")){
					herIP = recValues[1];
					break;
				}
				else
					findIP(recValues[1]);
			}
		}
	}
}

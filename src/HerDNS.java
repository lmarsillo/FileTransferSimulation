import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

/**
* @author Lauren Marsillo
* @author Michael Gorokhovsky
* @author Cassandra Ferworn
* 
*	HerCDN authoritative DNS
*/
public class HerDNS {
	
	private final static int THISPORT = 6798;
	static ArrayList<String> records = new ArrayList<String>();
	static String ContentServerIP;
	
	public static void main(String[] args) throws Exception{
		
		records.add("herCDN.com www.herCDN.com CNAME");
        records.add("www.herCDN.com 127.0.0.1 A");
		
		DatagramSocket serverSocket = new DatagramSocket(THISPORT);
		byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];
        System.out.println("Waiting for message");
        while(true)
        {
        	DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            String received = new String( receivePacket.getData());
            String sentence =  received.substring(1, 30);
            System.out.println("Requesting the video: " + sentence);
            
            //finding the IP of the content server and returning it to the LocalDNS
            String name = "herCDN.com";
            findIP(name);
            sendData = ContentServerIP.getBytes();
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
					ContentServerIP = recValues[1];
					break;
				}
				else
					findIP(recValues[1]);
			}
		}
	}

}

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * @author Lauren Marsillo
 * @author Michael Gorokhovsky
 * @author Cassandra Ferworn
 * 
 *	Client's local DNS
 */
public class LocalDNS {
	
	static ArrayList<String> records = new ArrayList<String>();
	static String hisIP;
	private static final int LOCALDNSPORT = 6796;
	private static final int HISDNSPORT = 6797;
	private static final int HERDNSPORT = 6798;
	
	public static void main(String[] args) throws Exception {
		
		records.add("video.hiscinema.com hiscinema.com CNAME");
		records.add("hiscinema.com NShiscinema.com NS");
		records.add("NShiscinema.com 127.0.0.1 A");
		
		DatagramSocket DNSSocket = new DatagramSocket(LOCALDNSPORT);
		 byte[] receiveData = new byte[1024];
		 byte[] receiveDataFromHisDNS = new byte[1024];
		 byte[] receiveDataFromHerDNS = new byte[1024];
         byte[] sendData = new byte[1024];
         byte[] sendDataToClient = new byte [1024];
         System.out.println("Listining for packets...");
         while (true)
         {
        	 //receiving the query from the client
        	 DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
             DNSSocket.receive(receivePacket);
             String received = new String( receivePacket.getData());
             String type = received.substring(0, 1);
             String sentence =  received.substring(1, 30);
             
             String name = sentence.substring(7, 26);
             findIP(name);
             System.out.println(hisIP);
             
             System.out.println("RECEIVED: " + sentence + " Of type: " + type);
             
             //Sending packet to HisDNS
             DatagramSocket clientSocket = new DatagramSocket();
             InetAddress NextIPAddress = InetAddress.getByName(hisIP);
             String sent = received;
             sendData = sent.getBytes();
             DatagramPacket sendPacketHisDNS = new DatagramPacket(sendData, sendData.length, NextIPAddress, HISDNSPORT);
             clientSocket.send(sendPacketHisDNS);
             
             //The returned IP address from HisDNS
             DatagramPacket receivePacketFromHisDNS = new DatagramPacket(receiveDataFromHisDNS, receiveDataFromHisDNS.length);
             clientSocket.receive(receivePacketFromHisDNS);
             String herIP = new String(receivePacketFromHisDNS.getData()).substring(0,9);
             System.out.println("HerCDN DNS IP is: " + herIP);
             
             //Sending a query to herDNS server
             InetAddress herIPAdress = InetAddress.getByName(herIP);
             DatagramPacket sendPacketHerDNS = new DatagramPacket(sendData, sendData.length, herIPAdress, HERDNSPORT);
             clientSocket.send(sendPacketHerDNS);
             
             //The returned IP address from HerDNS
             DatagramPacket receivePacketFromHerDNS = new DatagramPacket(receiveDataFromHerDNS, receiveDataFromHerDNS.length);
             clientSocket.receive(receivePacketFromHerDNS);
             String contentServerIP = new String(receivePacketFromHerDNS.getData()).substring(0,9);
             System.out.println("content server IP is: " + contentServerIP);
             
             //Sending the content Server IP to the CLient
             InetAddress ClientIPAddress = receivePacket.getAddress();
             int port = receivePacket.getPort();
             sendDataToClient = contentServerIP.getBytes();
             DatagramPacket sendPacketToClient = new DatagramPacket(sendDataToClient, sendDataToClient.length, ClientIPAddress, port);
             DNSSocket.send(sendPacketToClient);
            		 
             clientSocket.close();
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
					hisIP = recValues[1];
					break;
				}
				else
					findIP(recValues[1]);
			}
		}
	}
}

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Lauren Marsillo
 * @author Michael Gorokhovsky
 * @author Cassandra Ferworn
 * 
 *	Client application
 */
public class Client {
	
	private final static String QUERYTYPE = "V";
	private final static int HISPORT = 6795;
	private final static int LOCALDNSPORT = 6796;
	private final static int CONTENTSERVERPORT = 6800;
	
	public static void main(String[] args) throws Exception {
		ArrayList<String> links = new ArrayList<String>();
		int fileSelection;
    	InetAddress DNSIPAddress = InetAddress.getByName("localhost");
    	
		//initializing socket and request
		Socket hisSocket = new Socket("localhost",HISPORT);
		String httpGet = "GET /index.html HTTP/1.1\nHost: localhost";
		
		//in and out streams from hisSocket
		DataOutputStream out = new DataOutputStream(hisSocket.getOutputStream());
		InputStreamReader isr =  new InputStreamReader(hisSocket.getInputStream());
        BufferedReader reader = new BufferedReader(isr);
		
		//sending httpGet
		out.writeBytes(httpGet);
		out.flush();
		
		//response from HisCinema with all of the links
		String httpResponse = reader.readLine();
		StringBuilder htmlcontent = new StringBuilder(1024);
    	String s;
    	while((s = reader.readLine()) != null){
    		htmlcontent.append(s);
    	}
    	httpResponse += htmlcontent;
    	System.out.println(httpResponse);
		
    	ArrayList<String> fileLinks = parseHTML(httpResponse);
    	for (String link : fileLinks){
    		links.add(link);
    	}
    	
    	//User input for which file to select
    	Scanner sc = new Scanner(System.in);
    	System.out.println("Enter the number of which file you would like (0 to 4): ");
    	fileSelection = sc.nextInt();
    	sc.close();
    	
    	//Sending the link to file of choice to local DNS
    	DatagramSocket clientSocket = new DatagramSocket();
    	byte[] sendData = new byte[1024];
        byte[] receiveData = new byte[1024];
        String selection = links.get(fileSelection);
        String sendMessage = QUERYTYPE + selection;
        sendData = sendMessage.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, DNSIPAddress, LOCALDNSPORT);
        clientSocket.send(sendPacket);
        
        //receiving content Server IP address from local DNS
        DatagramPacket receivePacketFromLocalDNS = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacketFromLocalDNS);
        String contentServerIP = new String(receivePacketFromLocalDNS.getData()).substring(0,9);
        System.out.println("content server IP is: " + contentServerIP);
        clientSocket.close();
        
        //sending selection to content server
        InetAddress csIP = InetAddress.getByName(contentServerIP);
        Socket herSocket = new Socket(csIP,CONTENTSERVERPORT);
        String herGet = "GET /F" + fileSelection + " HTTP/1.1\nHost: localhost";
        
        //in and out streams from hisSocket
        DataOutputStream herOut = new DataOutputStream(herSocket.getOutputStream());
      	DataInputStream dis = new DataInputStream(herSocket.getInputStream());
      	InputStreamReader herisr =  new InputStreamReader(herSocket.getInputStream());
        BufferedReader herReader = new BufferedReader(herisr);
        
        //sending httpGet
      	herOut.writeBytes(herGet);
      	herOut.flush();
      	
      	//response from content server
      	String herResponse = herReader.readLine();
      	String [] statusCode = herResponse.split(" ");
      	
      	//checks status code before accepting content
      	if (statusCode[1].equals("404")){
      		System.out.println("File not found");
      	} 
      	else if (statusCode[1].equals("200")){
      		String fsize = herReader.readLine();
      		String filetype = herReader.readLine();
      		System.out.println(filetype);
      		byte[] buffer = new byte[4096];
      		FileOutputStream fos = new FileOutputStream("F" + Integer.toString(fileSelection) + "received" + filetype);
          	
          	int filesize =  Integer.parseInt(fsize);
          	int read = 0;
          	int totalRead = 0;
          	int remaining = filesize;
          	while ((read = dis.read(buffer,0, Math.min(buffer.length, remaining))) > 0){
          		totalRead += read;
          		remaining -= read;
          		System.out.println("read " + totalRead + " bytes.");
          		fos.write(buffer,0,read);
          	}
          	fos.close();
      	}
	}
	
	/*
	 * Goes through index.html and finds the file links
	 * @param httpResponse html to parse
	 */
	public static ArrayList<String> parseHTML(String httpResponse){
		ArrayList<String> fileLinks = new ArrayList<String>();
		Scanner scanner = new Scanner(httpResponse);
		String line;
		scanner.useDelimiter("</a>");
		while(scanner.hasNext()){
			line = scanner.next();
			if (line.contains("http://video")){
				line = line.substring(line.indexOf("http://"), line.indexOf("http://") + 29);
				fileLinks.add(line);
			}
		}
		scanner.close();
		return fileLinks;
	}
}

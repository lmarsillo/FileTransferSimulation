import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * @author Lauren Marsillo
 * @author Michael Gorokhovsky
 * @author Cassandra Ferworn
 * 
 *	HisCinema web server
 */
public class HisCinema {
	
	private final static int PORT = 6795;
	
	//starts web server
	public static void main(String args[] ) throws IOException {
		ServerSocket server = new ServerSocket(PORT);
		System.out.println("Listining for connection on port...");
        while (true) {
        	try {
        		Socket clientSocket = server.accept();
        		new Thread(new HisHandleRequest(clientSocket)).start();
            } catch (IOException e){
            	e.printStackTrace();
            }
        }
	}
}

/**
 * Class that handles the get requests as they come in
 */
class HisHandleRequest implements Runnable{
	
	private final Socket socket;
	
	public HisHandleRequest (Socket socket){
		this.socket = socket;
	}
	
	public void run(){
		try{
			InputStreamReader isr =  new InputStreamReader(socket.getInputStream());
	        BufferedReader reader = new BufferedReader(isr);
	        String request = reader.readLine(); 
	        String [] parameters = request.split(" ");
	        String filepath = parameters[1];
	        filepath = filepath.substring(1);
	        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
	        File file = new File(filepath);
	        String httpResponse;
	        
	        //checks if requested file exists, and sends appropriate status code
	        if(!file.exists()){
	        	httpResponse = "HTTP/1.1 404 Not Found\n\n" + "File not found\n";
	        	out.writeBytes(httpResponse);
	        }else{
	        	httpResponse = "HTTP/1.1 200 OK\n\n";
	        	FileReader fr = new FileReader(file);
	        	BufferedReader bfr = new BufferedReader(fr);
	        	StringBuilder htmlcontent = new StringBuilder(1024);
	        	String s;
	        	while((s = bfr.readLine()) != null){
	        		htmlcontent.append(s);
	        	}
	        	httpResponse += htmlcontent;
	        	out.writeBytes(httpResponse);
	        	bfr.close();
	        }
	        socket.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	
}

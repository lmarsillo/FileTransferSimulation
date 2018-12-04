import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
public class HerCDN {
	
	private static final int PORT = 6800;
	
	 public static void main(String args[]) throws Exception
     {
		 //starts web server
		 ServerSocket server = new ServerSocket(PORT);
		 System.out.println("Listening on port " + PORT + "...");
		 while (true){
			 try {
				 Socket clientSocket = server.accept();
				 new Thread(new HerHandleRequest(clientSocket)).start();
			 } catch (IOException e){
				 e.printStackTrace();
			 }
		 }
     }
}

/**
 * Class that handles incoming requests for files
 */
class HerHandleRequest implements Runnable{
	
	private final Socket socket;
	static String f0 = "3284257";
	static String f1 = "17059";
	static String f2 = "229455";
	static String f3 = "87";
	static String f4 = "471494";
	
	public HerHandleRequest (Socket socket){
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
            File file;
            String filesize;
            String filetype;
            
            //ensures correct file is chosen depending on client's input
            if (filepath.equals("F0")){
            	file = new File("F0.mov");
            	filetype = ".mov";
            	filesize = f0;
            }	
            else if (filepath.equals("F1")){
            	file = new File("F1.jpg");
            	filetype = ".jpg";
            	filesize = f1;
            }
            else if (filepath.equals("F2")){
            	file = new File("F2.webm");
            	filetype = ".webm";
            	filesize = f2;
            }
            else if (filepath.equals("F3")){
            	file = new File("F3.txt");
            	filetype = ".txt";
            	filesize = f3;
            }
            else if (filepath.equals("F4")){
            	file = new File("F4.mp4");
            	filetype = ".mp4";
            	filesize = f4;
            }
            else {
            	file = new File("shouldntexist.txt");
            	filesize = "doesn't matter";
            	filetype = "doesn't matter";
            }
            
            //checks if file exists and sends appropriate status code
            String httpResponse;
            if(!file.exists()){
            	httpResponse = "HTTP/1.1 404 Not Found\n" + "File not found\n";
            	out.writeBytes(httpResponse);
            }else{
            	System.out.println(filetype);
            	httpResponse = "HTTP/1.1 200 OK\n" + filesize + "\n" + filetype + "\n";
            	out.writeBytes(httpResponse);
            	
            	FileInputStream fis = new FileInputStream(file);
            	byte [] buffer = new byte[4096];
            	
            	while (fis.read(buffer) > 0){
            		out.write(buffer);
            	}
            	
            	fis.close();
            	out.close();
            }
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	
}

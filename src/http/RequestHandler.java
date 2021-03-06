package http;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class RequestHandler extends Thread {
	private static final String DOCUMENT_ROOT = "./webapp";

	private Socket socket;

	public RequestHandler(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			// logging Remote Host IP Address & Port
			InetSocketAddress inetSocketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
			consoleLog("connected from " + inetSocketAddress.getAddress().getHostAddress() + ":"
					+ inetSocketAddress.getPort());

			// get IOStream 소켓의 바이트  데이터를  문자열 데이터로 그리고  라인단위로  받는다
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8")); //byte를 line단위로 읽음//3byte를 char단위스트림 utf-8로 읽기위해 inputStreamReader로 바꾼다.//한줄씩 읽기 위해 bufferedreade로 생성
			OutputStream os = socket.getOutputStream(); //읽을 때는  line단위로 쓸때는 byte로 쓰기 위해 주output스트림만 생성
			 
			String request =null;
			while(true) {
				String line = br.readLine();
				if( line == null || "".equals(line)) { //해더부분만 읽겠다 바디 안읽겠다
					break;
				}
				if ( request == null) {
					request = line;
					
					String protocol ="";
					String url="";
					String[] tokens = request.split( " " );
					
					
					url = tokens[1];
					if(url == null || "".equals(url)|| "/".equals(url)) {
						url="/index.html";
					}
					protocol = tokens[2];
					//tokens[0] 가 “GET” 인 경우만 responseStaticResource 를 호출
					if(tokens[0].equals("GET")) {
						responseStaticResource( os, url, protocol );
					}else {
						response404Error( os, protocol );
					}
					
					 
					break;
					
				}
			}
						
			//consoleLog( "request : " + request );
			// 예제 응답입니다.
			// 서버 시작과 테스트를 마친 후, 주석 처리 합니다. //브라우져에 쓴다
			/**
			os.write( "HTTP/1.1 200 OK\r\n".getBytes( "UTF-8" ) );
			os.write( "Content-Type:text/html; charset=utf-8\r\n".getBytes( "UTF-8" ) );
			os.write( "\r\n".getBytes() );
			os.write( "<h1>이 페이지가 잘 보이면 실습과제 SimpleHttpServer를 시작할 준비가 된 것입니다.</h1>".getBytes( "UTF-8" ) );
			**/
		} catch ( Exception ex ) {
			consoleLog( "error:" + ex );
		} finally {
			// clean-up
			try {
				if ( socket != null && socket.isClosed() == false ) {
					socket.close();
				}
			} catch ( IOException ex)  {
				consoleLog( "error:" + ex );
			}
		}
	}
	
	
	private void response404Error(OutputStream outputStream, String protocol) throws IOException  {
		File file = new File( "./webapp/error/404.html" );
		Path path = file.toPath();
		
		String mimeType = Files.probeContentType(path);
		byte[] body = Files.readAllBytes( path );
		
        String msg = protocol + " 404 File Not Found\r\n";
		outputStream.write( msg.getBytes() );
		String contentType = "Content-Type:"+mimeType+"\r\n";
		outputStream.write( contentType.getBytes( "UTF-8" ) );
		outputStream.write( "\r\n".getBytes() ); 
		outputStream.write( body );

	}

	private void responseStaticResource( OutputStream outputStream, String url, String protocol ) throws IOException {
		File file = new File( "./webapp" + url );
		
		if ( file.exists() == false ) {
		     response404Error( outputStream, protocol );
		     return;
		}

		
		Path path = file.toPath();
		String mimeType = Files.probeContentType( path );
		byte[] body = Files.readAllBytes( path );
		outputStream.write( "HTTP/1.1 200 OK\r\n".getBytes( "UTF-8" ));
		String contentType = "Content-Type:"+mimeType+"\r\n";
		outputStream.write( contentType.getBytes( "UTF-8" ) );
		outputStream.write( "\r\n".getBytes() );
		outputStream.write( body );

    }  
  
	private void consoleLog(String message) {
		System.out.println("[RequestHandler#" + getId() + "] " + message);
	}
}
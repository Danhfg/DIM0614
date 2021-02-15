package br.imd.distribuida.trabalho2.servers;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.google.gson.Gson;

import br.imd.distribuida.trabalho2.models.ServerResponse;


public class TCPServerPredict {
	private static final int BUFFER_SIZE = 5120;
	
	private static Selector selector = null;
	
	private Gson gson = new Gson();	
	
	private Algorithm algorithm = Algorithm.HMAC256("AOsD89f&*Fujalo()*");

	public TCPServerPredict() {
		logger("Starting MySelectorClientExample...");
		try {
			InetAddress hostIP= InetAddress.getLocalHost();
			int port = 9999;
			logger(String.format("Trying to accept connections on %s:%d...",
			hostIP.getHostAddress(), port));
			selector = Selector.open();
			ServerSocketChannel mySocket = ServerSocketChannel.open();
			ServerSocket serverSocket = mySocket.socket();
			InetSocketAddress address = new InetSocketAddress(hostIP, port);
			serverSocket.bind(address);
			mySocket.configureBlocking(false);
			
			mySocket.register(selector,SelectionKey.OP_ACCEPT);
			
			while(true) {
				selector.select();
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> i = selectedKeys.iterator();
				while (i.hasNext()) {
					SelectionKey key = i.next();
					if (key.isAcceptable()) {
						processAcceptEvent(mySocket, key);
					} else if (key.isReadable()) {
						processReadEvent(key);
					}
					i.remove();
				}
			}			
		} catch (IOException e) {
			logger(e.getMessage());
			e.printStackTrace();
		}
		
	}

	private static void processAcceptEvent(ServerSocketChannel mySocket,
			SelectionKey key) throws IOException {
		logger("Connection Accepted...");
		// Accept the connection and make it non-blocking
		SocketChannel myClient = mySocket.accept();
		myClient.configureBlocking(false);
		// Register interest in reading this channel
		myClient.register(selector, SelectionKey.OP_READ);
	}
	
	private void processReadEvent(SelectionKey key)
		throws IOException {
		logger("Inside processReadEvent...");
		// create a ServerSocketChannel to read the request
		SocketChannel myClient = (SocketChannel) key.channel();
		// Set up out 1k buffer to read data into
		ByteBuffer myBuffer = ByteBuffer.allocate(BUFFER_SIZE);
		myClient.read(myBuffer);
		String data = new String(myBuffer.array()).trim();
		logger(data);
		if (data.length() > 0) {
			logger(String.format("Message Received.....: %s\n", data));

			if(data.contains("PING")) {
				myBuffer.clear();
				myBuffer.put("PONG".getBytes());
				myBuffer.flip();
				myClient.write(myBuffer);	
			}
			else {

			    JWTVerifier verifier = JWT.require(algorithm)
			        .withIssuer("predictor")
			        .build(); //Reusable verifier instance
			    DecodedJWT jwt = verifier.verify(data.replace("Bearer ", ""));
			    
				String e = jwt.getClaim("user").asString();
				System.out.println("User: " + e);
				
				File f = new File("data/predictions/"+e+".tsv");
				if(!f.exists()) {
					System.out.println("NOT FOUND");
					
					byte[] sendMessage;
		            
		            ServerResponse sr = new ServerResponse(true, "Nenhuma solicitação de predição  encontrada ou finalizada!");
					String srJson = gson.toJson(sr);
					sendMessage = srJson.getBytes();
					ByteBuffer myBufferClient = ByteBuffer.allocate(BUFFER_SIZE);
					myBufferClient.put(sendMessage);
					myBufferClient.flip();
					myClient.write(myBufferClient);
					myClient.close();
		            
					System.out.println("Retornando error");
				}
				else {
					String results = Files.readString(Paths.get("data/predictions/"+e+".tsv"));

					byte[] sendMessage;
					
		            ServerResponse sr = new ServerResponse(false, results);
					String srJson = gson.toJson(sr);
					sendMessage = srJson.getBytes();
					ByteBuffer myBufferClient = ByteBuffer.allocate(BUFFER_SIZE);
					myBufferClient.put(sendMessage);
					myBufferClient.flip();
					myClient.write(myBufferClient);
					myClient.close();
					
					System.out.println("Retornando resultados");					
				}
			}
		}
		myClient.close();
	}
	
	public static void logger(String msg) {
		System.out.println(msg);
	}

	public static void main(String[] args) {
		new TCPServerPredict();

	}

}

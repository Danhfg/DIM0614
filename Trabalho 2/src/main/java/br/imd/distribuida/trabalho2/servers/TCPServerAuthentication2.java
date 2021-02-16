package br.imd.distribuida.trabalho2.servers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.gson.Gson;

import br.imd.distribuida.trabalho2.models.ServerResponse;
import br.imd.distribuida.trabalho2.models.User;


public class TCPServerAuthentication2 {
	
	private static final int BUFFER_SIZE = 1024;
	
	private static Selector selector = null;

	private Gson gson = new Gson();

	private Algorithm algorithm = Algorithm.HMAC256("AOsD89f&*Fujalo()*");

	public TCPServerAuthentication2() {
		logger("Starting Authentication Server...");
		try {
			InetAddress hostIP= InetAddress.getLocalHost();
			int port = 7778;
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
				User user = gson.fromJson(data, User.class);
				if(user.getType() == 1) {
					File myObj = new File("users.tsv");
					Scanner myReader = new Scanner(myObj);
					Boolean bool = false;
					System.out.println("faishdufiashduf");
					while (myReader.hasNextLine()) {
						System.out.println("faishdufiashduf");
						String userline = myReader.nextLine();
						if(userline.split(";")[0].equals(user.getUser())) {
							bool = true;
						}
					}
					myReader.close();
					if(bool) {
						byte[] sendMessage;
			            
			            ServerResponse sr = new ServerResponse(true, "Usuátio já cadastrado!");
						String srJson = gson.toJson(sr);
						sendMessage = srJson.getBytes();
						ByteBuffer myBufferClient = ByteBuffer.allocate(BUFFER_SIZE);
						myBufferClient.put(sendMessage);
						myBufferClient.flip();
						myClient.write(myBufferClient);
			            
						System.out.println("Retornando error");
					}
					else {
						BufferedWriter bw = new BufferedWriter(new FileWriter("users.tsv", true)); 
						bw.write(user.getUser()+";"+user.getPassword());
						bw.newLine();
						bw.close();
						System.out.println("Usuário cadastrado!");

						byte[] sendMessage;

			            ServerResponse sr = new ServerResponse(false, "Uuário cadastrado com sucesso!");
						String srJson = gson.toJson(sr);
						sendMessage = srJson.getBytes();
						ByteBuffer myBufferClient = ByteBuffer.allocate(BUFFER_SIZE);
						myBufferClient.put(sendMessage);
						myBufferClient.flip();
						myClient.write(myBufferClient);
					}
				}
				else if(user.getType() == 2) {
					File myObj = new File("users.tsv");
					Scanner myReader = new Scanner(myObj);
					Boolean bool = false;
					while (myReader.hasNextLine()) {
						String userline = myReader.nextLine();
						if(userline.split(";")[0].equals(user.getUser()) && 
							userline.split(";")[1].equals(user.getPassword())) {
							bool = true;
							break;
						}
					}
					myReader.close();
					if(bool) {
						System.out.println("Retornando Token");
						String token = JWT.create()
						        .withClaim("user", user.getUser())
						        .withIssuer("predictor")
						        .sign(algorithm);
						System.out.println(token);

						byte[] sendMessage;
			            
			            ServerResponse sr = new ServerResponse(false, token);
						String srJson = gson.toJson(sr);
						sendMessage = srJson.getBytes();
						ByteBuffer myBufferClient = ByteBuffer.allocate(BUFFER_SIZE);
						myBufferClient.put(sendMessage);
						myBufferClient.flip();
						myClient.write(myBufferClient);
			            
						System.out.println("Retornando token");
					}
					else {
						byte[] sendMessage;

			            ServerResponse sr = new ServerResponse(true, "Uuário ou senha inválidos");
						String srJson = gson.toJson(sr);
						sendMessage = srJson.getBytes();
						ByteBuffer myBufferClient = ByteBuffer.allocate(BUFFER_SIZE);
						myBufferClient.put(sendMessage);
						myBufferClient.flip();
						myClient.write(myBufferClient);
						
						System.out.println("Retornando erro!");
					}
				}
			}
		}
		myClient.close();
	}
	
	public static void logger(String msg) {
		System.out.println(msg);
	}

	public static void main(String[] args) {
		new TCPServerAuthentication2();

	}

}

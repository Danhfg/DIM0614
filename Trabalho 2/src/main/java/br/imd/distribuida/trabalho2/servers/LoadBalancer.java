package br.imd.distribuida.trabalho2.servers;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import com.google.gson.Gson;

import br.imd.distribuida.trabalho2.models.Message;

public class LoadBalancer {
	
	private int port = 3333;

	private ArrayList<Integer> portsServerAuth = new ArrayList<Integer>();
	private ArrayList<Integer> portsServerDB = new ArrayList<Integer>();
	private ArrayList<Integer> portsServerPred= new ArrayList<Integer>();

	private Gson gson = new Gson();
	
	private Integer tempAuth;
	private Integer removedAuth;

	private Integer tempDB;
	private Integer removedDB;

	private Integer tempPred;
	private Integer removedPred;
	
	private static final int BUFFER_SIZE = 1024;
	
	private static Selector selector = null;
	
	private void updateServersAuth() {
		System.out.println(portsServerAuth);
		InetAddress hostIP;
		try {
			for (Integer integer : portsServerAuth) {
				System.out.println(integer);
				tempAuth = integer;
				hostIP = InetAddress.getLocalHost();

				InetSocketAddress myAddress =
						new InetSocketAddress(hostIP, integer);
				SocketChannel myClient= SocketChannel.open(myAddress);
				byte[] sendMessage;
				String ping = "PING";
				sendMessage = ping.getBytes();
				ByteBuffer serverBuffer = ByteBuffer.allocate(BUFFER_SIZE);
				serverBuffer.put(sendMessage);
				serverBuffer.flip();
				myClient.write(serverBuffer);
				myClient.read(serverBuffer);
				myClient.close();
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConnectException e ) {
			removedAuth = portsServerAuth.remove(portsServerAuth.indexOf(tempAuth)); 
			logger("Remove " + removedAuth);
			updateServersAuth();
			//e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	private void updateServersPred() {
		System.out.println(portsServerPred);
		InetAddress hostIP;
		try {
			for (Integer integer : portsServerPred) {
				System.out.println(integer);
				tempPred = integer;
				hostIP = InetAddress.getLocalHost();

				InetSocketAddress myAddress =
						new InetSocketAddress(hostIP, integer);
				SocketChannel myClient= SocketChannel.open(myAddress);
				byte[] sendMessage;
				String ping = "PING";
				sendMessage = ping.getBytes();
				ByteBuffer serverBuffer = ByteBuffer.allocate(BUFFER_SIZE);
				serverBuffer.put(sendMessage);
				serverBuffer.flip();
				myClient.write(serverBuffer);
				myClient.read(serverBuffer);
				myClient.close();
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConnectException e ) {
			removedPred= portsServerPred.remove(portsServerPred.indexOf(tempPred)); 
			logger("Remove " + removedPred);
			updateServersPred();
			//e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public LoadBalancer() {
		System.out.println("ITP Load Balancer Server Started");
		
		portsServerAuth.add(7777);
		portsServerAuth.add(7778);
		portsServerDB.add(8888);
		portsServerDB.add(8889);
		portsServerPred.add(9999);
		portsServerPred.add(9998);
		try {
			InetAddress hostIP= InetAddress.getLocalHost();
			int port = 3333;
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
			
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private void processAcceptEvent(ServerSocketChannel mySocket,
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
		data = data.replaceAll("\u0000.*", "");
		if(!data.contains("Bearer ")) {
			Message mes = gson.fromJson(data, Message.class);
			Integer serv = mes.getServer();
			System.out.println(serv);
			switch(serv) {
				case 1:{
					updateServersAuth();
					InetAddress hostIP= InetAddress.getLocalHost();
					InetSocketAddress myAddress =
							new InetSocketAddress(hostIP, portsServerAuth.get(0));
					logger(String.format("Trying to connect to %s:%d...",
					myAddress.getHostName(), myAddress.getPort()));
					//myBuffer.put(data.getBytes());
					myBuffer.flip();
					SocketChannel serverAuth = SocketChannel.open(myAddress);
					int bytesWritten= serverAuth.write(myBuffer);
					logger(String
					.format("Sending Message...: %s\nbytesWritten...: %d",
							data, bytesWritten));
					ByteBuffer myBufferServer = ByteBuffer.allocate(BUFFER_SIZE);
					serverAuth.read(myBufferServer);
					serverAuth.close();
					logger(new String(myBufferServer.array()).trim() );
					myBufferServer.flip();
					myClient.write(myBufferServer);
					//myClient.read(myBuffer);
					myClient.close();
					break;
				}
			}
		}
		else {
			updateServersPred();
			
			InetAddress hostIP= InetAddress.getLocalHost();
			InetSocketAddress myAddress =
					new InetSocketAddress(hostIP, portsServerPred.get(0));
			logger(String.format("Trying to connect to %s:%d...",
			myAddress.getHostName(), myAddress.getPort()));
			//myBuffer.put(data.getBytes());
			myBuffer.flip();
			SocketChannel serverPred = SocketChannel.open(myAddress);
			int bytesWritten= serverPred.write(myBuffer);
			logger(String
			.format("Sending Message...: %s\nbytesWritten...: %d",
					data, bytesWritten));
			ByteBuffer myBufferServer = ByteBuffer.allocate(BUFFER_SIZE);
			serverPred.read(myBufferServer);
			serverPred.close();
			logger(new String(myBufferServer.array()).trim() );
			myBufferServer.flip();
			myClient.write(myBufferServer);
			//myClient.read(myBuffer);
			myClient.close();
			
		}
		myClient.close();
	}
	
	public static void logger(String msg) {
		System.out.println(msg);
	}

	public static void main(String[] args) {
		new LoadBalancer();
	}

}

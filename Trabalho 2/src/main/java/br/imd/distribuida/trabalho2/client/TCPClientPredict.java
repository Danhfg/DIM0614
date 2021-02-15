package br.imd.distribuida.trabalho2.client;

import java.io.IOException;
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

import com.google.gson.Gson;

import br.imd.distribuida.trabalho2.models.Predict;
import br.imd.distribuida.trabalho2.models.ServerResponse;
import br.imd.distribuida.trabalho2.models.User;

public class TCPClientPredict {
	
	private Gson gson = new Gson();
	
	private String token;
	
	private static final int BUFFER_SIZE = 1024;

	public TCPClientPredict() {
		System.out.print("Starting MySelectorClientExample...");
		Scanner scanner = new Scanner(System.in);
		try {
			InetAddress hostIP= InetAddress.getLocalHost();
			int port = 3333;
			
			String message = "";
			
			int portAuth = 3333;
			int portDB= 3333;
			int portPred = 3333;
			

			while(true) {
				System.out.print("Deseja se [C]adastrar ou [E]ntrar: ");
				message = scanner.nextLine();
				if ("quit".equalsIgnoreCase(message)) {
					break;
				}
				if(message.equalsIgnoreCase("C")) {
					ByteBuffer myBuffer = ByteBuffer.allocate(BUFFER_SIZE);

					System.out.print("Login: ");
					String email = scanner.nextLine();
					System.out.print("Senha: ");
					String password = scanner.nextLine();
					User user = new User(1, 1, email, password);
					String userJson = gson.toJson(user);

					InetSocketAddress myAddress =
							new InetSocketAddress(hostIP, port);
					SocketChannel myClient= SocketChannel.open(myAddress);
					logger(String.format("Trying to connect to %s:%d...",
					myAddress.getHostName(), myAddress.getPort()));
					myBuffer.put(userJson.getBytes());
					myBuffer.flip();
					int bytesWritten= myClient.write(myBuffer);
					//logger(String
					//.format("Sending Message...: %s\nbytesWritten...: %d",
					//		userJson, bytesWritten));
					ByteBuffer myBufferServer = ByteBuffer.allocate(BUFFER_SIZE);
					myClient.read(myBufferServer);
					myClient.close();
					
					myBufferServer.flip();
					
					String serverResponse = new String(new String(myBufferServer.array()).trim());
					serverResponse = serverResponse.replaceAll("\u0000.*", "");
					
					ServerResponse sr = gson.fromJson(serverResponse, ServerResponse.class);
					
					if(sr.getError()) {
						System.out.println("Erro: " + sr.getMessage());
					}else {
						System.out.println("Sucesso: " + sr.getMessage());			
					}
				}
				else if(message.equalsIgnoreCase("E")) {
					ByteBuffer myBuffer = ByteBuffer.allocate(BUFFER_SIZE);

					System.out.print("Login: ");
					String email = scanner.nextLine();
					System.out.print("Senha: ");
					String password = scanner.nextLine();
					User user = new User(1, 2, email, password);
					String userJson = gson.toJson(user);

					InetSocketAddress myAddress =
							new InetSocketAddress(hostIP, port);
					SocketChannel myClient= SocketChannel.open(myAddress);
					logger(String.format("Trying to connect to %s:%d...",
					myAddress.getHostName(), myAddress.getPort()));
					myBuffer.put(userJson.getBytes());
					myBuffer.flip();
					int bytesWritten= myClient.write(myBuffer);
					//logger(String
					//.format("Sending Message...: %s\nbytesWritten...: %d",
					//		userJson, bytesWritten));
					ByteBuffer myBufferServer = ByteBuffer.allocate(BUFFER_SIZE);
					myClient.read(myBufferServer);
					myClient.close();

					myBufferServer.flip();

					String serverResponse = new String(new String(myBufferServer.array()).trim());
					serverResponse = serverResponse.replaceAll("\u0000.*", "");
					ServerResponse sr = gson.fromJson(serverResponse, ServerResponse.class);
					if(sr.getError()) {
						logger("Erro: " + sr.getMessage());
					}else {
						logger("Sucesso!");
						this.token = sr.getMessage();
						break;
					}
				}
			}
			
			while(true) {
				ByteBuffer myBuffer = ByteBuffer.allocate(BUFFER_SIZE);
				System.out.print("Deseja se [S]olicitar novas predições ou [V]isualizar suas predições: ");
				message = scanner.nextLine();
				if ("quit".equalsIgnoreCase(message)) {
					break;
				}
				else if("S".equalsIgnoreCase(message)) {
					byte[] sendMessage;
					
					System.out.print("Chromossomo: ");
					String chr = scanner.nextLine();
					System.out.print("Posição: ");
					String pos = scanner.nextLine();
					System.out.print("Nucleotídeo Referência: ");
					String ref= scanner.nextLine();
					System.out.print("Nucleotídeo Alternante: ");
					String alt= scanner.nextLine();
					System.out.print("Paciente: ");
					String pat= scanner.nextLine();
					
					Predict pred = new Predict(2, chr, Integer.valueOf(pos), ref.charAt(0), alt.charAt(0), pat, token);
					
					String predJson = gson.toJson(pred);
					
					InetSocketAddress myAddress =
							new InetSocketAddress(hostIP, port);
					SocketChannel myClient= SocketChannel.open(myAddress);
					
					sendMessage = predJson.getBytes();
					myBuffer.put(sendMessage);
					myBuffer.flip();
					int bytesWritten= myClient.write(myBuffer);
					logger(String
					.format("Sending Message...: %s\nbytesWritten...: %d",
							predJson, bytesWritten));
					ByteBuffer myBufferServer = ByteBuffer.allocate(BUFFER_SIZE);
					myClient.read(myBufferServer);
					myClient.close();
					
					myBufferServer.flip();
					
					String serverResponse = new String(new String(myBufferServer.array()).trim());
					serverResponse = serverResponse.replaceAll("\u0000.*", "");
					
					ServerResponse sr = gson.fromJson(serverResponse, ServerResponse.class);
					
					if(sr.getError()) {
						System.out.println("Erro: " + sr.getMessage());
					}else {
						System.out.println("Sucesso: " + sr.getMessage());			
					}
					
					
				}
				else if("V".equalsIgnoreCase(message)) {
					byte[] sendMessage;
					String tokenRequest = "Bearer " + token;
					sendMessage = tokenRequest.getBytes();

					InetSocketAddress myAddress =
							new InetSocketAddress(hostIP, port);
					SocketChannel myClient= SocketChannel.open(myAddress);
					logger(String.format("Trying to connect to %s:%d...",
					myAddress.getHostName(), myAddress.getPort()));
					myBuffer.put(sendMessage);
					myBuffer.flip();
					int bytesWritten= myClient.write(myBuffer);
					//logger(String
					//.format("Sending Message...: %s\nbytesWritten...: %d",
					//		userJson, bytesWritten));
					ByteBuffer myBufferServer = ByteBuffer.allocate(BUFFER_SIZE);
					myClient.read(myBufferServer);
					myClient.close();
					
					myBufferServer.flip();
					
					String serverResponse = new String(new String(myBufferServer.array()).trim());
					serverResponse = serverResponse.replaceAll("\u0000.*", "");
					
					ServerResponse sr = gson.fromJson(serverResponse, ServerResponse.class);
					
					if(sr.getError()) {
						System.out.println("Erro: " + sr.getMessage());
					}else {
						System.out.println("Sucesso: " + sr.getMessage());			
					}
				}
				
			}
			
			//logger("Closing Client connection...");
			//myClient.close();			
		} catch (IOException e) {
			logger(e.getMessage());
			e.printStackTrace();
		}
		
	}

	public static void logger(String msg) {
	System.out.println(msg);
	}

	public static void main(String[] args) {
		new TCPClientPredict();

	}

}

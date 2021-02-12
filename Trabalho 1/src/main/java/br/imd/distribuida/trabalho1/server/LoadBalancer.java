package br.imd.distribuida.trabalho1.server;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.google.gson.Gson;

import br.imd.distribuida.trabalho1.models.Message;
import br.imd.distribuida.trabalho1.models.Predict;
import br.imd.distribuida.trabalho1.models.User;

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
	
	private void updateServersAuth() {
		InetAddress inetAddress;
		System.out.println(portsServerAuth);
		try {
			inetAddress = InetAddress.getByName("localhost");
			DatagramSocket clientSocket = new DatagramSocket();
			clientSocket.setSoTimeout(500);
			for (Integer integer : portsServerAuth) {
				tempAuth = integer;
				
				byte[] sendMessage;
				
				byte[] receiveMessage = new byte[5120];
				DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);
				
				String ping = "PING";
				sendMessage = ping.getBytes();
				
				DatagramPacket sendPacket = new DatagramPacket(
						sendMessage, sendMessage.length,
						inetAddress, integer);
				clientSocket.send(sendPacket);
				clientSocket.receive(receivePacket);
				String serverResponse = new String(receivePacket.getData());
				serverResponse = serverResponse.replaceAll("\u0000.*", "");
			}
			portsServerAuth.add(portsServerAuth.get(0));
			portsServerAuth.remove(0);
			System.out.println(portsServerAuth);
			clientSocket.close();
		} catch (UnknownHostException e) {
			updateServersAuth();
			e.printStackTrace();
			removedAuth = portsServerAuth.remove(portsServerAuth.indexOf(tempAuth));
		}catch (SocketTimeoutException e) {
			e.printStackTrace();
			removedAuth = portsServerAuth.remove(portsServerAuth.indexOf(tempAuth));
			updateServersAuth();
		}
		catch (InterruptedIOException e) {
			e.printStackTrace();
			portsServerAuth.remove(portsServerAuth.indexOf(tempAuth));
			updateServersAuth();
		}catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void updateServersDB() {
		InetAddress inetAddress;
		System.out.println(portsServerDB);
		try {
			inetAddress = InetAddress.getByName("localhost");
			DatagramSocket clientSocket = new DatagramSocket();
			clientSocket.setSoTimeout(500);
			for (Integer integer : portsServerDB) {
				tempDB = integer;
				
				byte[] sendMessage;
				
				byte[] receiveMessage = new byte[5120];
				DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);
				
				String ping = "PING";
				sendMessage = ping.getBytes();
				
				DatagramPacket sendPacket = new DatagramPacket(
						sendMessage, sendMessage.length,
						inetAddress, integer);
				clientSocket.send(sendPacket);
				clientSocket.receive(receivePacket);
				String serverResponse = new String(receivePacket.getData());
				serverResponse = serverResponse.replaceAll("\u0000.*", "");
			}
			portsServerDB.add(portsServerDB.get(0));
			portsServerDB.remove(0);
			clientSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			removedDB = portsServerDB.remove(portsServerDB.indexOf(tempDB));
			updateServersDB();
		}catch (SocketTimeoutException e) {
			e.printStackTrace();
			removedDB = portsServerDB.remove(portsServerDB.indexOf(tempDB));
			updateServersDB();
		}
		catch (InterruptedIOException e) {
			e.printStackTrace();
			portsServerDB.remove(portsServerDB.indexOf(tempDB));
			updateServersDB();
		}catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void updateServersPred() {
		InetAddress inetAddress;
		System.out.println(portsServerPred);
		try {
			inetAddress = InetAddress.getByName("localhost");
			DatagramSocket clientSocket = new DatagramSocket();
			clientSocket.setSoTimeout(500);
			for (Integer integer : portsServerPred) {
				tempPred = integer;
				
				byte[] sendMessage;
				
				byte[] receiveMessage = new byte[5120];
				DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);
				
				String ping = "PING";
				sendMessage = ping.getBytes();
				
				DatagramPacket sendPacket = new DatagramPacket(
						sendMessage, sendMessage.length,
						inetAddress, integer);
				clientSocket.send(sendPacket);
				clientSocket.receive(receivePacket);
				String serverResponse = new String(receivePacket.getData());
				serverResponse = serverResponse.replaceAll("\u0000.*", "");
			}
			portsServerPred.add(portsServerPred.get(0));
			portsServerPred.remove(0);
			clientSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			removedPred = portsServerPred.remove(portsServerPred.indexOf(tempPred));
			updateServersPred();
		}catch (SocketTimeoutException e) {
			e.printStackTrace();
			removedPred = portsServerPred.remove(portsServerPred.indexOf(tempPred));
			updateServersPred();
		}
		catch (InterruptedIOException e) {
			e.printStackTrace();
			portsServerPred.remove(portsServerPred.indexOf(tempPred));
			updateServersPred();
		}catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public LoadBalancer() {
		System.out.println("UDP Load Balancer Server Started");
		
		portsServerAuth.add(7777);
		portsServerAuth.add(7778);
		portsServerDB.add(8888);
		portsServerDB.add(8889);
		portsServerPred.add(9999);
		portsServerPred.add(9998);
		try {
			DatagramSocket serverSocket = new DatagramSocket(port);

			InetAddress inetAddress = InetAddress.getByName("localhost");

			while(true) {
				byte[] receiveMessage = new byte[5120];
				DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);
				try {
					serverSocket.receive(receivePacket);
					String message = new String(receivePacket.getData());
					message = message.replaceAll("\u0000.*", "");
					System.out.println(message);
					if(!message.contains("Bearer ")) {
						Message mes = gson.fromJson(message, Message.class);
						Integer serv = mes.getServer();
						System.out.println(serv);
						switch(serv) {
							case 1:{
								byte[] sendMessageAuth;
	
								User mesU = gson.fromJson(message, User.class);
								String mesJson = gson.toJson(mesU);
								sendMessageAuth = mesJson.getBytes();
	
								updateServersAuth();
	
								DatagramPacket sendPacket = new DatagramPacket(
										sendMessageAuth, sendMessageAuth.length,
										inetAddress, portsServerAuth.get(0));
								serverSocket.send(sendPacket);
								System.out.println(portsServerAuth.get(0));
	
								byte[] receiveMessageAuth = new byte[5120];
								DatagramPacket receivePacketAuth = new DatagramPacket(receiveMessageAuth, receiveMessageAuth.length);
								serverSocket.receive(receivePacketAuth);
								String serverResponse = new String(receivePacketAuth.getData());
								serverResponse = serverResponse.replaceAll("\u0000.*", "");

								System.out.println(serverResponse);
					            int portClient = receivePacket.getPort();
	
								DatagramPacket sendPacketclient = new DatagramPacket(
										receiveMessageAuth, receiveMessageAuth.length,
										inetAddress, portClient);
								serverSocket.send(sendPacketclient);
	
								break;
							}
							case 2:{
								System.out.println(serv);
								byte[] sendMessageDB;
	
								Predict mesP = gson.fromJson(message, Predict.class);
								String mesJson = gson.toJson(mesP);
								sendMessageDB = mesJson.getBytes();
	
								updateServersDB();
	
								DatagramPacket sendPacket = new DatagramPacket(
										sendMessageDB, sendMessageDB.length,
										inetAddress, portsServerDB.get(0));
								serverSocket.send(sendPacket);
	
								byte[] receiveMessageDB = new byte[5120];
								DatagramPacket receivePacketDB = new DatagramPacket(receiveMessageDB, receiveMessageDB.length);
								serverSocket.receive(receivePacketDB);
								String serverResponse = new String(receivePacketDB.getData());
								serverResponse = serverResponse.replaceAll("\u0000.*", "");
								
								System.out.println(serverResponse);
					            int portClient = receivePacket.getPort();
	
								DatagramPacket sendPacketclient = new DatagramPacket(
										receiveMessageDB, receiveMessageDB.length,
										inetAddress, portClient);
								serverSocket.send(sendPacketclient);
	
								break;
							}
						}
					}
					else {
						byte[] sendMessagePred;

						sendMessagePred = message.getBytes();

						updateServersPred();

						DatagramPacket sendPacket = new DatagramPacket(
								sendMessagePred, sendMessagePred.length,
								inetAddress, portsServerPred.get(0));
						serverSocket.send(sendPacket);
						
						byte[] receiveMessagePred = new byte[5120];
						DatagramPacket receivePacketPred = new DatagramPacket(receiveMessagePred, receiveMessagePred.length);
						serverSocket.receive(receivePacketPred);
						String serverResponse = new String(receivePacketPred.getData());
						serverResponse = serverResponse.replaceAll("\u0000.*", "");
						
						System.out.println(serverResponse);
			            int portClient = receivePacket.getPort();

						DatagramPacket sendPacketclient = new DatagramPacket(
								receiveMessagePred, receiveMessagePred.length,
								inetAddress, portClient);
						serverSocket.send(sendPacketclient);
						
					}
					
				} catch (IOException e) {
					serverSocket.close();
					e.printStackTrace();
				}
				if(removedAuth != null && portsServerAuth.size() < 2)
					portsServerAuth.add(0,removedAuth);
				if(removedDB != null && portsServerDB.size() < 2) 
					portsServerDB.add(0,removedDB);
				if(removedPred != null && portsServerPred.size() < 2) 
					portsServerPred.add(0,removedPred);
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		new LoadBalancer();
	}	

}

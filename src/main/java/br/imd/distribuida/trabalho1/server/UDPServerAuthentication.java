package br.imd.distribuida.trabalho1.server;

import java.io.File;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.google.gson.Gson;

import br.imd.distribuida.trabalho1.models.ServerResponse;
import br.imd.distribuida.trabalho1.models.User;

public class UDPServerAuthentication {

	private Gson gson = new Gson();
	
	private Algorithm algorithm = Algorithm.HMAC256("secret");

	public UDPServerAuthentication() {

		System.out.println("UDP Server Prediction Started");
		
		try {
			int port = 7778;
			DatagramSocket serverSocket = new DatagramSocket(port);
			while(true) {
				byte[] receiveMessage = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);
				serverSocket.receive(receivePacket);
				String message = new String(receivePacket.getData());
				message = message.replaceAll("\u0000.*", "");
				
				try {
					User user = gson.fromJson(message, User.class);
					//System.out.println(message);
					if(user.getType() == 1) {
						File myObj = new File("users.tsv");
						Scanner myReader = new Scanner(myObj);
						Boolean bool = false;
						while (myReader.hasNextLine()) {
							String userline = myReader.nextLine();
							if(userline.split(";")[0].equals(user.getUser())) {
								bool = true;
								break;
							}
						}
						myReader.close();
						if(bool) {
							byte[] sendMessage;
							
				            InetAddress addressClient = receivePacket.getAddress();
				            int portClient = receivePacket.getPort();
				            
				            ServerResponse sr = new ServerResponse(true, "Usuário já cadastrado!");
							String srJson = gson.toJson(sr);
							sendMessage = srJson.getBytes();
							
							DatagramPacket sendPacket = new DatagramPacket(
									sendMessage, sendMessage.length,
									addressClient, portClient);
							serverSocket.send(sendPacket);
				            
							System.out.println("Retornando error");
						}
						else {
							FileWriter myWriter = new FileWriter("users.tsv");
							myWriter.write(user.getUser()+";"+user.getPassword()+"\n");
							myWriter.close();
							System.out.println("Usuário cadastrado!");

							byte[] sendMessage;
							
				            InetAddress addressClient = receivePacket.getAddress();
				            int portClient = receivePacket.getPort();
				            
				            ServerResponse sr = new ServerResponse(false, "Usuário cadastrado com sucesso!");
							String srJson = gson.toJson(sr);
							sendMessage = srJson.getBytes();
							
							DatagramPacket sendPacket = new DatagramPacket(
									sendMessage, sendMessage.length,
									addressClient, portClient);
							serverSocket.send(sendPacket);
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
							        .withIssuer(user.getUser())
							        .sign(algorithm);
							System.out.println(token);
							
							JWTVerifier verifier = JWT.require(algorithm)
							        .withIssuer("danielhenriquefg@gmail.com")
							        .build();
							DecodedJWT jwt = JWT.decode(token);
							
							System.out.println(jwt.getSubject());


							byte[] sendMessage;
							
				            InetAddress addressClient = receivePacket.getAddress();
				            int portClient = receivePacket.getPort();
				            
				            ServerResponse sr = new ServerResponse(false, token);
							String srJson = gson.toJson(sr);
							sendMessage = srJson.getBytes();
							
							DatagramPacket sendPacket = new DatagramPacket(
									sendMessage, sendMessage.length,
									addressClient, portClient);
							serverSocket.send(sendPacket);
						}else {
							byte[] sendMessage;
							
				            InetAddress addressClient = receivePacket.getAddress();
				            int portClient = receivePacket.getPort();
				            
				            ServerResponse sr = new ServerResponse(true, "Usuário ou senha inválidos!");
							String srJson = gson.toJson(sr);
							sendMessage = srJson.getBytes();
							
							DatagramPacket sendPacket = new DatagramPacket(
									sendMessage, sendMessage.length,
									addressClient, portClient);
							serverSocket.send(sendPacket);
				            
							System.out.println("Retornando error");
						}
						myReader.close();
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					serverSocket.close();
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) { 
			new UDPServerAuthentication();    
		}
}

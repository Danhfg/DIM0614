package br.imd.distribuida.trabalho1.server;

import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.google.gson.Gson;

import br.imd.distribuida.trabalho1.models.ServerResponse;

public class UDPServerPredictions2 {

	private Gson gson = new Gson();	
	
	private Algorithm algorithm = Algorithm.HMAC256("AOsD89f&*Fujalo()*");
	
	public UDPServerPredictions2() {

		int port = 9998;
		
		try {
			DatagramSocket serverSocket = new DatagramSocket(port);

			System.out.println("UDP Predictions Server Started " + port );

			try {
			while(true) {
				byte[] receiveMessage = new byte[10240];
				DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);
				serverSocket.receive(receivePacket);
				String message = new String(receivePacket.getData());
				message = message.replaceAll("\u0000.*", "");
				System.out.println(message);
				if(message.contains("PING")){
					byte[] sendMessage;
					String send = "PONG";
					sendMessage = send.getBytes();
					
		            InetAddress addressClient = receivePacket.getAddress();
		            int portClient = receivePacket.getPort();
					
					DatagramPacket sendPacket = new DatagramPacket(
							sendMessage, sendMessage.length,
							addressClient, portClient);
					serverSocket.send(sendPacket);

				}
				else {
				    JWTVerifier verifier = JWT.require(algorithm)
				        .withIssuer("predictor")
				        .build(); //Reusable verifier instance
				    DecodedJWT jwt = verifier.verify(message.replace("Bearer ", ""));
				    
					String e = jwt.getClaim("user").asString();
					System.out.println("User: " + e);
					
					File f = new File("data/predictions/"+e+".tsv");
					if(!f.exists()) {
						System.out.println("NOTFOUND");
						
						byte[] sendMessage;
						
			            InetAddress addressClient = receivePacket.getAddress();
			            int portClient = receivePacket.getPort();
			            
			            ServerResponse sr = new ServerResponse(true, "Nenhuma solicitação de predição encontrada ou finalizada!");
						String srJson = gson.toJson(sr);
						sendMessage = srJson.getBytes();
						
						DatagramPacket sendPacket = new DatagramPacket(
								sendMessage, sendMessage.length,
								addressClient, portClient);
						serverSocket.send(sendPacket);
					}
					else {
						String results = Files.readString(Paths.get("data/predictions/"+e+".tsv"));
	
						byte[] sendMessage;
						
			            InetAddress addressClient = receivePacket.getAddress();
			            int portClient = receivePacket.getPort();
			            
			            ServerResponse sr = new ServerResponse(false, results);
						String srJson = gson.toJson(sr);
						sendMessage = srJson.getBytes();
						
						DatagramPacket sendPacket = new DatagramPacket(
								sendMessage, sendMessage.length,
								addressClient, portClient);
						serverSocket.send(sendPacket);
					}
				}
			}
			}catch (JWTVerificationException exception){
				serverSocket.close();
			    exception.printStackTrace();
			}
			catch (Exception e) {
				e.printStackTrace();
				serverSocket.close();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public static void main(String[] args) {
		new UDPServerPredictions2();
	}
}

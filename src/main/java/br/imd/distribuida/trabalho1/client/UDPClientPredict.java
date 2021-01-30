package br.imd.distribuida.trabalho1.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

import com.google.gson.Gson;

import br.imd.distribuida.trabalho1.models.Predict;
import br.imd.distribuida.trabalho1.models.ServerResponse;
import br.imd.distribuida.trabalho1.models.User;

public class UDPClientPredict {
	
	private Gson gson = new Gson();
	
	private String token;

	public UDPClientPredict() {

		System.out.println("UDP Client Predict Started");

		Scanner scanner = new Scanner(System.in);
		try {
			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress inetAddress = InetAddress.getByName("localhost");
			String message = "";

			while(true) {
				System.out.print("Deseja se [C]adastrar ou [E]ntrar: ");
				message = scanner.nextLine();
				if ("quit".equalsIgnoreCase(message)) {
					break;
				}
				if(message.equalsIgnoreCase("C")) {
					byte[] sendMessage;
					
					byte[] receiveMessage = new byte[1024];
					DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);
					
					System.out.print("Login: ");
					String email = scanner.nextLine();
					System.out.print("Senha: ");
					String password = scanner.nextLine();
					User user = new User(email, password);
					user.setType(1);
					String userJson = gson.toJson(user);
					sendMessage = userJson.getBytes();
					
					int portAuth = 7778;
					
					DatagramPacket sendPacket = new DatagramPacket(
							sendMessage, sendMessage.length,
							inetAddress, portAuth);
					clientSocket.send(sendPacket);
					clientSocket.receive(receivePacket);
					String serverResponse = new String(receivePacket.getData());
					serverResponse = serverResponse.replaceAll("\u0000.*", "");
					
					ServerResponse sr = gson.fromJson(serverResponse, ServerResponse.class);
					
					if(sr.getError()) {
						System.out.println("Erro: " + sr.getMessage());
					}else {
						System.out.println("Sucesso: " + sr.getMessage());							
					}
				}
				else if(message.equalsIgnoreCase("E")) {
					byte[] sendMessage;
					
					byte[] receiveMessage = new byte[1024];
					DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);
					
					System.out.print("Login: ");
					String email = scanner.nextLine();
					System.out.print("Senha: ");
					String password = scanner.nextLine();
					User user = new User(email, password);
					user.setType(2);
					String userJson = gson.toJson(user);
					sendMessage = userJson.getBytes();
					
					int portAuth = 7777;
					
					DatagramPacket sendPacket = new DatagramPacket(
							sendMessage, sendMessage.length,
							inetAddress, portAuth);
					clientSocket.send(sendPacket);
					clientSocket.receive(receivePacket);
					String serverResponse = new String(receivePacket.getData());
					serverResponse = serverResponse.replaceAll("\u0000.*", "");
					
					ServerResponse sr = gson.fromJson(serverResponse, ServerResponse.class);
					
					if(sr.getError()) {
						System.out.println("Erro: " + sr.getMessage());
					}else {
						System.out.println("Sucesso!");
						this.token = sr.getMessage();
						break;
					}
				}
			}
			
			while(true) {
				System.out.print("Deseja se [S]olicitar novas predições ou [V]isualizar suas predições: ");
				message = scanner.nextLine();
				if ("quit".equalsIgnoreCase(message)) {
					break;
				}
				else if("S".equalsIgnoreCase(message)) {
					byte[] sendMessage;
					
					byte[] receiveMessage = new byte[1024];
					DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);

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
					
					Predict pred = new Predict(chr, Integer.valueOf(pos), ref.charAt(0), alt.charAt(0), pat, token);
					
					String predJson = gson.toJson(pred);
					sendMessage = predJson.getBytes();
					
					int portPred = 8888;
					
					DatagramPacket sendPacket = new DatagramPacket(
							sendMessage, sendMessage.length,
							inetAddress, portPred);
					clientSocket.send(sendPacket);
					/*clientSocket.receive(receivePacket);
					String serverResponse = new String(receivePacket.getData());
					serverResponse = serverResponse.replaceAll("\u0000.*", "");
					
					ServerResponse sr = gson.fromJson(serverResponse, ServerResponse.class);
					
					if(sr.getError()) {
						System.out.println("Erro: " + sr.getMessage());
					}else {
						System.out.println("Sucesso: " + sr.getMessage());							
					}*/
				}
				else if("V".equalsIgnoreCase(message)) {
					
				}
				
			}

			clientSocket.close();
			scanner.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new UDPClientPredict();
	}
	

}

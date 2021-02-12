package br.imd.distribuida.trabalho1.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.List;

import com.google.gson.Gson;

import br.imd.distribuida.trabalho1.models.Predict;
import br.imd.distribuida.trabalho1.models.ServerResponse;
import br.imd.distribuida.trabalho1.models.User;

public class UDPClientPredict {
	
	private Gson gson = new Gson();
	
	private String token;
	
	private String proceesPrediction(String bruteResult)
	{
		String exacResult = null;
		String common= "0";
		
		bruteResult = bruteResult.replaceAll("dbNSFP_", "");
		
		HashMap<String, String> allPredictors = new HashMap<String,String>();
		List<String> resultList = Arrays.asList(bruteResult.split(";"));
		
		for (String string : resultList) {
			if(string.contains("pred")) {
				List<String> singlePredList = Arrays.asList(string.split("="));
				allPredictors.put(singlePredList.get(0), singlePredList.get(1));
			}
			if(string.contains("ExAC_AF")) {
				exacResult = string.split("=")[1];
			}
			if(string.contains("1000Gp3_AF")) {
				common = string.split("=")[1];
			}
		}
		if ((allPredictors.get("SIFT_pred") != null &&
	            allPredictors.get("SIFT_pred").contains("T")) &&
	        (allPredictors.get("Polyphen2_HDIV_pred") != null &&
	            allPredictors.get("Polyphen2_HDIV_pred").contains("B")) &&
	        (allPredictors.get("PROVEAN_pred") != null &&
	            allPredictors.get("PROVEAN_pred").contains("N"))) {
	      return ("Neutra");
	    } 
		else {
			if (exacResult != null && Double.parseDouble(exacResult) < 0.0001) {
				return ("Patogênica");
			} 
			else {
				int nDAMAGE=0;
				if (allPredictors.get("SIFT_pred") != null &&
	        		allPredictors.get("SIFT_pred").contains("D")) {
					++nDAMAGE;
				}
		        if (allPredictors.get("Polyphen2_HDIV_pred") != null &&
	        		(allPredictors.get("Polyphen2_HDIV_pred").contains("D") ||
	        		allPredictors.get("Polyphen2_HDIV_pred").contains("P")) ) {
		        	++nDAMAGE;
		        }
		        if (allPredictors.get("PROVEAN_pred") != null &&
		        		allPredictors.get("PROVEAN_pred").contains("D")) {
		        	++nDAMAGE;
		        }
		        if (allPredictors.get("LRT_pred") != null &&
	        		allPredictors.get("LRT_pred").contains("D")) {
		        	++nDAMAGE;
		        }
		        if (allPredictors.get("MetaSVM_pred") != null &&
	        		allPredictors.get("MetaSVM_pred").contains("D")) {
		        	++nDAMAGE;
		        }
		        if (allPredictors.get("MetaSVM_pred") != null &&
	        		allPredictors.get("MetaSVM_pred").contains("D")) {
		        	++nDAMAGE;
		        }
		        if (allPredictors.get("MutationAssessor_pred") != null &&
		        		(allPredictors.get("MutationAssessor_pred").contains("H") ||
		        		allPredictors.get("MutationAssessor_pred").contains("M"))) {
		        	++nDAMAGE;
		        }
		        if (allPredictors.get("Polyphen2_HVAR_pred") != null &&
		        		allPredictors.get("Polyphen2_HVAR_pred").contains("D") &&
		        		allPredictors.get("Polyphen2_HVAR_pred").contains("P")) {
		        	++nDAMAGE;
		        }
		        if (allPredictors.get("MutationTaster_pred") != null &&
		        		allPredictors.get("MutationTaster_pred").contains("D") &&
		        		allPredictors.get("MutationTaster_pred").contains("A")) {
		        	++nDAMAGE;
		        }
		        if (nDAMAGE <= 6) {
		        	return "Neutra";
		        } 
		        else {
		        	if (Double.parseDouble(common) < 0.0001) {
			        	return "Patogênica";
			        } 
			        else {
			          return  "Neutra";
			        }
		        }
			}
		}
	}

	public UDPClientPredict() {

		System.out.println("UDP Client Predict Started");

		Scanner scanner = new Scanner(System.in);
		try {
			DatagramSocket clientSocket = new DatagramSocket();
			clientSocket.setSoTimeout(10000);
			InetAddress inetAddress = InetAddress.getByName("localhost");
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
					byte[] sendMessage;
					
					byte[] receiveMessage = new byte[5120];
					DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);
					
					System.out.print("Login: ");
					String email = scanner.nextLine();
					System.out.print("Senha: ");
					String password = scanner.nextLine();
					User user = new User(1, 1, email, password);
					String userJson = gson.toJson(user);
					System.out.println(userJson);
					sendMessage = userJson.getBytes();
					
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
					
					byte[] receiveMessage = new byte[5120];
					DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);
					
					System.out.print("Login: ");
					String email = scanner.nextLine();
					System.out.print("Senha: ");
					String password = scanner.nextLine();
					User user = new User(1, 2, email, password);
					String userJson = gson.toJson(user);
					sendMessage = userJson.getBytes();
					
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
					
					byte[] receiveMessage = new byte[5120];
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
					
					Predict pred = new Predict(2, chr, Integer.valueOf(pos), ref.charAt(0), alt.charAt(0), pat, token);
					
					String predJson = gson.toJson(pred);
					sendMessage = predJson.getBytes();
					
					DatagramPacket sendPacket = new DatagramPacket(
							sendMessage, sendMessage.length,
							inetAddress, portDB);
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
				else if("V".equalsIgnoreCase(message)) {

					byte[] sendMessage;
					
					byte[] receiveMessage = new byte[5120];
					DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);
					
					String tokenRequest = "Bearer " + token;
					sendMessage = tokenRequest.getBytes();
					
					DatagramPacket sendPacket = new DatagramPacket(
							sendMessage, sendMessage.length,
							inetAddress, portPred);
					clientSocket.send(sendPacket);
					clientSocket.receive(receivePacket);
					String serverResponse = new String(receivePacket.getData());
					serverResponse = serverResponse.replaceAll("\u0000.*", "");
					ServerResponse sr = gson.fromJson(serverResponse, ServerResponse.class);
					
					if(sr.getError()) {
						System.out.println("Erro: " + sr.getMessage());
					}else {
						String[] lines = sr.getMessage().split("\n");
						for (String string : lines) {
							String[] var = string.split("	");
							System.out.println("Paciente "+ var[0]+ ", posição "+ var[2] + ", mutação " + var[5] + ", resultado: "+ proceesPrediction(var[8]));
						}
					}
					
				}
				
			}

			clientSocket.close();
			scanner.close();
			
		} catch (Exception e) {
			System.out.println("Servidor indisponível tente novamente mais tarde!");
			//e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new UDPClientPredict();
	}
	

}

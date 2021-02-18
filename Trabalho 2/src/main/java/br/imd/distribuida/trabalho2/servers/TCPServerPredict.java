package br.imd.distribuida.trabalho2.servers;

import java.io.File;
import java.io.IOException;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.google.gson.Gson;

import br.imd.distribuida.trabalho2.models.ServerResponse;


public class TCPServerPredict {
	private static final int BUFFER_SIZE = 1024;
	
	private static Selector selector = null;
	
	private Gson gson = new Gson();	
	
	private Algorithm algorithm = Algorithm.HMAC256("AOsD89f&*Fujalo()*");

	public TCPServerPredict() {
		logger("Starting MySelectorClientExample...");
		try {
			InetAddress hostIP= InetAddress.getLocalHost();
			int port = 9998;
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
					

					String[] lines = results.split("\n");
					String returnString = "";
					for (String string : lines) {
						String[] var = string.split("	");
						returnString += "Paciente "+ var[0]+ ", posição "+ var[2] + ", mutação " + var[5] + ", resultado: "+ proceesPrediction(var[8]) + "\n";
					}

					byte[] sendMessage;
					
		            ServerResponse sr = new ServerResponse(false, returnString);
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


	public static void main(String[] args) {
		new TCPServerPredict();

	}

}

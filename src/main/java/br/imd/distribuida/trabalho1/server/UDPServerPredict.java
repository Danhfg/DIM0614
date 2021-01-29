package br.imd.distribuida.trabalho1.server;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import com.google.gson.Gson;

import br.imd.distribuida.trabalho1.models.Predict;

public class UDPServerPredict {
	
	private Gson gson = new Gson();

	public UDPServerPredict() {

		System.out.println("UDP Server Prediction Started");
		try {
			DatagramSocket serverSocket = new DatagramSocket(7777);
			while(true) {
				byte[] receiveMessage = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);
				serverSocket.receive(receivePacket);

				byte[] data = receivePacket.getData();
				ByteArrayInputStream in = new ByteArrayInputStream(data);
				ObjectInputStream is = new ObjectInputStream(in);
				
				try {
					Predict pred = (Predict) gson.fromJson(is.readObject().toString(), Predict.class);
					System.out.println("Token: " + pred.getToken()); 
					System.out.println("Predict received. Chr: " + pred.getChr()+", pos: "+ pred.getPos()+ ", ref: " + pred.getRef()+", alt: " + pred.getAlt());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
package br.imd.distribuida.trabalho1.models;

public class Message {
	public Integer server;

	public Integer getServer() {
		return server;
	}

	public void setServer(Integer server) {
		this.server = server;
	}

	public Message(Integer server) {
		this.server = server;
	}
}

package br.imd.distribuida.trabalho2.models;

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

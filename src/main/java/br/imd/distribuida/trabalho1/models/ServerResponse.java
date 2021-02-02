package br.imd.distribuida.trabalho1.models;

public class ServerResponse{
	
	private Boolean error;
	
	private String message;

	public Boolean getError() {
		return error;
	}

	public void setError(Boolean error) {
		this.error = error;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ServerResponse(Boolean error, String message) {
		this.error = error;
		this.message = message;
	}
}

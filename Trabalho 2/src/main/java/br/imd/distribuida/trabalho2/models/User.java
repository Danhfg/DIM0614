package br.imd.distribuida.trabalho2.models;

public class User extends Message{
	
	private Integer type;
	
	private String user;
	
	private String password;

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public User(Integer server, Integer type, String user, String password) {
		super(server);
		this.type = type;
		this.user = user;
		this.password = password;
	}

}

package br.imd.distribuida.trabalho1.models;

public class Predict {
	
	private Character chr;
	
	private Integer pos;
	
	private Character ref;
	
	private Character alt;
	
	private String token;

	public Character getChr() {
		return chr;
	}

	public void setChr(Character chr) {
		this.chr = chr;
	}

	public Integer getPos() {
		return pos;
	}

	public void setPos(Integer pos) {
		this.pos = pos;
	}

	public Character getRef() {
		return ref;
	}

	public void setRef(Character ref) {
		this.ref = ref;
	}

	public Character getAlt() {
		return alt;
	}

	public void setAlt(Character alt) {
		this.alt = alt;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

}
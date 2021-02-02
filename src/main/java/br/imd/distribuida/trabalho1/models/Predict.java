package br.imd.distribuida.trabalho1.models;

public class Predict extends Message{
	
	private String chr;
	
	private Integer pos;
	
	private Character ref;
	
	private Character alt;
	
	private String patient;
	
	private String token;

	public String getChr() {
		return chr;
	}

	public void setChr(String chr) {
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

	public String getPatient() {
		return patient;
	}

	public void setPatient(String patient) {
		this.patient = patient;
	}

	public Predict(Integer server, String chr, Integer pos, Character ref, Character alt, String patient,
			String token) {
		super(server);
		this.chr = chr;
		this.pos = pos;
		this.ref = ref;
		this.alt = alt;
		this.patient = patient;
		this.token = token;
	}

}
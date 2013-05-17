package br.unb.cic.bionimbus.messaging;

public abstract class MessageFactory {
	
	private String name;
	
	public MessageFactory(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public abstract Message getMessage(int id, byte[] buffer);

}

package br.unb.cic.bionimbus.rest.request;

import br.unb.cic.bionimbus.model.User;

/**
 * Defines a sign up information request
 * @author Vinicius
 */
public class SignUpRequest extends BaseRequest {
	private User user;

	public SignUpRequest(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}

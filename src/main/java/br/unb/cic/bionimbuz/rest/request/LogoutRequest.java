package br.unb.cic.bionimbuz.rest.request;

import br.unb.cic.bionimbuz.model.User;

/**
 * Defines a logout request to be used in a REST request
 * @author Vinicius
 */
public class LogoutRequest extends BaseRequest {
	private User user;

	public LogoutRequest() {
	}

	public LogoutRequest(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}

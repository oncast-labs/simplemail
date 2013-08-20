package br.com.oncast.simplemail.configuration;

import javax.mail.Authenticator;

public interface AuthenticationProvider {

	String getEmailUsername();

	String getEmailPassword();

	Authenticator mailAuthenticator();

}

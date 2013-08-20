package br.com.oncast.simplemail.configuration;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class PropertiesAuthenticationProvider implements AuthenticationProvider {

	public static final String AUTH_PROPERTIES_FILE_PATH = "auth.priperties.path";

	public static final String EMAIL_USER_KEY = "email.user";

	private static final String EMAIL_PASSWORD_KEY = "email.password";

	private final String propertiesPath;

	private Properties properties;

	private String emailUser;

	private String emailPassword;

	@Inject
	public PropertiesAuthenticationProvider(@Named(AUTH_PROPERTIES_FILE_PATH) final String propertiesPath) {
		this.propertiesPath = propertiesPath;
	}

	public String getEmailUsername() {
		return emailUser == null ? emailUser = loadProperties().getProperty(EMAIL_USER_KEY) : emailUser;
	}

	public String getEmailPassword() {
		return emailPassword == null ? emailPassword = loadProperties().getProperty(EMAIL_PASSWORD_KEY) : emailPassword;
	}

	public Authenticator mailAuthenticator() {
		return new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(getEmailUsername(), getEmailPassword());
			}
		};
	}

	private Properties loadProperties() {
		if (properties == null) {
			try {
				properties = new Properties();
				properties.load(PropertiesAuthenticationProvider.class.getResourceAsStream(propertiesPath));
			} catch (final IOException e) {
				throw new RuntimeException("Could not load mail configuration properties, did you created email.properties file?", e);
			}
		}
		return properties;
	}

}

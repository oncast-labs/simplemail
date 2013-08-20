package br.com.oncast.simplemail.configuration;

import java.util.Properties;


import com.google.inject.Singleton;

@Singleton
public class GmailSmtp implements SmtpConfigurationProvider {

	public Properties getConfiguration() {
		return properties();
	}

	public static Properties properties() {
		final Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");

		return props;
	}
}

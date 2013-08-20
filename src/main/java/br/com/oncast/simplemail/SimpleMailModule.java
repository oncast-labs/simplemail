package br.com.oncast.simplemail;

import br.com.oncast.simplemail.configuration.AuthenticationProvider;
import br.com.oncast.simplemail.configuration.GmailSmtp;
import br.com.oncast.simplemail.configuration.PropertiesAuthenticationProvider;
import br.com.oncast.simplemail.configuration.SmtpConfigurationProvider;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

public class SimpleMailModule extends AbstractModule {

	private static Injector injector;

	@Override
	protected void configure() {
		bind(AuthenticationProvider.class).to(PropertiesAuthenticationProvider.class);
		bind(SmtpConfigurationProvider.class).to(GmailSmtp.class);
		bind(String.class).annotatedWith(Names.named(PropertiesAuthenticationProvider.AUTH_PROPERTIES_FILE_PATH)).toInstance("/email.properties");
	}

	public static Injector getInjector() {
		if (injector == null) injector = Guice.createInjector(new SimpleMailModule());
		return injector;
	}

}

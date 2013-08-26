package br.com.oncast.simplemail;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import br.com.oncast.simplemail.configuration.AuthenticationProvider;
import br.com.oncast.simplemail.configuration.SmtpConfigurationProvider;
import br.com.oncast.simplemail.helper.TemplateMailHelper;
import br.com.oncast.simplemail.mail.Email;
import br.com.oncast.simplemail.mail.HasReplyTo;
import br.com.oncast.simplemail.mail.TemplateEmail;
import br.com.oncast.simplemail.mail.TextEmail;

import com.google.inject.Inject;

public class MailSender {

	private final AuthenticationProvider authenticationProvider;

	private final SmtpConfigurationProvider smtpProvider;

	private Email email;

	private Set<InternetAddress> recipients;

	private InternetAddress senderEmail;

	private Sender sender;

	private final InternetAddress defaultEmail;

	@Inject
	private MailSender(final AuthenticationProvider authenticationProvider, final SmtpConfigurationProvider smtpProvider) {
		this.authenticationProvider = authenticationProvider;
		this.smtpProvider = smtpProvider;
		this.sender = Sender.SEND_MUTUALLY_TO_ALL_RECIPIENTS;
		try {
			this.defaultEmail = new InternetAddress(authenticationProvider.getEmailUsername());
		} catch (final AddressException e) {
			throw new RuntimeException(String.format("Invalid default user e-mail: %s.", authenticationProvider.getEmailUsername()), e);
		}
	}

	public static MailSender send() {
		return SimpleMailModule.getInjector().getInstance(MailSender.class);
	}

	public MailSender email(final Email email) {
		this.email = email;
		return this;
	}

	public MailSender to(final String... recipients) {
		return to(Arrays.asList(recipients));
	}

	public MailSender individually() {
		this.sender = Sender.SEND_INDIVIDUALLY_TO_EACH_RECIPIENT;
		return this;
	}

	public MailSender to(final Collection<String> recipients) {
		this.recipients = new HashSet<InternetAddress>();
		for (final String email : recipients) {
			try {
				this.recipients.add(new InternetAddress(email));
			} catch (final AddressException e) {
				throw new RuntimeException(String.format("Invalid recipient e-mail: %s.", email), e);
			}
		}
		return this;
	}

	public MailSender from(final String senderEmail) {
		try {
			this.senderEmail = new InternetAddress(senderEmail);
		} catch (final AddressException e) {
			throw new RuntimeException(String.format("Invalid sender e-mail: %s.", senderEmail), e);
		}
		return this;
	}

	public void andWait() {
		doSend();
	}

	private void doSend() {
		isReadyToSend();

		try {
			final Session session = Session.getDefaultInstance(smtpProvider.getConfiguration(), authenticationProvider.mailAuthenticator());
			final MimeMessage message = new MimeMessage(session);
			from(message, defaultIfNull(senderEmail));
			subject(message, email.getSubject());
			if (email instanceof HasReplyTo) replyTo(message, ((HasReplyTo) email).getReplyTo());
			if (email instanceof TextEmail) text(message, ((TextEmail) email).getText());
			if (email instanceof TemplateEmail) htmlContent(message, (TemplateEmail) email);

			sender.sendTo(message, defaultIfEmpty(recipients));
		} catch (final MessagingException e) {
			throw new RuntimeException("Error sending e-mail.", e);
		}
	}

	public void inBackground() {
		new Thread() {
			@Override
			public void run() {
				andWait();
			}
		}.start();
	}

	public void inBackground(final AsyncCallback callback) {
		new Thread() {
			@Override
			public void run() {
				try {
					doSend();
					callback.onSuccess();
				} catch (final Throwable e) {
					callback.onFailure(e);
				}
			}
		}.start();
	}

	public interface AsyncCallback {

		void onSuccess();

		void onFailure(Throwable e);

	}

	private InternetAddress defaultIfNull(final InternetAddress value) {
		return value == null ? defaultEmail : value;
	}

	private Collection<InternetAddress> defaultIfEmpty(final Collection<InternetAddress> value) {
		return (value == null || value.isEmpty()) ? Arrays.asList(defaultEmail) : value;
	}

	private void isReadyToSend() {
		if (this.email == null) throw new IllegalStateException("It should set e-mail before trying to send");
	}

	private void from(final Message message, final InternetAddress sender) throws MessagingException {
		message.setFrom(sender);
	}

	private void subject(final Message message, final String string) throws MessagingException {
		message.setSubject(string);
	}

	private void replyTo(final Message message, final String replyTo) throws MessagingException {
		try {
			message.setReplyTo(new Address[] { new InternetAddress(replyTo) });
		} catch (final AddressException e) {
			throw new RuntimeException(String.format("Invalid 'replyTo' e-mail: %s.", replyTo), e);
		}
	}

	private void text(final Message message, final String text) throws MessagingException {
		message.setText(text);
	}

	private void htmlContent(final Message message, final TemplateEmail email) throws MessagingException {
		htmlContent(message, TemplateMailHelper.createContent(email));
	}

	private void htmlContent(final Message message, final String content) throws MessagingException {
		final Multipart multipart = new MimeMultipart();
		final MimeBodyPart html = new MimeBodyPart();
		html.setContent(content, "text/html; charset=UTF-8");
		multipart.addBodyPart(html);
		message.setContent(multipart);
	}

	private enum Sender {
		SEND_INDIVIDUALLY_TO_EACH_RECIPIENT {
			@Override
			void sendTo(final Message message, final Collection<InternetAddress> recipients) {
				for (final InternetAddress recipient : recipients) {
					try {
						message.setRecipient(Message.RecipientType.TO, recipient);
						Transport.send(message);
					} catch (final MessagingException e) {
						throw new RuntimeException("Failed to send e-mail to " + recipient.getAddress() + ".", e);
					}
				}

			}
		},
		SEND_MUTUALLY_TO_ALL_RECIPIENTS {
			@Override
			void sendTo(final Message message, final Collection<InternetAddress> recipients) {
				try {
					message.addRecipients(Message.RecipientType.TO, recipients.toArray(new InternetAddress[recipients.size()]));
					Transport.send(message);
				} catch (final MessagingException e) {
					throw new RuntimeException("Failed to send e-mail to " + recipients.size() + " recipients.", e);
				}
			}
		};

		abstract void sendTo(Message message, Collection<InternetAddress> recipients);
	}

}
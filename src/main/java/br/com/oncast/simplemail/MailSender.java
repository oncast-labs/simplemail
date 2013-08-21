package br.com.oncast.simplemail;

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

	private final AuthenticationProvider authentication;

	private final SmtpConfigurationProvider smtp;

	private Email email;

	private String receiverEmail;

	private String senderEmail;

	@Inject
	private MailSender(final AuthenticationProvider authentication, final SmtpConfigurationProvider smtp) {
		this.authentication = authentication;
		this.smtp = smtp;
	}

	public static MailSender send() {
		return SimpleMailModule.getInjector().getInstance(MailSender.class);
	}

	public MailSender email(final Email email) {
		this.email = email;
		return this;
	}

	public MailSender to(final String receiverEmail) {
		this.receiverEmail = receiverEmail;
		return this;
	}

	public MailSender from(final String senderEmail) {
		this.senderEmail = senderEmail;
		return this;
	}

	public void andWait() {
		isReadyToSend();

		try {
			final Session session = Session.getDefaultInstance(smtp.getConfiguration(), authentication.mailAuthenticator());
			final MimeMessage message = new MimeMessage(session);
			from(message, defaultIfNull(senderEmail));
			to(message, defaultIfNull(receiverEmail));
			subject(message, email.getSubject());
			if (email instanceof HasReplyTo) replyTo(message, ((HasReplyTo) email).getReplyTo());
			if (email instanceof TextEmail) text(message, ((TextEmail) email).getText());
			if (email instanceof TemplateEmail) htmlContent(message, (TemplateEmail) email);
			Transport.send(message);
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
					andWait();
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

	private String defaultIfNull(final String value) {
		return value == null ? authentication.getEmailUsername() : value;
	}

	private void isReadyToSend() {
		if (this.email == null) throw new IllegalStateException("It should set e-mail before trying to send");
	}

	private void from(final Message message, final String senderEmail) throws MessagingException {
		try {
			message.setFrom(new InternetAddress(senderEmail));
		} catch (final AddressException e) {
			throw new RuntimeException(String.format("Invalid sender e-mail: %s.", senderEmail), e);
		}
	}

	private void to(final Message message, final String receiverEmail) throws MessagingException {
		try {
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(receiverEmail));
		} catch (final AddressException e) {
			throw new RuntimeException(String.format("Invalid receiver e-mail: %s.", receiverEmail), e);
		}
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

}

package br.com.oncast.simplemail.mail.impl;

import br.com.oncast.simplemail.mail.TextEmail;

public class SimpleTextEmail implements TextEmail {

	private final String subject;

	private final String text;

	public SimpleTextEmail(final String subject, final String text) {
		this.subject = subject;
		this.text = text;
	}

	public String getSubject() {
		return subject;
	}

	public String getText() {
		return text;
	}

}

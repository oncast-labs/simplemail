package br.com.oncast.simplemail.example;

import br.com.oncast.simplemail.MailSender;
import br.com.oncast.simplemail.MailSender.AsyncCallback;
import br.com.oncast.simplemail.mail.impl.SimpleTextEmail;

public class HelloWorld {

	public static void main(final String[] args) {
		final String to = "email@example.com";
		MailSender.mail(new SimpleTextEmail("Simple Subject", "Simple e-mail content text")).to(to).sendAndWait();
		System.out.println("Simple e-mail sent synchronously!");

		MailSender.mail(new TemplateExampleEmail("Template example email", "This is the body content of a template example e-mail", "this is an example, feel free to use it!")).to(to)
				.sendAsync(new AsyncCallback() {
					public void onSuccess() {
						System.out.println("Template e-mail sent asynchronously!");
					}

					public void onFailure(final Throwable e) {
						System.err.println("error: " + e.getMessage());
					}
				});
	}

}
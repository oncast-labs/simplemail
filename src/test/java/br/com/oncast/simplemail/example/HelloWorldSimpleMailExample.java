package br.com.oncast.simplemail.example;

import br.com.oncast.simplemail.MailSender;
import br.com.oncast.simplemail.MailSender.AsyncCallback;
import br.com.oncast.simplemail.mail.impl.SimpleTextEmail;

public class HelloWorldSimpleMailExample {

	public static void main(final String[] args) {
		MailSender.send()
				.email(new SimpleTextEmail("Simple Subject", "Simple e-mail content text"))
				.to("email@example.com", "another@example.com")
				.andWait();
		System.out.println("Simple e-mail sent synchronously!");

		MailSender.send().email(new TemplateExampleEmail("Template example email", "This is the body content of a template example e-mail", "this is an example, feel free to use it!"))
				.individually()
				.to("email@example.com", "another@example.com")
				.inBackground(new AsyncCallback() {
					public void onSuccess() {
						System.out.println("Template e-mail sent asynchronously!");
					}

					public void onFailure(final Throwable e) {
						System.err.println("error: " + e.getMessage());
					}
				});
		System.out.println("Sending e-mail asynchronously!");
	}

}

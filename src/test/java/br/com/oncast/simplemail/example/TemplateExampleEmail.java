package br.com.oncast.simplemail.example;

import org.apache.velocity.VelocityContext;

import br.com.oncast.simplemail.mail.TemplateEmail;

public class TemplateExampleEmail implements TemplateEmail {

	private final String title;

	private final String bodyText;

	private final String footer;

	public TemplateExampleEmail(final String title, final String bodyText, final String footer) {
		this.title = title;
		this.bodyText = bodyText;
		this.footer = footer;
	}

	public String getSubject() {
		return "example subject";
	}

	public String getTemplatePath() {
		return "template/exampleTemplate.html";
	}

	public VelocityContext getContext() {
		final VelocityContext context = new VelocityContext();
		context.put("title", title);
		context.put("bodyText", bodyText);
		context.put("footer", footer);
		return context;
	}

}

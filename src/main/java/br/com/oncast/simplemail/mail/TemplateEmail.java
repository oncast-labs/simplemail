package br.com.oncast.simplemail.mail;

import org.apache.velocity.VelocityContext;

public interface TemplateEmail extends Email {

	String getTemplatePath();

	VelocityContext getContext();

}

Simplemail
==========

Simple e-mail sender using javax.mail that suports gmail's smtp

Usage
==========

Sending using g-mail:

first create configuration file named 'email.properties' and put the content bellow:
  
    email.user=email@example.com
    email.password=123456

then send synchronously:
  
    MailSender.mail(new SimpleTextEmail("Subject", "content text")).to("email@example.com").sendAndWait();

or asynchronously:
  
    MailSender.mail(new SimpleTextEmail("Subject", "content text")).to("email@example.com").sendAsync();
		
It also supports callbacks and HTML templates. Check it out!

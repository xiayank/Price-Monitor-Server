import org.apache.commons.mail.*;


/**
 * Created by NIC on 6/12/17.
 */
public class EmailSender {
    public void sendEmail() throws EmailException {
        Email email = new SimpleEmail();
        email.setHostName("smtp.googlemail.com");
        email.setSmtpPort(465);
        email.setAuthenticator(new DefaultAuthenticator("yan.xia.cs", "7474741123@xY"));
        email.setSSLOnConnect(true);
        email.setFrom("yan.xia.cs@gmail.com");
        email.setSubject("TestMail");
        email.setMsg("This is a test mail ... :-)");
        email.addTo("yan.xia.cs@gmail.com");
        email.send();
    }
}

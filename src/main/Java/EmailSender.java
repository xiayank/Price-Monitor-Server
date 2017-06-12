import org.apache.commons.mail.*;
import product.Product;

import java.util.ArrayList;


/**
 * Created by NIC on 6/12/17.
 */
public class EmailSender {
    public void sendProductsEmail(ArrayList<Product> productList) throws EmailException {
        HtmlEmail email = new HtmlEmail();
        email.setHostName("smtp.googlemail.com");
        email.setSmtpPort(465);
        email.setAuthenticator(new DefaultAuthenticator("yan.xia.cs", "7474741123@xY"));
        email.setSSLOnConnect(true);
        email.setFrom("yan.xia.cs@gmail.com");
        email.setSubject("TestMail");
        StringBuffer sb = new StringBuffer("<html>\n" +
                "<body>\n" +
                "\n"+
                "<h2>Total product number:"+productList.size()+"</h2> \n <hr>");
        for(Product product: productList){
            sb.append("<h2> Title: "+product.title +"</h2>\n"+
                    "<h3> Old Price: "+product.oldPrice +"</h3>\n"+
                    "<h3> New Price: "+product.newPrice +"</h3>\n"+
                    "<h3> Link: "+product.detailUrl +"</h3>\n <hr>");


//            email.setMsg("Title: "+product.title +"/n" );
//            email.setMsg("oldPrice: "+product.oldPrice +"/n" );
//            email.setMsg("detailUrl: "+product.detailUrl +"/n" );
        }
        sb.append("\n" +
                "</body>\n" +
                "</html>");
        email.setHtmlMsg(sb.toString());
        // set the alternative message
        email.setTextMsg("Your email client does not support HTML messages");
        email.addTo("yan.xia.cs@gmail.com");
        email.send();
    }
}

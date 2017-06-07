import com.rabbitmq.client.*;
import org.apache.commons.lang.SerializationUtils;
import product.Product;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by NIC on 6/6/17.
 */
public class MonitorMain {
    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare("Q_demo",true,false,false,null);
        Consumer consumer = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                Product product = (Product) SerializationUtils.deserialize(body);
                System.out.println("title     --> " + product.title);
                System.out.println("detailUrl --> " + product.detailUrl);
                System.out.println("price     --> " + product.price);
                product.category = "Sports&Outdoors";
            }
        };

        channel.basicConsume("Q_demo", true, consumer);


    }

}

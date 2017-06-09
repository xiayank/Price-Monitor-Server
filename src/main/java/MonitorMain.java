import com.rabbitmq.client.*;
import net.spy.memcached.MemcachedClient;
import org.apache.commons.lang.SerializationUtils;
import product.Product;

import java.io.IOException;
import java.net.InetSocketAddress;
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
                System.out.println("id  -->" + product.productId);
                System.out.println("price     --> " + product.price);

                double newPrice = product.price;
                MemcachedClient cache = new MemcachedClient(new InetSocketAddress("127.0.0.1",11211));
                if(cache.get(product.productId) instanceof Double){
                    double cachedPrice = (double) cache.get(product.productId);
                    //1.update cached price
                    cache.set(product.productId, 72000, newPrice);
                    //2.update DB: oldPice = cacahedPrice,newPrice = newPrice

                    if(cachedPrice > newPrice){
                        //price reduce -> flag = 1

                        //add id into queue
                    }else{
                        //price increase -> flag = 0

                    }

                }else {
                    //set cache
                    cache.set(product.productId, 72000, newPrice);
                    //set database

                }



            }
        };

        channel.basicConsume("Q_demo", true, consumer);


    }

}

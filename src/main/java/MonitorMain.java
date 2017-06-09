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
    static final String mysql_host = "127.0.0.1:3306";
    static final String mysql_db = "project";
    static final String mysql_user = "root";
    static final String mysql_psw = "1127";

    public static void main(String[] args) throws IOException, TimeoutException {
        final MySQLAccess sqlAccess = new MySQLAccess(mysql_host, mysql_user, mysql_psw,mysql_db);

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
                System.out.println("price     --> " + product.newPrice);

                double newPrice = product.newPrice;
                MemcachedClient cache = new MemcachedClient(new InetSocketAddress("127.0.0.1",11211));

                if(cache.get(product.productId) instanceof Double){
                    //get old Price from cache
                    double cachedPrice = (double) cache.get(product.productId);

                    //Price has changed, update database and cache
                    //if(cachedPrice != newPrice){
                        //1.update DB: oldPice = cacahedPrice,newPrice = newPrice
                        try {
                            sqlAccess.updatePrice(product.productId, cachedPrice, newPrice);
                            System.out.println("update product" + product.productId + " " + product.newPrice);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //2.update cached price
                        cache.set(product.productId, 72000, newPrice);
                    //}




                //Current product not exist, add it into DB and cache
                }else {
                    //set cache
                    cache.set(product.productId, 72000, newPrice);
                    //set database
                    try {
                        sqlAccess.addProductData(product);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }



            }
        };

        channel.basicConsume("Q_demo", true, consumer);


    }

}

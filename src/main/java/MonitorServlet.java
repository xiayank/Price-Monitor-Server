import com.rabbitmq.client.*;
import net.spy.memcached.MemcachedClient;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.mail.EmailException;
import product.Product;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

/**
 * Servlet implementation class MonitorServlet
 */
@WebServlet("/MonitorServlet")
public class MonitorServlet extends HttpServlet {
	private ServletConfig config = null;
	private static final long serialVersionUID = 1L;
	static final String mysql_host = "127.0.0.1:3306";
	static final String mysql_db = "project";
	static final String mysql_user = "root";
	static final String mysql_psw = "1127";


	/**
     * @see HttpServlet#HttpServlet()
     */
    public MonitorServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		this.config =  config;
		super.init(config);
		System.out.println("server start!!!!!!!!!!!!!!");
//		ServletContext application = config.getServletContext();
//	    String adsDataFilePath = application.getRealPath(application.getInitParameter("adsDataFilePath"));
//		int memcachedPortal = Integer.parseInt(application.getInitParameter("memcachedPortal"));
//		this.adsEngine = new AdsEngine(adsDataFilePath,budgetDataFilePath,memcachedServer,memcachedPortal,mSynonymsFilePath,mysqlHost,mysqlDb,mysqlUser,mysqlPass);
//		this.adsEngine.init();
//		System.out.println("adsEngine initilized");
		final MySQLAccess sqlAccess = new MySQLAccess(mysql_host, mysql_user, mysql_psw,mysql_db);

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("127.0.0.1");
		Connection connection = null;

		try {
			connection = factory.newConnection();
			Channel channel = connection.createChannel();
			channel.queueDeclare("Q_demo",true,false,false,null);
			Consumer consumer = new DefaultConsumer(channel){
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope,
										   AMQP.BasicProperties properties, byte[] body)
						throws IOException {
					Product product = (Product) SerializationUtils.deserialize(body);


					double newPrice = product.newPrice;
					MemcachedClient cache = new MemcachedClient(new InetSocketAddress("127.0.0.1",11211));

					if(cache.get(product.productId) instanceof Double){
						//get old Price from cache
						double cachedPrice = (double) cache.get(product.productId);

						//Price has changed, update database and cache
						if(cachedPrice != newPrice){
							//1.update DB: oldPice = cacahedPrice,newPrice = newPrice
							try {
								sqlAccess.updatePrice(product.productId, cachedPrice, newPrice);
								System.out.println("update product" + product.productId + " " + product.newPrice);
							} catch (Exception e) {
								e.printStackTrace();
							}
							//2.update cached price
							cache.set(product.productId, 72000, newPrice);
						}




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
						System.out.println("Add into database:Id  -->" + product.productId);
						System.out.println("Add into database: Price --> " + product.newPrice);



					}



				}
			};

			channel.basicConsume("Q_demo", true, consumer);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}




	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		EmailSender emailSender = new EmailSender();


		System.out.println("request ACK!!!!!!!!!!!!!!");
		String username = request.getParameter("username");
		MySQLAccess sqlAccess = new MySQLAccess(mysql_host, mysql_user, mysql_psw,mysql_db);
		String subscribe = null;
		try {
			subscribe = sqlAccess.getUserSubscribe(username);
		} catch (Exception e) {
			e.printStackTrace();
		}

		ArrayList<Product> productList = null;
		try {
			productList = sqlAccess.getReducedProductListBasedCategory("Sports&Outdoors");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(productList.size());
		for(Product reducedProduct :productList){
			System.out.println(reducedProduct.title);
		}
		//System.out.println(subscribe);
		try {
			emailSender.sendEmail();
		} catch (EmailException e) {
			e.printStackTrace();
		}

	}
}

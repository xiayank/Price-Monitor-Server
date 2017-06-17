import com.rabbitmq.client.*;
import net.spy.memcached.MemcachedClient;
import org.apache.commons.lang.SerializationUtils;
import product.Product;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
	static final String productsQueueName_1 = "LevelOne";
	static final String productsQueueName_2 = "LevelTwo";
	static final String productsQueueName_3 = "LevelThree";
	static final String reducedQueueName = "ReducedProducts";

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

		MemcachedClient cache = null;
		try {
			cache = new MemcachedClient(new InetSocketAddress("127.0.0.1",11211));
		} catch (IOException e) {
			e.printStackTrace();
		}
		//consumer queue, decide whether the price has reduced or not
		productQueueConsumer(productsQueueName_1, reducedQueueName,cache);
		productQueueConsumer(productsQueueName_2,reducedQueueName, cache);

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("127.0.0.1");
		Connection connection = null;
		try {
			connection = factory.newConnection();
			Channel consumer_Channel = connection.createChannel();
			consumer_Channel.queueDeclare(reducedQueueName,true,false,false,null);
			Consumer consumer = new DefaultConsumer(consumer_Channel) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope,
										   AMQP.BasicProperties properties, byte[] body)
						throws IOException {
					Product product = (Product) SerializationUtils.deserialize(body);
					EmailSender emailSender = new EmailSender();
					MySQLAccess sqlAccess = new MySQLAccess(mysql_host, mysql_user, mysql_psw,mysql_db);

					try {
						ArrayList<String> emailList = sqlAccess.getAllEmails();
						ArrayList<Product> productList = new ArrayList<>();
						productList.add(product);
						for(String email :emailList){

							emailSender.sendProductsEmail(productList,email);

						}
					} catch (Exception e) {
						e.printStackTrace();
					}


				}
			};
			consumer_Channel.basicConsume(reducedQueueName, true, consumer);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		//create consumer Channel




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
		String userSubscribe = null;
		String userEmail = null;
		try {
			userSubscribe = sqlAccess.getUserSubscribe(username);

			userEmail = sqlAccess.getUserEmailByUsername(username);


			ArrayList<Product> productList = null;

			productList = sqlAccess.getReducedProductListBasedCategory(userSubscribe);


			System.out.println(productList.size());

			for(Product reducedProduct :productList){
			System.out.println(reducedProduct.title);
			}
			//send email
			emailSender.sendProductsEmail(productList,userEmail );

			//show on page
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			StringBuffer sb = new StringBuffer("<html>\n" +
					"<body>\n" +
					"\n"+
					"<h2>Total product number:"+productList.size()+"</h2> \n <hr>");
			for(Product product: productList){
				sb.append("<h2> Title: "+product.title +"</h2>\n"+
						"<h3> Old Price: "+product.oldPrice +"</h3>\n"+
						"<h3> New Price: "+product.newPrice +"</h3>\n"+
						"<h3> Link: "+product.detailUrl +"</h3>\n <hr>");

			}
			sb.append("\n" +
					"</body>\n" +
					"</html>");
			out.println(sb.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void productQueueConsumer(String ConsumerQueueName, String producerQueueName, MemcachedClient cache){
		final MySQLAccess sqlAccess = new MySQLAccess(mysql_host, mysql_user, mysql_psw,mysql_db);

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("127.0.0.1");
		Connection connection = null;

		try {
			connection = factory.newConnection();
			//create consumer Channel
			Channel consumer_Channel = connection.createChannel();
			consumer_Channel.queueDeclare(ConsumerQueueName,true,false,false,null);

			//create producer Channel
			Channel producer_Channel = connection.createChannel();
			producer_Channel.queueDeclare(producerQueueName, true, false,false,null);
//			producer_Channel.close();

			Consumer consumer = new DefaultConsumer(consumer_Channel){
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope,
										   AMQP.BasicProperties properties, byte[] body)
						throws IOException {
					Product product = (Product) SerializationUtils.deserialize(body);

					double newPrice = product.newPrice;

					//decide where there is key "productId" in cached or not
					if(cache.get(product.productId) instanceof Double){
						//get old Price from cache
						double cachedPrice = (double) cache.get(product.productId);

						//Price has changed, update database and cache
						if(cachedPrice != newPrice){
							//1.update DB : oldPice = cacahedPrice,newPrice = newPrice
							try {

								if(cachedPrice > newPrice){

									//update DB
									double percentage = (cachedPrice - newPrice) / cachedPrice ;
									sqlAccess.updatePrice(product.productId, cachedPrice, newPrice,percentage);
									//push reduced product into queue
									product.oldPrice = cachedPrice;
									producer_Channel.basicPublish("", producerQueueName, null, SerializationUtils.serialize(product));
									//producer_Channel.close();


								//if price increased -> update DB
								}else {
									sqlAccess.updatePrice(product.productId, cachedPrice, newPrice,0);

									System.out.println("update product " + product.productId + " " + newPrice + " " + cachedPrice);
									System.out.println(product.detailUrl);

									Calendar cal = Calendar.getInstance();
									SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
									System.out.println( "current time "+ sdf.format(cal.getTime()) );
								}

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
							product.oldPrice = 0;
							product.reducedPercentage = 0;
							sqlAccess.addProductData(product);
						} catch (Exception e) {
							e.printStackTrace();
						}
						System.out.println("Add into database:Id  -->" + product.productId);
						System.out.println("Add into database: Price --> " + product.newPrice);



					}



				}
			};

			consumer_Channel.basicConsume(ConsumerQueueName, true, consumer);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
	}
}

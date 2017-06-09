import product.Product;

import java.sql.*;

/**
 * Created by NIC on 6/8/17.
 */
public class MySQLAccess {
    private Connection connection = null;
    private String user_name;
    private String psw;
    private String server_name;
    private String db_name;

    public void close(){
        System.out.println("Closing database");
        try {
            if (connection != null) {
                connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    public MySQLAccess(String server, String user, String psw, String db){
        this.user_name = user;
        this.psw = psw;
        this.server_name = server;
        this.db_name =db;
    }

    private Connection getConnection () throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        String conn = "jdbc:mysql://" + server_name + "/" +
                db_name+"?user="+user_name+"&password="+psw;
        System.out.println("Connecting to database: " + conn);
        connection = DriverManager.getConnection(conn);
        System.out.println("Connected to database");
        return connection;
    }

    private Boolean isRecordExist(Connection connect,String sql_string) throws SQLException {
        PreparedStatement existStatement = null;
        boolean isExist = false;

        try
        {
            existStatement = connect.prepareStatement(sql_string);
            ResultSet result_set = existStatement.executeQuery();
            if (result_set.next())
            {
                isExist = true;
            }
        }
        catch(SQLException e )
        {
            System.out.println(e.getMessage());
            throw e;
        }
        finally
        {
            if (existStatement != null)
            {
                existStatement.close();
            }
        }

        return isExist;
    }

    public void addAdData(Product product) throws Exception {
        Connection connect = null;
        boolean isExist = false;
        String sql_string = "select adId from " + db_name + ".PriceMonitor where ProductId=" + product.productId;
        PreparedStatement product_info = null;
        try
        {
            connect = getConnection();
            //isExist = isRecordExist(connect, sql_string);
        }
        catch(SQLException e )
        {
            System.out.println(e.getMessage());
            throw e;
        }
        finally
        {
            if(connect != null && isExist) {
                connect.close();
            }
        }

        if(isExist) {
            return;
        }

        sql_string = "insert into " + db_name +".PriceMonitor(ProductId, Title, OldPrice, NewPirce, Flag, Category) "
                + "values(?,?,?,?,?,?)";
        try {
            product_info = connect.prepareStatement(sql_string);
            product_info.setString(1, product.productId);
            product_info.setString(2, product.title);

            product_info.setDouble(3, 0);
            product_info.setDouble(4, product.newPrice);
            product_info.setInt(5, 0);
            product_info.setString(6, product.category);
            product_info.setString(7,product.detailUrl);
            product_info.executeUpdate();
        }
        catch(SQLException e )
        {
            System.out.println(e.getMessage());
            throw e;
        }
        finally
        {
            if (product_info != null) {
                product_info.close();
            };
            if (connect != null) {
                connect.close();
            }
        }
    }

    public Product getProduct(String productId) throws Exception {
        Connection connect = null;
        PreparedStatement adStatement = null;
        ResultSet result_set = null;
        Product product = new Product();
        String sql_string = "select * from " + db_name + ".PriceMonitor where ProductId=" + productId;
        try {
            connect = getConnection();
            adStatement = connect.prepareStatement(sql_string);
            result_set = adStatement.executeQuery();
            while (result_set.next()) {
                product.productId = result_set.getString("ProductId");
                product.title = result_set.getString("Title");
                product.newPrice = result_set.getDouble("NewPrice");
                product.oldPrice = result_set.getDouble("OldPrice");
                product.category = result_set.getString("Category");
                product.detailUrl = result_set.getString("URL");


            }
        }
        catch(SQLException e )
        {
            System.out.println(e.getMessage());
            throw e;
        }
        finally
        {
            if (adStatement != null) {
                adStatement.close();
            };
            if (result_set != null) {
                result_set.close();
            }
            if (connect != null) {
                connect.close();
            }
        }
        return product;
    }

}


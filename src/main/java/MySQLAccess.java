import product.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by NIC on 6/8/17.
 */
public class MySQLAccess {
    private Connection connection = null;
    private String db_user_name;
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
        this.db_user_name = user;
        this.psw = psw;
        this.server_name = server;
        this.db_name =db;
    }

    private Connection getConnection () throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        String conn = "jdbc:mysql://" + server_name + "/" +
                db_name+"?user="+ db_user_name +"&password="+psw;
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

    public void addProductData(Product product) throws Exception {
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

        sql_string = "insert into " + db_name +".PriceMonitor(ProductId, Title, OldPrice, NewPrice, Reduced_Percentage, Category, URL) "
                + "values(?,?,?,?,?,?,?)";
        try {
            product_info = connect.prepareStatement(sql_string);
            product_info.setString(1, product.productId);
            product_info.setString(2, product.title);

            product_info.setDouble(3, product.oldPrice);
            product_info.setDouble(4, product.newPrice);
            product_info.setDouble(5, product.reducedPercentage);
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

    public Product getProductBasedId(String productId) throws Exception {
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

    public ArrayList<Product> getReducedProductListBasedCategoryMap(HashMap<String, Double> categoryMap) throws Exception {
        Connection connect = null;
        PreparedStatement adStatement = null;
        ResultSet result_set = null;
        ArrayList<Product>productList = new ArrayList<>();

        for( String category : categoryMap.keySet()){
            double threshold = categoryMap.get(category);

            System.out.println("category ->"+ category+ ", threshold ->" + threshold);

            String sql_string = "select * from " + db_name + ".PriceMonitor where Category=" +"'"+ category+"' AND Reduced_Percentage > " + threshold ;
            try {
                connect = getConnection();
                adStatement = connect.prepareStatement(sql_string);
                result_set = adStatement.executeQuery();
                while (result_set.next()) {
                    Product product = new Product();
                    product.productId = result_set.getString("ProductId");
                    product.title = result_set.getString("Title");
                    product.newPrice = result_set.getDouble("NewPrice");
                    product.oldPrice = result_set.getDouble("OldPrice");
                    product.category = result_set.getString("Category");
                    product.detailUrl = result_set.getString("URL");
                    product.reducedPercentage = result_set.getDouble("Reduced_Percentage");

                    productList.add(product);
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

        }


        return productList;
    }

    public ArrayList<HashMap<String, Double>> getUserSubscribeAndThreshold(String username) throws Exception {
        Connection connect = null;
        PreparedStatement adStatement = null;
        ResultSet result_set = null;
        ArrayList<HashMap<String, Double>> userSubscribeList = new ArrayList<>();
        String sql_string = "select * from " + db_name + ".Users where username=" +"'"+ username+"'";
        try {
            connect = getConnection();
            adStatement = connect.prepareStatement(sql_string);
            result_set = adStatement.executeQuery();
            while (result_set.next()) {

                String userSubscribe = result_set.getString("subscribe");
                double threshold = result_set.getDouble("threshold");
                HashMap<String,Double> map =  new HashMap<String,Double>();
                map.put(userSubscribe, threshold);
                userSubscribeList.add(map);
                //System.out.println(userSubscribe);

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
        return userSubscribeList;
    }

    //By default, return first email of user. Each user has one email
    public String getUserEmailByUsername(String username) throws Exception {
        Connection connect = null;
        PreparedStatement adStatement = null;
        ResultSet result_set = null;
        String userEmail = "";
        String sql_string = "select * from " + db_name + ".Users where username=" +"'"+ username+"'";
        try {
            connect = getConnection();
            adStatement = connect.prepareStatement(sql_string);
            result_set = adStatement.executeQuery();
            while (result_set.next()) {

                userEmail = result_set.getString("Email");

                //System.out.println(userSubscribe);

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
        return userEmail;
    }

    public HashSet<String> getAllEmails() throws Exception {
        Connection connect = null;
        PreparedStatement adStatement = null;
        ResultSet result_set = null;
        String userEmail = "";
        HashSet<String> emailSet = new HashSet<>();
        String sql_string = "select Email from " + db_name + ".Users ";
        try {
            connect = getConnection();
            adStatement = connect.prepareStatement(sql_string);
            result_set = adStatement.executeQuery();

            while (result_set.next()) {

                userEmail = result_set.getString("Email");
                if(!emailSet.contains(userEmail)){
                    emailSet.add(userEmail);
                }
                //System.out.println(userSubscribe);

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
        return emailSet;
    }

    public void updatePrice(String productId, Double oldPrice, Double newPrice, Double percentage) throws Exception {
        Connection connect = null;
        PreparedStatement updateStatement = null;
        String sql_string= "UPDATE "+ db_name + ".PriceMonitor SET OldPrice = ?, NewPrice = ?, Reduced_Percentage = ? WHERE ProductId = ?";

        System.out.println("sql: " + sql_string);
        try
        {   System.out.println("update product" + oldPrice + " " + newPrice);
            connect = getConnection();
            updateStatement = connect.prepareStatement(sql_string);
            updateStatement.setDouble(1, oldPrice);
            updateStatement.setDouble(2, newPrice);
            updateStatement.setString(4, productId);
            updateStatement.setDouble(3, percentage);
//            if(oldPrice > newPrice){
//                updateStatement.setInt(3,1);
//            }else {
//                updateStatement.setInt(3,0);
//            }

            updateStatement.executeUpdate();
        }
        catch(SQLException e )
        {
            System.out.println(e.getMessage());
            throw e;
        }
        finally
        {
            if(updateStatement != null) {
                updateStatement.close();
            }
            if(connect != null) {
                connect.close();
            }
        }

    }
}


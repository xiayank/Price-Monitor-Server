# Price-Monitor-Server
This server implement pull and push notify model.
## Part1: Project Design
### User Case
User for this app will be the customer of Amazon who wants to know the price reducing of particular category products. User will first subscribe a few categories they are interesting, then they will receive emails of products list if there are price reducing. Also, user can also select the email sending frequency. The default will be once per day. 

### Architecture Design
![Architecture schema](https://github.com/xiayank/Price-Monitor-Server/blob/master/src/main/resources/Architecture.png)


###  Component Design
#### Seed URL
- Make a list URL of various categories, then crawl the sub-category url on the page

#### Crawler

- Distribute Crawlers for different categories
- According to users' interest, total user subscribe, assign different priority to different category. 
- Higher priority products will be crawled at a higher frequency, vice versa. 
- **Discovery Crawler : ** 
	- Use seed url as input to crawl the sub-category url on the page, which contains product list.
- **Product Crawler :**
	- Need to be crawled : `ProductId`,`Title`, `URL`, `Price`, `Category`
	- Send data to different rabbit MQ based different category.

#### Product Queue
- Data:  All crawled product info
- Format : `Product` bean
- Implementation:  Rabbit MQ
- Different category products in different queue

#### Price Monitor Server
- Consume data from `Product Queue`.
- Compare the item with the key-value store. Check whether the item has been crawled or not. 
- If the item is exist in key-value store and also the price has changed, put the value in file `price` into `oldPrice`. Then update the current price in DB as `price`. If the product has not been crawled before, insert a new row of this item into database and new key-value into store.
- After comparing with key-value store, if the price is reduce, input this product into `Reduced Item Queue`.
- Notify User:
	- **Pull Model** : After User send a pull request to server. Response to user request, retrieve all the reduced price products of request category from database.
	- **Push Model** :  Send price reduced products to users at fixed period. See `Push Server` for more.

#### Push Server
- Consume Product info from `Reduced Item Queue`, send it to the client by email
- The email frequency is either customize or default
- Combined into `Price Monitor Server`

#### Key-Value Store
- Apply `memcached` or `Redis` to implement
- Filed: `Key: ProductId`, `Value:price`

#### Price Reduced  Queue
- Data: Price reduced products info.
- Format:   `Product` bean.
- Implementation:  Rabbit MQ

#### MySQL Database
###### Product DB
- Field : ProductId, Title, NewPrice, OldPrice, , Reduced_Percentage, Category,, URL

###### User DB
-	Field `Id`,`Username`, `Password`, `Subscribe`,`Email`,`Threshold`




### Bottlenecks
#### Crawler + queue
- Use multiple Crawler + queue models

#### Price monitor server
- Apply multiple servers to handle the messages from queue

#### Capacity estimation
- Key-Value store
> Number of amazon product : 480 millions
> Key: 16 bytes
> Val: 4 bytes
> (16+4) * 480,000,000 = 9,600,000,000 bytes = 9.6 GB
> 9.6GB memory is not a problem for our system.

- MySQL
> Number of amazon product : 480 millions
> Size of product: title: 50 byte , price:4 byte, url: 100 bytes, last price: 4 byte, category: 20 bytes
> 200 * 480,000,000 = 96,000,000,000 = 96 GB
> 96GB disk is not a problem for our system


## Part2: Run Project
## Run Crawler first
[Amazon-Preparing-Crawler](https://github.com/xiayank/Amazon-Preparing-Crawler)
## Getting started

### Memcached
Key-Value: Value can be any type object.

#### Install Memcached on Mac
```bash
> brew install memcached
```

#### Start Up Memcached Server: 
```bash
memcached -d -p 11211
```
#### Shut down
```bash
pidof memcached
kill -9 'PID number'
```

### MySQL

Create new user 'testuser' with password 'testpass' on MySQL server.
```
CREATE USER 'testuser'@'localhost' IDENTIFIED BY 'testpass';
GRANT ALL PRIVILEGES ON *.* TO 'testuser'@'localhost';
```
Test login with testuser.
```
> mysql -u testuser -p
Enter password: testpass
```
Restore DB structure from dump file.
```
> mysql -u testuser -p < Users.sql
> mysql -u testuser -p < PriceMonitor.sql
Enter password: testpass
```
Check the table structure
```
> use searchads;
> show tables;
```
Add rows into `Users` table by following table structure:
```sql
  `username` varchar(255) DEFAULT NULL,
  `subscribe` varchar(255) DEFAULT NULL,
  `Email` varchar(255) DEFAULT NULL,
  `threshold` double DEFAULT NULL,
```
Notice: You can select one category from following list:
```text
Book
Electronics&Computer
Home,Garden&Tools
Food&Grocery
Beauty&Health
Toys,Kids&Baby
Handmade
Sports&Outdoors
Automotive&Industrial
```




### Run Application

#### Build server
```bash
mvn clean install
```
#### Start server
```bash
mvn jetty:run
```

### Test server
#### Pull server 
Go to page :
```
http://localhost:8080/Amazon-Price-Monitor/
```
Input your username, then you will receive a email about the reduced price products from your subscribe.

#### Push server
You will receive a instant email notify if there is price decrease. 
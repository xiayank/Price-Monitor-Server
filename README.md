# Price-Monitor-Server
This server implement pull and push notify model.

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
<h1> 
    <a href="https://magician-io.com">Magician-JDBC</a> ·
    <img src="https://img.shields.io/badge/licenes-MIT-brightgreen.svg"/>
    <img src="https://img.shields.io/badge/jdk-17+-brightgreen.svg"/>
    <img src="https://img.shields.io/badge/release-master-brightgreen.svg"/>
</h1>

Magician-JDBC is the official JDBC component of Magician, supporting multiple data sources, no sql single table operations, complex operations can write sql, transaction management, etc.

## Documentation

[https://magician-io.com/db](https://magician-io.com/db)

## Example

### Importing dependencies

```xml
<dependency>
    <groupId>com.github.yuyenews</groupId>
    <artifactId>Magician-JDBC</artifactId>
    <version>2.0.5</version>
</dependency>

<!-- mysql driver package -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.20</version>
</dependency>
<!-- druid connection pool -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid</artifactId>
    <version>1.2.5</version>
</dependency>

<!-- This is the log package, which supports any package that can be bridged with slf4j -->
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-jdk14</artifactId>
    <version>1.7.12</version>
</dependency>
```
### Creating a data source
```java
// Here is an example using druid, which can actually support any connection pool that implements the DataSource interface
DruidDataSource dataSource = new DruidDataSource();

Properties properties = new Properties();
properties.put("druid.name", "local");
properties.put("druid.url", "jdbc:mysql://127.0.0.1:3306/martian-test?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf8&autoReconnect=true&rewriteBatchedStatements=true&useSSL=false");
properties.put("druid.username", "root");
properties.put("druid.password", "123456");
properties.put("druid.driverClassName", Driver.class.getName());

dataSource.setConnectProperties(properties);
```

### Adding a data source to JDBC
```java
// Create JDBC, it is recommended to execute it only once when the project starts
MagicianJDBC.createJDBC()
        .addDataSource("a", dataSource)// Add data source, this method can be called multiple times to add multiple data sources
        .defaultDataSourceName("a");// Set the name of the default data source
```

### Single Table Operations

Search by condition
```java
List<Condition> conditionList = ConditionBuilder.createCondition()
            .add("id > ?", 10)
            .add("and (name = ? or age > ?)", "bee", 10))
            .add("order by create_time", Condition.NOT_WHERE))
            .build();

List<Map> result = JDBCTemplate.get().select("table name", conditionList, Map.class);
```

Delete by condition
```java
List<Condition> conditionList = ConditionBuilder.createCondition()
        .add("id = ?", 10)
        .build();

JDBCTemplate.get().delete("table name", conditionList);
```

Insert a piece of data
```java
DemoPO demoPO = new DemoPo();
demoPO.setName("bee");
demoPo.setAge(10);

JDBCTemplate.get().insert("table name", demoPO);
```

Modify data
```java
DemoPO demoPO = new DemoPo();
demoPO.setName("bee");
demoPo.setAge(10);

List<Condition> conditionList = ConditionBuilder.createCondition()
        .add("id > ?", 10)
        .add("and name = ?", "bee"))
        .build();

JDBCTemplate.get().update("table name", demoPO, conditionList);
```

## Write your own sql

Select

```java
DemoPO demoPO = new DemoPo();
demoPO.setName("bee");
demoPo.setAge(10);

List<Map> result = JDBCTemplate.get().selectList("select * from xxx where name={name} and age={age}", demoPO, Map.class);
```

insert, delete, update

```java
DemoPO demoPO = new DemoPo();
demoPO.setName("bee");
demoPo.setAge(10);

JDBCTemplate.get().exec("update xxx set xxx = {xxx}, ccc = {ccc} where name={name} and age={age}", demoPO);
```

In addition, transaction management and paging are also supported, see the documentation for details
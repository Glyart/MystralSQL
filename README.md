# Mystral

An efficient library to deal with relational databases quickly.

A little request: read the [Javadoc](https://glyart.github.io/Mystral/) to understand how these elements work in deep.

## Prerequisites
This library requires Java 8.  
Before reading on, make sure the DBMS' drivers you are using are correctly loaded.
If you are working under __[Spigot API](https://www.spigotmc.org/wiki/index/)__, MySQL drivers will be already loaded.


## Maven artifacts

```XML
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.glyart</groupId>
        <artifactId>mystral</artifactId>
        <version>1.2.1-SNAPSHOT</version>
    </dependency>
</dependencies>
```

# Why Mystral?

Mystral merges Spring JDBC's user-friendliness and [HikariCP](https://github.com/brettwooldridge/HikariCP)'s speed (along with its connection pooling system).

This library helps to avoid writing boilerplate code which is common for developers dealing with JDBC.

Note: *this library follows the same logic as Spring JDBC. We simplified it and only took the very essentials elements for projects which don't need an entire framework support.*  
[Learn more about Spring JDBC](https://docs.spring.io/spring-framework/docs/4.0.x/spring-framework-reference/html/jdbc.html).  
[Learn more about JDBC](https://www.infoworld.com/article/3388036/what-is-jdbc-introduction-to-java-database-connectivity.html).

Also, Mystral supports asynchronous computation. Read more about this in the following sections.
# Getting started

Just start by instantiating a Credentials object.

```java
Credentials credentials = Credentials.builder()
  .host("yourHostName")
  .password("yourPassword")
  .user("yourUsername")
  .schema("yourDatabase")
  .pool("yourPoolName")
  //.port(3306) you can avoid specifying the port (default is 3306)
  .build();
```

## Working Asynchronously

If you are working synchronously, you can skip this section.

### Implementing Executor

In order to work, the AsyncDatabase class needs an implementation of the [Executor](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Executor.html) interface.
Internally, each data access operation is a task, implemented via [Runnable](https://docs.oracle.com/javase/7/docs/api/java/lang/Runnable.html) tasks.  
Such tasks are executed by your Executor implementation.

A very simple implementation:

```java
Executor exe = (command) -> new Thread(command).start();
```

If the environment you are working with has strictly rules for async computation, then you may need to use its scheduler.

For example:

```java
Executor exe = YourEnvironmentScheduler::schedule;
```

Here are some examples if you are working with Minecraft plugin-related projects.

Spigot:
```java
Executor exe = (command) -> Bukkit.getScheduler().runTaskAsynchronously(plugin, command);
```

Bungeecord:
```java
Executor exe = (command) -> getProxy().getScheduler().runAsync(plugin, command);
```

Velocity:
```java
Executor exe = (command) -> yourProxyServer.getScheduler().buildTask(plugin, command).schedule();
```

If you are using Sponge, you can get a ready-to-use Executor service called SpongeExecutorService:
```java
SpongeExecutorService exe = Sponge.getScheduler().createAsyncExecutor(plugin);
```

[Read more about task scheduling on Sponge](https://docs.spongepowered.org/stable/en/plugin/scheduler.html#).

### Instantiating AsyncDatabase

Finally, we are ready to use the AsyncDatabase class to access the database. You can get its instance by doing:

```java
AsyncDatabase asyncDb = Mystral.newAsyncDatabase(credentials, exe);
```

The AsyncDatabase class has a lot of methods, which can perform data access operations given callback objects.
Anyway, you don't need to do such complicated things: these methods are heavily overloaded.
Each overload gives different combination of parameters, until we get methods which don't need callback objects, because default callback implementations are already provided internally.

__Remember that every data access method returns a [CompletableFuture](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html) object. You must invoke the [whenComplete](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html#whenComplete-java.util.function.BiConsumer-) method when accessing the result(s), which could be null.__

## Working synchronously

If you need to work synchronously, just use the Database class.

```java
Database db = Mystral.newDatabase(credentials);
```
This class' usage is almost as similar as its asynchronous counterpart. The main differences are listed in the table below.

<br>
<table>
  <th colspan=3 style="text-align:center">Differences between usages</th>
  <tr>
    <td></td>
    <th><b style="font-size:30px">Sync</b></th>
    <th><b style="font-size:30px">Async</b></th>
  </tr>
  <tr>
    <th>What does it returns?</th>
    <td>The raw result. It could be null.</td>
    <td>A <b>never null</b> CompletableFuture object which <b>wraps</b> the result (it could be null).</td>
  </tr>
  <tr>
    <th>How do I handle exceptions?</th>
    <td>Exceptions in sync usage are unchecked. You don't have to use a try-catch block if it isn't needed.</td>
    <td>You can handle exceptions inside the whenComplete method.</td>
  </tr>
  <tr>
    <th>Who carries out the operations?</th>
    <td>The thread where methods are called from.</td>
    <td>The Executor implementation.</td>
  </tr>
</table>
<br>

# Usage

Note: <i>the following examples will deal with asynchronous usage</i>.

Let's assume we are working with a simple table:

![https://i.imgur.com/AFatpsY.png](https://i.imgur.com/AFatpsY.png)

We need to represent tables by using Java classes, but this is simple either:

```java
public class User {

    private int id;
    private String name;
    private int score;

    public User(int id, String name) {
        this.id = id;
        this.name = name;	
    }

    // getters, setter, constructor(s)
}
```

This section will only show examples on methods whose parameters take:

- SQL statements (static or with "?" placeholders);
- Array of objects representing the SQL statement's parameters (needed with parametrized SQL statements only) and array of [Types](https://docs.oracle.com/javase/8/docs/api/java/sql/Types.html) representing parameters' types;
- Lambda functions (ResultSetRowMapper) which hold a mapping logic for supplying results (query methods only);
- Two other interfaces used for batch updates. They will be discussed in the batch updates section.

### Query

If you need to query the database you can use two methods: queryForList and queryForObject.

The first one gets a list of results, the second one gets one result. Use it when you are sure that the query will supply exactly one result.

Query methods need a ResultSetRowMapper implementation. A ResultSetRowMapper implementation maps a result for each ResultSet row (we don't worry about exceptions or empty ResultSets).

Example on getting a list by using a static SQL statement:

```java
CompletableFuture<List<User>> future = asyncDb.queryForList("SELECT * FROM users", (resultSet, rowNumber) -> {
    /* We use this ResultSetRowMapper implementation to work with ResultSet's rows.
    *  For example, if we want to get users with 0 score only we can do the following:
    */
    if (resultSet.getInt("score") == 0) {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setName(resultSet.getString("name"));
        return user;
    }
    return null;
});

// Just wait for the query to complete. When it's time, whenComplete method is executed
future.whenComplete((users, exception) -> {
    if (exception != null) {
        // you can handle the error
        return;
    }
    // "users" is the list of results, extracted from ResultSet with ResultSetRowMapper (users with 0 score)
    // note that the list can be empty, but never null
    for (User user : users) 
        player.sendMessage(user.getId() + " - " + user.getName());
});
```

Example on getting a single result by using an SQL statement with single parameter:

```java
// make sure to import java.sql.Types
String sql = "SELECT * FROM users WHERE id = ?";
CompletableFuture<User> future = asyncDb.queryForObject(sql, new Integer[] {1}, (resultSet, rowNumber) -> {
    // Code inside this lambda will be executed once
    return new User(resultSet.getInt(1), resultSet.getString(2), resultSet.getInt(3));
}, Types.INTEGER);

// Same logic as before
future.whenComplete((user, exception) -> {
    if (exception != null) {
        // you can handle the error
        return;
    }
    // Warning: a single result can be null
    if (user != null)
        player.sendMessage("Score of " + user.getName() + ": " + user.getScore());
});
```

Example on getting a single result by using an SQL statement with multiple parameters:

```java
String sql = "SELECT * FROM users WHERE id = ? OR score > ?";
// If parameter types are different we must use new Object[] {...}
// e.g. new Object[] {1, "HelloSecondParam", 4.4, otherRandomVariable}
CompletableFuture<User> future = asyncDb.queryForObject(sql, new Integer[] {1, 10}, (resultSet, rowNumber) -> {
    return new User(resultSet.getInt(1), resultSet.getString(2));
}, Types.INTEGER, Types.INTEGER);

// Same logic as before
future.whenComplete((user, exception) -> {
    if (exception != null) {
        // you can handle the error
        return;
    }
    // Warning: a single result can be null
    if (user != null) {
        // things
    }
});
```
<b>ATTENTION: SQL Types in methods' parameters are not mandatory. Avoiding SQL types will let Mystral to use the [PreparedStatement#setObject](https://docs.oracle.com/javase/7/docs/api/java/sql/PreparedStatement.html#setObject) method.  
This method's behavior depends on the JDBC Driver you're using. Each JDBC Driver has its own PreparedStatement class' implementation.
Be careful when not specifying SQL Types: this <i>could</i> lead to unpredictable results.
</b>


### Single update (delete, insert, update, create, drop...)

These methods can handle every type of update statement (static or not).

Every update method returns the number of the affected rows. By setting `getGeneratedKeys` argument on true, the method will return the primary key of the generated row (if it was really created).
Note: *right now, this works with numeric primary keys only. "getGeneratedKeys" is useless when you are not using an INSERT statement.*

The usage of these methods is as simple as the query ones. Here are some examples.

Update with parametrized SQL statement:

```java
String sql = "INSERT INTO users VALUES(?, ?, ?)";
CompletableFuture<Integer> future = asyncDb.update(sql, new Object[] {3, "ErMandarone", 10}, false, Types.INTEGER, Types.VARCHAR, Types.INTEGER);

// Same logic as before
future.whenComplete((integer, exception) -> {
    if (exception != null) {
        return; // you can handle the error
    }
    System.out.println(integer); // Expected 1
}
```

Update with static SQL statement:

```java
String sql = "INSERT INTO users VALUES(null, 'Helo', 50)";
CompletableFuture<Integer> future = asyncDb.update(sql, true, Types.NULL, Types.VARCHAR, Types.INTEGER);

// Same logic as before
future.whenComplete((integer, exception) -> {
    if (exception != null) {
        // you can handle the error
        return;
    }
    System.out.println(integer); // Expected the primary key of this new row
}
```

### Batch update (delete, insert, update, create, drop...)

These methods perform multiple updates by using the same SQL statement.

Right now, no results are supplied by Mystral's batch update methods. Anyway, you can handle possible exceptions.

Usage of these interfaces is encouraged when you are using these methods:

- BatchSetter;
- ParametrizedBatchSetter.

Also, you don't have to specify SQL Types when you're using these interfaces.  
Read their documentations for further information.

Example with BatchSetter:

```java
// Let's prepare 100 insert statements
List<User> users = new ArrayList<>();
for (int i = 0; i < 100; i++) 
    users.add(new User(i, "Test" + 1, 0));

String sql = "INSERT INTO users VALUES(?, ?, ?)";

CompletableFuture<Void> future = asyncDb.batchUpdate(sql, new BatchSetter() {
    @Override
    public void setValues(@NotNull PreparedStatement ps, int i) throws SQLException {
	    User user = users.get(i);
        ps.setInt(1, user.getId());
	    ps.setString(2, user.getName());
	    ps.setInt(3, 0); 
    }

    @Override
    public int getBatchSize() {
        return users.size();
    }
});

//Same logic as before
future.whenComplete((unused, exception) -> {
    if (exception != null) {
        // you can handle the error
    }
});

```

Example with ParametrizedBatchSetter:

```java
List<User> users = coolMethodFor100Users(); // Let's assume that "users" is a list containing 100 different users
String sql = "INSERT INTO users VALUES(?, ?, ?)";

CompletableFuture<Void> future = asyncDb.batchUpdate(sql, users, (ps, user) -> {
    ps.setInt(1, user.getId());
    ps.setString(2, user.getName());
    ps.setInt(3, user.getScore());
});

//Same logic as before
future.whenComplete((unused, exception) -> {
    if (exception != null) {
        // you can handle the error
    }
});

```

#
### A special thanks to xelverethx for her contribution.

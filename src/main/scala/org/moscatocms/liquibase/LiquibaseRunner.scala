package org.moscatocms.liquibase

import java.io.Writer
import liquibase.integration.ant.`type`.DatabaseType
import liquibase.database.DatabaseFactory
import java.sql.Driver
import java.util.Properties
import liquibase.database.jvm.JdbcConnection
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import liquibase.Liquibase
import liquibase.configuration.LiquibaseConfiguration
import liquibase.resource.ClassLoaderResourceAccessor

case class LiquibaseConfig(
    url: String,
    driver: String,
    user: String,
    password: String,
    changelog: String
)

object LiquibaseRunner {
  
  lazy val config = {
    val conf: Config = ConfigFactory.load.getConfig("moscato.liquibase")
    LiquibaseConfig(
      url = conf.getString("url"),
      driver = conf.getString("driver"),
      user = conf.getString("user"),
      password = conf.getString("password"),
      changelog = conf.getString("changelog")
    )
  }
  
  def createDatabase() = {
    val databaseFactory = DatabaseFactory.getInstance

    val driver: Driver = Class.forName(config.driver).newInstance.asInstanceOf[Driver]
    if (driver == null) {
        throw new RuntimeException(s"Could not instantiate the JDBC driver ${config.driver}")
    }
    
    val connectionProps = new Properties()
    connectionProps.setProperty("user", config.user)
    connectionProps.setProperty("password", config.password)
    
    val connection = driver.connect(config.url, connectionProps)
    if (connection == null) {
        throw new RuntimeException("Could not connect to the database.")
    }
    
    val jdbcConnection = new JdbcConnection(connection)
    val database = databaseFactory.findCorrectDatabaseImplementation(jdbcConnection)
    database
  }
  
  def update() {
    val writer: Writer = null
    val database = createDatabase()
    val resourceAccessor = new ClassLoaderResourceAccessor
    val liquibase = new Liquibase(config.changelog, resourceAccessor, database)
    liquibase.update("");
  }
  
}
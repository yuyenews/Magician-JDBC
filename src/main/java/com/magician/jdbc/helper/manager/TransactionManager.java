package com.magician.jdbc.helper.manager;

import com.magician.jdbc.core.constant.enums.TractionLevel;
import com.magician.jdbc.core.util.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * transaction management
 * @author yuye
 *
 */
public class TransactionManager {

	private static Logger logger = LoggerFactory.getLogger(TransactionManager.class);

	/**
	 * Get a database connection and set it to not commit automatically
	 * Put the acquired connection in the cache
	 */
	public static void beginTraction() {
		beginTraction(TractionLevel.READ_COMMITTED);
	}

	/**
	 * Get a database connection and set it to not commit automatically
	 * Put the acquired connection in the cache
	 */
	public static void beginTraction(TractionLevel tractionLevel) {
		try {
			Map<String, DataSource> maps = DataSourceManager.getDruidDataSources();

			Map<String, Connection> connections = new HashMap<>();

			for (String key : maps.keySet()) {
				Connection connection = maps.get(key).getConnection();
				connection.setAutoCommit(false);
				connection.setTransactionIsolation(tractionLevel.getLevel());
				connections.put(key, connection);
			}

			ThreadUtil.getThreadLocal().set(connections);
		} catch (Exception e) {
			logger.error("Error begin transaction", e);
		}
	}

	/**
	 * Get the current thread's database connection from the cache and commit the transaction
	 */
	public static void commit() throws Exception {
		Map<String, Connection> connections = null;
		try {
			connections = (Map<String, Connection>) ThreadUtil.getThreadLocal().get();

			for (String key : connections.keySet()) {
				Connection connection = connections.get(key);
				connection.commit();
			}
		} catch (Exception e) {
			logger.error("Error committing transaction", e);
			throw e;
		} finally {
			try {
				if(connections != null){
					for (String key : connections.keySet()) {
						Connection connection = connections.get(key);
						connection.close();
					}
				}
			} catch (Exception e){
				throw e;
			}
			ThreadUtil.getThreadLocal().remove();
		}
	}

	/**
	 * Get the current thread's database connection from the cache and roll back the transaction
	 */
	public static void rollback() throws SQLException {
		Map<String, Connection> connections = null;
		try {
			connections = (Map<String, Connection>) ThreadUtil.getThreadLocal().get();

			for (String key : connections.keySet()) {
				Connection connection = connections.get(key);
				connection.rollback();
			}
		} catch (Exception e) {
			logger.error("rollback transaction error", e);
			throw e;
		} finally {
			try {
				if(connections != null){
					for (String key : connections.keySet()) {
						Connection connection = connections.get(key);
						connection.close();
					}
				}
			} catch (Exception e){
				throw e;
			}
			ThreadUtil.getThreadLocal().remove();
		}
	}
}

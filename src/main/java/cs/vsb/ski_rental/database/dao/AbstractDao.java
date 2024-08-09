package cs.vsb.ski_rental.database.dao;

import cs.vsb.ski_rental.database.connection.ConnectionProvider;
import cs.vsb.ski_rental.database.connection.NamedParameterStatement;
import cs.vsb.ski_rental.database.connection.SQLCallable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

public abstract class AbstractDao<T> {

	/**
	 * ============= CRUD - Create part
	 */

	/**
	 * Insert the record.
	 *
	 * @param object to be inserted
	 * @return number of affected rows
	 * @throws SQLException if an error occurred
	 */
	public int insert(T object) throws SQLException {
    	return insert(object, null);
    }

	/**
	 * Insert the record.
	 *
	 * @param object to be inserted
	 * @param connection to be used
	 * @return number of affected rows
	 * @throws SQLException if an error occurred
	 */
	public int insert(T object, Connection connection) throws SQLException {
		return ensureConnection(connection, conn -> {
			try (NamedParameterStatement pStmt = new NamedParameterStatement(conn, getSQLInsert(), Statement.RETURN_GENERATED_KEYS)) {
				prepareCommand(pStmt, object);
				int result = pStmt.executeUpdate();
				setKeys(object, pStmt.getGeneratedKeys());
				return result;
			}
		});
	}

	/**
	 * ============= CRUD - Read part
	 */

	/**
	 * Select all records.
	 *
	 * @return collection of records
	 * @throws SQLException if an error occurred
	 */
	public Collection<T> select() throws SQLException {
		return select(null);
	}

	/**
	 * Select all records.
	 *
	 * @param connection to be used
	 * @return collection of records
	 * @throws SQLException if an error occurred
	 */
	public Collection<T> select(Connection connection) throws SQLException {
		return ensureConnection(connection, conn -> {
			try (ResultSet rs = conn.createStatement().executeQuery(getSQLSelect())) {
				return read(rs);
			}
		});
	}

	/**
	 * Select the record by id.
	 *
	 * @param id of the record
	 * @return record
	 * @throws SQLException if an error occurred
	 */
	public T select(int id) throws SQLException {
		return select(id,null);
	}

	/**
	 * Select the record by id.
	 *
	 * @param id of the record
	 * @param connection to be used
	 * @return record
	 * @throws SQLException if an error occurred
	 */
	public T select(int id, Connection connection) throws SQLException {
		return ensureConnection(connection, conn -> {
			try (NamedParameterStatement statement = new NamedParameterStatement(conn, getSQLSelectById())) {
				statement.setInt("id", id);

				try (ResultSet rs = statement.executeQuery()) {
					Collection<T> objects = read(rs);
					return objects.size() == 1 ? objects.iterator().next() : null;
				}
			}
		});
	}

	/**
	 * ============= CRUD - Update part
	 */

	/**
	 * Update the record.
	 *
	 * @param object to be updated
	 * @return number of affected rows
	 * @throws SQLException if an error occurred
	 */
	public int update(T object) throws SQLException {
		return update(object,null);
	}

	/**
	 * Update the record.
	 *
	 * @param object to be updated
	 * @param connection to be used
	 * @return number of affected rows
	 * @throws SQLException if an error occurred
	 */
	public int update(T object, Connection connection) throws SQLException {
		return ensureConnection(connection, conn -> {
			try (NamedParameterStatement pStmt = new NamedParameterStatement(conn, getSQLUpdate())) {
				prepareCommand(pStmt, object);
				return pStmt.executeUpdate();
			}
		});
	}

	/**
	 * ============= CRUD - Delete part
	 */

	/**
	 * Delete the record by id.
	 *
	 * @param id of the record
	 * @return number of affected rows
	 * @throws SQLException if an error occurred
	 */
	public int delete(int id) throws SQLException {
		return delete(id,null);
	}

	/**
	 * Delete the record by id.
	 *
	 * @param id of the record
	 * @param connection to be used
	 * @return number of affected rows
	 * @throws SQLException if an error occurred
	 */
    public int delete(int id, Connection connection) throws SQLException {
    	return ensureConnection(connection, conn -> {
    		try (NamedParameterStatement pStmt = new NamedParameterStatement(conn, getSQLDeleteById())) {
    			pStmt.setInt("id", id);
    			return pStmt.executeUpdate();
    		}
		});
    }

	/**
	 * ============= CRUD - SQL part
	 */

    protected abstract String getSQLInsert();

	protected abstract String getSQLSelect();

	protected abstract String getSQLSelectById();

	protected abstract String getSQLUpdate();

	protected abstract String getSQLDeleteById();

	protected abstract void prepareCommand(NamedParameterStatement statement, T object) throws SQLException;

	protected abstract Collection<T> read(ResultSet rs) throws SQLException;

	protected abstract void setKeys(T user, ResultSet generatedKeys) throws SQLException;

	private <V> V ensureConnection(Connection connection, SQLCallable<V> callable) throws SQLException {
		boolean closeConnection = false;

		if (connection == null) {
			connection = ConnectionProvider.getConnection();
			closeConnection = true;
		}

		try {
			return callable.call(connection);
		} finally {
			if (closeConnection) {
				connection.close();
			}
		}
	}

}

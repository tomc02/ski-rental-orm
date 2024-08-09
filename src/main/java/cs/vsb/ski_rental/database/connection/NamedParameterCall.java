package cs.vsb.ski_rental.database.connection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class NamedParameterCall implements NamedParameter {

	/** The statement this object is wrapping. */
	private final CallableStatement statement;

	/**
	 * Maps parameter names to arrays of ints which are the parameter indices.
	 */
	private final Map<String, Object> indexMap = new HashMap<>();

	/**
	 * Creates a NamedParameterStatement. Wraps a call to
	 * c.{@link Connection#prepareStatement(String) prepareStatement}.
	 *
	 * @param connection the database connection
	 * @param query      the parameterized query
	 * @throws SQLException if the statement could not be created
	 */
	public NamedParameterCall(Connection connection, String query) throws SQLException {
		String parsedQuery = parse(query);
		statement = connection.prepareCall(parsedQuery);
	}

	/**
	 * Sets a parameter.
	 * 
	 * @param name  parameter name
	 * @param value parameter value
	 * @throws SQLException             if an error occurred
	 * @throws IllegalArgumentException if the parameter does not exist
	 * @see PreparedStatement#setObject(int, Object)
	 */
	public void setObject(String name, Object value) throws SQLException {
		for (int index : getIndexes(name)) {
			statement.setObject(index, value);
		}
	}

	/**
	 * Sets a parameter.
	 * 
	 * @param name  parameter name
	 * @param value parameter value
	 * @throws SQLException             if an error occurred
	 * @throws IllegalArgumentException if the parameter does not exist
	 * @see PreparedStatement#setString(int, String)
	 */
	public void setString(String name, String value) throws SQLException {
		if (value == null) {
			setNull(name);
		} else {
			for (int index : getIndexes(name)) {
				statement.setString(index, value);
			}
		}
	}

	/**
	 * Sets a parameter.
	 * 
	 * @param name  parameter name
	 * @param value parameter value
	 * @throws SQLException             if an error occurred
	 * @throws IllegalArgumentException if the parameter does not exist
	 * @see PreparedStatement#setInt(int, int)
	 */
	public void setInt(String name, int value) throws SQLException {
		for (int index : getIndexes(name)) {
			statement.setInt(index, value);
		}
	}

	/**
	 * Sets a parameter.
	 * 
	 * @param name  parameter name
	 * @param value parameter value
	 * @throws SQLException             if an error occurred
	 * @throws IllegalArgumentException if the parameter does not exist
	 * @see PreparedStatement#setInt(int, int)
	 */
	public void setLong(String name, long value) throws SQLException {
		for (int index : getIndexes(name)) {
			statement.setLong(index, value);
		}
	}

	/**
	 * Sets a parameter.
	 * 
	 * @param name  parameter name
	 * @param value parameter value
	 * @throws SQLException             if an error occurred
	 * @throws IllegalArgumentException if the parameter does not exist
	 * @see PreparedStatement#setTimestamp(int, Timestamp)
	 */
	public void setTimestamp(String name, Timestamp value) throws SQLException {
		if (value == null) {
			setNull(name);
		} else {
			for (int index : getIndexes(name)) {
				statement.setTimestamp(index, value);
			}
		}
	}

	/**
	 * Sets a parameter.
	 * 
	 * @param name  parameter name
	 * @param value parameter value
	 * @throws SQLException             if an error occurred
	 * @throws IllegalArgumentException if the parameter does not exist
	 * @see PreparedStatement#setTimestamp(int, Timestamp)
	 */
	public void setDateTime(String name, LocalDateTime value) throws SQLException {
		setTimestamp(name, value == null ? null : Timestamp.valueOf(value));
	}

	public void setArray(String name, Array value) throws SQLException {
		if (value == null) {
			setNull(name);
		} else {
			for (int index : getIndexes(name)) {
				statement.setArray(index, value);
			}
		}
	}

	public void setBigDecimal(String name, java.math.BigDecimal value) throws SQLException {
		if (value == null) {
			setNull(name);
		} else {
			for (int index : getIndexes(name)) {
				statement.setBigDecimal(index, value);
			}
		}
	}
	/**
	 * Sets a parameter.
	 *
	 * @param name parameter name
	 * @param type parameter type
	 * @throws SQLException 		   if an error occurred
	 */
	public void registerOutParameter(String name, int type) throws SQLException {
		for (int index : getIndexes(name)) {
			statement.registerOutParameter(index, type);
		}
	}

	public Object getOutParameter(String name) throws SQLException {
		return statement.getObject(name);
	}

	/**
	 * Sets a parameter.
	 *
	 * @param name parameter name
	 * @param type parameter type
	 * @throws SQLException 		   if an error occurred
	 */
	public void registerOutParameter(String name, SQLType type) throws SQLException {
		for (int index : getIndexes(name)) {
			statement.registerOutParameter(index, type);
		}
	}

	/**
	 * Returns the underlying statement.
	 * 
	 * @return the statement
	 */
	public java.sql.CallableStatement getStatement() {
		return statement;
	}

	/**
	 * Executes the statement.
	 * 
	 * @return true if the first result is a {@link ResultSet}
	 * @throws SQLException if an error occurred
	 * @see java.sql.CallableStatement#execute()
	 */
	public boolean execute() throws SQLException {
		return statement.execute();
	}

	/**
	 * Executes the statement, which must be a query.
	 * 
	 * @return the query results
	 * @throws SQLException if an error occurred
	 * @see java.sql.CallableStatement#executeQuery()
	 */
	public ResultSet executeQuery() throws SQLException {
		return statement.executeQuery();
	}

	/**
	 * Executes the statement, which must be an SQL INSERT, UPDATE or DELETE
	 * statement; or an SQL statement that returns nothing, such as a DDL statement.
	 * 
	 * @return number of rows affected
	 * @throws SQLException if an error occurred
	 * @see java.sql.CallableStatement#executeUpdate()
	 */
	public int executeUpdate() throws SQLException {
		return statement.executeUpdate();
	}

	/**
	 * Closes the statement.
	 * 
	 * @throws SQLException if an error occurred
	 * @see CallableStatement#close()
	 */
	@Override
	public void close() throws SQLException {
		statement.close();
	}

	/**
	 * Adds the current set of parameters as a batch entry.
	 * 
	 * @throws SQLException if something went wrong
	 */
	public void addBatch() throws SQLException {
		statement.addBatch();
	}

	/**
	 * Executes all the batched statements.
	 * See {@link CallableStatement#executeBatch()} for details.
	 * 
	 * @return update counts for each statement
	 * @throws SQLException if something went wrong
	 */
	public int[] executeBatch() throws SQLException {
		return statement.executeBatch();
	}

	/**
	 * Returns the generated keys for an auto-incremented statement.
	 *
	 * @return generated keys
	 * @throws SQLException if something went wrong
	 */
	public ResultSet getGeneratedKeys() throws SQLException {
		return statement.getGeneratedKeys();
	}

	/**
	 * Returns the indexes for a parameter.
	 * 
	 * @param name parameter name
	 * @return parameter indexes
	 * @throws IllegalArgumentException if the parameter does not exist
	 */
	private int[] getIndexes(String name) {
		int[] indexes = (int[]) indexMap.get(name);

		if (indexes == null) {
			throw new IllegalArgumentException("Parameter not found: " + name);
		}

		return indexes;
	}

	/**
	 * Returns if the given parameter name exists.
	 *
	 * @param name parameter name
	 * @return if the parameter exists
	 */
	public boolean hasParameter(String name) {
		return indexMap.containsKey(name);
	}

	/**
	 * Parses a query with named parameters. The parameter-index mappings are put
	 * into the map, and the parsed query is returned. DO NOT CALL FROM CLIENT CODE.
	 * This method is non-private so JUnit code can test it.
	 * 
	 * @param query    query to parse
	 * @return the parsed query
	 */
	private String parse(String query) {
		// I was originally using regular expressions, but they didn't work well for
		// ignoring parameter-like strings inside quotes.
		int length = query.length();
		StringBuffer parsedQuery = new StringBuffer(length);
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		int index = 1;

		for (int i = 0; i < length; i++) {
			char c = query.charAt(i);

			if (inSingleQuote) {
				if (c == '\'') {
					inSingleQuote = false;
				}
			} else if (inDoubleQuote) {
				if (c == '"') {
					inDoubleQuote = false;
				}
			} else {
				if (c == '\'') {
					inSingleQuote = true;
				} else if (c == '"') {
					inDoubleQuote = true;
				} else if (c == ':' && i + 1 < length && Character.isJavaIdentifierStart(query.charAt(i + 1))) {
					int j = i + 2;
					while (j < length && Character.isJavaIdentifierPart(query.charAt(j))) {
						j++;
					}

					String name = query.substring(i + 1, j);
					c = '?'; // replace the parameter with a question mark
					i += name.length(); // skip past the end if the parameter

					@SuppressWarnings("unchecked")
					List<Integer> indexList = (List<Integer>) indexMap.get(name);
					if (indexList == null) {
						indexList = new LinkedList<>();
						indexMap.put(name, indexList);
					}

					indexList.add(index++);
				}
			}

			parsedQuery.append(c);
		}

		// replace the lists of Integer objects with arrays of ints
		for (Entry<String, Object> entry : indexMap.entrySet()) {
			@SuppressWarnings("unchecked")
			List<Integer> list = (List<Integer>) entry.getValue();
			int[] indexes = new int[list.size()];
			int i = 0;

			for (Integer x : list) {
				indexes[i++] = x.intValue();
			}

			entry.setValue(indexes);
		}

		return parsedQuery.toString();
	}

	private void setNull(String name) throws SQLException {
		for (int index : getIndexes(name)) {
			statement.setNull(index, Types.NULL);
		}
	}


}

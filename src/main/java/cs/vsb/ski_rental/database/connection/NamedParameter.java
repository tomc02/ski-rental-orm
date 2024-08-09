package cs.vsb.ski_rental.database.connection;

import java.sql.*;
import java.time.LocalDateTime;

public interface NamedParameter extends AutoCloseable {
    void setObject(String name, Object value) throws SQLException;

    void setString(String name, String value) throws SQLException;

    void setInt(String name, int value) throws SQLException;

    void setLong(String name, long value) throws SQLException;

    void setTimestamp(String name, Timestamp value) throws SQLException;

    void setDateTime(String name, LocalDateTime value) throws SQLException;

    void setArray(String name, Array value) throws SQLException;
    void setBigDecimal(String name, java.math.BigDecimal value) throws SQLException;

    PreparedStatement getStatement();

    boolean execute() throws SQLException;

    ResultSet executeQuery() throws SQLException;

    int executeUpdate() throws SQLException;

    @Override
    void close() throws SQLException;

    void addBatch() throws SQLException;

    int[] executeBatch() throws SQLException;

    ResultSet getGeneratedKeys() throws SQLException;

    boolean hasParameter(String name);
}

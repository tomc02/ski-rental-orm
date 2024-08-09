package cs.vsb.ski_rental.database.connection;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface SQLCallable<V> {
    V call(Connection connection) throws SQLException;
}

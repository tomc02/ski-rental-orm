package cs.vsb.ski_rental.database.dao;

import cs.vsb.ski_rental.database.connection.ConnectionProvider;
import cs.vsb.ski_rental.database.connection.NamedParameterStatement;
import cs.vsb.ski_rental.database.model.Rental;
import cs.vsb.ski_rental.database.model.Ski;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Collection;
import java.util.LinkedList;

public class RentalDao extends AbstractDao<Rental> {
    public static String SQL_SELECT = "SELECT * FROM Rental";
    public static String SQL_SELECT_ID = "SELECT * FROM rental WHERE rental_id=:id";
    //Instead of using SQL_INSERT use function createRental this is function 3.1 from the documentation
    public static String SQL_INSERT = "";
    //Deleting Rental is not possible
    public static String SQL_DELETE_ID = "";
    public static String SQL_UPDATE = "UPDATE rental SET date_from=:date_from, date_to=:date_to, rental_status=:rental_status, "
            + "date_returned=:date_returned, customer_user_id=:customer_user_id, employee_user_id=:employee_user_id WHERE rental_id=:id";
    public static String SQL_END_RENTAL = "UPDATE rental SET date_returned=SYSDATE, rental_status='Returned' WHERE rental_id=:id";
    public static String SQL_SELECT_RENTAL_SKIS = "SELECT * FROM ski WHERE ski_id IN (SELECT ski_id FROM rental_ski WHERE rental_id=:id)";
    @Override
    protected String getSQLInsert() {
        return SQL_INSERT;
    }

    @Override
    protected String getSQLSelect() {
        return SQL_SELECT;
    }

    @Override
    protected String getSQLSelectById() {
        return SQL_SELECT_ID;
    }

    @Override
    protected String getSQLUpdate() {
        return SQL_UPDATE;
    }

    @Override
    protected String getSQLDeleteById() {
        return SQL_DELETE_ID;
    }

    @Override
    protected void prepareCommand(NamedParameterStatement statement, Rental object) throws SQLException {
        if (statement.hasParameter("id")) {
            statement.setInt("id", object.getRentalId());
        }
        statement.setDateTime("date_from", object.getDateFrom());
        statement.setDateTime("date_to", object.getDateTo());
        statement.setString("rental_status", object.getRentalStatus());
        statement.setDateTime("date_returned", object.getDateReturned());
        statement.setInt("customer_user_id", object.getCustomerId());
        statement.setInt("employee_user_id", object.getEmployeeId());
    }


    protected Collection<Rental> read(ResultSet rs) throws SQLException {
        Collection<Rental> rentals = new LinkedList<>();
        while (rs.next()) {
            Rental rental = Rental.builder()
                    .rentalId(rs.getInt("rental_id"))
                    .dateTo(rs.getTimestamp("date_to").toLocalDateTime())
                    .dateFrom(rs.getTimestamp("date_from").toLocalDateTime())
                    .rentalStatus(rs.getString("rental_status"))
                    .customerId(rs.getInt("customer_user_id"))
                    .employeeId(rs.getInt("employee_user_id"))
                    .dateReturned(rs.getTimestamp("date_returned") != null ? rs.getTimestamp("date_returned").toLocalDateTime() : null)
                    .build();
            rentals.add(rental);
        }

        return rentals;
    }
    @Override
    protected void setKeys(Rental rental, ResultSet generatedKeys) throws SQLException {
    }
    public void createRental(Rental rental, String paymentType) throws SQLException {
        String sql = "{call create_rental(?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = ConnectionProvider.getConnection()) {
            try (CallableStatement cs = conn.prepareCall(sql)) {
                Timestamp dateFrom = Timestamp.valueOf(rental.getDateFrom());
                Timestamp dateTo = Timestamp.valueOf(rental.getDateTo());

                Integer[] skiIds = rental.getRentedSkis().stream().map(Ski::getSkiId).toArray(Integer[]::new);
                oracle.sql.ArrayDescriptor descriptor = oracle.sql.ArrayDescriptor.createDescriptor("RENTAL_SKI_LIST", conn);
                oracle.sql.ARRAY skiIdsArray = new oracle.sql.ARRAY(descriptor, conn, skiIds);

                BigDecimal paymentAmount = rental.getRentedSkis().stream().map(Ski::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);

                cs.setTimestamp(1, dateFrom);
                cs.setTimestamp(2, dateTo);
                cs.setInt(3, rental.getCustomerId());
                cs.setInt(4, rental.getEmployeeId());
                cs.setArray(5, skiIdsArray);
                cs.setString(6, paymentType);
                cs.setBigDecimal(7, paymentAmount);
                cs.execute();
            }
        }
    }
    public void endRental(Rental rental) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection()) {
            try (NamedParameterStatement st = new NamedParameterStatement(conn, SQL_END_RENTAL)) {
                st.setInt("id", rental.getRentalId());
                st.executeUpdate();
            }
        }
    }
    public Collection<Ski> getRentalSkis(Rental rental) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection()) {
            try (NamedParameterStatement st = new NamedParameterStatement(conn, SQL_SELECT_RENTAL_SKIS)) {
                st.setInt("id", rental.getRentalId());
                try (ResultSet rs = st.executeQuery()) {
                    return new SkiDao().read(rs);
                }
            }
        }
    }


}

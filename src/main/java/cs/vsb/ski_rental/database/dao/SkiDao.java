package cs.vsb.ski_rental.database.dao;

import cs.vsb.ski_rental.database.connection.ConnectionProvider;
import cs.vsb.ski_rental.database.connection.NamedParameterStatement;
import cs.vsb.ski_rental.database.model.Ski;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class SkiDao extends AbstractDao<Ski> {
    public static final String SQL_SELECT = "SELECT * FROM ski";
    public static final String SQL_SELECT_BY_ID = "SELECT * FROM ski WHERE ski_id = :id";
    public static final String SQL_INSERT = "INSERT INTO ski (brand, model, length, price, description, skier_experience, category_id) " +
            "VALUES (:brand, :model, :length, :price, :description, :skier_experience, :category_id)";
    public static final String SQL_UPDATE = "UPDATE ski SET brand=:brand, model=:model, length=:length, price=:price, " +
            "description=:description, skier_experience=:skier_experience, category_id=:category_id " +
            "WHERE ski_id=:id";
    public static final String SQL_DELETE = "UPDATE SKI SET DELETED_AT = SYSDATE WHERE ski_id = :id";
    public static final String SQL_RESTORE = "UPDATE SKI SET DELETED_AT = NULL WHERE ski_id = :id";

    @Override
    protected String getSQLSelect() {
        return SQL_SELECT;
    }

    @Override
    protected String getSQLSelectById() {
        return SQL_SELECT_BY_ID;
    }

    @Override
    protected String getSQLInsert() {
        return SQL_INSERT;
    }

    @Override
    protected String getSQLUpdate() {
        return SQL_UPDATE;
    }

    @Override
    protected String getSQLDeleteById() {
        return SQL_DELETE;
    }

    @Override
    protected void prepareCommand(NamedParameterStatement statement, Ski object) throws SQLException {
        if (statement.hasParameter("id")) {
            statement.setInt("id", object.getSkiId());
        }
        statement.setString("brand", object.getBrand());
        statement.setString("model", object.getModel());
        statement.setInt("length", object.getLength());
        statement.setBigDecimal("price", object.getPrice());
        statement.setString("description", object.getDescription());
        statement.setString("skier_experience", object.getSkierExperience());
        statement.setInt("category_id", object.getCategoryId());
    }

    @Override
    protected Collection<Ski> read(ResultSet rs) throws SQLException {
        Collection<Ski> skis = new LinkedList<>();
        while (rs.next()) {
            Ski ski = Ski.builder()
                    .skiId(rs.getInt("ski_id"))
                    .brand(rs.getString("brand"))
                    .model(rs.getString("model"))
                    .length(rs.getInt("length"))
                    .price(rs.getBigDecimal("price"))
                    .description(rs.getString("description"))
                    .skierExperience(rs.getString("skier_experience"))
                    .categoryId(rs.getInt("category_id"))
                    .deletedAt(rs.getTimestamp("deleted_at") != null ? LocalDate.from(rs.getTimestamp("deleted_at").toLocalDateTime()) : null)
                    .build();
            skis.add(ski);
        }

        return skis;
    }

    @Override
    protected void setKeys(Ski ski, ResultSet generatedKeys) throws SQLException {
    }

    // Function 1.4 from documentation
    public List<Ski> getSkiListWithRentalCounts(int id_category, BigDecimal min_price, BigDecimal max_price, int min_length, int max_length) {
        String sql = "SELECT s.ski_id,\n" +
                "       s.brand,\n" +
                "       s.model,\n" +
                "       s.price,\n" +
                "       s.length,\n" +
                "       s.description,\n" +
                "       s.skier_experience,\n" +
                "       c.category_id as category_id,\n" +
                "       c.name                       as category_name,\n" +
                "       (SELECT COUNT(DISTINCT r.rental_id)\n" +
                "        FROM rental_ski rs\n" +
                "                 JOIN rental r ON rs.rental_id = r.rental_id\n" +
                "        WHERE rs.ski_id = s.ski_id) as count_of_all_rentals,\n" +
                "       (SELECT Count(*)\n" +
                "        FROM rental r\n" +
                "                 JOIN rental_ski rs on r.rental_id = rs.rental_id\n" +
                "        WHERE rs.ski_id = s.ski_id\n" +
                "          AND r.rental_status = 'Active')\n" +
                "                                    as count_of_active_rentals\n" +
                "FROM ski s\n" +
                "         JOIN category c ON s.category_id = c.category_id\n" +
                "WHERE s.deleted_at IS NULL";

        if (id_category > 0) {
            sql += " AND s.category_id = ?";
        }
        if (min_price != null) {
            sql += " AND s.price >= ?";
        }
        if (max_price != null) {
            sql += " AND s.price <= ?";
        }
        if (min_length > 0) {
            sql += " AND s.length >= ?";
        }
        if (max_length > 0) {
            sql += " AND s.length <= ?";
        }

        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            if (id_category > 0) {
                stmt.setInt(paramIndex++, id_category);
            }
            if (min_price != null) {
                stmt.setBigDecimal(paramIndex++, min_price);
            }
            if (max_price != null) {
                stmt.setBigDecimal(paramIndex++, max_price);
            }
            if (min_length > 0) {
                stmt.setInt(paramIndex++, min_length);
            }
            if (max_length > 0) {
                stmt.setInt(paramIndex++, max_length);
            }

            ResultSet rs = stmt.executeQuery();

            List<Ski> skis = new LinkedList<>();
            while (rs.next()) {
                Ski ski = Ski.builder()
                        .skiId(rs.getInt("ski_id"))
                        .brand(rs.getString("brand"))
                        .model(rs.getString("model"))
                        .length(rs.getInt("length"))
                        .price(rs.getBigDecimal("price"))
                        .description(rs.getString("description"))
                        .skierExperience(rs.getString("skier_experience"))
                        .categoryId(rs.getInt("category_id"))
                        .countOfAllRentals(rs.getInt("count_of_all_rentals"))
                        .countOfActiveRentals(rs.getInt("count_of_active_rentals"))
                        .build();
                skis.add(ski);
            }
            return skis;
        } catch (SQLException ex) {
            throw new RuntimeException("Error executing SQL query: " + sql, ex);
        }
    }

    // Function 1.6 from documentation
    public void restoreSkis(Ski ski) {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_RESTORE)) {
            stmt.setInt(1, ski.getSkiId());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error executing SQL query: " + SQL_RESTORE, ex);
        }
    }
}


package cs.vsb.ski_rental;

import cs.vsb.ski_rental.database.connection.ConnectionProvider;
import cs.vsb.ski_rental.database.dao.RentalDao;
import cs.vsb.ski_rental.database.dao.SkiDao;
import cs.vsb.ski_rental.database.model.Rental;
import cs.vsb.ski_rental.database.model.Ski;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

@Log4j2(topic = "App")
public class Program {

	private static final SkiDao skiDao = new SkiDao();
	private static final RentalDao rentalDao = new RentalDao();
	public static void main(String[] args) throws SQLException {
		loadProperties();
		Connection connection = testConnection();
		Ski ski = Ski.builder()
				.brand("Atomic")
				.model("Redster")
				.length(170)
				.price(BigDecimal.valueOf(500))
				.description("Atomic Redster X9 S9")
				.skierExperience("Advanced")
				.categoryId(1)
				.build();
		skiDao.insert(ski);

		Ski ski2 = skiDao.select(4);
		Ski ski3 = skiDao.select(5);
		log.info("Loaded skis ski2 from database");
		showSkis(ski2);
		BigDecimal newPrice = ski2.getPrice().add(BigDecimal.valueOf(5));
		ski2.setPrice(newPrice);
		skiDao.update(ski2);
		ski2 = skiDao.select(4);
		log.info("Updated skis ski2 in database");
		showSkis(ski2);

		log.info("Loaded skis ski3 from database");
		showSkis(ski3);
		skiDao.delete(ski3.getSkiId());
		ski3 = skiDao.select(5);
		log.info("Deleted skis ski3 in database");
		showSkis(ski3);
		log.info("Restored skis ski3 in database");
		skiDao.restoreSkis(ski3);
		ski3 = skiDao.select(5);
		showSkis(ski3);

		// Funkce 1.4 Seznam lyží
		// Function 1.4 List of skis
		Collection<Ski> skis = skiDao.getSkiListWithRentalCounts(1,null, BigDecimal.valueOf(700),-1,180);
		for (Ski s : skis) {
			showSkis(s);
		}

		Rental rental = Rental.builder()
				.dateFrom(LocalDateTime.now())
				.dateTo(LocalDateTime.now().plusDays(1))
				.rentalStatus("Coming")
				.customerId(1)
				.employeeId(1)
				.rentedSkis(List.of(ski2, ski3))
				.build();
		// Funkce 3.1 Vytvoření výpůjčky, která je realizována jako uložená procedura
		// Function 3.1 Rental creation, which is realized as a stored procedure
		rentalDao.createRental(rental,"Card");
		Collection<Rental> rentals = rentalDao.select();
		for (Rental r : rentals) {
			showRental(r);
		}

		Rental rental2 = rentalDao.select(1);
		log.info("Loaded rental rental2 from database");
		showRental(rental2);
		LocalDateTime newDateTo = rental2.getDateTo().plusDays(1);
		rental2.setDateTo(newDateTo);
		rentalDao.update(rental2);
		rental2 = rentalDao.select(1);
		log.info("Updated rental rental2 in database");
		showRental(rental2);
		rentalDao.endRental(rental2);
		rental2 = rentalDao.select(1);
		log.info("Ended rental rental2 in database");
		showRental(rental2);

		Collection<Ski> skis1 = rentalDao.getRentalSkis(rental2);
		for (Ski s : skis1) {
			showSkis(s);
		}
	}

	private static void loadProperties() {
		try (InputStream is = Program.class.getResourceAsStream("/application.properties")) {
			System.getProperties().load(is);
		} catch (IOException e) {
			log.error("loadProperties", e);
		}
	}
	private static Connection testConnection() throws SQLException {
		try {
			Connection connection = ConnectionProvider.getConnection();
			log.info("Connection established");
			return connection;
		} catch (SQLException e) {
			log.error("testConnection", e);
			throw e;
		}
	}
	private static void showRental(Rental rental) throws SQLException {
			log.info("Rental id: " + rental.getRentalId());
			log.info("Date from: " + rental.getDateFrom());
			log.info("Date to: " + rental.getDateTo());
			log.info("Rental status: " + rental.getRentalStatus());
			log.info("Date returned: " + rental.getDateReturned());
			log.info("Customer id: " + rental.getCustomerId());
			log.info("Employee id: " + rental.getEmployeeId());
			log.info("*******************************************");
	}
	private static void showSkis(Ski ski) throws SQLException{
			log.info("Ski id: " + ski.getSkiId());
			log.info("Ski brand: " + ski.getBrand());
			log.info("Ski model: " + ski.getModel());
			log.info("Ski length: " + ski.getLength());
			log.info("Ski price: " + ski.getPrice());
			log.info("Ski desc: " + ski.getDescription());
			log.info("Ski category: " + ski.getCategoryId());
			log.info("Ski deleted at: " + ski.getDeletedAt());
			log.info("Ski count of all rentals: " + ski.getCountOfAllRentals());
			log.info("Ski count of active rentals: " + ski.getCountOfActiveRentals());
			log.info("*******************************************");
	}

}

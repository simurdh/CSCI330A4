import java.util.Properties;
import java.util.Scanner;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

class SimurdakAssignment2 {

	static Connection conn = null;

	static ArrayList<Prices> dayPrices;
	static CompanyData co_data;

	public static void main(String[] args) throws Exception {
		// Get connection properties
		String paramsFile = "ConnectionParameters.txt";
		if (args.length >= 1) {
			paramsFile = args[0];
		}
		Properties connectprops = new Properties();
		connectprops.load(new FileInputStream(paramsFile));

		try {
			// Get connection
			Class.forName("com.mysql.jdbc.Driver");
			String dburl = connectprops.getProperty("dburl");
			String username = connectprops.getProperty("user");
			conn = DriverManager.getConnection(dburl, connectprops);
			System.out.printf("Database connection %s %s established.%n", dburl, username);

			// Enter Ticker and optional start end dates, Fetch data for that ticker and
			// dates
			Scanner in = new Scanner(System.in);
			while (true) {
				System.out.print("Enter ticker and date (YYYY.MM.DD): ");
				String[] data = in.nextLine().trim().split("\\s+");

				if (data.length == 1) {
					if (data[0].equals("")) {
						System.out.println("Database connection closed.");
						break;
					}	
					if (showTicker(data[0]) == 0) {
						CalculatePrices();
					}
				} else if (data.length == 3) {
					if (showTickerDays(data[0], data[1], data[2]) == 0) {
						CalculatePrices();
					}
				}
			}

			conn.close();
		} catch (SQLException ex) {
			System.out.printf("SQLException: %s%nSQLState: %s%nVendorError: %s%n", ex.getMessage(), ex.getSQLState(),
					ex.getErrorCode());
		}
	}

	/* When no dates are specified */
	static int showTicker(String ticker) throws SQLException {
		// Prepare query
		PreparedStatement name = conn
				.prepareStatement("select name, Ticker" + "	from company " + "   where Ticker = ?");

		// PV Data Columns: 1) Ticker 2) TransDate 3) OpenPrice 4)HighPrice 5) LowPrice
		// 6) ClosePrice 7) Volume
		ResultSet rs2 = null;
		final String getData = "select Ticker, TransDate, OpenPrice, HighPrice, LowPrice, ClosePrice, Volume, AdjustedClose"
				+ "	from PriceVolume " + "   where Ticker = ? " + "   order by TransDate DESC";
		PreparedStatement PVdata = conn.prepareStatement(getData, ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_UPDATABLE);

		name.setString(1, ticker);
		ResultSet rs = name.executeQuery();

		PVdata.setString(1, ticker);
		rs2 = PVdata.executeQuery();

		if (rs.next()) {
			System.out.printf("%s%n", rs.getString(1));
			co_data = new CompanyData(rs.getString(1));
			dayPrices = new ArrayList<Prices>();
			while (rs2.next()) {
				Prices curDay = new Prices(rs2.getString(2));
				curDay.addDay(rs2.getDouble(3), rs2.getDouble(4), rs2.getDouble(5), rs2.getDouble(6));
				dayPrices.add(curDay);
				co_data.countDay();
			}

		} else {
			System.out.printf("%s not found in database.\n%n", ticker);
			return -1;
		}
		name.close();
		PVdata.close();
		return 0;
	}

	/* When dates are specified */
	static int showTickerDays(String ticker, String start_date, String end_date) throws SQLException {

		// Prepare query
		PreparedStatement name = conn
				.prepareStatement("select name, Ticker" + "	from company " + "   where Ticker = ?");

		PreparedStatement PVdata1 = conn.prepareStatement("select *" + "	from PriceVolume "
				+ "   where Ticker = ? and TransDate between ? and ? " + "   order by TransDate DESC");

		// PV Data Columns: 1) Ticker 2) TransDate 3) OpenPrice 4)HighPrice 5) LowPrice
		// 6) ClosePrice 7) Volume

		name.setString(1, ticker);
		ResultSet rs = name.executeQuery();

		PVdata1.setString(1, ticker);
		PVdata1.setString(1, ticker);
		PVdata1.setString(2, start_date);
		PVdata1.setString(3, end_date);

		ResultSet rs2 = PVdata1.executeQuery();

		if (rs.next()) {
			System.out.printf("%s%n", rs.getString(1));
			co_data = new CompanyData(rs.getString(1));
			dayPrices = new ArrayList<Prices>();
			while (rs2.next()) {
				Prices curDay = new Prices(rs2.getString(2));
				curDay.addDay(rs2.getDouble(3), rs2.getDouble(4), rs2.getDouble(5), rs2.getDouble(6));
				dayPrices.add(curDay);
				co_data.countDay();
			}

		} else {
			System.out.printf("%s not found in database.\n%n", ticker);
			return -1;
			
		}
		name.close();
		PVdata1.close();
		return 0;
	}

	/* Calculate Price data */
	static void CalculatePrices() {
		int daySize = dayPrices.size() - 1;
		String split = null;
		int totalDivisor = 1;
		double movingAverage = 0;
		double cash = 0.0;
		int numStock = 0;
		int numTrans = 0;
		int day = 0;

//		System.out.println("size: " + daySize);

		for (int d = daySize - 1; d > 0; d--) {

//			 System.out.println(dayPrices.get(d).getDate());
			if (d > 0) {
				split = calcSplitDay(dayPrices.get(d).getClose(), dayPrices.get(d - 1).getOpen());
				if (split != null) {
					if (split == "2:1") {
						totalDivisor *= 2.0;
					} else if (split == "3:1") {
						totalDivisor *= 3.0;
					} else if (split == "3:2") {
						totalDivisor *= 1.5;
					}
					co_data.addSplitDay(dayPrices.get(d).getDate() + "\t" + dayPrices.get(d).getClose() + " --> "
							+ dayPrices.get(d - 1).getOpen(), split);
				}
			}

			dayPrices.get(d).updateClose(totalDivisor);
			dayPrices.get(d - 1).updateOpen(totalDivisor);

			co_data.addClose(dayPrices.get(d).getClose());

			if (daySize < 50) { // should be 50
				// do no trading and report a net gain of
				// zero
				System.out.println("daysize less than 51");
			} else {
				movingAverage = co_data.getAvg();

				if (daySize - d == 49) {
//					System.out.println("Day 50: " + dayPrices.get(d).getDate() + " average: " + movingAverage);
//					System.out
//							.println("open:  " + dayPrices.get(d).getOpen() + " close: " + dayPrices.get(d).getClose());
				}

				if (day >= 50) {
					// buy criterion
					if ((dayPrices.get(day).getClose() < movingAverage)
							&& (dayPrices.get(day).getClose() / dayPrices.get(day).getOpen() <= 0.97000001)) {
						numStock += 100;
						cash -= 100 * dayPrices.get(day + 1).getOpen();
						numTrans++;
						cash -= 8.0;
						// sell criterion
					} else if ((numStock >= 100)
							&& (dayPrices.get(day).getOpen() / dayPrices.get(day - 1).getClose() >= 1.00999999)) {
						numStock -= 100;
						cash += 100 * ((dayPrices.get(day).getOpen() + dayPrices.get(day).getClose()) / 2);
						numTrans++;
						cash -= 8.0;
					}
					if (day == (daySize - 1)) { // last day
						if (numStock > 0) {
							cash += dayPrices.get(d).getOpen() * numStock;
						}
					}
				}
			}
			day++;
		}
		co_data.getSplitDays();

		System.out.println("Executing investment strategy");
		System.out.println("Transations executed: " + numTrans++);
		System.out.println("Net cash: " + cash + "\n");

	}

	/* returns a string of the split or null if not */
	public static String calcSplitDay(Double C, Double O) {
		// System.out.println("close = " + C + " open = " + O);
		double trysplit = Math.abs(C / O - 3.0);
		// System.out.println("trySplit: " + trysplit);
		if (Math.abs(C / O - 2.0) < 0.20) {
			// System.out.println("split!\n");
			return "2:1";
		} else if (Math.abs(C / O - 3.0) < 0.30) {
			// System.out.println("split!\n");
			return "3:1";
		} else if (Math.abs(C / O - 1.5) < 0.15) {
			// System.out.println("split!\n");
			return "3:2";
		} else {
			return null;
		}
	}

}
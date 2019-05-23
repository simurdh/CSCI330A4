
/* Hannah Simurdak
05/17/2019
CSCI 330 Assignment 2
*/


public class Prices {
	private String day;
	double openPrice = 0;
	double highPrice = 0;
	double lowPrice = 0;
	double closePrice = 0;


	// Constructor
	public Prices(String day) {
		this.day = day;
	}

	public void addDay(double open, double high, double low, double close) {
		openPrice = open;
		highPrice = high;
		lowPrice = low;
		closePrice = close;
	}
	// get closing price for the day
		public String getDate() {
			return day;
		}

	// get closing price for the day
	public double getClose() {
		return closePrice;
	}

	// get opening price for the day
	public double getOpen() {
		return openPrice;
	}
	
	public void updateClose(double div) {
		closePrice = closePrice/div;
	}
	
	public void updateOpen(double div) {
		openPrice = openPrice/div;
	}

}










/* Hannah Simurdak
05/17/2019
CSCI 330 Assignment 2
*/

import java.util.*;

public class CompanyData {

  //Instance Variables
  private String ticker;
  private int days = 0;
  private double sum = 0;
  private LinkedHashMap<String, String> splitD = new LinkedHashMap<>();
  private LinkedList<Double> movingAvg = new LinkedList<>();

  //Constructor
  public CompanyData(String ticker) {
    this.ticker = ticker;
  }

  public String getCompany() {
    return ticker;
  }
  
  //Add days to a 50 day window
  public void addClose(Double close) {
	  if (movingAvg.size() < 50) {
		  movingAvg.add(close);
	  } else {
		  sum = sum - movingAvg.pollFirst();  
		  movingAvg.addLast(close);
	  }
	  sum += close;
  }
  
  //calculate average
  public double getAvg() {
//	  System.out.println("size: " + Double.valueOf(movingAvg.size()));
	  return sum/Double.valueOf(movingAvg.size());
  }
  //add day count
  public int getDays() {
	    return days;
  }
  
  public void countDay() {
	  days++;
  }


  //add split days
  public void addSplitDay(String date, String info) {
    splitD.put(date, info);
  }

  public void getSplitDays() {

    int count = 0;
    ArrayList<String> reverseList = new ArrayList<>();
    for (Map.Entry<String, String> day : splitD.entrySet()) {
    	reverseList.add(day.getValue() + " split on " + day.getKey());
//      System.out.println(day.getValue() + " split on " + day.getKey());
      count++;
    }
    
    for (int i = reverseList.size() - 1; i >= 0; i--) {
    	System.out.println(reverseList.get(i));
    }
    
    System.out.println(count + " splits in " + days + " trading days\n");
  }
}

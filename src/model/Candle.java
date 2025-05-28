package model;

public class Candle {
	public String date;
	public double open, high, low, close, volume;

	public Candle(String date, double open, double high, double low, double close, double volume) {
		this.date = date;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.volume = volume;
	}
}

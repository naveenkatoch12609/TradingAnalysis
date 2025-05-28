package strategy;

import java.util.List;

import model.Candle;
import model.Summary;
import model.Trade;

public interface Strategy {
	
	List<Trade> generateSignals(List<Candle> candles, Summary summary);
}

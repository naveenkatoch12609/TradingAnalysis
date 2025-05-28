package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Candle;

public class HistoricalDataLoader {

	public static Map<String, List<Candle>> loadAllStocks(String folderPath) {
		Map<String, List<Candle>> stockData = new HashMap<>();

		File folder = new File(folderPath);
		File[] files = folder.listFiles((dir, name) -> name.endsWith(".csv"));

		if (files == null)
			return stockData;

		for (File file : files) {
			String stockName = file.getName().replace(".csv", "");
			List<Candle> candles = loadFromFile(file.getAbsolutePath());
			stockData.put(stockName, candles);
		}

		return stockData;
	}

	public static List<Candle> loadFromFile(String filePath) {
		List<Candle> candles = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			reader.readLine(); // Skip header
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(",");
				candles.add(new Candle(parts[0], Double.parseDouble(parts[1]), Double.parseDouble(parts[2]),
						Double.parseDouble(parts[3]), Double.parseDouble(parts[4]), Long.parseLong(parts[5])));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return candles;
	}
}

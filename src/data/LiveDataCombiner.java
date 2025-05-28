package data;

import model.Candle;

import java.util.*;

public class LiveDataCombiner {

    public static Map<String, List<Candle>> combineWithLive(Map<String, List<Candle>> bhavcopyData, Map<String, Candle> liveData) {
        Map<String, List<Candle>> combined = new HashMap<>();

        for (Map.Entry<String, List<Candle>> entry : bhavcopyData.entrySet()) {
            String symbol = entry.getKey();
            List<Candle> history = new ArrayList<>(entry.getValue());

            if (liveData.containsKey(symbol)) {
                Candle last = history.isEmpty() ? null : history.get(history.size() - 1);
                Candle live = liveData.get(symbol);

                if (last == null || !last.date.equals(live.date)) {
                    history.add(live);  // append only if newer
                }
            }

            combined.put(symbol, history);
        }

        return combined;
    }
}

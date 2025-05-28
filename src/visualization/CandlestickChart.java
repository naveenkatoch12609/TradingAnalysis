package visualization;

import model.Candle;
import model.Summary;
import model.Trade;

import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.xy.DefaultHighLowDataset;

public class CandlestickChart extends JPanel {

	private static final long serialVersionUID = 1L;

	public CandlestickChart(String title, List<Candle> candles, List<Trade> trades, Summary summary)
			throws ParseException {

		setLayout(new BorderLayout());

		JFreeChart chart = createChart(candles, trades);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(1000, 600));

		// Summary panel
		JPanel summaryPanel = new JPanel(new GridLayout(1, 6, 15, 0));
		summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JLabel lblOpen = new JLabel("Opening Balance: $" + String.format("%.2f", summary.getOpeningBalance()));
		JLabel lblClose = new JLabel("Closing Balance: $" + String.format("%.2f", summary.getClosingBalance()));
		JLabel lblLast = new JLabel("Last Position: " + (summary.isHoldingPosition() ? "BUY/HOLD" : "No Position"));

		JLabel lblPL = new JLabel("Net P&L: $" + String.format("%.2f", summary.getNetPL()));
		lblPL.setForeground(summary.getNetPL() >= 0 ? Color.GREEN.darker() : Color.RED);

		String tooltipText = "<html>" + "Total Trades Executed: " + summary.getTradeCount() + "<br>"
				+ "<span style='color:green;'>Profitable Trades: " + summary.getWinCount() + " ($"
				+ String.format("%.2f", summary.getTotalProfit()) + ")</span><br>"
				+ "<span style='color:red;'>Losing Trades: " + summary.getLossCount() + " ($"
				+ String.format("%.2f", summary.getTotalLoss()) + ")</span>" + "</html>";

		lblPL.setToolTipText(tooltipText);

		JLabel lblStrategy = new JLabel("Strategy: " + summary.getStrategyName());
		lblStrategy.setToolTipText(summary.getStrategyDescription());

		summaryPanel.add(lblOpen);
		summaryPanel.add(lblClose);
		summaryPanel.add(lblLast);
		summaryPanel.add(lblPL);
		summaryPanel.add(lblStrategy);

		// Interactions
		chartPanel.setMouseWheelEnabled(true);
		chartPanel.setMouseZoomable(true);
		chartPanel.setDomainZoomable(true);
		chartPanel.setRangeZoomable(true);
		chartPanel.setHorizontalAxisTrace(true);
		chartPanel.setVerticalAxisTrace(true);
		chartPanel.setDisplayToolTips(true);

		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		add(summaryPanel, BorderLayout.NORTH);
		add(chartPanel, BorderLayout.CENTER);
	}

	private JFreeChart createChart(List<Candle> candles, List<Trade> trades) throws ParseException {
		DefaultHighLowDataset dataset = createDataset(candles);

		JFreeChart chart = ChartFactory.createCandlestickChart("Candlestick Chart", "Date", "Price", dataset, false);
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
		plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
		plot.setAxisOffset(new RectangleInsets(5, 5, 5, 5));

		DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
		domainAxis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));
		domainAxis.setLabel("Date");

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setAutoRangeIncludesZero(false);
		rangeAxis.setLabel("Price");

		CandlestickRenderer renderer = new CandlestickRenderer();
		renderer.setSeriesPaint(0, Color.BLACK);
		renderer.setUpPaint(Color.GREEN);
		renderer.setDownPaint(Color.RED);
		renderer.setDrawVolume(false);
		renderer.setAutoWidthMethod(CandlestickRenderer.WIDTHMETHOD_SMALLEST);
		plot.setRenderer(renderer);

		// Add trade markers
		for (Trade t : trades) {
			long time = new SimpleDateFormat("yyyy-MM-dd").parse(t.date).getTime();
			XYTextAnnotation annotation = new XYTextAnnotation(t.type.equals("BUY") ? "B" : "S", time, t.price);
			annotation.setFont(new Font("Arial", Font.BOLD, 12));
			annotation.setPaint(t.type.equals("BUY") ? Color.BLUE : Color.MAGENTA);
			annotation.setTextAnchor(t.type.equals("BUY") ? TextAnchor.BOTTOM_LEFT : TextAnchor.TOP_RIGHT);
			plot.addAnnotation(annotation);
		}

		return chart;
	}

	private DefaultHighLowDataset createDataset(List<Candle> candles) {
		int size = candles.size();
		Date[] dates = new Date[size];
		double[] highs = new double[size];
		double[] lows = new double[size];
		double[] opens = new double[size];
		double[] closes = new double[size];
		double[] volumes = new double[size];

		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			for (int i = 0; i < size; i++) {
				Candle c = candles.get(i);
				dates[i] = sdf.parse(c.date);
				highs[i] = c.high;
				lows[i] = c.low;
				opens[i] = c.open;
				closes[i] = c.close;
				volumes[i] = c.volume;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new DefaultHighLowDataset("Price", dates, highs, lows, opens, closes, volumes);
	}
}

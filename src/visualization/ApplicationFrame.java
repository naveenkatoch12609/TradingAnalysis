package visualization;

import model.Candle;
import model.Summary;
import model.Trade;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class ApplicationFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private JPanel chartPanelContainer;
	private JScrollPane scrollPane;
	private JTable table;
	private JSplitPane splitPane;
	private JComboBox<String> stockSelector;

	private final Map<String, List<Candle>> candleMap;
	private final Map<String, List<Trade>> tradeMap;
	private final Map<String, List<Trade>> signalMap;
	private final Map<String, Summary> summaryMap;

	public ApplicationFrame(String title,
							Map<String, List<Candle>> candleMap,
							Map<String, List<Trade>> tradeMap,
							Map<String, List<Trade>> signalMap,
							Map<String, Summary> summaryMap) throws ParseException {

		this.candleMap = candleMap;
		this.tradeMap = tradeMap;
		this.signalMap = signalMap;
		this.summaryMap = summaryMap;

		setTitle("Trading Visualization");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		// Dropdown Selector
		stockSelector = new JComboBox<>(candleMap.keySet().toArray(new String[0]));
		stockSelector.addActionListener(e -> {
			String selected = (String) stockSelector.getSelectedItem();
			if (selected != null) {
				try {
					updateView(selected);
				} catch (ParseException ex) {
					ex.printStackTrace();
				}
			}
		});
		add(stockSelector, BorderLayout.NORTH);

		// Chart and table containers
		chartPanelContainer = new JPanel(new BorderLayout());
		table = new JTable();
		scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(1000, 200));

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, chartPanelContainer, scrollPane);
		splitPane.setDividerLocation(600);
		splitPane.setResizeWeight(0.8);
		add(splitPane, BorderLayout.CENTER);

		// Load default (first) stock
		if (!candleMap.isEmpty()) {
			String firstKey = candleMap.keySet().iterator().next();
			updateView(firstKey);
		}

		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void updateView(String stock) throws ParseException {
		List<Candle> candles = candleMap.get(stock);
		List<Trade> trades = tradeMap.get(stock);
		List<Trade> signals = signalMap.get(stock);
		Summary summary = summaryMap.get(stock);

		// Update Chart
		CandlestickChart chartPanel = new CandlestickChart(stock, candles, trades, summary);
		chartPanelContainer.removeAll();
		chartPanelContainer.add(chartPanel, BorderLayout.CENTER);
		chartPanelContainer.revalidate();
		chartPanelContainer.repaint();

		// Update Table
		updateTable(trades, signals);
	}

	private void updateTable(List<Trade> trades, List<Trade> signals) {
		String[] columns = { "Type", "Date", "Price", "P/L", "Source" };
		DefaultTableModel model = new DefaultTableModel(columns, 0);

		for (Trade t : trades) {
			model.addRow(new Object[] { t.type, t.date, t.price,
					t.type.equals("BUY") ? "" : String.format("%.2f", t.profitLoss), "Trade" });
		}
		for (Trade s : signals) {
			model.addRow(new Object[] { s.type, s.date, s.price, "", "Signal" });
		}

		table.setModel(model);
		table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;

			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				String source = table.getValueAt(row, 4).toString();
				String plStr = table.getValueAt(row, 3).toString();

				if (source.equals("Trade") && !plStr.isEmpty()) {
					try {
						double pl = Double.parseDouble(plStr);
						if (pl > 0) {
							c.setBackground(new Color(200, 255, 200)); // light green
						} else if (pl < 0) {
							c.setBackground(new Color(255, 200, 200)); // light red
						} else {
							c.setBackground(Color.WHITE);
						}
					} catch (NumberFormatException e) {
						c.setBackground(Color.WHITE);
					}
				} else if (source.equals("Signal")) {
					c.setBackground(new Color(255, 255, 200)); // light yellow
				} else {
					c.setBackground(Color.WHITE);
				}
				return c;
			}
		});

		table.setFillsViewportHeight(true);
		table.setFont(new Font("SansSerif", Font.PLAIN, 12));
		table.setRowHeight(20);
	}
}

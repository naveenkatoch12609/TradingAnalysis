# TradingAnalysis
 Java-based Stock Data Fetcher project that uses API for daily OHLC data and merges it with NSE Bhavcopy data.


📈 Stock Data Fetcher
This Java project fetches daily OHLC stock data from Yahoo Finance for Indian equities (.NS symbols) and merges it with historical NSE Bhavcopy CSV data. It is designed for use in trading analysis, backtesting, and research applications.

🔧 Features
Fetch daily OHLC (Open, High, Low, Close) + volume data from Yahoo Finance.

Supports multiple stock symbols.

Automatically handles cookies and sessions via Jsoup.

Skips invalid or delisted symbols gracefully.

Merges Yahoo data with NSE Bhavcopy historical data (if needed).

Designed for integration in Java trading systems or analysis tools.

📚 Requirements
Java 8 or higher

Jsoup library

Internet connection (for live data)

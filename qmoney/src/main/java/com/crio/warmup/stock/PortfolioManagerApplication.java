package com.crio.warmup.stock;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.dto.TotalReturnsDto;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class PortfolioManagerApplication {
  // private static final String token =
  // "1f104d6c699b5f17af4377cdff36e8927c11413f";
  // private static final String token =
  // "15157ddc88fa5734f1f5569337963e392421cd9b";
  private static final String token = "5387030ac72d2b5f361acbde43f681f619418bd7";
  private static RestTemplate restTemplate = new RestTemplate();
  private static StockQuotesService stockQuotesService;

  // TODO: CRIO_TASK_MODULE_JSON_PARSING
  // Task:
  // - Read the json file provided in the argument[0], The file is available in
  // the classpath.
  // - Go through all of the trades in the given file,
  // - Prepare the list of all symbols a portfolio has.
  // - if "trades.json" has trades like
  // [{ "symbol": "MSFT"}, { "symbol": "AAPL"}, { "symbol": "GOOGL"}]
  // Then you should return ["MSFT", "AAPL", "GOOGL"]
  // Hints:
  // 1. Go through two functions provided - #resolveFileFromResources() and
  // #getObjectMapper
  // Check if they are of any help to you.
  // 2. Return the list of all symbols in the same order as provided in json.

  // Note:
  // 1. There can be few unused imports, you will need to fix them to make the
  // build pass.
  // 2. You can use "./gradlew build" to check if your code builds successfully.

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {

    File inputFile = resolveFileFromResources(args[0]);
    ObjectMapper objectMapper = getObjectMapper();
    PortfolioTrade[] trades = objectMapper.readValue(inputFile, PortfolioTrade[].class);
    List<String> symbols = new ArrayList<>();
    for (PortfolioTrade trade : trades) {
      symbols.add(trade.getSymbol());
    }
    return symbols;
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  // Now that you have the list of PortfolioTrade and their data, calculate
  // annualized returns
  // for the stocks provided in the Json.
  // Use the function you just wrote #calculateAnnualizedReturns.
  // Return the list of AnnualizedReturns sorted by annualizedReturns in
  // descending order.

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.

  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  // and deserialize the results in List<Candle>

  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(Thread.currentThread().getContextClassLoader().getResource(filename).toURI())
        .toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  // TODO: CRIO_TASK_MODULE_JSON_PARSING
  // Follow the instructions provided in the task documentation and fill up the
  // correct values for
  // the variables provided. First value is provided for your reference.
  // A. Put a breakpoint on the first line inside mainReadFile() which says
  // return Collections.emptyList();
  // B. Then Debug the test #mainReadFile provided in
  // PortfoliomanagerApplicationTest.java
  // following the instructions to run the test.
  // Once you are able to run the test, perform following tasks and record the
  // output as a
  // String in the function below.
  // Use this link to see how to evaluate expressions -
  // https://code.visualstudio.com/docs/editor/debugging#_data-inspection
  // 1. evaluate the value of "args[0]" and set the value
  // to the variable named valueOfArgument0 (This is implemented for your
  // reference.)
  // 2. In the same window, evaluate the value of expression below and set it
  // to resultOfResolveFilePathArgs0
  // expression ==> resolveFileFromResources(args[0])
  // 3. In the same window, evaluate the value of expression below and set it
  // to toStringOfObjectMapper.
  // You might see some garbage numbers in the output. Dont worry, its expected.
  // expression ==> getObjectMapper().toString()
  // 4. Now Go to the debug window and open stack trace. Put the name of the
  // function you see at
  // second place from top to variable functionNameFromTestFileInStackTrace
  // 5. In the same window, you will see the line number of the function in the
  // stack trace window.
  // assign the same to lineNumberFromTestFileInStackTrace
  // Once you are done with above, just run the corresponding test and
  // make sure its working as expected. use below command to do the same.
  // ./gradlew test --tests PortfolioManagerApplicationTest.testDebugValues

  public static List<String> debugOutputs() {

    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 = "trades.json";
    String toStringOfObjectMapper = "ObjectMapper";
    String functionNameFromTestFileInStackTrace = "mainReadFile";
    String lineNumberFromTestFileInStackTrace = "";

    return Arrays.asList(
        new String[] { valueOfArgument0, resultOfResolveFilePathArgs0, toStringOfObjectMapper,
            functionNameFromTestFileInStackTrace, lineNumberFromTestFileInStackTrace });
  }

  // Note:
  // Remember to confirm that you are getting same results for annualized returns
  // as in Module 3.
  // and deserialize the results in List<Candle>
  public static double getTiingoClosingPrice(String url) {
    TiingoCandle[] tiingoCandles = restTemplate.getForObject(url, TiingoCandle[].class);
    return tiingoCandles[tiingoCandles.length - 1].getClose();
  }

  // sort all totalReturnsDtos according to Closing price
  public static Comparator<TotalReturnsDto> closingPriceComparator() {
    Comparator<TotalReturnsDto> comparator = new Comparator<>() {
      public int compare(TotalReturnsDto trd1, TotalReturnsDto trd2) {
        return Double.compare(trd1.getClosingPrice(), trd2.getClosingPrice());
      }
    };
    return comparator;
  }

  // TODO:
  // Ensure all tests are passing using below command
  // ./gradlew test --tests ModuleThreeRefactorTest
  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    return candles.get(0).getOpen();
  }

  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    return candles.get(candles.size() - 1).getClose();
  }

  public static double calculateYearsBetweenDates(LocalDate startDate, LocalDate endDate) {
    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("startDate must be before endDate");
    }

    double years = startDate.until(endDate, ChronoUnit.DAYS) / 365.24;
    return years;
  }

  public static TiingoCandle[] fetchTiingoCandles(PortfolioTrade trade, LocalDate endDate,
      String token) {
    String url = prepareUrl(trade, endDate, token);
    return restTemplate.getForObject(url, TiingoCandle[].class);
  }

  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    return Arrays.asList(fetchTiingoCandles(trade, endDate, token));
  }

  // totalReturn of a portfolio
  private static double holdingPeriodReturn(double buyPrice, double sellPrice) {
    return (sellPrice - buyPrice) / buyPrice;
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {

    String filename = args[0];
    LocalDate endDate = LocalDate.parse(args[1]);

    List<AnnualizedReturn> annualizedReturns = new ArrayList<>();
    List<PortfolioTrade> portfolioTrades = readTradesFromJson(filename);

    for (PortfolioTrade trade : portfolioTrades) {
      List<Candle> candles = fetchCandles(trade, endDate, token);
      annualizedReturns.add(calculateAnnualizedReturns(endDate, trade,
          getOpeningPriceOnStartDate(candles), getClosingPriceOnEndDate(candles)));
    }

    Comparator<AnnualizedReturn> comparator = new Comparator<AnnualizedReturn>() {
      public int compare(AnnualizedReturn ar1, AnnualizedReturn ar2) {
        return Double.compare(ar2.getAnnualizedReturn(), ar1.getAnnualizedReturn());
      }
    };

    Collections.sort(annualizedReturns, comparator);
    return annualizedReturns;

  }

  public static double roundUptoThreeDecimals(double arg) {
    DecimalFormat df = new DecimalFormat("#.###");

    // Format the number to three decimal places
    String roundedNumber = df.format(arg);

    // Convert the formatted string back to a double (if needed)
    double roundedValue = Double.parseDouble(roundedNumber);
    return roundedValue;
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  // Return the populated list of AnnualizedReturn for all stocks.
  // Annualized returns should be calculated in two steps:
  // 1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  // 1.1 Store the same as totalReturns
  // 2. Calculate extrapolated annualized returns by scaling the same in years
  // span.
  // The formula is:
  // annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  // 2.1 Store the same as annualized_returns
  // Test the same using below specified command. The build should be successful.
  // ./gradlew test --tests
  // PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate, PortfolioTrade trade,
      Double buyPrice, Double sellPrice) {
    double years = calculateYearsBetweenDates(trade.getPurchaseDate(), endDate);
    double totalReturn = holdingPeriodReturn(buyPrice, sellPrice);
    double annualizedReturn = Math.pow((1 + totalReturn), 1 / years) - 1;
    return new AnnualizedReturn(trade.getSymbol(), annualizedReturn, totalReturn);
  }

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    // this was to read stocks data from Tiingo API using end_date and token
    LocalDate endDate = LocalDate.parse(args[1]);
    String filename = args[0];
    List<PortfolioTrade> trades = readTradesFromJson(filename);
    List<TotalReturnsDto> totalReturnsDtos = new ArrayList<>();
    List<String> symbols = new ArrayList<>();

    String url;

    for (PortfolioTrade trade : trades) {
      url = prepareUrl(trade, endDate, token);
      double closingPrice = getTiingoClosingPrice(url);
      totalReturnsDtos.add(new TotalReturnsDto(trade.getSymbol(), closingPrice));
    }
    Collections.sort(totalReturnsDtos, closingPriceComparator());

    for (TotalReturnsDto trd : totalReturnsDtos) {
      symbols.add(trd.getSymbol());
    }
    return symbols;
  }

  // TODO:
  // After refactor, make sure that the tests pass by using these two commands
  // ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  // ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
  public static List<PortfolioTrade> readTradesFromJson(String filename)
      throws IOException, URISyntaxException {
    File file = resolveFileFromResources(filename);
    ObjectMapper objectMapper = getObjectMapper();
    PortfolioTrade[] portfolioTrades = objectMapper.readValue(file, PortfolioTrade[].class);
    return Arrays.asList(portfolioTrades);

  }

  // TODO:
  // Build the Url using given parameters and use this function in your code to
  // cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
    String url = "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices";
    String QueryVariables = "?startDate=" + trade.getPurchaseDate().toString() + "&endDate="
        + endDate + "&token=" + token;
    return url + QueryVariables;
  }

  // TODO: CRIO_TASK_MODULE_REFACTOR
  // Once you are done with the implementation inside PortfolioManagerImpl and
  // PortfolioManagerFactory, create PortfolioManager using
  // PortfolioManagerFactory.
  // Refer to the code from previous modules to get the List<PortfolioTrades> and
  // endDate, and
  // call the newly implemented method in PortfolioManager to calculate the
  // annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns
  // as in Module 3.

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {

    String filename = args[0];
    LocalDate endDate = LocalDate.parse(args[1]);
    String contents = readFileAsString(filename);
    ObjectMapper objectMapper = getObjectMapper();
    List<PortfolioTrade> trades = Arrays.asList(objectMapper.readValue(contents, PortfolioTrade[].class));
    PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(restTemplate);
    List<AnnualizedReturn> refactoredReturns = portfolioManager.calculateAnnualizedReturn(trades, endDate);

    return refactoredReturns;
  }

  private static String readFileAsString(String filename) throws IOException, URISyntaxException {
    File file = resolveFileFromResources(filename);
    ObjectMapper objectMapper = getObjectMapper();
    String pTrades = objectMapper.readValue(file, String.class);
    // System.out.println(pTrades);
    return pTrades;
  }

  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    printJsonObject(mainReadFile(args));
    printJsonObject(mainReadQuotes(args));
    printJsonObject(mainCalculateSingleReturn(args));
    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }

  public static String getToken() {
    return token;
  }
}

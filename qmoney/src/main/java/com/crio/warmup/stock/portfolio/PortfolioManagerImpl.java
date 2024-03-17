
package com.crio.warmup.stock.portfolio;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.web.client.RestTemplate;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;

public class PortfolioManagerImpl implements PortfolioManager, StockQuotesService {

  // private static final String token =
  // "1f104d6c699b5f17af4377cdff36e8927c11413f";
  // private static final String token =
  // "15157ddc88fa5734f1f5569337963e392421cd9b";
  private static final String token = "5387030ac72d2b5f361acbde43f681f619418bd7";

  private RestTemplate restTemplate;
  private StockQuotesService stockQuotesService;

  // Caution: Do not delete or modify the constructor, or else your build will
  // break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  protected PortfolioManagerImpl(StockQuotesService stockQuotesService) {
    this.stockQuotesService = stockQuotesService;
  }

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

  private static double holdingPeriodReturn(double buyPrice, double sellPrice) {
    return (sellPrice - buyPrice) / buyPrice;
  }

  // TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from
  // main anymore.
  // Copy your code from Module#3
  // PortfolioManagerApplication#calculateAnnualizedReturn
  // into #calculateAnnualizedReturn function here and ensure it follows the
  // method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required
  // further as our
  // clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command
  // below:
  // ./gradlew test --tests PortfolioManagerTest

  // CHECKSTYLE:OFF
  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate) throws JsonProcessingException, StockQuoteServiceException {
    List<AnnualizedReturn> annualizedReturns = new ArrayList<>();

    for (PortfolioTrade trade : portfolioTrades) {
      List<Candle> candle = getStockQuote(trade.getSymbol(), trade.getPurchaseDate(), endDate);
      double sellPrice = getClosingPriceOnEndDate(candle);
      double buyPrice = getOpeningPriceOnStartDate(candle);

      double years = calculateYearsBetweenDates(trade.getPurchaseDate(), endDate);
      double totalReturn = holdingPeriodReturn(buyPrice, sellPrice);
      double annualizedReturn = Math.pow((1 + totalReturn), 1 / years) - 1;
      annualizedReturns.add(new AnnualizedReturn(trade.getSymbol(), annualizedReturn, totalReturn));

    }

    Comparator<AnnualizedReturn> comparator = getComparator();
    Collections.sort(annualizedReturns, comparator);

    return annualizedReturns;

  }

  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  // CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  // Extract the logic to call Tiingo third-party APIs to a separate function.
  // Remember to fill out the buildUri function and use that.

  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException, StockQuoteServiceException {

    List<Candle> res = stockQuotesService.getStockQuote(symbol, from, to);
    return res;
    // String url = buildUri(symbol, from, to);
    // List<Candle> candles = Arrays.asList(restTemplate.getForObject(url,
    // TiingoCandle[].class));
    // return candles;
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String uriTemplate = "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?" + "startDate="
        + startDate.toString() + "&endDate=" + endDate.toString() + "&token=" + token;
    return uriTemplate;
  }

  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturnParallel(
      List<PortfolioTrade> portfolioTrades, LocalDate endDate, int numThreads)
      throws InterruptedException, StockQuoteServiceException {

    List<AnnualizedReturn> annualizedReturns = new ArrayList<AnnualizedReturn>();
    List<Future<AnnualizedReturn>> futureReturnsList = new ArrayList<Future<AnnualizedReturn>>();

    final ExecutorService pool = Executors.newFixedThreadPool(numThreads);

    for (int i = 0; i < portfolioTrades.size(); i++) {
      PortfolioTrade trade = portfolioTrades.get(i);
      Callable<AnnualizedReturn> callableTask = () -> {
        return getAnnualizedReturn(trade, endDate);
        // return getAnnualizedAndTotalReturns(trade, endDate);
      };

      Future<AnnualizedReturn> futureReturns = pool.submit(callableTask);
      futureReturnsList.add(futureReturns);
    }

    for (int i = 0; i < portfolioTrades.size(); i++) {
      Future<AnnualizedReturn> futureReturns = futureReturnsList.get(i);
      try {
        AnnualizedReturn returns = futureReturns.get();
        annualizedReturns.add(returns);
      } catch (ExecutionException e) {
        throw new StockQuoteServiceException("Error when calling the API", e);

      }
    }
    Comparator<AnnualizedReturn> comparator = new Comparator<AnnualizedReturn>() {
      public int compare(AnnualizedReturn a1, AnnualizedReturn a2) {
        return Double.compare(a2.getAnnualizedReturn(), a1.getAnnualizedReturn());
      }
    };
    Collections.sort(annualizedReturns, comparator);
    return annualizedReturns;

  }

  public AnnualizedReturn getAnnualizedReturn(PortfolioTrade portfolioTrade, LocalDate endDate)
      throws StockQuoteServiceException {
    LocalDate startDate = portfolioTrade.getPurchaseDate();
    String symbol = portfolioTrade.getSymbol();

    Double buyPrice = 0.0, sellPrice = 0.0;

    try {
      List<Candle> candles = getStockQuote(symbol, startDate, endDate);

      Comparator<Candle> comparator = new Comparator<Candle>() {
        public int compare(Candle c1, Candle c2) {
          if (c1.getDate().isBefore(c2.getDate())) {
            return 1;
          }
          return 0;
        }
      };

      Collections.sort(candles, comparator);
      Candle stockStartDate = candles.get(0);
      Candle stocksLatest = candles.get(candles.size() - 1);

      buyPrice = stockStartDate.getOpen();
      sellPrice = stocksLatest.getClose();
      endDate = stocksLatest.getDate();

    } catch (JsonProcessingException e) {
      throw new StockQuoteServiceException(
          "Error in getting Json Processing in PortfolioManagerImpl");

    }

    double years = calculateYearsBetweenDates(startDate, endDate);
    double totalReturn = holdingPeriodReturn(buyPrice, sellPrice);
    double annualizedReturn = Math.pow((1 + totalReturn), 1 / years) - 1;

    return new AnnualizedReturn(symbol, annualizedReturn, totalReturn);

  }

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  // Modify the function #getStockQuote and start delegating to calls to
  // stockQuoteService provided via newly added constructor of the class.
  // You also have a liberty to completely get rid of that function itself,
  // however, make sure
  // that you do not delete the #getStockQuote function.

}


package com.crio.warmup.stock.quotes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.web.client.RestTemplate;

import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class TiingoService implements StockQuotesService {

  private RestTemplate restTemplate;
  private static final String token = "1f104d6c699b5f17af4377cdff36e8927c11413f";
  // private static final String token =
  // "15157ddc88fa5734f1f5569337963e392421cd9b";

  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  // Implement getStockQuote method below that was also declared in the interface.

  // Note:
  // 1. You can move the code from PortfolioManagerImpl#getStockQuote inside newly
  // created method.
  // 2. Run the tests using command below and make sure it passes.
  // ./gradlew test --tests TiingoServiceTest

  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException, StockQuoteServiceException, RuntimeException {
    if (from.isAfter(to)) {
      throw new RuntimeException("Start date should be before end date");
    }

    String url = buildUri(symbol, from, to);
    List<Candle> candles = new ArrayList<>();
    try {
      String response = restTemplate.getForObject(url, String.class);

      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JavaTimeModule());

      candles = Arrays.asList(objectMapper.readValue(response, TiingoCandle[].class));

    } catch (NullPointerException e) {
      throw new StockQuoteServiceException("Invalid response from Tiingo Service", e.getCause());

    }
    return candles;

  }

  // CHECKSTYLE:OFF

  // Write a method to create appropriate url to call the Tiingo API.
  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String uriTemplate = "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?" + "startDate="
        + startDate.toString() + "&endDate=" + endDate.toString() + "&token=" + token;
    return uriTemplate;
  }

  // TODO: CRIO_TASK_MODULE_EXCEPTIONS
  // 1. Update the method signature to match the signature change in the
  // interface.
  // Start throwing new StockQuoteServiceException when you get some invalid
  // response from
  // Tiingo, or if Tiingo returns empty results for whatever reason, or you
  // encounter
  // a runtime exception during Json parsing.
  // 2. Make sure that the exception propagates all the way from
  // PortfolioManager#calculateAnnualisedReturns so that the external user's of
  // our API
  // are able to explicitly handle this exception upfront.

  // CHECKSTYLE:OFF

}

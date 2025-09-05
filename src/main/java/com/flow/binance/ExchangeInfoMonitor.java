package com.flow.binance;

import com.flow.exception.ExceptionUtils;
import com.flow.tool.Constants;
import com.flow.tool.JsonObjectTool;
import com.flow.tool.HttpUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * /api/v3/exchangeInfo 缓存获取
 */
@Slf4j
@Component
public class ExchangeInfoMonitor {


    private ReentrantLock monitorLock = new ReentrantLock();

    private ReentrantLock futureMonitorLock = new ReentrantLock();

    @Async(value = "exchangeThreadPoolTaskExecutor")
    @Scheduled(fixedDelay = 300_000, initialDelay = 1_00)
    public void monitor() {
        boolean lock = monitorLock.tryLock();
        if (!lock) {
            log.info("monitor get reentrantLock failed");
            return;
        }
        try {
            HttpResponse httpResponse = HttpUtils.doGet("https://api.binance.com", "/api/v3/exchangeInfo", Map.of(), Map.of());
            String dataJsonString = EntityUtils.toString(httpResponse.getEntity());

            JsonObject jsonObject = Constants.GSON.fromJson(dataJsonString, JsonObject.class);
            JsonArray symbols = JsonObjectTool.getAsJsonArray(jsonObject, "symbols");
            for (JsonElement element : symbols) {
                JsonObject symbol = (JsonObject)element;
                BinanceExchangeInfoCache.ExchangeInfo exchangeInfo = new BinanceExchangeInfoCache.ExchangeInfo();
                String symbolString = JsonObjectTool.getAsString(symbol, "symbol");
                exchangeInfo.setSymbol(symbolString);
                exchangeInfo.setBaseAsset(JsonObjectTool.getAsString(symbol, "baseAsset"));
                exchangeInfo.setBaseAssetPrecision(JsonObjectTool.getAsInt(symbol, "baseAssetPrecision"));
                exchangeInfo.setQuoteAsset(JsonObjectTool.getAsString(symbol, "quoteAsset"));
                exchangeInfo.setQuoteAssetPrecision(JsonObjectTool.getAsInt(symbol, "quoteAssetPrecision"));
                exchangeInfo.setQuoteOrderQtyMarketAllowed(JsonObjectTool.getAsBool(symbol,"quoteOrderQtyMarketAllowed"));
                JsonArray filters = JsonObjectTool.getAsJsonArray(symbol, "filters");
                for (JsonElement filterElement : filters) {
                    JsonObject filter = (JsonObject)filterElement;
                    String filterType = JsonObjectTool.getAsString(filter, "filterType");
                    if(StringUtils.equals(filterType, "PRICE_FILTER")){
                        exchangeInfo.setMinPrice(JsonObjectTool.getAsBigDecimal(filter, "minPrice"));
                        exchangeInfo.setMaxPrice(JsonObjectTool.getAsBigDecimal(filter, "maxPrice"));
                        BigDecimal tickSize = JsonObjectTool.getAsBigDecimal(filter, "tickSize");
                        exchangeInfo.setTickSize(tickSize);
                        exchangeInfo.setTickSizePrecision(Objects.nonNull(tickSize) ? (-(int) (Math.log10(tickSize.doubleValue()))) : 0);
                    }else if(StringUtils.equals(filterType, "LOT_SIZE")){
                        exchangeInfo.setMinQty(JsonObjectTool.getAsBigDecimal(filter, "minQty"));
                        exchangeInfo.setMaxQty(JsonObjectTool.getAsBigDecimal(filter, "maxQty"));
                        BigDecimal stepSize = JsonObjectTool.getAsBigDecimal(filter, "stepSize");
                        exchangeInfo.setStepSize(stepSize);
                        exchangeInfo.setStepSizePrecision(Objects.nonNull(stepSize) ? (-(int) (Math.log10(stepSize.doubleValue()))) : 0);
                    }else if(StringUtils.equals(filterType, "NOTIONAL")){
                        exchangeInfo.setMinNotional(JsonObjectTool.getAsBigDecimal(filter, "minNotional"));
                        exchangeInfo.setApplyToMarket(JsonObjectTool.getAsBool(filter, "applyMinToMarket"));
                    }
                }
                BinanceExchangeInfoCache.updateSymbol(symbolString, exchangeInfo);
            }
        } catch (Exception e) {
            ExceptionUtils.printStackTrace(e);
        } finally {
            monitorLock.unlock();
        }
    }

    public void monitorWithoutLock() {
        try {
            HttpResponse httpResponse = HttpUtils.doGet("https://api.binance.com", "/api/v3/exchangeInfo", Map.of(), Map.of());
            String dataJsonString = EntityUtils.toString(httpResponse.getEntity());
            JsonObject jsonObject = Constants.GSON.fromJson(dataJsonString, JsonObject.class);
            JsonArray symbols = JsonObjectTool.getAsJsonArray(jsonObject, "symbols");
            for (JsonElement element : symbols) {
                JsonObject symbol = (JsonObject)element;
                BinanceExchangeInfoCache.ExchangeInfo exchangeInfo = new BinanceExchangeInfoCache.ExchangeInfo();
                String symbolString = JsonObjectTool.getAsString(symbol, "symbol");
                exchangeInfo.setSymbol(symbolString);
                exchangeInfo.setBaseAsset(JsonObjectTool.getAsString(symbol, "baseAsset"));
                exchangeInfo.setBaseAssetPrecision(JsonObjectTool.getAsInt(symbol, "baseAssetPrecision"));
                exchangeInfo.setQuoteAsset(JsonObjectTool.getAsString(symbol, "quoteAsset"));
                exchangeInfo.setQuoteAssetPrecision(JsonObjectTool.getAsInt(symbol, "quoteAssetPrecision"));
                exchangeInfo.setQuoteOrderQtyMarketAllowed(JsonObjectTool.getAsBool(symbol,"quoteOrderQtyMarketAllowed"));
                JsonArray filters = JsonObjectTool.getAsJsonArray(symbol, "filters");
                for (JsonElement filterElement : filters) {
                    JsonObject filter = (JsonObject)filterElement;
                    String filterType = JsonObjectTool.getAsString(filter, "filterType");
                    if(StringUtils.equals(filterType, "PRICE_FILTER")){
                        exchangeInfo.setMinPrice(JsonObjectTool.getAsBigDecimal(filter, "minPrice"));
                        exchangeInfo.setMaxPrice(JsonObjectTool.getAsBigDecimal(filter, "maxPrice"));
                        BigDecimal tickSize = JsonObjectTool.getAsBigDecimal(filter, "tickSize");
                        exchangeInfo.setTickSize(tickSize);
                        exchangeInfo.setTickSizePrecision(Objects.nonNull(tickSize) ? (-(int) (Math.log10(tickSize.doubleValue()))) : 0);
                    }else if(StringUtils.equals(filterType, "LOT_SIZE")){
                        exchangeInfo.setMinQty(JsonObjectTool.getAsBigDecimal(filter, "minQty"));
                        exchangeInfo.setMaxQty(JsonObjectTool.getAsBigDecimal(filter, "maxQty"));
                        BigDecimal stepSize = JsonObjectTool.getAsBigDecimal(filter, "stepSize");
                        exchangeInfo.setStepSize(stepSize);
                        exchangeInfo.setStepSizePrecision(Objects.nonNull(stepSize) ? (-(int) (Math.log10(stepSize.doubleValue()))) : 0);
                    }else if(StringUtils.equals(filterType, "NOTIONAL")){
                        exchangeInfo.setMinNotional(JsonObjectTool.getAsBigDecimal(filter, "minNotional"));
                        exchangeInfo.setApplyToMarket(JsonObjectTool.getAsBool(filter, "applyMinToMarket"));
                    }
                }
                BinanceExchangeInfoCache.updateSymbol(symbolString, exchangeInfo);
            }
        } catch (Exception e) {
            ExceptionUtils.printStackTrace(e);
        }
    }

    @Async(value = "exchangeThreadPoolTaskExecutor")
    @Scheduled(fixedDelay = 300_000, initialDelay = 1_00)
    public void futureMonitor() {
        boolean lock = futureMonitorLock.tryLock();
        if (!lock) {
            log.info("futureMonitor get reentrantLock failed");
            return;
        }
        try {
            HttpResponse httpResponse = HttpUtils.doGet("https://fapi.binance.com", "/fapi/v1/exchangeInfo", Map.of(), Map.of());
            String dataJsonString = EntityUtils.toString(httpResponse.getEntity());
            JsonObject jsonObject = Constants.GSON.fromJson(dataJsonString, JsonObject.class);
            JsonArray symbols = JsonObjectTool.getAsJsonArray(jsonObject, "symbols");
            for (JsonElement element : symbols) {
                JsonObject symbol = (JsonObject)element;
                BinanceExchangeInfoCache.ExchangeInfo exchangeInfo = new BinanceExchangeInfoCache.ExchangeInfo();
                String symbolString = JsonObjectTool.getAsString(symbol, "symbol");
                exchangeInfo.setSymbol(symbolString);
                exchangeInfo.setBaseAsset(JsonObjectTool.getAsString(symbol, "baseAsset"));
                exchangeInfo.setQuoteAsset(JsonObjectTool.getAsString(symbol, "quoteAsset"));
                exchangeInfo.setMarginAsset(JsonObjectTool.getAsString(symbol,"marginAsset"));
                exchangeInfo.setPricePrecision(JsonObjectTool.getAsInt(symbol,"pricePrecision"));
                exchangeInfo.setQuantityPrecision(JsonObjectTool.getAsInt(symbol,"quantityPrecision"));
                exchangeInfo.setBaseAssetPrecision(JsonObjectTool.getAsInt(symbol, "baseAssetPrecision"));
                exchangeInfo.setQuoteAssetPrecision(JsonObjectTool.getAsInt(symbol, "quotePrecision"));
                exchangeInfo.setLiquidationFee(JsonObjectTool.getAsBigDecimal(symbol,"liquidationFee"));
                JsonArray filters = JsonObjectTool.getAsJsonArray(symbol, "filters");
                for (JsonElement filterElement : filters) {
                    JsonObject filter = (JsonObject)filterElement;
                    String filterType = JsonObjectTool.getAsString(filter, "filterType");
                    if(StringUtils.equals(filterType, "PRICE_FILTER")){
                        exchangeInfo.setMinPrice(JsonObjectTool.getAsBigDecimal(filter, "minPrice"));
                        exchangeInfo.setMaxPrice(JsonObjectTool.getAsBigDecimal(filter, "maxPrice"));
                        BigDecimal tickSize = JsonObjectTool.getAsBigDecimal(filter, "tickSize");
                        exchangeInfo.setTickSize(tickSize);
                        exchangeInfo.setTickSizePrecision(Objects.nonNull(tickSize) ? (-(int) (Math.log10(tickSize.doubleValue()))) : 0);
                    }else if(StringUtils.equals(filterType, "LOT_SIZE")){
                        exchangeInfo.setMinQty(JsonObjectTool.getAsBigDecimal(filter, "minQty"));
                        exchangeInfo.setMaxQty(JsonObjectTool.getAsBigDecimal(filter, "maxQty"));
                        BigDecimal stepSize = JsonObjectTool.getAsBigDecimal(filter, "stepSize");
                        exchangeInfo.setStepSize(stepSize);
                        exchangeInfo.setStepSizePrecision(Objects.nonNull(stepSize) ? (-(int) (Math.log10(stepSize.doubleValue()))) : 0);
                    }else if (StringUtils.equals(filterType,"MARKET_LOT_SIZE")) {
                        exchangeInfo.setMarketMinQty(JsonObjectTool.getAsBigDecimal(filter, "minQty"));
                        exchangeInfo.setMarketMaxQty(JsonObjectTool.getAsBigDecimal(filter, "maxQty"));
                        BigDecimal stepSize = JsonObjectTool.getAsBigDecimal(filter, "stepSize");
                        exchangeInfo.setMarketStepSize(stepSize);
                        exchangeInfo.setMarketStepSizePrecision(Objects.nonNull(stepSize) ? (-(int) (Math.log10(stepSize.doubleValue()))) : 0);
                    }
                    else if(StringUtils.equals(filterType, "MIN_NOTIONAL")){
                        exchangeInfo.setMinNotional(JsonObjectTool.getAsBigDecimal(filter, "notional"));
                    }
                }
                BinanceExchangeInfoCache.updateFutureSymbol(symbolString, exchangeInfo);
            }
        } catch (Exception e) {
            ExceptionUtils.printStackTrace(e);
        } finally {
            futureMonitorLock.unlock();
        }
    }

    public void futureMonitorWithoutLock() {
        try {
            HttpResponse httpResponse = HttpUtils.doGet("https://fapi.binance.com", "/fapi/v1/exchangeInfo", Map.of(), Map.of());
            String dataJsonString = EntityUtils.toString(httpResponse.getEntity());
            JsonObject jsonObject = Constants.GSON.fromJson(dataJsonString, JsonObject.class);
            JsonArray symbols = JsonObjectTool.getAsJsonArray(jsonObject, "symbols");
            for (JsonElement element : symbols) {
                JsonObject symbol = (JsonObject)element;
                BinanceExchangeInfoCache.ExchangeInfo exchangeInfo = new BinanceExchangeInfoCache.ExchangeInfo();
                String symbolString = JsonObjectTool.getAsString(symbol, "symbol");
                exchangeInfo.setSymbol(symbolString);
                exchangeInfo.setBaseAsset(JsonObjectTool.getAsString(symbol, "baseAsset"));
                exchangeInfo.setQuoteAsset(JsonObjectTool.getAsString(symbol, "quoteAsset"));
                exchangeInfo.setMarginAsset(JsonObjectTool.getAsString(symbol,"marginAsset"));
                exchangeInfo.setPricePrecision(JsonObjectTool.getAsInt(symbol,"pricePrecision"));
                exchangeInfo.setQuantityPrecision(JsonObjectTool.getAsInt(symbol,"quantityPrecision"));
                exchangeInfo.setBaseAssetPrecision(JsonObjectTool.getAsInt(symbol, "baseAssetPrecision"));
                exchangeInfo.setQuoteAssetPrecision(JsonObjectTool.getAsInt(symbol, "quotePrecision"));
                JsonArray filters = JsonObjectTool.getAsJsonArray(symbol, "filters");
                for (JsonElement filterElement : filters) {
                    JsonObject filter = (JsonObject)filterElement;
                    String filterType = JsonObjectTool.getAsString(filter, "filterType");
                    if(StringUtils.equals(filterType, "PRICE_FILTER")){
                        exchangeInfo.setMinPrice(JsonObjectTool.getAsBigDecimal(filter, "minPrice"));
                        exchangeInfo.setMaxPrice(JsonObjectTool.getAsBigDecimal(filter, "maxPrice"));
                        BigDecimal tickSize = JsonObjectTool.getAsBigDecimal(filter, "tickSize");
                        exchangeInfo.setTickSize(tickSize);
                        exchangeInfo.setTickSizePrecision(Objects.nonNull(tickSize) ? (-(int) (Math.log10(tickSize.doubleValue()))) : 0);
                    }else if(StringUtils.equals(filterType, "LOT_SIZE")){
                        exchangeInfo.setMinQty(JsonObjectTool.getAsBigDecimal(filter, "minQty"));
                        exchangeInfo.setMaxQty(JsonObjectTool.getAsBigDecimal(filter, "maxQty"));
                        BigDecimal stepSize = JsonObjectTool.getAsBigDecimal(filter, "stepSize");
                        exchangeInfo.setStepSize(stepSize);
                        exchangeInfo.setStepSizePrecision(Objects.nonNull(stepSize) ? (-(int) (Math.log10(stepSize.doubleValue()))) : 0);
                    }else if (StringUtils.equals(filterType,"MARKET_LOT_SIZE")) {
                        exchangeInfo.setMarketMinQty(JsonObjectTool.getAsBigDecimal(filter, "minQty"));
                        exchangeInfo.setMarketMaxQty(JsonObjectTool.getAsBigDecimal(filter, "maxQty"));
                        BigDecimal stepSize = JsonObjectTool.getAsBigDecimal(filter, "stepSize");
                        exchangeInfo.setMarketStepSize(stepSize);
                        exchangeInfo.setMarketStepSizePrecision(Objects.nonNull(stepSize) ? (-(int) (Math.log10(stepSize.doubleValue()))) : 0);
                    }
                    else if(StringUtils.equals(filterType, "MIN_NOTIONAL")){
                        exchangeInfo.setMinNotional(JsonObjectTool.getAsBigDecimal(filter, "notional"));
                    }
                }
                BinanceExchangeInfoCache.updateFutureSymbol(symbolString, exchangeInfo);
            }
        } catch (Exception e) {
            ExceptionUtils.printStackTrace(e);
        }
    }

}

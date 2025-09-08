package com.flow.binance;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.Method;
import com.flow.exception.ErrorCodeEnum;
import com.flow.exception.ExceptionUtils;
import com.flow.tool.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.crypto.util.DigestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.flow.binance.BinanceApiConstants.*;

public class SpotHttpService {

    private static final Logger log = LoggerFactory.getLogger(SpotHttpService.class);

    public static BigDecimal getSpotBestBuyPrice(String symbol) {
        JsonObject bestBookTicker = getSpotBestBookTicker(symbol);
        return JsonObjectTool.getAsBigDecimal(bestBookTicker, "bidPrice"); // 最优买单价

    }

    public static BigDecimal getSpotBestSellPrice(String symbol) {
        JsonObject bestBookTicker = getSpotBestBookTicker(symbol);
        return JsonObjectTool.getAsBigDecimal(bestBookTicker, "askPrice"); // 最优卖单价
    }

    private static JsonObject getSpotBestBookTicker(String symbol) {
        String depth;
        try {
            HttpResponse depthRes = HttpUtils.doGet(BASE_URL, "/api/v3/ticker/bookTicker?symbol=" + symbol);
            depth = EntityUtils.toString(depthRes.getEntity());
        } catch (Exception e) {
            log.warn("getBestBookTicker symbol:{}, errMsg:{}", symbol, e.getMessage());
            return null;
        }
        return Constants.GSON.fromJson(depth, JsonObject.class);
    }

    public static String sendHttp(Method method, String path, Map<String, String> params) {
        String res;
        switch (method) {
            case GET: {
                res = get(path, params);
                break;
            }
            case POST: {
                res = post(path, params);
                break;
            }
            case PUT: {
                res = put(path, params);
                break;
            }
            case DELETE: {
                res = delete(path, params);
                break;
            }
            default: {
                res = "null";
            }
        }
        return res;
    }

    public static String get(String path, Map<String, String> params) {
        String url = assembleUrl(path, params);
        cn.hutool.http.HttpResponse execute = HttpRequest.get(url).header(API_HTTP_HEADER_KEY, BIAN_API_KEY) //头信息，多个头信息多次调用此方法即可
                .timeout(20_000).execute();
        return execute.body();
    }

    public static String post(String path, Map<String, String> params) {
        String url = assembleUrl(path, params);
        cn.hutool.http.HttpResponse execute = HttpRequest.post(url).header(API_HTTP_HEADER_KEY, BIAN_API_KEY) //头信息，多个头信息多次调用此方法即可
                .timeout(20_000) //超时，毫秒
                .execute();
        return execute.body();
    }

    public static String put(String path, Map<String, String> params) {
        String url = assembleUrl(path, params);
        cn.hutool.http.HttpResponse execute = HttpRequest.put(url).header(API_HTTP_HEADER_KEY, BIAN_API_KEY) //头信息，多个头信息多次调用此方法即可
                .timeout(20_000) //超时，毫秒
                .execute();
        return execute.body();

    }

    public static String delete(String path, Map<String, String> params) {
        String url = assembleUrl(path, params);
        cn.hutool.http.HttpResponse execute = HttpRequest.delete(url).header(API_HTTP_HEADER_KEY, BIAN_API_KEY) //头信息，多个头信息多次调用此方法即可
                .timeout(20_000) //超时，毫秒
                .execute();
        return execute.body();
    }

    private static String assembleUrl(String path, Map<String, String> params) {

        String host = BASE_URL;
        if (StringUtils.isBlank(path)) {
            ErrorCodeEnum.throwException("path is null");
        }

        StringBuilder querys = new StringBuilder();
        // recvWindow=60000&
        long currentTimeMillis = System.currentTimeMillis();
        querys.append("recvWindow=30000&timestamp=").append(currentTimeMillis);
        if (!CollectionUtils.isEmpty(params)) {
            params.forEach((key, value) -> {
                if (Objects.nonNull(key) && Objects.nonNull(value)) {
                    querys.append("&").append(key).append("=").append(value);
                }
            });
        }
        String reqStr = querys.toString();
        String signature = Crypto.hmacToString(DigestFactory.createSHA256(), BIAN_SECRET_KEY, reqStr);
        return host + path + "?" + reqStr.concat("&signature=").concat(signature);
    }

    static {
        EXCHANGE_INFO_CACHE = new ConcurrentHashMap<>(2500);
        syncSpotExchangeInfo();
    }

    public static void syncSpotExchangeInfo() {
        try {
            HttpResponse httpResponse = HttpUtils.doGet("https://api.binance.com", "/api/v3/exchangeInfo");
            String dataJsonString = EntityUtils.toString(httpResponse.getEntity());
            JsonObject jsonObject = Constants.GSON.fromJson(dataJsonString, JsonObject.class);
            JsonArray symbols = JsonObjectTool.getAsJsonArray(jsonObject, "symbols");
            if (Objects.isNull(symbols) || symbols.size() <= 0) ErrorCodeEnum.NETWORK_ERROR.throwException();
            for (JsonElement element : symbols) {
                JsonObject symbol = (JsonObject) element;
                BinanceExchangeInfoCache.ExchangeInfo exchangeInfo = new BinanceExchangeInfoCache.ExchangeInfo();
                String symbolString = JsonObjectTool.getAsString(symbol, "symbol");
                exchangeInfo.setSymbol(symbolString);
                exchangeInfo.setBaseAsset(JsonObjectTool.getAsString(symbol, "baseAsset"));
                exchangeInfo.setBaseAssetPrecision(JsonObjectTool.getAsInt(symbol, "baseAssetPrecision"));
                exchangeInfo.setQuoteAsset(JsonObjectTool.getAsString(symbol, "quoteAsset"));
                exchangeInfo.setQuoteAssetPrecision(JsonObjectTool.getAsInt(symbol, "quoteAssetPrecision"));
                exchangeInfo.setQuoteOrderQtyMarketAllowed(JsonObjectTool.getAsBool(symbol, "quoteOrderQtyMarketAllowed"));
                JsonArray filters = JsonObjectTool.getAsJsonArray(symbol, "filters");
                for (JsonElement filterElement : filters) {
                    JsonObject filter = (JsonObject) filterElement;
                    String filterType = JsonObjectTool.getAsString(filter, "filterType");
                    if (StringUtils.equals(filterType, "PRICE_FILTER")) {
                        exchangeInfo.setMinPrice(JsonObjectTool.getAsBigDecimal(filter, "minPrice"));
                        exchangeInfo.setMaxPrice(JsonObjectTool.getAsBigDecimal(filter, "maxPrice"));
                        BigDecimal tickSize = JsonObjectTool.getAsBigDecimal(filter, "tickSize");
                        exchangeInfo.setTickSize(tickSize);
                        exchangeInfo.setTickSizePrecision(Objects.nonNull(tickSize) ? (-(int) (Math.log10(tickSize.doubleValue()))) : 0);
                    } else if (StringUtils.equals(filterType, "LOT_SIZE")) {
                        exchangeInfo.setMinQty(JsonObjectTool.getAsBigDecimal(filter, "minQty"));
                        exchangeInfo.setMaxQty(JsonObjectTool.getAsBigDecimal(filter, "maxQty"));
                        BigDecimal stepSize = JsonObjectTool.getAsBigDecimal(filter, "stepSize");
                        exchangeInfo.setStepSize(stepSize);
                        exchangeInfo.setStepSizePrecision(Objects.nonNull(stepSize) ? (-(int) (Math.log10(stepSize.doubleValue()))) : 0);
                    } else if (StringUtils.equals(filterType, "NOTIONAL")) {
                        exchangeInfo.setMinNotional(JsonObjectTool.getAsBigDecimal(filter, "minNotional"));
                        exchangeInfo.setApplyToMarket(JsonObjectTool.getAsBool(filter, "applyMinToMarket"));
                    }
                }
                EXCHANGE_INFO_CACHE.put(symbolString, exchangeInfo);
            }
        } catch (Exception e) {
            ExceptionUtils.printStackTrace(e);
        }
    }

    public static final ConcurrentHashMap<String, BinanceExchangeInfoCache.ExchangeInfo> EXCHANGE_INFO_CACHE;

}

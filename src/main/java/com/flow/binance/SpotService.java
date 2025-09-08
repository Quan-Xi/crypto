package com.flow.binance;

import cn.hutool.http.Method;
import com.flow.exception.ErrorCodeEnum;
import com.flow.tool.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class SpotService {

    private static final Logger log = LoggerFactory.getLogger(SpotService.class);

    public static void spotBalanceMonitoring() {
        spotBalanceMonitoring(60_000L);
    }

    /**
     * 现货资产监控
     * @param sleepTime 间隔时间
     */
    public static void spotBalanceMonitoring(long sleepTime) {
        Map<String, JsonObject> balanceMap = new HashMap<>();

        while (true) {
            try {
                {
                    String dataJsonString = SpotHttpService.sendHttp(Method.GET, "/api/v3/account", Map.of("omitZeroBalances", "true"));
                    JsonObject jsonObject = Constants.GSON.fromJson(dataJsonString, JsonObject.class);
                    JsonArray asJsonArray = JsonObjectTool.getAsJsonArray(jsonObject, "balances");
                    for (JsonElement jsonElement : asJsonArray) {
                        JsonObject asJsonObject = jsonElement.getAsJsonObject();
                        String asset = JsonObjectTool.getAsString(asJsonObject, "asset");
//                        Double free = JsonObjectTool.getAsDouble(asJsonObject, "free");
//                        Double locked = JsonObjectTool.getAsDouble(asJsonObject, "locked");
                        balanceMap.put(asset, asJsonObject);
                    }
                }

                Map<String, Double> priceMap = new HashMap<>();
                List.of("APT", "DOGE", "PEPE", "POL", "OP").forEach(e -> {
                    double close = 0;
                    try {
                        HttpResponse httpResponse = HttpUtils.doGet("https://api.binance.com", "/api/v3/klines?symbol=" + e + "USDT&interval=1d&limit=1");
                        String responseJson = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                        JsonArray jsonObject = new Gson().fromJson(responseJson, JsonArray.class).get(0).getAsJsonArray();
                        close = jsonObject.get(4).getAsDouble();
                    } catch (Exception ignored) {
                    }
                    priceMap.put(e, close);
                });
                double amountTotalUsdt = 0.0;
                double amountTotalFree = 0.0;
                for (JsonObject value : balanceMap.values()) {
                    String asset = JsonObjectTool.getAsString(value, "asset");
                    if (StringUtils.equalsIgnoreCase(asset, "DOGE")) continue;
                    Double price = priceMap.getOrDefault(asset, 0.0);
                    Double free = JsonObjectTool.getAsDouble(value, "free");
                    Double locked = JsonObjectTool.getAsDouble(value, "locked");
                    amountTotalUsdt += price * (free + locked);
                    amountTotalFree += price * free;
                }
                {
                    double dogeAmount = amountTotalUsdt / priceMap.get("DOGE");
                    double difDogeAmount = dogeAmount - 20000;
                    double dogeDifUsdt = difDogeAmount * priceMap.get("DOGE");
                    if (dogeDifUsdt > 100) {
                        DingDingMsgUtil.sendDiscard_("交换提醒\n dogeDifUsdt > 30U", List.of(), "https://oapi.dingtalk.com/robot/send?access_token=b938a6debec2e79077f773fca68ab4e99ada7cd2823b468573e627ff0b30455e");
                    }
                    System.out.println("当前可以兑换的DOGE(" + String.format("%.5f", priceMap.get("DOGE")) + "): " + String.format("%.2f", dogeAmount) + " (期望：20000, 盈亏：" + String.format("%.2f", difDogeAmount) + "个DOGE/" + String.format("%.2f", dogeDifUsdt) + "U)");
                    System.out.println("计算在哪的资金是：" + amountTotalFree);
                    double pepeAmount = amountTotalFree / priceMap.get("PEPE");
                    double difPepeAmount = pepeAmount - 186982981;
                    double pepeDifUsdt = difPepeAmount * priceMap.get("PEPE");
                    if (pepeDifUsdt > 100) {
                        DingDingMsgUtil.sendDiscard_("交换提醒\n pepeDifUsdt > 30U", List.of(), "https://oapi.dingtalk.com/robot/send?access_token=b938a6debec2e79077f773fca68ab4e99ada7cd2823b468573e627ff0b30455e");
//                        现货_兑换("OP", "PEPE", new BigDecimal(1700));
//                        System.out.println("兑换成功 return");
//                        return;
                    }
                    System.out.println("当前可以兑换的PEPE(" + String.format("%.8f", priceMap.get("PEPE")) + "): " + String.format("%.2f", pepeAmount) + " (期望：186982981, 盈亏：" + String.format("%.2f", difPepeAmount) + "个PEPE/" + String.format("%.2f", pepeDifUsdt) + "U)");
                    double aptAmount = amountTotalFree / priceMap.get("APT");
                    double difAptAmount = aptAmount - 480.08;
                    double aptDifUsdt = difAptAmount * priceMap.get("APT");
                    if (aptDifUsdt > 100) {
                        DingDingMsgUtil.sendDiscard_("交换提醒\n aptDifUsdt > 30U", List.of(), "https://oapi.dingtalk.com/robot/send?access_token=b938a6debec2e79077f773fca68ab4e99ada7cd2823b468573e627ff0b30455e");
                    }
                    System.out.println("当前可以兑换的APT(" + String.format("%.3f", priceMap.get("APT")) + "): " + String.format("%.2f", aptAmount) + " (期望：480.08, 盈亏：" + String.format("%.2f", difAptAmount) + "个APT/" + String.format("%.2f", aptDifUsdt) + "U)");
                    double polAmount = amountTotalFree / priceMap.get("POL");
                    double difPolAmount = polAmount - 9485;
                    double polDifUsdt = difPolAmount * priceMap.get("POL");
                    if (polDifUsdt > 100) {
                        DingDingMsgUtil.sendDiscard_("交换提醒\n polDifUsdt > 30U", List.of(), "https://oapi.dingtalk.com/robot/send?access_token=b938a6debec2e79077f773fca68ab4e99ada7cd2823b468573e627ff0b30455e");
                    }
                    System.out.println("当前可以兑换的POL(" + String.format("%.4f", priceMap.get("POL")) + "): " + String.format("%.2f", polAmount) + " (期望：9485, 盈亏：" + String.format("%.2f", difPolAmount) + "个POL/" + String.format("%.2f", polDifUsdt) + "U)");
                    double opAmount = amountTotalFree / priceMap.get("OP");
                    double dofOpAmount = opAmount - 2868.28;
                    double opDifUsdt = dofOpAmount * priceMap.get("OP");
                    if (opDifUsdt > 100) {
                        DingDingMsgUtil.sendDiscard_("交换提醒\n opDifUsdt > 30U", List.of(), "https://oapi.dingtalk.com/robot/send?access_token=b938a6debec2e79077f773fca68ab4e99ada7cd2823b468573e627ff0b30455e");
                    }
                    System.out.println("当前可以兑换的OP(" + String.format("%.4f", priceMap.get("OP")) + "): " + String.format("%.2f", opAmount) + " (期望：2868.28, 盈亏：" + String.format("%.2f", dofOpAmount) + "个OP/" + String.format("%.2f", opDifUsdt) + "U)");
                }
                TimeTool.sleep(sleepTime);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                TimeTool.sleep(sleepTime);
            }
        }
    }

    public static void swap(String fromBaseAsset, String toBaseAsset, BigDecimal quantity) {
        swap(fromBaseAsset, toBaseAsset, "FDUSD", quantity);
    }

    /**
     * 卖出 + 买入 双双执行成功
     */
    public static void swap(String fromBaseAsset, String toBaseAsset, String quoteAsset, BigDecimal quantity) {
        SpotMarkerOrderDetail orderDetailSell = markerOrderSellSuccess(fromBaseAsset, quoteAsset, quantity);
        SpotMarkerOrderDetail orderDetailBuy = markerOrderBuySuccess(toBaseAsset, quoteAsset, orderDetailSell.getExecutedU());
        System.out.println("\n\n");
        String sellStr = "SELL: " + orderDetailSell.getSymbol()  + ", 卖出价格：" + orderDetailSell.getExecutedU().divide(orderDetailSell.getExecutedQty(), 8, RoundingMode.DOWN) + ", 卖出数量：" + orderDetailSell.getExecutedQty() + ", 获取U: " + orderDetailSell.getExecutedU();
        System.out.println(sellStr);
        String buyStr = "BUY: " + orderDetailBuy.getSymbol()  + ", 买入价格：" + orderDetailBuy.getExecutedU().divide(orderDetailBuy.getExecutedQty(), 8, RoundingMode.DOWN) + ", 买出数量：" + orderDetailBuy.getExecutedQty() + ", 花费U: " + orderDetailBuy.getExecutedU();
        System.out.println(buyStr);
        System.out.println("\n🎉🎉🎉完成兑换SUCCESS 🎉🎉🎉");
    }

    /**
     * 挂单成功到全部成交
     * sell
     * buy
     */
    public static SpotMarkerOrderDetail markerOrderSellSuccess(String baseAsset, String quoteAsset, BigDecimal quantity) {

        String symbolSell = baseAsset + quoteAsset;
        BinanceExchangeInfoCache.ExchangeInfo exchangeInfo = SpotHttpService.EXCHANGE_INFO_CACHE.get(symbolSell);
        quantity = quantity.setScale(exchangeInfo.getStepSizePrecision(), RoundingMode.DOWN);
        JsonObject resSellJson;
        BigDecimal executedSellQty = BigDecimal.ZERO;
        BigDecimal executedSellU = BigDecimal.ZERO;
        {
            BigDecimal baseSellFreeAmount = BigDecimal.ZERO;
            {
                String dataJsonString = SpotHttpService.sendHttp(Method.GET, "/api/v3/account", Map.of("omitZeroBalances", "true"));
                JsonObject jsonObject = Constants.GSON.fromJson(dataJsonString, JsonObject.class);
                JsonArray asJsonArray = JsonObjectTool.getAsJsonArray(jsonObject, "balances");
                for (JsonElement jsonElement : asJsonArray) {
                    JsonObject asJsonObject = jsonElement.getAsJsonObject();
                    String asset = JsonObjectTool.getAsString(asJsonObject, "asset");
                    if (StringUtils.equalsIgnoreCase(asset, baseAsset)) {
                        baseSellFreeAmount = JsonObjectTool.getAsBigDecimal(asJsonObject, "free");
                        break;
                    }
                }
            }

            if (baseSellFreeAmount.compareTo(BigDecimal.ZERO) <= 0) ErrorCodeEnum.throwException("资产为0");

            if (baseSellFreeAmount.compareTo(quantity) < 0) {
                quantity = baseSellFreeAmount.setScale(exchangeInfo.getStepSizePrecision(), RoundingMode.DOWN);;
            }

            BigDecimal minQty = exchangeInfo.getMinQty();
            String 现货_挂单_卖 = null;
            int tryCount = 0;
            while (true) {
                if (StringUtils.isBlank(现货_挂单_卖)) {
                    BigDecimal subtract = quantity.subtract(executedSellQty);
                    if (subtract.compareTo(minQty) < 0) {
                        if (executedSellQty.compareTo(BigDecimal.ZERO) > 0) {
                            break;
                        } else {
                            ErrorCodeEnum.throwException("卖单：不对劲呀");
                        }
                    }
                    现货_挂单_卖 = markerOrderSell(symbolSell, subtract);
                }
                String res = SpotHttpService.sendHttp(Method.GET, "/api/v3/order", Map.of("symbol", symbolSell, "origClientOrderId", 现货_挂单_卖));
                resSellJson = Constants.GSON.fromJson(res, JsonObject.class);
                if (StringUtils.equalsIgnoreCase(JsonObjectTool.getAsString(resSellJson, "status"), "FILLED")) {
                    executedSellQty = executedSellQty.add(JsonObjectTool.getAsBigDecimal(resSellJson, "executedQty"));
                    executedSellU = executedSellU.add(JsonObjectTool.getAsBigDecimal(resSellJson, "cummulativeQuoteQty"));
                    break;
                } else {
                    if (tryCount++ < 10) {
                        TimeTool.sleep(300);
                    } else {
                        BigDecimal spotBestPrice = SpotHttpService.getSpotBestSellPrice(JsonObjectTool.getAsString(resSellJson, "symbol"));
                        if (spotBestPrice.compareTo(JsonObjectTool.getAsBigDecimal(resSellJson, "price")) >= 0) {
                            tryCount = 0;
                            continue;
                        }
                        String delete = SpotHttpService.sendHttp(Method.DELETE, "/api/v3/order", Map.of("symbol", symbolSell, "origClientOrderId", 现货_挂单_卖));
                        System.out.println("删除卖单，重新下单～: " + delete);
                        JsonObject deleteJson = Constants.GSON.fromJson(delete, JsonObject.class);
                        if (Objects.isNull(JsonObjectTool.getAsInt(deleteJson, "code"))) {
                            executedSellQty = executedSellQty.add(JsonObjectTool.getAsBigDecimal(deleteJson, "executedQty"));
                            executedSellU = executedSellU.add(JsonObjectTool.getAsBigDecimal(deleteJson, "cummulativeQuoteQty"));
                            现货_挂单_卖 = null;
                            tryCount = 0;
                        } else {
                            System.out.println("删除卖单失败： " + delete);
                        }
                    }
                }
            }
        }
        return SpotMarkerOrderDetail.builder().symbol(symbolSell).executedQty(executedSellQty).executedU(executedSellU).build();
    }
    public static SpotMarkerOrderDetail markerOrderBuySuccess(String baseAsset, String quoteAsset, BigDecimal amountUsdt) {

        String symbolBuy = baseAsset + quoteAsset;
        BinanceExchangeInfoCache.ExchangeInfo exchangeInfo = SpotHttpService.EXCHANGE_INFO_CACHE.get(symbolBuy);
        JsonObject resBuyJson;
        BigDecimal executedBuyQty = BigDecimal.ZERO;
        BigDecimal executedBuyU = BigDecimal.ZERO;
        {
            BigDecimal fdusdFreeAmount = BigDecimal.ZERO;
            {
                String dataJsonString = SpotHttpService.sendHttp(Method.GET, "/api/v3/account", Map.of("omitZeroBalances", "true"));
                JsonObject jsonObject = Constants.GSON.fromJson(dataJsonString, JsonObject.class);
                JsonArray asJsonArray = JsonObjectTool.getAsJsonArray(jsonObject, "balances");
                for (JsonElement jsonElement : asJsonArray) {
                    JsonObject asJsonObject = jsonElement.getAsJsonObject();
                    String asset = JsonObjectTool.getAsString(asJsonObject, "asset");
                    if (StringUtils.equals(asset, quoteAsset)) {
                        fdusdFreeAmount = JsonObjectTool.getAsBigDecimal(asJsonObject, "free");
                        break;
                    }
                }
            }

            if (fdusdFreeAmount.compareTo(BigDecimal.ZERO) <= 0) ErrorCodeEnum.throwException("资产为0");

            if (fdusdFreeAmount.compareTo(amountUsdt) < 0) {
                amountUsdt = fdusdFreeAmount;
            }
            String 现货_挂单_买 = null;
            int tryCount2 = 0;
            BigDecimal minNotional = exchangeInfo.getMinNotional();
            while (true) {
                if (StringUtils.isBlank(现货_挂单_买)) {
                    BigDecimal subtract = amountUsdt.subtract(executedBuyU);
                    if (subtract.compareTo(minNotional) < 0) {
                        if (executedBuyU.compareTo(BigDecimal.ZERO) > 0) {
                            break;
                        } else {
                            ErrorCodeEnum.throwException("买单：不对劲呀");
                        }
                    }
                    现货_挂单_买 = markerOrderBuy(symbolBuy, subtract);
                }
                String res = SpotHttpService.sendHttp(Method.GET, "/api/v3/order", Map.of("symbol", symbolBuy, "origClientOrderId", 现货_挂单_买));
                resBuyJson = Constants.GSON.fromJson(res, JsonObject.class);
                if (StringUtils.equalsIgnoreCase(JsonObjectTool.getAsString(resBuyJson, "status"), "FILLED")) {
                    executedBuyQty = executedBuyQty.add(JsonObjectTool.getAsBigDecimal(resBuyJson, "executedQty"));
                    executedBuyU = executedBuyU.add(JsonObjectTool.getAsBigDecimal(resBuyJson, "cummulativeQuoteQty"));
                    break;
                } else {
                    if (tryCount2++ < 10) {
                        TimeTool.sleep(300);
                    } else {
                        BigDecimal spotBestPrice = SpotHttpService.getSpotBestBuyPrice(JsonObjectTool.getAsString(resBuyJson, "symbol"));
                        if (spotBestPrice.compareTo(JsonObjectTool.getAsBigDecimal(resBuyJson, "price")) <= 0) {
                            tryCount2 = 0;
                            continue;
                        }
                        String delete = SpotHttpService.sendHttp(Method.DELETE, "/api/v3/order", Map.of("symbol", symbolBuy, "origClientOrderId", 现货_挂单_买));
                        System.out.println("删除买单，重新下单～: " + delete);
                        JsonObject deleteJson = Constants.GSON.fromJson(delete, JsonObject.class);
                        if (Objects.isNull(JsonObjectTool.getAsInt(deleteJson, "code"))) {
                            executedBuyQty = executedBuyQty.add(JsonObjectTool.getAsBigDecimal(deleteJson, "executedQty"));
                            executedBuyU = executedBuyU.add(JsonObjectTool.getAsBigDecimal(deleteJson, "cummulativeQuoteQty"));
                            现货_挂单_买 = null;
                            tryCount2 = 0;
                        } else {
                            System.out.println("删除买单失败： " + delete);
                        }
                    }
                }
            }
        }
        return SpotMarkerOrderDetail.builder().symbol(symbolBuy).executedQty(executedBuyQty).executedU(executedBuyU).build();
    }

    /**
     * 挂单卖出
     */
    public static String markerOrderSell(String symbol, BigDecimal quantity) {
        return markerOrder(symbol, "SELL", null, quantity);
    }

    /**
     * 挂单买入
     */
    public static String markerOrderBuy(String symbol, BigDecimal amountUsdt) {
        return markerOrder(symbol, "BUY", amountUsdt, null);
    }

    /**
     * 币安挂单
     * sell
     * buy
     */
    public static String markerOrder(String symbol, String side, BigDecimal amountUsdt, BigDecimal quantity) {
        // 获取价格
        String clientId = UUID.randomUUID().toString().replaceAll("-", "");
        BinanceExchangeInfoCache.ExchangeInfo exchangeInfo = SpotHttpService.EXCHANGE_INFO_CACHE.get(symbol);
        if (Objects.isNull(exchangeInfo)) ErrorCodeEnum.throwException("exchangeInfo is null");
        while (true) {
            BigDecimal spotBestPrice = null;
            if (StringUtils.equalsIgnoreCase(side, "BUY")) {
                spotBestPrice = SpotHttpService.getSpotBestBuyPrice(symbol);
            } else if (StringUtils.equalsIgnoreCase(side, "SELL")) {
                spotBestPrice = SpotHttpService.getSpotBestSellPrice(symbol);
            } else {
                ErrorCodeEnum.throwException("side参数错误");
            }
            if (Objects.isNull(spotBestPrice) || spotBestPrice.compareTo(BigDecimal.ZERO) <= 0)
                ErrorCodeEnum.throwException("获取价格失败");
            if (Objects.isNull(quantity)) {
                quantity = amountUsdt.divide(spotBestPrice, exchangeInfo.getStepSizePrecision(), RoundingMode.DOWN);
            }
            String res = SpotHttpService.sendHttp(Method.POST, "/api/v3/order", Map.of("symbol", symbol, "side", side, "type", "LIMIT_MAKER", "quantity", quantity.stripTrailingZeros().toPlainString(), "price", spotBestPrice.toPlainString(), "newClientOrderId", clientId));
            JsonObject resJson = Constants.GSON.fromJson(res, JsonObject.class);
            if (StringUtils.isNotBlank(JsonObjectTool.getAsString(resJson, "orderId"))) {
                break;
            } else {
                System.out.println("现货_挂单 res: " + res);
                TimeTool.sleep(5_00L);
            }
        }
        return clientId;
    }


    /**
     * 批量挂买单
     * 价格等比间隔
     */
    public static void markerBuyOrderBatch(String symbol, BigDecimal orderAmount, BigDecimal orderIntervalRate,
                                           BigDecimal startPrice, BigDecimal endPrice) {
        BinanceExchangeInfoCache.ExchangeInfo exchangeInfo = SpotHttpService.EXCHANGE_INFO_CACHE.get(symbol);
        if (Objects.isNull(exchangeInfo)) ErrorCodeEnum.throwException("exchangeInfo is null");
        BigDecimal spotBestBuyPrice = SpotHttpService.getSpotBestBuyPrice(symbol);
        if (startPrice.compareTo(spotBestBuyPrice) > 0) {
            startPrice = spotBestBuyPrice;
        }
        LinkedHashMap<String, Object> resMap = new LinkedHashMap<>();
        BigDecimal nextRate = BigDecimal.ONE.subtract(orderIntervalRate);
        for (int i = 0; i < 200; i++) {
            startPrice = startPrice.multiply(nextRate).setScale(exchangeInfo.getTickSizePrecision(), RoundingMode.DOWN);
            if (endPrice.compareTo(startPrice) > 0) {
                break;
            }
            BigDecimal quantity = orderAmount.divide(startPrice, exchangeInfo.getStepSizePrecision(), RoundingMode.UP);
            String res = SpotHttpService.sendHttp(Method.POST, "/api/v3/order", Map.of("symbol", symbol, "side", "BUY", "type", "LIMIT_MAKER", "quantity", quantity.stripTrailingZeros().toPlainString(), "price", startPrice.stripTrailingZeros().toPlainString(), "newClientOrderId", UUID.randomUUID().toString().replaceAll("-", "")));
            resMap.put("price:" + startPrice.toPlainString(), res);
            TimeTool.sleep(200L);
        }
        resMap.entrySet().forEach(e -> {
            System.out.println(e.getKey() + "   " + e.getValue());
        });
    }

    /**
     * 批量挂卖单
     * 价格等比间隔
     */
    public static void markerSellOrderBatch(String symbol, BigDecimal orderAmount, BigDecimal orderIntervalRate,
                                           BigDecimal startPrice, BigDecimal endPrice) {
        BinanceExchangeInfoCache.ExchangeInfo exchangeInfo = SpotHttpService.EXCHANGE_INFO_CACHE.get(symbol);
        if (Objects.isNull(exchangeInfo)) ErrorCodeEnum.throwException("exchangeInfo is null");
        BigDecimal spotBestSellPrice = SpotHttpService.getSpotBestSellPrice(symbol);
        if (startPrice.compareTo(spotBestSellPrice) < 0) {
            startPrice = spotBestSellPrice;
        }
        LinkedHashMap<String, Object> resMap = new LinkedHashMap<>();
        BigDecimal nextRate = BigDecimal.ONE.add(orderIntervalRate);
        orderAmount = orderAmount.setScale(exchangeInfo.getStepSizePrecision(), RoundingMode.DOWN);
        for (int i = 0; i < 200; i++) {
            startPrice = startPrice.multiply(nextRate).setScale(exchangeInfo.getTickSizePrecision(), RoundingMode.DOWN);
            if (endPrice.compareTo(startPrice) > 0) {
                break;
            }
            String res = SpotHttpService.sendHttp(Method.POST, "/api/v3/order", Map.of("symbol", symbol, "side", "SELL", "type", "LIMIT_MAKER", "quantity", orderAmount.toPlainString(), "price", startPrice.stripTrailingZeros().toPlainString(), "newClientOrderId", UUID.randomUUID().toString().replaceAll("-", "")));
            resMap.put("price:" + startPrice.toPlainString(), res);
            TimeTool.sleep(200L);
        }
        resMap.entrySet().forEach(e -> {
            System.out.println(e.getKey() + "   " + e.getValue());
        });
    }

}

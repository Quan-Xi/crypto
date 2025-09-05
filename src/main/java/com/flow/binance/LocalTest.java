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
import lombok.Builder;
import lombok.Data;
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


public class LocalTest {
    public static final String BASE_URL = "https://api.binance.com";
    public static final ConcurrentHashMap<String, BinanceExchangeInfoCache.ExchangeInfo> EXCHANGE_INFO_CACHE = new ConcurrentHashMap<>(2500);
    private static final Logger log = LoggerFactory.getLogger(LocalTest.class);
    private static final String API_HTTP_HEADER_KEY = "X-MBX-APIKEY";
    private static String BIAN_API_KEY = "1eVvdc3IEjtVc7jlhs57ghpgqugxHHcpSgk6KgOx7k5BQUq7Z2bbEyIkSkvhQ8su";
    private static String BIAN_SECRET_KEY = "UgcLYE6d7kizdf29nqm6thzR232wePO6BwEdFk3QRiBg8HVpngPkJAX56d2wFNht";

    private static StringBuilder LOG_CACHE = new StringBuilder();

    static class Monitor2 {
        public static void main(String[] args) {
            LocalTest.监控_钉钉通知_自动(30_000);
//            LocalTest.监控_钉钉通知_自动();
        }
    }

    /**
     * 兑换程序
     *
     * BTC -> ETH: BTC/FDUSD卖出成功，然后ETH/FDUSD买入直到成功；
     */
    static class SellAndBuy {
        public static void main(String[] args) {
               /*
            PEPE: 186982981
            APT:  480.08
            POL:  9485
            OP:  2868.28
            */
            String sell = "DOGE";
            String sellAmount = "175649059";
            String buy = "DOGE";
            System.out.println("执行0手续费兑换\n兑换: " + sell + " >> " + buy + "\n数量: " + sellAmount);
            Scanner scanner = new Scanner(System.in);
            System.out.print("\n--------------------\n当前程序为买卖程序\n确认: YES\n取消: 任意\n--------------------\n请输入: ");
            String key = scanner.nextLine();
            if (!StringUtils.equalsAnyIgnoreCase(key, "YES", "Y")) {
                return;
            }
//            LocalTest.现货_兑换(sell, buy, new BigDecimal(sellAmount));
        }
    }

    /**
     * 手动买卖程序
     */
    static class SellOrBuy {
        public static void main(String[] args) {

//            {
//                OrderDetail orderDetailSell = LocalTest.现货_挂单_卖_SUCCESS("DOGE", "FDUSD", BigDecimal.valueOf(38));
//                System.out.println("\n\n");
//                String sellStr = "SELL: " + orderDetailSell.getSymbol()  + ", 卖出价格：" + orderDetailSell.getExecutedU().divide(orderDetailSell.getExecutedQty(), 8, RoundingMode.DOWN) + ", 卖出数量：" + orderDetailSell.getExecutedQty() + ", 获取U: " + orderDetailSell.getExecutedU();
//                System.out.println(sellStr);
//            }
//            {
//                OrderDetail orderDetailBuy = LocalTest.现货_挂单_买_SUCCESS("DOGE", "FDUSD", BigDecimal.valueOf(11111111));
//                System.out.println("\n\n");
//                String buyStr = "BUY: " + orderDetailBuy.getSymbol()  + ", 买入价格：" + orderDetailBuy.getExecutedU().divide(orderDetailBuy.getExecutedQty(), 8, RoundingMode.DOWN) + ", 买出数量：" + orderDetailBuy.getExecutedQty() + ", 花费U: " + orderDetailBuy.getExecutedU();
//                System.out.println(buyStr);
//            }
            System.out.println("\n🎉🎉🎉完成兑换SUCCESS 🎉🎉🎉");

        }
    }

    /**
     * 批量挂买单
     */
    static class BatchLimitBuy {
        public static void main(String[] args) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("\n--------------------\n【批量 挂 BUY单，BUY单，BUY单】,是否继续执行\n--------------------\n请输入: ");
            String key = scanner.nextLine();
            if (!StringUtils.equalsAnyIgnoreCase(key, "YES", "Y")) {
                return;
            }
            String symbol = "DOGEFDUSD";
            BigDecimal orderAmount = new BigDecimal("10");
            BigDecimal orderIntervalRate = new BigDecimal("0.001");
            BigDecimal startPrice = new BigDecimal("1");
            BigDecimal endPrice = new BigDecimal("0.188");

            BinanceExchangeInfoCache.ExchangeInfo exchangeInfo = EXCHANGE_INFO_CACHE.get(symbol);
            if (Objects.isNull(exchangeInfo)) ErrorCodeEnum.throwException("exchangeInfo is null");
            BigDecimal spotBestBuyPrice = getSpotBestBuyPrice(symbol);
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
                String res = sendHttp(Method.POST, "/api/v3/order", Map.of("symbol", symbol, "side", "BUY", "type", "LIMIT_MAKER", "quantity", quantity.stripTrailingZeros().toPlainString(), "price", startPrice.stripTrailingZeros().toPlainString(), "newClientOrderId", UUID.randomUUID().toString().replaceAll("-", "")));
                resMap.put("price:" + startPrice.toPlainString(), res);
                TimeTool.sleep(200L);
            }
            resMap.entrySet().forEach(e -> {
                System.out.println(e.getKey() + "   " + e.getValue());
            });
        }
    }

    /**
     * 批量挂卖单
     */
    static class BatchLimitSell {
        public static void main(String[] args) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("\n--------------------\n【批量 挂 SELL单，SELL单，SELL单】,是否继续执行\n--------------------\n请输入: ");
            String key = scanner.nextLine();
            if (!StringUtils.equalsAnyIgnoreCase(key, "YES", "Y")) {
                return;
            }
            String symbol = "DOGEFDUSD";
            BigDecimal orderAmount = new BigDecimal("10");
            BigDecimal orderIntervalRate = new BigDecimal("0.001");
            BigDecimal startPrice = new BigDecimal("0.23");
            BigDecimal endPrice = new BigDecimal("2");

            BinanceExchangeInfoCache.ExchangeInfo exchangeInfo = EXCHANGE_INFO_CACHE.get(symbol);
            if (Objects.isNull(exchangeInfo)) ErrorCodeEnum.throwException("exchangeInfo is null");
            BigDecimal spotBestSellPrice = getSpotBestSellPrice(symbol);
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
                String res = sendHttp(Method.POST, "/api/v3/order", Map.of("symbol", symbol, "side", "SELL", "type", "LIMIT_MAKER", "quantity", orderAmount.toPlainString(), "price", startPrice.stripTrailingZeros().toPlainString(), "newClientOrderId", UUID.randomUUID().toString().replaceAll("-", "")));
                resMap.put("price:" + startPrice.toPlainString(), res);
                TimeTool.sleep(200L);
            }
            resMap.entrySet().forEach(e -> {
                System.out.println(e.getKey() + "   " + e.getValue());
            });
        }
    }

    public static void 监控_钉钉通知() {
        double pepeAmount = 0;
        double dogeFreeAmount = 0;
        double aptAmount = 0;
        double usdtAmount = 0;
        long currentTimeMillis = 0L;
        // 查询账户信息： https://api.binance.com/api/v3/account

        while (true) {
            try {
                {
                    String dataJsonString = sendHttp(Method.GET, "/api/v3/account", Map.of("omitZeroBalances", "true"));
                    JsonObject jsonObject = Constants.GSON.fromJson(dataJsonString, JsonObject.class);
                    JsonArray asJsonArray = JsonObjectTool.getAsJsonArray(jsonObject, "balances");
                    for (JsonElement jsonElement : asJsonArray) {
                        JsonObject asJsonObject = jsonElement.getAsJsonObject();
                        String asset = JsonObjectTool.getAsString(asJsonObject, "asset");
                        if (StringUtils.equals(asset, "DOGE")) {
                            dogeFreeAmount = JsonObjectTool.getAsDouble(asJsonObject, "free");
                        } else if (StringUtils.equals(asset, "PEPE")) {
                            Double locked = JsonObjectTool.getAsDouble(asJsonObject, "locked");
                            pepeAmount = JsonObjectTool.getAsDouble(asJsonObject, "free") + locked;
                        } else if (StringUtils.equals(asset, "USDT")) {
                            Double locked = JsonObjectTool.getAsDouble(asJsonObject, "locked");
                            usdtAmount = JsonObjectTool.getAsDouble(asJsonObject, "free") + locked;
                        }
                    }
                }

                double pepeUsdt;
                double pepePrice;

                {
                    HttpResponse httpResponse = HttpUtils.doGet("https://api.binance.com", "/api/v3/klines?symbol=PEPEUSDT&interval=1d&limit=1");
                    String responseJson = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                    JsonArray jsonObject = new Gson().fromJson(responseJson, JsonArray.class).get(0).getAsJsonArray();
                    double close = jsonObject.get(4).getAsDouble();
                    pepeUsdt = pepeAmount * close;
                    pepePrice = close;
                }

                double dogePrice;
                {
                    HttpResponse httpResponse2 = HttpUtils.doGet("https://api.binance.com", "/api/v3/klines?symbol=DOGEUSDT&interval=1d&limit=1");
                    String responseJson2 = EntityUtils.toString(httpResponse2.getEntity(), "UTF-8");
                    JsonArray jsonObject2 = new Gson().fromJson(responseJson2, JsonArray.class).get(0).getAsJsonArray();
                    double close = jsonObject2.get(4).getAsDouble();
                    dogePrice = close;
                }

                double aptPrice;
                {
                    HttpResponse httpResponse2 = HttpUtils.doGet("https://api.binance.com", "/api/v3/klines?symbol=APTUSDT&interval=1d&limit=1");
                    String responseJson2 = EntityUtils.toString(httpResponse2.getEntity(), "UTF-8");
                    JsonArray jsonObject2 = new Gson().fromJson(responseJson2, JsonArray.class).get(0).getAsJsonArray();
                    double close = jsonObject2.get(4).getAsDouble();
                    aptPrice = close;
                }

                String msgInfo = "【交换提醒】" + "\nDOGE价格: " + String.format("%.5f", dogePrice) + "\nPEPE价格: " + String.format("%.8f", pepePrice) + "\n成本PEPE价格: " + String.format("%.8f", ((dogePrice - 0.1883) / 0.1883 + 1) * 0.00001213) + "\nPEPE➡️DOGE: " + String.format("%.2f", pepeUsdt / dogePrice + dogeFreeAmount) + "(" + String.format("%.2f", (pepeUsdt / dogePrice - 20000 + dogeFreeAmount)) + " DOGE)";

                System.out.println(msgInfo);

                if (System.currentTimeMillis() - currentTimeMillis > 600_000 || (pepeUsdt / dogePrice >= 20000 - dogeFreeAmount)) {
                    currentTimeMillis = System.currentTimeMillis();
                    DingDingMsgUtil.sendDiscard_(msgInfo, List.of(), "https://oapi.dingtalk.com/robot/send?access_token=b938a6debec2e79077f773fca68ab4e99ada7cd2823b468573e627ff0b30455e");
                }
                if ((pepeUsdt * 0.998 / dogePrice) > (20000 - dogeFreeAmount)) {
                    // 执行分批卖出
                    DingDingMsgUtil.sendDiscard_("【交换提醒】" + "\n可以立即执行兑换PEPE➡️DOGE: " + String.format("%.2f", pepeUsdt / dogePrice + dogeFreeAmount) + "(" + String.format("%.2f", (pepeUsdt / dogePrice - 20000 + dogeFreeAmount)) + " DOGE)" + "\n市价买卖手续费: " + pepeUsdt * 0.002 / dogePrice + " DOGE", List.of("15558125752"), "https://oapi.dingtalk.com/robot/send?access_token=b938a6debec2e79077f773fca68ab4e99ada7cd2823b468573e627ff0b30455e");
                    Constants.ORDERLY_EXECUTOR_SERVICE.execute(() -> {
                        LocalTest.现货_兑换("PEPE", "DOGE", BigDecimal.valueOf(500_0000L));
                    });
                }
                if (LOG_CACHE.toString().length() > 10) {
                    System.out.println("\n\n\n" + LOG_CACHE + "\n\n");
                }
                TimeTool.sleep(180_000);
            } catch (Exception ex) {
                TimeTool.sleep(60_000);
            }
        }
    }

    public static void 监控_钉钉通知_自动() {
        监控_钉钉通知_自动(60_000L);
    }

    public static void 监控_钉钉通知_自动(long sleepTime) {
        Map<String, JsonObject> balanceMap = new HashMap<>();

        while (true) {
            try {
                {
                    String dataJsonString = sendHttp(Method.GET, "/api/v3/account", Map.of("omitZeroBalances", "true"));
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
                TimeTool.sleep(sleepTime);
            }
        }
    }

    private static void 现货_兑换(String fromBaseAsset, String toBaseAsset, BigDecimal quantity) {
        现货_兑换(fromBaseAsset, toBaseAsset, "FDUSD", quantity);
    }

    /**
     * 卖出 + 买入 双双执行成功
     */
    private static void 现货_兑换(String fromBaseAsset, String toBaseAsset, String quoteAsset, BigDecimal quantity) {
        OrderDetail orderDetailSell = 现货_挂单_卖_SUCCESS(fromBaseAsset, quoteAsset, quantity);
        OrderDetail orderDetailBuy = 现货_挂单_买_SUCCESS(toBaseAsset, quoteAsset, BigDecimal.valueOf(99999999999L));
        System.out.println("\n\n");
        String sellStr = "SELL: " + orderDetailSell.getSymbol()  + ", 卖出价格：" + orderDetailSell.getExecutedU().divide(orderDetailSell.getExecutedQty(), 8, RoundingMode.DOWN) + ", 卖出数量：" + orderDetailSell.getExecutedQty() + ", 获取U: " + orderDetailSell.getExecutedU();
        System.out.println(sellStr);
        String buyStr = "BUY: " + orderDetailBuy.getSymbol()  + ", 买入价格：" + orderDetailBuy.getExecutedU().divide(orderDetailBuy.getExecutedQty(), 8, RoundingMode.DOWN) + ", 买出数量：" + orderDetailBuy.getExecutedQty() + ", 花费U: " + orderDetailBuy.getExecutedU();
        System.out.println(buyStr);
        System.out.println("\n🎉🎉🎉完成兑换SUCCESS 🎉🎉🎉");
    }

    @Data
    @Builder
    static class OrderDetail {
        private String symbol;
        private BigDecimal executedQty;
        private BigDecimal executedU;
    }

    /**
     * 挂单成功到全部成交
     */
    private static OrderDetail 现货_挂单_卖_SUCCESS(String baseAsset, String quoteAsset, BigDecimal quantity) {

        String symbolSell = baseAsset + quoteAsset;
        BinanceExchangeInfoCache.ExchangeInfo exchangeInfo = EXCHANGE_INFO_CACHE.get(symbolSell);
        quantity = quantity.setScale(exchangeInfo.getStepSizePrecision(), RoundingMode.DOWN);
        JsonObject resSellJson;
        BigDecimal executedSellQty = BigDecimal.ZERO;
        BigDecimal executedSellU = BigDecimal.ZERO;
        {
            BigDecimal baseSellFreeAmount = BigDecimal.ZERO;
            {
                String dataJsonString = sendHttp(Method.GET, "/api/v3/account", Map.of("omitZeroBalances", "true"));
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
                    现货_挂单_卖 = 现货_挂单_卖(symbolSell, subtract);
                }
                String res = sendHttp(Method.GET, "/api/v3/order", Map.of("symbol", symbolSell, "origClientOrderId", 现货_挂单_卖));
                resSellJson = Constants.GSON.fromJson(res, JsonObject.class);
                if (StringUtils.equalsIgnoreCase(JsonObjectTool.getAsString(resSellJson, "status"), "FILLED")) {
                    executedSellQty = executedSellQty.add(JsonObjectTool.getAsBigDecimal(resSellJson, "executedQty"));
                    executedSellU = executedSellU.add(JsonObjectTool.getAsBigDecimal(resSellJson, "cummulativeQuoteQty"));
                    break;
                } else {
                    if (tryCount++ < 10) {
                        TimeTool.sleep(300);
                    } else {
                        BigDecimal spotBestPrice = getSpotBestSellPrice(JsonObjectTool.getAsString(resSellJson, "symbol"));
                        if (spotBestPrice.compareTo(JsonObjectTool.getAsBigDecimal(resSellJson, "price")) >= 0) {
                            tryCount = 0;
                            continue;
                        }
                        String delete = sendHttp(Method.DELETE, "/api/v3/order", Map.of("symbol", symbolSell, "origClientOrderId", 现货_挂单_卖));
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
        return OrderDetail.builder().symbol(symbolSell).executedQty(executedSellQty).executedU(executedSellU).build();
    }
    private static OrderDetail 现货_挂单_买_SUCCESS(String baseAsset, String quoteAsset, BigDecimal amountUsdt) {

        String symbolBuy = baseAsset + quoteAsset;
        BinanceExchangeInfoCache.ExchangeInfo exchangeInfo = EXCHANGE_INFO_CACHE.get(symbolBuy);
        JsonObject resBuyJson;
        BigDecimal executedBuyQty = BigDecimal.ZERO;
        BigDecimal executedBuyU = BigDecimal.ZERO;
        {
            BigDecimal fdusdFreeAmount = BigDecimal.ZERO;
            {
                String dataJsonString = sendHttp(Method.GET, "/api/v3/account", Map.of("omitZeroBalances", "true"));
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
                    现货_挂单_买 = 现货_挂单_买(symbolBuy, subtract);
                }
                String res = sendHttp(Method.GET, "/api/v3/order", Map.of("symbol", symbolBuy, "origClientOrderId", 现货_挂单_买));
                resBuyJson = Constants.GSON.fromJson(res, JsonObject.class);
                if (StringUtils.equalsIgnoreCase(JsonObjectTool.getAsString(resBuyJson, "status"), "FILLED")) {
                    executedBuyQty = executedBuyQty.add(JsonObjectTool.getAsBigDecimal(resBuyJson, "executedQty"));
                    executedBuyU = executedBuyU.add(JsonObjectTool.getAsBigDecimal(resBuyJson, "cummulativeQuoteQty"));
                    break;
                } else {
                    if (tryCount2++ < 10) {
                        TimeTool.sleep(300);
                    } else {
                        BigDecimal spotBestPrice = getSpotBestBuyPrice(JsonObjectTool.getAsString(resBuyJson, "symbol"));
                        if (spotBestPrice.compareTo(JsonObjectTool.getAsBigDecimal(resBuyJson, "price")) <= 0) {
                            tryCount2 = 0;
                            continue;
                        }
                        String delete = sendHttp(Method.DELETE, "/api/v3/order", Map.of("symbol", symbolBuy, "origClientOrderId", 现货_挂单_买));
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
        return OrderDetail.builder().symbol(symbolBuy).executedQty(executedBuyQty).executedU(executedBuyU).build();
    }

    /**
     * 挂单卖出
     */
    private static String 现货_挂单_卖(String symbol, BigDecimal quantity) {
        return 现货_挂单(symbol, "SELL", null, quantity);
    }

    /**
     * 挂单买入
     */
    private static String 现货_挂单_买(String symbol, BigDecimal amountUsdt) {
        return 现货_挂单(symbol, "BUY", amountUsdt, null);
    }

    /**
     * 币安挂单
     */
    private static String 现货_挂单(String symbol, String side, BigDecimal amountUsdt, BigDecimal quantity) {
        // 获取价格
        String clientId = UUID.randomUUID().toString().replaceAll("-", "");
        BinanceExchangeInfoCache.ExchangeInfo exchangeInfo = EXCHANGE_INFO_CACHE.get(symbol);
        if (Objects.isNull(exchangeInfo)) ErrorCodeEnum.throwException("exchangeInfo is null");
        while (true) {
            BigDecimal spotBestPrice = null;
            if (StringUtils.equalsIgnoreCase(side, "BUY")) {
                spotBestPrice = getSpotBestBuyPrice(symbol);
            } else if (StringUtils.equalsIgnoreCase(side, "SELL")) {
                spotBestPrice = getSpotBestSellPrice(symbol);
            } else {
                ErrorCodeEnum.throwException("side参数错误");
            }
            if (Objects.isNull(spotBestPrice) || spotBestPrice.compareTo(BigDecimal.ZERO) <= 0)
                ErrorCodeEnum.throwException("获取价格失败");
            if (Objects.isNull(quantity)) {
                quantity = amountUsdt.divide(spotBestPrice, exchangeInfo.getStepSizePrecision(), RoundingMode.DOWN);
            }
            String res = sendHttp(Method.POST, "/api/v3/order", Map.of("symbol", symbol, "side", side, "type", "LIMIT_MAKER", "quantity", quantity.stripTrailingZeros().toPlainString(), "price", spotBestPrice.toPlainString(), "newClientOrderId", clientId));
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

    private static BigDecimal getSpotBestBuyPrice(String symbol) {
        JsonObject bestBookTicker = getSpotBestBookTicker(symbol);
        return JsonObjectTool.getAsBigDecimal(bestBookTicker, "bidPrice"); // 最优买单价

    }

    private static BigDecimal getSpotBestSellPrice(String symbol) {
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


}

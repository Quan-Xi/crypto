package com.flow.binance;

import cn.hutool.http.Method;
import com.flow.exception.ErrorCodeEnum;
import com.flow.tool.Constants;
import com.flow.tool.JsonObjectTool;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


public class LocalTest {
    private static final Logger log = LoggerFactory.getLogger(LocalTest.class);

    static class Monitor2 {
        public static void main(String[] args) {
            SpotService.spotBalanceMonitoring(30_000);
        }
    }

    /**
     * 兑换程序
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
            SpotService.swap(sell, buy, new BigDecimal(sellAmount));
        }
    }

    /**
     * BUY/SELL直到成功
     */
    static class SellOrBuy {
        public static void main(String[] args) {

            {
                SpotMarkerOrderDetail orderDetailSell = SpotService.markerOrderSellSuccess("DOGE", "FDUSD", BigDecimal.valueOf(38));
                System.out.println("\n\n");
                String sellStr = "SELL: " + orderDetailSell.getSymbol()  + ", 卖出价格：" + orderDetailSell.getExecutedU().divide(orderDetailSell.getExecutedQty(), 8, RoundingMode.DOWN) + ", 卖出数量：" + orderDetailSell.getExecutedQty() + ", 获取U: " + orderDetailSell.getExecutedU();
                System.out.println(sellStr);
            }
            {
                SpotMarkerOrderDetail orderDetailBuy = SpotService.markerOrderBuySuccess("DOGE", "FDUSD", BigDecimal.valueOf(11111111));
                System.out.println("\n\n");
                String buyStr = "BUY: " + orderDetailBuy.getSymbol()  + ", 买入价格：" + orderDetailBuy.getExecutedU().divide(orderDetailBuy.getExecutedQty(), 8, RoundingMode.DOWN) + ", 买出数量：" + orderDetailBuy.getExecutedQty() + ", 花费U: " + orderDetailBuy.getExecutedU();
                System.out.println(buyStr);
            }

            System.out.println("\n🎉🎉🎉完成兑换SUCCESS 🎉🎉🎉");

        }
    }

    /**
     * 分多笔执行：BUY/SELL直到成功
     */
    static class SellOrBuyBatch {
        public static void main(String[] args) {
            // 查询现货的代币信息
            System.out.println("------ Binance资产信息 ------");
            {
                String dataJsonString = SpotHttpService.sendHttp(Method.GET, "/api/v3/account", Map.of("omitZeroBalances", "true"));
                JsonObject jsonObject = Constants.GSON.fromJson(dataJsonString, JsonObject.class);
                JsonArray asJsonArray = JsonObjectTool.getAsJsonArray(jsonObject, "balances");
                for (JsonElement jsonElement : asJsonArray) {
                    JsonObject asJsonObject = jsonElement.getAsJsonObject();
                    String asset = JsonObjectTool.getAsString(asJsonObject, "asset");
                    BigDecimal free = JsonObjectTool.getAsBigDecimal(asJsonObject, "free");
                    BigDecimal locked = JsonObjectTool.getAsBigDecimal(asJsonObject, "locked");
                    System.out.println(asset + ": free:" + free + ", locked:" + locked);
                }
            }
            Scanner scanner = new Scanner(System.in);
            String buyOrSell;
            while (true) {
                System.out.print("------ 执行程序 ------\n1: BUY\n2: SELL\n请输入: ");
                buyOrSell = scanner.nextLine();
                if (!StringUtils.equalsAny(buyOrSell, "1", "2")) {
                    System.out.println("输入有误，请重试");
                    continue;
                }
                if (StringUtils.equals(buyOrSell, "1")) {
                    buyOrSell = "BUY";
                }else {
                    buyOrSell = "SELL";
                }
                break;
            }
            String baseAsset;
            String quoteAsset;
            while (true) {
                System.out.print("\n交易对信息[ETHFDUSD]：");
                String symbol = scanner.nextLine().toUpperCase().replaceAll(" ", "");
                if (StringUtils.endsWithIgnoreCase(symbol, "FDUSD")) {
                    baseAsset = symbol.replaceAll("FDUSD", "");
                    quoteAsset = "FDUSD";
                }else if (StringUtils.endsWithIgnoreCase(symbol, "USDT")) {
                    baseAsset = symbol.replaceAll("USDT", "");
                    quoteAsset = "USDT";
                }else if (StringUtils.endsWithIgnoreCase(symbol, "USDC")) {
                    baseAsset = symbol.replaceAll("USDC", "");
                    quoteAsset = "USDC";
                }else {
                    System.out.println("输入有误，请重试");
                    continue;
                }
                break;
            }

            BigDecimal amountOnce;
            while (true) {
                System.out.print("\n每次" + buyOrSell + "数量：");
                String opsAmount = scanner.nextLine().toUpperCase().replaceAll(" ", "");
                try {
                    amountOnce = new BigDecimal(opsAmount);
                }catch (Exception e){
                    System.out.println("输入有误，请重试");
                    continue;
                }
                break;
            }
            Integer orderCount;
            while (true) {
                System.out.print("\n" + buyOrSell + "次数：");
                String opsCount = scanner.nextLine().toUpperCase().replaceAll(" ", "");
                try {
                    orderCount = Integer.parseInt(opsCount);
                }catch (Exception e){
                    System.out.println("输入有误，请重试");
                    continue;
                }
                break;
            }
            {
                System.out.println("----------------- 全部信息 -----------------");
                System.out.println("操作程序：" + buyOrSell);
                System.out.println("交易对信息：" + baseAsset + "/" + quoteAsset);
                System.out.println("每次" + buyOrSell + "数量：" + amountOnce);
                System.out.println(buyOrSell + "次数：" + orderCount);
                System.out.println("以上信息正确吗? \n确认: YES\n取消: 任意\n--------------------\n请输入: ");
                String key = scanner.nextLine();
                if (!StringUtils.equalsAnyIgnoreCase(key, "YES", "Y")) {
                    return;
                }
            }
            System.out.println("开始执行 >>>>");
            List<SpotMarkerOrderDetail> orderDetailList = new ArrayList<>();
            for (int i = 0; i < orderCount; i++) {
                if (StringUtils.equalsIgnoreCase(buyOrSell, "BUY")) {
                    SpotMarkerOrderDetail orderDetailSell = SpotService.markerOrderSellSuccess(baseAsset, quoteAsset, amountOnce);
                    orderDetailList.add(orderDetailSell);
                }else if (StringUtils.equalsIgnoreCase(buyOrSell, "SELL")) {
                    SpotMarkerOrderDetail orderDetailBuy = SpotService.markerOrderBuySuccess(baseAsset, quoteAsset, amountOnce);
                    orderDetailList.add(orderDetailBuy);
                }
                System.out.println("第" + (i + 1) + "笔执行成功🎉🎉🎉, 是否继续");
                scanner.nextLine();
            }
            int index = 1;
            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal totalAmountU = BigDecimal.ZERO;
            for (SpotMarkerOrderDetail orderDetailSell : orderDetailList) {
                String sellStr =  index + "：" +buyOrSell + "[" + orderDetailSell.getSymbol() + "]"  +
                        ", 价格：" + orderDetailSell.getExecutedU().divide(orderDetailSell.getExecutedQty(), 8, RoundingMode.DOWN) +
                        ", 数量：" + orderDetailSell.getExecutedQty() +
                        ", 得到: " + orderDetailSell.getExecutedU();
                System.out.println(sellStr);
                index ++;
            }
            System.out.println("-------- 汇总 --------\nbase:"  + totalAmount + ", quote:" + totalAmountU + ", 价格：" + totalAmountU.divide(totalAmount, 8, RoundingMode.DOWN));
            System.out.println("\n🎉🎉🎉完成兑换SUCCESS 🎉🎉🎉");

        }
    }

    /**
     * 批量挂买单 BUY
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
            SpotService.markerBuyOrderBatch(symbol, orderAmount, orderIntervalRate, startPrice, endPrice);
        }
    }

    /**
     * 批量挂卖单 SELL
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
            SpotService.markerSellOrderBatch(symbol, orderAmount, orderIntervalRate, startPrice, endPrice);
        }
    }

}

package com.flow.binance;

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
            SpotService.swap(sell, buy, new BigDecimal(sellAmount));
        }
    }

    /**
     * 手动买卖程序
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
            SpotService.markerBuyOrderBatch(symbol, orderAmount, orderIntervalRate, startPrice, endPrice);
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
            SpotService.markerSellOrderBatch(symbol, orderAmount, orderIntervalRate, startPrice, endPrice);
        }
    }

}

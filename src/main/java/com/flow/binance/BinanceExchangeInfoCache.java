package com.flow.binance;

import cn.hutool.core.util.ObjectUtil;
import com.flow.common.ApplicationContextTool;
import com.flow.tool.Constants;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class BinanceExchangeInfoCache {

    public static ExchangeInfo getExchangeInfoBySymbol(String symbol) {

        ExchangeInfo exchangeInfo = EXCHANGE_INFO_CACHE.get(symbol);
        log.info("symbol:{} spot exchangeInfo:{}",symbol, Constants.GSON.toJson(exchangeInfo));
        if (Objects.isNull(exchangeInfo)) {
            ApplicationContextTool.getBean("exchangeInfoMonitor", ExchangeInfoMonitor.class).monitorWithoutLock();
            exchangeInfo = EXCHANGE_INFO_CACHE.get(symbol);
        }
        return exchangeInfo;
    }

    public static void updateSymbol(String symbol, ExchangeInfo exchangeInfo) {
        EXCHANGE_INFO_CACHE.put(symbol, exchangeInfo);
    }

    public static ExchangeInfo getExchangeInfo(String symbol, StrategyTypeEnum strategyType) {
        if (ObjectUtil.equals(StrategyTypeEnum.SPOT,strategyType)) return EXCHANGE_INFO_CACHE.get(symbol);
        return USDT_FUTURE_EXCHANGE_INFO_CACHE.get(symbol);
    }

    public static ExchangeInfo getFutureExchangeInfoBySymbol(String symbol) {

        ExchangeInfo exchangeInfo = USDT_FUTURE_EXCHANGE_INFO_CACHE.get(symbol);
        log.info("symbol:{} future exchangeInfo:{}",symbol, Constants.GSON.toJson(exchangeInfo));
        if (Objects.isNull(exchangeInfo)) {
            ApplicationContextTool.getBean("exchangeInfoMonitor", ExchangeInfoMonitor.class).futureMonitorWithoutLock();
            exchangeInfo = USDT_FUTURE_EXCHANGE_INFO_CACHE.get(symbol);
        }
        return exchangeInfo;
    }

    public static void updateFutureSymbol(String symbol, ExchangeInfo exchangeInfo) {
        USDT_FUTURE_EXCHANGE_INFO_CACHE.put(symbol, exchangeInfo);
    }

    public static final ConcurrentHashMap<String, ExchangeInfo> EXCHANGE_INFO_CACHE = new ConcurrentHashMap<>(2500);

    public static final ConcurrentHashMap<String, ExchangeInfo> USDT_FUTURE_EXCHANGE_INFO_CACHE = new ConcurrentHashMap<>(500);

    @Data
    public static class ExchangeInfo {
        /**
         * 交易对
         */
        private String symbol;
        /**
         * 交易对前缀代币
         * 例: BTCUSDT -> BTC
         */
        private String baseAsset;

        /**
         * 交易对后缀代币
         * 例: BTCUSDT -> USDT
         */
        private String quoteAsset;

        /**
         * 保证金资产
         * 合约交易对信息专用
         */
        private String marginAsset;

        /**
         * 价格小数点位数
         * 合约交易对信息专用
         */
        private Integer pricePrecision;

        /**
         * 数量小数点位数
         * 合约交易对信息专用
         */
        private Integer quantityPrecision;
        /**
         * 交易对前缀代币的精度
         */
        private Integer baseAssetPrecision;

        /**
         * 交易对后缀代币的精度
         */
        private Integer quoteAssetPrecision;

        // PRICE_FILTER
        /**
         * 下单的最小价格
         */
        private BigDecimal minPrice;
        /**
         * 下单的最大价格
         */
        private BigDecimal maxPrice;
        /**
         * 价格的最小波动大小
         * 即price = minPrice + (n * tickSize);
         */
        private BigDecimal tickSize;
        /**
         * tickSize的保留的小数位数 价格
         * 即: 0.001 --> 3, 0.000001  --> 6
         */
        private Integer tickSizePrecision;

        // LOT_SIZE
        /**
         * 下单的最小数量
         */
        private BigDecimal minQty;
        /**
         * 下单的最大数量
         */
        private BigDecimal maxQty;

        /**
         * 市价单 数量上限
         * 合约交易对信息专用
         */
        private BigDecimal marketMaxQty;

        /**
         * 市价单数量下限
         * 合约交易对信息专属
         */
        private BigDecimal marketMinQty;
        /**
         * 数量的最小波动大小
         * 即: Qty = minQty + (stepSize * n);
         */
        private BigDecimal stepSize;

        /**
         * 市价单数量的最小波动大小
         * 即: Qty = minQty + (stepSize * n);
         * 合约交易对信息专属
         */
        private BigDecimal marketStepSize;
        /**
         * stepSize的保留的小数位数 数量
         * 即: 0.001 --> 3, 0.000001  --> 6
         */
        private Integer stepSizePrecision;

        /**
         * 市价单stepSize的保留的小数位数
         * 即: 0.001 --> 3, 0.000001  --> 6
         * 合约交易对信息专属
         */
        private Integer marketStepSizePrecision;

        // MIN_NOTIONAL
        /**
         * 下单的细小金额
         * 总成交额 = 价格 * 数量
         * 例如: 10U
         */
        private BigDecimal minNotional;

        /**
         * 是否应用到市价单
         */
        private Boolean applyToMarket;

        /**
         * 是否支持quoteOrderQty参数
         */
        private Boolean quoteOrderQtyMarketAllowed;

        /**
         * 强平费率
         */
        private BigDecimal liquidationFee;

    }
}

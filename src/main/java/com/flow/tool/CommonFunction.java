package com.flow.tool;

import cn.hutool.core.codec.Base58;
import com.flow.common.ApplicationContextTool;
import com.flow.tool.crypto.Bech32;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.web3j.utils.Numeric;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @Author wangqiyun
 * @Date 2018/12/25 2:14 PM
 */
public class CommonFunction {

    public static String CARDANO_REGEX = "^(([0-9A-Za-z]{57,59})|([0-9A-Za-z]{100,104}))$";

    public static Map<String, Object> paramMapToMap(Map<String, String[]> map) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue()[0]);
        }
        return result;
    }

    public static Map<String, String> paramMapToStringMap(Map<String, String[]> map) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue()[0]);
        }
        return result;
    }

    /**
     * 将数字拆分成n等份，用于本金以及利息的拆分
     * n必须大于0
     */
    public static List<Long> divide(long number, int n) {
        List<Long> result = new ArrayList<>(n);
        if (number <= 0L) {
            for (int i = 0; i < n; i++)
                result.add(0L);
            return result;
        }
        long base = number / n, num = number - n * base;
        for (int i = 0; i < n; i++)
            if (i < num)
                result.add(base + 1);
            else result.add(base);
        return result;
    }

    public static long roundUp(Double num) {
        long value = num.longValue();
        if (num > value) return value + 1L;
        else return value;
    }


    public static long generalId() {
        return generalId(System.currentTimeMillis());
    }

    private static final String CLOCK_PREFIX = "ID_CLOCK_";

    public static long generalId(long time) {
        RedisTemplate<String, Object> redisTemplate = ApplicationContextTool.getBean("redisTemplate", RedisTemplate.class);
        if (Objects.isNull(redisTemplate)){
            return (time << 20) | (ThreadLocalRandom.current().nextLong(0, 1 << 20));
        }else {
            BoundValueOperations<String, Object> valueOperations = redisTemplate.boundValueOps(CLOCK_PREFIX + time);
            valueOperations.setIfAbsent(1, 5L, TimeUnit.SECONDS);
            Long nextId = valueOperations.increment();
            if (Objects.isNull(nextId)) nextId = ThreadLocalRandom.current().nextLong(0, 1 << 20);
            return (time << 20) | nextId;
        }
    }

    public static long generalMinId(long time) {
        return (time << 20);
    }

    public static long generalMaxId(long time) {
        return (time << 20) | ((1 << 20) - 1);
    }

    public static String generalSn(long id) {
        long timestamp = id >> 20;
        return Constants.dateTimeFormatterFraction.format(Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault())) + String.format("%07d", id & 1048575L);
    }

    public static boolean bitcoinAddress(String... addresses) {
        for (String address : addresses) {
            if (StringUtils.isEmpty(address)) return false;
            if (address.startsWith("1") || address.startsWith("3")) {
                if (Base58.decodeChecked(address) == null) return false;
            } else if (address.startsWith("bc1q")) {
                if (Bech32.SegwitAddrDecode(address) == null) return false;
            } else return address.startsWith("bc1p");
        }
        return true;
    }

    public static boolean ethAddress(String... addresses) {
        try {
            for (String address : addresses) {
                if (StringUtils.isEmpty(address)) return false;
                if (!address.startsWith("0x")) return false;
                if (address.length() != 42) return false;
                Numeric.toBigInt(address);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean trxAddress(String... addresses){
        for (String address : addresses) {
            if(StringUtils.isEmpty(address)) return false;
            if(!address.startsWith("T")) return false;
            if(Base58.decode(address) == null) return false;
        }
        return true;
    }

    public static boolean aptAddress(String address) {
        if(StringUtils.isNotBlank(address) && address.startsWith("0x")) {
            try {
                Numeric.toBigInt(address);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
}

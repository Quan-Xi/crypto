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


public class BinanceApiConstants {
    public static final String BASE_URL = "https://api.binance.com";
    public static final String API_HTTP_HEADER_KEY = "X-MBX-APIKEY";
    public static String BIAN_API_KEY = "1eVvdc3IEjtVc7jlhs57ghpgqugxHHcpSgk6KgOx7k5BQUq7Z2bbEyIkSkvhQ8su";
    public static String BIAN_SECRET_KEY = "UgcLYE6d7kizdf29nqm6thzR232wePO6BwEdFk3QRiBg8HVpngPkJAX56d2wFNht";
}

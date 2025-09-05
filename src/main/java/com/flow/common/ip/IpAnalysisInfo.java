package com.flow.common.ip;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IpAnalysisInfo {
    // 错误码, 当请求异常的时候会有code值
    private String code;
    // AS
    private String continent_code;
    // China
    private String country;
    // Zhejiang
    private String region;
    // Cixi
    private String city;
    // 121.241
    private String longitude;
    // 30.176
    private String latitude;
    // Chinanet
    private String organization;
    // Asia\/Shanghai
    private String timezone;
}
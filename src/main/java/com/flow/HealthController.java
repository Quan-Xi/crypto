package com.flow;

import com.flow.exception.Result;
import com.flow.tool.MapTool;
import com.flow.tool.TimeTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangqiyun
 * @since 2020/8/14 17:59
 */
@RestController
public class HealthController {

    @RequestMapping("/ping")
    public Result ping() {
        return Result.instance();
    }

    private final static String time = TimeTool.getNowDateTimeSSSDisplayString();

    @Value("${release.version:1.0.0}")
    private String version;

    @RequestMapping("/version")
    public Result version() {
        return Result.success(MapTool.Map()
                .put("version", version)
                .put("time", time)
        );
    }

}

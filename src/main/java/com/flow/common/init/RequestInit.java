package com.flow.common.init;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class RequestInit {
    private LocalDateTime now = LocalDateTime.now();
    private Long uid;
    private boolean admin;
    private String requestId = UUID.randomUUID().toString();
    private String ip = "";
}

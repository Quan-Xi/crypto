package com.flow.tool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Constants {

    public static final Executor VIRTUAL_THREAD_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    // GSON
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();


    public static ExecutorService ORDERLY_EXECUTOR_SERVICE = new ThreadPoolExecutor(1, 8, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>(64), new ThreadFactory() {
        AtomicInteger threadId = new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("Orderly_Thread-"+threadId.getAndIncrement());
            return thread;
        }
    }, new ThreadPoolExecutor.CallerRunsPolicy());

    public static final DateTimeFormatter dateTimeFormatterFraction = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    public static final DateTimeFormatter standardDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

}

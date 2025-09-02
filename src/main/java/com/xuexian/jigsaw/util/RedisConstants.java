package com.xuexian.jigsaw.util;

import java.util.Locale;

public class RedisConstants {

    public static final Long CACHE_NULL_TTL = 2L;

    public static final String CURRENT_KEY = "jigsaw:%d:user:%d:current";
    public static final String HISTORY_KEY = "jigsaw:%d:user:%d:history";
    public static final String INITIAL_KEY = "jigsaw:%d:user:%d:initial";



    public static final Long LOCK_SHOP_TTL = 10L;


}

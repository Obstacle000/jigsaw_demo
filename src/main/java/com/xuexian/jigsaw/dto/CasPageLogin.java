package com.xuexian.jigsaw.dto;


import com.xuexian.jigsaw.util.HttpUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CasPageLogin {
    /**
     * 统一认证登入网页
     */
    private static final String CAS_LOGIN_SERVER = "https://pass.sdu.edu.cn/cas/login";

    /**
     * 统一认证ticket校验网页
     */
    private static final String CAS_VALIDATE_SERVER = "https://pass.sdu.edu.cn/cas/serviceValidate";

    /**
     * 本地接口
     */
    public static final String LOCAL_API = "https://i.sdu.edu.cn/cas/proxy/login/page";

    /**
     * 默认跳转
     */
    public static final String DEFAULT_FORWARD = "拼图游戏首页";

    /**
     * 执行cas登入，返回结果
     */
    @SneakyThrows
    public static CasLoginResult login(String ticket, String forward) {
        if (forward == null) forward = DEFAULT_FORWARD;
        // url编码
        String forwardServerEncoder = URLEncoder.encode(forward, StandardCharsets.UTF_8);
        String localServerEncoder = URLEncoder.encode(LOCAL_API + "?forward=" + forwardServerEncoder, StandardCharsets.UTF_8);

        if (ticket == null) {
            // ticket不存在
            return new CasLoginResult(false, CAS_LOGIN_SERVER + "?service=" + localServerEncoder, null);
        }

        // 检查ticket有效性
        Document xmlDoc = HttpUtil.connect(CAS_VALIDATE_SERVER + "?ticket=" + ticket + "&service=" + localServerEncoder).execute().parse();
        Elements successList = xmlDoc.getElementsByTag("sso:authenticationSuccess");
        if (successList.size() == 1) {
            // ticket有效，获取user信息
            String casID = xmlDoc.getElementsByTag("sso:user").get(0).text();
            return new CasLoginResult(true, forward, casID);
        } else {
            // ticket无效，重新进入验证界面
            return new CasLoginResult(false, CAS_LOGIN_SERVER + "?service=" + localServerEncoder, null);
        }
    }

    @AllArgsConstructor
    public static class CasLoginResult {
        /**
         * 是否检查通过
         */
        @Getter
        private boolean validate;

        /**
         * 跳转方向
         */
        public String redirect;

        /**
         * 检查通过后得到的userId
         */
        public String casId;

    }

}
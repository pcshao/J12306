package com.kalvin.J12306.api;

import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.kalvin.J12306.config.UrlsEnum;
import com.kalvin.J12306.dto.UserInfoDTO;
import com.kalvin.J12306.http.Session;

import java.util.HashMap;
import java.util.Scanner;

/**
 * 登录 202601适用
 * @author pcshao.cn
 * @date 20260125
 */
public class Login202601 extends Login {

    private static final Log log = LogFactory.get();
    private String idCardLast4;

    public Login202601(Session session, String username, String password, String idCardLast4) {
        super(session, username, password);
        // 设置身份证后4位用于获取验证码
        this.idCardLast4 = idCardLast4;
        // 设置爬取模式
        this.session.setSpiderMode(Session.MODE_202601);
    }

    @Override
    public UserInfoDTO send() {
        // 2. 登录前检查 checkLoginVerify
        HashMap<String, Object> formData = new HashMap<>();
        formData.put("username", this.username);
        formData.put("appid", "otn");
        formData.put("_json_att", "");
        HttpResponse initRes = this.session.httpClient.send(UrlsEnum.LOGIN_VERIFY, formData);
        this.session.setCookie(initRes.getCookies());
        log.info("进入12306登录页，状态码：{}", initRes.getStatus());
        // 3. 获取手机验证码
        String idCardLast4 = this.idCardLast4;
        formData.put("castNum", idCardLast4);
        HttpResponse repMessageCode = this.session.httpClient.send(UrlsEnum.LOGIN_MESSAGE_CODE, formData);
        // 4. 登录
        // 外部输入手机验证码
        String messageCode = "";
        messageCode = inputMessageCode();
        formData.put("randCode", messageCode);
        formData.put("sessionId", "");
        formData.put("checkMode", "0");
        formData.put("password", this.password);    // 密文密码
        HttpResponse repLogin = this.session.httpClient.send(UrlsEnum.LOGIN_202601, formData);
        String uamtk = repLogin.getCookieValue("uamtk");
        // 5. 登录uamtk
        formData.clear();
        formData.put("appid", "otn");
        formData.put("_json_att", "");
        HttpResponse reqUamtk = this.session.httpClient.send(UrlsEnum.UAM_TK, formData);
        JSONObject jsonObject = JSONUtil.parseObj(reqUamtk.body());
        String newapptk = jsonObject.getStr("newapptk");
        this.session.token = newapptk;
        // tk 设入cookie
        this.session.setCookie("tk=" + newapptk);
        return this.getUserInfo();
    }

    private String inputMessageCode() {
        System.out.println("请输入验证码后按回车处理：");
        Scanner scanner = new Scanner(System.in);
        return scanner.next().trim();
    }


}

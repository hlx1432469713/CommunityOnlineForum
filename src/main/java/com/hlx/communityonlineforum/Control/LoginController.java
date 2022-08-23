package com.hlx.communityonlineforum.Control;

import com.google.code.kaptcha.Producer;
import com.hlx.communityonlineforum.Entity.User;
import com.hlx.communityonlineforum.Service.UserService;
import com.hlx.communityonlineforum.Until.CommunityOnlineForumConstant;
import com.hlx.communityonlineforum.Until.CommunityUtil;
import com.hlx.communityonlineforum.Until.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityOnlineForumConstant {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer producer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 返回注册页面
     * @return
     */
    @RequestMapping(path = "/register" , method = RequestMethod.GET)
    public String getRegister(){
        return "/site/register";
    }

    /**
     * 返回登录页面
     * @return
     */
    @RequestMapping(path = "/login" , method = RequestMethod.GET)
    public String getLoginr(){
        return "/site/login";
    }

    /**
     * 注册新用户
     * @param model
     * @param user
     * @return
     */
    @RequestMapping(path = "/registerUser", method = RequestMethod.POST)
    public String registerUser(Model model, User user){
        Map<String,Object> map = userService.register(user);
        if (map == null || map.isEmpty()){
            model.addAttribute("msg", "社区在线交流平台用户账号注册成功,我们已经向您的邮箱发送了一封激活邮件,请尽快激活!");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        }else{
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    /**
     * 激活新注册用户的账号
     * @param model
     * @param userId
     * @param code
     * @return
     */
    @RequestMapping(path = "activationUser/{userId}/{code}" , method = RequestMethod.GET)
    public String activationUser(Model model, @PathVariable("userId") int userId,@PathVariable("code") String code){
        int result = userService.activation(userId,code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "用户账号激活成功,您的用户账号已经可以正常使用了!");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作,该用户账号已经激活过了!");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "用户账号激活失败,您提供的激活码不正确!");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    /**
     * 动态生成登录页面的验证码
     * @param httpResponse
     */
    @RequestMapping(path = "/kaptcha" , method = RequestMethod.GET)
    public void getKaptchaImg(HttpServletResponse httpResponse){
        // 生成验证码
        String imgText = producer.createText();
        BufferedImage image = producer.createImage(imgText);

        // 验证码的归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        httpResponse.addCookie(cookie);
        // 将验证码存入Redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, imgText, 60, TimeUnit.SECONDS);

        httpResponse.setContentType("image/png");
        try {
            OutputStream os = httpResponse.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            logger.error("响应验证码失败:" + e.getMessage());
        }
    }

    /**
     * 用户登录
     * @param username : 用户名称
     * @param password ： 用户登录密码
     * @param code ： 登录页面验证码
     * @param rememberme ： 是否勾选“记住我”
     * @param model
     * @param response
     * @return
     */
    @RequestMapping(path = "/loginUser", method = RequestMethod.POST)
    public String loginUser(String username, String password, String code, boolean rememberme, Model model,
                            HttpServletResponse response, @CookieValue("kaptchaOwner") String kaptchaOwner){

        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确!");
            return "/site/login";
        }
        //登录凭证的超时时间
        long expiredSeconds = rememberme ? (long)REMEMBER_EXPIRED_SECONDS : (long)DEFAULT_EXPIRED_SECONDS;
        Map<String,Object> map = userService.loginUser(username,password,expiredSeconds);
        if (map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge((int)expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    /**
     * 用户退出登录
     * @param ticket
     * @return
     */
    @RequestMapping(path = "/logoutUser", method = RequestMethod.GET)
    public String logoutUser(@CookieValue("ticket")String ticket){
        userService.logout(ticket);
        return "redirect:/login";
    }
}

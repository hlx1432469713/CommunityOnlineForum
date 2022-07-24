package com.hlx.communityonlineforum.Control;

import com.google.code.kaptcha.Producer;
import com.hlx.communityonlineforum.Entity.User;
import com.hlx.communityonlineforum.Service.UserService;
import com.hlx.communityonlineforum.Until.CommunityOnlineForumConstant;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
public class LoginController implements CommunityOnlineForumConstant {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer producer;

    @RequestMapping(path = "/register" , method = RequestMethod.GET)
    public String getRegister(){
        return "/site/register";
    }

    @RequestMapping(path = "/login" , method = RequestMethod.GET)
    public String getLoginr(){
        return "/site/login";
    }

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

    @RequestMapping(path = "/kaptcha" , method = RequestMethod.GET)
    public void getKaptchaImg(HttpServletResponse httpResponse, HttpSession httpSession){
        // 生成验证码
        String imgText = producer.createText();
        BufferedImage image = producer.createImage(imgText);

        // 将验证码存入session--在后面的登录验证请求中，可以获得该内容
        httpSession.setAttribute("kaptcha", imgText);

        httpResponse.setContentType("image/png");
        try {
            OutputStream os = httpResponse.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            logger.error("响应验证码失败:" + e.getMessage());
        }
    }
}

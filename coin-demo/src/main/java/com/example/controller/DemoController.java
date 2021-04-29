package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: ljj
 * @Date: 2021/4/28
 * @Version: 1.0
 * @Description:
 */
@RestController
public class DemoController {

    @Autowired
    private JavaMailSender jMailSender;

    @Value("${spring.mail.username}")
    private String from;
    @Value("${spark.system.host}")
    private String host;
    @Value("${spark.system.name}")
    private String company;

    @Value("${spark.system.admins}")
    private String admins;

    @Value("${spark.system.admin-phones}")
    private String adminPhones;

    @RequestMapping("/test")
    public void test(){
        String[] adminList = admins.split(",");
        for (int i = 0; i < adminList.length; i++) {
            sendEmailMsg(adminList[i], "msg", "[服务]服务下线通知");
        }

    }

    @Async
    public void sendEmailMsg(String email, String msg, String subject) {
        try {
            MimeMessage mimeMessage = jMailSender.createMimeMessage();
            MimeMessageHelper helper = null;
            helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(from);
            helper.setTo(email);
            helper.setSubject(company + "-" + subject);
            Map<String, Object> model = new HashMap<>(16);
            model.put("msg", msg);
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
            cfg.setClassForTemplateLoading(this.getClass(), "/templates");
            Template template = cfg.getTemplate("message.ftl");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            helper.setText(html, true);

            //发送邮件
            jMailSender.send(mimeMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

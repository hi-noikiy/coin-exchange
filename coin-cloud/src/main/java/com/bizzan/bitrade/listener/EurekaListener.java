package com.bizzan.bitrade.listener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.mail.internet.MimeMessage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.eureka.server.event.*;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.netflix.appinfo.InstanceInfo;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * @Author: Killah
 * @Version: 1.0
 */
@Slf4j
@Component
public class EurekaListener {

    @Autowired
    private JavaMailSender jMailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${system.company.name}")
    private String company;

    @Value("${system.notice.admin-mails}")
    private String adminMails;

    @Value("${system.notice.admin-mails}")
    private String adminPhones;

    /**
     * 服务下线事件
     * @param event
     */
    @EventListener(condition = "#event.replication==false")
    public void listen(EurekaInstanceCanceledEvent event) {

        String msg = "您的服务【" + event.getAppName() + "\n" + event.getServerId() + "已下线";
        log.info(msg);

        String[] adminList = adminMails.split(",");
        for (int i = 0; i < adminList.length; i++) {
            sendEmailMsg(adminList[i], msg, "[服务]服务下线通知");
        }
    }

    /**
     * 服务注册事件
     * @param event
     */
    @EventListener(condition = "#event.replication==false")
    public void listen(EurekaInstanceRegisteredEvent event) {

        InstanceInfo instanceInfo = event.getInstanceInfo();
        String msg = "服务" + instanceInfo.getAppName() + "\n" + instanceInfo.getHostName() + ":" + instanceInfo.getPort() + " \nip: " + instanceInfo.getIPAddr() + "进行注册";
        log.info(msg);

        String[] adminList = adminMails.split(",");
        for (int i = 0; i < adminList.length; i++) {
            sendEmailMsg(adminList[i], msg, "[服务]服务上线通知");
        }
    }

    /**
     * 服务续约事件
     * @param event
     */
    @EventListener
    public void listen(EurekaInstanceRenewedEvent event) {
        log.info("[{}] 服务[{}] 进行续约...", simpleDate("yyyy-MM-dd HH:mm:ss"),
                event.getServerId() + "-" + event.getAppName());
    }

    /**
     * Eureka注册中心启动事件
     * @param event
     */
    @EventListener
    public void listen(EurekaRegistryAvailableEvent event) {
        log.info("[{}] Eureka 注册中心 启动...", simpleDate("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Eureka Server启动事件
     * @param event
     */
    @EventListener
    public void listen(EurekaServerStartedEvent event) {
        log.info("[{}] Eureka Server 启动...", simpleDate("yyyy-MM-dd HH:mm:ss"));
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
            Template template = cfg.getTemplate("simpleMessage.ftl");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            helper.setText(html, true);

            //发送邮件
            jMailSender.send(mimeMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 简单格式化时间
     * @param format
     * @return
     */
    public String simpleDate(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }

}

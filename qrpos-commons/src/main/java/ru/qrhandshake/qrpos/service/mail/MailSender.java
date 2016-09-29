package ru.qrhandshake.qrpos.service.mail;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;


import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Date;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;

public class MailSender {

    private static Logger logger = LoggerFactory.getLogger(MailSender.class);
    private static final String DEFAULT_ENCODING = "UTF-8";
    private String defaultSender;
    private String debugAddress;
    private boolean debugMode = false;

    private final List<JavaMailSender> javaMailSenders;

    public MailSender(List<JavaMailSender> javaMailSenders) {
        this.javaMailSenders = javaMailSenders;
    }

    public void setDefaultSender(String defaultSender) {
        this.defaultSender = defaultSender;
    }
    public void setDebugAddress(String debugAddress) {
        this.debugAddress = debugAddress;
    }
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    /**
     * @throw runTimeException if unable to sent email
     * @param mailObject
     */
    public void send(MailObject mailObject) throws MailSenderException {
        boolean sent = false;
        Exception lastException = null;
        int i = 1;
        int countSender = (null != javaMailSenders ? javaMailSenders.size() : 0);
        for (JavaMailSender javaMailSender : javaMailSenders) {
            if ( sent ) break;
            try {
                logger.debug("Try to send mail message with id: " + mailObject.getId() + " by [" + i + "/" + countSender + "] sender" + "[" + ((JavaMailSenderImpl)javaMailSender).getHost() + "]");
                MimeMessage mimeMessage = javaMailSender.createMimeMessage();
                mimeMessage.setSentDate(new Date());
                toMimeMessage(mailObject,new MimeMessageHelper(mimeMessage,true,isNotBlank(mailObject.getEncoding()) ? mailObject.getEncoding() : DEFAULT_ENCODING),javaMailSender);
                javaMailSender.send(mimeMessage);
                sent = true;
                mimeMessage = null;
                logger.debug("Message with id: {} has been sent by sender [{}]",mailObject.getId(),((JavaMailSenderImpl)javaMailSender).getHost());
            } catch (MessagingException e) {
                logger.error("Cannot build mail " + mailObject + " use sender " + ((JavaMailSenderImpl)javaMailSender).getHost() + "]. Try to send by next mail sender. Description: " + e.getMessage(),e);
            } catch (Exception e) {
                lastException = e;
                logger.warn("Unable to send mail message with id: " + mailObject.getId() + " by sender [" + ((JavaMailSenderImpl)javaMailSender).getHost() + "]. Try to send by next mail sender. Description: " + e.getMessage());
            }
            i++;
        }
        logger.debug("Result of {} is {}",mailObject,sent);
        if ( !sent && null != lastException ) throw new MailSenderException(lastException);
    }

    protected void toMimeMessage(MailObject mailTO, MimeMessageHelper helper, JavaMailSender javaMailSender) throws MessagingException {
        if ( debugMode ) {
            helper.addTo(new InternetAddress(debugAddress));
            helper.setFrom(new InternetAddress(debugAddress));
        } else {

            //sender
            if (mailTO.getSender() != null) {
                helper.setFrom(new InternetAddress(mailTO.getSender()));
            } else if ( StringUtils.isNotBlank(defaultSender) ) {
                helper.setFrom(new InternetAddress(defaultSender));
            } else if ( null != ((JavaMailSenderImpl)javaMailSender).getJavaMailProperties().getProperty(MailObject.SENDER_PARAM) ) {
                helper.setFrom(new InternetAddress(((JavaMailSenderImpl)javaMailSender).getJavaMailProperties().getProperty(MailObject.SENDER_PARAM)));
            } else {
                logger.error("Unable to set 'sender' for send mail. Use or 'defaultSender' or '" + MailObject.SENDER_PARAM + "' property by javaMailSender bean.");
            }

            //recipients
            if ( StringUtils.isNotBlank(mailTO.getRecipients()) ) {
                for(String recipient: mailTO.getRecipients().split(",")) {
                    helper.addTo(new InternetAddress(recipient));
                }
            } else if (
                    null != ((JavaMailSenderImpl)javaMailSender).getJavaMailProperties().getProperty(MailObject.RECIPIENTS_PARAM)
                            &&
                            StringUtils.isNotBlank(((JavaMailSenderImpl) javaMailSender).getJavaMailProperties().getProperty(MailObject.RECIPIENTS_PARAM)) ) {
                String resp = ((JavaMailSenderImpl)javaMailSender).getJavaMailProperties().getProperty(MailObject.RECIPIENTS_PARAM);
                for (String recipient : resp.split(",")) {
                    helper.addTo(new InternetAddress(recipient));
                }
                if ( mailTO.isShadowCopy() ) {
                    helper.addBcc(new InternetAddress(mailTO.getSender()));
                }
            } else  {
                logger.error("Unable to set 'recipients' for send mail. Use or 'defaultRecipients' or '" + MailObject.RECIPIENTS_PARAM + "' property by javaMailSender bean.");
            }
        }

        if ( StringUtils.isNotBlank(mailTO.getCc()) ) helper.setCc(new InternetAddress(mailTO.getCc()));
        if ( StringUtils.isNotBlank(mailTO.getBcc()) ) helper.setBcc(new InternetAddress(mailTO.getBcc()));
        if ( StringUtils.isNotBlank(mailTO.getSubject()) ) helper.setSubject(mailTO.getSubject());
        else if ( null != ((JavaMailSenderImpl)javaMailSender).getJavaMailProperties().getProperty(MailObject.SUBJECT_PARAM)
                && StringUtils.isNotBlank(((JavaMailSenderImpl) javaMailSender).getJavaMailProperties().getProperty(MailObject.SUBJECT_PARAM)) ) {
            helper.setSubject(((JavaMailSenderImpl)javaMailSender).getJavaMailProperties().getProperty(MailObject.SUBJECT_PARAM));
        } else {
            logger.error("Unable to set 'subject' for send mail. Use or 'defaultSubject' or '" + MailObject.SUBJECT_PARAM + "' property by javaMailSender bean.");
        }
        helper.setText(mailTO.getBody(),true);

        if ( null != mailTO.getAttachments() && 0 < mailTO.getAttachments().size() ) {
            for (File file : mailTO.getAttachments()) {
                helper.addAttachment(file.getName(),file);
            }
        }
    }
}

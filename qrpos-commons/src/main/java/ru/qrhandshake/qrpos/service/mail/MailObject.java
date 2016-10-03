package ru.qrhandshake.qrpos.service.mail;

import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * User: Krainov
 * Date: 13.03.14
 * Time: 15:42
 */
public interface MailObject extends Serializable {

    public final static String SENDER_PARAM = "sender";
    public final static String RECIPIENTS_PARAM = "recipients";
    public final static String SUBJECT_PARAM = "subject";

    public String getSender();
    public String getRecipients();
    public String getSubject();
    public String getBody();
    public String getCc();
    public String getBcc();
    public String getMimeType();
    public String getEncoding();
    public boolean isShadowCopy();
    public Set<File> getAttachments();
    public Serializable getId();
    public String getApplication();

    public static class SimpleMailObject implements MailObject {
        private static final long serialVersionUID = -2017199943304759193L;
        private String sender;
        private String recipients;
        private String subject;
        private String body;
        private String cc;
        private String bcc;
        private String mimeType;
        private String encoding = "UTF-8";
        private boolean shadowCopy;
        private Set<File> attachments;
        private Serializable id = UUID.randomUUID().toString();
        private String application = "default";

        public SimpleMailObject sender(String sender) {
            this.sender = sender;
            return this;
        }
        public SimpleMailObject recipients(String recipients) {
            this.recipients = recipients;
            return this;
        }
        public SimpleMailObject subject(String subject) {
            this.subject = subject;
            return this;
        }
        public SimpleMailObject body(String body) {
            this.body = body;
            return this;
        }
        public SimpleMailObject encoding(String encoding) {
            this.encoding = encoding;
            return this;
        }
        public SimpleMailObject mimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public SimpleMailObject setCc(String cc) {
            this.cc = cc;
            return this;
        }

        public SimpleMailObject setBcc(String bcc) {
            this.bcc = bcc;
            return this;
        }


        public SimpleMailObject setShadowCopy(boolean shadowCopy) {
            this.shadowCopy = shadowCopy;
            return this;
        }

        public SimpleMailObject setApplication(String application) {
            this.application = application;
            return this;
        }

        public void setId(Serializable id) {
            this.id = id;
        }

        @Override
        public String getSender() {
            return sender;
        }

        @Override
        public String getRecipients() {
            return recipients;
        }

        @Override
        public String getSubject() {
            return subject;
        }

        @Override
        public String getBody() {
            return body;
        }

        @Override
        public String getCc() {
            return cc;
        }

        @Override
        public String getBcc() {
            return bcc;
        }

        @Override
        public String getMimeType() {
            return mimeType;
        }

        @Override
        public String getEncoding() {
            return encoding;
        }

        @Override
        public boolean isShadowCopy() {
            return shadowCopy;
        }

        @Override
        public Set<File> getAttachments() {
            return attachments;
        }

        @Override
        public String getApplication() {
            return application;
        }

        public SimpleMailObject addAttachment(File file) {
            if ( null == attachments ) attachments = new HashSet<File>();
            attachments.add(file);
            return this;
        }

        @Override
        public Serializable getId() {
            return id;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("SimpleMailObject");
            sb.append("{sender='").append(sender).append('\'');
            sb.append(", recipients='").append(recipients).append('\'');
            sb.append(", subject='").append(subject).append('\'');
            sb.append(", body='").append(body).append('\'');
            sb.append(", application='").append(application).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }

}

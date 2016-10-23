package ru.qrhandshake.qrpos.util;


import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MaskingPatternLayout extends PatternLayout {

    private List<ReplaceTemplate> replaceTemplates = new LinkedList<>();

    private ObjectMapper objectMapper = new ObjectMapper();

    public void setPatternsProperty(String patternsProperty) throws IOException {
        @SuppressWarnings("unchecked") Map<String, String> map = objectMapper.readValue(patternsProperty, Map.class);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue().contains("~")) {
                String[] s = entry.getValue().split("~");
                int firstPartLength = Integer.valueOf(s[0]);
                int lastPartLength = Integer.valueOf(s[2]);
                Pattern sourcePattern = Pattern.compile(entry.getKey(),Pattern.CASE_INSENSITIVE);
                String replaceString = s[1];
                replaceTemplates.add(new ReplaceTemplate(sourcePattern, replaceString, firstPartLength, lastPartLength));
            } else {
                Pattern sourcePattern = Pattern.compile(entry.getKey());
                replaceTemplates.add(new ReplaceTemplate(sourcePattern, entry.getValue()));
            }
        }
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        String msg = super.doLayout(event);
        for (ReplaceTemplate replaceTemplate : replaceTemplates) {
            Matcher matcher = replaceTemplate.getSourcePattern().matcher(msg);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                if (replaceTemplate.isSimpleReplace()) {
                    matcher.appendReplacement(sb, replaceTemplate.getReplaceString());
                } else {
                    String group = matcher.group();
                    String s1 = group.substring(0, replaceTemplate.getFirstPartLength());
                    String s2 = group.substring(group.length() - replaceTemplate.getLastPartLength());
                    matcher.appendReplacement(sb, s1 + replaceTemplate.replaceString + s2);
                }
            }
            matcher.appendTail(sb);
            msg = sb.toString();
        }
        return msg;
    }

    private static class ReplaceTemplate {
        private Pattern sourcePattern;
        private String replaceString;
        private int firstPartLength;
        private int lastPartLength;
        private boolean simpleReplace;

        ReplaceTemplate(Pattern sourcePattern, String replaceString) {
            this.sourcePattern = sourcePattern;
            this.replaceString = replaceString;
            this.simpleReplace = true;
        }

        ReplaceTemplate(Pattern sourcePattern, String replaceString, int firstPartLength, int lastPartLength) {
            this.sourcePattern = sourcePattern;
            this.replaceString = replaceString;
            this.firstPartLength = firstPartLength;
            this.lastPartLength = lastPartLength;
            this.simpleReplace = false;
        }

        Pattern getSourcePattern() {
            return sourcePattern;
        }

        String getReplaceString() {
            return replaceString;
        }

        int getFirstPartLength() {
            return firstPartLength;
        }

        int getLastPartLength() {
            return lastPartLength;
        }

        boolean isSimpleReplace() {
            return simpleReplace;
        }
    }
}

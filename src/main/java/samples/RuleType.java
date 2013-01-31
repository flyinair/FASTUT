/*
 * Copyright 2012 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package samples;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * 规则类型
 *
 * @author fangpan 2012-8-13 下午02:13:27
 */
public enum RuleType implements Serializable {
    /** Delay Task Rule */
    DL("DL"),
    /** Immediately Completed Task Rule */
    IC("IC"),
    /** Real Time Task Rule */
    RT("RT");

    private static Pattern RULE_TYPE_PATTERN = Pattern.compile("\\#\\*(.*?)\\*\\#", Pattern.CASE_INSENSITIVE);

    private String         type;

    RuleType(String type){
        this.type = type;
    }

    public static RuleType matchOf(String rule) {
        if (StringUtils.isBlank(rule)) {
            return null;
        }
        Matcher m = RULE_TYPE_PATTERN.matcher(rule);
        while (m.find()) {
            String type = m.group(1);
            RuleType t = typeOf(type.trim());
            if (t != null) {
                return t;
            }
        }
        return RT;
    }

    public static RuleType typeOf(String type) {
        if (DL.getType().equals(type)) {
            return DL;
        }
        if (IC.getType().equals(type)) {
            return IC;
        }
        if (RT.getType().equals(type)) {
            return RT;
        }
        return null;
    }

    public String getType() {
        return type;
    }

    public boolean isDelay() {
        return this.equals(DL);
    }

    public boolean isImediatelyCompleted() {
        return this.equals(IC);
    }

    public boolean isRealTime() {
        return this.equals(RT);
    }
}

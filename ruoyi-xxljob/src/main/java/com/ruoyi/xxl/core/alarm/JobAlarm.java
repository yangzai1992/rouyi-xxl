package com.ruoyi.xxl.core.alarm;

import com.ruoyi.xxl.core.model.XxlJobLog;
import com.ruoyi.xxl.core.model.XxlJobInfo;

/**
 * @author xuxueli 2020-01-19
 */
public interface JobAlarm {

    /**
     * job alarm
     *
     * @param info
     * @param jobLog
     * @return
     */
    public boolean doAlarm(XxlJobInfo info, XxlJobLog jobLog);

}

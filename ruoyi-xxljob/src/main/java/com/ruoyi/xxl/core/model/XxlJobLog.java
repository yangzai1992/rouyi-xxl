package com.ruoyi.xxl.core.model;

import java.util.Date;

/**
 * xxl-job log, used to track trigger process
 * @author xuxueli  2015-12-19 23:19:09
 */
public class XxlJobLog {
	
	private long id;
	
	// job info
	private int jobGroup;
	private int jobId;

	// execute info
	private String executorAddress;
	private String executorHandler;
	private String executorParam;
	private String executorShardingParam;
	private int executorFailRetryCount;
	
	// trigger info
	private Date triggerTime;
	private int triggerCode;
	private String triggerMsg;
	
	// handle info
	private Date handleTime;
	private int handleCode;
	private String handleMsg;

	// alarm info
	private int alarmStatus;

	// 日志开始时间
	private String triggerTimeStart;
	// 日志结束时间
	private String triggerTimeEnd;
	private String logStatus;

	private String filterTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getJobGroup() {
		return jobGroup;
	}

	public void setJobGroup(int jobGroup) {
		this.jobGroup = jobGroup;
	}

	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	public String getExecutorAddress() {
		return executorAddress;
	}

	public void setExecutorAddress(String executorAddress) {
		this.executorAddress = executorAddress;
	}

	public String getExecutorHandler() {
		return executorHandler;
	}

	public void setExecutorHandler(String executorHandler) {
		this.executorHandler = executorHandler;
	}

	public String getExecutorParam() {
		return executorParam;
	}

	public void setExecutorParam(String executorParam) {
		this.executorParam = executorParam;
	}

	public String getExecutorShardingParam() {
		return executorShardingParam;
	}

	public void setExecutorShardingParam(String executorShardingParam) {
		this.executorShardingParam = executorShardingParam;
	}

	public int getExecutorFailRetryCount() {
		return executorFailRetryCount;
	}

	public void setExecutorFailRetryCount(int executorFailRetryCount) {
		this.executorFailRetryCount = executorFailRetryCount;
	}

	public Date getTriggerTime() {
		return triggerTime;
	}

	public void setTriggerTime(Date triggerTime) {
		this.triggerTime = triggerTime;
	}

	public int getTriggerCode() {
		return triggerCode;
	}

	public void setTriggerCode(int triggerCode) {
		this.triggerCode = triggerCode;
	}

	public String getTriggerMsg() {
		return triggerMsg;
	}

	public void setTriggerMsg(String triggerMsg) {
		this.triggerMsg = triggerMsg;
	}

	public Date getHandleTime() {
		return handleTime;
	}

	public void setHandleTime(Date handleTime) {
		this.handleTime = handleTime;
	}

	public int getHandleCode() {
		return handleCode;
	}

	public void setHandleCode(int handleCode) {
		this.handleCode = handleCode;
	}

	public String getHandleMsg() {
		return handleMsg;
	}

	public void setHandleMsg(String handleMsg) {
		this.handleMsg = handleMsg;
	}

	public int getAlarmStatus() {
		return alarmStatus;
	}

	public void setAlarmStatus(int alarmStatus) {
		this.alarmStatus = alarmStatus;
	}

	public String getTriggerTimeStart() {
		return triggerTimeStart;
	}

	public void setTriggerTimeStart(String triggerTimeStart) {
		this.triggerTimeStart = triggerTimeStart;
	}

	public String getTriggerTimeEnd() {
		return triggerTimeEnd;
	}

	public void setTriggerTimeEnd(String triggerTimeEnd) {
		this.triggerTimeEnd = triggerTimeEnd;
	}

	public String getLogStatus() {
		return logStatus;
	}

	public void setLogStatus(String logStatus) {
		this.logStatus = logStatus;
	}

	public String getFilterTime() {
		return filterTime;
	}

	public void setFilterTime(String filterTime) {
		this.filterTime = filterTime;
	}
}

package com.ruoyi.xxl.controller;

import com.github.pagehelper.PageInfo;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.xxl.core.complete.XxlJobCompleter;
import com.ruoyi.xxl.core.exception.XxlJobException;
import com.ruoyi.xxl.core.model.XxlJobGroup;
import com.ruoyi.xxl.core.model.XxlJobInfo;
import com.ruoyi.xxl.core.model.XxlJobLog;
import com.ruoyi.xxl.core.scheduler.XxlJobScheduler;
import com.ruoyi.xxl.core.util.I18nUtil;
import com.ruoyi.xxl.mapper.XxlJobGroupDao;
import com.ruoyi.xxl.mapper.XxlJobInfoDao;
import com.ruoyi.xxl.mapper.XxlJobLogDao;
import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.biz.model.KillParam;
import com.xxl.job.core.biz.model.LogParam;
import com.xxl.job.core.biz.model.LogResult;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.util.DateUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * index controller
 * @author xuxueli 2015-12-19 16:13:16
 */
@Controller
@RequestMapping("/xxl/joblog")
public class JobLogController {

	private String prefix = "xxl/joblog";

	private static Logger logger = LoggerFactory.getLogger(JobLogController.class);

	@Resource
	private XxlJobGroupDao xxlJobGroupDao;
	@Resource
	public XxlJobInfoDao xxlJobInfoDao;
	@Resource
	public XxlJobLogDao xxlJobLogDao;

	@RequestMapping
	public String index(HttpServletRequest request, Model model, @RequestParam(required = false, defaultValue = "0") Integer jobId) {

		// 执行器列表
		List<XxlJobGroup> jobGroupList_all =  xxlJobGroupDao.findAll();

		// filter group
		List<XxlJobGroup> jobGroupList = JobInfoController.filterJobGroupByRole(request, jobGroupList_all);
		if (jobGroupList==null || jobGroupList.size()==0) {
			throw new XxlJobException(I18nUtil.getString("jobgroup_empty"));
		}

		model.addAttribute("JobGroupList", jobGroupList);

		// 任务
		if (jobId > 0) {
			XxlJobInfo jobInfo = xxlJobInfoDao.loadById(jobId);
			if (jobInfo == null) {
				throw new RuntimeException(I18nUtil.getString("jobinfo_field_id") + I18nUtil.getString("system_unvalid"));
			}

			model.addAttribute("jobInfo", jobInfo);

			// valid permission
			JobInfoController.validPermission(request, jobInfo.getJobGroup());
		}

		return prefix + "/joblog";
	}


	@RequestMapping("/getJobsByGroup")
	@ResponseBody
	public ReturnT<List<XxlJobInfo>> getJobsByGroup(int jobGroup){
		List<XxlJobInfo> list = xxlJobInfoDao.getJobsByGroup(jobGroup);
		return new ReturnT<List<XxlJobInfo>>(list);
	}

	@RequiresPermissions("xxl:joblog:view")
	@RequestMapping("/list")
	@ResponseBody
	public TableDataInfo pageList(XxlJobLog xxlJobLog) {

		// valid permission
		//JobInfoController.validPermission(request, jobGroup);	// 仅管理员支持查询全部；普通用户仅支持查询有权限的 jobGroup
		String filterTime = xxlJobLog.getFilterTime();
		if(StringUtils.isNotBlank(filterTime)){
			// parse param
			Date triggerTimeStart = null;
			Date triggerTimeEnd = null;
			if (filterTime!=null && filterTime.trim().length()>0) {
				String[] temp = filterTime.split(" - ");
				if (temp.length == 2) {
					triggerTimeStart = DateUtil.parseDateTime(temp[0]);
					triggerTimeEnd = DateUtil.parseDateTime(temp[1]);
					xxlJobLog.setTriggerTimeStart(temp[0]);
					xxlJobLog.setTriggerTimeEnd(temp[1]);
				}
			}
		}


		
		// page query
		List<XxlJobLog> list = xxlJobLogDao.selectList(xxlJobLog);

		TableDataInfo rspData = new TableDataInfo();
		rspData.setCode(0);
		rspData.setRows(list);
		rspData.setTotal(new PageInfo(list).getTotal());
		return rspData;
	}

	@RequiresPermissions("xxl:joblog:view")
	@RequestMapping("/logDetailPage")
	public String logDetailPage(int id, Model model){

		// base check
		ReturnT<String> logStatue = ReturnT.SUCCESS;
		XxlJobLog jobLog = xxlJobLogDao.load(id);
		if (jobLog == null) {
            throw new RuntimeException(I18nUtil.getString("joblog_logid_unvalid"));
		}

        model.addAttribute("triggerCode", jobLog.getTriggerCode());
        model.addAttribute("handleCode", jobLog.getHandleCode());
        model.addAttribute("logId", jobLog.getId());
		return prefix + "/detail";
	}

	@RequestMapping("/logDetailCat")
	@ResponseBody
	public ReturnT<LogResult> logDetailCat(long logId, int fromLineNum){
		try {
			// valid
			XxlJobLog jobLog = xxlJobLogDao.load(logId);	// todo, need to improve performance
			if (jobLog == null) {
				return new ReturnT<LogResult>(ReturnT.FAIL_CODE, I18nUtil.getString("joblog_logid_unvalid"));
			}

			// log cat
			ExecutorBiz executorBiz = XxlJobScheduler.getExecutorBiz(jobLog.getExecutorAddress());
			ReturnT<LogResult> logResult = executorBiz.log(new LogParam(jobLog.getTriggerTime().getTime(), logId, fromLineNum));

			// is end
            if (logResult.getContent()!=null && logResult.getContent().getFromLineNum() > logResult.getContent().getToLineNum()) {
                if (jobLog.getHandleCode() > 0) {
                    logResult.getContent().setEnd(true);
                }
            }

			return logResult;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ReturnT<LogResult>(ReturnT.FAIL_CODE, e.getMessage());
		}
	}

	@RequestMapping("/logKill")
	@ResponseBody
	public AjaxResult logKill(int id){
		// base check
		XxlJobLog log = xxlJobLogDao.load(id);
		XxlJobInfo jobInfo = xxlJobInfoDao.loadById(log.getJobId());
		if (jobInfo==null) {
			return AjaxResult.error( I18nUtil.getString("jobinfo_glue_jobid_unvalid"));
		}
		if (ReturnT.SUCCESS_CODE != log.getTriggerCode()) {
			return AjaxResult.error(I18nUtil.getString("joblog_kill_log_limit"));
		}

		// request of kill
		ReturnT<String> runResult = null;
		try {
			ExecutorBiz executorBiz = XxlJobScheduler.getExecutorBiz(log.getExecutorAddress());
			runResult = executorBiz.kill(new KillParam(jobInfo.getId()));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			runResult = new ReturnT<String>(500, e.getMessage());
		}

		if (ReturnT.SUCCESS_CODE == runResult.getCode()) {
			log.setHandleCode(ReturnT.FAIL_CODE);
			log.setHandleMsg( I18nUtil.getString("joblog_kill_log_byman")+":" + (runResult.getMsg()!=null?runResult.getMsg():""));
			log.setHandleTime(new Date());
			XxlJobCompleter.updateHandleInfoAndFinish(log);
			return AjaxResult.success(runResult.getMsg());
		} else {
			return AjaxResult.error(runResult.getMsg());
		}
	}

	/**
	 * 清除日志
	 *
	 * @param model
	 * @param map
	 * @return
	 */
	@RequiresPermissions("job:joblog:remove")
	@RequestMapping("/remove")
	public String joblog(Model model, @RequestParam HashMap<String, Object> map) {
		// 执行器列表
		model.addAttribute("jobGroupText", map.get("jobGroupText"));
		model.addAttribute("jobGroup", map.get("jobGroup"));
		model.addAttribute("jobIdText", map.get("jobIdText"));
		model.addAttribute("jobId", map.get("jobId"));
		model.addAttribute("projectId", map.get("projectId"));
		model.addAttribute("projectText", map.get("projectText"));
		return prefix + "/deletelog";
	}


	@Log(title = "清除调度日志", businessType = BusinessType.DELETE)
	@PostMapping("/clearLog")
	@ResponseBody
	public AjaxResult clearLog(int jobGroup, int jobId, int type){
		Date clearBeforeTime = null;
		int clearBeforeNum = 0;
		if (type == 1) {
			clearBeforeTime = DateUtil.addMonths(new Date(), -1);	// 清理一个月之前日志数据
		} else if (type == 2) {
			clearBeforeTime = DateUtil.addMonths(new Date(), -3);	// 清理三个月之前日志数据
		} else if (type == 3) {
			clearBeforeTime = DateUtil.addMonths(new Date(), -6);	// 清理六个月之前日志数据
		} else if (type == 4) {
			clearBeforeTime = DateUtil.addYears(new Date(), -1);	// 清理一年之前日志数据
		} else if (type == 5) {
			clearBeforeNum = 1000;		// 清理一千条以前日志数据
		} else if (type == 6) {
			clearBeforeNum = 10000;		// 清理一万条以前日志数据
		} else if (type == 7) {
			clearBeforeNum = 30000;		// 清理三万条以前日志数据
		} else if (type == 8) {
			clearBeforeNum = 100000;	// 清理十万条以前日志数据
		} else if (type == 9) {
			clearBeforeNum = 0;			// 清理所有日志数据
		} else {
			return AjaxResult.error(I18nUtil.getString("joblog_clean_type_unvalid"));
		}

		List<Long> logIds = null;
		do {
			logIds = xxlJobLogDao.findClearLogIds(jobGroup, jobId, clearBeforeTime, clearBeforeNum, 1000);
			if (logIds!=null && logIds.size()>0) {
				xxlJobLogDao.clearLog(logIds);
			}
		} while (logIds!=null && logIds.size()>0);
		return AjaxResult.success(true);
	}

}

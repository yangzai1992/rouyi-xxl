package com.ruoyi.xxl.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.xxl.core.thread.JobScheduleHelper;
import com.ruoyi.xxl.core.exception.XxlJobException;
import com.ruoyi.xxl.core.model.XxlJobGroup;
import com.ruoyi.xxl.core.model.XxlJobInfo;
import com.ruoyi.xxl.core.model.XxlJobUser;
import com.ruoyi.xxl.core.route.ExecutorRouteStrategyEnum;
import com.ruoyi.xxl.core.scheduler.MisfireStrategyEnum;
import com.ruoyi.xxl.core.scheduler.ScheduleTypeEnum;
import com.ruoyi.xxl.core.thread.JobTriggerPoolHelper;
import com.ruoyi.xxl.core.trigger.TriggerTypeEnum;
import com.ruoyi.xxl.core.util.I18nUtil;
import com.ruoyi.xxl.mapper.XxlJobGroupDao;
import com.ruoyi.xxl.mapper.XxlJobInfoDao;
import com.ruoyi.xxl.service.LoginService;
import com.ruoyi.xxl.service.XxlJobService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.enums.ExecutorBlockStrategyEnum;
import com.xxl.job.core.glue.GlueTypeEnum;
import com.xxl.job.core.util.DateUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * index controller
 * @author xuxueli 2015-12-19 16:13:16
 */
@Controller
@RequestMapping("/xxl/jobinfo")
public class JobInfoController extends BaseController {

	private String prefix = "xxl/jobinfo";

	private static Logger logger = LoggerFactory.getLogger(JobInfoController.class);

	@Resource
	private XxlJobGroupDao xxlJobGroupDao;

	@Resource
	private XxlJobInfoDao xxlJobInfoDao;
	@Resource
	private XxlJobService xxlJobService;

	@RequiresPermissions("xxl:jobinfo:view")
	@RequestMapping
	public String index(HttpServletRequest request, Model model, @RequestParam(required = false, defaultValue = "-1") int jobGroup) {

		// 枚举-字典
		model.addAttribute("ExecutorRouteStrategyEnum", ExecutorRouteStrategyEnum.values());	    // 路由策略-列表
		model.addAttribute("GlueTypeEnum", GlueTypeEnum.values());								// Glue类型-字典
		model.addAttribute("ExecutorBlockStrategyEnum", ExecutorBlockStrategyEnum.values());	    // 阻塞处理策略-字典
		model.addAttribute("ScheduleTypeEnum", ScheduleTypeEnum.values());	    				// 调度类型
		model.addAttribute("MisfireStrategyEnum", MisfireStrategyEnum.values());	    			// 调度过期策略

		// 执行器列表
		List<XxlJobGroup> jobGroupList_all =  xxlJobGroupDao.findAll();

		// filter group
		List<XxlJobGroup> jobGroupList = filterJobGroupByRole(request, jobGroupList_all);
		if (jobGroupList==null || jobGroupList.size()==0) {
			throw new XxlJobException(I18nUtil.getString("jobgroup_empty"));
		}

		model.addAttribute("JobGroupList", jobGroupList);
		model.addAttribute("jobGroup", jobGroup);

		return prefix + "/jobinfo";
	}

	public static List<XxlJobGroup> filterJobGroupByRole(HttpServletRequest request, List<XxlJobGroup> jobGroupList_all){
	/*	List<XxlJobGroup> jobGroupList = new ArrayList<>();
		if (jobGroupList_all!=null && jobGroupList_all.size()>0) {
			XxlJobUser loginUser = (XxlJobUser) request.getAttribute(LoginService.LOGIN_IDENTITY_KEY);
			if (loginUser.getRole() == 1) {
				jobGroupList = jobGroupList_all;
			} else {
				List<String> groupIdStrs = new ArrayList<>();
				if (loginUser.getPermission()!=null && loginUser.getPermission().trim().length()>0) {
					groupIdStrs = Arrays.asList(loginUser.getPermission().trim().split(","));
				}
				for (XxlJobGroup groupItem:jobGroupList_all) {
					if (groupIdStrs.contains(String.valueOf(groupItem.getId()))) {
						jobGroupList.add(groupItem);
					}
				}
			}
		}*/
		return jobGroupList_all;
	}
	public static void validPermission(HttpServletRequest request, int jobGroup) {
		/*XxlJobUser loginUser = (XxlJobUser) request.getAttribute(LoginService.LOGIN_IDENTITY_KEY);
		if (!loginUser.validPermission(jobGroup)) {
			throw new RuntimeException(I18nUtil.getString("system_permission_limit") + "[username="+ loginUser.getUsername() +"]");
		}*/
	}

	@RequiresPermissions("xxl:jobinfo:view")
	@RequestMapping("/list")
	@ResponseBody
	public TableDataInfo pageList(XxlJobInfo jobInfo) {
		startPage();
		//jobInfo.setUserId(ShiroUtils.getUserId());
		List<XxlJobInfo> jobinfoList = xxlJobInfoDao.selectXxlJobInfoList(jobInfo);
		return getDataTable(jobinfoList);
	}

	/**
	 * 新增任务
	 */
	@RequiresPermissions("job:jobinfo:add")
	@GetMapping("/add")
	public String add(@RequestParam(name = "jobGroup",required = false) String jobGroup,Model model) {
		model.addAttribute("jobGroup", jobGroup);
		// 枚举-字典
		model.addAttribute("ExecutorRouteStrategyEnum", ExecutorRouteStrategyEnum.values());	    // 路由策略-列表
		model.addAttribute("GlueTypeEnum", GlueTypeEnum.values());								// Glue类型-字典
		model.addAttribute("ExecutorBlockStrategyEnum", ExecutorBlockStrategyEnum.values());	    // 阻塞处理策略-字典
		model.addAttribute("ScheduleTypeEnum", ScheduleTypeEnum.values());	    				// 调度类型
		model.addAttribute("MisfireStrategyEnum", MisfireStrategyEnum.values());	    			// 调度过期策略

		// 执行器列表
		List<XxlJobGroup> jobGroupList =  xxlJobGroupDao.findAll();
		if (jobGroupList==null || jobGroupList.size()==0) {
			throw new XxlJobException(I18nUtil.getString("jobgroup_empty"));
		}
		model.addAttribute("JobGroupList", jobGroupList);
		return prefix + "/add";
	}


	@RequiresPermissions("xxl:jobinfo:add")
	@PostMapping("/add")
	@ResponseBody
	public AjaxResult addSave(HttpServletRequest request,XxlJobInfo jobInfo) {
		String scheduleType = request.getParameter("scheduleType");
		if("CRON".equals(scheduleType)){
			jobInfo.setScheduleConf(request.getParameter("schedule_conf_CRON"));
		}else if("FIX_RATE".equals(scheduleType)){
			jobInfo.setScheduleConf(request.getParameter("schedule_conf_FIX_RATE"));
		}else if("FIX_DELAY".equals(scheduleType)){
			jobInfo.setScheduleConf(request.getParameter("schedule_conf_FIX_DELAY"));
		}
		ReturnT<String> add = xxlJobService.add(jobInfo);
		return add.getCode() != ReturnT.SUCCESS_CODE ? AjaxResult.error(add.getMsg(),add.getContent()) : AjaxResult.success(add.getMsg());
	}

	/**
	 * 更新任务
	 */
	@RequiresPermissions("xxl:jobinfo:edit")
	@GetMapping("/edit/{id}")
	public String edit(ModelMap mmap, @PathVariable("id") int id) {
		// 枚举-字典
		mmap.addAttribute("ExecutorRouteStrategyEnum",
				ExecutorRouteStrategyEnum.values()); // 路由策略-列表

		mmap.addAttribute("GlueTypeEnum", GlueTypeEnum.values()); // Glue类型-字典
		mmap.addAttribute("ExecutorBlockStrategyEnum",
				ExecutorBlockStrategyEnum.values()); // 阻塞处理策略-字典

		mmap.addAttribute("ExecutorRouteStrategyEnum", ExecutorRouteStrategyEnum.values());	    // 路由策略-列表
		mmap.addAttribute("GlueTypeEnum", GlueTypeEnum.values());								// Glue类型-字典
		mmap.addAttribute("ExecutorBlockStrategyEnum", ExecutorBlockStrategyEnum.values());	    // 阻塞处理策略-字典
		mmap.addAttribute("ScheduleTypeEnum", ScheduleTypeEnum.values());	    				// 调度类型
		mmap.addAttribute("MisfireStrategyEnum", MisfireStrategyEnum.values());	    			// 调度过期策略

		// 任务组
		XxlJobGroup jobGroup = new XxlJobGroup();
		List<XxlJobGroup> jobGroupList = xxlJobGroupDao.findAll();
		mmap.addAttribute("JobGroupList", jobGroupList);
		mmap.addAttribute("jobInfo", xxlJobInfoDao.loadById(id));
		return prefix + "/edit";
	}


	@RequiresPermissions("xxl:jobinfo:update")
	@RequestMapping("/update")
	@ResponseBody
	public AjaxResult update(HttpServletRequest request,XxlJobInfo jobInfo) {
		ReturnT<String> update = xxlJobService.update(jobInfo);
		return update.getCode() == ReturnT.SUCCESS_CODE ?
				AjaxResult.success():
				AjaxResult.error(update.getMsg());
	}

	@RequiresPermissions("xxl:jobinfo:remove")
	@RequestMapping("/remove")
	@ResponseBody
	public AjaxResult remove(int id) {
		xxlJobService.remove(id);
		return AjaxResult.success();
	}

	@RequiresPermissions("xxl:jobinfo:view")
	@RequestMapping("/stop")
	@ResponseBody
	public AjaxResult pause(int id) {
		xxlJobService.stop(id);
		return AjaxResult.success();
	}

	@RequiresPermissions("xxl:jobinfo:view")
	@RequestMapping("/start")
	@ResponseBody
	public AjaxResult start(int id) {
		ReturnT<String> start = xxlJobService.start(id);
		return start.getCode() == ReturnT.SUCCESS_CODE ?
				AjaxResult.success():
				AjaxResult.error(start.getMsg());
	}

	/**
	 * 执行
	 */
	@RequiresPermissions("xxl:jobinfo:view")
	@GetMapping("/trigger/{id}")
	public String trigger(Model mmap, @PathVariable("id") int id) {
		mmap.addAttribute("id", id);
		return prefix + "/trigger";
	}


	@RequiresPermissions("xxl:jobinfo:view")
	@RequestMapping("/trigger")
	@ResponseBody
	//@PermissionLimit(limit = false)
	public AjaxResult triggerJob(int id, String executorParam, String addressList) {
		// force cover job param
		if (executorParam == null) {
			executorParam = "";
		}

		JobTriggerPoolHelper.trigger(id, TriggerTypeEnum.MANUAL, -1, null, executorParam, addressList);
		return AjaxResult.success();
	}

	@RequiresPermissions("xxl:jobinfo:view")
	@RequestMapping("/nextTriggerTime")
	@ResponseBody
	public AjaxResult nextTriggerTime(String scheduleType, String scheduleConf) {

		XxlJobInfo paramXxlJobInfo = new XxlJobInfo();
		paramXxlJobInfo.setScheduleType(scheduleType);
		paramXxlJobInfo.setScheduleConf(scheduleConf);

 		List<String> result = new ArrayList<>();
		try {
			Date lastTime = new Date();
			for (int i = 0; i < 5; i++) {
				lastTime = JobScheduleHelper.generateNextValidTime(paramXxlJobInfo, lastTime);
				if (lastTime != null) {
					result.add(DateUtil.formatDateTime(lastTime));
				} else {
					break;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return AjaxResult.error(I18nUtil.getString("schedule_type")+I18nUtil.getString("system_unvalid") + e.getMessage());
		}
		return AjaxResult.success(result);

	}
	
}

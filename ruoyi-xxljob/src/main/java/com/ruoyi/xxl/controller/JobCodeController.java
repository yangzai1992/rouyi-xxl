package com.ruoyi.xxl.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.xxl.core.model.XxlJobInfo;
import com.ruoyi.xxl.core.model.XxlJobLogGlue;
import com.ruoyi.xxl.core.util.I18nUtil;
import com.ruoyi.xxl.mapper.XxlJobInfoDao;
import com.ruoyi.xxl.mapper.XxlJobLogGlueDao;
import com.xxl.job.core.glue.GlueTypeEnum;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * job code controller
 * @author xuxueli 2015-12-19 16:13:16
 */
@Controller
@RequestMapping("/xxl/jobcode")
public class JobCodeController{
	private String prefix = "xxl/jobcode";

	@Resource
	private XxlJobInfoDao xxlJobInfoDao;
	@Resource
	private XxlJobLogGlueDao xxlJobLogGlueDao;

	@RequestMapping
	public String index(HttpServletRequest request, Model model, int jobId) {
		XxlJobInfo jobInfo = xxlJobInfoDao.loadById(jobId);
		List<XxlJobLogGlue> jobLogGlues = xxlJobLogGlueDao.findByJobId(jobId);

		if (jobInfo == null) {
			throw new RuntimeException(I18nUtil.getString("jobinfo_glue_jobid_unvalid"));
		}
		if (GlueTypeEnum.BEAN == GlueTypeEnum.match(jobInfo.getGlueType())) {
			throw new RuntimeException(I18nUtil.getString("jobinfo_glue_gluetype_unvalid"));
		}

		// valid permission
		JobInfoController.validPermission(request, jobInfo.getJobGroup());

		// Glue类型-字典
		//model.addAttribute("GlueTypeEnum", GlueTypeEnum.values());
		GlueTypeEnum gule =	GlueTypeEnum.match(jobInfo.getGlueType());
		model.addAttribute("glueDesc", gule.getDesc());

		model.addAttribute("jobInfo", jobInfo);
		model.addAttribute("jobLogGlues", jobLogGlues);
		return prefix + "/jobcode";
	}
	
	@RequestMapping("/save")
	@ResponseBody
	public AjaxResult save(Model model, int id, String glueSource, String glueRemark) {
		// valid
		if (glueRemark==null) {
			return AjaxResult.error(I18nUtil.getString("system_please_input") + I18nUtil.getString("jobinfo_glue_remark"));
		}
		if (glueRemark.length()<4 || glueRemark.length()>100) {
			return AjaxResult.error(I18nUtil.getString("jobinfo_glue_remark_limit"));
		}
		XxlJobInfo exists_jobInfo = xxlJobInfoDao.loadById(id);
		if (exists_jobInfo == null) {
			return AjaxResult.error(I18nUtil.getString("jobinfo_glue_jobid_unvalid"));
		}
		
		// update new code
		exists_jobInfo.setGlueSource(glueSource);
		exists_jobInfo.setGlueRemark(glueRemark);
		exists_jobInfo.setGlueUpdatetime(new Date());

		exists_jobInfo.setUpdateTime(new Date());
		xxlJobInfoDao.update(exists_jobInfo);

		// log old code
		XxlJobLogGlue xxlJobLogGlue = new XxlJobLogGlue();
		xxlJobLogGlue.setJobId(exists_jobInfo.getId());
		xxlJobLogGlue.setGlueType(exists_jobInfo.getGlueType());
		xxlJobLogGlue.setGlueSource(glueSource);
		xxlJobLogGlue.setGlueRemark(glueRemark);

		xxlJobLogGlue.setAddTime(new Date());
		xxlJobLogGlue.setUpdateTime(new Date());
		xxlJobLogGlueDao.save(xxlJobLogGlue);

		// remove code backup more than 30
		xxlJobLogGlueDao.removeOld(exists_jobInfo.getId(), 30);

		return AjaxResult.success();
	}
	
}

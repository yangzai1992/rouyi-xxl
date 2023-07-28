package com.ruoyi.xxl.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.xxl.core.model.XxlJobGroup;
import com.ruoyi.xxl.core.model.XxlJobRegistry;
import com.ruoyi.xxl.core.util.I18nUtil;
import com.ruoyi.xxl.mapper.XxlJobGroupDao;
import com.ruoyi.xxl.mapper.XxlJobInfoDao;
import com.ruoyi.xxl.mapper.XxlJobRegistryDao;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.enums.RegistryConfig;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * job group controller
 * @author xuxueli 2016-10-02 20:52:56
 */
@Controller
@RequestMapping("/xxl/jobgroup")
public class JobGroupController extends BaseController {

	private String prefix = "xxl/jobgroup";

	@Resource
	public XxlJobInfoDao xxlJobInfoDao;
	@Resource
	public XxlJobGroupDao xxlJobGroupDao;
	@Resource
	private XxlJobRegistryDao xxlJobRegistryDao;

	@RequiresPermissions("xxl:jobgroup:view")
	@RequestMapping
	public String index(Model model) {
		return prefix + "/jobgroup";
	}

	@RequiresPermissions("xxl:jobgroup:add")
	@GetMapping("/add")
	public String add(Model model) {
		return prefix + "/add";
	}

	@RequiresPermissions("xxl:jobgroup:edit")
	@GetMapping("/edit/{id}")
	public String edit(@PathVariable("id") Integer id, ModelMap mmap) {
		XxlJobGroup xxlJobGroup = xxlJobGroupDao.load(id);
		mmap.put("xxlJobGroup", xxlJobGroup);
		return prefix + "/edit";
	}



	@RequiresPermissions("xxl:jobgroup:view")
	@RequestMapping("/list")
	@ResponseBody
	public TableDataInfo list(XxlJobGroup xxlJobGroup) {
		startPage();
		List<XxlJobGroup> list = xxlJobGroupDao.selectXxlJobGroupList(xxlJobGroup);
		return getDataTable(list);
	}

	@RequiresPermissions("xxl:jobgroup:add")
	@PostMapping("/add")
	@ResponseBody
	public AjaxResult addSave(XxlJobGroup xxlJobGroup){

		// valid
		if (xxlJobGroup.getAppname()==null || xxlJobGroup.getAppname().trim().length()==0) {
			return AjaxResult.error(I18nUtil.getString("system_please_input")+"AppName");
		}
		if (xxlJobGroup.getAppname().length()<4 || xxlJobGroup.getAppname().length()>64) {
			return AjaxResult.error(I18nUtil.getString("jobgroup_field_appname_length") );
		}
		if (xxlJobGroup.getAppname().contains(">") || xxlJobGroup.getAppname().contains("<")) {
			return AjaxResult.error( "AppName"+I18nUtil.getString("system_unvalid") );
		}
		if (xxlJobGroup.getTitle()==null || xxlJobGroup.getTitle().trim().length()==0) {
			return AjaxResult.error( (I18nUtil.getString("system_please_input") + I18nUtil.getString("jobgroup_field_title")) );
		}
		if (xxlJobGroup.getTitle().contains(">") || xxlJobGroup.getTitle().contains("<")) {
			return AjaxResult.error(I18nUtil.getString("jobgroup_field_title")+I18nUtil.getString("system_unvalid") );
		}
		if (xxlJobGroup.getAddressType()!=0) {
			if (xxlJobGroup.getAddressList()==null || xxlJobGroup.getAddressList().trim().length()==0) {
				return AjaxResult.error(I18nUtil.getString("jobgroup_field_addressType_limit") );
			}
			if (xxlJobGroup.getAddressList().contains(">") || xxlJobGroup.getAddressList().contains("<")) {
				return AjaxResult.error(I18nUtil.getString("jobgroup_field_registryList")+I18nUtil.getString("system_unvalid") );
			}

			String[] addresss = xxlJobGroup.getAddressList().split(",");
			for (String item: addresss) {
				if (item==null || item.trim().length()==0) {
					return AjaxResult.error( I18nUtil.getString("jobgroup_field_registryList_unvalid") );
				}
			}
		}

		// process
		xxlJobGroup.setUpdateTime(new Date());

		int ret = xxlJobGroupDao.save(xxlJobGroup);
		return (ret>0)? AjaxResult.success(): AjaxResult.error();
	}

	@RequiresPermissions("xxl:jobgroup:edit")
	@PostMapping("/edit")
	@ResponseBody
	public AjaxResult editSave(XxlJobGroup xxlJobGroup){
		// valid
		if (xxlJobGroup.getAppname()==null || xxlJobGroup.getAppname().trim().length()==0) {
			return AjaxResult.error(I18nUtil.getString("system_please_input")+"AppName");
		}
		if (xxlJobGroup.getAppname().length()<4 || xxlJobGroup.getAppname().length()>64) {
			return AjaxResult.error( I18nUtil.getString("jobgroup_field_appname_length") );
		}
		if (xxlJobGroup.getTitle()==null || xxlJobGroup.getTitle().trim().length()==0) {
			return AjaxResult.error(I18nUtil.getString("system_please_input") + I18nUtil.getString("jobgroup_field_title"));
		}
		if (xxlJobGroup.getAddressType() == 0) {
			// 0=自动注册
			List<String> registryList = findRegistryByAppName(xxlJobGroup.getAppname());
			String addressListStr = null;
			if (registryList!=null && !registryList.isEmpty()) {
				Collections.sort(registryList);
				addressListStr = "";
				for (String item:registryList) {
					addressListStr += item + ",";
				}
				addressListStr = addressListStr.substring(0, addressListStr.length()-1);
			}
			xxlJobGroup.setAddressList(addressListStr);
		} else {
			// 1=手动录入
			if (xxlJobGroup.getAddressList()==null || xxlJobGroup.getAddressList().trim().length()==0) {
				return AjaxResult.error(I18nUtil.getString("jobgroup_field_addressType_limit") );
			}
			String[] addresss = xxlJobGroup.getAddressList().split(",");
			for (String item: addresss) {
				if (item==null || item.trim().length()==0) {
					return AjaxResult.error( I18nUtil.getString("jobgroup_field_registryList_unvalid") );
				}
			}
		}

		// process
		xxlJobGroup.setUpdateTime(new Date());

		int ret = xxlJobGroupDao.update(xxlJobGroup);
		return (ret>0)? AjaxResult.success(): AjaxResult.error();
	}

	private List<String> findRegistryByAppName(String appnameParam){
		HashMap<String, List<String>> appAddressMap = new HashMap<String, List<String>>();
		List<XxlJobRegistry> list = xxlJobRegistryDao.findAll(RegistryConfig.DEAD_TIMEOUT, new Date());
		if (list != null) {
			for (XxlJobRegistry item: list) {
				if (RegistryConfig.RegistType.EXECUTOR.name().equals(item.getRegistryGroup())) {
					String appname = item.getRegistryKey();
					List<String> registryList = appAddressMap.get(appname);
					if (registryList == null) {
						registryList = new ArrayList<String>();
					}

					if (!registryList.contains(item.getRegistryValue())) {
						registryList.add(item.getRegistryValue());
					}
					appAddressMap.put(appname, registryList);
				}
			}
		}
		return appAddressMap.get(appnameParam);
	}

	@RequiresPermissions("xxl:jobgroup:remove")
	@RequestMapping("/remove")
	@ResponseBody
	public AjaxResult remove(@RequestParam("ids")int id){

		// valid
		int count = xxlJobInfoDao.pageListCount(0, 10, id, -1,  null, null, null);
		if (count > 0) {
			return AjaxResult.error( I18nUtil.getString("jobgroup_del_limit_0") );
		}

		List<XxlJobGroup> allList = xxlJobGroupDao.findAll();
		if (allList.size() == 1) {
			return AjaxResult.error(I18nUtil.getString("jobgroup_del_limit_1") );
		}

		int ret = xxlJobGroupDao.remove(id);
		return (ret>0)?  AjaxResult.success(): AjaxResult.error();
	}

	@RequestMapping("/loadById")
	@ResponseBody
	public AjaxResult loadById( int id){
		XxlJobGroup jobGroup = xxlJobGroupDao.load(id);
		return jobGroup!=null?AjaxResult.success(jobGroup):AjaxResult.error();
	}
}

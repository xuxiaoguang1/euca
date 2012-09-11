package com.eucalyptus.webui.server;

import java.util.ArrayList;
import java.util.Arrays;
import com.eucalyptus.webui.client.service.QuickLink;
import com.eucalyptus.webui.client.service.QuickLinkTag;
import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.shared.checker.InvalidValueException;
import com.eucalyptus.webui.shared.query.QueryType;
import com.eucalyptus.webui.shared.user.EnumUserType;

public class QuickLinks {
	
	public static ArrayList<QuickLinkTag> getTags(boolean isSysAdmin, EnumUserType userType) throws EucalyptusServiceException {
		if (isSysAdmin)
			return getSystemAdminTags();
		else if (userType == EnumUserType.ADMIN) {
			return getAccountAdminTags();
		}
		else {
			return getUserTags();
		}
	}
	
	private static ArrayList<QuickLinkTag> getSystemAdminTags() throws EucalyptusServiceException {
		try {
			return new ArrayList<QuickLinkTag>(Arrays.asList(
					getQuickLinkTag("用户管理", "用户管理", "组管理", "账户管理", "策略管理", "密钥管理", "证书管理", "用户申请管理"),
					getQuickLinkTag("设备管理", "区域管理", "机房管理", "机柜管理"),
					getQuickLinkTag("资源管理", "服务器管理", "内存管理", "硬盘管理", "CPU管理", "带宽管理", "IP管理", "模板管理", "虚拟机管理", "镜像管理", "Keypair管理", "安全组管理", "规则管理", "原模板管理"),
					getQuickLinkTag("定价管理", "CPU定价", "其他设备定价", "模版定价"),
					getQuickLinkTag("组件管理", "节点控制器管理", "集群控制器管理", "存储控制器管理", "Walrus控制器管理"),
					getQuickLinkTag("统计管理", "基本资源统计", "CPU统计", "内存统计", "硬盘统计", "历史使用情况统计")));
		}
		catch (Exception e) {
			throw new EucalyptusServiceException("Failed to load user information for ");
		}
	}
	
	private static ArrayList<QuickLinkTag> getAccountAdminTags() throws EucalyptusServiceException {
		try {
			return new ArrayList<QuickLinkTag>(
					Arrays.asList(getQuickLinkTag("用户管理", "用户管理", "组管理", "策略管理", "密钥管理", "证书管理", "用户申请管理")));
		}
		catch (Exception e) {
			throw new EucalyptusServiceException("Failed to load user information for ");
		}
	}
	
	private static ArrayList<QuickLinkTag> getUserTags() throws EucalyptusServiceException {
		try {
			return new ArrayList<QuickLinkTag>(
					Arrays.asList(getQuickLinkTag("用户管理", "用户管理", "策略管理", "密钥管理", "证书管理", "用户申请管理")));
		}
		catch (Exception e) {
			throw new EucalyptusServiceException("Failed to load user information for ");
		}
	}
	
	private static QuickLink allQuickLinks[] = {
			getQuickLink("用户管理", "user", QueryType.user),
			getQuickLink("用户申请管理", "user_app", QueryType.user_app),
			getQuickLink("组管理", "group", QueryType.group),
			getQuickLink("账户管理", "accout", QueryType.account),
			getQuickLink("策略管理", "policy", QueryType.policy),
			getQuickLink("密钥管理", "key", QueryType.key),
			getQuickLink("证书管理", "cert", QueryType.cert),
			
			getQuickLink("区域管理", "device", QueryType.device_area),
			getQuickLink("机房管理", "device", QueryType.device_room),
			getQuickLink("机柜管理", "device", QueryType.device_cabinet),
			
			getQuickLink("服务器管理", "device", QueryType.device_server),
			getQuickLink("内存管理", "device", QueryType.device_memory),
			getQuickLink("硬盘管理", "device", QueryType.device_disk),
			getQuickLink("CPU管理", "device", QueryType.device_cpu),
			getQuickLink("IP管理", "device", QueryType.device_ip),
			getQuickLink("带宽管理", "device", QueryType.device_bw),
			getQuickLink("模板管理", "device", QueryType.device_template),
			getQuickLink("虚拟机管理", "device", QueryType.instance),
			getQuickLink("镜像管理", "device", QueryType.image),
			getQuickLink("Keypair管理", "device", QueryType.keypair),
			getQuickLink("安全组管理", "device", QueryType.securityGroup),
			getQuickLink("规则管理", "device", QueryType.ipPermission),
			getQuickLink("原模板管理", "device", QueryType.vmtype),
			
			// 定价管理
			getQuickLink("CPU定价", "device", QueryType.device_cpu_price),
			getQuickLink("其他设备定价", "device", QueryType.device_others_price),
			getQuickLink("模版定价", "device", QueryType.device_template_price),
			
			// 组件管理
			getQuickLink("节点控制器管理", "component", QueryType.nodeCtrl),
			getQuickLink("集群控制器管理", "component", QueryType.clusterCtrl),
			getQuickLink("存储控制器管理", "component", QueryType.storageCtrl),
			getQuickLink("Walrus控制器管理", "component", QueryType.walrusCtrl),
			
			// 系统管理
			getQuickLink("服务组件", "config", QueryType.config),
			
			// 资源管理
			getQuickLink("镜像", "image", QueryType.image),
			getQuickLink("虚拟机类型", "type", QueryType.vmtype),
			getQuickLink("使用报告", "report", QueryType.report),
			
			// 统计管理
			getQuickLink("基本资源统计", "report", QueryType.res_stat),
			getQuickLink("CPU统计", "report", QueryType.cpu_stat),
			getQuickLink("内存统计", "report", QueryType.memory_stat),
			getQuickLink("硬盘统计", "report", QueryType.disk_stat),
			getQuickLink("历史使用情况统计", "report", QueryType.history_stat),
	};
	
	private static QuickLink getQuickLink(String name, String image, QueryType type) {
		return new QuickLink(name, name, image, QueryBuilder.get().start(type).query());
	}
	
	private static QuickLink getQuickLink(String name) {
		for (QuickLink link : allQuickLinks) {
			if (link.getName().equals(name)) {
				return link;
			}
		}
		throw new InvalidValueException(String.format("Cannot find QuickLink with name `%s'", name));
	}
	
	private static QuickLinkTag getQuickLinkTag(String tagName, String... linkNames) {
		ArrayList<QuickLink> linkList = new ArrayList<QuickLink>();
		for (String name : linkNames) {
			linkList.add(getQuickLink(name));
		}
		return new QuickLinkTag(tagName, linkList);
	}
	
}

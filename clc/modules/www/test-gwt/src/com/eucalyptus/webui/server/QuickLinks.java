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
		  return getSystemAdminTags( );
	  else if (userType == EnumUserType.ADMIN) {
		  return getAccountAdminTags();
	  }
	  else {
		  return getUserTags();
	  }
  }
  
	private static ArrayList<QuickLinkTag> getSystemAdminTags( ) throws EucalyptusServiceException {
		try {
			return new ArrayList<QuickLinkTag>( Arrays.asList(getQuickLinkTag("用户管理", "用户管理", "组管理", "账户管理", "策略管理", "密钥管理", "证书管理"),
									getQuickLinkTag("资源管理", "服务器管理",  "内存管理", "硬盘管理", "CPU管理", "带宽管理", "IP管理", "模板管理", "虚拟机管理", "镜像管理"),
									getQuickLinkTag("组件管理", "节点控制器管理", "集群控制器管理", "存储控制器管理", "Walrus控制器管理")));
		} catch ( Exception e ) { 
			throw new EucalyptusServiceException( "Failed to load user information for ");
		}    
	}
  
	private static ArrayList<QuickLinkTag> getAccountAdminTags( ) throws EucalyptusServiceException {
		try {
			return new ArrayList<QuickLinkTag>( Arrays.asList(new QuickLinkTag( "用户管理", accountdminUserLinks)));
	    } catch ( Exception e ) { 
	    	throw new EucalyptusServiceException( "Failed to load user information for ");
	    }
	}
	
	private static ArrayList<QuickLinkTag> getUserTags( ) throws EucalyptusServiceException {
		try {
			return new ArrayList<QuickLinkTag>( Arrays.asList(new QuickLinkTag( "用户管理", sysAdminUserLinks)));
	    } catch ( Exception e ) { 
	    	throw new EucalyptusServiceException( "Failed to load user information for ");
	    }
	}
  
	private static QuickLink allQuickLinks[] = {
			getQuickLink("用户管理", "user", QueryType.user),
			getQuickLink("组管理", "group", QueryType.group),
			getQuickLink("账户管理", "accout", QueryType.account),
			getQuickLink("策略管理", "policy", QueryType.policy),
			getQuickLink("密钥管理", "key", QueryType.key),
			getQuickLink("证书管理", "cert", QueryType.cert),
			getQuickLink("服务器管理", "device", QueryType.device_server),
			getQuickLink("内存管理", "device", QueryType.device_memory),
			getQuickLink("硬盘管理", "device", QueryType.device_disk),
			getQuickLink("CPU管理", "device", QueryType.device_cpu),
			getQuickLink("IP管理", "device", QueryType.device_ip),
			getQuickLink("带宽管理", "device", QueryType.device_bw),
			getQuickLink("模板管理", "device", QueryType.device_template),
			getQuickLink("虚拟机管理", "device", QueryType.instance),
			getQuickLink("镜像管理", "device", QueryType.image),
			
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
	
	private static QuickLinkTag getQuickLinkTag(String tagName, String ... linkNames) {
		ArrayList<QuickLink> linkList = new ArrayList<QuickLink>();
		for (String name : linkNames) {
			linkList.add(getQuickLink(name));
		}
		return new QuickLinkTag(tagName, linkList);
	}
	
	private static QuickLink userLinks[] =  {
		//new QuickLink("个人管理", "个人管理", "individual", QueryBuilder.get( ).start( QueryType.individual ).query()),
		new QuickLink("用户管理", "用户管理", "user", QueryBuilder.get( ).start( QueryType.user ).query()),
		new QuickLink("组管理", "组管理", "group", QueryBuilder.get( ).start( QueryType.group ).query()),
		new QuickLink("账户管理", "账户管理", "accout", QueryBuilder.get( ).start( QueryType.account ).query()),
		new QuickLink("策略管理", "策略管理", "policy", QueryBuilder.get( ).start( QueryType.policy ).query()),
		new QuickLink("密钥管理", "密钥管理", "key", QueryBuilder.get( ).start( QueryType.key ).query()),
		new QuickLink("证书管理", "证书管理", "certificate", QueryBuilder.get( ).start( QueryType.cert ).query())
	};
	
	private static QuickLink serviceLinks[] = {
		new QuickLink( "服务组件", "Configuration of service components", "config", QueryBuilder.get( ).start( QueryType.config ).query( ))
	};
	  
	private static QuickLink resourceLinks[] = {
		new QuickLink( "镜像", "Virtual machine images (EMIs)", "image", QueryBuilder.get( ).start( QueryType.image ).query( ) ),
		new QuickLink( "虚拟机类型", "Virtual machine types", "type", QueryBuilder.get( ).start( QueryType.vmtype ).query( ) ),
		new QuickLink( "使用报告", "Resource usage report", "report", QueryBuilder.get( ).start( QueryType.report ).query( ))
	};
		
	private static ArrayList<QuickLink> sysAdminUserLinks = new ArrayList<QuickLink>(Arrays.asList(userLinks[0], userLinks[1], userLinks[2], userLinks[3], userLinks[4], userLinks[5]));
	private static ArrayList<QuickLink> sysAdminSysLinks = new ArrayList<QuickLink>(Arrays.asList(serviceLinks[0]));
	private static ArrayList<QuickLink> sysAdminResLinks = new ArrayList<QuickLink>(Arrays.asList(resourceLinks[0], resourceLinks[1], resourceLinks[2]));
  
	private static ArrayList<QuickLink> accountdminUserLinks = new ArrayList<QuickLink>(Arrays.asList(userLinks[0], userLinks[1], userLinks[3], userLinks[4], userLinks[5]));
}

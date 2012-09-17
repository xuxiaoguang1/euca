package com.eucalyptus.webui.server.dictionary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.eucalyptus.webui.server.QuickLinks;
import com.eucalyptus.webui.shared.config.SysConfig;
import com.google.gwt.thirdparty.guava.common.base.Strings;

public class SysConfParser {

	public SysConfParser() {
	}
	
	public void parse(String filePath) {
		
		SAXReader saxReader = new SAXReader();
		
		try {
			Document document = saxReader.read(ClassLoader.getSystemResource(filePath));
			
			readElements(document);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			SysConfParser.LOG.log(Level.ERROR, "SysConfParser parsing exception");
			e.printStackTrace();
		}
	}
	
	public SysConfig getSysConfig() {
		return this.sysConfig;
	}
	
	private void readElements(Document document) {
		Element root = document.getRootElement();
		List<?> list = root.elements();
		Iterator<?> iter = list.iterator();
		while (iter.hasNext()) {
			Element parent = (Element) iter.next();
			String tag = parent.getName();
			
			if (tag.equalsIgnoreCase(SysConfParser.XML_TAG_LAN))
				parseLanguageConf(parent);
			else if (tag.equalsIgnoreCase(XML_TAG_LINKS))
				parseLinks(parent);
			else if (tag.equalsIgnoreCase(XML_TAG_LINKCONFIG))
				parseLinkConfig(parent);
			else if (tag.equalsIgnoreCase(SysConfParser.XML_TAG_TABLESIZE))
				parseViewTableSizeConfig(parent);			
		}
	}
	
	private void parseLanguageConf(Element node) {
		List<?> childs = node.elements();
		
		Iterator<?> childIter = childs.iterator();
		
		while (childIter.hasNext()) {
			Element child = (Element) childIter.next();
			System.out.println(child.getName());
			if (child.getName().equalsIgnoreCase(XML_TAG_LAN_INDEX)) {
				Object value = child.getData();
				
				if (value != null)
					this.sysConfig.setLanguage(value.toString());
			}
		}
	}
	
	private void parseLinks(Element node) {
		List<?> childs = node.elements();
		
		Iterator<?> childIter = childs.iterator();
		
		while (childIter.hasNext()) {
			Element child = (Element) childIter.next();
			
			if (child.getName().equalsIgnoreCase(XML_TAG_LINKS_LINK)) {
				
				List<?> eles = child.elements();
				
				Iterator<?> propIter = eles.iterator();
				String linkName = null;
				String linkDesc = null;
				String image = null;
				String queryType = null;
				
				while (propIter.hasNext()) {
					Element ele = (Element) propIter.next();
					String name = ele.getName();
					String data = ele.getData().toString();
					
					if (Strings.isNullOrEmpty(name) || Strings.isNullOrEmpty(data))
						continue;
					
					if (name.equalsIgnoreCase(XML_TAG_LINKS_LINK_NAME)) {
						linkName = data;
					}
					else if (name.equalsIgnoreCase(XML_TAG_LINKS_LINK_DESC)) {
						linkDesc = data;
					}
					else if (name.equalsIgnoreCase(XML_TAG_LINKS_LINK_IMAGE)) {
						image = data;
					}
					else if (name.equalsIgnoreCase(XML_TAG_LINKS_LINK_QUERYTYPE)) {
						queryType = data;
					}
				}
				
				if (!Strings.isNullOrEmpty(linkName) 
						&& !Strings.isNullOrEmpty(linkDesc)
						&& !Strings.isNullOrEmpty(image)
						&& !Strings.isNullOrEmpty(queryType))
					QuickLinks.addLink(linkName, linkDesc, image, queryType);
			}
		}
	}
	
	private void parseLinkConfig(Element node) {
		List<?> childs = node.elements();
		
		Iterator<?> childIter = childs.iterator();
		
		while (childIter.hasNext()) {
			Element child = (Element) childIter.next();
			
			if (child.getName().equalsIgnoreCase(XML_TAG_LINKCONFIG_GROUP)) {
				
				String userType = child.attributeValue(XML_TAG_LINKCONFIG_GROUP_PROP_USERTYPE);
				
				List<?> grandchilds = child.elements();
				
				Iterator<?> grandchildIter = grandchilds.iterator();
				
				String tag = null;
				ArrayList<String> links = null;
				
				while (grandchildIter.hasNext()) {
					Element grandchild = (Element) grandchildIter.next();
					
					if (grandchild.getName().equalsIgnoreCase(XML_TAG_LINKCONFIG_GROUP_TAG)) {
						
						tag = grandchild.attributeValue(XML_TAG_LINKCONFIG_GROUP_TAG_PROP_NAME);
						
						List<?> eles = grandchild.elements();
						
						Iterator<?> propIter = eles.iterator();
						
						links = new ArrayList<String>();
						
						while (propIter.hasNext()) {
							Element ele = (Element) propIter.next();
							
							String name = ele.getName();
							String data = ele.getData().toString();
							
							if (Strings.isNullOrEmpty(name) || Strings.isNullOrEmpty(data))
								continue;
							
							if (name.equalsIgnoreCase(XML_TAG_LINKCONFIG_GROUP_TAG_LINK)) {
								links.add(data);
							}
						}
						
						if (!Strings.isNullOrEmpty(userType) 
								&& !Strings.isNullOrEmpty(tag)
								&& links.size() > 0) {
							if (userType.equalsIgnoreCase(XML_TAG_LINKCONFIG_GROUP_PROP_USERTYPE_SYSADMIN))
								QuickLinks.addSysAdminLinkGroup(tag, links);
							else if (userType.equalsIgnoreCase(XML_TAG_LINKCONFIG_GROUP_PROP_USERTYPE_ACCOUNTADMIN))
								QuickLinks.addAccountAdminLinkGroup(tag, links);
							else if (userType.equalsIgnoreCase(XML_TAG_LINKCONFIG_GROUP_PROP_USERTYPE_USER))
								QuickLinks.addUserLinkGroup(tag, links);
						}
					}
				}
			}
		}
	}
	
	private void parseViewTableSizeConfig(Element node) {
		List<?> childs = node.elements();
		
		Iterator<?> childIter = childs.iterator();
		
		while (childIter.hasNext()) {
			Element child = (Element) childIter.next();
			
			if (child.getName().equalsIgnoreCase(XML_TAG_TABLESIZE_VIEW)) {
				
				List<?> eles = child.elements();
				
				Iterator<?> propIter = eles.iterator();
				String viewName = null;
				String tableSize = null;
				
				while (propIter.hasNext()) {
					Element ele = (Element) propIter.next();
					String name = ele.getName();
					String data = ele.getData().toString();
					
					if (Strings.isNullOrEmpty(name) || Strings.isNullOrEmpty(data))
						continue;
					
					if (name.equalsIgnoreCase(XML_TAG_TABLESIZE_VIEW_NAME)) {
						viewName = data;
					}
					else if (name.equalsIgnoreCase(XML_TAG_TABLESIZE_VIEW_SIZE)) {
						tableSize = data;
					}
				}
				
				if (!Strings.isNullOrEmpty(viewName) && !Strings.isNullOrEmpty(tableSize))
					this.sysConfig.getViewTableSizeConfig().put(viewName.toLowerCase(), tableSize);
			}
		}
	}
	
	private static String XML_TAG_LAN = "language";
	private static String XML_TAG_LAN_INDEX = "index";
	
	private static String XML_TAG_TABLESIZE = "searchtable_size";
	private static String XML_TAG_TABLESIZE_VIEW = "view";
	private static String XML_TAG_TABLESIZE_VIEW_NAME = "name";
	private static String XML_TAG_TABLESIZE_VIEW_SIZE = "size";
	
	private static String XML_TAG_LINKS = "links";
	private static String XML_TAG_LINKS_LINK = "link";
	private static String XML_TAG_LINKS_LINK_NAME = "name";
	private static String XML_TAG_LINKS_LINK_DESC = "desc";
	private static String XML_TAG_LINKS_LINK_IMAGE = "image";
	private static String XML_TAG_LINKS_LINK_QUERYTYPE = "query_type_enum";
	
	private static String XML_TAG_LINKCONFIG = "link_config";
	private static String XML_TAG_LINKCONFIG_GROUP = "group";
	private static String XML_TAG_LINKCONFIG_GROUP_PROP_USERTYPE = "user_type";
	private static String XML_TAG_LINKCONFIG_GROUP_PROP_USERTYPE_SYSADMIN = "system_admin";
	private static String XML_TAG_LINKCONFIG_GROUP_PROP_USERTYPE_ACCOUNTADMIN = "account_admin";
	private static String XML_TAG_LINKCONFIG_GROUP_PROP_USERTYPE_USER = "user";
	private static String XML_TAG_LINKCONFIG_GROUP_TAG = "tag";
	private static String XML_TAG_LINKCONFIG_GROUP_TAG_PROP_NAME = "name";
	private static String XML_TAG_LINKCONFIG_GROUP_TAG_LINK = "link";
	
	private SysConfig sysConfig = new SysConfig();
	
	private static final Logger LOG = Logger.getLogger( SysConfParser.class.getName( ) );
}

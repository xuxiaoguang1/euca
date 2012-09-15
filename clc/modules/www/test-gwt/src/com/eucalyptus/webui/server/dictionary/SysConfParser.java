package com.eucalyptus.webui.server.dictionary;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

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
			
			if (tag.equalsIgnoreCase(SysConfParser.SYS_CONF_XML_TAG_LAN))
				parseLanguageConf(parent);
			else if (tag.equalsIgnoreCase(SysConfParser.SYS_CONF_XML_TAG_TABLESIZE))
				parseViewTableSizeConf(parent);			
		}
	}
	
	private void parseLanguageConf(Element node) {
		List<?> childs = node.elements();
		
		Iterator<?> childIter = childs.iterator();
		
		while (childIter.hasNext()) {
			Element child = (Element) childIter.next();
			System.out.println(child.getName());
			if (child.getName().equalsIgnoreCase(SYS_CONF_XML_TAG_LAN_INDEX)) {
				Object value = child.getData();
				
				if (value != null)
					this.sysConfig.setLanguage(value.toString());
			}
		}
	}
	
	private void parseViewTableSizeConf(Element node) {
		List<?> childs = node.elements();
		
		Iterator<?> childIter = childs.iterator();
		
		while (childIter.hasNext()) {
			Element child = (Element) childIter.next();
			
			if (child.getName().equalsIgnoreCase(SYS_CONF_XML_TAG_TABLESIZE_VIEW)) {
				
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
					
					if (name.equalsIgnoreCase(SYS_CONF_XML_TAG_TABLESIZE_VIEW_NAME)) {
						viewName = data;
					}
					else if (name.equalsIgnoreCase(SYS_CONF_XML_TAG_TABLESIZE_VIEW_SIZE)) {
						tableSize = data;
					}
				}
				
				if (!Strings.isNullOrEmpty(viewName) && !Strings.isNullOrEmpty(tableSize))
					this.sysConfig.getViewTableSizeConfig().put(viewName, tableSize);
			}
		}
	}
	
	private static String SYS_CONF_XML_TAG_LAN = "Language";
	private static String SYS_CONF_XML_TAG_LAN_INDEX = "Index";
	
	private static String SYS_CONF_XML_TAG_TABLESIZE = "SearchTableSize";
	private static String SYS_CONF_XML_TAG_TABLESIZE_VIEW = "View";
	private static String SYS_CONF_XML_TAG_TABLESIZE_VIEW_NAME = "Name";
	private static String SYS_CONF_XML_TAG_TABLESIZE_VIEW_SIZE = "Size";
	
	private SysConfig sysConfig = new SysConfig();
	
	private static final Logger LOG = Logger.getLogger( SysConfParser.class.getName( ) );
}

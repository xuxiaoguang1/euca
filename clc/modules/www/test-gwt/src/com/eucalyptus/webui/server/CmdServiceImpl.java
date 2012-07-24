package com.eucalyptus.webui.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.eucalyptus.webui.client.service.AwsService;
import com.eucalyptus.webui.client.service.CmdService;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.service.Session;
import com.google.common.collect.Lists;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class CmdServiceImpl extends RemoteServiceServlet implements CmdService {
	static final String EC2_ACCESS_KEY="YA8IBOXPEL3X3J7F2ZWYT";
	static final String EC2_SECRET_KEY="GZO4qV10hsKJ5abci3pRYGNnX7J8KG71MNcrz7Q2";
	static final String EC2_URL="http://166.111.134.121:8773/services/Eucalyptus";
	static final String SSH_HOST="root@166.111.134.121";
	
	public static final ArrayList<SearchResultFieldDesc> CTRL_COMMON_FIELD_DESCS = Lists.newArrayList();
	static {
		CTRL_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "ID", true, "10%") );
		CTRL_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "IP", true, "10%") );
		CTRL_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "端口号", true, "10%") );
		CTRL_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "状态", true, "10%") );
	}

	@Override
	public String run(Session session, String[] cmd) {
		String ret = "";
		try {
			ProcessBuilder b = new ProcessBuilder(cmd);
			Map<String, String> env = b.environment();
			env.put("EC2_URL", EC2_URL);
			env.put("EC2_ACCESS_KEY", EC2_ACCESS_KEY);
			env.put("EC2_SECRET_KEY", EC2_SECRET_KEY);
			Process p = b.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String s = reader.readLine();
			while (s != null) {
				ret = ret.concat(s + "\n");
				s = reader.readLine();
			}
			p.waitFor(); 
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	@Override
	public String sshRun(Session session, String[] cmd) {
	    final String[] _cmd = {"ssh", SSH_HOST, 
	    		"EC2_URL=" + EC2_URL + " EC2_ACCESS_KEY=" + EC2_ACCESS_KEY + " EC2_SECRET_KEY=" +  EC2_SECRET_KEY + " " +  
	    		StringUtils.join(cmd, " ")};
	    return run(session, _cmd);
	}

	@Override
	public SearchResult lookupNodeCtrl(Session session, String search,
			SearchRange range) {
		final String[] cmd = {"euca_conf --list-nodes|cut -f 2"};
		String ret = sshRun(session, cmd);
		List<SearchResultRow> data = new ArrayList<SearchResultRow>();
		for (String s : ret.split("\n")) {
			if (s != null)
				data.add(new SearchResultRow(Arrays.asList("", s, "", "")));
		}
		SearchResult result = new SearchResult(data.size(), range);
		result.setDescs(CTRL_COMMON_FIELD_DESCS);
		result.setRows(data.subList(range.getStart(), range.getStart() + data.size()));
		return result;
	}
	
	@Override
	public SearchResult lookupStorageCtrl(Session session, String search,
			SearchRange range) {
		final String[] cmd = {"euca_conf --list-scs"};
		String ret = sshRun(session, cmd);
		List<SearchResultRow> data = new ArrayList<SearchResultRow>();
		for (String s : ret.split("\n")) {
			if (s != null) {
				String[] i = s.split("\\s+");
				data.add(new SearchResultRow(Arrays.asList(i[2], i[3], "", i[4])));
			}
		}
		SearchResult result = new SearchResult(data.size(), range);
		result.setDescs(CTRL_COMMON_FIELD_DESCS);
		result.setRows(data.subList(range.getStart(), range.getStart() + data.size()));
		return result;
	}
	@Override
	public SearchResult lookupClusterCtrl(Session session, String search,
			SearchRange range) {
		final String[] cmd = {"euca_conf --list-cluster"};
		String ret = sshRun(session, cmd);
		List<SearchResultRow> data = new ArrayList<SearchResultRow>();
		for (String s : ret.split("\n")) {
			if (s != null) {
				String[] i = s.split("\\s+");
				data.add(new SearchResultRow(Arrays.asList(i[2], i[3], "", i[4])));
			}
				
		}
		SearchResult result = new SearchResult(data.size(), range);
		result.setDescs(CTRL_COMMON_FIELD_DESCS);
		result.setRows(data.subList(range.getStart(), range.getStart() + data.size()));
		return result;
	}
	@Override
	public SearchResult lookupWalrusCtrl(Session session, String search,
			SearchRange range) {
		final String[] cmd = {"euca_conf --list-walruses"};
		String ret = sshRun(session, cmd);
		List<SearchResultRow> data = new ArrayList<SearchResultRow>();
		for (String s : ret.split("\n")) {
			if (s != null) {
				String[] i = s.split("\\s+");
				data.add(new SearchResultRow(Arrays.asList(i[2], i[3], "", i[4])));
			}
		}
		SearchResult result = new SearchResult(data.size(), range);
		result.setDescs(CTRL_COMMON_FIELD_DESCS);
		result.setRows(data.subList(range.getStart(), range.getStart() + data.size()));
		return result;
	}
}

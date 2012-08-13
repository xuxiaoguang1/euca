package com.eucalyptus.webui.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.eucalyptus.webui.client.service.CmdService;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.shared.aws.ImageType;
import com.google.common.collect.Lists;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class CmdServiceImpl extends RemoteServiceServlet implements CmdService {
	static final String EC2_ACCESS_KEY="YA8IBOXPEL3X3J7F2ZWYT";
	static final String EC2_SECRET_KEY="GZO4qV10hsKJ5abci3pRYGNnX7J8KG71MNcrz7Q2";
	static final String EC2_URL="http://166.111.134.80:8773/services/Eucalyptus";
	static final String SSH_HOST="root@166.111.134.80";
	//FIXME !! howto get certs? 
	static final String EC2_CERT="/root/cr/euca2-admin-005381a0-cert.pem";
	static final String EC2_PRIVATE_KEY="/root/cr/euca2-admin-005381a0-pk.pem";
	static final String EC2_USER_ID="950563033661";
	static final String EUCALYPTUS_CERT="/root/cr/cloud-cert.pem";
	
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
	    		"EC2_CERT=" + EC2_CERT + " EC2_ACCESS_KEY=" + EC2_ACCESS_KEY + " EC2_SECRET_KEY=" +  EC2_SECRET_KEY + " " +  " EC2_PRIVATE_KEY=" + EC2_PRIVATE_KEY +  
	    		" EC2_USER_ID=" + EC2_USER_ID + " EUCALYPTUS_CERT=" + EUCALYPTUS_CERT + " " +   
	    		StringUtils.join(cmd, " ")};
	    System.out.println("sshRun: " + _cmd[2]);
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

  @Override
  public String uploadImage(Session session, String file, ImageType type, String bucket,
      String name, String kernel, String ramdisk) {
    //won't need if run on server
    final String[] cmd0 = {"scp", file, SSH_HOST + ":/tmp/"};
    String ret = run(session, cmd0);
    System.out.println("ret: " + ret);
    
    //rename
    String _file = "/tmp/" + name;
    String s = "mv " + file + " " + _file;
    final String[] cmd = {s};
    ret = sshRun(session, cmd);
    System.out.println("ret: " + ret);
    
    //bundle
    String[] cmd1 = null;
    switch (type) {
    case KERNEL:
      String[] k = {"euca-bundle-image", "-i", _file, "--kernel", "true"};
      cmd1 = k;
      break;
    case RAMDISK:
      String[] ra = {"euca-bundle-image", "-i", _file, "--ramdisk", "true"};
      cmd1 = ra;
      break;
    case ROOTFS:
      String[] ro = {"euca-bundle-image", "-i", _file, "--kernel", kernel, "--ramdisk", ramdisk};
      cmd1 = ro;
      break;
    }
    ret = sshRun(session, cmd1);
    System.out.println("ret: " + ret);
    String manifest = "";
    {     
      String[] tmp  = ret.split("\n");
      for (String t : tmp) {
        if (t.contains("manifest")) {
          manifest = t.split("\\s")[2];
        }
      }
    }
    System.out.println("manifest: " + manifest);
    
    //upload
    final String[] cmd2 = {"euca-upload-bundle", "-b", bucket, "-m", manifest};
    ret = sshRun(session, cmd2);
    System.out.println("ret: " + ret);
    
    //register
    File m = new File(manifest);
    final String[] cmd3 = {"euca-register", bucket + "/" + m.getName()};
    ret = sshRun(session, cmd3);
    System.out.println("ret: " + ret);
    {
      String[] tmp  = ret.split("\n");
      for (String t : tmp) {
        if (t.contains("IMAGE")) {
          ret = t.split("\\s")[1];
        }
      }    
    }
    return ret; 
  }
}

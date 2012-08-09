package com.eucalyptus.webui.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.eucalyptus.webui.client.service.AwsInstance;
import com.eucalyptus.webui.client.service.AwsService;
import com.eucalyptus.webui.client.service.SearchRange;
import com.eucalyptus.webui.client.service.SearchResult;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc.TableDisplay;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc.Type;
import com.google.common.collect.Lists;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class AwsServiceImpl extends RemoteServiceServlet implements AwsService {
	static final String ACCESS_KEY="YA8IBOXPEL3X3J7F2ZWYT";
	static final String SECRET_KEY="GZO4qV10hsKJ5abci3pRYGNnX7J8KG71MNcrz7Q2";
	static final String ENDPOINT="http://166.111.134.80:8773/services/Eucalyptus";
	
	public static final ArrayList<SearchResultFieldDesc> INSTANCE_COMMON_FIELD_DESCS = Lists.newArrayList();
	static {
		INSTANCE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "虚拟机ID", true, "10%") );
		INSTANCE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "镜像ID", true, "10%") );
		INSTANCE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "IP地址", true, "10%") );
		INSTANCE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "带宽", true, "10%") );
		INSTANCE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "所属安全组", true, "10%") );
		INSTANCE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "使用用户", true, "10%") );
		INSTANCE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "开始时间", true, "10%") );
		INSTANCE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "服务期限", true, "10%") );
		INSTANCE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "剩余时间", true, "10%") );
		INSTANCE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc( "运行状态", true, "10%") );		
	}
	public static final ArrayList<SearchResultFieldDesc> IMAGE_COMMON_FIELD_DESCS = Lists.newArrayList();
	static {
		IMAGE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc("镜像ID", true, "10%"));
		IMAGE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc("类型", true, "10%"));
		IMAGE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc("架构", true, "10%"));
		IMAGE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc("位置", true, "20%"));
		IMAGE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc("内核", true, "10%"));
		IMAGE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc("Ramdisk", true, "10%"));
		IMAGE_COMMON_FIELD_DESCS.add(new SearchResultFieldDesc("状态", true, "10%"));
	}

	
	AmazonEC2 getEC2(Session s) {
		AWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
		ClientConfiguration cc = new ClientConfiguration();
		AmazonEC2 ec2 = new AmazonEC2Client(credentials, cc);
		ec2.setEndpoint(ENDPOINT);
		System.out.println(ec2.describeAvailabilityZones());
		return ec2;
	}

	@Override
	public SearchResult lookupInstance(Session session, String search, SearchRange range) {
		/*)ec2.
		SearchResult result = new SearchResult(1, range);
		result.setDescs(INSTANCE_COMMON_FIELD_DESCS);
		List<SearchResultRow> DATA = Arrays.asList( 
				new SearchResultRow( Arrays.asList( "a", "", "", "", "", "", "", "", "", "" ) ));
		result.setRows(DATA);
		return result;
		*/
		
		AmazonEC2 ec2 = getEC2(session);
		DescribeInstancesResult r = ec2.describeInstances();
		//DescribeInstanceStatusResult rr = ec2.describeInstanceStatus();
		List<SearchResultRow> data = new ArrayList<SearchResultRow>();
		for (Reservation res : r.getReservations()) {
			Instance ins = res.getInstances().get(0);
			String owner = res.getOwnerId();
			String id = ins.getInstanceId();
			String image = ins.getImageId();
			String priIp = ins.getPrivateIpAddress();
			String state = ins.getState().getName();
			String seGroup = res.getGroups().size() > 0 ? res.getGroups().get(0).getGroupName() : "";
			String date = ins.getLaunchTime().toString();
			data.add(new SearchResultRow(Arrays.asList(id, image, priIp, "", seGroup, owner, date, "", "", state)));
		}
		
		int resultLength = data.size();
		SearchResult result = new SearchResult(data.size(), range);
		result.setDescs(INSTANCE_COMMON_FIELD_DESCS);
		result.setRows(data.subList(range.getStart(), range.getStart() + resultLength));
		return result;
	}

	@Override
	public ArrayList<String> stopInstances(Session session, List<String> ids) {
		AmazonEC2 ec2 = getEC2(session);
		ec2.stopInstances(new StopInstancesRequest(ids));
		return null;
	}

	@Override
	public ArrayList<String> startInstances(Session session, List<String> ids) {
		AmazonEC2 ec2 = getEC2(session);
		ec2.startInstances(new StartInstancesRequest(ids));
		return null;
	}

	@Override
	public ArrayList<String> terminateInstances(Session session,
			List<String> ids) {
		AmazonEC2 ec2 = getEC2(session);
		ec2.terminateInstances(new TerminateInstancesRequest(ids));
		return null;
	}

	@Override
	public SearchResult lookupImage(Session session, String search,
			SearchRange range) {
		AmazonEC2 ec2 = getEC2(session);
		DescribeImagesResult r = ec2.describeImages();
		List<SearchResultRow> data = new ArrayList<SearchResultRow>();
		for (Image i : r.getImages()) {
			String id = i.getImageId();
			String type = i.getImageType();
			String arch = i.getArchitecture();
			String loc = i.getImageLocation();
			String kern = i.getKernelId();
			String rd = i.getRamdiskId();
			String state = i.getState();
			data.add(new SearchResultRow(Arrays.asList(id, type, arch, loc, kern, rd, state)));
		}
		int resultLength = data.size();
		SearchResult result = new SearchResult(data.size(), range);
		result.setDescs(IMAGE_COMMON_FIELD_DESCS);
		result.setRows(data.subList(range.getStart(), range.getStart() + resultLength));
		return result;
	}
}

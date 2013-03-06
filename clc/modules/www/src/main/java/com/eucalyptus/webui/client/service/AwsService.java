package com.eucalyptus.webui.client.service;

import java.util.ArrayList;
import java.util.List;

import com.eucalyptus.webui.client.session.Session;
import com.eucalyptus.webui.shared.aws.ImageType;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("awsBackend")
public interface AwsService extends RemoteService {
	public SearchResult lookupInstance(Session session, int userID, String search, SearchRange range) throws EucalyptusServiceException;
	public SearchResult lookupImage(Session session, int userID, String search, SearchRange range) throws EucalyptusServiceException;
	public SearchResult lookupKeypair(Session session, int userID, String search, SearchRange range) throws EucalyptusServiceException;
	
	public ArrayList<String> stopInstances(Session session, int userID, List<String> ids) throws EucalyptusServiceException;
	public ArrayList<String> startInstances(Session session, int userID, List<String> ids) throws EucalyptusServiceException;
	public ArrayList<String> terminateInstances(Session session, int userID, List<String> ids) throws EucalyptusServiceException;
		
	public String runInstance(Session session, int userID, String image, String key) throws EucalyptusServiceException;
	public String addKeypair(Session session, int userID, String name) throws EucalyptusServiceException;
	public void importKeypair(Session session, int userID, String name, String key) throws EucalyptusServiceException;
	public void deleteKeypairs(Session session, int userID, List<String> keys) throws EucalyptusServiceException;
	
	public String createSecurityGroup(Session session, int userID, String name, String desc) throws EucalyptusServiceException;
	public void deleteSecurityGroups(Session session, int userID, List<String> names) throws EucalyptusServiceException;
	public SearchResult lookupSecurityGroup(Session session, int userID, String search, SearchRange range) throws EucalyptusServiceException;
	public SearchResult lookupSecurityRule(Session session, int userID, String search, SearchRange range) throws EucalyptusServiceException;
	public void addSecurityRule(Session session, int userID, String group, String fromPort, String toPort, String proto, String ipRange) throws EucalyptusServiceException;
	public void delSecurityRules(Session session, int userID, List<String> groups, List<String> fromPorts, List<String> toPorts, List<String> protos, List<String> ipRanges) throws EucalyptusServiceException;
	
	public void bindImage(Session session, int userID, String id, String sysName, String sysVer) throws EucalyptusServiceException;
	public void unbindImages(Session session, int userID, List<String> ids) throws EucalyptusServiceException;
	
	public ArrayList<String> lookupAvailablityZones(Session session) throws EucalyptusServiceException;
	
	public void associateAddress(Session session, int userID, String ip, String instanceID) throws EucalyptusServiceException;
	public List<String> lookupOwnAddress(Session session, int userID) throws EucalyptusServiceException;
	
	//run via eucatools
	
	public SearchResult lookupNodeCtrl(Session session, String search, SearchRange range) throws EucalyptusServiceException;
  public SearchResult lookupWalrusCtrl(Session session, String search, SearchRange range) throws EucalyptusServiceException;
  public SearchResult lookupClusterCtrl(Session session, String search, SearchRange range) throws EucalyptusServiceException;
  public SearchResult lookupStorageCtrl(Session session, String search, SearchRange range) throws EucalyptusServiceException;
  
  public String uploadImage(Session session, int userID, String file, ImageType type, String bucket, String name, String kernel, String ramdisk) throws EucalyptusServiceException;
  
  public String runInstance(Session session, int userID, String image, String keypair, String vmtype, String group) throws EucalyptusServiceException;
  
  public void registerCluster(Session session, String part, String host, String name) throws EucalyptusServiceException;
  public void deregisterCluster(Session session, String part, String name) throws EucalyptusServiceException;
  public void registerNode(Session session, String host) throws EucalyptusServiceException;
  public void deregisterNode(Session session, String host) throws EucalyptusServiceException;
  public void registerStorage(Session session, String part, String host, String name) throws EucalyptusServiceException;
  public void deregisterStorage(Session session, String part, String name) throws EucalyptusServiceException;
  public void registerWalrus(Session session, String host, String name) throws EucalyptusServiceException;
  public void deregisterWalrus(Session session, String name) throws EucalyptusServiceException;

}

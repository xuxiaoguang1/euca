package com.eucalyptus.webui.client;

import com.eucalyptus.webui.client.place.AccountPlace;
import com.eucalyptus.webui.client.place.ApprovePlace;
import com.eucalyptus.webui.client.place.CPUStatPlace;
import com.eucalyptus.webui.client.place.CertPlace;
import com.eucalyptus.webui.client.place.ClusterCtrlPlace;
import com.eucalyptus.webui.client.place.DeviceBWPlace;
import com.eucalyptus.webui.client.place.DeviceCPUPlace;
import com.eucalyptus.webui.client.place.DeviceDiskPlace;
import com.eucalyptus.webui.client.place.DeviceMemoryPlace;
import com.eucalyptus.webui.client.place.DeviceServerPlace;
import com.eucalyptus.webui.client.place.DeviceIPPlace;
import com.eucalyptus.webui.client.place.DeviceTemplatePlace;
import com.eucalyptus.webui.client.place.DeviceVMPlace;
import com.eucalyptus.webui.client.place.DiskStatPlace;
import com.eucalyptus.webui.client.place.ErrorSinkPlace;
import com.eucalyptus.webui.client.place.ConfigPlace;
import com.eucalyptus.webui.client.place.GroupPlace;
import com.eucalyptus.webui.client.place.ImagePlace;
import com.eucalyptus.webui.client.place.InstancePlace;
import com.eucalyptus.webui.client.place.IndividualPlace;
import com.eucalyptus.webui.client.place.IpPermissionPlace;
import com.eucalyptus.webui.client.place.KeyPlace;
import com.eucalyptus.webui.client.place.KeypairPlace;
import com.eucalyptus.webui.client.place.MemoryStatPlace;
import com.eucalyptus.webui.client.place.NodeCtrlPlace;
import com.eucalyptus.webui.client.place.PolicyPlace;
import com.eucalyptus.webui.client.place.RejectPlace;
import com.eucalyptus.webui.client.place.ReportPlace;
import com.eucalyptus.webui.client.place.SecurityGroupPlace;
import com.eucalyptus.webui.client.place.StartPlace;
import com.eucalyptus.webui.client.place.StorageCtrlPlace;
import com.eucalyptus.webui.client.place.UserAppPlace;
import com.eucalyptus.webui.client.place.UserPlace;
import com.eucalyptus.webui.client.place.VmTypePlace;
import com.eucalyptus.webui.client.place.WalrusCtrlPlace;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;

@WithTokenizers( {
  StartPlace.Tokenizer.class,
  ErrorSinkPlace.Tokenizer.class,
  ConfigPlace.Tokenizer.class,
  AccountPlace.Tokenizer.class,
  VmTypePlace.Tokenizer.class,
  ReportPlace.Tokenizer.class,
  GroupPlace.Tokenizer.class,
  UserPlace.Tokenizer.class,
  IndividualPlace.Tokenizer.class,
  PolicyPlace.Tokenizer.class,
  KeyPlace.Tokenizer.class,
  CertPlace.Tokenizer.class,
  ImagePlace.Tokenizer.class,
  ApprovePlace.Tokenizer.class,
  RejectPlace.Tokenizer.class,
  InstancePlace.Tokenizer.class,
  NodeCtrlPlace.Tokenizer.class,
  ClusterCtrlPlace.Tokenizer.class,
  WalrusCtrlPlace.Tokenizer.class,
  StorageCtrlPlace.Tokenizer.class,
  DeviceServerPlace.Tokenizer.class,
  DeviceCPUPlace.Tokenizer.class,
  DeviceMemoryPlace.Tokenizer.class,
  DeviceDiskPlace.Tokenizer.class,
  DeviceIPPlace.Tokenizer.class,
  DeviceBWPlace.Tokenizer.class,
  DeviceTemplatePlace.Tokenizer.class,
  DeviceVMPlace.Tokenizer.class,
  UserAppPlace.Tokenizer.class,
  SecurityGroupPlace.Tokenizer.class,
  IpPermissionPlace.Tokenizer.class,
  KeypairPlace.Tokenizer.class,
  CPUStatPlace.Tokenizer.class,
  MemoryStatPlace.Tokenizer.class,
  DiskStatPlace.Tokenizer.class,
} )
public interface MainPlaceHistoryMapper extends PlaceHistoryMapper {

}

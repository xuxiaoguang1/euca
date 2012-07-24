package com.eucalyptus.webui.client;

import com.eucalyptus.webui.client.place.AccountPlace;
import com.eucalyptus.webui.client.place.ApprovePlace;
import com.eucalyptus.webui.client.place.CertPlace;
import com.eucalyptus.webui.client.place.ClusterCtrlPlace;
import com.eucalyptus.webui.client.place.ErrorSinkPlace;
import com.eucalyptus.webui.client.place.ConfigPlace;
import com.eucalyptus.webui.client.place.GroupPlace;
import com.eucalyptus.webui.client.place.ImagePlace;
import com.eucalyptus.webui.client.place.InstancePlace;
import com.eucalyptus.webui.client.place.KeyPlace;
import com.eucalyptus.webui.client.place.NodeCtrlPlace;
import com.eucalyptus.webui.client.place.PolicyPlace;
import com.eucalyptus.webui.client.place.RejectPlace;
import com.eucalyptus.webui.client.place.ReportPlace;
import com.eucalyptus.webui.client.place.StartPlace;
import com.eucalyptus.webui.client.place.StorageCtrlPlace;
import com.eucalyptus.webui.client.place.TestPlace;
import com.eucalyptus.webui.client.place.UserPlace;
import com.eucalyptus.webui.client.place.VmTypePlace;
import com.eucalyptus.webui.client.place.WalrusCtrlPlace;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;

@WithTokenizers( {
  InstancePlace.Tokenizer.class,
  TestPlace.Tokenizer.class,
  StartPlace.Tokenizer.class,
  ErrorSinkPlace.Tokenizer.class,
  ConfigPlace.Tokenizer.class,
  AccountPlace.Tokenizer.class,
  VmTypePlace.Tokenizer.class,
  ReportPlace.Tokenizer.class,
  GroupPlace.Tokenizer.class,
  UserPlace.Tokenizer.class,
  PolicyPlace.Tokenizer.class,
  KeyPlace.Tokenizer.class,
  CertPlace.Tokenizer.class,
  ImagePlace.Tokenizer.class,
  ApprovePlace.Tokenizer.class,
  RejectPlace.Tokenizer.class,
  NodeCtrlPlace.Tokenizer.class,
  ClusterCtrlPlace.Tokenizer.class,
  WalrusCtrlPlace.Tokenizer.class,
  StorageCtrlPlace.Tokenizer.class
  
} )
public interface MainPlaceHistoryMapper extends PlaceHistoryMapper {

}

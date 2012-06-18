package com.eucalyptus.webui.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.eucalyptus.webui.client.service.QuickLink;
import com.eucalyptus.webui.client.service.QuickLinkTag;
import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.shared.query.QueryType;

public class Categories {
  
  public static ArrayList<QuickLinkTag> getTags( ) throws EucalyptusServiceException {
    String accountId = "123456";
    String userId = "4567";
    return new ArrayList<QuickLinkTag>( Arrays.asList( new QuickLinkTag( "System", 
                                           new ArrayList<QuickLink>( Arrays.asList( new QuickLink( "Start", "Start guide", "home",
                                                                            QueryBuilder.get( ).start( QueryType.start ).query( ) ),
                                                          new QuickLink( "Service Components", "Configuration of service components", "config",
                                                          		              QueryBuilder.get( ).start( QueryType.config ).query( ) ) ) ) ),
                          new QuickLinkTag( "Identity",
                                           new ArrayList<QuickLink>( Arrays.asList( new QuickLink( "Account", "Accounts", "dollar", 
                                                                            QueryBuilder.get( ).start( QueryType.account ).query( ) ),
                                                          new QuickLink( "Group", "User groups", "group",
                                                                            QueryBuilder.get( ).start( QueryType.group ).add( "accountid", accountId ).query( ) ),
                                                          new QuickLink( "User", "Users", "user",
                                                                            QueryBuilder.get( ).start( QueryType.user ).add( "accountid", accountId ).query( ) ),
                                                          new QuickLink( "Policy", "Policies", "lock",
                                                                            QueryBuilder.get( ).start( QueryType.policy ).add( "userid", userId ).query( ) ),
                                                          new QuickLink( "Key", "Access keys", "key",
                                                                            QueryBuilder.get( ).start( QueryType.key ).add( "userid", userId ).query( ) ),
                                                          new QuickLink( "Certificate", "X509 certificates", "sun",
                                                                            QueryBuilder.get( ).start( QueryType.cert ).add( "userid", userId ).query( ) ) ) ) ),
                          new QuickLinkTag( "Resource",
                                           new ArrayList<QuickLink>( Arrays.asList( new QuickLink( "Image", "Virtual machine images (EMIs)", "image",
                                                                            QueryBuilder.get( ).start( QueryType.image ).query( ) ),
                                                          new QuickLink( "VmType", "Virtual machine types", "type",
                                                                            QueryBuilder.get( ).start( QueryType.vmtype ).query( ) ),
                                                          new QuickLink( "Report", "Resource usage report", "report",
                                                                            QueryBuilder.get( ).start( QueryType.report ).query( ) ) ) ) ) ) );
  }
  
}

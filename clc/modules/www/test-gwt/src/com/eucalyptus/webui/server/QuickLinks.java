package com.eucalyptus.webui.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.eucalyptus.webui.client.service.QuickLink;
import com.eucalyptus.webui.client.service.QuickLinkTag;
import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.shared.query.QueryType;

public class QuickLinks {
  
  
  public static ArrayList<QuickLinkTag> getTags( ) throws EucalyptusServiceException {
      return getSystemAdminTags( );
  }
  
  private static ArrayList<QuickLinkTag> getSystemAdminTags( ) throws EucalyptusServiceException {
    try {
    	return new ArrayList<QuickLinkTag>( Arrays.asList( 
              new QuickLinkTag( "System Management", 
                               new ArrayList<QuickLink>( Arrays.asList(
                                              new QuickLink( "Start", "Start guide", "home",
                                                                QueryBuilder.get( ).start( QueryType.start ).query( ) ),
                                              new QuickLink( "Service Components", "Configuration of service components", "config",
                                                                QueryBuilder.get( ).start( QueryType.config ).query( ) ) ) ) ),
              new QuickLinkTag( "Identity Management",
                               new ArrayList<QuickLink>( Arrays.asList(
                                              new QuickLink( "Accounts", "Accounts", "dollar", 
                                                                QueryBuilder.get( ).start( QueryType.account ).query( ) ),
                                              new QuickLink( "Groups", "User groups", "group",
                                                                QueryBuilder.get( ).start( QueryType.group ).query( ) ),
                                              new QuickLink( "Users", "Users", "user",
                                                                QueryBuilder.get( ).start( QueryType.user ).query( ) ),
                                              new QuickLink( "Policies", "Policies", "lock",
                                                                QueryBuilder.get( ).start( QueryType.policy ).query( ) ),
                                              new QuickLink( "Keys", "Access keys", "key",
                                                                QueryBuilder.get( ).start( QueryType.key ).query( ) ),
                                              new QuickLink( "Certificates", "X509 certificates", "sun",
                                                                QueryBuilder.get( ).start( QueryType.cert ).query( ) ) ) ) ),
              new QuickLinkTag( "Resource Management",
                               new ArrayList<QuickLink>( Arrays.asList(
                            		          new QuickLink( "Images", "Virtual machine images (EMIs)", "image",
                                                                QueryBuilder.get( ).start( QueryType.image ).query( ) ),
                                              new QuickLink( "VmTypes", "Virtual machine types", "type",
                                                                QueryBuilder.get( ).start( QueryType.vmtype ).query( ) ),
                                              new QuickLink( "Usage Report", "Resource usage report", "report",
                                                                QueryBuilder.get( ).start( QueryType.report ).query( ) ) ) )),
              new QuickLinkTag( "Test Panel", 
                               new ArrayList<QuickLink>( Arrays.asList(
                                              new QuickLink( "Link1", "desc", "test", 
                                                                QueryBuilder.get( ).start( QueryType.test ).query( ) ) ) ) ) ) );
    } catch ( Exception e ) { 
      throw new EucalyptusServiceException( "Failed to load user information for ");
    }    
  }


}

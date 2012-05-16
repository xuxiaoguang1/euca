package com.eucalyptus.webui.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.NavigableSet;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.ProxyHost;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import com.eucalyptus.address.AddressingConfiguration;
import com.eucalyptus.bootstrap.HttpServerBootstrapper;
import com.eucalyptus.cluster.ClusterConfiguration;
import com.eucalyptus.component.Component;
import com.eucalyptus.component.Components;
import com.eucalyptus.component.Dispatcher;
import com.eucalyptus.component.Partitions;
import com.eucalyptus.component.ServiceConfiguration;
import com.eucalyptus.component.ServiceConfigurations;
import com.eucalyptus.component.id.ClusterController;
import com.eucalyptus.component.id.Eucalyptus;
import com.eucalyptus.component.id.Storage;
import com.eucalyptus.component.id.Walrus;
import com.eucalyptus.config.ArbitratorConfiguration;
import com.eucalyptus.config.StorageControllerConfiguration;
import com.eucalyptus.config.WalrusConfiguration;
import com.eucalyptus.empyrean.Empyrean.Arbitrator;
import com.eucalyptus.entities.EntityWrapper;
import com.eucalyptus.entities.Transactions;
import com.eucalyptus.event.EventFailedException;
import com.eucalyptus.event.ListenerRegistry;
import com.eucalyptus.event.SystemConfigurationEvent;
import com.eucalyptus.images.ImageConfiguration;
import com.eucalyptus.util.Callback;
import com.eucalyptus.util.DNSProperties;
import com.eucalyptus.util.EucalyptusCloudException;
import com.eucalyptus.util.Internets;
import com.eucalyptus.util.LogUtil;
import com.eucalyptus.util.WalrusProperties;
import com.eucalyptus.webui.client.service.CloudInfo;
import com.eucalyptus.webui.client.service.EucalyptusServiceException;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc.TableDisplay;
import com.eucalyptus.webui.client.service.SearchResultFieldDesc.Type;
import com.eucalyptus.webui.client.service.SearchResultRow;
import com.eucalyptus.ws.client.ServiceDispatcher;
import com.google.common.collect.Lists;
import edu.ucsb.eucalyptus.cloud.entities.SystemConfiguration;
import edu.ucsb.eucalyptus.msgs.ComponentProperty;
import edu.ucsb.eucalyptus.msgs.GetStorageConfigurationResponseType;
import edu.ucsb.eucalyptus.msgs.GetStorageConfigurationType;
import edu.ucsb.eucalyptus.msgs.GetWalrusConfigurationResponseType;
import edu.ucsb.eucalyptus.msgs.GetWalrusConfigurationType;
import edu.ucsb.eucalyptus.msgs.UpdateStorageConfigurationType;
import edu.ucsb.eucalyptus.msgs.UpdateWalrusConfigurationType;


/**
 * Translate system configurations into dumb UI display data, and vice versa. 
 * 
 * @author Ye Wen (wenye@eucalyptus.com)
 */
public class ConfigurationWebBackend {

	public static final String ID = "Id";
	public static final String NAME = "Name";
	public static final String PARTITION = "Partition";
	public static final String TYPE = "Type";
	public static final String HOST = "Host";
	public static final String STATE = "Status";

	public static final String CLOUD_NAME = "cloud";
	public static final String WALRUS_NAME = "walrus";

	public static final String CLOUD_TYPE = "cloud controller";
	public static final String CLUSTER_TYPE = "cluster controller";
	public static final String STORAGE_TYPE = "storage controller";
	public static final String WALRUS_TYPE = "walrus";
	public static final String VMWARE_BROKER_TYPE = "vmware broker";
	public static final String ARBITRATOR_TYPE = "arbitrator";

	public static final String DEFAULT_KERNEL = "Default kernel";
	public static final String DEFAULT_RAMDISK = "Default ramdisk";
	public static final String DNS_DOMAIN = "DNS domain";
	public static final String DNS_NAMESERVER = "DNS nameserver";
	public static final String DNS_IP = "DNS IP";
	public static final String MAX_USER_PUBLIC_ADDRESSES = "Max public addresses per user";
	public static final String ENABLE_DYNAMIC_PUBLIC_ADDRESSES = "Enable dynamic public addresses";
	public static final String SYSTEM_RESERVED_PUBLIC_ADDRESSES = "System reserved public addresses";

	public static final String PORT = "Port";
	public static final String MAX_VLAN = "Max VLAN tag";
	public static final String MIN_VLAN = "Min VLAN tag";

	public static final String GATEWAY_HOST = "Gateway Host";

	public static final String COMPONENT_PROPERTY_TYPE_KEY_VALUE = "KEYVALUE";
	public static final String COMPONENT_PROPERTY_TYPE_KEY_VALUE_HIDDEN = "KEYVALUEHIDDEN";
	public static final String COMPONENT_PROPERTY_TYPE_BOOLEAN = "BOOLEAN";

	public static final int TYPE_FIELD_INDEX = 3;
	public static final String UNKNOWN_STATE = "Unknown";

	// Common fields
	public static final ArrayList<SearchResultFieldDesc> COMMON_FIELD_DESCS = Lists.newArrayList( );
	static {
		COMMON_FIELD_DESCS.add( new SearchResultFieldDesc( ID, ID, false, "0px", TableDisplay.NONE, Type.TEXT, false, true ) );
		COMMON_FIELD_DESCS.add( new SearchResultFieldDesc( NAME, NAME, false, "20%", TableDisplay.MANDATORY, Type.TEXT, false, false ) );
		COMMON_FIELD_DESCS.add( new SearchResultFieldDesc( PARTITION, PARTITION, false, "20%", TableDisplay.MANDATORY, Type.TEXT, false, false ) );
		COMMON_FIELD_DESCS.add( new SearchResultFieldDesc( TYPE, TYPE, false, "20%", TableDisplay.MANDATORY, Type.TEXT, false, false ) );
		COMMON_FIELD_DESCS.add( new SearchResultFieldDesc( "hostName", HOST, false, "20%", TableDisplay.MANDATORY, Type.TEXT, false, false ) );
		COMMON_FIELD_DESCS.add( new SearchResultFieldDesc( "port", PORT, false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ) );
		COMMON_FIELD_DESCS.add( new SearchResultFieldDesc( STATE, STATE, false, "10%", TableDisplay.MANDATORY, Type.TEXT, false, false ) );
	}
	// Cloud config extra fields
	public static final ArrayList<SearchResultFieldDesc> CLOUD_CONFIG_EXTRA_FIELD_DESCS = Lists.newArrayList( );
	static {
		CLOUD_CONFIG_EXTRA_FIELD_DESCS.add( new SearchResultFieldDesc( "dnsDomain", DNS_DOMAIN, false, "0px", TableDisplay.NONE, Type.TEXT, true, false ) );
		CLOUD_CONFIG_EXTRA_FIELD_DESCS.add( new SearchResultFieldDesc( "nameserver", DNS_NAMESERVER, false, "0px", TableDisplay.NONE, Type.TEXT, true, false ) );
		CLOUD_CONFIG_EXTRA_FIELD_DESCS.add( new SearchResultFieldDesc( "nameserverAddress", DNS_IP, false, "0px", TableDisplay.NONE, Type.TEXT, true, false ) );
		CLOUD_CONFIG_EXTRA_FIELD_DESCS.add( new SearchResultFieldDesc( "defaultKernel", DEFAULT_KERNEL, false, "0px", TableDisplay.NONE, Type.TEXT, true, false ) );
		CLOUD_CONFIG_EXTRA_FIELD_DESCS.add( new SearchResultFieldDesc( "defaultRamdisk", DEFAULT_RAMDISK, false, "0px", TableDisplay.NONE, Type.TEXT, true, false ) );
		CLOUD_CONFIG_EXTRA_FIELD_DESCS.add( new SearchResultFieldDesc( "doDynamicPublicAddresses", ENABLE_DYNAMIC_PUBLIC_ADDRESSES, false, "0px", TableDisplay.NONE, Type.BOOLEAN, true, false ) );
		//CLOUD_CONFIG_EXTRA_FIELD_DESCS.add( new SearchResultFieldDesc( "maxUserPublicAddresses", MAX_USER_PUBLIC_ADDRESSES, false, "0px", TableDisplay.NONE, Type.TEXT, true, false ) );
		CLOUD_CONFIG_EXTRA_FIELD_DESCS.add( new SearchResultFieldDesc( "systemReservedPublicAddresses", SYSTEM_RESERVED_PUBLIC_ADDRESSES, false, "0px", TableDisplay.NONE, Type.TEXT, true, false ) );
	}
	// Cluster config extra fields
	public static final ArrayList<SearchResultFieldDesc> CLUSTER_CONFIG_EXTRA_FIELD_DESCS = Lists.newArrayList( );
	static {
		CLUSTER_CONFIG_EXTRA_FIELD_DESCS.add( new SearchResultFieldDesc( "minVlan", MIN_VLAN, false, "0px", TableDisplay.NONE, Type.TEXT, false, false ) );
		CLUSTER_CONFIG_EXTRA_FIELD_DESCS.add( new SearchResultFieldDesc( "maxVlan", MAX_VLAN, false, "0px", TableDisplay.NONE, Type.TEXT, false, false ) );
	}

	public static final ArrayList<SearchResultFieldDesc> ARBITRATOR_CONFIG_EXTRA_FIELD_DESCS = Lists.newArrayList( );
	static {
		ARBITRATOR_CONFIG_EXTRA_FIELD_DESCS.add( new SearchResultFieldDesc( "gatewayHost", GATEWAY_HOST, false, "0px", TableDisplay.NONE, Type.TEXT, true, false ) );
	}

	public static final String SC_DEFAULT_NAME = "sc-default";
	public static final String SC_DEFAULT_HOST = "sc-default";
	public static final Integer SC_DEFAULT_PORT = 8773;

	private static final Logger LOG = Logger.getLogger( ConfigurationWebBackend.class );

	private static String makeConfigId( String name, String type ) {
		return type + "." + name;
	}

	private static void serializeSystemConfiguration( SystemConfiguration sysConf, ServiceConfiguration cloud, SearchResultRow result ) {
		// First fill in the common fields
		result.addField( makeConfigId( cloud.getName(), CLOUD_TYPE ) );// id
		result.addField( cloud.getName() );                            // name  
		result.addField( cloud.getPartition() );                            // partition  
		result.addField( CLOUD_TYPE );                            // type
		result.addField( cloud.getHostName() );         // host
		result.addField( cloud.getPort( ) == null ? null : cloud.getPort( ).toString( ) );
		result.addField( cloud.lookupState().toString( ) );
		// Then fill in the specific fields
		result.addField( sysConf.getDnsDomain( ) );               // dns domain
		result.addField( sysConf.getNameserver( ) );              // dns nameserver
		result.addField( sysConf.getNameserverAddress( ) );       // dns IP
		result.addField( ImageConfiguration.getInstance( ).getDefaultKernelId( ) );           // default kernel
		result.addField( ImageConfiguration.getInstance( ).getDefaultRamdiskId( ) );          // default ramdisk
		result.addField( AddressingConfiguration.getInstance( ).getDoDynamicPublicAddresses( ).toString( ) );// enable dynamic public addresses
		result.addField( AddressingConfiguration.getInstance( ).getSystemReservedPublicAddresses( ).toString( ) ); // system reserved addresses
	}

	/**
	 * @return the cloud configuration row with its own specific fields for UI display.
	 */
	public static List<SearchResultRow> getCloudConfigurations( ) {
		List<SearchResultRow> results = Lists.newArrayList( );
		SystemConfiguration sysConf = SystemConfiguration.getSystemConfiguration( );
		NavigableSet<ServiceConfiguration> configs = Components.lookup(Eucalyptus.class).services();
		for ( ServiceConfiguration c : configs ) {
			SearchResultRow row = new SearchResultRow( );
			// Set the extra field descs
			row.setExtraFieldDescs( CLOUD_CONFIG_EXTRA_FIELD_DESCS );
			// Fill the fields
			serializeSystemConfiguration( sysConf, c, row ); 
			results.add(row);
		}
		return results;
	}

	private static void deserializeSystemConfiguration( SystemConfiguration sysConf, SearchResultRow input, int index ) {
		sysConf.setDnsDomain( input.getField( index++ ) );
		sysConf.setNameserver( input.getField( index++ ) );
		sysConf.setNameserverAddress( input.getField( index++ ) );
	}

	/**
	 * Set the cloud configuration using UI input.
	 * 
	 * @param input
	 */
	public static void setCloudConfiguration( final SearchResultRow input ) throws EucalyptusServiceException {
		final int i = COMMON_FIELD_DESCS.size( );
		EntityWrapper<SystemConfiguration> db = EntityWrapper.get( SystemConfiguration.class );
		SystemConfiguration sysConf = null;
		try {
			sysConf = db.getUnique( new SystemConfiguration( ) );
			deserializeSystemConfiguration( sysConf, input, i );
			db.commit( );
			DNSProperties.update( );      
		} catch ( EucalyptusCloudException e ) {
			try {
				LOG.debug( e, e );
				sysConf = new SystemConfiguration( );
				deserializeSystemConfiguration( sysConf, input, i );
				db.persist( sysConf );
				db.commit( );
				DNSProperties.update( );
			} catch ( Exception e1 ) {
				LOG.error( "Failed to set system configuration", e1 );
				throw new EucalyptusServiceException( "Failed to set system configuration", e1 );
			}
		}
		try {
			ListenerRegistry.getInstance( ).fireEvent( new SystemConfigurationEvent( sysConf ) );
		} catch ( EventFailedException e ) {
			LOG.debug( e, e );
		}
		final int j = i + 3;
		try {
			Transactions.one( ImageConfiguration.getInstance( ), new Callback<ImageConfiguration>( ) {
				@Override
				public void fire( ImageConfiguration t ) {
					int n = j;
					t.setDefaultKernelId( input.getField( n++ ) );
					t.setDefaultRamdiskId( input.getField( n++ ) );
				}
			} );
		} catch ( ExecutionException e ) {
			LOG.error( "Failed to set image configuration", e );
			LOG.debug( e, e );
			throw new EucalyptusServiceException( "Failed to set image configuration", e );
		}
		final int k = j + 2;
		try {
			Transactions.one( AddressingConfiguration.getInstance( ), new Callback<AddressingConfiguration>( ) {

				@Override
				public void fire( AddressingConfiguration t ) {
					int n = k;
					t.setDoDynamicPublicAddresses( Boolean.parseBoolean( input.getField( n++ ) ) );
					try {
						Integer val = Integer.parseInt( input.getField( n++ ) );
						if ( val > 0 ) {
							t.setSystemReservedPublicAddresses( val );
						}
					} catch ( Exception e ) {
						LOG.error( e, e );
					}
				}
			} );
		} catch ( ExecutionException e ) {
			LOG.error( "Failed to set addressing configuration", e );
			LOG.debug( e, e );
			throw new EucalyptusServiceException( "Failed to set image configuration", e );
		}    
	}

	private static void serializeClusterConfiguration( ServiceConfiguration serviceConf, String minNetworkTag, String maxNetworkTag, SearchResultRow result ) {
		// Common
		result.addField( makeConfigId( serviceConf.getName( ), CLUSTER_TYPE ) );
		result.addField( serviceConf.getName( ) );
		result.addField( serviceConf.getPartition( ) );
		result.addField( CLUSTER_TYPE );
		result.addField( serviceConf.getHostName( ) );
		result.addField( serviceConf.getPort( ) == null ? null : serviceConf.getPort( ).toString( ) );
		result.addField( serviceConf.lookupState().toString( ) );
		// Specific
		result.addField( minNetworkTag );
		result.addField( maxNetworkTag );
	}

	/**
	 * @return the list of cluster configurations for UI display.
	 */
	public static List<SearchResultRow> getClusterConfigurations( ) {
		List<SearchResultRow> results = Lists.newArrayList( );
		HashMap<String, List<String>> configProps = new HashMap<String, List<String>>();
		NavigableSet<ServiceConfiguration> configs = Components.lookup(ClusterController.class).services();
		for (ServiceConfiguration c : configs ) {
			if (!configProps.containsKey(c.getPartition())) {
				//This is bad. We should never directly have to cast
				//This entire class should never reference backend props directly

				//Pick the first one. It shouldn't matter which one.
				ClusterConfiguration config = (ClusterConfiguration) c;
				List<String> props = new ArrayList<String>();
				props.add(config.getMinNetworkTag() == null ? "0" : config.getMinNetworkTag().toString());
				props.add(config.getMaxNetworkTag() == null ? "0" : config.getMaxNetworkTag().toString());
				configProps.put(c.getPartition(), props);
			}			
		}
		for (ServiceConfiguration c : configs ) {
			List<String> props = configProps.get(c.getPartition());
			if(props != null) {
				SearchResultRow row = new SearchResultRow( );
				row.setExtraFieldDescs( CLUSTER_CONFIG_EXTRA_FIELD_DESCS );
				//ugly: this is temporary and needs to go away
				serializeClusterConfiguration( c, props.get(0), props.get(1), row );
				results.add( row );
			} else {
				LOG.debug( "Got an error while trying to retrieving cluster configuration list");
			}	
		}
		return results;
	}

	private static void deserializeClusterConfiguration( ServiceConfiguration serviceConf, SearchResultRow input ) {
		ClusterConfiguration clusterConf = ( ClusterConfiguration ) serviceConf;//NOTE:GRZE: depending on referencing the Cluster-specific configuration type is not a safe assumption as that is a component-private type
		int i = COMMON_FIELD_DESCS.size( );
		try {
			Integer val = Integer.parseInt( input.getField( i++ ) );
			clusterConf.setMinNetworkTag( val );
		} catch ( Exception e ) { }
		try {
			Integer val = Integer.parseInt( input.getField( i++ ) );
			clusterConf.setMaxNetworkTag( val );
		} catch ( Exception e ) { }
	}


	private static void serializeVMwareBrokerConfiguration( ServiceConfiguration serviceConf, SearchResultRow result ) {
		// Common
		result.addField( makeConfigId( serviceConf.getName( ), VMWARE_BROKER_TYPE ) );
		result.addField( serviceConf.getName( ) );
		result.addField( serviceConf.getPartition( ) );
		result.addField( VMWARE_BROKER_TYPE );
		result.addField( serviceConf.getHostName( ) );
		result.addField( serviceConf.getPort( ) == null ? null : serviceConf.getPort( ).toString( ) );
		result.addField( serviceConf.lookupState().toString( ) );
	}

	/**
	 * @return the list of VMware broker configurations for UI display.
	 */
	public static List<SearchResultRow> getVMwareBrokerConfigurations( ) {
		List<SearchResultRow> results = Lists.newArrayList( );
		try {
			NavigableSet<ServiceConfiguration> configs = Components.lookup(VMWARE_BROKER_TYPE).services();
			for (ServiceConfiguration c : configs ) {
				SearchResultRow row = new SearchResultRow( );
				serializeVMwareBrokerConfiguration( c, row );
				results.add( row );
			}
		} catch ( Exception e ) { }
		return results;
	}

	private static void serializeArbitratorConfiguration( ServiceConfiguration serviceConf, String gatewayHost, SearchResultRow result ) {
		// Common
		result.addField( makeConfigId( serviceConf.getName( ), ARBITRATOR_TYPE ) );
		result.addField( serviceConf.getName( ) );
		result.addField( serviceConf.getPartition( ) );
		result.addField( ARBITRATOR_TYPE );
		result.addField( serviceConf.getHostName( ) );
		result.addField( serviceConf.getPort( ) == null ? null : serviceConf.getPort( ).toString( ) );
		result.addField( serviceConf.lookupState().toString( ) );
		result.addField( gatewayHost );
	}

	private static void deserializeArbitratorConfiguration( ServiceConfiguration serviceConf, SearchResultRow input ) {
		ArbitratorConfiguration arbConfig = ( ArbitratorConfiguration ) serviceConf;//NOTE: depending on referencing the specific configuration type is not a safe assumption as that is a component-private type
		int i = COMMON_FIELD_DESCS.size( );
		try {
			String val = input.getField(i++);
			arbConfig.setGatewayHost(val);
		} catch ( Exception e ) { 
			LOG.error(e, e);
		}
	}

	/**
	 * @return the list of Arbitrator configurations for UI display.
	 */
	public static List<SearchResultRow> getArbitratorConfigurations( ) {
		List<SearchResultRow> results = Lists.newArrayList( );
		NavigableSet<ServiceConfiguration> configs = Components.lookup(Arbitrator.class).services();
		for (ServiceConfiguration c : configs ) {
			ArbitratorConfiguration arbConfig = (ArbitratorConfiguration) c;
			SearchResultRow row = new SearchResultRow( );
			row.setExtraFieldDescs( ARBITRATOR_CONFIG_EXTRA_FIELD_DESCS );
			serializeArbitratorConfiguration( c, arbConfig.getGatewayHost(), row );
			results.add( row );
		}
		return results;
	}

	/**
	 * Set the cluster configuration using the UI input.
	 * 
	 * @param input
	 */
	public static void setClusterConfiguration( final SearchResultRow input ) throws EucalyptusServiceException {
		try {
			//set props for all in the same partition
			NavigableSet<ServiceConfiguration> configs = Components.lookup(ClusterController.class).services();
			for ( ServiceConfiguration c : configs ) {
				if (input.getField(2).equals(c.getPartition())) {
					deserializeClusterConfiguration( c, input );
					EntityWrapper.get( c ).mergeAndCommit( c );
				}
			}
		} catch ( Exception e ) {
			LOG.error( "Failed to set cluster configuration" );
			LOG.debug( e, e );
			throw new EucalyptusServiceException( "Failed to set cluster configuration", e );
		}
	}

	/**
	 * Set the VMware broker configuration using the UI input.
	 * 
	 * @param input
	 */
	public static void setVMwareBrokerConfiguration( final SearchResultRow input ) throws EucalyptusServiceException {
		//Do nothing for now. Revisit.
	}

	/**
	 * Set the Arbitrator configuration using the UI input.
	 * 
	 * @param input
	 */
	public static void setArbitratorConfiguration( final SearchResultRow input ) throws EucalyptusServiceException {
		try {
			//set props for all in the same partition
			NavigableSet<ServiceConfiguration> configs = Components.lookup(Arbitrator.class).services();
			for ( ServiceConfiguration c : configs ) {
				if (input.getField(2).equals(c.getPartition())) {
					deserializeArbitratorConfiguration( c, input );
					EntityWrapper.get( c ).mergeAndCommit( c );
				}
			}
		} catch ( Exception e ) {
			LOG.error( "Failed to set arbitrator configuration" );
			LOG.debug( e, e );
			throw new EucalyptusServiceException( "Failed to set arbitrator configuration", e );
		}
	}

	private static Type propertyTypeToFieldType( String propertyType ) {
		if ( COMPONENT_PROPERTY_TYPE_KEY_VALUE.equals( propertyType ) ) {
			return Type.TEXT;
		} else if ( COMPONENT_PROPERTY_TYPE_KEY_VALUE_HIDDEN.equals( propertyType ) ) {
			return Type.HIDDEN;
		} else if ( COMPONENT_PROPERTY_TYPE_BOOLEAN.equals( propertyType ) ) {
			return Type.BOOLEAN;
		}
		return Type.TEXT;
	}

	private static String fieldTypeToPropertyType( Type fieldType ) {
		if ( Type.TEXT == fieldType ) {
			return COMPONENT_PROPERTY_TYPE_KEY_VALUE;
		} else if ( Type.HIDDEN == fieldType ) {
			return COMPONENT_PROPERTY_TYPE_KEY_VALUE_HIDDEN;
		} else if ( Type.BOOLEAN == fieldType ) {
			return COMPONENT_PROPERTY_TYPE_BOOLEAN;
		}
		return COMPONENT_PROPERTY_TYPE_KEY_VALUE;
	}

	private static void serializeStorageConfiguration( String type, String name, String partition, String host, Integer port, List<ComponentProperty> properties, String state, SearchResultRow result ) {
		// Common fields
		result.addField( makeConfigId( name, type ) );
		result.addField( name );
		result.addField( partition );
		result.addField( type );
		result.addField( host );
		result.addField( port == null ? null : port.toString( ) );
		result.addField(state);
		// Dynamic fields
		serializeComponentProperties( properties, result );
	}

	private static void serializeComponentProperties( List<ComponentProperty> properties, SearchResultRow result ) {
		// Make sure we see consistent order of properties.
		Collections.<ComponentProperty>sort( properties, new Comparator<ComponentProperty>( ) {
			@Override
			public int compare( ComponentProperty r1, ComponentProperty r2 ) {
				if ( r1 == r2 ) {
					return 0;
				}
				int diff = -1;
				if ( r1 != null ) {
					diff = ( r2 != null ) ? r1.getDisplayName( ).compareTo( r2.getDisplayName( ) ) : 1;
				}
				return diff;
			}
		} );
		for ( ComponentProperty prop : properties ) {
			result.addExtraFieldDesc( new SearchResultFieldDesc( prop.getQualifiedName( ), prop.getDisplayName( ), false, "0px", TableDisplay.NONE, propertyTypeToFieldType( prop.getType( ) ), true, false ) );
			result.addField( prop.getValue( ) );
		}
	}

	private static void deserializeComponentProperties( List<ComponentProperty> properties, SearchResultRow input, int startIndex ) {
		int i = startIndex;
		for ( SearchResultFieldDesc desc : input.getExtraFieldDescs( ) ) {
			properties.add( new ComponentProperty( fieldTypeToPropertyType( desc.getType( ) ), desc.getTitle( ), input.getField( i++ ), desc.getName( ) ) );
		}
	}

	private static SearchResultRow createStorageConfiguration( String type, String name, String partition, String host, Integer port, List<ComponentProperty> properties, String state ) {
		SearchResultRow result = new SearchResultRow( );
		serializeStorageConfiguration( type, name, partition, host, port, properties, state, result );
		return result;
	}

	/**
	 * @return the storage configurations for UI display.
	 */
	public static List<SearchResultRow> getStorageConfigurations( ) {
		List<SearchResultRow> results = Lists.newArrayList( );
		//StorageControllerConfiguration c;
		HashMap<String, List<ComponentProperty>> configMap = new HashMap<String, List<ComponentProperty>> (); 

		NavigableSet<ServiceConfiguration> configs = Components.lookup(Storage.class).services();
		for ( ServiceConfiguration c : configs ) {
			if(Component.State.ENABLED.equals(c.lookupState())) {
				//send for config and add result row
				List<ComponentProperty> properties = Lists.newArrayList( );
				try {
					GetStorageConfigurationResponseType getStorageConfigResponse = sendForStorageInfo( c );
					if ( c.getPartition( ).equals( getStorageConfigResponse.getName( ) ) ) {
						properties.addAll( getStorageConfigResponse.getStorageParams( ) );
					} else {
						LOG.debug( "Unexpected storage controller name: " + getStorageConfigResponse.getName( ), new Exception( ) );
						LOG.debug( "Expected configuration for SC related to CC: " + LogUtil.dumpObject( c ) );
						LOG.debug( "Received configuration for SC related to CC: " + LogUtil.dumpObject( getStorageConfigResponse ) );
					}
				} catch ( Exception e ) {
					LOG.debug( "Got an error while trying to communicate with remote storage controller", e );
				}
				configMap.put(c.getPartition(), properties );    	
			}
		}

		for ( ServiceConfiguration c : configs ) {
			//add result row corresponding to partition.
			List<ComponentProperty> properties = configMap.get(c.getPartition());
			if(properties != null) {
				results.add( createStorageConfiguration( STORAGE_TYPE, c.getName( ), c.getPartition( ), c.getHostName( ), c.getPort( ), properties, c.lookupState().toString() ) );
			} else {
				results.add( createStorageConfiguration( STORAGE_TYPE, SC_DEFAULT_NAME, SC_DEFAULT_NAME, SC_DEFAULT_HOST, SC_DEFAULT_PORT, new ArrayList<ComponentProperty>( ), UNKNOWN_STATE ) );
			}
		}    	
		return results;
	}

	/**
	 * Set properties for a storage controller from UI input.
	 * 
	 * @param input
	 */
	public static void setStorageConfiguration( SearchResultRow input ) throws EucalyptusServiceException  {
		int i = 0;
		i++;//id
		String name = input.getField( i++ );
		String partition = input.getField( i++ );
		i++;//type
		String host = input.getField( i++ );
		Integer port = null;
		try {
			port = Integer.parseInt( input.getField( i++ ) );
		} catch ( Exception e ) {
			LOG.error( "Failed to parse port for storage configuration from UI input" );
			return;
		}
		i++; //status
		ArrayList<ComponentProperty> properties = Lists.newArrayList( );
		deserializeComponentProperties( properties, input, i );

		//Get enabled component for partition. Send to enabled component corresponding to partition.
		NavigableSet<ServiceConfiguration> configs = Components.lookup(Storage.class).services();
		for ( ServiceConfiguration c : configs ) {
			if ( partition.equals(c.getPartition()) && Component.State.ENABLED.equals(c.lookupState())) {
				final UpdateStorageConfigurationType updateStorageConfiguration = new UpdateStorageConfigurationType( );
				updateStorageConfiguration.setName( c.getPartition( ) );
				updateStorageConfiguration.setStorageParams( properties );
				Dispatcher scDispatch = ServiceDispatcher.lookup( c );
				try {
					scDispatch.send( updateStorageConfiguration );
				} catch ( Exception e ) {
					LOG.error( "Error sending update configuration message to storage controller: " + updateStorageConfiguration );
					LOG.error( "The storage controller's configuration may be out of sync!" );
					LOG.debug( e, e );
					throw new EucalyptusServiceException( "Failed to update storage configuration", e ); 
				}
			}
		}
	}

	private static GetStorageConfigurationResponseType sendForStorageInfo( ServiceConfiguration c ) throws EucalyptusCloudException {
		GetStorageConfigurationType getStorageConfiguration = new GetStorageConfigurationType( c.getPartition( ) );
		Dispatcher scDispatch = ServiceDispatcher.lookup( c );
		GetStorageConfigurationResponseType getStorageConfigResponse = scDispatch.send( getStorageConfiguration );
		return getStorageConfigResponse;
	}

	/**
	 * @return the list of Walrus configurations for UI display.
	 */
	public static List<SearchResultRow> getWalrusConfigurations( ) {
		List<SearchResultRow> results = new ArrayList<SearchResultRow>( );
		HashMap<String, List<ComponentProperty>> configMap = new HashMap<String, List<ComponentProperty>> (); 
		NavigableSet<ServiceConfiguration> configs = Components.lookup(Walrus.class).services();
		for ( ServiceConfiguration c : configs ) {
			if(Component.State.ENABLED.equals(c.lookupState())) {
				//send for config and add result row
				List<ComponentProperty> properties = Lists.newArrayList( );

				try {
					GetWalrusConfigurationType getWalrusConfiguration = new GetWalrusConfigurationType( c.getPartition() );
					Dispatcher walrusDispatch = ServiceDispatcher.lookup( c );
					GetWalrusConfigurationResponseType getWalrusConfigResponse = walrusDispatch.send( getWalrusConfiguration );
					configMap.put( c.getPartition(), getWalrusConfigResponse.getProperties( ));
				} catch ( Exception ex ) {
					LOG.error( "Failed to retrieve walrus configuration", ex );
					LOG.debug( ex , ex );
				}
			}
		}

		for ( ServiceConfiguration c : configs ) {
			List<ComponentProperty> properties = configMap.get(c.getPartition()); 
			if (properties != null) {
				results.add( createStorageConfiguration( WALRUS_TYPE, c.getName( ), c.getPartition( ), c.getHostName( ), c.getPort( ), properties, c.lookupState().toString() ) );
			}
		}
		return results;    
	}

	/**
	 * Set Walrus configuration using UI input.
	 *	 
	 * @param input
	 */
	public static void setWalrusConfiguration( SearchResultRow input ) throws EucalyptusServiceException  {
		ArrayList<ComponentProperty> properties = Lists.newArrayList( );
		deserializeComponentProperties( properties, input, COMMON_FIELD_DESCS.size( ) );

		NavigableSet<ServiceConfiguration> configs = Components.lookup(Walrus.class).services();
		for ( ServiceConfiguration c : configs ) {
			if ( input.getField(2).equals(c.getPartition()) && Component.State.ENABLED.equals(c.lookupState())) {
				UpdateWalrusConfigurationType updateWalrusConfiguration = new UpdateWalrusConfigurationType( );
				updateWalrusConfiguration.setName( c.getPartition() );
				updateWalrusConfiguration.setProperties( properties );
				Dispatcher scDispatch = ServiceDispatcher.lookup( c );
				try {
					scDispatch.send( updateWalrusConfiguration );
				} catch ( Exception e ) {
					LOG.error( "Failed to set Walrus configuration", e );
					LOG.debug( e, e );
					throw new EucalyptusServiceException( "Failed to set Walrus configuration", e );
				}
			}
		}

	}

	private static String getExternalIpAddress ( ) {
		String ipAddr = null;
		HttpClient httpClient = new HttpClient( );
		//support for http proxy
		if( HttpServerBootstrapper.httpProxyHost != null && ( HttpServerBootstrapper.httpProxyHost.length( ) > 0 ) ) {
			String proxyHost = HttpServerBootstrapper.httpProxyHost;
			if( HttpServerBootstrapper.httpProxyPort != null && ( HttpServerBootstrapper.httpProxyPort.length( ) > 0 ) ) {
				int proxyPort = Integer.parseInt( HttpServerBootstrapper.httpProxyPort );
				httpClient.getHostConfiguration( ).setProxy( proxyHost, proxyPort );
			} else {
				httpClient.getHostConfiguration( ).setProxyHost( new ProxyHost( proxyHost ) );
			}
		}
		// Use Rightscale's "whoami" service
		String whoamiUrl = WebProperties.getProperty( WebProperties.RIGHTSCALE_WHOAMI_URL, WebProperties.RIGHTSCALE_WHOAMI_URL_DEFAULT );
		GetMethod method = new GetMethod( whoamiUrl );
		Integer timeoutMs = new Integer( 3 * 1000 ); // TODO: is this working?
		method.getParams( ).setSoTimeout( timeoutMs );

		try {
			httpClient.executeMethod( method );
			String str = "";
			InputStream in = method.getResponseBodyAsStream( );
			byte[] readBytes = new byte[1024];
			int bytesRead = -1;
			while ( ( bytesRead = in.read( readBytes ) ) > 0) {
				str += new String( readBytes, 0, bytesRead );
			}
			Matcher matcher = Pattern.compile( ".*your ip is (.*)" ).matcher( str );
			if ( matcher.find( ) ) {
				ipAddr = matcher.group( 1 );
			}

		} catch ( MalformedURLException e ) {
			LOG.warn( "Malformed URL exception: " + e.getMessage( ) );
			LOG.debug( e, e );
		} catch ( IOException e ) {
			LOG.warn( "I/O exception: " + e.getMessage( ) );
			LOG.debug( e, e );
		} finally {
			method.releaseConnection( );
		}

		return ipAddr;
	}

	public static final String CLOUD_PORT = "8443";

	public static CloudInfo getCloudInfo( boolean setExternalHostPort ) throws EucalyptusServiceException {
		String cloudRegisterId = null;
		cloudRegisterId = SystemConfiguration.getSystemConfiguration().getRegistrationId( );
		CloudInfo cloudInfo = new CloudInfo( );
		cloudInfo.setInternalHostPort (Internets.localHostInetAddress( ).getHostAddress( ) + ":" + CLOUD_PORT );
		if ( setExternalHostPort ) {
			String ipAddr = getExternalIpAddress( );
			if ( ipAddr != null ) {
				cloudInfo.setExternalHostPort ( ipAddr + ":" + CLOUD_PORT );
			}
		}
		cloudInfo.setServicePath( "/register" ); // TODO: what is the actual cloud registration service?
		cloudInfo.setCloudId( cloudRegisterId ); // TODO: what is the actual cloud registration ID?
		return cloudInfo;
	}

}

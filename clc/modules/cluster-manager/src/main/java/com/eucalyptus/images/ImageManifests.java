/*******************************************************************************
 * Copyright (c) 2009  Eucalyptus Systems, Inc.
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, only version 3 of the License.
 * 
 * 
 *  This file is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 * 
 *  You should have received a copy of the GNU General Public License along
 *  with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *  Please contact Eucalyptus Systems, Inc., 130 Castilian
 *  Dr., Goleta, CA 93101 USA or visit <http://www.eucalyptus.com/licenses/>
 *  if you need additional information or have any questions.
 * 
 *  This file may incorporate work covered under the following copyright and
 *  permission notice:
 * 
 *    Software License Agreement (BSD License)
 * 
 *    Copyright (c) 2008, Regents of the University of California
 *    All rights reserved.
 * 
 *    Redistribution and use of this software in source and binary forms, with
 *    or without modification, are permitted provided that the following
 *    conditions are met:
 * 
 *      Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * 
 *      Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 * 
 *    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 *    IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *    TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 *    PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 *    OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *    EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *    PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *    PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *    NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. USERS OF
 *    THIS SOFTWARE ACKNOWLEDGE THE POSSIBLE PRESENCE OF OTHER OPEN SOURCE
 *    LICENSED MATERIAL, COPYRIGHTED MATERIAL OR PATENTED MATERIAL IN THIS
 *    SOFTWARE, AND IF ANY SUCH MATERIAL IS DISCOVERED THE PARTY DISCOVERING
 *    IT MAY INFORM DR. RICH WOLSKI AT THE UNIVERSITY OF CALIFORNIA, SANTA
 *    BARBARA WHO WILL THEN ASCERTAIN THE MOST APPROPRIATE REMEDY, WHICH IN
 *    THE REGENTS' DISCRETION MAY INCLUDE, WITHOUT LIMITATION, REPLACEMENT
 *    OF THE CODE SO IDENTIFIED, LICENSING OF THE CODE SO IDENTIFIED, OR
 *    WITHDRAWAL OF THE CODE CAPABILITY TO THE EXTENT NEEDED TO COMPLY WITH
 *    ANY SUCH LICENSES OR RIGHTS.
 *******************************************************************************
 * @author chris grzegorczyk <grze@eucalyptus.com>
 */

package com.eucalyptus.images;

import java.io.ByteArrayInputStream;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.NoSuchElementException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.eucalyptus.auth.Accounts;
import com.eucalyptus.auth.AuthException;
import com.eucalyptus.auth.policy.PolicySpec;
import com.eucalyptus.auth.principal.User;
import com.eucalyptus.cloud.ImageMetadata;
import com.eucalyptus.cloud.ImageMetadata.DeviceMappingType;
import com.eucalyptus.component.Components;
import com.eucalyptus.component.auth.SystemCredentials;
import com.eucalyptus.component.id.Eucalyptus;
import com.eucalyptus.component.id.Walrus;
import com.eucalyptus.context.Context;
import com.eucalyptus.context.Contexts;
import com.eucalyptus.crypto.util.B64;
import com.eucalyptus.util.EucalyptusCloudException;
import com.eucalyptus.util.FullName;
import com.eucalyptus.util.RestrictedTypes;
import com.eucalyptus.ws.client.ServiceDispatcher;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import edu.ucsb.eucalyptus.msgs.GetBucketAccessControlPolicyResponseType;
import edu.ucsb.eucalyptus.msgs.GetBucketAccessControlPolicyType;
import edu.ucsb.eucalyptus.msgs.GetObjectResponseType;
import edu.ucsb.eucalyptus.msgs.GetObjectType;

public class ImageManifests {
  private static Logger LOG = Logger.getLogger( ImageManifests.class );
  
  static boolean verifyBucketAcl( String bucketName ) {
    Context ctx = Contexts.lookup( );
    GetBucketAccessControlPolicyType getBukkitInfo = new GetBucketAccessControlPolicyType( );
    getBukkitInfo.setBucket( bucketName );
    try {
      GetBucketAccessControlPolicyResponseType reply = ( GetBucketAccessControlPolicyResponseType ) ServiceDispatcher.lookupSingle( Components.lookup( Walrus.class ) ).send( getBukkitInfo );
      String ownerName = reply.getAccessControlPolicy( ).getOwner( ).getDisplayName( );
      return ctx.getUserFullName( ).getAccountNumber( ).equals( ownerName ) || ctx.getUserFullName( ).getUserId( ).equals( ownerName );
    } catch ( EucalyptusCloudException ex ) {
      LOG.error( ex, ex );
    } catch ( NoSuchElementException ex ) {
      LOG.error( ex, ex );
    }
    return false;
  }
  
  private static boolean verifyManifestSignature( final X509Certificate cert, final String signature, String pad ) {
    Signature sigVerifier;
    try {
      sigVerifier = Signature.getInstance( "SHA1withRSA" );
      PublicKey publicKey = cert.getPublicKey( );
      sigVerifier.initVerify( publicKey );
      sigVerifier.update( ( pad ).getBytes( ) );
      return sigVerifier.verify( hexToBytes( signature ) );
    } catch ( Exception ex ) {
      LOG.error( ex, ex );
      return false;
    }
  }
  
  private static byte[] hexToBytes( String data ) {
    int k = 0;
    byte[] results = new byte[data.length( ) / 2];
    for ( int i = 0; i < data.length( ); ) {
      results[k] = ( byte ) ( Character.digit( data.charAt( i++ ), 16 ) << 4 );
      results[k] += ( byte ) ( Character.digit( data.charAt( i++ ), 16 ) );
      k++;
    }
    
    return results;
  }
  
  static String requestManifestData( FullName userName, String bucketName, String objectName ) throws EucalyptusCloudException {
    GetObjectResponseType reply = null;
    try {
      GetObjectType msg = new GetObjectType( bucketName, objectName, true, false, true );
//TODO:GRZE:WTF.      
//      User user = Accounts.lookupUserById( userName.getNamespace( ) );
//      msg.setUserId( user.getName( ) );
      msg.regarding( );
      msg.setCorrelationId( Contexts.lookup( ).getRequest( ).getCorrelationId( ) );
      reply = ( GetObjectResponseType ) ServiceDispatcher.lookupSingle( Components.lookup( Walrus.class ) ).send( msg );
    } catch ( Exception e ) {
      throw new EucalyptusCloudException( "Failed to read manifest file: " + bucketName + "/" + objectName, e );
    }
    return B64.url.decString( reply.getBase64Data( ).getBytes( ) );
  }
  
  public static class ManifestDeviceMapping {
    ManifestDeviceMapping( DeviceMappingType type, String virtualName, String deviceName ) {
      super( );
      this.type = type;
      this.virtualName = virtualName;
      this.deviceName = deviceName;
    }
    
    DeviceMappingType type;
    String            virtualName;
    String            deviceName;
    
    public DeviceMapping generateRealMapping( ImageInfo parent ) {
      if ( DeviceMappingType.ephemeral.equals( type ) ) {
        return new EphemeralDeviceMapping( parent, deviceName, virtualName );
      } else {//if( DeviceMappingType.root.equals( type ) || DeviceMappingType.swap.equals( type ) ) {
        return new DeviceMapping( parent, type, deviceName, virtualName );
      }
    }
  }
  
  public static class ImageManifest {
    private final String                     imageLocation;
    private final ImageMetadata.Architecture architecture;
    private final String                     kernelId;
    private final String                     ramdiskId;
    private final ImageMetadata.Type         imageType;
    private final ImageMetadata.Platform     platform;
    private final String                     signature;
    private final String                     checksum;
    private final String                     checksumType;
    private final String                     manifest;
    private final Document                   inputSource;
    private final String                     name;
    private final Long                       size;
    private final Long                       bundledSize;
    private XPath                            xpath;
    private Function<String, String>         xpathHelper;
    private String                           encryptedKey;
    private String                           encryptedIV;
    private List<ManifestDeviceMapping>      deviceMappings = Lists.newArrayList( );
    
    ImageManifest( String imageLocation ) throws EucalyptusCloudException {
      Context ctx = Contexts.lookup( );
      String cleanLocation = imageLocation.replaceAll( "^/*", "" );
      this.imageLocation = cleanLocation;
      int index = cleanLocation.indexOf( '/' );
      if ( index < 2 || index + 1 >= cleanLocation.length( ) ) {
        throw new EucalyptusCloudException( "Image registration failed:  Invalid image location: " + imageLocation );
      }
      String bucketName = cleanLocation.substring( 0, index );
      String manifestKey = cleanLocation.substring( index + 1 );
      final String manifestName = manifestKey.replaceAll( ".*/", "" );
      if ( !ImageManifests.verifyBucketAcl( bucketName ) ) {
        throw new EucalyptusCloudException( "Image registration failed: you must own the bucket containing the image." );
      }
      this.manifest = ImageManifests.requestManifestData( ctx.getUserFullName( ), bucketName, manifestKey );
      try {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance( ).newDocumentBuilder( );
        this.inputSource = builder.parse( new ByteArrayInputStream( this.manifest.getBytes( ) ) );
      } catch ( Exception e ) {
        throw new EucalyptusCloudException( "Failed to read manifest file: " + bucketName + "/" + manifestKey, e );
      }
      this.xpath = XPathFactory.newInstance( ).newXPath( );
      this.xpathHelper = new Function<String, String>( ) {
        
        @Override
        public String apply( String input ) {
          try {
            return ( String ) ImageManifest.this.xpath.evaluate( input, ImageManifest.this.inputSource, XPathConstants.STRING );
          } catch ( XPathExpressionException ex ) {
            return null;
          }
        }
      };
      
      String temp;
      this.name = ( ( temp = this.xpathHelper.apply( "/manifest/image/name/text()" ) ) != null )
        ? temp
        : manifestName.replace( ".manifest.xml", "" );
      this.checksum = ( ( temp = this.xpathHelper.apply( "/manifest/image/digest/text()" ) ) != null )
        ? temp
        : "0000000000000000000000000000000000000000";
      this.checksumType = ( ( temp = this.xpathHelper.apply( "/manifest/image/digest/@algorithm" ) ) != null )
        ? temp
        : "SHA1";
      this.signature = ( ( temp = this.xpathHelper.apply( "//signature" ) ) != null )
        ? temp
        : null;
      this.encryptedKey = this.xpathHelper.apply( "//ec2_encrypted_key" );
      this.encryptedIV = this.xpathHelper.apply( "//ec2_encrypted_iv" );
      Predicate<ImageMetadata.Type> checkIdType = new Predicate<ImageMetadata.Type>( ) {
        
        @Override
        public boolean apply( ImageMetadata.Type input ) {
          String value = ImageManifest.this.xpathHelper.apply( input.getManifestPath( ) );
          if ( "yes".equals( value ) || "true".equals( value ) || manifestName.startsWith( input.getNamePrefix( ) ) ) {
            return true;
          } else {
            return false;
          }
        }
      };
      String typeInManifest = this.xpathHelper.apply( ImageMetadata.TYPE_MANIFEST_XPATH );
      
      this.size = ( ( temp = this.xpathHelper.apply( "/manifest/image/size/text()" ) ) != null )
        ? Long.parseLong( temp )
        : -1l;
      this.bundledSize = ( ( temp = this.xpathHelper.apply( "/manifest/image/bundled_size/text()" ) ) != null )
        ? Long.parseLong( temp )
        : -1l;
      
      String arch = this.xpathHelper.apply( "/manifest/machine_configuration/architecture/text()" );
      this.architecture = ImageMetadata.Architecture.valueOf( ( ( arch == null )
          ? "i386"
            : arch ) );
      try {
        NodeList devMapList = ( NodeList ) this.xpath.evaluate( "/manifest/machine_configuration/block_device_mapping/mapping", inputSource,
                                                                XPathConstants.NODESET );
        for ( int i = 0; i < devMapList.getLength( ); i++ ) {
          Node node = devMapList.item( i );
          NodeList children = node.getChildNodes( );
          String virtualName = null;
          String device = null;
          for ( int j = 0; j < children.getLength( ); j++ ) {
            Node childNode = children.item( j );
            String nodeType = childNode.getNodeName( );
            if ( "virtual".equals( nodeType ) && childNode.getTextContent( ) != null ) {
              virtualName = childNode.getTextContent( );
            } else if ( "device".equals( nodeType ) && childNode.getTextContent( ) != null ) {
              device = childNode.getTextContent( );
            }
          }
          if ( virtualName != null && device != null ) {
            if ( "ami".equals( virtualName ) ) {
              continue;
            } else if ( "root".equals( virtualName ) ) {
              this.deviceMappings.add( new ManifestDeviceMapping( DeviceMappingType.root, virtualName, device ) );
            } else if ( "swap".equals( virtualName ) ) {
              this.deviceMappings.add( new ManifestDeviceMapping( DeviceMappingType.swap, virtualName, device ) );
            } else if ( virtualName.startsWith( "ephemeral" ) ) {
              this.deviceMappings.add( new ManifestDeviceMapping( DeviceMappingType.ephemeral, virtualName, device ) );
            }
          }
        }
      } catch ( XPathExpressionException ex ) {
        LOG.error( ex, ex );
      }
      
      if ( ( checkIdType.apply( ImageMetadata.Type.kernel ) || checkIdType.apply( ImageMetadata.Type.ramdisk ) ) && !ctx.hasAdministrativePrivileges( ) ) {
        throw new EucalyptusCloudException( "Only administrators can register kernel images." );
      } else {
        if ( checkIdType.apply( ImageMetadata.Type.kernel ) ) {
          this.imageType = ImageMetadata.Type.kernel;
          this.platform = ImageMetadata.Platform.linux;
          this.kernelId = null;
          this.ramdiskId = null;
        } else if ( checkIdType.apply( ImageMetadata.Type.ramdisk ) ) {
          this.imageType = ImageMetadata.Type.ramdisk;
          this.platform = ImageMetadata.Platform.linux;
          this.kernelId = null;
          this.ramdiskId = null;
        } else {
          String kId = this.xpathHelper.apply( ImageMetadata.Type.kernel.getManifestPath( ) );
          String rId = this.xpathHelper.apply( ImageMetadata.Type.ramdisk.getManifestPath( ) );
          this.imageType = ImageMetadata.Type.machine;
          if ( !manifestName.startsWith( ImageMetadata.Platform.windows.toString( ) ) 
              && !( kId != null && ImageMetadata.Platform.windows.name( ).equals( kId ) ) ) {
            this.platform = ImageMetadata.Platform.linux;
            if ( kId != null && kId.startsWith( ImageMetadata.Type.kernel.getTypePrefix( ) ) ) {
              ImageManifests.checkPrivileges( this.kernelId );
              this.kernelId = kId;
            } else {
              this.kernelId = null;
            }
            if ( kId != null && kId.startsWith( ImageMetadata.Type.kernel.getTypePrefix( ) ) ) {
              ImageManifests.checkPrivileges( this.ramdiskId );
              this.ramdiskId = rId;
            } else {
              this.ramdiskId = null;
            }
          } else {
            this.platform = ImageMetadata.Platform.windows;
            this.kernelId = null;
            this.ramdiskId = null;
          }
        }
      }
    }
    
    private boolean checkManifest( User user ) throws EucalyptusCloudException {
      String image = this.manifest.replaceAll( ".*<image>", "<image>" ).replaceAll( "</image>.*", "</image>" );
      String machineConfiguration = this.manifest.replaceAll( ".*<machine_configuration>", "<machine_configuration>" ).replaceAll( "</machine_configuration>.*",
                                                                                                                                   "</machine_configuration>" );
      final String pad = ( machineConfiguration + image );
      Predicate<Certificate> tryVerifyWithCert = new Predicate<Certificate>( ) {
        
        @Override
        public boolean apply( Certificate checkCert ) {
          if ( checkCert instanceof X509Certificate ) {
            X509Certificate cert = ( X509Certificate ) checkCert;
            Signature sigVerifier;
            try {
              sigVerifier = Signature.getInstance( "SHA1withRSA" );
              PublicKey publicKey = cert.getPublicKey( );
              sigVerifier.initVerify( publicKey );
              sigVerifier.update( ( pad ).getBytes( ) );
              return sigVerifier.verify( hexToBytes( ImageManifest.this.signature ) );
            } catch ( Exception ex ) {
              LOG.error( ex, ex );
              return false;
            }
          } else {
            return false;
          }
        }
      };
      Function<com.eucalyptus.auth.principal.Certificate, X509Certificate> euareToX509 = new Function<com.eucalyptus.auth.principal.Certificate, X509Certificate>( ) {
        
        @Override
        public X509Certificate apply( com.eucalyptus.auth.principal.Certificate input ) {
          return input.getX509Certificate( );
        }
      };
      
      try {
        if ( Iterables.any( Lists.transform( user.getCertificates( ), euareToX509 ), tryVerifyWithCert ) ) {
          return true;
        } else if ( tryVerifyWithCert.apply( SystemCredentials.lookup( Eucalyptus.class ).getCertificate( ) ) ) {
          return true;
        } else {
          for ( User u : Accounts.listAllUsers( ) ) {
            if ( Iterables.any( Lists.transform( u.getCertificates( ), euareToX509 ), tryVerifyWithCert ) ) {
              return true;
            }
          }
        }
      } catch ( AuthException e ) {
        throw new EucalyptusCloudException( "Invalid Manifest: Failed to verify signature because of missing (deleted?) user certificate.", e );
      }
      return false;
    }
    
    public String getSignature( ) {
      return this.signature;
    }
    
    public ImageMetadata.Platform getPlatform( ) {
      return this.platform;
    }
    
    public ImageMetadata.Architecture getArchitecture( ) {
      return this.architecture;
    }
    
    public String getKernelId( ) {
      return this.kernelId;
    }
    
    public String getRamdiskId( ) {
      return this.ramdiskId;
    }
    
    public ImageMetadata.Type getImageType( ) {
      return this.imageType;
    }
    
    public String getImageLocation( ) {
      return this.imageLocation;
    }
    
    public String getManifest( ) {
      return this.manifest;
    }
    
    public String getName( ) {
      return this.name;
    }
    
    public Long getSize( ) {
      return this.size;
    }
    
    public Long getBundledSize( ) {
      return this.bundledSize;
    }
    
    public String getChecksum( ) {
      return this.checksum;
    }
    
    public String getChecksumType( ) {
      return this.checksumType;
    }
    
  }
  
  public static ImageManifest lookup( String imageLocation ) throws EucalyptusCloudException {
    return new ImageManifest( imageLocation );
  }
  
  static void checkPrivileges( String diskId ) throws EucalyptusCloudException {
    Context ctx = Contexts.lookup( );
    if ( diskId != null ) {
      ImageInfo disk = null;
      try {
        disk = Images.lookupImage( diskId );
      } catch ( Exception ex ) {
        LOG.error( ex, ex );
        throw new EucalyptusCloudException( "Referenced image id is invalid: " + diskId, ex );
      }
      if ( !RestrictedTypes.filterPrivileged( ).apply( disk ) ) {
        throw new EucalyptusCloudException( "Access to " + disk.getImageType( ).toString( ) + " image " + diskId + " is denied for " + ctx.getUser( ).getName( ) );
      }
    }
  }
}

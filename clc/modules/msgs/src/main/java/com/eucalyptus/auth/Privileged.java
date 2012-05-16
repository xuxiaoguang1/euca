package com.eucalyptus.auth;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.eucalyptus.auth.policy.PolicySpec;
import com.eucalyptus.auth.principal.AccessKey;
import com.eucalyptus.auth.principal.Account;
import com.eucalyptus.auth.principal.Certificate;
import com.eucalyptus.auth.principal.Group;
import com.eucalyptus.auth.principal.Policy;
import com.eucalyptus.auth.principal.User;
import com.eucalyptus.auth.util.X509CertHelper;
import com.eucalyptus.crypto.Certs;
import com.eucalyptus.crypto.Crypto;
import com.eucalyptus.crypto.util.B64;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Privileged {
  
  public static Account createAccount( boolean hasAdministrativePrivilege, String accountName, String password, String email, boolean skipRegistration ) throws AuthException {
    if ( !hasAdministrativePrivilege ) {
      throw new AuthException( AuthException.ACCESS_DENIED );
    }
    Account newAccount = Accounts.addAccount( accountName );
    Map<String, String> info = null;
    if ( email != null ) {
      info = Maps.newHashMap( );
      info.put( User.EMAIL, email );
    }
    User admin = newAccount.addUser( User.ACCOUNT_ADMIN, "/", skipRegistration, true/*enabled*/, info );
    admin.resetToken( );
    admin.createConfirmationCode( );
    if ( password != null ) {
      admin.setPassword( Crypto.generateEncryptedPassword( password ) );
      admin.setPasswordExpires( System.currentTimeMillis( ) + User.PASSWORD_LIFETIME );
    }
    return newAccount;
  }
  
  public static void deleteAccount( boolean hasAdministrativePrivilege, String accountName, boolean recursive ) throws AuthException {
    if ( !hasAdministrativePrivilege ) {
      throw new AuthException( AuthException.ACCESS_DENIED );
    }
    Accounts.deleteAccount( accountName, false/*forceDeleteSystem*/, recursive );    
  }
  
  public static boolean allowReadAccount( User requestUser, Account account ) throws AuthException {
    return requestUser.isSystemAdmin( ) || // system admin or ...
           requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ); // same account
  }
  
  public static boolean allowListOrReadAccountPolicy( User requestUser, Account account ) throws AuthException {
    return requestUser.isSystemAdmin( ) || // system admin or ...
           ( requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) &&
             requestUser.isAccountAdmin( ) ); // account admin
  }
  
  public static void modifyAccount( User requestUser, Account account, String newName ) throws AuthException {
    if ( Account.SYSTEM_ACCOUNT.equals( account.getName( ) ) ) {
      throw new AuthException( AuthException.ACCESS_DENIED );
    }
    try {
      Accounts.lookupAccountByName( newName );
      throw new AuthException( AuthException.CONFLICT );
    } catch ( AuthException ae ) {
      if ( !requestUser.isSystemAdmin( ) ) {
        if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
          throw new AuthException( AuthException.ACCESS_DENIED );
        }        
        if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.ALL_RESOURCE, PolicySpec.ALL_RESOURCE, account, PolicySpec.IAM_CREATEACCOUNTALIAS, requestUser ) ) {
          throw new AuthException( AuthException.ACCESS_DENIED );
        }
      }
      account.setName( newName );
    }
  }
  
  public static void deleteAccountAlias( User requestUser, Account account, String alias ) throws AuthException {
    if ( Account.SYSTEM_ACCOUNT.equals( account.getName( ) ) ) {
      throw new AuthException( AuthException.ACCESS_DENIED );
    }
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }              
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.ALL_RESOURCE, PolicySpec.ALL_RESOURCE, account, PolicySpec.IAM_DELETEACCOUNTALIAS, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }
    if ( Strings.isNullOrEmpty( alias ) ) {
      throw new AuthException( AuthException.EMPTY_ACCOUNT_NAME );
    }
    // Only one alias is allowed by AWS IAM spec. Overwrite the current alias if matches.
    if ( account.getName( ).equals( alias ) ) {
      account.setName( account.getAccountNumber( ) );
    }
  }
  
  public static List<String> listAccountAliases( User requestUser, Account account ) throws AuthException {
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }                    
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.ALL_RESOURCE, PolicySpec.ALL_RESOURCE, account, PolicySpec.IAM_LISTACCOUNTALIASES, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }
    List<String> aliases = Lists.newArrayList( );
    aliases.add( account.getName( ) );
    return aliases;
  }
  
  public static Account getAccountSummary( User requestUser, Account account ) throws AuthException {
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.ALL_RESOURCE, PolicySpec.ALL_RESOURCE, account, PolicySpec.IAM_GETACCOUNTSUMMARY, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }
    return account;
  }
  
  public static Group createGroup( User requestUser, Account account, String groupName, String path ) throws AuthException {
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_GROUP, "", account, PolicySpec.IAM_CREATEGROUP, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.canAllocate( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_GROUP, "", PolicySpec.IAM_CREATEGROUP, requestUser, 1L ) ) {
        throw new AuthException( AuthException.QUOTA_EXCEEDED );
      }
    }
    return account.addGroup( groupName, path );
  }
  
  public static boolean allowListGroup( User requestUser, Account account, Group group ) throws AuthException {
    return requestUser.isSystemAdmin( ) || // system admin or ...
           ( requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) && // same account and ...
             // allowed to list group
             Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_GROUP, Accounts.getGroupFullName( group ), account, PolicySpec.IAM_LISTGROUPS, requestUser ) );
  }

  public static boolean allowReadGroup( User requestUser, Account account, Group group ) throws AuthException {
    return requestUser.isSystemAdmin( ) || // system admin or ...
           ( requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) && // same account and ...
             // allowed to list group
             Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_GROUP, Accounts.getGroupFullName( group ), account, PolicySpec.IAM_GETGROUP, requestUser ) );
  }

  public static void deleteGroup( User requestUser, Account account, Group group, boolean recursive ) throws AuthException {
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_GROUP, Accounts.getGroupFullName( group ), account, PolicySpec.IAM_DELETEGROUP, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }
    account.deleteGroup( group.getName( ), recursive );
  }
  
  public static void modifyGroup( User requestUser, Account account, Group group, String newName, String newPath ) throws AuthException {
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_GROUP, Accounts.getGroupFullName( group ), account, PolicySpec.IAM_UPDATEGROUP, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }
    if ( !Strings.isNullOrEmpty( newName ) ) {
      group.setName( newName );
    }
    if ( !Strings.isNullOrEmpty( newPath ) ) {
      group.setPath( newPath );
    }
  }

  public static User createUser( User requestUser, Account account, String userName, String path ) throws AuthException {
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, "", account, PolicySpec.IAM_CREATEUSER, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.canAllocate( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, "", PolicySpec.IAM_CREATEUSER, requestUser, 1L ) ) {
        throw new AuthException( AuthException.QUOTA_EXCEEDED );
      }
    }
    return account.addUser( userName, path, true, true, null );
  }
  
  public static boolean allowReadUser( User requestUser, Account account, User user ) throws AuthException {
    return requestUser.isSystemAdmin( ) || // system admin or ...
           requestUser.getUserId( ).equals( user.getUserId( ) ) || // user himself or ...
           ( requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) && // in the same account and ...
             Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_GETUSER, requestUser ) );
  }

  public static boolean allowListUser( User requestUser, Account account, User user ) throws AuthException {
    return requestUser.isSystemAdmin( ) || // system admin or ...
           requestUser.getUserId( ).equals( user.getUserId( ) ) || // user himself or ...
           ( requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) && // in the same account and ...
             Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_LISTUSERS, requestUser ) );
  }

  public static boolean allowListAndReadUser( User requestUser, Account account, User user ) throws AuthException {
    return requestUser.isSystemAdmin( ) || // system admin or ...
           requestUser.getUserId( ).equals( user.getUserId( ) ) || // user himself or ...
           ( requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) && // in the same account and ...
               // allowed to list and get user
             ( Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_GETUSER, requestUser ) &&
               Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_LISTUSERS, requestUser ) ) );
  }

  public static void deleteUser( User requestUser, Account account, User user, boolean recursive ) throws AuthException {
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_DELETEUSER, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }
    if ( user.isAccountAdmin( ) ) {
      throw new AuthException( AuthException.ACCESS_DENIED );
    }
    account.deleteUser( user.getName( ), false/*forceDeleteAdmin*/, recursive );
  }

  public static void modifyUser( User requestUser, Account account, User user, String newName, String newPath, Boolean enabled, Long passwordExpires, Map<String, String> info ) throws AuthException {
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_UPDATEUSER, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }
    if ( !Strings.isNullOrEmpty( newName ) ) {
      // Not allowed to modify admin user
      if ( user.isAccountAdmin( ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      user.setName( newName );
    }
    if ( !Strings.isNullOrEmpty( newPath ) ) {
      // Not allowed to modify admin user
      if ( user.isAccountAdmin( ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      user.setPath( newPath );
    }
    if ( enabled != null ) {
      // Not allowed to disable system account user
      // Only system admin can disable account admin
      if ( user.isSystemAdmin( ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      } else if ( user.isAccountAdmin( ) && !requestUser.isSystemAdmin( ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );  
      }
      user.setEnabled( enabled );
    }
    if ( passwordExpires != null ) {
      if ( passwordExpires > new Date( ).getTime( ) ) {
        user.setPasswordExpires( passwordExpires );
      }
    }
    if ( info != null ) {
      user.setInfo( info );
    }
  }
  
  public static void updateUserInfoItem( User requestUser, Account account, User user, String key, String value ) throws AuthException {
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_UPDATEUSER, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }
    if ( value != null ) {
      user.setInfo( key, value );
    } else {
      user.removeInfo( key );
    }
  }
  
  public static void addUserToGroup( User requestUser, Account account, User user, Group group ) throws AuthException {
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_ADDUSERTOGROUP, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_GROUP, Accounts.getGroupFullName( group ), account, PolicySpec.IAM_ADDUSERTOGROUP, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }
    if ( group.hasUser( user.getName( ) ) ) {
      throw new AuthException( AuthException.CONFLICT );
    }
    group.addUserByName( user.getName( ) );
  }
  
  public static void removeUserFromGroup( User requestUser, Account account, User user, Group group ) throws AuthException {
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_REMOVEUSERFROMGROUP, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_GROUP, Accounts.getGroupFullName( group ), account, PolicySpec.IAM_REMOVEUSERFROMGROUP, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }
    if ( !group.hasUser( user.getName( ) ) ) {
      throw new AuthException( AuthException.NO_SUCH_USER );
    }
    group.removeUserByName( user.getName( ) );
  }
  
  public static List<Group> listGroupsForUser( User requestUser, Account account, User user ) throws AuthException {
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_LISTGROUPSFORUSER, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }
    List<Group> groups = Lists.newArrayList( );
    for ( Group g : user.getGroups( ) ) {
      if ( !g.isUserGroup( ) ) {
        groups.add( g );
      }
    }
    return groups;
  }
  
  public static void putAccountPolicy( boolean hasAdministrativePrivilege, Account account, String name, String policy ) throws AuthException, PolicyParseException {
    if ( !hasAdministrativePrivilege ) {
      throw new AuthException( AuthException.ACCESS_DENIED );
    }
    // Can not add policy to system account "eucalyptus"
    if ( Account.SYSTEM_ACCOUNT.equals( account.getName( ) ) ) {
      throw new AuthException( AuthException.ACCESS_DENIED );
    }
    User admin = account.lookupUserByName( User.ACCOUNT_ADMIN );
    admin.addPolicy( name, policy );
  }
  
  public static void putGroupPolicy( User requestUser, Account account, Group group, String name, String policy ) throws AuthException, PolicyParseException {
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_GROUP, Accounts.getGroupFullName( group ), account, PolicySpec.IAM_PUTGROUPPOLICY, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }
    group.addPolicy( name, policy );
  }

  public static void putUserPolicy( User requestUser, Account account, User user, String name, String policy ) throws AuthException, PolicyParseException {
    if ( !requestUser.isSystemAdmin( ) ) {
      // Policy attached to account admin is the account policy. Only system admin can put policy to an account.
      if ( user.isAccountAdmin( ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_GROUP, Accounts.getUserFullName( user ), account, PolicySpec.IAM_PUTUSERPOLICY, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }
    user.addPolicy( name, policy );
  }
  
  public static void deleteAccountPolicy( boolean hasAdministrativePrivilege, Account account, String name ) throws AuthException {
    if ( !hasAdministrativePrivilege ) {
      throw new AuthException( AuthException.ACCESS_DENIED );
    }
    User admin = account.lookupUserByName( User.ACCOUNT_ADMIN );
    admin.removePolicy( name );
  }
  
  public static void deleteGroupPolicy( User requestUser, Account account, Group group, String name ) throws AuthException {
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_GROUP, Accounts.getGroupFullName( group ), account, PolicySpec.IAM_DELETEGROUPPOLICY, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }
    group.removePolicy( name );
  }

  public static void deleteUserPolicy( User requestUser, Account account, User user, String name ) throws AuthException {
    if ( !requestUser.isSystemAdmin( ) ) {
      // Policy attached to account admin is the account policy. Only system admin can remove policy to an account.
      if ( user.isAccountAdmin( ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_GROUP, Accounts.getUserFullName( user ), account, PolicySpec.IAM_DELETEUSERPOLICY, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }
    user.removePolicy( name );
  }
  
  public static List<Policy> listAccountPolicies( User requestUser, Account account ) throws AuthException {
    if ( !allowListOrReadAccountPolicy( requestUser, account ) ) {
      throw new AuthException( AuthException.ACCESS_DENIED );
    }
    User admin = account.lookupUserByName( User.ACCOUNT_ADMIN );
    return admin.getPolicies( );
  }
  
  public static List<Policy> listGroupPolicies( User requestUser, Account account, Group group ) throws AuthException {
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_GROUP, Accounts.getGroupFullName( group ), account, PolicySpec.IAM_LISTGROUPPOLICIES, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }
    return group.getPolicies( );
  }

  public static List<Policy> listUserPolicies( User requestUser, Account account, User user ) throws AuthException {
    if ( !allowListUserPolicy( requestUser, account, user ) ) {
      throw new AuthException( AuthException.ACCESS_DENIED );
    }
    return user.getPolicies( );
  }
  
  public static Policy getAccountPolicy( User requestUser, Account account, String policyName ) throws AuthException {
    if ( !allowListOrReadAccountPolicy( requestUser, account ) ) {
      throw new AuthException( AuthException.ACCESS_DENIED );
    }
    if ( Strings.isNullOrEmpty( policyName ) ) {
      throw new AuthException( AuthException.EMPTY_POLICY_NAME );
    }
    User admin = account.lookupUserByName( User.ACCOUNT_ADMIN );
    Policy policy = null;
    for ( Policy p : admin.getPolicies( ) ) {
      if ( p.getName( ).equals( policyName ) ) {
        policy = p;
        break;
      }
    }
    return policy;
  }
  
  public static Policy getGroupPolicy( User requestUser, Account account, Group group, String policyName ) throws AuthException {
    if ( !allowReadGroupPolicy( requestUser, account, group ) ) {
      throw new AuthException( AuthException.ACCESS_DENIED );
    }
    if ( Strings.isNullOrEmpty( policyName ) ) {
      throw new AuthException( AuthException.EMPTY_POLICY_NAME );
    }
    Policy policy = null;
    for ( Policy p : group.getPolicies( ) ) {
      if ( p.getName( ).equals( policyName ) ) {
        policy = p;
        break;
      }
    }
    return policy;
  }
  
  public static Policy getUserPolicy( User requestUser, Account account, User user, String policyName ) throws AuthException {
    if ( !allowReadUserPolicy( requestUser, account, user ) ) {
      throw new AuthException( AuthException.ACCESS_DENIED );
    }
    if ( Strings.isNullOrEmpty( policyName ) ) {
      throw new AuthException( AuthException.EMPTY_POLICY_NAME );
    }
    Policy policy = null;
    for ( Policy p : user.getPolicies( ) ) {
      if ( p.getName( ).equals( policyName ) ) {
        policy = p;
        break;
      }
    }    
    return policy;
  }
  
  public static boolean allowReadGroupPolicy( User requestUser, Account account, Group group ) throws AuthException {
    return requestUser.isSystemAdmin( ) || // system admin or ...
           ( requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) && // in the same account and ...
             Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_GROUP, Accounts.getGroupFullName( group ), account, PolicySpec.IAM_GETGROUPPOLICY, requestUser ) );
  }

  public static boolean allowListUserPolicy( User requestUser, Account account, User user ) throws AuthException {
    return requestUser.isSystemAdmin( ) || // system admin or ...
           ( requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) && // in the same account and ...
             ( requestUser.isAccountAdmin( ) || // is the account admin or ...
               ( !user.isAccountAdmin( ) && // we are not looking at account admin's policies and authorized
                 Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_LISTUSERPOLICIES, requestUser ) ) ) );
  }

  public static boolean allowReadUserPolicy( User requestUser, Account account, User user ) throws AuthException {
    return requestUser.isSystemAdmin( ) || // system admin or ...
           ( requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) && // in the same account and ...
             ( requestUser.isAccountAdmin( ) || // is the account admin or ...
               ( !user.isAccountAdmin( ) && // we are not looking at account admin's policies and authorized
                 Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_GETUSERPOLICY, requestUser ) ) ) );
  }

  public static boolean allowListAndReadUserPolicy( User requestUser, Account account, User user ) throws AuthException {
    return requestUser.isSystemAdmin( ) || // system admin or ...
           ( requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) && // in the same account and ...
             ( requestUser.isAccountAdmin( ) || // is the account admin or ...
               ( !user.isAccountAdmin( ) && // we are not looking at account admin's policies and authorized
                 Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_LISTUSERPOLICIES, requestUser ) && 
                 Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_GETUSERPOLICY, requestUser ) ) ) );
  }

  public static AccessKey createAccessKey( User requestUser, Account account, User user ) throws AuthException {
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_CREATEACCESSKEY, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }
    return user.createKey( );
  }

  public static void deleteAccessKey( User requestUser, Account account, User user, String keyId ) throws AuthException {
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_DELETEACCESSKEY, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }  
    user.removeKey( keyId );
  }
  
  public static List<AccessKey> listAccessKeys( User requestUser, Account account, User user ) throws AuthException {
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_LISTACCESSKEYS, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }  
    return user.getKeys( );
  }

  public static void modifyAccessKey( User requestUser, Account account, User user, String keyId, String status ) throws AuthException {
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_UPDATEACCESSKEY, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }
    if ( Strings.isNullOrEmpty( keyId ) ) {
      throw new AuthException( AuthException.EMPTY_KEY_ID );
    }
    if ( Strings.isNullOrEmpty( status ) ) {
      throw new AuthException( AuthException.EMPTY_STATUS );
    }
    AccessKey key = user.getKey( keyId );
    key.setActive( "Active".equalsIgnoreCase( status ) );
  }
  
  public static Certificate createSigningCertificate( User requestUser, Account account, User user, KeyPair keyPair ) throws AuthException {
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      // Use the official UPLOADSIGNINGCERTIFICATE action here
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_UPLOADSIGNINGCERTIFICATE, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }
    X509Certificate x509 = Certs.generateCertificate( keyPair, user.getName( ) );
    try {
      x509.checkValidity( );
    } catch ( Exception e ) {
      throw new AuthException( "Invalid X509 Certificate", e );
    }
    return user.addCertificate( x509 );
  }

  public static Certificate uploadSigningCertificate( User requestUser, Account account, User user, String certBody ) throws AuthException {
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_UPLOADSIGNINGCERTIFICATE, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }
    if ( Strings.isNullOrEmpty( certBody ) ) {
      throw new AuthException( AuthException.EMPTY_CERT );
    }
    String encodedPem = B64.url.encString( certBody );
    for ( Certificate c : user.getCertificates( ) ) {
      if ( c.getPem( ).equals( encodedPem ) ) {
        if ( !c.isRevoked( ) ) {
          throw new AuthException( AuthException.CONFLICT );        
        } else {
          user.removeCertificate( c.getCertificateId( ) );
        }
      }
    }
    X509Certificate x509 = X509CertHelper.toCertificate( encodedPem );
    if ( x509 == null ) {
      throw new AuthException( AuthException.INVALID_CERT );        
    }
    return user.addCertificate( x509 );
  }

  public static List<Certificate> listSigningCertificates( User requestUser, Account account, User user ) throws AuthException {
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_LISTSIGNINGCERTIFICATES, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }  
    List<Certificate> certs = Lists.newArrayList( );
    for ( Certificate cert : user.getCertificates( ) ) {
      if ( !cert.isRevoked( ) ) {
        certs.add( cert );
      }
    }
    return certs;
  }

  public static void deleteSigningCertificate( User requestUser, Account account, User user, String certId ) throws AuthException {
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_DELETESIGNINGCERTIFICATE, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }  
    user.removeCertificate( certId );
  }
  
  public static void modifySigningCertificate( User requestUser, Account account, User user, String certId, String status ) throws AuthException {
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_UPDATESIGNINGCERTIFICATE, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }
    if ( Strings.isNullOrEmpty( status ) ) {
      throw new AuthException( AuthException.EMPTY_STATUS );
    }
    if ( Strings.isNullOrEmpty( certId ) ) {
      throw new AuthException( AuthException.EMPTY_CERT_ID );
    }
    Certificate cert = user.getCertificate( certId );
    if ( cert.isRevoked( ) ) {
      throw new AuthException( AuthException.NO_SUCH_CERTIFICATE );
    }
    cert.setActive( "Active".equalsIgnoreCase( status ) );
  }

  public static void createLoginProfile( User requestUser, Account account, User user, String password ) throws AuthException {
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_CREATELOGINPROFILE, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }
    setUserPassword( user, password );
  }
  
  public static void deleteLoginProfile( User requestUser, Account account, User user ) throws AuthException {
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_DELETELOGINPROFILE, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }
    user.setPassword( null );
  }

  public static boolean allowReadLoginProfile( User requestUser, Account account, User user ) throws AuthException {
    return requestUser.isSystemAdmin( ) ||
           ( requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) && // same account and ...
             Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_GETLOGINPROFILE, requestUser ) );
  }

  public static void updateLoginProfile( User requestUser, Account account, User user, String newPass ) throws AuthException {
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_UPDATELOGINPROFILE, requestUser ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }
    setUserPassword( user, newPass );
  }

  public static boolean allowProcessUserSignup( User requestUser, User user ) throws AuthException {
    return requestUser.isSystemAdmin( ) ||
           ( requestUser.getAccount( ).getAccountNumber( ).equals( user.getAccount( ).getAccountNumber( ) ) &&
             requestUser.isAccountAdmin( ) );
  }

  private static void setUserPassword( User user, String newPass ) throws AuthException {
    if ( Strings.isNullOrEmpty( newPass ) || user.getName( ).equals( newPass ) ) {
      throw new AuthException( AuthException.INVALID_PASSWORD );
    }
    String newEncrypted = Crypto.generateEncryptedPassword( newPass );
    user.setPassword( newEncrypted );
    user.setPasswordExpires( System.currentTimeMillis( ) + User.PASSWORD_LIFETIME );
  }
  
  // Special case for change password. We should allow a user to do all the stuff here for himself without explicit permission.
  public static void changeUserPasswordAndEmail( User requestUser, Account account, User user, String newPass, String email ) throws AuthException {
    if ( !requestUser.isSystemAdmin( ) ) {
      if ( !requestUser.getAccount( ).getAccountNumber( ).equals( account.getAccountNumber( ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
      if ( !requestUser.getUserId( ).equals( user.getUserId( ) ) && 
           ( !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_UPDATELOGINPROFILE, requestUser ) ||
             ( email != null &&
               !Permissions.isAuthorized( PolicySpec.VENDOR_IAM, PolicySpec.IAM_RESOURCE_USER, Accounts.getUserFullName( user ), account, PolicySpec.IAM_UPDATEUSER, requestUser ) ) ) ) {
        throw new AuthException( AuthException.ACCESS_DENIED );
      }
    }
    setUserPassword( user, newPass );
    if ( email != null ) {
      user.setInfo( User.EMAIL, email );
    }
  }
  
}

package com.eucalyptus.auth;

import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import com.eucalyptus.auth.checker.InvalidValueException;
import com.eucalyptus.auth.checker.ValueChecker;
import com.eucalyptus.auth.checker.ValueCheckerFactory;
import com.eucalyptus.auth.entities.AccountEntity;
import com.eucalyptus.auth.entities.AuthorizationEntity;
import com.eucalyptus.auth.entities.ConditionEntity;
import com.eucalyptus.auth.entities.GroupEntity;
import com.eucalyptus.auth.entities.PolicyEntity;
import com.eucalyptus.auth.entities.StatementEntity;
import com.eucalyptus.auth.entities.UserEntity;
import com.eucalyptus.auth.policy.PolicyParser;
import com.eucalyptus.auth.principal.Account;
import com.eucalyptus.auth.principal.Group;
import com.eucalyptus.auth.principal.Policy;
import com.eucalyptus.auth.principal.User;
import com.eucalyptus.entities.EntityWrapper;
import com.eucalyptus.entities.Transactions;
import java.util.concurrent.ExecutionException;
import com.eucalyptus.util.Tx;
import com.google.common.collect.Lists;

public class DatabaseGroupProxy implements Group {
  
  private static final long serialVersionUID = 1L;

  private static final ValueChecker NAME_CHECKER = ValueCheckerFactory.createUserAndGroupNameChecker( );
  private static final ValueChecker PATH_CHECKER = ValueCheckerFactory.createPathChecker( );
  private static final ValueChecker POLICY_NAME_CHECKER = ValueCheckerFactory.createPolicyNameChecker( );

  private static Logger LOG = Logger.getLogger( DatabaseGroupProxy.class );
  
  private GroupEntity delegate;
  
  public DatabaseGroupProxy( GroupEntity delegate ) {
    this.delegate = delegate;
  }
  
  @Override
  public String toString( ) {
    final StringBuilder sb = new StringBuilder( );
    try {
      DatabaseAuthUtils.invokeUnique( GroupEntity.class, "groupId", this.delegate.getGroupId( ), new Tx<GroupEntity>( ) {
        public void fire( GroupEntity t ) {
          sb.append( t.toString( ) );
        }
      } );
    } catch ( ExecutionException e ) {
      Debugging.logError( LOG, e, "Failed to toString for " + this.delegate );
    }
    return sb.toString( );
  }

  @Override
  public String getName( ) {
    return this.delegate.getName( );
  }

  @Override
  public void setName( final String name ) throws AuthException {
    try {
      NAME_CHECKER.check( name );
    } catch ( InvalidValueException e ) {
      Debugging.logError( LOG, e, "Invalid group name " + name );
      throw new AuthException( AuthException.INVALID_NAME, e );
    }
    try {
      // try looking up the group with the same name first
      this.getAccount( ).lookupGroupByName( name );
    } catch ( AuthException ae ) {
      // not found
      try {
        DatabaseAuthUtils.invokeUnique( GroupEntity.class, "groupId", this.delegate.getGroupId( ), new Tx<GroupEntity>( ) {
          public void fire( GroupEntity t ) {
            t.setName( name );
          }
        } );
      } catch ( ExecutionException e ) {
        Debugging.logError( LOG, e, "Failed to setName for " + this.delegate );
        throw new AuthException( e );
      }
      return;
    }
    // found
    throw new AuthException( AuthException.GROUP_ALREADY_EXISTS );
  }

  @Override
  public String getPath( ) {
    return this.delegate.getPath( );
  }

  @Override
  public void setPath( final String path ) throws AuthException {
    try {
      PATH_CHECKER.check( path );
    } catch ( InvalidValueException e ) {
      Debugging.logError( LOG, e, "Invalid path " + path );
      throw new AuthException( AuthException.INVALID_PATH, e );
    }    
    try {
      DatabaseAuthUtils.invokeUnique( GroupEntity.class, "groupId", this.delegate.getGroupId( ), new Tx<GroupEntity>( ) {
        public void fire( GroupEntity t ) {
          t.setPath( path );
        }
      } );
    } catch ( ExecutionException e ) {
      Debugging.logError( LOG, e, "Failed to setPath for " + this.delegate );
      throw new AuthException( e );
    }
  }
  
  @Override
  public Boolean isUserGroup( ) {
    return this.delegate.isUserGroup( );
  }
  
  @Override
  public void setUserGroup( final Boolean userGroup ) throws AuthException {
    try {
      DatabaseAuthUtils.invokeUnique( GroupEntity.class, "groupId", this.delegate.getGroupId( ), new Tx<GroupEntity>( ) {
        public void fire( GroupEntity t ) {
          t.setUserGroup( userGroup );
        }
      } );
    } catch ( ExecutionException e ) {
      Debugging.logError( LOG, e, "Failed to setUserGroup for " + this.delegate );
      throw new AuthException( e );
    }
  }

  @Override
  public void addUserByName( String userName ) throws AuthException {
    EntityWrapper<GroupEntity> db = EntityWrapper.get( GroupEntity.class );
    try {
      GroupEntity groupEntity = DatabaseAuthUtils.getUnique( db, GroupEntity.class, "groupId", this.delegate.getGroupId( ) );
      UserEntity userEntity = DatabaseAuthUtils.getUniqueUser( db.recast( UserEntity.class ), userName, groupEntity.getAccount( ).getName( ) );
      groupEntity.getUsers( ).add( userEntity );
      userEntity.getGroups( ).add( groupEntity );
      db.commit( );
    } catch ( Exception e ) {
      db.rollback( );
      Debugging.logError( LOG, e, "Failed to add user " + userName + " to group " + this.delegate );
      throw new AuthException( e );
    }
  }

  @Override
  public void removeUserByName( String userName ) throws AuthException {
    EntityWrapper<GroupEntity> db = EntityWrapper.get( GroupEntity.class );
    try {
      GroupEntity groupEntity = DatabaseAuthUtils.getUnique( db, GroupEntity.class, "groupId", this.delegate.getGroupId( ) );
      UserEntity userEntity = DatabaseAuthUtils.getUniqueUser( db.recast( UserEntity.class ), userName, groupEntity.getAccount( ).getName( ) );
      groupEntity.getUsers( ).remove( userEntity );
      userEntity.getGroups( ).remove( groupEntity );
      db.commit( );
    } catch ( Exception e ) {
      db.rollback( );
      Debugging.logError( LOG, e, "Failed to remove user " + userName + " from group " + this.delegate );
      throw new AuthException( e );
    }
  }
  
  @Override
  public boolean hasUser( String userName ) throws AuthException {
    EntityWrapper<UserEntity> db = EntityWrapper.get( UserEntity.class );
    try {
      @SuppressWarnings( "unchecked" )
      List<UserEntity> users = ( List<UserEntity> ) db
          .createCriteria( UserEntity.class ).setCacheable( true ).add( Restrictions.eq( "name", userName ) )
          .createCriteria( "groups" ).setCacheable( true ).add( Restrictions.eq( "groupId", this.delegate.getGroupId( ) ) )
          .list( );
      db.commit( );
      return users.size( ) > 0;
    } catch ( Exception e ) {
      db.rollback( );
      Debugging.logError( LOG, e, "Failed to check membership for group " + this.delegate );
      throw new AuthException( e );
    }
  }
  
  @Override
  public List<Policy> getPolicies( ) {
    final List<Policy> results = Lists.newArrayList( );
    try {
      DatabaseAuthUtils.invokeUnique( GroupEntity.class, "groupId", this.delegate.getGroupId( ), new Tx<GroupEntity>( ) {
        public void fire( GroupEntity t ) {
          for ( PolicyEntity p : t.getPolicies( ) ) {
            results.add( new DatabasePolicyProxy( p ) );
          }
        }
      } );
    } catch ( ExecutionException e ) {
      Debugging.logError( LOG, e, "Failed to getPolicies for " + this.delegate );
    }
    return results;
  }

  @Override
  public Policy addPolicy( String name, String policy ) throws AuthException, PolicyParseException {
    try {
      POLICY_NAME_CHECKER.check( name );
    } catch ( InvalidValueException e ) {
      Debugging.logError( LOG, e, "Invalid policy name " + name );
      throw new AuthException( AuthException.INVALID_NAME, e );
    }
    if ( DatabaseAuthUtils.policyNameinList( name, this.getPolicies( ) ) ) {
      Debugging.logError( LOG, null, "Policy name already used: " + name );
      throw new AuthException( AuthException.INVALID_NAME );
    }    
    PolicyEntity parsedPolicy = PolicyParser.getInstance( ).parse( policy );
    parsedPolicy.setName( name );
    EntityWrapper<GroupEntity> db = EntityWrapper.get( GroupEntity.class );
    try {
      GroupEntity groupEntity = DatabaseAuthUtils.getUnique( db, GroupEntity.class, "groupId", this.delegate.getGroupId( ) );
      db.recast( PolicyEntity.class ).add( parsedPolicy );
      parsedPolicy.setGroup( groupEntity );
      for ( StatementEntity statement : parsedPolicy.getStatements( ) ) {
        db.recast( StatementEntity.class ).add( statement );
        statement.setPolicy( parsedPolicy );
        for ( AuthorizationEntity auth : statement.getAuthorizations( ) ) {
          db.recast( AuthorizationEntity.class ).add( auth );
          auth.setStatement( statement );
        }
        for ( ConditionEntity cond : statement.getConditions( ) ) {
          db.recast( ConditionEntity.class ).add( cond );
          cond.setStatement( statement );
        }
      }
      groupEntity.getPolicies( ).add( parsedPolicy );
      db.commit( );
      return new DatabasePolicyProxy( parsedPolicy );
    } catch ( Exception e ) {
      db.rollback( );
      Debugging.logError( LOG, e, "Failed to attach policy for " + this.delegate.getName( ) );
      throw new AuthException( "Failed to attach policy", e );
    }
  }

  @Override
  public void removePolicy( String name ) throws AuthException {
    if ( name == null ) {
      throw new AuthException( AuthException.EMPTY_POLICY_NAME );
    }
    EntityWrapper<GroupEntity> db = EntityWrapper.get( GroupEntity.class );
    try {
      GroupEntity group = DatabaseAuthUtils.getUnique( db, GroupEntity.class, "groupId", this.delegate.getGroupId( ) );
      PolicyEntity policy = DatabaseAuthUtils.removeGroupPolicy( group, name );
      if ( policy != null ) {
        db.recast( PolicyEntity.class ).delete( policy );
      }
      db.commit( );
    } catch ( Exception e ) {
      db.rollback( );
      Debugging.logError( LOG, e, "Failed to remove policy " + name + " in " + this.delegate );
      throw new AuthException( "Failed to remove policy", e );
    }
  }
  
  @Override
  public List<User> getUsers( ) {
    final List<User> results = Lists.newArrayList( );
    try {
      DatabaseAuthUtils.invokeUnique( GroupEntity.class, "groupId", this.delegate.getGroupId( ), new Tx<GroupEntity>( ) {
        public void fire( GroupEntity t ) {
          for ( UserEntity u : t.getUsers( ) ) {
            results.add( new DatabaseUserProxy( u ) );
          }
        }
      } );
    } catch ( ExecutionException e ) {
      Debugging.logError( LOG, e, "Failed to getUsers for " + this.delegate );
    }
    return results;
  }

  @Override
  public Account getAccount( ) {
    final List<DatabaseAccountProxy> results = Lists.newArrayList( );
    try {
      DatabaseAuthUtils.invokeUnique( GroupEntity.class, "groupId", this.delegate.getGroupId( ), new Tx<GroupEntity>( ) {
        public void fire( GroupEntity t ) {
          results.add( new DatabaseAccountProxy( ( AccountEntity) t.getAccount( ) ) );
        }
      } );
    } catch ( ExecutionException e ) {
      Debugging.logError( LOG, e, "Failed to getAccount for " + this.delegate );
    }
    return results.get( 0 );
  }

  @Override
  public String getGroupId( ) {
    return this.delegate.getGroupId( );
  }
  
}

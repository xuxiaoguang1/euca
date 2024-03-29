import boto,sys,euca_admin
from boto.exception import EC2ResponseError
from euca_admin.generic import BooleanResponse
from euca_admin.generic import StringList
from boto.resultset import ResultSet
from euca_admin import EucaAdmin
from optparse import OptionParser

SERVICE_PATH = '/services/Accounts'
class Group():
  
  
  def __init__(self, groupName=None):
    self.group_groupName = groupName
    self.group_users = StringList()
    self.group_auths = StringList()
    self.euca = EucaAdmin(path=SERVICE_PATH)
          
  def __repr__(self):
    r = 'GROUP\t%s\t' % (self.group_groupName)
    r = '%s\nUSERS\t%s\t%s' % (r,self.group_groupName,self.group_users)
    r = '%s\nAUTH\t%s\t%s' % (r,self.group_groupName,self.group_auths)
    return r
      
  def startElement(self, name, attrs, connection):
    if name == 'euca:users':
      return self.group_users
    if name == 'euca:authorizations':
      return self.group_auths
    else:
      return None

  def endElement(self, name, value, connection):
    if name == 'euca:groupName':
      self.group_groupName = value
    else:
      setattr(self, name, value)
          
  def get_describe_parser(self):
    parser = OptionParser("usage: %prog [GROUPS...]",version="Eucalyptus %prog VERSION")
    return parser.parse_args()
  
  def cli_describe(self):
    (options, args) = self.get_describe_parser()
    self.group_describe(args)
    
  def group_describe(self,groups=None):
    params = {}
    if groups:
      self.euca.connection.build_list_params(params,groups,'GroupNames')
    try:
      list = self.euca.connection.get_list('DescribeGroups', params, [('euca:item', Group)])
      for i in list:
        print i
    except EC2ResponseError, ex:
      self.euca.handle_error(ex)

  def get_single_parser(self):
    parser = OptionParser("usage: %prog GROUPNAME",version="Eucalyptus %prog VERSION")
    (options,args) = parser.parse_args()    
    if len(args) != 1:
      print "ERROR  Required argument GROUPNAME is missing or malformed."
      parser.print_help()
      sys.exit(1)
    else:
      return (options,args)  

  def cli_add(self):
    (options, args) = self.get_single_parser()
    self.group_add(args[0])

  def group_add(self, groupName):
    try:
      reply = self.euca.connection.get_object('AddGroup', {'GroupName':groupName}, BooleanResponse)
      print reply
    except EC2ResponseError, ex:
      self.euca.handle_error(ex)

  def cli_delete(self):
    (options, args) = self.get_single_parser()
    self.group_delete(args[0])
            
  def group_delete(self, groupName):
    try:
      reply = self.euca.connection.get_object('DeleteGroup', {'GroupName':groupName},BooleanResponse)
      print reply
    except EC2ResponseError, ex:
      self.euca.handle_error(ex)

  def get_membership_parser(self):
    parser = OptionParser("usage: %prog [options] GROUPNAME",version="Eucalyptus %prog VERSION")
    parser.add_option("-u","--user",dest="userName",help="Name of the User.")
    (options,args) = parser.parse_args()    
    if len(args) != 1:
      print "ERROR  Required argument GROUPNAME is missing or malformed."
      parser.print_help()
      sys.exit(1)
    else:
      return (options,args)  
  
  def cli_add_membership(self):
    (options, args) = self.get_membership_parser()
    self.add_membership(args[0], options.userName)
    

  def add_membership(self,groupName,userName):
    try:
      reply = self.euca.connection.get_object('AddGroupMember', {'GroupName':groupName,'UserName':userName},BooleanResponse)
      print reply
    except EC2ResponseError, ex:
      self.euca.handle_error(ex)

  def cli_remove_membership(self):
    (options, args) = self.get_membership_parser()
    self.remove_membership(args[0], options.userName)


  def remove_membership(self,groupName,userName):
    try:
      reply = self.euca.connection.get_object('RemoveGroupMember', {'GroupName':groupName,'UserName':userName},BooleanResponse)
      print reply
    except EC2ResponseError, ex:
      self.euca.handle_error(ex)


  def get_permission_parser(self):
    parser = OptionParser("usage: %prog [options] GROUP",version="Eucalyptus %prog VERSION")
    parser.add_option("-z","--zone",dest="zoneName",help="Name of the availability zone.")
    (options,args) = parser.parse_args()    
    if len(args) != 1:
      print "ERROR  Required argument GROUPNAME is missing or malformed."
      parser.print_help()
      sys.exit(1)
    else:
      return (options,args)  
            
  def cli_grant_permission(self):
    (options,args) = self.get_permission_parser()
    self.grant_permission(args[0],options.zoneName)
    
  def grant_permission(self,groupName,zoneName):
    try:
      reply = self.euca.connection.get_object('GrantGroupAuthorization', {'GroupName':groupName,'ZoneName':zoneName},BooleanResponse)
      print reply
    except EC2ResponseError, ex:
      self.euca.handle_error(ex)

  def cli_revoke_permission(self):
    (options,args) = self.get_permission_parser()
    self.revokepermission(args[0],options.zoneName)
            
  def revoke_permission(self):
    try:
      reply = self.euca.connection.get_object('RevokeGroupAuthorization', {'GroupName':options.groupName,'ZoneName':options.zoneName},BooleanResponse)
      print reply
    except EC2ResponseError, ex:
      self.euca.handle_error(ex)
  

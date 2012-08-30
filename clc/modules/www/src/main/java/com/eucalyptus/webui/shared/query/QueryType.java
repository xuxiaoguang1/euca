package com.eucalyptus.webui.shared.query;

public enum QueryType {  
  test, 
  instance,

  start,
  config,
  
  individual,
  group,
  user,
  account,
  policy,
  key,
  cert,
  user_req,
  
  
  image,
  vmtype,
  report,
  
  downloads,
  rightscale,
  
  approve,
  reject,
  
  confirm,
  reset, 
  
  nodeCtrl,
  clusterCtrl,
  storageCtrl,
  walrusCtrl,
  keypair,
  securityGroup,
  ipPermission,
  
  device_server,
  device_cpu,
  device_bw,
  device_disk,
  device_image,
  device_ip,
  device_memory,
  device_template,
  device_vm,
  
  res_stat,
  cpu_stat,
  memory_stat,
  disk_stat,
  history_stat,
}

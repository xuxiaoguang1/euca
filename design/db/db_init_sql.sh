#!/bin/bash

command() {
    cmd=$@
    # echo $cmd
    mysql -u root --password=root -D eucalyptus -e "$cmd"
    if [ $? -ne 0 ]; then
        echo $cmd
    fi
}

getvalue() {
    table=$1; shift
    col=$1; shift
    match=$1; shift
    arg=$1; shift
    cmd="command select $col from $table where $match=\""$arg"\""
    id=`$cmd`
    id=`echo $id | awk '{print $2}'`
    echo $id
}

command 'delete from template_price where 1=1'
command 'delete from template where 1=1'
command 'delete from bw_service where 1=1'
command 'delete from ip_service where 1=1'
command 'delete from ip where 1=1'
command 'delete from vm_service where 1=1'
command 'delete from vm where 1=1'
command 'delete from disk_service where 1=1'
command 'delete from disk where 1=1'
command 'delete from mem_service where 1=1'
command 'delete from memory where 1=1'
command 'delete from cpu_service where 1=1'
command 'delete from cpu where 1=1'
command 'delete from cpu_price where 1=1'
command 'delete from server where 1=1'
command 'delete from others_price where 1=1'
command 'delete from cabinet where 1=1'
command 'delete from room where 1=1'
command 'delete from area where 1=1'
command 'delete from user where 1=1'
command 'delete from groups where 1=1'
command 'delete from account where 1=1'
command 'delete from history where 1=1'

command insert into account \(account_name, account_email, account_descrip, account_state, account_del\) \
    values \(\"eucalyptus\", \"email\", \"desc\", 0, 0\)

command insert into account \(account_name, account_email, account_descrip, account_state, account_del\) \
    values \(\"abc\", \"email\", \"desc\", 0, 0\)

id=`getvalue account account_id account_name eucalyptus`

command insert into groups \(group_name, group_descrip, group_state, account_id, group_del\) \
    values \(\"eucalyptus\", \"desc\", 0, $id, 0\)

command insert into groups \(group_name, group_descrip, group_state, account_id, group_del\) \
    values \(\"abc\", \"desc\", 0, $id, 0\)

group=`getvalue groups group_id group_name eucalyptus`
account=`getvalue account account_id account_name eucalyptus`

command insert into user \(user_name, user_pwd, user_title, user_mobile, user_email, user_type, user_state, user_reg_state, group_id, account_id, user_del\) \
    values \(\"admin\", \"admin\", \"title\", \"mobile\", \"email\", 1, 1, 2, $group, $account, 0\)

group1=`getvalue groups group_id group_name abc`
account1=`getvalue account account_id account_name abc`

command insert into user \(user_name, user_pwd, user_title, user_mobile, user_email, user_type, user_state, user_reg_state, group_id, account_id, user_del\) \
    values \(\"admin\", \"admin\", \"title\", \"mobile\", \"email\", 1, 1, 2, $group1, $account1, 0\)

for ((i=0;i<5;i++)) do
    command insert into account \(account_name, account_email, account_descrip, account_state, account_del\) \
        values \(\"account$i\", \"email$i\", \"desc$i\", 0, 0\)
    account=`getvalue account account_id account_name account$i`
    command insert into groups \(group_name, group_descrip, group_state, account_id, group_del\) \
        values \(\"group$i\", \"desc$i\", 0, $account, 0\)
    group=`getvalue groups group_id group_name group$i`
    for ((j=0;j<10;j++)) do
        id="user$i$j"
        command insert into user \(user_name, user_pwd, user_title, user_mobile, user_email, user_type, user_state, group_id, account_id, user_del\) \
            values \(\"$id\", \"$id\", \"title\", \"mobile\", \"email\", 0, 0, $group, $account, 0\)
    done
done

for ((i=0;i<25;i++)) do
    let mod="i%2";
    if [ $mod -ne 0 ]; then
        command insert into area \(area_name, area_desc, area_creationtime, area_modifiedtime\) \
            values \(\"area$i\", \"area_desc$i\", \"2012-07-07\", \"2012-07-08\"\)
    else
        command insert into area \(area_name, area_desc, area_creationtime\) \
            values \(\"area$i\", \"area_desc$i\", \"2012-07-07\"\)
    fi
done

id=`getvalue area area_id area_name area0`

for ((i=0;i<5;i++)) do
    let mod="i%2";
    if [ $mod -ne 0 ]; then
        command insert into room \(room_name, room_desc, room_creationtime, room_modifiedtime, area_id\) \
            values \(\"room$i\", \"room_desc$i\", \"2012-07-07\", \"2012-07-08\", \"$id\"\)
    else
        command insert into room \(room_name, room_desc, room_creationtime, area_id\) \
            values \(\"room$i\", \"room_desc$i\", \"2012-07-07\", \"$id\"\)
    fi
done

command insert into others_price \(others_price_name, others_price, others_price_desc\) values \(\"memory\", \"12.3\", \"\"\)
command insert into others_price \(others_price_name, others_price, others_price_desc\) values \(\"disk\", \"23.4\", \"\"\)
command insert into others_price \(others_price_name, others_price, others_price_desc\) values \(\"bandwidth\", \"34.5\", \"\"\)

id=`getvalue room room_id room_name room0`

for ((i=0;i<5;i++)) do
    let mod="i%2";
    if [ $mod -ne 0 ]; then
        command insert into cabinet \(cabinet_name, cabinet_desc, cabinet_creationtime, cabinet_modifiedtime, room_id\) \
            values \(\"cabinet$i\", \"cabinet_desc$i\", \"2012-07-07\", \"2012-07-08\", \"$id\"\)
    else
        command insert into cabinet \(cabinet_name, cabinet_desc, cabinet_creationtime, room_id\) \
            values \(\"cabinet$i\", \"cabinet_desc$i\", \"2012-07-07\", \"$id\"\)
    fi
done

id=`getvalue cabinet cabinet_id cabinet_name cabinet0`

for ((i=0;i<20;i++)) do
    let x="$i%3";
    command insert into server \(server_name, server_mark, server_conf, server_ip, server_bw, server_state, server_starttime, cabinet_id\) \
        values \(\"name$x\", \"mark$i\", \"conf$i\", \"192.168.134.$i\", \"$i\", \"$x\", \"2012-07-07\", \"$id\"\)
done

# insert cpu

for ((i=0;i<40;i++)) do
    let j="$i%5";
    let x="$i%3";
    id=`getvalue server server_id server_name name$x`
    let mod="i%3";
    if [ $mod -ne 0 ]; then
        vendor="intel"
    else
        vendor="amd"
    fi
    let cache="2 << $i % 5";
    command insert into cpu \(cpu_name, cpu_vendor, cpu_model, cpu_ghz, cpu_cache, server_id\) \
        values \(\"cpu$i\", \"$vendor\", \"model$j\", \"2.$j\", \"$cache\", \"$id\"\)
done

# insert cpu_service

for ((i=0;i<40;i++)) do
    cpuid=`getvalue cpu cpu_id cpu_name cpu$i`
    userid=`getvalue user user_id user_name admin`
    let d="10+$i%20";
    let x="$i%3";
    if [ $x -ne 2 ]; then
        command insert into cpu_service \(cs_starttime, cs_life, cs_state, cpu_id, user_id\) \
            values \(\"2012-07-$d\", \"32\", \"$x\", \"$cpuid\", \"$userid\"\)
    else
        command insert into cpu_service \(cs_state, cpu_id, user_id\) \
            values \(\"$x\", \"$cpuid\", \"$userid\"\)
    fi
done

# insert memory

for ((i=0;i<20;i++)) do
    let j="$i%5";
    let x="$i%3";
    let size="(1024 + i * 10) * 1024";
    id=`getvalue server server_id server_name name$x`
    command insert into memory \(mem_name, mem_total, server_id\) \
        values \(\"memory$i\", $size, \"$id\"\)
done

# insert mem_service

for ((i=0;i<20;i++)) do
    memid=`getvalue memory mem_id mem_name memory$i`
    total=`getvalue memory mem_total mem_name memory$i`
    userid=`getvalue user user_id user_name admin`
    let used="(- $i * 10 + 512) * 1024"
    let remain="$total - $used"
    let d="10+$i%10";
    command insert into mem_service \(ms_used, ms_state, mem_id, user_id\) \
        values \(\"$remain\", \"2\", \"$memid\", \"$userid\"\)
    let x="$i%2";
    command insert into mem_service \(ms_used, ms_starttime, ms_life, ms_state, mem_id, user_id\) \
        values \(\"$used\", \"2012-07-$d\", \"32\", \"$x\", \"$memid\", \"$userid\"\)
done

# insert disk

for ((i=0;i<20;i++)) do
    let j="$i%5";
    let x="$i%3";
    let size="(1000 + i * 10) * 1000";
    id=`getvalue server server_id server_name name$x`
    command insert into disk \(disk_name, disk_total, server_id\) \
        values \(\"disk$i\", $size, \"$id\"\)
done

# insert disk_service

for ((i=0;i<20;i++)) do
    diskid=`getvalue disk disk_id disk_name disk$i`
    total=`getvalue disk disk_total disk_name disk$i`
    userid=`getvalue user user_id user_name admin`
    let used="(- $i * 10 + 500) * 1000"
    let remain="$total - $used"
    let d="10+$i%10";
    command insert into disk_service \(ds_used, ds_state, disk_id, user_id\) \
        values \(\"$remain\", \"2\", \"$diskid\", \"$userid\"\)
    let x="$i%2";
    command insert into disk_service \(ds_used, ds_starttime, ds_life, ds_state, disk_id, user_id\) \
        values \(\"$used\", \"2012-07-$d\", \"32\", \"$x\", \"$diskid\", \"$userid\"\)
done

# insert vm/vm_service

userid=`getvalue user user_id user_name admin`

for ((i=0;i<10;i++)) do
    command insert into vm \(vm_mark\) values \(\"vmware$i\"\)
    vmid=`getvalue vm vm_id vm_mark vmware$i`
    command insert into vm_service \(user_id, vm_id\) values \(\"$userid\", \"$vmid\"\)
done

vmid=`getvalue vm vm_id vm_mark vmware0`
 
# insert ip/ip_service/bw_service

for ((i=0;i<32;i++)) do
    let d="10+$i%10";
    let x="$i%2";
    command insert into ip \(ip_addr, ip_type\) \
        values \(\"166.111.0.$i\", 0\);
    ipid=`getvalue ip ip_id ip_addr 166.111.0.$i`
    command insert into ip_service \(is_starttime, is_life, is_state, user_id, vm_id, ip_id\) \
        values \(\"2012-07-$d\", \"32\", \"$x\", \"$userid\", \"$vmid\", \"$ipid\"\)
    command insert into ip \(ip_addr, ip_type\) \
        values \(\"192.168.0.$i\", 1\);
    command insert into bw_service \(bs_starttime, bs_life, ip_id, user_id, bs_bw\) \
        values \(\"2012-07-$d\", 32, \"$ipid\", \"$userid\", \"$d\"\)
    ipid=`getvalue ip ip_id ip_addr 192.168.0.$i`
    command insert into ip_service \(is_starttime, is_life, is_state, user_id, vm_id, ip_id\) \
        values \(\"2012-07-$d\", \"32\", \"$x\", \"$userid\", \"$vmid\", \"$ipid\"\)
done

# insert template
for ((i=0;i<10;i++)) do
    let d="10+$i%10";
    command insert into template \( \
        template_name, \
        template_cpu, \
        template_mem, \
        template_disk, \
        template_bw, \
        template_image, \
        template_starttime, \
        template_ncpus\) \
        values \( \
        \"name$i\", \
        \"cpu0\", \
        \"2$i""000000\", \
        \"3$i""000000\", \
        \"4$i""000\", \
        \"image$i\", \
        \"2012-07-$d\", \
        \"1\" \
        \)
done

for ((i=0;i<5;i++)) do
    let d="10+$i%10";
    id=`getvalue template template_id template_name name$i`
    command insert into template_price \( \
        template_price_desc, \
        template_price_cpu, \
        template_price_mem, \
        template_price_disk, \
        template_price_bw, \
        template_price_creationtime, \
        template_id\) \
        values \( \
        \"desc$i\", \
        \"12.$i\", \
        \"23.$i\", \
        \"34.$i\", \
        \"45.$i\", \
        \"2012-07-$d\", \
        \"$id\" \
        \)
done

# inser history
for ((i=0;i<10;i++)) do
    let d="10+$i%10";
    command insert into history \( \
        history_action, \
        history_reason, \
        history_date, \
        history_user_id, \
        history_vm_id\) \
        values \( \
        \"start\", \
        \"init cloud environment\", \
        \"2012-07-$d\", \
        \"1\", \
        \"1\" \
        \)
done

# insert into vm image type
command INSERT INTO vm_image_type \( vit_id, vit_os, vit_ver, euca_vit_id \) VALUES \( \
                                     null, \"Ubuntu 32Bit\", \"12.04\", null \)

command INSERT INTO vm_image_type \( vit_id, vit_os, vit_ver, euca_vit_id \) VALUES \( \
                                     null, \"Windows7 32Bit\", \"Home\", null \)

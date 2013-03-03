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
command 'delete from vm_service where 1=1'
command 'delete from vm where 1=1'
command 'delete from disk_service where 1=1'
command 'delete from disk where 1=1'
command 'delete from mem_service where 1=1'
command 'delete from memory where 1=1'
command 'delete from cpu_service where 1=1'
command 'delete from cpu where 1=1'
command 'delete from server where 1=1'
command 'delete from device_price where 1=1'
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
command insert into device_price \(device_price_name, device_price, device_price_desc\) values \(\"cpu\", \"1.2\", \"\"\)
command insert into device_price \(device_price_name, device_price, device_price_desc\) values \(\"memory\", \"12.3\", \"\"\)
command insert into device_price \(device_price_name, device_price, device_price_desc\) values \(\"disk\", \"23.4\", \"\"\)
command insert into device_price \(device_price_name, device_price, device_price_desc\) values \(\"bandwidth\", \"34.5\", \"\"\)

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
    let j="$i%10";
    command insert into server \(server_name, server_desc, server_ip, server_bw, server_state, server_creationtime, server_modifiedtime, cabinet_id\) \
        values \(\"name$i\", \"desc$i\", \"192.168.134.$i\", \"$i\", \"$x\", \"2011-01-0$j\", \"\", \"$id\"\)
done

# insert cpu

for ((i=0;i<40;i++)) do
    let j="$i%5";
    let x="$i%3";
    id=`getvalue server server_id server_name name$x`
    command insert into cpu \(cpu_name, cpu_desc, cpu_total, cpu_creationtime, cpu_modifiedtime, server_id\) \
        values \(\"cpu$i\", \"\", \"10\", \"2012-02-0$j\", \"\", \"$id\"\)
done

# insert cpu_service

for ((i=0;i<40;i++)) do
    cpuid=`getvalue cpu cpu_id cpu_name cpu$i`
    userid=`getvalue user user_id user_name admin`
    let d="10+$i%20";
    let x="$i%3";
    if [ $x -ne 2 ]; then
        command insert into cpu_service \(cs_desc, cs_used, cs_starttime, cs_endtime, cs_state, \
            cs_creationtime, cs_modifiedtime, cpu_id, user_id\) \
            values \(\"\", \"8\", \"2012-03-$d\", \"2012-04-$d\", \"$x\", \"2012-01-$d\", \"\", \"$cpuid\", \"$userid\"\)
        command insert into cpu_service \(cs_desc, cs_used, cs_starttime, cs_endtime, cs_state, \
            cs_creationtime, cs_modifiedtime, cpu_id, user_id\) \
            values \(\"\", \"2\", \"2012-03-$d\", \"2012-04-$d\", \"$x\", \"2012-01-$d\", \"\", \"$cpuid\", \"$userid\"\)
        command insert into cpu_service \(cs_desc, cs_used, cs_state, cpu_id\) \
            values \(\"\", \"0\", \"2\", \"$cpuid\"\)
    else
        command insert into cpu_service \(cs_desc, cs_used, cs_state, cpu_id\) \
            values \(\"\", \"10\", \"$x\", \"$cpuid\"\)
    fi
done

# insert memory

for ((i=0;i<20;i++)) do
    let j="$i%5";
    let x="$i%3";
    let size="($i + 1) * 2 * 1024 * 1024 * 1024";
    id=`getvalue server server_id server_name name$x`
    command insert into memory \(mem_name, mem_desc, mem_total, mem_creationtime, server_id\) \
        values \(\"memory$i\", \"desc$i\", $size, \"2012-07-9\", \"$id\"\)
done

# insert mem_service

for ((i=0;i<20;i++)) do
    memid=`getvalue memory mem_id mem_name memory$i`
    total=`getvalue memory mem_total mem_name memory$i`
    userid=`getvalue user user_id user_name admin`
    let used="($i + 1) * 1024 * 1024 * 1024"
    let remain="$total - $used"
    let d="10+$i%20";
    command insert into mem_service \(ms_desc, ms_used, ms_starttime, ms_endtime, ms_state, \
        ms_creationtime, ms_modifiedtime, mem_id, user_id\) \
        values \(\"\", \"$used\", \"2012-07-$d\", \"2012-08-$d\", \"0\", \"2012-07-9\", \"\", \"$memid\", \"$userid\"\)
    command insert into mem_service \(ms_desc, ms_used, ms_state, mem_id\) \
        values \(\"\", \"$remain\", \"2\", \"$memid\"\)
done

# insert disk

for ((i=0;i<20;i++)) do
    let j="$i%5";
    let x="$i%3";
    let size="($i + 1) * 2 * 1000 * 1000 * 1000";
    id=`getvalue server server_id server_name name$x`
    command insert into disk \(disk_name, disk_desc, disk_total, disk_creationtime, server_id\) \
        values \(\"disk$i\", \"desc$i\", $size, \"2012-08-9\", \"$id\"\)
done

# insert disk_service

for ((i=0;i<20;i++)) do
    diskid=`getvalue disk disk_id disk_name disk$i`
    total=`getvalue disk disk_total disk_name disk$i`
    userid=`getvalue user user_id user_name admin`
    let used="($i + 1) * 1000 * 1000 * 1000"
    let remain="$total - $used"
    let d="10+$i%20";
    command insert into disk_service \(ds_desc, ds_used, ds_starttime, ds_endtime, ds_state, \
        ds_creationtime, ds_modifiedtime, disk_id, user_id\) \
        values \(\"\", \"$used\", \"2012-08-$d\", \"2012-09-$d\", \"0\", \"2012-08-9\", \"\", \"$diskid\", \"$userid\"\)
    command insert into disk_service \(ds_desc, ds_used, ds_state, disk_id\) \
        values \(\"\", \"$remain\", \"2\", \"$diskid\"\)
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
    command insert into ip_service \(ip_addr, ip_type, ip_creationtime, is_state\) \
        values \(\"166.111.0.$i\", 0, \"2012-10-01\", 2\);
    command insert into ip_service \(ip_addr, ip_type, ip_creationtime, is_state\) \
        values \(\"192.168.0.$i\", 1, \"2012-10-01\", 2\);
done

exit

# insert template
for ((i=0;i<10;i++)) do
    let mem="($i + 1) * 1024"
    let disk="($i + 10) * 1000"
    command insert into template \( \
        template_name, \
        template_desc, \
        template_ncpus, \
        template_mem, \
        template_disk, \
        template_bw, \
        template_creationtime, \
        template_modifiedtime\)
        values \( \
        \"name$i\", \
        \"\", \
        2, \
        $mem, \
        $disk, \
        32, \
        \"2012-07-10\"\)
done

exit

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

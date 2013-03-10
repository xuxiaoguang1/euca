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

for ((i=0;i<20;i++)) do
    server_id=`getvalue server server_id server_name name$i`
    command insert into cpu \(cpu_desc, cpu_total, cpu_creationtime, cpu_modifiedtime, server_id\) \
        values \(\"\", \"10\", \"2012-02-0$j\", \"\", \"$server_id\"\)
    cpu_id=`getvalue cpu cpu_id server_id $server_id`
    command insert into cpu_service \(cs_desc, cs_used, cs_state, cpu_id\) \
        values \(\"\", \"10\", \"2\", \"$cpu_id\"\)
done

# insert memory

for ((i=0;i<20;i++)) do
    let size="($i + 1) * 1024"
    server_id=`getvalue server server_id server_name name$i`
    command insert into memory \(mem_desc, mem_total, mem_creationtime, server_id\) \
        values \(\"desc$i\", $size, \"2012-07-9\", \"$server_id\"\)
    mem_id=`getvalue memory mem_id server_id $server_id`
    command insert into mem_service \(ms_desc, ms_used, ms_state, mem_id\) \
        values \(\"\", \"$size\", \"2\", \"$mem_id\"\)
done

# insert disk

for ((i=0;i<20;i++)) do
    let size="($i + 1) * 1000"
    server_id=`getvalue server server_id server_name name$i`
    command insert into disk \(disk_desc, disk_total, disk_creationtime, server_id\) \
        values \(\"desc$i\", $size, \"2012-08-9\", \"$server_id\"\)
    disk_id=`getvalue disk disk_id server_id $server_id`
    command insert into disk_service \(ds_desc, ds_used, ds_state, disk_id\) \
        values \(\"\", \"$size\", \"2\", \"$disk_id\"\)
done

# insert vm/vm_service

userid=`getvalue user user_id user_name admin`

for ((i=0;i<10;i++)) do
    command insert into vm \(vm_mark\) values \(\"vmware$i\"\)
    vmid=`getvalue vm vm_id vm_mark vmware$i`
    command insert into vm_service \(user_id, vm_id\) values \(\"$userid\", \"$vmid\"\)
done

vmid=`getvalue vm vm_id vm_mark vmware0`

# insert template
for ((i=0;i<10;i++)) do
    let mem="1024"
    let disk="($i + 10) * 1000"
    command insert into template \( \
        template_name, \
        template_desc, \
        template_ncpus, \
        template_mem, \
        template_disk, \
        template_bw, \
        template_creationtime, \
        template_modifiedtime\) \
        values \( \
        \"name$i\", \
        \"\", \
        2, \
        $mem, \
        $disk, \
        32, \
        \"2012-07-10\", \
        \"\"\)
done

exit

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

#!/bin/bash

command() {
    cmd=$@
    # echo $cmd
    mysql -u root --password=root -D eucalyptus -e "$cmd"
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

command 'delete from cpu_service where 1=1'
command 'delete from cpu where 1=1'
command 'delete from server where 1=1'
command 'delete from user where 1=1'
command 'delete from groups where 1=1'
command 'delete from account where 1=1'

command insert into account \(account_name, account_email, account_descrip, account_state\) \
    values \(\"root\", \"email0\", \"desc0\", 0\)

id=`getvalue account account_id account_name root`


command insert into groups \(group_name, group_descrip, group_state, account_id\) \
    values \(\"group0\", \"desc0\", 0, $id\)


group=`getvalue groups group_id group_name group0`
account=`getvalue groups account_id group_name group0`

command insert into user \(user_name, user_pwd, user_title, user_mobile, user_email, user_type, user_state, group_id, account_id\) \
    values \(\"admin\", \"admin\", \"title\", \"mobile\", \"email\", 0, 0, $group, $account\)


for ((i=0;i<20;i++)) do
    let x="$i%3";
    command insert into server \(server_name, server_mark, server_image, server_conf, server_ip, server_bw, server_state\) \
        values \(\"name$i\", \"mark$i\", \"image$i\", \"conf$i\", \"192.168.134.$i\", \"$i\", \"$x\"\)
done

# insert cpu

for ((i=0;i<40;i++)) do
    let j="$i%20";
    id=`getvalue server server_id server_name name$j`
    command insert into cpu \(cpu_name, cpu_vendor, cpu_model, cpu_ghz, cpu_cache, server_id\) \
        values \(\"cpu$i\", \"vender$i\", \"model$i\", \"$i\", \"$i\", \"$id\"\)
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

exit

for ((i=0;i<19;i++)) do
    id=`getvalue server server_id server_name name$i`
    let d="12+$i";
    let x="$i%3";
    if [ $x -ne 2 ]; then
        command insert into server_service \(ss_starttime, ss_life, ss_state, server_id\) \
            values \(\"2012-07-$d\", 32, "$x", "$id"\);
    else
        command insert into server_service \(ss_state, server_id\) \
            values \("$x", "$id"\);
    fi
done

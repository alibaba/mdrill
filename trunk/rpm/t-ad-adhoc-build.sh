#!/bin/bash
export temppath=$1
cd $temppath/
df -h 
pwd

mvn clean && mvn package   assembly:assembly
cd $temppath/rpm/
if [ `cat /etc/redhat-release|cut -d " " -f 7|cut -d "." -f 1` = 4 ]
then
sed -i  "s/^Release:.*$/Release: "$4".el4/" $2.spec
else
sed -i  "s/^Release:.*$/Release: "$4".el5/" $2.spec
fi
sed -i  "s/^Version:.*$/Version: "$3"/" $2.spec
rpm_create -v $3 -r $4 $2.spec -k
svn revert $2.spec
mv `find . -name $2-$3-$4*rpm`

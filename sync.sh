#!/bin/bash

PROJEKT=`pwd | rev | cut -d "/" -f 1 | rev`

DEP=conf/dependencies.yml
DEPBAK=conf/dependencies.yml.bak
MAJOR=`grep $PROJEKT conf/dependencies.yml | cut  -d ' ' -f 5 | cut -d '.' -f 1`
SUB=`grep $PROJEKT conf/dependencies.yml | cut  -d ' ' -f 5 | cut -d '.' -f 2`
MINOR=`grep $PROJEKT conf/dependencies.yml | cut  -d ' ' -f 5 | cut -d '.' -f 3`

declare i MINOR=$((MINOR+1))
sed  -e "s/$PROJEKT\s\s*\(.*\)*/$PROJEKT $MAJOR.$SUB.$MINOR/" $DEP > $DEPBAK
mv $DEPBAK $DEP

/usr/local/play_framework/current/play build-module 

scp dist/* root@vsvr1.no-ip.biz:/var/www/playmodules/



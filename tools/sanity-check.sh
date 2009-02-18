#!/bin/sh
# This file is part of InfoGrid(tm). You may not use this file except in
# compliance with the InfoGrid license. The InfoGrid license and important
# disclaimers are contained in the file LICENSE.InfoGrid.txt that you should
# have received with InfoGrid. If you have not received LICENSE.InfoGrid.txt
# or you do not consent to all aspects of the license and the disclaimers,
# no license is granted; do not use this file.
#
# For more information about InfoGrid go to http://infogrid.org/
#
# Copyright 1998-2009 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
# All rights reserved.
#
# (end of header)
#
# Performs some sanity checks for common errors. Should be run prior to
# check-in

FLAGS="-i";
THISYEAR="2009";

echo '** Checking that no funny paths exist. **'
grep ${FLAGS} '\.\./\.\./\.\.' {modules*,apps*,tests*}/*/nbproject/project.properties

echo '** Checking that the vendor is set right. **'
grep ${FLAGS} application.vendor {modules*,apps*,tests*}/*/nbproject/project.properties | grep -v InfoGrid.org

echo '** Checking copyright. **'
for f in `svn status | egrep -v '^D|^\?' | awk '{ print $2 }'`; do
	egrep -H '(copyright|Copyright|&copy).*-[0-9]{4}' $f | grep -v "${THISYEAR}" > /dev/null && echo $f
done

for pattern in "$@"; do
	echo '** Checking that pattern' ${pattern} 'does not exist. **'
	find . -type f -and -not -path '*/.svn/*' -exec grep ${FLAGS} -H ${pattern} {} \;
done

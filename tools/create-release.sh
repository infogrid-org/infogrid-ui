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
# Create an InfoGrid release.
#

set -e

script=tools/`basename $0`

if [ ! -d modules.fnd ]; then
        echo "ERROR: this script must be invoked from the root directory of the branch using the command ${script}"
        exit 1;
fi;

if [ ! -d dist ]; then
	echo "ERROR: Cannot find directory 'dist'. Build first."
	exit 1;
fi;

version=;
for arg in $*; do
	if [ -z $version ]; then
		version=$arg;
	else
		echo "ERROR: Unexpected argument $arg."
		echo "Synposys: $script <version tag>"
		exit 1;
	fi
done

if [ -z $version ]; then
	version=`date "+%Y%m%dZ%H%M%S"`;
fi

upload=upload;
filename="infogrid-$version";

echo "Creating InfoGrid version $filename.";

mkdir -p $upload;
mv dist $filename;
tar -c -z -f $upload/$filename-all.tgz $filename;
tar --exclude '*/apps.*' --exclude '*.docs*' --exclude '*javadoc-overview.html' -c -z -f $upload/$filename-modules-nodocs.tgz $filename;
tar --exclude '*/apps.*' -c -z -f $upload/$filename-modules-docs.tgz $filename;
cp $filename/apps.fnd/org.infogrid.meshworld.war $upload/org.infogrid.meshworld-$version.war;
cp $filename/apps.net/org.infogrid.meshworld.net.war $upload/org.infogrid.meshworld.net-$version.war;
mkdir -p $upload/$filename-javadoc
cp $filename/javadoc-overview.html $upload/$filename-javadoc/
( cd $filename ; tar -c -f - */*.docs | ( cd ../$upload/$filename-javadoc; tar xf - ))
exit 0;


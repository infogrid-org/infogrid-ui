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
# Copyright 1998-2008 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
# All rights reserved.
#
# (end of header)
#
# Makes it easier to add a new NetBeans project into SVN
#

script=tools/`basename $0`

if [ ! -d modules.fnd ]; then
        echo "ERROR: this script must be invoked from the root directory of the branch using the command ${script}"
        exit 1;
fi;

if [ $# != 1 ]; then
	echo "Synopsis: $0 <project directory>"
	exit 1;
fi
if [ ! -d $1 ]; then
	echo ERROR: $1 is not a directory
	exit 1;
fi

export PROJ=$1;

svn add -N ${PROJ}
svn propset svn:ignore -F tools/help-files/ignore-build-dist ${PROJ}
svn add ${PROJ}/{src,web,test,infogrid*} ${PROJ}/build.xml
svn add -N ${PROJ}/nbproject
svn propset svn:ignore -F tools/help-files/ignore-private-genfiles ${PROJ}/nbproject
svn add ${PROJ}/nbproject/{ant-deploy,build-impl,project}.xml ${PROJ}/nbproject/project.properties

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
# Determine the difference between this branch and some other

script=tools/`basename $0`

if [ ! -d modules.fnd ]; then
        echo "ERROR: this script must be invoked from the root directory of the branch using the command ${script}"
        exit 1;
fi;

branch=;

for arg in $*; do
        if [ "${branch}" = '' ]; then
                branch="${arg}";
        else
                echo "ERROR: Unknown argument: $arg"
                exit 1;
        fi
        shift;
done
if [ "${branch}" = '' ]; then
	echo "ERROR: need branch argument"
	exit 1
fi

diff -r -x .svn -x genfiles.properties -x private -x build -x dist . "${branch}"

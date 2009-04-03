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
# Updates TLD files in a project.
#

modprefix=modules;
script=tools/`basename $0`

if [ ! -d modules.fnd ]; then
        echo "ERROR: this script must be invoked from the root directory of the branch using the command ${script}"
        exit 1;
fi;

module=;

for arg in $*; do
	if [ "${module}" == "" ]; then
		module="${arg}";
	fi
done;

if [ "${module}" == "" ]; then
	echo Synopsis:
	echo "    ${script} <module>"
	echo "        <module>: name of a module, e.g. 'apps.fnd/org.infogrid.meshworld"
	exit 1;
fi

for f in `find ${module}/web/v -name '*.tld' -print`; do
	g=`echo $f | sed "s#^\${module}/web/v/##g"`;
	cp ${modprefix}*/*/src/$g "${module}/web/v/$g"
done;

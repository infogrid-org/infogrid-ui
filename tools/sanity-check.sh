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
for f in `svn status | egrep -v '^D|^\?' | cut -c 8-`; do
	egrep -H '(copyright|Copyright|&copy|\(C\)).*[0-9]{4}' $f | grep -v "${THISYEAR}" > /dev/null && echo $f
done

echo '** Checking for empty directories. **'
for f in `find modules* apps* tests* tools* -type d -and -not -path '*.svn*' -and -not -name src -print`; do
	if [ 0 == `ls -1 "$f/" | wc -l` ]; then
		echo $f
	fi
done

echo '** Checking that TLD files copied across projects are the same **'
for v in `find . -path '*/web/v' -and -not -path '*/build/*'`; do
	for f in `find ${v} -name '*.tld' -print`; do
		g=`echo $f | sed "s#^\${v}/##g"`;
#		/bin/echo -n '---- now looking at: '
#		ls modules.*/*/src/$g ${f}
		diff -H -q modules.*/*/src/$g ${f} | sed 's/ and//'
	done;
done;

echo '** Checking that Viewlet CSS files copied across projects are the same **'
for f in apps.net/org.infogrid.meshworld.net ; do
	for c in \
web/v/org/infogrid/jee/shell/http/HttpShellVerb.css \
web/v/org/infogrid/jee/taglib/candy/OverlayTag.css \
web/v/org/infogrid/jee/taglib/mesh/RefreshTag.css \
web/v/org/infogrid/jee/taglib/viewlet/ViewletAlternativesTag.css \
web/v/org/infogrid/jee/viewlet/graphtree/GraphTreeViewlet.css \
web/v/org/infogrid/jee/viewlet/meshbase/AllMeshObjectsViewlet.css \
web/v/org/infogrid/jee/viewlet/modelbase/AllMeshTypesViewlet.css \
web/v/org/infogrid/jee/viewlet/objectset/ObjectSetViewlet.css \
web/v/org/infogrid/jee/viewlet/propertysheet/PropertySheetViewlet.css \
web/v/org/infogrid/jee/viewlet/wikiobject/WikiObjectDisplayViewlet.css \
web/v/org/infogrid/jee/viewlet/wikiobject/WikiObjectEditViewlet.css \
; do
		diff -H -q apps.fnd/org.infogrid.meshworld/$c $f/$c | sed 's/ and//'
	done;
done

for pattern in "$@"; do
	echo '** Checking that pattern' ${pattern} 'does not exist. **'
	find . -type f -and -not -path '*/.svn/*' -exec grep ${FLAGS} -H ${pattern} {} \;
done

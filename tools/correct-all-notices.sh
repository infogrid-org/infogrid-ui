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
# Replaces all copyright notices with the correct ones: Java, properties, XML files
#

export tempsuffix=".fix-notice";

echo "-- Fixing Java files --"

find modules.fnd modules.net apps.fnd apps.net tests.fnd tests.net -name '*.java' | {
    while read f
    do
        echo $f;
        /bin/mv $f $f$tempsuffix;
        perl tools/correct-java-notice.pl <"$f$tempsuffix" >$f
    done
}

echo "-- Fixing properties files --"

find modules.fnd modules.net apps.fnd apps.net tests.fnd tests.net -name '*.properties' | {
    while read f
    do
        echo $f;
        /bin/mv $f "$f$tempsuffix";
        perl tools/correct-properties-notice.pl <"$f$tempsuffix" >$f
    done
}

echo "-- Fixing XML files --"

find modules.fnd modules.net apps.fnd apps.net tests.fnd tests.net \( -name '*.xml' -or -name 'module.adv' \) | {
    while read f
    do
        echo $f;
        /bin/mv $f "$f$tempsuffix";
        perl tools/correct-xml-notice.pl <"$f$tempsuffix" >$f
    done
}

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
# Print some statistics. Not the world's more efficient algorithm, but that doesn't seem very important for this purpose here.
#

script=tools/`basename $0`

if [ ! -d modules.fnd ]; then
        echo "ERROR: this script must be invoked from the root directory of the branch using the command ${script}"
        exit 1;
fi;

ECHO=/bin/echo;
DIRS="modules.fnd modules.net apps.fnd apps.net tests.fnd tests.net"
SKIP1='.*/build/.*';
SKIP2='.*/dist/.*';

${ECHO} 'Spilling the beans ... this might take a while. You have:'
${ECHO} '    ' `find ${DIRS} \( -name project.xml -and \! -regex "${SKIP1}" -and \! -regex "${SKIP2}" \) -print | wc -l` 'NetBeans projects.'
${ECHO} '    ' `find ${DIRS} \( -name model.xml -and \! -regex "${SKIP1}" -and \! -regex "${SKIP2}" \) -print | wc -l` 'InfoGrid models.'
${ECHO} -n '    ' `find ${DIRS} \( -name \*.java -and \! -regex "${SKIP1}" -and \! -regex "${SKIP2}" \) -print | wc -l` 'Java files'
${ECHO} ', of which' `find ${DIRS} \( -name \*Test[0-9]\*.java -and \! -regex "${SKIP1}" -and \! -regex "${SKIP2}" \) -print | wc -l` 'are test files.'
${ECHO} '    ' `find ${DIRS} \( -name \*.properties -and \! -regex "${SKIP1}" -and \! -regex "${SKIP2}" \) -print | grep -v nbproject | wc -l` 'properties files (not counting NetBeans files).'
${ECHO} '    ' `find ${DIRS} \( -name \*.jsp -and \! -regex "${SKIP1}" -and \! -regex "${SKIP2}" \) -print | wc -l` 'JSP files'
${ECHO} '    ' `find ${DIRS} \( -name \*.html -and \! -regex "${SKIP1}" -and \! -regex "${SKIP2}" \) -print | wc -l` 'HTML files'
${ECHO} '    ' `find ${DIRS} \( -name \*.css -and \! -regex "${SKIP1}" -and \! -regex "${SKIP2}" \) -print | wc -l` 'CSS files'
${ECHO} '    ' `find ${DIRS} \( \( -name \*.jpg -or -name \*.gif -or -name \*.png \) -and \! -regex "${SKIP1}" -and \! -regex "${SKIP2}" \) -print | wc -l` 'image files'
${ECHO} -n '    ' `find ${DIRS} \( -name \*.java -and \! -regex "${SKIP1}" -and \! -regex "${SKIP2}" \) -exec cat {} \; | wc -l` 'lines of Java.'
${ECHO} -n ' (' `find ${DIRS} \( -name \*.java -and \! -regex "${SKIP1}" -and \! -regex "${SKIP2}" \) -exec cat {} \; | wc -c` 'bytes'
${ECHO} ',' `find ${DIRS} \( -name \*.java -and \! -regex "${SKIP1}" -and \! -regex "${SKIP2}" \) -exec cat {} \; | gzip | wc -c` 'bytes compressed)'
${ECHO} 'This branch currently takes up' `du -s -h | sed -e 's/[ \t\.]//g'` 'bytes of disk space.'
${ECHO} 'Did you expect more or less? ;-)'

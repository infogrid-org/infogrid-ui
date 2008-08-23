#!/bin/sh
#
# Updates TLD files in a project.
#

modprefix=modules;
script=tools/`basename $0`

if [ ! -d modules ]; then
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
	echo "        <module>: name of a module, e.g. 'apps/org.infogrid.meshworld"
	exit 1;
fi

for f in `find ${module}/web/v -name '*.tld' -print`; do
	g=`echo $f | sed "s#^\${module}/web/v/##g"`;
	cp ${modprefix}*/*/src/$g "${module}/web/v/$g"
done;

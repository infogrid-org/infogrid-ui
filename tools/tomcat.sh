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
# Make it easier to invoke deploy/undeploy commands on Tomcat.
#

script=tools/`basename $0`

if [ ! -d modules.fnd ]; then
	echo "ERROR: this script must be invoked from the root directory of the branch using the command ${script}"
	exit 1;
fi;

# Use the ant in SVN
export ANT_HOME=vendors/ant.apache.org/apache-ant
export PATH=$ANT_HOME/bin:$PATH

RUNFLAGS=-Dno.deps=1;

do_nothing=1;
do_deploy=1;
do_undeploy=1;
verbose=1;
help=1;
ANTFLAGS=;
CONFIG=;
project=;
path=;

for arg in $*; do
	if [ "${ANTFLAGS}" = 'ANTFLAGS' ]; then
		ANTFLAGS="$arg";
	elif [ "${CONFIG}" = 'CONFIG' ]; then
		CONFIG="$arg";
	elif [ "$arg" = '-deploy' ]; then
		do_deploy=0;
	elif [ "$arg" = '-undeploy' ]; then
		do_undeploy=0;
	elif [ "$arg" = '-n' ]; then
		do_nothing=0;
	elif [ "$arg" = '-v' ]; then
		verbose=0;
	elif [ "$arg" = '-h' ]; then
		help=0;
	elif [ "$arg" = '-c' ]; then
		CONFIG='CONFIG';
	elif [ "$arg" = '-antflags' ]; then
		ANTFLAGS='ANTFLAGS';
	elif [ "$project" = '' ]; then
		project="${arg}";
	elif [ "$path" = '' ]; then
		project="${path}";
	else
		echo "ERROR: Cannot work on two projects at a time: ${project} vs. ${arg}."
		exit 1;
	fi
	shift;
done

if [ "${help}" = 0 -o "${ANTFLAGS}" = 'ANTFLAGS' -o "${CONFIG}" = 'CONFIG' ]; then
	echo Synopsis:
	echo "    ${script} [-v][-n] -deploy [-antflags <flags>] -c <config file> <project> [<path>]"
	echo "    ${script} [-v][-n] -undeploy [-antflags <flags>] -c <config file> <project> [<path>]"
	echo "    ${script} -h"
	echo "        -v: verbose output"
	echo "        -h: this help"
	echo "        -n: do not execute, only print"
	echo "        -deploy: deploy the project"
	echo "        -undeploy: undeploy the project"
	echo "        -antflags <flags>: pass flags to ant invocation"
	echo "        -c <configfile>: use configuration file"
	echo "        <project>: relative path to project's top directory"
	echo "        <path>: if given, context path for deployment; defaults to project name"
	exit 1;
fi

if [ "${project}" = '' ]; then
	echo ERROR: No project given.
	exit 1;
fi
if [ ! -d "${project}" ]; then
	echo ERROR: directory ${project} does not exist.
	exit 1;
fi

if [ "${CONFIG}" = '' ]; then
	echo ERROR: No config file given.
	exit 1;
fi
if [ ! -r "${CONFIG}" ]; then
	echo ERROR: Configuration file "${CONFIG}" cannot be read.
	exit 1;
fi
ANTFLAGS="${ANTFLAGS} -Dbuild.properties=../../${CONFIG}"

if [ "${do_deploy}" != 0 -a "${do_undeploy}" != 0 ]; then
	echo ERROR: Must either -deploy or -undeploy.
	exit 1;
fi

if [ "${do_nothing}" = 0 ]; then
	/bin/echo -n Will
	if [ "${do_deploy}" = 0 ]; then
		/bin/echo -n " deploy"
	fi
	if [ "${do_undeploy}" = 0 ]; then
		/bin/echo -n " undeploy"
	fi
	/bin/echo -n : ${project} with configuration ${CONFIG}
	if [ ! -z "${path}" ]; then
		/bin/echo -n " at path ${path}"
	fi
	if [ "${verbose}" = 0 ]; then
		/bin/echo -n " (verbose)"
	fi
	/bin/echo .
	exit 0;
fi
# We can do deploy and undeploy at the same time, just do undeploy first

trap stop 2
function stop
{
	echo 'Interrupted by control-c. Exiting ...'
	exit 1;
}

if [ "${do_undeploy}" = 0 ]; then
	cmd="ant -f ${project}/build.xml ${ANTFLAGS} tomcat-undeploy"
        if [ ! -z "${path}" ]; then
		cmd="${cmd} -Dwebapp.path=${path}"
	fi
	${cmd}
fi

if [ "${do_deploy}" = 0 ]; then
	cmd="ant -f ${project}/build.xml ${ANTFLAGS} tomcat-deploy"
        if [ ! -z "${path}" ]; then
		cmd="${cmd} -Dwebapp.path=${path}"
	fi
	${cmd}
fi
	

echo '** DONE **'


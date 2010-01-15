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
# Build all of InfoGrid. Run with option -h to see synopsis.
#

# Find branch root.
DIR=`pwd`

if [ -d "$DIR/ig-tools" ]; then
	BRANCH=$DIR
elif [ -d "$DIR/../ig-tools" ]; then
	cd ..
	BRANCH=`pwd`
elif [ -d "$DIR/../../ig-tools" ]; then
	cd ../..
	BRANCH=`pwd`
else
	echo "ERROR: cannot find root of the branch (checked ., .., ../..)" 
	exit 1
fi

# Use the ant in SVN
export ANT_HOME=$BRANCH/ig-vendors/libraries/ant.apache.org/apache-ant
export PATH=$ANT_HOME/bin:$PATH

FLAGS=
TMPFILE=`mktemp /tmp/infogrid-build-temp.XXXX`;
JAVADOC_OVERVIEW=dist/javadoc-overview.html;
BUILDLIST=.IG-BUILDLIST

do_clean=1;
do_build=1;
do_run=1;
do_doc=1;
do_dist=1;
do_nothing=1;
do_all=1;
verbose=1;
help=1;
projects=;
ANTFLAGS=;
CONFIG=;

for arg in $*; do
	if [ "${ANTFLAGS}" = 'ANTFLAGS' ]; then
		ANTFLAGS="$arg";
		FLAGS="${FLAGS} ${arg}"
	elif [ "${CONFIG}" = 'CONFIG' ]; then
		CONFIG="$arg";
		FLAGS="${FLAGS} ${arg}"
	elif [ "$arg" = '-clean' ]; then
		do_clean=0;
	elif [ "$arg" = '-build' ]; then
		do_build=0;
	elif [ "$arg" = '-run' ]; then
		do_run=0;
	elif [ "$arg" = '-doc' ]; then
		do_doc=0;
	elif [ "$arg" = '-dist' ]; then
		do_dist=0;
	elif [ "$arg" = '-n' ]; then
		do_nothing=0;
		FLAGS="${FLAGS} ${arg}"
	elif [ "$arg" = '-v' ]; then
		verbose=0;
		FLAGS="${FLAGS} ${arg}"
	elif [ "$arg" = '-h' ]; then
		help=0;
		FLAGS="${FLAGS} ${arg}"
	elif [ "$arg" = '-a' ]; then
		do_all=0;
	elif [ "$arg" = '-c' ]; then
		CONFIG='CONFIG';
		FLAGS="${FLAGS} ${arg}"
	elif [ "$arg" = '-antflags' ]; then
		ANTFLAGS='ANTFLAGS';
		FLAGS="${FLAGS} ${arg}"
	elif [ -d "${DIR}/${arg}" ]; then
		projects="$projects $arg"
		# Don't set FLAGS here
	else
		echo "ERROR: Unknown argument: $arg"
		echo "       Cannot be a project because it does not exist."
		exit 1;
	fi
	shift;
done

# echo args ${do_clean} ${do_build} ${do_run} ${do_doc} ${do_dist} ${do_nothing} ${do_all} ${verbose} ${ANTFLAGS}
# exit 0;

if [ "${help}" = 0 -o "${ANTFLAGS}" = 'ANTFLAGS' -o "${CONFIG}" = 'CONFIG' ]; then
	echo Synopsis:
	echo "    ${script} [-v][-h][-n] [-clean][-build][-doc][-run] [-antflags <flags>] [<project>...]"
	echo "        -v: verbose output"
	echo "        -h: this help"
	echo "        -n: do not execute, only print"
	echo "        -a: complete rebuild, equivalent to -clean -build -run -doc -dist "
	echo "        -clean: remove old build artifacts"
	echo "        -build: build"
	echo "        -doc: document"
	echo "        -run: run"
	echo "            (more than one of -clean,-build,-run,-doc,-dist may be given. Default is -build,-run,-doc,-dist)"
	echo "        -antflags <flags>: pass flags to ant invocation"
	echo "        -c <configfile>: use configuration file"
	echo "        <project>: name of a directory that can be built. If not given, use content of file $BUILDLIST"
	exit 1;
fi

if [ "${CONFIG}" != '' ]; then
	if [ ! -r "${DIR}/${CONFIG}" ]; then
		echo ERROR: Configuration file "${DIR}/${CONFIG}" cannot be read.
		exit 1;
	fi
	ANTFLAGS="${ANTFLAGS} -Dbuild.properties=${DIR}/${CONFIG}"
fi
ANTFLAGS="${ANTFLAGS} -Dno.deps=1"

if [ "${do_all}" = 0 ]; then
	do_clean=0;
	do_build=0;
	do_run=0;
	do_doc=0;
	do_dist=0;
else
	if [ "${do_clean}" = 1 -a "${do_build}" = 1 -a "${do_doc}" = 1 -a "${do_run}" = 1 -a "${do_dist}" = 1 ]; then
		do_clean=1;
		do_build=0;
		do_run=0;
		do_doc=0;
		do_dist=0;
	fi
fi

if [ "${do_nothing}" = 0 ]; then
	/bin/echo -n Will
	if [ "${do_clean}" = 0 ]; then
		/bin/echo -n " clean"
	fi
	if [ "${do_build}" = 0 ]; then
		/bin/echo -n " build"
	fi
	if [ "${do_run}" = 0 ]; then
		/bin/echo -n " run"
	fi
	if [ "${do_doc}" = 0 ]; then
		/bin/echo -n " doc"
	fi
	if [ "${do_dist}" = 0 ]; then
		/bin/echo -n " dist"
	fi
	if [ -z "${projects}" ]; then
		if [ -r "${DIR}/${BUILDLIST}" ]; then
			projects=`sed -e 's/#.*$//g' "${DIR}/${BUILDLIST}" | sed -s 's/\[.*\]//g`
		else
			echo "ERROR: No projects given, and no "${DIR}/${BUILDLIST}". Don't know what to do."
			exit 1
		fi
	fi
	/bin/echo -n : $projects
	if [ "${verbose}" = 0 ]; then
		/bin/echo -n " (verbose)"
	fi
	/bin/echo .
	exit 0;
fi

TARGETS=
if [ "${do_clean}" = 0 ]; then
	TARGETS="${TARGETS} -clean"
fi
if [ "${do_build}" = 0 ]; then
	TARGETS="${TARGETS} -build"
fi
if [ "${do_run}" = 0 ]; then
	TARGETS="${TARGETS} -run"
fi
if [ "${do_doc}" = 0 ]; then
	TARGETS="${TARGETS} -doc"
fi
if [ "${do_dist}" = 0 ]; then
	TARGETS="${TARGETS} -dist"
fi

trap stop 2
function stop {
	echo 'Interrupted by control-c. Exiting ...'
	exit 1;
}

run_command()
{
	if [ "${verbose}" = 0 ]; then
		echo About to execute: $*
		$*
        	if [ "${?}" -gt 0 ]; then
			echo FAILED: $*
			return 1;
		fi
	else
		$* >${TMPFILE} 2>&1
        	if [ "${?}" -gt 0 ]; then
			cat ${TMPFILE};
			echo FAILED: $*
			return 1;
		else
			grep -i warning ${TMPFILE} | egrep -v '_jsp\.java.*unchecked call to add\(E\) as a member of the raw type java\.util\.List' | egrep -v '[0-9]+[ \t]+warnings$'
# This grep prevents us getting annoyed by generics warnings caused by the JSP-to-Java compiler, while still getting our own warnings through.
		fi
	fi
	return 0;
}

for t in ${TARGETS}; do
	realProjects=${projects}
        if [ -z "${projects}" ]; then
                if [ -r "${DIR}/${BUILDLIST}" ]; then
                        realProjects=`sed -e 's/#.*$//g' "${DIR}/${BUILDLIST}" | grep -v "\[no$t\]" | sed -e 's/\[.*\]//g'`
                else
                        echo "ERROR: No projects given, and no "${DIR}/${BUILDLIST}". Don't know what to do."
                        exit 1
                fi
        fi
	for p in ${realProjects}; do
		echo ' -' `date '+%Y%m%d-%H:%M:%S'` : ${DIR}/${p} $t
		if [ -r "${DIR}/${p}/${BUILDLIST}" ]; then
			# Recursively invoke ourselves
			cd "${DIR}/${p}" && ${BRANCH}/ig-tools/build.sh ${FLAGS} $t
        		if [ "${?}" -gt 0 ]; then
				echo FAILED: "${DIR}/${p}"
				exit 1;
			fi
		elif [ -r "${DIR}/${p}/build.xml" ]; then
			# Invoke ant
			if [ $t = "-clean" ]; then
				antt=clean
			elif [ $t = "-build" ]; then
				antt=jar
			elif [ $t = "-run" ]; then
				antt=run
			elif [ $t = "-doc" ]; then
				antt=javadoc
			fi # don't do dist
			if [ ! -z "${antt}" ]; then
				run_command ant -f "${DIR}/${p}/build.xml" ${ANTFLAGS} $antt
        			if [ "${?}" -gt 0 ]; then
					echo FAILED: "${DIR}/${p}"
					exit 1;
				fi
			fi
		else
			echo ERROR: Cannot build project "${DIR}/${p}", no ${BUILDLIST} or build.xml found.
			exit 1
		fi
	done
done

exit 0;


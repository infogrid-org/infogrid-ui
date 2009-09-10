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

script=tools/`basename $0`

if [ ! -d modules.fnd ]; then
	echo "ERROR: this script must be invoked from the root directory of the branch using the command ${script}"
	exit 1;
fi;

# Use the ant in SVN
export ANT_HOME=vendors/ant.apache.org/apache-ant
export PATH=$ANT_HOME/bin:$PATH

CLEANFLAGS=-Dno.deps=1;
BUILDFLAGS=-Dno.deps=1;
DOCFLAGS=;
RUNFLAGS=-Dno.deps=1;
DEBIANFLAGS=-Dno.deps=1;
TMPFILE=`mktemp /tmp/infogrid-build-temp.XXXX`;
JAVADOC_OVERVIEW=dist/javadoc-overview.html;

do_clean=1;
do_build=1;
do_run=1;
do_doc=1;
do_dist=1;
do_modules_fnd=1;
do_modules_net=1;
do_apps_fnd=1;
do_apps_net=1;
do_tests_fnd=1;
do_tests_net=1;
do_nothing=1;
do_all=1;
verbose=1;
help=1;
ANTFLAGS=;
CONFIG=;

for arg in $*; do
	if [ "${ANTFLAGS}" = 'ANTFLAGS' ]; then
		ANTFLAGS="$arg";
	elif [ "${CONFIG}" = 'CONFIG' ]; then
		CONFIG="$arg";
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
	elif [ "$arg" = '-v' ]; then
		verbose=0;
	elif [ "$arg" = '-h' ]; then
		help=0;
	elif [ "$arg" = '-a' ]; then
		do_all=0;
	elif [ "$arg" = '-c' ]; then
		CONFIG='CONFIG';
	elif [ "$arg" = '-antflags' ]; then
		ANTFLAGS='ANTFLAGS';
	elif [ "$arg" = 'modules.fnd' ]; then
		do_modules_fnd=0;
	elif [ "$arg" = 'modules.net' ]; then
		do_modules_net=0;
	elif [ "$arg" = 'apps.fnd' ]; then
		do_apps_fnd=0;
	elif [ "$arg" = 'apps.net' ]; then
		do_apps_net=0;
	elif [ "$arg" = 'tests.fnd' ]; then
		do_tests_fnd=0;
	elif [ "$arg" = 'tests.net' ]; then
		do_tests_net=0;
	else
		echo "ERROR: Unknown argument: $arg"
		exit 1;
	fi
	shift;
done

# echo args ${do_clean} ${do_build} ${do_run} ${do_doc} ${do_dist} ${do_modules_fnd} ${do_modules_net} ${do_apps_fnd} ${do_apps_net} ${do_tests_fnd} ${do_tests_net} ${do_nothing} ${do_all} ${verbose} ${ANTFLAGS}
# exit 0;

if [ "${help}" = 0 -o "${ANTFLAGS}" = 'ANTFLAGS' -o "${CONFIG}" = 'CONFIG' ]; then
	echo Synopsis:
	echo "    ${script} [-v][-h][-n] [-clean][-build][-doc][-run] [-antflags <flags>] [<category>...]"
	echo "        -v: verbose output"
	echo "        -h: this help"
	echo "        -n: do not execute, only print"
	echo "        -a: complete rebuild, equivalent to -clean -build -run -doc -dist modules.fnd modules.net apps.fnd apps.net tests.fnd tests.net"
	echo "        -clean: remove old build artifacts"
	echo "        -build: build"
	echo "        -doc: document"
	echo "        -run: run"
	echo "            (more than one of -clean,-build,-run,-doc,-dist may be given. Default is -build,-run,-doc,-dist)"
	echo "        -antflags <flags>: pass flags to ant invocation"
	echo "        -c <configfile>: use configuration file"
	echo "        <category>: one or more of modules.fnd, modules.net, apps.fnd, apps.net, tests.fnd, tests.net"
	exit 1;
fi

if [ "${CONFIG}" != '' ]; then
	if [ ! -r "${CONFIG}" ]; then
		echo ERROR: Configuration file "${CONFIG}" cannot be read.
		exit 1;
	fi
	ANTFLAGS="${ANTFLAGS} -Dbuild.properties=../../${CONFIG}"
fi

if [ "${do_all}" = 0 ]; then
	do_clean=0;
	do_build=0;
	do_run=0;
	do_doc=0;
	do_dist=0;
	do_modules_fnd=0;
	do_modules_net=0;
	do_apps_fnd=0;
	do_apps_net=0;
	do_tests_fnd=0;
	do_tests_net=0;
else
	if [ "${do_clean}" = 1 -a "${do_build}" = 1 -a "${do_doc}" = 1 -a "${do_run}" = 1 -a "${do_dist}" = 1 ]; then
		do_clean=1;
		do_build=0;
		do_run=0;
		do_doc=0;
		do_dist=0;
	fi
	if [ "${do_modules_fnd}" = 1 -a "${do_modules_net}" = 1 -a "${do_apps_fnd}" = 1 -a "${do_apps_net}" = 1 -a "${do_tests_fnd}" = 1 -a "${do_tests_net}" = 1 ]; then
		do_modules_fnd=0;
		do_modules_net=0;
		do_apps_fnd=0;
		do_apps_net=0;
		do_tests_fnd=0;
		do_tests_net=0;
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
	/bin/echo -n :
	if [ "${do_modules_fnd}" = 0 ]; then
		/bin/echo -n " modules.fnd"
	fi
	if [ "${do_modules_net}" = 0 ]; then
		/bin/echo -n " modules.net"
	fi
	if [ "${do_apps_fnd}" = 0 ]; then
		/bin/echo -n " apps.fnd"
	fi
	if [ "${do_apps_net}" = 0 ]; then
		/bin/echo -n " apps.net"
	fi
	if [ "${do_tests_fnd}" = 0 ]; then
		/bin/echo -n " tests.fnd"
	fi
	if [ "${do_tests_net}" = 0 ]; then
		/bin/echo -n " tests.net"
	fi
	if [ "${verbose}" = 0 ]; then
		/bin/echo -n " (verbose)"
	fi
	/bin/echo .
	exit 0;
fi

trap stop 2
function stop
{
	echo 'Interrupted by control-c. Exiting ...'
	exit 1;
}

run_command()
{
	echo ' -' `date '+%Y%m%d-%H:%M:%S'` : $1
	shift
	if [ "${verbose}" = 0 ]; then
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

filter_modules()
{
	egrep -v '^(\\s)*#' $1 | grep -v "$2" | sed -e 's/\[.*\]//g'
}

clean_module()
{
	run_command $1 ant ${ANTFLAGS} -f $1/build.xml ${CLEANFLAGS} clean
	return "${?}";
}

build_module()
{
	run_command $1 ant ${ANTFLAGS} -f $1/build.xml ${BUILDFLAGS} $2
	return "${?}";
}

run_module()
{
	run_command $1 ant ${ANTFLAGS} -f $1/build.xml ${RUNFLAGS} run
	return "${?}";
}

doc_module()
{
	run_command $1 ant ${ANTFLAGS} -f $1/build.xml ${DOCFLAGS} javadoc
	return "${?}";
}

dist_module()
{
	local ext;
	local f;
	for ext in jar war ser adv; do
		# No double-quotes here, need to expand wildcard
		for f in $1/dist/*.$ext; do
			if [ -f $f ]; then
				if [[ $f =~ /dist/org\.infogrid\. ]]; then
					run_command "$1 - copy $f" cp $f dist/$2/
				fi
			fi
		done;
	done

	if [ -d $1/dist/javadoc ]; then
		run_command "$1 - copy javadoc" cp -r $1/dist/javadoc dist/$1.docs
	fi
	return "${?}";
}

if [ "${do_clean}" = 0 ]; then
	if [ "${do_modules_fnd}" = 0 ]; then
		echo '**** Cleaning modules.fnd ****'
		for f in `filter_modules modules.fnd/ALLMODULES '\[noclean\]'`; do
			clean_module modules.fnd/$f || exit 1;
		done;
	fi
	if [ "${do_modules_net}" = 0 ]; then
		echo '**** Cleaning modules.net ****'
		for f in `filter_modules modules.net/ALLMODULES '\[noclean\]'`; do
			clean_module modules.net/$f || exit 1;
		done;
	fi

	if [ "${do_apps_fnd}" = 0 ]; then
		echo '**** Cleaning apps.fnd ****'
		for f in `filter_modules apps.fnd/ALLAPPS '\[noclean\]'`; do
			clean_module apps.fnd/$f || exit 1;
		done;
	fi
	if [ "${do_apps_net}" = 0 ]; then
		echo '**** Cleaning apps.net ****'
		for f in `filter_modules apps.net/ALLAPPS '\[noclean\]'`; do
			clean_module apps.net/$f || exit 1;
		done;
	fi

	if [ "${do_tests_fnd}" = 0 ]; then
		echo '**** Cleaning tests.fnd ****'
		for f in `filter_modules tests.fnd/ALLTESTS '\[noclean\]'`; do
			clean_module tests.fnd/$f || exit 1;
		done;
	fi
	if [ "${do_tests_net}" = 0 ]; then
		echo '**** Cleaning tests.net ****'
		for f in `filter_modules tests.net/ALLTESTS '\[noclean\]'`; do
			clean_module tests.net/$f || exit 1;
		done;
	fi

	echo '**** Cleaning dist ****'
	if [ "${verbose}" = 0 ]; then
		echo /bin/rm -rf dist
	fi
	/bin/rm -rf dist
fi

if [ "${do_build}" = 0 ]; then
	if [ "${do_modules_fnd}" = 0 ]; then
		echo '**** Building modules.fnd ****'
		for f in `filter_modules modules.fnd/ALLMODULES '\[nobuild\]'`; do
			build_module modules.fnd/$f jar || exit 1;
		done;
	fi
	if [ "${do_modules_net}" = 0 ]; then
		echo '**** Building modules.net ****'
		for f in `filter_modules modules.net/ALLMODULES '\[nobuild\]'`; do
			build_module modules.net/$f jar || exit 1;
		done;
	fi

	if [ "${do_apps_fnd}" = 0 ]; then
		echo '**** Building apps.fnd ****'
		for f in `filter_modules apps.fnd/ALLAPPS '\[nobuild\]'`; do
			build_module apps.fnd/$f dist || exit 1;
		done;
	fi
	if [ "${do_apps_net}" = 0 ]; then
		echo '**** Building apps.net ****'
		for f in `filter_modules apps.net/ALLAPPS '\[nobuild\]'`; do
			build_module apps.net/$f dist || exit 1;
		done;
	fi

	if [ "${do_tests_fnd}" = 0 ]; then
		echo '**** Building tests.fnd ****'
		for f in `filter_modules tests.fnd/ALLTESTS '\[nobuild\]'`; do
			build_module tests.fnd/$f jar || exit 1;
		done;
	fi
	if [ "${do_tests_net}" = 0 ]; then
		echo '**** Building tests.net ****'
		for f in `filter_modules tests.net/ALLTESTS '\[nobuild\]'`; do
			build_module tests.net/$f jar || exit 1;
		done;
	fi
fi

if [ "${do_run}" = 0 ]; then
	if [ "${do_tests_fnd}" = 0 ]; then
		echo '**** Running tests.fnd ****'
		for f in `filter_modules tests.fnd/ALLTESTS '\[norun\]'`; do
			run_module tests.fnd/$f run || exit 1;
		done;
	fi
	if [ "${do_tests_net}" = 0 ]; then
		echo '**** Running tests.net ****'
		for f in `filter_modules tests.net/ALLTESTS '\[norun\]'`; do
			run_module tests.net/$f run || exit 1;
		done;
	fi
fi

if [ "${do_doc}" = 0 ]; then
	if [ "${do_modules_fnd}" = 0 ]; then
		echo '**** Documenting modules.fnd ****'
		for f in `filter_modules modules.fnd/ALLMODULES '\[nodoc\]'`; do
			doc_module modules.fnd/$f || exit 1;
		done;
	fi
	if [ "${do_modules_net}" = 0 ]; then
		echo '**** Documenting modules.net ****'
		for f in `filter_modules modules.net/ALLMODULES '\[nodoc\]'`; do
			doc_module modules.net/$f || exit 1;
		done;
	fi

	if [ "${do_apps_fnd}" = 0 ]; then
		echo '**** Documenting apps.fnd ****'
		for f in `filter_modules apps.fnd/ALLAPPS '\[nodoc\]'`; do
			doc_module apps.fnd/$f || exit 1;
		done;
	fi
	if [ "${do_apps_net}" = 0 ]; then
		echo '**** Documenting apps.net ****'
		for f in `filter_modules apps.net/ALLAPPS '\[nodoc\]'`; do
			doc_module apps.net/$f || exit 1;
		done;
	fi

	if [ "${do_tests_fnd}" = 0 ]; then
		echo '**** Documenting tests.fnd ****'
		for f in `filter_modules tests.fnd/ALLTESTS '\[nodoc\]'`; do
			doc_module tests.fnd/$f || exit 1;
		done;
	fi
	if [ "${do_tests_net}" = 0 ]; then
		echo '**** Documenting tests.net ****'
		for f in `filter_modules tests.net/ALLTESTS '\[nodoc\]'`; do
			doc_module tests.net/$f || exit 1;
		done;
	fi
fi

if [ "${do_dist}" = 0 ]; then
	mkdir -p dist
	cp LICENSE.InfoGrid.txt LICENSE.agplv3.txt dist/
	echo '<html><head><title>InfoGrid JavaDoc Overview</title>' > $JAVADOC_OVERVIEW;
	echo '<style>' >> $JAVADOC_OVERVIEW;
	echo 'table { border-collapse: collapse; }' >> $JAVADOC_OVERVIEW;
	echo 'td { vertical-align: top; border: #f0f0f0 solid 1px; width: 25%; padding: 5px; }' >> $JAVADOC_OVERVIEW;
	echo '</style></head>' >> $JAVADOC_OVERVIEW;

	echo '<body><h1>InfoGrid<sup style="font-size:60%">TM</sup> JavaDoc Overview</h1>' >> $JAVADOC_OVERVIEW;
	echo '<table><tr>' >> $JAVADOC_OVERVIEW;
        if [ "${do_modules_fnd}" = 0 ]; then
		echo '**** Creating dist for modules.fnd ****'
		mkdir -p dist/modules.fnd
		echo '<td>' >> $JAVADOC_OVERVIEW;
		echo '<h2>modules.fnd</h2>' >> $JAVADOC_OVERVIEW;
		echo '<ul>' >> $JAVADOC_OVERVIEW;
		for f in `filter_modules modules.fnd/ALLMODULES '\[nodist\]' | sort`; do
			dist_module modules.fnd/$f modules.fnd || exit 1;
			echo "<li><a href=\"modules.fnd/$f.docs/index.html\">$f</a></li>" >> $JAVADOC_OVERVIEW;
		done;
		echo '</ul>' >> $JAVADOC_OVERVIEW;
		echo '</td>' >> $JAVADOC_OVERVIEW;
	fi
        if [ "${do_modules_net}" = 0 ]; then
                echo '**** Creating dist for modules.net ****'
                mkdir -p dist/modules.net
                echo '<td>' >> $JAVADOC_OVERVIEW;
                echo '<h2>modules.net</h2>' >> $JAVADOC_OVERVIEW;
                echo '<ul>' >> $JAVADOC_OVERVIEW;
                for f in `filter_modules modules.net/ALLMODULES '\[nodist\]' | sort`; do
                        dist_module modules.net/$f modules.net || exit 1;
                        echo "<li><a href=\"modules.net/$f.docs/index.html\">$f</a></li>" >> $JAVADOC_OVERVIEW;
                done;
                echo '</ul>' >> $JAVADOC_OVERVIEW;
                echo '</td>' >> $JAVADOC_OVERVIEW;
        fi
        if [ "${do_apps_fnd}" = 0 ]; then
                echo '**** Creating dist for apps.fnd ****'
                mkdir -p dist/apps.fnd
                echo '<td>' >> $JAVADOC_OVERVIEW;
                echo '<h2>apps.fnd</h2>' >> $JAVADOC_OVERVIEW;
                echo '<ul>' >> $JAVADOC_OVERVIEW;
                for f in `filter_modules apps.fnd/ALLAPPS '\[nodist\]' | sort`; do
                        dist_module apps.fnd/$f apps.fnd || exit 1;
                        echo "<li><a href=\"apps.fnd/$f.docs/index.html\">$f</a></li>" >> $JAVADOC_OVERVIEW;
                done;
                echo '</ul>' >> $JAVADOC_OVERVIEW;
                echo '</td>' >> $JAVADOC_OVERVIEW;
        fi
        if [ "${do_apps_net}" = 0 ]; then
                echo '**** Creating dist for apps.net ****'
                mkdir -p dist/apps.net
                echo '<td>' >> $JAVADOC_OVERVIEW;
                echo '<h2>apps.net</h2>' >> $JAVADOC_OVERVIEW;
                echo '<ul>' >> $JAVADOC_OVERVIEW;
                for f in `filter_modules apps.net/ALLAPPS '\[nodist\]' | sort`; do
                        dist_module apps.net/$f apps.net || exit 1;
                        echo "<li><a href=\"apps.net/$f.docs/index.html\">$f</a></li>" >> $JAVADOC_OVERVIEW;
                done;
                echo '</ul>' >> $JAVADOC_OVERVIEW;
                echo '</td>' >> $JAVADOC_OVERVIEW;
        fi
	echo '</td></table>' >> $JAVADOC_OVERVIEW;
	echo '</body></html>' >> $JAVADOC_OVERVIEW;
fi

echo '** DONE at' `date` '**'

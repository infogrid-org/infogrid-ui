#!/bin/sh
#
# Build all of InfoGrid. Run with option -h to see synopsis.
#

script=tools/`basename $0`

if [ ! -d modules.fnd ]; then
	echo "ERROR: this script must be invoked from the root directory of the branch using the command ${script}"
	exit 1;
fi;

CLEANFLAGS=-Dno.deps=1;
BUILDFLAGS=-Dno.deps=1;
DOCFLAGS=;
RUNFLAGS=-Dno.deps=1;
TMPFILE=`mktemp /tmp/infogrid-build-temp.XXXX`;

do_clean=1;
do_build=1;
do_doc=1;
do_run=1;
do_modules_fnd=1;
do_modules_net=1;
do_apps_fnd=1;
do_apps_net=1;
do_tests_fnd=1;
do_tests_net=1;
do_dist_fnd=1;
do_dist_net=1;
do_nothing=1;
do_all=1;
verbose=1;
help=1;
ANTFLAGS=;

for arg in $*; do
	if [ "${ANTFLAGS}" = 'ANTFLAGS' ]; then
		ANTFLAGS="$arg";
	elif [ "$arg" = '-clean' ]; then
		do_clean=0;
	elif [ "$arg" = '-build' ]; then
		do_build=0;
	elif [ "$arg" = '-doc' ]; then
		do_doc=0;
	elif [ "$arg" = '-run' ]; then
		do_run=0;
	elif [ "$arg" = '-n' ]; then
		do_nothing=0;
	elif [ "$arg" = '-v' ]; then
		verbose=0;
	elif [ "$arg" = '-h' ]; then
		help=0;
	elif [ "$arg" = '-a' ]; then
		do_all=0;
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
	elif [ "$arg" = 'dist.fnd' ]; then
		do_dist_fnd=0;
	elif [ "$arg" = 'dist.net' ]; then
		do_dist_net=0;
	else
		echo "ERROR: Unknown argument: $arg"
		exit 1;
	fi
	shift;
done

# echo args ${do_clean} ${do_build} ${do_doc} ${do_run} ${do_modules_fnd} ${do_modules_net} ${do_apps_fnd} ${do_apps_net} ${do_tests_fnd} ${do_tests_net} ${do_dist_fnd} ${do_dist_net} ${do_nothing} ${do_all} ${verbose} ${ANTFLAGS}
# exit 0;

if [ "${help}" = 0 -o "${ANTFLAGS}" = 'ANTFLAGS' ]; then
	echo Synopsis:
	echo "    ${script} [-v][-h][-n] [-clean][-build][-doc][-run] [-antflags <flags>] [<category>...]"
	echo "        -v: verbose output"
	echo "        -h: this help"
	echo "        -n: do not execute, only print"
	echo "        -a: complete rebuild, equivalent to -clean -build -doc -run modules.fnd modules.net apps.fnd apps.net tests.fnd tests.net dist.fnd dist.net"
	echo "        -clean: remove old build artifacts"
	echo "        -build: build"
	echo "        -doc: document"
	echo "        -run: run"
	echo "            (more than one of -clean,-build,-doc,-run may be given. Default is -build,-doc,-run)"
	echo "        -antflags <flags>: pass flags to ant invocation"
	echo "        <category>: one or more of modules.fnd, modules.net, apps.fnd, apps.net, tests.fnd, tests.net, dist.fnd, dist.net"
	exit 1;
fi

if [ "${do_all}" = 0 ]; then
	do_clean=0;
	do_build=0;
	do_doc=0;
	do_run=0;
	do_modules_fnd=0;
	do_modules_net=0;
	do_apps_fnd=0;
	do_apps_net=0;
	do_tests_fnd=0;
	do_tests_net=0;
	do_dist_fnd=0;
	do_dist_net=0;
else
	if [ "${do_clean}" = 1 -a "${do_build}" = 1 -a "${do_doc}" = 1 -a "${do_run}" = 1 ]; then
		do_clean=1;
		do_build=0;
		do_doc=0;
		do_run=0;
	fi
	if [ "${do_modules_fnd}" = 1 -a "${do_modules_net}" = 1 -a "${do_apps_fnd}" = 1 -a "${do_apps_net}" = 1 -a "${do_tests_fnd}" = 1 -a "${do_tests_net}" = 1 -a "${do_dist_fnd}" = 1 -a "${do_dist_net}" = 1 ]; then
		do_modules_fnd=0;
		do_modules_net=0;
		do_apps_fnd=0;
		do_apps_net=0;
		do_tests_fnd=0;
		do_tests_net=0;
		do_dist_fnd=0;
		do_dist_net=0;
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
	if [ "${do_doc}" = 0 ]; then
		/bin/echo -n " doc"
	fi
	if [ "${do_run}" = 0 ]; then
		/bin/echo -n " run"
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
	if [ "${do_dist_fnd}" = 0 ]; then
		/bin/echo -n " dist.fnd"
	fi
	if [ "${do_dist_net}" = 0 ]; then
		/bin/echo -n " dist.net"
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

doc_module()
{
	run_command $1 ant ${ANTFLAGS} -f $1/build.xml ${DOCFLAGS} javadoc
	return "${?}";
}

run_module()
{
	run_command $1 ant ${ANTFLAGS} -f $1/build.xml ${RUNFLAGS} run
	return "${?}";
}

expand_module()
{
	local cmd;
	pushd $3 > /dev/null
	cmd="jar xf ../$2/$1/dist/$1.jar"
	if [ "${verbose}" = 0 ]; then
		echo $cmd
	fi
	$cmd
	popd > /dev/null
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

	if [ "${do_dist_fnd}" = 0 ]; then
		echo '**** Cleaning dist.fnd ****'
		if [ "${verbose}" = 0 ]; then
			/bin/rm -rf build.fnd dist.fnd
		fi
		/bin/rm -rf build.fnd dist.fnd
	fi
	if [ "${do_dist_net}" = 0 ]; then
		echo '**** Cleaning dist.net ****'
		if [ "${verbose}" = 0 ]; then
			echo rm -rf build.net dist.net
		fi
		/bin/rm -rf build.net dist.net
	fi
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

	if [ "${do_dist_fnd}" = 0 ]; then
		echo '**** Building dist.fnd ****'
		/bin/mkdir -p dist.fnd
		/bin/mkdir -p build.fnd
		for f in `filter_modules modules.fnd/ALLMODULES '\[nodist\]'`; do
			expand_module $f modules.fnd build.fnd || exit 1;
		done;
		if [ "${verbose}" = 0 ]; then
			echo jar cf dist.fnd/infogrid-fnd.jar 'build.fnd/*'
		fi
		( cd build.fnd; jar cf ../dist.fnd/infogrid-fnd.jar * )
	fi
	if [ "${do_dist_net}" = 0 ]; then
		echo '**** Building dist.net ****'
		/bin/mkdir -p dist.net
		/bin/mkdir -p build.net
		for f in `filter_modules modules.net/ALLMODULES '\[nodist\]'`; do
			expand_module $f modules.net build.net || exit 1;
		done;
		if [ "${verbose}" = 0 ]; then
			echo jar cf dist.net/infogrid-net.jar 'build.net/*'
		fi
		( cd build.net; jar cf ../dist.net/infogrid-net.jar * )
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

echo '** DONE **'

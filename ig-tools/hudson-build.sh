#!/bin/sh -x
#
# Perform the automated build

trap stop 2
function stop {
        echo 'Interrupted by control-c. Exiting ...'
        exit 1;
}

PROMOTE=$1

CMD=
# CMD=echo

# Clean up last build
$CMD ig-tools/build.sh -c ig-config/build.infogrid.org.tomcat55.properties -clean

# Get new code
$CMD svn update .

# New build
$CMD ig-tools/build.sh -c ig-config/build.infogrid.org.tomcat55.properties -clean -build
code=$?

# If build worked, run tests
if [ $code = 0 ]; then
	$CMD ig-tools/build.sh -c ig-config/build.infogrid.org.tomcat55.properties -run
	code=$?
fi

# If build worked and tests passed, spill beans and attempt to promote
if [ $code = 0 ]; then
	$CMD ig-tools/spill-beans.sh

	# Attempt to promote
	if [ ! -z "${PROMOTE}" ]; then
		trunkVersion=`svnversion .`
		$CMD svn update ${PROMOTE}
		passLatestVersion=`svnversion ${PROMOTE}`
		passLatestMerged=`svn propget infogrid:last-merged ${PROMOTE}`
		trunkurl=`svn info . | awk '/^URL:/ { print $2 }'`
		mergeCommand="svn merge -r ${passLatestMerged}:${trunkVersion} ${trunkurl}"
		if [[ "${trunkVersion}" =~ ^[0-9]+$ ]]; then
			if [ ! -z "${passLatestMerged}" ]; then
				echo Found clean version ${trunkVersion}, promoting ...
				pushd "${PROMOTE}"
				$CMD ${mergeCommand}
				code=$?
				if [ $code = 0 ]; then
					$CMD svn propset infogrid:last-merged "${passLatestVersion}" .
					$CMD svn commit . -m "${mergeCommand}"
				else
					echo Merge failed, attempting to revert
					$CMD svn revert .
				fi
				popd
			else
				echo No property infogrid:last-merged found on ${PROMOTE}, cannot merge.
			fi
		else
			echo Version ${trunkVersion} not clean, not promoting. Command would have been ${mergeCommand}
		fi
	fi
fi

echo Exiting with code $code.
exit $code
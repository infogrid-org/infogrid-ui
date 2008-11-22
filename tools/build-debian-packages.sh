#!/bin/sh

maintainername="InfoGrid Build Master";
maintaineremail="buildmaster@infogrid.org";

vendors/svn.clonecloud.com/tools/make-package-java-war.pl \
	-wardir apps.fnd/org.infogrid.meshworld/dist \
	-builddir build.deb \
	-templatedir vendors/svn.clonecloud.com/templates/debian-module-java-war \
	-maintainername "${maintainername}" \
	-maintaineremail "${maintaineremail}" \
        org.infogrid.meshworld

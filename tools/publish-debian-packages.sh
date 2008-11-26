#!/bin/sh

vendors/svn.clonecloud.com/tools/publish-to-depot.pl \
	-builddir apps.fnd/org.infogrid.meshworld/dist \
	-confdir vendors/svn.clonecloud.com/conf/depot.clonecloud.com \
	-depotdir /home/depot/http/clonecloud \
        org.infogrid.meshworld

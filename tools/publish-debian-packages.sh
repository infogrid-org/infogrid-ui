#!/bin/sh

vendors/svn.clonecloud.com/tools/publish-to-depot.pl \
	-builddir build.deb \
	-confdir vendors/svn.clonecloud.com/conf/depot.clonecloud.com \
	-depotdir /home/depot/http/clonecloud \
        org.infogrid.meshworld

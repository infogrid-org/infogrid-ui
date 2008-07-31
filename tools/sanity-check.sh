#!/bin/sh
#
# Performs some sanity checks for common errors. Should be run prior to
# check-in

echo '** Checking that no funny paths exist **'
grep '\.\./\.\./\.\.' {modules*,apps*,tests*}/*/nbproject/project.properties

echo '** Checking that the Vendor is set right'
grep application.vendor {modules*,apps*,tests*}/*/nbproject/project.properties | grep -v InfoGrid.org

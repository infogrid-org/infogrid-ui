#!/bin/sh
# Replaces all copyright notices with the correct ones: Java, properties, XML files

export tempsuffix=".fix-notice";

echo "-- Fixing Java files --"

find modules.fnd modules.net apps.fnd apps.net tests.fnd tests.net -name '*.java' | {
    while read f
    do
        echo $f;
        /bin/mv $f $f$tempsuffix;
        perl tools/correct-java-notice.pl <"$f$tempsuffix" >$f
    done
}

echo "-- Fixing properties files --"

find modules.fnd modules.net apps.fnd apps.net tests.fnd tests.net -name '*.properties' | {
    while read f
    do
        echo $f;
        /bin/mv $f "$f$tempsuffix";
        perl tools/correct-properties-notice.pl <"$f$tempsuffix" >$f
    done
}

echo "-- Fixing XML files --"

find modules.fnd modules.net apps.fnd apps.net tests.fnd tests.net \( -name '*.xml' -or -name 'module.adv' \) | {
    while read f
    do
        echo $f;
        /bin/mv $f "$f$tempsuffix";
        perl tools/correct-xml-notice.pl <"$f$tempsuffix" >$f
    done
}

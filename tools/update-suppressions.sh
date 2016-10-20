#!/bin/bash

# This script generates suppressions files for PMD and Android Lint.
# The suppressions files list all rules that we violate.
#
# Before committing the result of running this script, make sure no
# new suppressions have been added. We want to get rid of them, not
# introduce more problems.
#
# Note that this script will do a build with all suppressions
# disabled. That will print a lot of error messages, but that's OK.

ROOTDIR=$(cd $(dirname "$0") ; cd .. ; pwd)
LINT_XML="${ROOTDIR}/config/quality/lint/lint.xml"
PMD_XML="${ROOTDIR}/config/quality/pmd/pmd-ruleset.xml"

# From: https://sipb.mit.edu/doc/safe-shell/
set -euf -o pipefail

function set_lint_suppressions() {
  cat > ${LINT_XML} << EOF
<?xml version="1.0" encoding="UTF-8"?>
<lint>
    <!-- FIXME: This file should be empty and all violations fixed. Then we will all hug. -->

EOF

  for RULE in $1 ; do
    echo "    <issue id=\"${RULE}\" severity=\"ignore\" />" >> ${LINT_XML}
  done

  echo '</lint>' >> ${LINT_XML}
}

function set_pmd_suppressions() {
  cat > ${PMD_XML} << EOF
<?xml version="1.0"?>
<ruleset xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" name="Bankdroid Rules"
    xmlns="http://pmd.sf.net/ruleset/1.0.0"
    xsi:noNamespaceSchemaLocation="http://pmd.sf.net/ruleset_xml_schema.xsd"
    xsi:schemaLocation="http://pmd.sf.net/ruleset/1.0.0 http://pmd.sf.net/ruleset_xml_schema.xsd">

    <description>Custom ruleset for Bankdroid, created by $(basename $0)</description>

    <exclude-pattern>.*/R.java</exclude-pattern>
    <exclude-pattern>.*/gen/.*</exclude-pattern>

    <rule ref="rulesets/internal/all-java.xml/TooManyStaticImports">
        <properties>
            <property name="legalPackages" type="String"
              description="Allow static imports for following packages."
              value="org.hamcrest.*|org.junit.*"/>
            <property name="xpath">
                <value><![CDATA[
                    .[count(ImportDeclaration[@Static='true' and
                    not(
                        matches(@PackageName, \$legalPackages)
                    )]) > \$maximumStaticImports]
                ]]></value>
            </property>
        </properties>
    </rule>

    <!-- FIXME: This file should be empty and all violations fixed. Then we will all hug. -->
    <rule ref="rulesets/internal/all-java.xml">

        <!-- This check needs extra configuration to work, disable it for now -->
        <exclude name="LoosePackageCoupling" />

EOF

  for RULE in $1; do
    echo "        <exclude name=\"${RULE}\" />" >> ${PMD_XML}
  done

  cat >> ${PMD_XML} << EOF
    </rule>
</ruleset>
EOF
}

set_lint_suppressions ""
set_pmd_suppressions ""

./gradlew clean check --continue || true

LINT_RESULTFILES=$(find ${ROOTDIR} -name 'lint-results*.xml')
LINT_VIOLATIONS=$(egrep -h ' *id=".*"$' ${LINT_RESULTFILES} | cut '-d"' -f2 | sort | uniq)
set_lint_suppressions "$LINT_VIOLATIONS"

PMD_RESULTFILES=$(find ${ROOTDIR} -name 'pmd.xml')
PMD_VIOLATIONS=$(grep externalInfoUrl= ${PMD_RESULTFILES} | sed 's/.*rule="//' | cut '-d"' -f1 | sort | uniq)
set_pmd_suppressions "$PMD_VIOLATIONS"

git diff ${ROOTDIR}/config

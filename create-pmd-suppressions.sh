#!/bin/bash

# Create a PMD suppressions file with a list of all failing
# PMD checks. Store in config/quality/pmd/pmd-ruleset.xml.

PMD_XML='config/quality/pmd/pmd-ruleset.xml'

# From: https://sipb.mit.edu/doc/safe-shell/
set -euf -o pipefail

# List failing PMD checks (with no suppressions)
cat > ${PMD_XML} << EOF
<?xml version="1.0"?>
<ruleset xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" name="All Java Rules"
    xmlns="http://pmd.sf.net/ruleset/1.0.0"
    xsi:noNamespaceSchemaLocation="http://pmd.sf.net/ruleset_xml_schema.xsd"
    xsi:schemaLocation="http://pmd.sf.net/ruleset/1.0.0 http://pmd.sf.net/ruleset_xml_schema.xsd">

    <description>All Java rules, $(basename $0) running...</description>

    <exclude-pattern>.*/R.java</exclude-pattern>
    <exclude-pattern>.*/gen/.*</exclude-pattern>

    <rule ref="rulesets/internal/all-java.xml" />
</ruleset>
EOF
./gradlew clean check --continue || true
RESULTSFILES=$(find . -name 'pmd.xml')

cat << EOF

Here's a PMD config file with suppressions for everything we're violating.

For great success, store in ${PMD_XML}.

Or just fix all issues and skip the suppressions file entirely.

<?xml version="1.0"?>
<ruleset xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" name="Bankdroid Rules"
    xmlns="http://pmd.sf.net/ruleset/1.0.0"
    xsi:noNamespaceSchemaLocation="http://pmd.sf.net/ruleset_xml_schema.xsd"
    xsi:schemaLocation="http://pmd.sf.net/ruleset/1.0.0 http://pmd.sf.net/ruleset_xml_schema.xsd">

    <description>Custom ruleset for Bankdroid, created by $(basename $0)</description>

    <exclude-pattern>.*/R.java</exclude-pattern>
    <exclude-pattern>.*/gen/.*</exclude-pattern>

    <!-- FIXME: This file should be empty and all violations fixed. Then we'll all hug. -->
    <rule ref="rulesets/internal/all-java.xml">
EOF

for RULE in $(grep externalInfoUrl= ${RESULTSFILES} | sed 's/.*rule="//' | cut '-d"' -f1 | sort | uniq) ; do
    echo "        <exclude name=\"${RULE}\" />"
done

cat << EOF
    </rule>
</ruleset>
EOF

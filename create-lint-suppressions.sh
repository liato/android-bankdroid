#!/bin/bash

# Create an Android Lint suppressions file with a list of all
# failing Lint checks. Store in config/quality/lint/lint.xml.

LINT_XML='config/quality/lint/lint.xml'

# From: https://sipb.mit.edu/doc/safe-shell/
set -euf -o pipefail

# List failing lint checks (with no suppressions)
echo '<lint></lint>' > ${LINT_XML}
./gradlew clean check --continue || true
RESULTSFILES=$(find . -name 'lint-results*.xml')

cat << EOF

Here's an Android Lint config file with suppressions for
everything we're violating.

For great success, store in ${LINT_XML}.

Or just fix all issues and skip the suppressions file entirely.

<?xml version="1.0" encoding="UTF-8"?>
<lint>
    <!-- FIXME: This file should be empty and all violations fixed. Then we'll all hug. -->

EOF

for RULE in $(egrep -h ' *id=".*"$' ${RESULTSFILES} | cut '-d"' -f2 | sort | uniq) ; do
    echo "    <issue id=\"${RULE}\" severity=\"ignore\" />"
done

cat << EOF
</lint>
EOF

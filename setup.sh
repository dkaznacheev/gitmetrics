#! /bin/bash

# Path to your IDEA executing script.
# Make sure your IDEA has MetricsReloaded plugin installed by running
# {IDEA_PATH} metrics -h
# Example: /home/dk/.local/share/JetBrains/Toolbox/apps/IDEA-C/ch-0/181.4203.550/bin/idea.sh
IDEA_PATH=

# Path to your IDEA config files.
# Example: /home/dk/.IdeaIC2018.1/config/
IDEA_CONFIG_PATH=


SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
mkdir -p ${IDEA_CONFIG_PATH}/metrics && touch ${IDEA_CONFIG_PATH}/metrics/customprofile.xml
cp ${SCRIPT_DIR}/customprofile.xml ${IDEA_CONFIG_PATH}/metrics/customprofile.xml
echo ${IDEA_PATH} > ${SCRIPT_DIR}/GitMetrics/src/main/resources/ideapath.conf

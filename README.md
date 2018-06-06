# GitMetrics
* Count metrics, print stats by committers in an interactive chart.

## Before building:
* Install MetricsReloaded plugin to your IDEA
* Set path to your IDEA executing script and config folder in `setup.sh`
* Run `setup.sh`

## Build
`gradle build`

## Usage
`gradle run -Dexec.args="[ARGS]"`, make sure IDEA is closed while the program runs.

Options:
* `-u` : path to git repository (without .git)
* `-p` : relative path to IDEA project inside the repository

Example: `gradle run -Dexec.args="-u /home/dk/labyrinth -p Labyrinth"`

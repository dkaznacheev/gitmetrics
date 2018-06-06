# GitMetrics
* Count metrics, print stats by committers in an interactive chart.

## Before building:
Change the contents of `GitMetrics/src/resources/ideapath.conf` to absolute path to your IDEA running script.

## Build
`gradle build`

## Usage
`gradle run -Dexec.args="[ARGS]"`, make sure IDEA is closed while the program runs.

Options:
* `-u` : path to git repository (without .git)
* `-p` : relative path to IDEA project inside the repository

Example: `gradle run -Dexec.args="-u /home/dk/labyrinth -p Labyrinth"`

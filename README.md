# GitMetrics
Test task for SPbAU practice: a CLI java application for gathering stats about the Git repository committers.
It is able to clone the repository, then:
* Count the lines of code in the repository with each commit and save it as a JPEG chart.
* Print the stats of committers: their name, number of commits, average number of added and deleted lines of code, and average time of commit.

## Build
`gradle build`

## Usage
Run like a regular Java CLI program with arguments:

`java Main -[OPTION]`

Options:
* `-q` : silent mode, do not write info about the committers
* `-u [URI]` : the repository URI, local repository if unspecified
* `-g` : save the chart

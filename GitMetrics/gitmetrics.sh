#!/bin/bash
(
cd ~/gitmetrics/GitMetrics; 
printf -v var "’%s’, " “$@”;

var=${var%??};

gradle run -PappArgs="[$var]"
)

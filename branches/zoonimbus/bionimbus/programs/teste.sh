#!/bin/sh

cd /home/parallels/Devel/UnB/apps/hadoop-1.0.2
bin/hadoop jar hadoop-examples-1.0.2.jar grep input output 'dfs[a-z.]+'


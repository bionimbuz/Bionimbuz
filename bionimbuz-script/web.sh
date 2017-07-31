#!/usr/bin/env bash
BIONIMBUZ_CLIENT='/home/biolabid1/BionimbuzClient'
APPLICATION_SERVER='/home/biolabid1/wildfly'

clear;
echo "
*******************************************************************************

                                   STARTING                                         
                    _____ _     _____ _       _       _____ 
                   | __  |_|___|   | |_|_____| |_ _ _|__   |
                   | __ -| | . | | | | |     | . | | |   __|
                   |_____|_|___|_|___|_|_|_|_|___|___|_____|

                                Web Application
                                         
*******************************************************************************
"
cd $BIONIMBUZ_CLIENT; 
mvn clean; 
mvn install;
cp $BIONIMBUZ_CLIENT/target/BionimbuzClient-0.0.1-SNAPSHOT.war $APPLICATION_SERVER/standalone/deployments/;
mv $APPLICATION_SERVER/standalone/deployments/BionimbuzClient-0.0.1-SNAPSHOT.war $APPLICATION_SERVER/standalone/deployments/BionimbuzClient.war;
cd $APPLICATION_SERVER/bin;
./standalone.sh --server-config=standalone-full-ha.xml


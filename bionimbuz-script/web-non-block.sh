#!/usr/bin/env bash
BIONIMBUZ_CLIENT='/home/biolabid1/BionimbuzClient/'
APPLICATION_SERVER='/opt/wildfly-10.0.0.Final/'
LOG='/opt/bionimbuz-script/log/'

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
cd $BIONIMBUZ_CLIENT_ROOT; 
mvn clean; 
mvn install;
cp $BIONIMBUZ_CLIENT_ROOT/target/BionimbuzClient-0.0.1-SNAPSHOT.war /home/zoonimbus/wildfly-10.0.0.Final/standalone/deployments/;
mv $APPLICATION_SERVER/standalone/deployments/BionimbuzClient-0.0.1-SNAPSHOT.war $APPLICATION_SERVER/standalone/deployments/BionimbuzClient.war;
cd $APPLICATION_SERVER/bin;
./standalone.sh --server-config=standalone-full-ha.xml > $LOG/stdout-web.txt 2> $LOG/stderr-web.txt &


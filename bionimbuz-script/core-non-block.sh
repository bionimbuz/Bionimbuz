clear;
echo "
*******************************************************************************

				   STARTING                                         
		    _____ _     _____ _       _       _____ 
  		   | __  |_|___|   | |_|_____| |_ _ _|__   |
		   | __ -| | . | | | | |     | . | | |   __|
		   |_____|_|___|_|___|_|_|_|_|___|___|_____|

		  	       Core Application
                                         
*******************************************************************************
"
echo '[INFO] ------------------------------------------------------------------------'
echo '[INFO] Initializing ZooKeeper'
cd /opt/zookeeper/bin/
sudo ./zkServer.sh start
echo '[INFO] ZooKeeper initialized'
echo '[INFO] ------------------------------------------------------------------------'
echo '[INFO] Initializing Non Blocking Execution'
cd /home/zoonimbus/script/;
./init-bionimbuz.sh > log/stdout-core.txt 2 > log/stderr-core.txt &
echo '[INFO] Running...'

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
cd /home/bionimbuz/Bionimbuz/;
mvn exec:java -Dexec.mainClass="br.unb.cic.bionimbuz.BioNimbuZ" -Dexec.args="-classpath %classpath br.unb.cic.bionimbuz.BioNimbuZ" -Dexec.executable="java"

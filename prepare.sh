#!/bin/bash


echo "#########################################"
echo "# Script de preparacao de ambiente"
echo "#########################################"


echo "# Configurando diretorio raiz do projeto"
echo "# ======================================="

ROOT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"


echo "# Preparacao de ambiente de desenvolvimento"
echo "# ======================================="

sudo add-apt-repository ppa:webupd8team/java -y
sudo apt-get update
sudo apt-get install -y \
    oracle-java8-installer \
    maven


echo "# Download de dependencias"
echo "# ======================================="

sudo apt-get install -y \
    mysql-server mysql-client \
    openssh-server \
    openssh-sftp-server \
    dstat \
    nmap \
    unzip \
    
    
echo "# Download de aplicacoes de bioinformatica"
echo "# ======================================="

sudo apt-get install -y \
    bowtie \
    bedtools \


echo "# Download e configuracao de ZooKeeper"
echo "# ======================================="

ZK_VERSION=3.4.10
ZK_FOLDER=zookeeper-${ZK_VERSION}
ZK_PACKAGE=${ZK_FOLDER}.tar.gz

echo "# Download de ZooKeeper"
wget http://ftp.unicamp.br/pub/apache/zookeeper/${ZK_FOLDER}/${ZK_PACKAGE}

echo "# Extraindo pacote"
tar -zxvf ${ZK_PACKAGE}

echo "# Remocao de ZooKeeper existente"
rm -rf ${ROOT_DIR}/system/zookeeper

echo "# Movendo para diretorio padrao"
mkdir -p ${ROOT_DIR}/system
mv ${ZK_FOLDER} ${ROOT_DIR}/system/zookeeper

echo "# Configuracoes iniciais"
cp ${ROOT_DIR}/system/zookeeper/conf/zoo_sample.cfg ${ROOT_DIR}/system/zookeeper/conf/zoo.cfg

echo "# Removendo pacote do ZooKeeper"
rm ${ZK_PACKAGE}


echo "# Build project"
echo "# ======================================="

mvn clean install




    

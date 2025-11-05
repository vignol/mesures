

architecture : M1 -> M2 -> M3
M1 est le client qui tourne firefox
M2 est la machine tomcat qui forwarde
M3 est le back end

3 images : large.jpg, small.jpg, tiny.jpg
une petite servlet
	accessible avec http://localhost:8080/serv/Serv?machine=xxxx&image=large.jpg
	si machine est null, elle lit image à partir du fichier local
	sinon, elle forwarde la requete à la machine xxxx

source comp.sh serv pour compiler la servlet

test content-length ok : 
curl -sS -D - -o /dev/null 'http://solo:8080/serv/Serv?image=large.jpg'
curl -sS -D - -o /dev/null 'http://vador:8080/serv/Serv?machine=solo&image=large.jpg'

Scenario :

- Machine M3 : lancer tomcat avec serv.war dans webapps
- Machine M2 : lancer tomcat avec serv.war dans webapps
- Machine M2 : lancer source cpu_logger.sh results.csv 1
- Machine M1 : wrk

-------------------

sur solo *6
source .bashrc ; ./mesures/apache-tomcat-11.0.1/bin/startup.sh

sur vador *7
source .bashrc ; ./mesures/apache-tomcat-11.0.1/bin/startup.sh
cd mesures ; source cpu_logger.sh res.txt 1

-------------------
si trop de connexions, timeout sur les connexions
si image trop grosse, timeout sur wrk
partir de 100 RPS et 50 connexions ... monter le RPS et si ca plafonne, monter les connexions


sur luke
wrk2/wrk -t10 -c300 -d60s -R1000 --timeout 10s --latency 'http://vador *7:8080/serv/Serv?machine=solo *6&image=large.jpg'
RPS=996 r/s
Transfer=97 MB/s
=> CPU=3%
20 core

grid5000 commands:

##following commands are done while inside my machine
#connection: 
ssh mteumou@access.grid5000.fr
ssh toulouse
#upload example:
scp apache-tomcat-11.0.1/webapps/serv/zero.jpg mteumou@access.grid5000.fr:toulouse/mesures/apache-tomcat-11.0.1/webapps/serv/zero.jpg
#download example:
scp mteumou@access.grid5000.fr:~/toulouse/mesures/res-tiny.csv ~/Downloads/

##following commands are done from distant machine grid5000
#Reserve nodes: 
oarsub -I -l nodes=3,walltime=1:00
#Get all reserved nodes:
uniq $OAR_NODEFILE
#max thread cpu logging on node:
htop res_large_htop.csv 1


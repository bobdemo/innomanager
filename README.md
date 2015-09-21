# Inno Manager Web Portal 

**Autore**: Roberto Demontis
**Email**: demontis@crs4.it
© CRS4, 2015

This repository contains the source code of the **"InnoManager"** Web Portal. It is based on the Entando Platform (http://www.entando.com/) and it has been developed for the **INNO** project (http://inno.crs4.it/innomanager). Through the web portal it is possible to read the metadata of the GIS layers stored in the INNO bucket of a Couchbase cluster (http://www.couchbase.com/). The portal allows to check the metadata of the GIS layer stored in the Inno database and to add new data from Esri shapefiles. The "resources" folder of this distribution also contains the tool "InnoImportShapeFile" that allows to import large size data (more than 100MB ) in a Linux environment using the System Shell. An example of the web app can be found at (http://inno.crs4.it/innomanager). The portal depends on PostgreSQL 9.3 or above, on PostGIS, on the cbdocloader tool of couchbase and it was tested on a Linux Ubuntu Server 12.04 in a Tomcat7 web container.

**Quick Start in a Linux Environment**
Note: is supposed that a cluster of couchbase composed by one or more nodes is up with a configured bucket named "Inno" and that the local server is able to connect to these nodes.

1. Download the innomanager source code from repository (e.g with clone)
2. Install Couchbase Server 2.2 Community Edition http://www.couchbase.com/download the couchbase server can be disabled (not working). If the path of cbdocloader differs to /opt/couchbase/bin/tools/cbdocloader you have to chnange the org.crs4.entando.innomanager.aps.system.InnoManagerSystemConstants.java java class 
3. Configure the filters in the src/main/filters folder 
4. Configure the psql.psw file in the main/config folder with the postgresql  connections parameters 
5. Create the innomanagerPort database on postgresql and load the schema and after the data using the innomanagerPort*.sql files in the main/db folder.
6. Create the innomanagerServ database on postgresql and load the schema and after the data using the innomanagerServ*.sql files in the main/db folder.
7. Change the couchbase connector configuration 'innoConnectorConfig' in the 'sysconfig' table of the innomanagerPort database :
UPDATE sysconfig SET  config = '<?xml version="1.0" encoding="UTF-8"?>
<innoConnectorConfig>
    <bucket name="nome_admin" password="password_admin" >
        <nodes>aserver1.adomain.it,aserver2.adomain.it,aserver3.adomain.it,aserver4.adomain.it</nodes> 
    </bucket>
</innoConnectorConfig>' WHERE item = 'innoConnectorConfig' ; 
8. Compile the project and deploy the WEB-APP (*.war) into Tomcat7

-------------------------------------------------------------------------------------

Questo repository contiene il codice sorgente e le risorse dell'applicazione web **"InnoManager"** sviluppata per il progetto **INNO** (http://inno.crs4.it/innomanager).

Innomanager è il portale web del progetto Inno. Tramite il portale, oltre alle informazioni sul progetto, è possibile verificare quali dati siano presenti nel database di Inno, basato su couchbase (http://www.couchbase.com/), caricare i tasselli di uno strato informativo GIS e aggiungerne di nuovi da Shapefile di medie dimensioni (minore di 80MB). La cartella "resources" contiene il tool "InnoImportShapeFile" che permette di caricare ed elaborare, dentro il database INNO, uno shapefile Esri di grandi dimensioni, compresso con zip, tramite uno script da eseguire nella shell di sistema di Linux. Un esempio dell'applicazione si può trovare all'indirizzo http://inno.crs4.it/innomanager. Il portale dipende da PostgreSQL versione 9.3 o superiore, PostGIS e dal tool di couchbase cbdocloader. E' stato testato in un ambiente Linux, Ubuntu Server 12.04, nel web container Tomcat7.

**Quick Start in un ambiente Linux***
Nota: si suppone sia già presente un cluster di couchbase, composto da uno o più nodi, che esista un bucket per il caricamento dei dati e che il server su cui viene installato il portale riesca a connettersi ai nodi del cluster.

1. Scaricare il codice sorgente di Innomanager dal repository
2. Installare Couchbase Server 2.2 Community Edition http://www.couchbase.com/download. Il server può essere disabilitato ma il tool cbdocloader deve essere presente
3. Configurare i file di configurazione nella cartella src/main/filters 
4. Configurare il file psql.psw in  src/main/config folder con i parametri di connessione a postgresql 
5. Modificare la classe org.crs4.entando.innomanager.aps.system.InnoManagerSystemConstants.java nel caso in cui la path di cbdocloader non sia "/opt/couchbase/bin/tools/cbdocloader"
6. creare il database innomanagerPort su postgresql, caricare lo schema e poi i dati usando il file innomanagerPort*.sql nella cartella  src/main/db
7. creare il database innomanagerServ su postgresql, caricare lo schema e poi i dati usando il file innomanagerServ*.sql nella cartella the src/main/db
8. modificare la configurazione della connessione con couchbase nella tabella "sysconfig" del database innomanagerPort:     UPDATE sysconfig SET config = '<?xml version="1.0" encoding="UTF-8"?> 
        <innoConnectorConfig>
            <bucket name="nome_admin" password="password_admin" >
            <nodes> aserver1.adomain.it,aserver2.adomain.it,aserver3.adomain.it,aserver4.adomain.it </nodes>
            </bucket>
        </innoConnectorConfig>' 
    WHERE item = 'innoConnectorConfig' ;
9. Compilare il progetto ed eseguire il deploy della WEB-APP (*.war) su Tomcat7

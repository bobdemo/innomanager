/*
*
* Copyright 2015 CRS4 (http://www.crs4.it) All rights reserved.
*
* This file is part of the Inno Data Management Portal based on 
* Entando Software  (http://www.entando.com).
* The  Inno Data Management Portal is a free software; 
* you can redistribute it and/or modify it
* under the terms of the GNU General Public License (GPL) 
* as published by the Free Software Foundation; version 2.
* 
* See the file License for the specific language governing permissions   
* and limitations under the License
* 
*
*/
package org.crs4.entando.innomanager.aps.system.services.layer;


import org.crs4.entando.innomanager.aps.system.services.layer.model.InnoLayer;
import org.crs4.entando.innomanager.aps.system.services.layer.model.WorkLayer;
import java.util.List;
import javax.ws.rs.core.Response;
import org.entando.entando.aps.system.services.api.IApiErrorCodes;
import org.entando.entando.aps.system.services.api.model.ApiException;
import com.agiletec.aps.system.ApsSystemUtils;
import com.agiletec.aps.system.SystemConstants;
import com.agiletec.aps.system.common.AbstractService;
import com.agiletec.aps.system.exception.ApsSystemException;
import com.agiletec.aps.system.services.baseconfig.ConfigInterface;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;
import org.crs4.entando.innomanager.aps.system.InnoManagerSystemConstants;
import org.crs4.entando.innomanager.aps.system.services.layer.model.Infos;
import org.crs4.entando.innomanager.aps.system.services.layer.model.Tile;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileReader;



/**
 * Servizio gestore degli oggetti WorkLayer.
 * @author R.Demontis
 */ 
public class LayerManager extends AbstractService implements ILayerManager {  
 
    @Override 
    public void init() throws Exception {
        ApsSystemUtils.getLogger().debug(this.getClass().getName() + ": initialized ");
    }
    
    /** 
     *  METODI PER LA GESTIONE DEGLI STRATI INFORMATIVI DI LAVORO 
     */
    
    @Override 
    public WorkLayer getWorkLayer(String name) throws ApsSystemException {
        WorkLayer layer = null;
        try {
            layer = this.getWorkLayerDAO().loadLayer(name);
        } catch (Throwable t) {
            ApsSystemUtils.logThrowable(t, this, "getLayer");
        }
        return layer;
    }
    
    @Override
    public void deleteWorkLayer(WorkLayerActionThread th) throws ApsSystemException {
        try {
              ApsSystemUtils.getLogger().info("Thread delete Start " );  
              getWorkLayerDAO().deleteLayer(th.getWorkLayerName());
              ApsSystemUtils.getLogger().info("Thread delete End " ); 
        } catch (Exception e) {
            ApsSystemUtils.getLogger().error("Thread delete ERROR " + e.getMessage());
        }
    }
    
    @Override 
    public WorkLayer createWorkLayer(String name, String description, String operator) throws ApsSystemException {
        WorkLayer layer = null;
        try {
            layer = this.getWorkLayerDAO().loadLayer(name.trim());
            if ( layer != null ) return null;
            if ( name == null || name.length() > 10 ) 
                 return null;
            // regexp!
            char ch;
            for ( int i=0; i <name.length();i++){
                ch = name.charAt(i);
                if ( !( (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9') ) )  
                     return null;
            }
            if ( name.equals("public") ) name = "_public_";
            layer = new WorkLayer();
            layer.setName(name.trim());
            layer.setDescription(description);
            layer.setOperator(operator);
            layer.setLastUpdate(new Date());
            layer.setLog("[" + (new Date()) + "] Created. ");
            layer.setStatus(InnoManagerSystemConstants.WORK_LAYER_STATUS_CREATE);
            this.getWorkLayerDAO().addLayer(layer);
        } catch (Throwable t) {
            ApsSystemUtils.logThrowable(t, this, "getLayer");
        }
        return layer;
    }
    
    @Override
    public void updateWorkLayer(WorkLayer layer) throws ApsSystemException {
        try {
            this.getWorkLayerDAO().updateLayer(layer);
        } catch (Throwable t) {
            ApsSystemUtils.logThrowable(t, this, "updateLayer");
            throw new ApsSystemException("Error updating layer", t);
        }
    }

    @Override
    public void deleteWorkLayer(String name) throws ApsSystemException {
        try {
            this.getWorkLayerDAO().deleteLayer(name);
        } catch (Throwable t) {
            ApsSystemUtils.logThrowable(t, this, "deleteLayer");
            throw new ApsSystemException("Error deleting layer by name " + name, t);
        }
    }
    
    @Override
    public List<WorkLayer> getWorkLayers() throws ApsSystemException {
         List<WorkLayer> layers = new ArrayList<WorkLayer> ();
         try {
            for (Iterator<WorkLayer> it = this.getWorkLayerDAO().loadLayers().iterator(); it.hasNext();) 
            {
                 WorkLayer layer = it.next();
                 if ( layer != null )
                     layers.add(layer);
            }
        } catch (Throwable t) {
            ApsSystemUtils.logThrowable(t, this, "getLayers");
            throw new ApsSystemException("Error searching layers", t);
        }
        return layers;
    }
    
    @Override
    public HashMap<String,String> getWorkLayerAttributes(String name) throws ApsSystemException {
         HashMap<String,String> attributes = null;
         try {
             attributes = this.getWorkLayerDAO().getWorkLayerAttributes(name);
         } catch (Throwable t) {
             ApsSystemUtils.logThrowable(t, this, "getWorkLayerAttributes");
             throw new ApsSystemException("Error loading layers", t);
         }
         return attributes;
    }
    
    @Override
    public List<WorkLayer> searchWorkLayers( String name, String operator )  throws ApsSystemException{
        List<WorkLayer> layers = new ArrayList<WorkLayer> ();
        try {
            for (Iterator<WorkLayer> it = this.getWorkLayerDAO().searchWorkLayers(name,operator).iterator(); it.hasNext();) 
            {
                 WorkLayer layer = it.next();
                 if ( layer != null )
                     layers.add(layer);
            }
        } catch (Throwable t) {
            ApsSystemUtils.logThrowable(t, this, "searchLayers");
            throw new ApsSystemException("Error searching layers", t);
        } 
        return layers;
    }
    
    
    /** 
     *  METODI PER LA GESTIONE DELLE ELABORAZIONI SUGLI STRATI INFORMATIVI DI LAVORO 
     */
    @Override
    public WorkLayer resetWorkLayerStatus( WorkLayer layer )  throws ApsSystemException{
        if ( layer == null ){
            ApsSystemUtils.getLogger().error("Work Layer Not Found");
        } 
        else {
            int status = layer.getStatus();
            // shapefile presenti nella cartella di lavoro
            boolean hasShapeFiles = !getShapeFileInfo(layer.getName()).isEmpty();
            // shapefile presenti nella cartella di lavoro
            Map<String,String> tables_properties = getTablesReport(layer.getName());
            // azione in esecuzione
            int action = InnoManagerSystemConstants.WORK_LAYER_ACTION_NONE;
            for (Iterator<WorkLayerActionThread> it = getThreadsWorking(layer.getName()).iterator(); it.hasNext();) {
                action = it.next().getAction();
            }
            switch (status){
                case InnoManagerSystemConstants.WORK_LAYER_STATUS_CREATE:
                    // no elaboration and no files
                    if ( hasShapeFiles )
                        removeShapeFiles(layer.getName());
                    if ( action != InnoManagerSystemConstants.WORK_LAYER_ACTION_NONE ) 
                        this.removeFromThreadWorking(layer.getName());
                    break;
                case InnoManagerSystemConstants.WORK_LAYER_STATUS_UPLOADED:
                    if ( !hasShapeFiles ){
                        layer.setStatus(InnoManagerSystemConstants.WORK_LAYER_STATUS_CREATE);
                        updateWorkLayer(layer);
                    }
                    if ( action != InnoManagerSystemConstants.WORK_LAYER_ACTION_NONE ) 
                        this.removeFromThreadWorking(layer.getName());
                    break;
                case InnoManagerSystemConstants.WORK_LAYER_STATUS_IMPORTED:
                    if ( action != InnoManagerSystemConstants.WORK_LAYER_ACTION_NONE ) 
                        this.removeFromThreadWorking(layer.getName());
                    if ( tables_properties.get(layer.getName() ) == null )
                    {  // verifica esistenza tabella 
                        if ( hasShapeFiles )
                            layer.setStatus(InnoManagerSystemConstants.WORK_LAYER_STATUS_UPLOADED);
                        else
                            layer.setStatus(InnoManagerSystemConstants.WORK_LAYER_STATUS_CREATE);
                        updateWorkLayer(layer);
                    }
                    break;
                case InnoManagerSystemConstants.WORK_LAYER_STATUS_READY:
                    if ( action != InnoManagerSystemConstants.WORK_LAYER_ACTION_NONE ) 
                        this.removeFromThreadWorking(layer.getName());
                    if ( tables_properties.get(layer.getName()) == null ){
                        if ( hasShapeFiles ) layer.setStatus(InnoManagerSystemConstants.WORK_LAYER_STATUS_UPLOADED);
                        else layer.setStatus(InnoManagerSystemConstants.WORK_LAYER_STATUS_CREATE);
                        updateWorkLayer(layer);
                    } 
                    break;
                case InnoManagerSystemConstants.WORK_LAYER_STATUS_COMPLETED:
                    if ( action != InnoManagerSystemConstants.WORK_LAYER_ACTION_NONE ) 
                        this.removeFromThreadWorking(layer.getName());
                    break;
                case InnoManagerSystemConstants.WORK_LAYER_STATUS_DELETING:
                    if ( action != InnoManagerSystemConstants.WORK_LAYER_ACTION_DELETE ){
                        this.removeFromThreadWorking(layer.getName());
                    }
                    // DISABLED
                    break;
                case InnoManagerSystemConstants.WORK_LAYER_WRONG_STATUS:
                    return null;
            }
        } 
        return layer;
    }
    
    @Override
    public void importWorkLayer(WorkLayerActionThread th) throws ApsSystemException {
        try {
              ApsSystemUtils.getLogger().info("Thread import Start " );
              WorkLayer layer = getWorkLayer(th.getWorkLayerName());
              ApsSystemUtils.getLogger().info(layer.getStatusText() );
              getWorkLayerDAO().prepareWorkLayerArea(layer); 
              if ( shp2pgsql( th.getWorkLayerName() ) ) {
                ApsSystemUtils.getLogger().info("Thread import End " ); 
                layer.setStatus(InnoManagerSystemConstants.WORK_LAYER_STATUS_IMPORTED);
                layer.setLog("[" + (new Date()).toString()+ "] Thread import Succeded \n" + layer.getLog());
              } 
              else {
                ApsSystemUtils.getLogger().info("Thread import End with error " ); 
                layer.setStatus(InnoManagerSystemConstants.WORK_LAYER_STATUS_CREATE);
                layer.setLog("[" + (new Date()).toString()+ "] Thread import end with error, reset data \n" + layer.getLog());
              } 
              updateWorkLayer(layer);
        } catch (Exception e) {
            ApsSystemUtils.getLogger().error("Thread import ERROR " + e.getMessage());
        }
    }
    
    @Override
    public void elaborateWorkLayer(WorkLayerActionThread th) throws ApsSystemException {
        try {
              String name = th.getWorkLayerName();
              WorkLayer layer = getWorkLayer(name);
              ApsSystemUtils.getLogger().info("Thread elaborate started ");
              Map<String,String> tables = getTablesReport(name);
              // verifica presenza tabella principale
              if ( tables.get(name) != null ) {
                   layer.setLog("[" + (new Date()).toString()+ "] Thread elaborate: " + name + " start for table " + name + "." + name + " with  " + tables.get(name) +  " rows\n" + layer.getLog());
                   layer.setLog("[" + (new Date()).toString()+ "] Thread elaborate: Creating priority table  \n" + layer.getLog());
                   updateWorkLayer(layer);
                   addPriority(layer.getName());
              }
              else {
                   layer.setLog("[" + (new Date()).toString()+ "] Thread elaborate: Error, table " + name + "." + name + " not exist \n" + layer.getLog());
                   layer.setStatus(InnoManagerSystemConstants.WORK_LAYER_STATUS_IMPORTED);
                   updateWorkLayer(layer);
                   return;
              }
              // verifica presenza tabella ordinata 
              tables = getTablesReport(name);
              if ( tables.get(name+"_ord") != null ) {
                   layer.setLog("[" + (new Date()).toString()+ "] Thread elaborate: New table: " + name + "." + name + "_ord with  " + tables.get(name+"_ord") +  " rows added\n" + layer.getLog());
                   layer.setLog("[" + (new Date()).toString()+ "] Thread elaborate: Creating attribute's table..... \n" + layer.getLog());
                   updateWorkLayer(layer);
                   generateInfo(layer.getName());
              }
              else {
                   layer.setLog("[" + (new Date()).toString()+ "] Thread elaborate: Error, table " + name + "." + name + "_ord not exist \n" + layer.getLog());
                   layer.setStatus(InnoManagerSystemConstants.WORK_LAYER_STATUS_IMPORTED);
                   updateWorkLayer(layer);
                   return;
              }
              // verifica presenza tabella info
              tables = getTablesReport(name);
              if ( tables.get(name+"_info") != null ) {
                   layer.setLog("[" + (new Date()).toString()+ "] Thread elaborate: New table: " + name + "." + name + "_info with  " + tables.get(name+"_info") +  " rows added\n" + layer.getLog());
                   updateWorkLayer(layer);
              }
              else {
                   layer.setLog("[" + (new Date()).toString()+ "] Thread elaborate: Error,  table: " + name + "." + name + "_info not created \n" + layer.getLog());
                   layer.setStatus(InnoManagerSystemConstants.WORK_LAYER_STATUS_IMPORTED);
                   updateWorkLayer(layer);
                   return;
              }
              for ( int zoom = 10; zoom < 18; zoom ++) {
                    layer.setLog("[" + (new Date()).toString()+ "] Thread elaborate: Init generation tiles for zoom " + 
                                          zoom + "..... \n" + layer.getLog());
                    generateTiles(layer.getName(),zoom);
                    layer.setLog("[" + (new Date()).toString()+ "] Thread elaborate: Tile for zoom " + 
                                          zoom + " extracted \n" + layer.getLog());
                    updateWorkLayer(layer);
              }
              // verifica presenza tiles 
              tables = getTablesReport(name);
              String table;
              layer.setStatus(InnoManagerSystemConstants.WORK_LAYER_STATUS_READY);
              layer.setLog("[" + (new Date()).toString()+ "] Thread elaborate: tables ready; \n" + layer.getLog());
              if ( tables.size() < 19) {
                   layer.setLog("[" + (new Date()).toString()+ "] Warning: some table not created \n" + layer.getLog());
              }
              layer.setLog("[" + (new Date()).toString()+ "] Report: \n" + layer.getLog());
              for ( Iterator iter = tables.keySet().iterator(); iter.hasNext(); )  {
                    table = (String)iter.next();
                    layer.setLog(table + " : " + tables.get(table)+ "rows \n" + layer.getLog());
              }
              updateWorkLayer(layer);
              ApsSystemUtils.getLogger().info("Thread elaborate End "); 
        } catch (Exception e) {
            ApsSystemUtils.getLogger().error("Thread elaborate ERROR " + e.getMessage());
        }
    }
    
    @Override
    public void generateTiles(String name, int zoom) throws ApsSystemException {
    try {
            StringBuilder buffer = new StringBuilder();
            buffer.append(this.getConfigManager().getParam(SystemConstants.PAR_RESOURCES_DISK_ROOT));
            if ( !buffer.toString().endsWith(File.separator)) 
                  buffer.append(File.separator);
            String path = getShapeFileDiskFolder(name);
            File passfile = new File(buffer.toString() + "../META-INF/psql.psw");
            // il database di lavoro è preso dalla configurazione jdbc/servDataSource
            String passpath = passfile.getAbsolutePath();Properties properties = getWorkLayerDAO().getConnectionParameters();
            String database = properties.getProperty("Url");
            String user = properties.getProperty("User");
            ApsSystemUtils.getLogger().info( user + " --- " + database );
            database = database.substring( database.lastIndexOf("/") + 1 );
            String comand = "SELECT " + IWorkLayerDAO.CREATE_TILE + " ( '" + name + "'," + zoom + " )";
            String cmdline = "export PGPASSFILE="+passpath+";  psql -c \"" + comand + "\" -d " + database + " -h localhost -U " + user + " -w ";  
            ApsSystemUtils.getLogger().info("esecuzione: " + cmdline);
            Process  process = 
                new ProcessBuilder(new String[] {"bash", "-c", cmdline})
                   .redirectErrorStream(true)
                   .start();
            BufferedReader br = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            String line = null;
            while ( (line = br.readLine()) != null ){
//TO DO gestione log
            }    

        } catch (Exception e) {
            ApsSystemUtils.getLogger().error("Thread elaborate ERROR " + e.getMessage());
        }
    }
    
    @Override
    public void addPriority(String name) throws ApsSystemException {
    try {
            StringBuilder buffer = new StringBuilder();
            buffer.append(this.getConfigManager().getParam(SystemConstants.PAR_RESOURCES_DISK_ROOT));
            if ( !buffer.toString().endsWith(File.separator)) 
                  buffer.append(File.separator);
            String path = getShapeFileDiskFolder(name);
            File passfile = new File(buffer.toString() + "../META-INF/psql.psw");
            String passpath = passfile.getAbsolutePath();Properties properties = getWorkLayerDAO().getConnectionParameters();
            String database = properties.getProperty("Url");
            String user = properties.getProperty("User");
            ApsSystemUtils.getLogger().info( user + " --- " + database );
            database = database.substring( database.lastIndexOf("/") + 1 );
            String comand = "SELECT " + IWorkLayerDAO.PREPARE_LAYER + " ( '" + name + "' )";
            String cmdline = "export PGPASSFILE="+passpath+";  psql -c \"" + comand + "\" -d " + database + " -h localhost -U " + user + " -w ";  
            ApsSystemUtils.getLogger().info("esecuzione: " + cmdline);
            Process  process = 
                new ProcessBuilder(new String[] {"bash", "-c", cmdline})
                   .redirectErrorStream(true)
                   .start();
            BufferedReader br = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            String line = null;
            while ( (line = br.readLine()) != null ){
//TO DO gestione log
            }    

        } catch (Exception e) {
            ApsSystemUtils.getLogger().error("Thread elaborate ERROR " + e.getMessage());
        }
    }
    
    @Override
    public void generateInfo(String name) throws ApsSystemException {
    try {
            StringBuilder buffer = new StringBuilder();
            buffer.append(this.getConfigManager().getParam(SystemConstants.PAR_RESOURCES_DISK_ROOT));
            if ( !buffer.toString().endsWith(File.separator)) 
                  buffer.append(File.separator);
            String path = getShapeFileDiskFolder(name);
            File passfile = new File(buffer.toString() + "../META-INF/psql.psw");
            String passpath = passfile.getAbsolutePath();Properties properties = getWorkLayerDAO().getConnectionParameters();
            String database = properties.getProperty("Url");
            String user = properties.getProperty("User");
            ApsSystemUtils.getLogger().info( user + " --- " + database );
            database = database.substring( database.lastIndexOf("/") + 1 );
            String comand = "SELECT " + IWorkLayerDAO.CREATE_INFOS + " ( '" + name + "' )";
            String cmdline = "export PGPASSFILE="+passpath+";  psql -c \"" + comand + "\" -d " + database + " -h localhost -U " + user + " -w ";  
            ApsSystemUtils.getLogger().info("esecuzione: " + cmdline);
            Process  process = 
                new ProcessBuilder(new String[] {"bash", "-c", cmdline})
                   .redirectErrorStream(true)
                   .start();
            BufferedReader br = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            String line = null;
            while ( (line = br.readLine()) != null ){
//TO DO gestione log
            }    

        } catch (Exception e) {
            ApsSystemUtils.getLogger().error("Thread elaborate ERROR " + e.getMessage());
        }
    }
    
    @Override
    public boolean writeToDisk(String name) throws ApsSystemException {
    try {
            WorkLayer layer = getWorkLayer(name);
            layer.setLog("[" + (new Date()).toString()+ "] Thread export: writing documents on disk...; \n" + layer.getLog());
            updateWorkLayer(layer);
            StringBuilder buffer = new StringBuilder();
            buffer.append(this.getConfigManager().getParam(SystemConstants.PAR_RESOURCES_DISK_ROOT));
            if ( !buffer.toString().endsWith(File.separator)) 
                  buffer.append(File.separator);
            String path = InnoManagerSystemConstants.WORK_LAYER_EXPORT_PATH;
            File passfile = new File(buffer.toString() + "../META-INF/psql.psw");
            String passpath = passfile.getAbsolutePath();
            Properties properties = getWorkLayerDAO().getConnectionParameters();
            String database = properties.getProperty("Url");
            String user = properties.getProperty("User");
            database = database.substring( database.lastIndexOf("/") + 1 );
            String comand = "SELECT " + IWorkLayerDAO.WRITE_TO_DISK + " ( '" + name + "'" + ", '" + path + "' )";
            String cmdline = "export PGPASSFILE="+passpath+";chmod -R 777 " + path + File.separator + name + "; psql -c \"" + comand + "\" -d " + database + " -h localhost -U " + user + " -w ";  
            ApsSystemUtils.getLogger().info("esecuzione: " + cmdline);
            Process  process = 
                new ProcessBuilder(new String[] {"bash", "-c", cmdline})
                   .redirectErrorStream(true)
                   .start();
            BufferedReader br = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            String line = null;
            while ( (line = br.readLine()) != null ){
//TO DO gestione log
            }
            layer.setLog("[" + (new Date()).toString()+ "] Thread export:  JSON documents wrote on disk; \n" + layer.getLog());
            layer.setLog("[" + (new Date()).toString()+ "] Thread export:  creating spatial index.....; \n" + layer.getLog());
            updateWorkLayer(layer);
            // It generates the json document to manage the spatial query with no element
            // Genera il documento json per la gestione delle query spaziali senza risultato
            String doc_false = "{ \"id\": \"" + name + ":false\",\"page\": 1,\"pages\": 1,'";
            for ( int zoom = 10; zoom < 18; zoom++ ) { 
                  doc_false = doc_false + "\"_"+name+"macro"+zoom+"bbox_\": {\"type\": \"Polygon\",\"coordinates\":" 
                           +"[[[-180,-90],[-180,90],[180,90],[180,-90],[-180,90]]]},";
            }
            doc_false += "\"_"+name+"bbox_\": {\"type\": \"Polygon\",\"coordinates\": [[[-180,-90],[-180,90],[180,90],[180,-90],[-180,90]]]}}";
            File doc_false_file = new File(path+File.separator+layer.getName()+File.separator+layer.getName()+File.separator+"docs"+File.separator+name+":false");
            FileUtils.writeStringToFile(doc_false_file, doc_false);
            // It creates the json document with spatial view definition
            // Crea i documenti json con le definizioni delle viste
            String doc_views = "{ \"_id\" : \"_design/" + name + "views\", \"language\": \"javascript\",\"spatial\": {";
            for ( int zoom = 10; zoom < 18; zoom++ ) { 
                  doc_views +=  "\"bymacrobbox"+zoom+"\": \"function(doc) { if (doc._"+name+"macro"+zoom+"bbox_) emit(doc._"+name+"macro"+zoom+"bbox_, null); }\",";
            }
            doc_views +=  "\"bybbox\": \"function(doc) { if (doc._"+name+"bbox_) emit(doc._"+name+"bbox_, null); }\"},";
            doc_views +=  "\"views\": { ";
            doc_views +=  "\"forInfoDelete\":  { \"map\": \"function(doc) { if (doc._"+name+"bbox_) emit(doc._"+name+"bbox_,null); }\"},";
            doc_views +=  "\"forTileDelete\":  { \"map\": \"function(doc) { if (doc.id && doc.id.startsWith('"+name+"')) emit(doc.id, null); }\"}}}";
            File doc_views_file = new File(path+File.separator+layer.getName()+File.separator+layer.getName()+File.separator+"design_docs"+File.separator+name+"views.json");
            FileUtils.writeStringToFile(doc_views_file, doc_views);
        
            layer.setLog("[" + (new Date()).toString()+ "] Thread export:  JSON documents wrote on disk; \n" + layer.getLog());
            updateWorkLayer(layer);
            return true;
    } catch (Exception e) {
        ApsSystemUtils.getLogger().error("Thread Export WriteToDisk ERROR " + e.getMessage());
    }
        return false;
    }
    
    
    @Override
    public void exportWorkLayer(WorkLayerActionThread th) throws ApsSystemException { 
         try {
              WorkLayer layer = getWorkLayer(th.getWorkLayerName());
              if ( layer == null ) 
                  return;
              String path = InnoManagerSystemConstants.WORK_LAYER_EXPORT_PATH;
              File dir = new File(path);
              if ( !dir.exists() ){ 
                  FileUtils.forceMkdir(dir);
              }
              path += File.separator+layer.getName();
              dir = new File(path);
              if ( dir.exists() ){ 
                  FileUtils.forceDelete(dir);
              }
              FileUtils.forceMkdir(new File(path));
              FileUtils.forceMkdir(new File(path+File.separator+layer.getName()+File.separator+"docs"));
              FileUtils.forceMkdir(new File(path+File.separator+layer.getName()+File.separator+"design_docs"));
              for ( int i = 10; i< 18; i++ ) { 
                    FileUtils.forceMkdir(new File(path+File.separator+layer.getName()+i));
                    FileUtils.forceMkdir(new File(path+File.separator+layer.getName()+i+File.separator+"design_docs"));
                    FileUtils.forceMkdir(new File(path+File.separator+layer.getName()+i+File.separator+"docs"));
              }
              ApsSystemUtils.getLogger().info("Thread export documents " + layer.getName() + " Start "); 
              if ( !writeToDisk(layer.getName()) ){ 
                  ApsSystemUtils.getLogger().info("Thread export: documents " + layer.getName() + " Error "); 
                  layer.setLog("[" + (new Date()).toString()+ "] Thread export:  Errori nella scrittura dei documenti JSON; \n" + layer.getLog());
                  layer.setStatus(InnoManagerSystemConstants.WORK_LAYER_STATUS_READY);
              }
              else if ( !compressData(layer.getName()) ){ 
                  ApsSystemUtils.getLogger().info("Thread export: error in documents compression for " + layer.getName()); 
                  layer.setLog("[" + (new Date()).toString()+ "] Thread export:  Errori nella compressione dei documenti JSON; \n" + layer.getLog());
                  layer.setStatus(InnoManagerSystemConstants.WORK_LAYER_STATUS_READY);
              }
              else if ( !cbdocloader(layer.getName()) ){ 
                  ApsSystemUtils.getLogger().info("Thread export " + layer.getName() + " Error "); 
                  layer.setLog("[" + (new Date()).toString()+ "] Thread export:  Errori nella scrittura su couchbase tramite cbdocloader ; \n" + layer.getLog());
                  layer.setStatus(InnoManagerSystemConstants.WORK_LAYER_STATUS_READY);
              }
              else { 
                  layer.setStatus(InnoManagerSystemConstants.WORK_LAYER_STATUS_COMPLETED);
                  layer.setLog("[" + (new Date()).toString()+ "] Thread export Succeded \n" + layer.getLog());
              }
              updateWorkLayer(layer);
        } catch (Exception e) {
            ApsSystemUtils.getLogger().error("Thread export ERROR " + e.getMessage());
        }
    }
    
    
    @Override  
    public Map<String,String>  getTablesReport (String layername) throws ApsSystemException{
            return getWorkLayerDAO().getTablesReport(layername); 
    }
    
    @Override
    public void verifyFile ( WorkLayer layer ) throws ApsSystemException{
        try {
           StringBuilder buffer = new StringBuilder();
           buffer.append(this.getConfigManager().getParam(SystemConstants.PAR_RESOURCES_DISK_ROOT));
           if ( !buffer.toString().endsWith(File.separator)) 
                 buffer.append(File.separator);
           // verifica solo la presenza di shapefile
           String[] ext = {"shp"}; 
           String path = getShapeFileDiskFolder(layer.getName());
           if ( layer.getStatus() == InnoManagerSystemConstants.WORK_LAYER_STATUS_UPLOADED 
                && FileUtils.listFiles(new File(path), ext, false).isEmpty() ){
                   layer.setStatus(InnoManagerSystemConstants.WORK_LAYER_STATUS_CREATE );
           }
           updateWorkLayer(layer);
        } catch (Exception e) {
           
           
        }
    }
      
    /**
     * Esegue l'import del dato gis da shapefile verso il database 
     * @param layername
     * @return 
     */
    public boolean shp2pgsql(String layername) {
        String head = "", tail = "";
        try {
           StringBuilder buffer = new StringBuilder();
           buffer.append(this.getConfigManager().getParam(SystemConstants.PAR_RESOURCES_DISK_ROOT));
           if ( !buffer.toString().endsWith(File.separator)) 
                 buffer.append(File.separator);
            
           String path = getShapeFileDiskFolder(layername);
           File passfile = new File(buffer.toString() + "../META-INF/psql.psw");
           String passpath = passfile.getAbsolutePath();
           File dir = new File (path);
           String[] ext = {"shp"};
           String options =  " -s 4326 -g geom  ";
           String filePath = "";
           Process process;
           String cmdline;
           boolean add_opt = true;
           int headrow = 4, tailrow = 4;
           Properties properties = getWorkLayerDAO().getConnectionParameters();
           String database = properties.getProperty("Url");
           String user = properties.getProperty("User");
           ApsSystemUtils.getLogger().info( user + " --- " + database );
           database = database.substring( database.lastIndexOf("/") + 1 );
           if ( dir.exists() ) {
               for (Iterator<File> it = FileUtils.listFiles(dir, ext, false).iterator(); it.hasNext();) {
                    filePath = it.next().getAbsolutePath();
                    cmdline = "export PGPASSFILE="+passpath+"; chmod 600 " + passpath + ";shp2pgsql " + options + filePath + " " + layername + "." + layername + " | psql -d " + database + " -h localhost -U " + user + " -w ";  
                    ApsSystemUtils.getLogger().info(cmdline);
                    process = 
                        new ProcessBuilder(new String[] {"bash", "-c", cmdline})
                           .redirectErrorStream(true)
                           .start();
                    BufferedReader br = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                    String line = null;
                    while ( (line = br.readLine()) != null ){
                              if (headrow > 0 ){
                                  ApsSystemUtils.getLogger().info( line );
                                  headrow--;
                              }
                              else {
                                  if (tailrow == 4 ){
                                      ApsSystemUtils.getLogger().info( "...");
                                  }
                                  ApsSystemUtils.getLogger().info( line );
                                  tailrow--;
                              }
                    }    
                    if ( add_opt ) {
                        options += " -a ";
                        add_opt = false;
                    } 
               }
           }
           if ( this.getTablesReport(layername).size() > 0 )
               return true;
        } catch (Exception e) {
           ApsSystemUtils.getLogger().error("Errors in shp2psql " + layername );
        }
        return false;
    }
   
    /**
     * Esegue l'export del dato gis dal database verso couchbase 
     * @param layername
     * @return 
     */
    public boolean cbdocloader( String layername ) {
        try {
            WorkLayer layer = getWorkLayer(layername);
            layer.setLog("[" + (new Date()).toString()+ "] Thread export: start to write on couchbase; \n" + layer.getLog());
            updateWorkLayer(layer);
            Properties configuration = getCouchbaseDAO().getInnoConnectorConfig();
            Process process ;
            String cmdline = configuration.getProperty(InnoManagerSystemConstants.CBDOCLOADER_PATH);
            cmdline += " -u " + configuration.getProperty(InnoManagerSystemConstants.CONFIG_ATTRIBUTE_NAME);
            cmdline += " -p " + configuration.getProperty(InnoManagerSystemConstants.CONFIG_ATTRIBUTE_PASSWORD);
            cmdline += " -n " + configuration.getProperty(InnoManagerSystemConstants.CONFIG_TAG_NODES).split(",")[0];
            cmdline += " -b inno -s 100 " + configuration.getProperty(InnoManagerSystemConstants.CONFIG_TAG_NODES).split(",")[0];
            String nomefile = "info"+layername;
            String pathfile;
            File file;
            for ( int i = 10 ; i < 18; i++ ) {
                  pathfile =  InnoManagerSystemConstants.WORK_LAYER_EXPORT_PATH + File.separator + layername + File.separator + nomefile + i + ".zip";
                  file = new File(pathfile);
                  if ( file.exists() ){
                      process = new ProcessBuilder(new String[] {"bash", "-c", cmdline + " " + pathfile})
                            .redirectErrorStream(true)
                            .start();
                  BufferedReader br = new BufferedReader(
                      new InputStreamReader(process.getInputStream()));
                  int headrow = 4, tailrow = 4;
                  String line = null;
                  while ( (line = br.readLine()) != null ){
                            if (headrow > 0 ){
                                ApsSystemUtils.getLogger().info( line );
                                headrow--;
                            }
                            else {
                                if (tailrow == 4 ){
                                    ApsSystemUtils.getLogger().info( "..." );
                                }
                                ApsSystemUtils.getLogger().info( line );
                                tailrow--;
                            }
                        }    
                  
                  }
            }      
            layer.setLog("[" + (new Date()).toString()+ "] Thread export: documents wrote in couchbase; \n" + layer.getLog());
            updateWorkLayer(layer);
            
            return true;
        } catch (Exception e) {
            //Warning: doing this is no good in high quality applications.
            //Instead, present appropriate error messages to the user.
            //But it's perfectly fine for prototyping.
            e.printStackTrace();
            //!! gestire errore
            
        }
        return false;
    }        
    
    /**
     * Esegue la compressione dei documenti JSON scritti su disco tramite l'utility zip 
     * @param layername
     * @return true if the documents was compressed, false otherwise.
     */
    public boolean compressData( String layername ) {
        try {
            Process process ;
            if (layername != null) {
                String path = InnoManagerSystemConstants.WORK_LAYER_EXPORT_PATH+File.separator+layername;
                String cmdline = "cd " + path + "; zip -qr " + layername + ".zip" + " " + layername;  
                process = new ProcessBuilder(new String[] {"bash", "-c", cmdline })
                          .redirectErrorStream(true)
                          .start();
                BufferedReader br = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
                String line = null;
                while ( (line = br.readLine()) != null ){
                }    
                for ( int i = 10 ; i < 18; i++ ) {
                    cmdline = "cd " + path + "; zip -qr " + layername + i + ".zip" + " " + layername + i ;  
                    process = new ProcessBuilder(new String[] {"bash", "-c", cmdline })
                              .redirectErrorStream(true)
                              .start();
                    br = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                    while ( (line = br.readLine()) != null ){
                    }    
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            //!! gestire errore
            
        }
        return false;
    }
    
    /**
     * restituisce i thread che lavorano sul WorkLayer
     * @param name il nome del WorkLayer
     * @return 
     */
    protected List<WorkLayerActionThread> getThreadsWorking(String name) {
         ArrayList<WorkLayerActionThread> threads = new ArrayList<WorkLayerActionThread>();
         WorkLayerActionThread th;
         for (Iterator<WorkLayerActionThread> it = getListThreadWorking().iterator(); it.hasNext();) {
              th = it.next();
              if ( th.getWorkLayerName().equals(name) ) threads.add(th);
         }
         return threads;
    }
    
    
    
    /**
     * rimuove un thread 
     * @param th il thread da rimuovere 
     * @param forced esecuzione forzata
     */
    protected void removeFromThreadWorking(WorkLayerActionThread th, boolean forced) {
        if ( ! getListThreadWorking().contains(th))
             ApsSystemUtils.getLogger().error("INNO WORK LAYER MANAGER: try to remove not present working thread for layer  " +   th.getWorkLayerName());
        getListThreadWorking().remove(th);
        if (forced) th.interrupt(); /// possible wrong db status
    }
    
    /**
     * rimuove tutti i thread al lavoro su di un WorkLayer
     * @param th il thread da rimuovere 
     * @param forced esecuzione forzata
     */
    protected void removeFromThreadWorking(String name) {
        WorkLayerActionThread th = null;
        for (Iterator<WorkLayerActionThread> it = _listThreadWorking.iterator(); it.hasNext();) {
            th = it.next();
            if ( th.getWorkLayerName().equals(name) ) {
                getListThreadWorking().remove(th);
                th.interrupt(); /// possible wrong db status
            }    
        }
    }
    
    /**
     * aggiunge un thread 
     * @param th il thread da rimuovere 
     * @param forced esecuzione forzata
     */
    protected boolean addToThreadWorking(WorkLayerActionThread th, boolean forced) {
        if ( getListThreadWorking().contains(th)){
             ApsSystemUtils.getLogger().error("INNO WORK LAYER MANAGER: try to add present working thread for layer  " +   th.getWorkLayerName());
             return true;
        }
        if ( !forced && !getThreadsWorking(th.getWorkLayerName()).isEmpty() ){
             ApsSystemUtils.getLogger().error("INNO WORK LAYER MANAGER: try to add a wrong working thread for layer  " +   th.getWorkLayerName());
             return false;
        }
        if ( forced && !getThreadsWorking(th.getWorkLayerName()).isEmpty() ){
             removeFromThreadWorking(th.getWorkLayerName());
        }
        getListThreadWorking().add(th);
        return true;
    }
    
    /**
     * Restituisce la lista worklayer su cui si esegue una elaborazione
     * @return la lista worklayer su cui si esegue una elaborazione
     */
    protected List<WorkLayerActionThread> getListThreadWorking() {
        if ( _listThreadWorking == null ) {
            _listThreadWorking = new ArrayList<WorkLayerActionThread>();
        }    
        return _listThreadWorking;
    }
    
    /**
     * Gestisce un eventuale errore di sistema nel thread di elaborazione dati
     * di un work layer.
     * @param action l'azione che il thread stava eseguendo
     * @param message Il messaggio di errore.
     * @param name Il layer di lavoro su cui si esegue l'elaborazone.
     */
    public void manageThreadError(String name, String message, int action ) {
        ApsSystemUtils.getLogger().error("Errors in thread " + name + " - " + action + "_" + message );
    }
    
    /**
     * Verifica se l'azione può essere eseguita quindi instanzia il thread 
     * delegato ad eseguirla. 
     * @param action il codice dell'azione che il thread deve eseguire
     * @param layername il nome del work layer;
     * @return true se non si verificano errori di inizializzazione;
     */
     public boolean startThreadAction (String layername, int action) {
            try {
                 WorkLayer layer = getWorkLayer(layername);
                 if ( layer == null ){
                    return false;
                 }
                 boolean correctStatus = false;
                 if (  ( action == InnoManagerSystemConstants.WORK_LAYER_ACTION_DELETE &&
                         layer.getStatus() == InnoManagerSystemConstants.WORK_LAYER_STATUS_DELETING ) 
                    || ( action == InnoManagerSystemConstants.WORK_LAYER_ACTION_IMPORT &&
                         layer.getStatus() == InnoManagerSystemConstants.WORK_LAYER_STATUS_IMPORTING ) 
                    || ( action == InnoManagerSystemConstants.WORK_LAYER_ACTION_EXPORT &&
                         layer.getStatus() == InnoManagerSystemConstants.WORK_LAYER_STATUS_EXPORTING ) 
                    || ( action == InnoManagerSystemConstants.WORK_LAYER_ACTION_ELABORATE &&
                      layer.getStatus() == InnoManagerSystemConstants.WORK_LAYER_STATUS_ELABORATING ) )
                 { 
                     correctStatus = true;
                 }
                 if ( !correctStatus ) return false;
                 
                 WorkLayerActionThread workThread = new WorkLayerActionThread(this,layername,action,true);
                 workThread.start();
                 return true;
            } catch (Exception e) {
                 ApsSystemUtils.getLogger().error("Errors starting thread " + e.getMessage() );                
            }
            return false;
    }

    /** 
      *  FINE METODI PER LA GESTIONE DELLE ELABORAZIONI SUGLI STRATI INFORMATIVI DI LAVORO 
      */
    
    /**
     * API SUPPORT PER LA GESTIONE LAYER DI LAVORO
     * Not yet implemented
     */
    
    @Override
    public List<WorkLayer> getWorkLayersForApi( String name, String operator ) throws Throwable {
         return this.searchWorkLayers( name,  operator );
    }
     
    @Override
    public void addWorkLayerForApi(String name, String description, String operator) throws ApiException, ApsSystemException {
         this.createWorkLayer(name,description,operator);
    }
    
    @Override
    public void updateWorkLayerForApi(WorkLayer layer) throws ApiException, ApsSystemException {
         if (null == this.getWorkLayer(layer.getName())) {
            throw new ApiException(IApiErrorCodes.API_VALIDATION_ERROR, "Layer with name " + layer.getName() + " not exist", Response.Status.CONFLICT);
         }
         this.updateWorkLayer(layer);
    }
    
    @Override
    public void deleteWorkLayerForApi(String name) throws Throwable {
         this.deleteWorkLayer(name);
    }
    
    /**
     * FINE API SUPPORT PER LA GESTIONE LAYER DI LAVORO
     * 
     */
    
    
    @Override
    public InnoLayer getCouchbaseLayer(String name) throws ApsSystemException{
        return getCouchbaseDAO().loadLayer(name);
    }
    
    @Override
    public List<InnoLayer> getCouchbaseLayers() throws ApsSystemException{
        return getCouchbaseDAO().loadLayers( );
    }
    
    @Override
    public List<Tile> getGeomTilesFromCouchbase(String layer, int x, int y, int zoom ) throws ApsSystemException{
        return getCouchbaseDAO().getGeomTiles(layer, x, y, zoom);
    }
    
    @Override
    public Tile getGeomTileFromCouchbase(String layer, int x, int y, int zoom, int page ) throws ApsSystemException{
        return getCouchbaseDAO().getGeomTile(layer, x, y, zoom, page);
    }
    
    @Override
    public List<Tile> getGeomTilesFromCouchbase(String layer, double lat, double lon, int zoom ) throws ApsSystemException{
        return getCouchbaseDAO().getGeomTiles(layer, lat, lon, zoom);
    }
    
    @Override
    public Tile getGeomTileFromCouchbase(String layer, double lat, double lon, int zoom, int page ) throws ApsSystemException{
        return getCouchbaseDAO().getGeomTile(layer, lat, lon, zoom, page);
    }
    
    @Override
    public List<Infos> getInfoTilesFromCouchbase(String layer, int x, int y, int zoom ) throws ApsSystemException{
        return getCouchbaseDAO().getInfoTiles(layer, x, y, zoom);
    }
    
    
    @Override
    public List<Infos> getInfoTilesFromCouchbase(String layer, double lat, double lon, int zoom ) throws ApsSystemException{
        return getCouchbaseDAO().getInfoTiles(layer, lat, lon, zoom);
    }
    
    
    @Override
    public List<InnoLayer> searchCouchbaseLayers(String name) throws ApsSystemException{
        return getCouchbaseDAO().searchCouchbaseLayerByName(name);
    }

    @Override
    public boolean deleteCouchbaseLayer(String name) throws ApsSystemException{
        //---solo supeuser -- disabled
       
        if (name != null)
        try {
             getCouchbaseDAO().deleteLayer( name );
             return true;
        } catch (Exception e) {
            ApsSystemUtils.getLogger().warn("Error writing layer to couchbase: " + e.getMessage());
        }
        return false;
    }
    
    @Override
    public boolean updateCouchbaseLayer(InnoLayer layer) throws ApsSystemException{
        //---solo supeuser -- disabled
        if (layer != null)
        try {
            getCouchbaseDAO().updateLayer(layer);
            return true;
        } catch (Exception e) {
            ApsSystemUtils.getLogger().warn("Error writing layer to couchbase: " + e.getMessage());
        }
        return false;
    }
    
    @Override
    public Infos getInfoFromCouchbase(String layer, String feature) throws ApsSystemException{
        return null;
    }
    
    @Override
    public String getInfoFromCouchbaseForAPI(String layer, int x, int y, int zoom, int page) throws ApsSystemException{
        return null;
    }
    
    @Override
    public String getTileFromCouchbaseForAPI(String layer, int x, int y, int zoom, int page ) throws ApsSystemException{
        return null;
    }
    
    @Override
    public String getDocumentFromCouchbaseForAPI(String ids) throws ApsSystemException{
        return null;
    }
    
    /**
     *  INIZIO  GESTIONE SHAPEFILES
     */
    
    /**
     * Restituisce il path alla cartella dove scrivere gli shapefile compressi.
     * Se la cartella non è presente nel sistema verrà creata
     * @return il path della cartella dove storare gli shapefiles compressi.
     */
    private String getShapeFileDiskFolder(String layername) {
        try {
            /// verifica area di lavoro resources/innoShapeFiles/<layername>
                StringBuilder buffer = new StringBuilder();
                buffer.append(this.getConfigManager().getParam(SystemConstants.PAR_RESOURCES_DISK_ROOT));
                if ( !buffer.toString().endsWith(File.separator)) 
                      buffer.append(File.separator);
                buffer.append("innoShapeFiles").append(File.separator);
                File dir = new File(buffer.toString());
                if ( !dir.exists() ) 
                    FileUtils.forceMkdir(dir);
                buffer.append(layername).append(File.separator);
                dir = new File(buffer.toString());
                if ( !dir.exists() ) 
                    FileUtils.forceMkdir(dir);
                return buffer.toString();                
        } catch (Throwable t) {
                ApsSystemUtils.logThrowable(t, this, "getShapeFileDiskFolder");                
                return null;
        }       
    }
    
    @Override
    public File unzipShapeFile ( String layername, File zipFile, String zipFileName ){
        int page = 0;
        InputStream fis;
        ZipInputStream zis;
        FileOutputStream fos;
        byte[] buffer = new byte[1024];
        String type;
        File newFile;
        String path = "";
        WorkLayer layer;
        try {
             layer = this.getWorkLayer(layername);
             path = getShapeFileDiskFolder(layername);
             if ( layer == null ) return null;
        } catch (Throwable t) {
            ApsSystemUtils.logThrowable(t, this, "UnZipShapeFile"); 
            return null;
        }
        if ( ! zipFileName.substring( zipFileName.length() - 4 ).equals ( ".zip") )
            return null;
        String fileName = null;
        String shapefileName = null;
        //try to unzip 
        boolean reset = false;
        try {
            fis = FileUtils.openInputStream(zipFile);
            zis = new ZipInputStream (fis);
                //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();
            
            boolean[] ext = {true,true,true};  
            // controllo contenuto zip ( only the first 3 files: .shx, .shp, .dbf with the same name)
            while( ze!=null)
            {
                type = null;
                ApsSystemUtils.getLogger().info("parse file: " + ze.getName() );
                if (  ze.getName().substring ( ze.getName().length() - 4 ).equals ( ".shp") 
                    && ( shapefileName == null || ze.getName().substring ( 0, ze.getName().length() - 4 ).equals(shapefileName))
                    &&  ext[0] )
                { 
                     type = ".shp";
                     ext[0] = false;
                }
                else if (  ze.getName().substring ( ze.getName().length() - 4 ).equals ( ".dbf") 
                        && ( shapefileName == null || ze.getName().substring ( 0, ze.getName().length() - 4 ).equals(shapefileName))
                        &&  ext[1] ) 
                { 
                     type = ".dbf";
                     ext[1] = false;
                }
                else if (  ze.getName().substring ( ze.getName().length() - 4 ).equals ( ".shx") 
                        && ( shapefileName == null || ze.getName().substring ( 0, ze.getName().length() - 4 ).equals(shapefileName))
                        &&  ext[2] ) 
                { 
                     type = ".shx";
                     ext[2] = false;
                }
                // else the shapefiles haven't good ext or name 
                ApsSystemUtils.getLogger().info("type: " + type );
                
                // set the correct name of the shapefiles  (the first valid)
                   
                if (  type != null )
                {  
                    if ( fileName == null ) {
                        shapefileName = ze.getName().substring ( 0, ze.getName().length() - 4 );
                        fileName = zipFileName.substring(0,zipFileName.length() - 4 );
                        boolean found = false;
                        /// if exist changename
                        while (!found){
                            newFile = new File( path + fileName + ".shp");
                            if ( newFile.exists() ) fileName += "0";
                            else found = true;
                        } 
                    } 
                    ApsSystemUtils.getLogger().info("file write: " + path + fileName + type );
                    newFile = new File(path + fileName + type);
                    if ( newFile.exists() ) 
                        FileUtils.forceDelete(newFile);
                    newFile = new File(path + fileName + type);
                    {
                        fos = new FileOutputStream(newFile);
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                        fos.close(); 
                        zis.closeEntry();  
                    }
                }
                ze = zis.getNextEntry();
            }     
            zis.close();
        } catch (Throwable t) {
            ApsSystemUtils.logThrowable(t, this, "UnZippingShapeFile"); 
            reset = true;
        }
        if ( fileName != null ){
            if (reset ){  
                removeShapeFile(layername, fileName +  ".shp");
            }
            else {
                newFile = new File( path + fileName  + ".shp" );
                if ( newFile.exists() )
                    return newFile;
            }
        }    
        return null;
    } 
    
    @Override
    public List<String> getShapeFileInfo ( String layername ){
           List<String> shapeInfo = new ArrayList<String>();
           String path = getShapeFileDiskFolder(layername);
           File dir = new File (path);
           String[] ext = {"shp"};
           if ( dir.exists())
               for (Iterator<File> it = FileUtils.listFiles(dir, ext, false).iterator(); it.hasNext();) {
                   File file = it.next();
                   shapeInfo.add(file.getName()+" ("+file.length()+")");
               }
          return shapeInfo; 
    }
    
    @Override
    public List<String> getShapeFilesAttributes ( String layername ){
           List<String> attributes = new ArrayList<String>();
           String path = getShapeFileDiskFolder(layername);
           File dir = new File (path);
           File dbfFile = null;
           String[] ext = {"dbf"};
           Collection<File> files;
           if ( dir.exists() ) {
               files = FileUtils.listFiles(dir, ext, false);
               if ( files != null && files.iterator().hasNext() )
                    dbfFile = files.iterator().next();
               return getShapeFileAttributes(dbfFile);
           }     
           return attributes;
    }
    
    @Override
    public List<String> getShapeFileAttributes ( File dbfFile ){
           List<String> attributes = new ArrayList<String>();
           try {
                    FileInputStream fis=new FileInputStream(dbfFile);
                    DbaseFileReader dbfReader=new DbaseFileReader(fis.getChannel(),false, Charset.forName("UTF8"));
                    DbaseFileHeader dbfHeader=dbfReader.getHeader();
                    int n=dbfHeader.getNumFields();
                    for (int i=0; i < n; i++)   
                        attributes.add(dbfHeader.getFieldName(i) + ": "+ dbfHeader.getFieldClass(i).getSimpleName());
                    return attributes;
           } catch (Exception e) {
               ApsSystemUtils.getLogger().debug(e.getMessage()); 
           }
           return attributes;
    }
    
    @Override
    public boolean removeShapeFile ( String layername, String shapeFileName ){
        try {
            boolean result = true;
            String path = getShapeFileDiskFolder(layername);
            File newFile = new File(path + shapeFileName);
            if ( newFile.exists()  )
                FileUtils.forceDelete(newFile);
            newFile = new File(path + shapeFileName.substring(0,shapeFileName.length() - 4 ) + ".shx");
            if ( newFile.exists()  )
                FileUtils.forceDelete(newFile);
            newFile = new File(path + shapeFileName.substring(0,shapeFileName.length() - 4 ) + ".dbf");
            if ( newFile.exists()  )
                FileUtils.forceDelete(newFile);
            ApsSystemUtils.getLogger().info("files deleted" + shapeFileName );
            return result;            
        } catch (Throwable t) {
            ApsSystemUtils.logThrowable(t, this, "removeShapeFile"); 
            return false;
        }
    }
    
    /**
     * eseguela rimozione dei files .shp, .dbf, .shx del layer indicato   
     * @param layername il nome dello strato informativo di lavoro
     * @return true se la cartella con i shapefiles è stata cancellata altrimenti false   
     */
    public boolean removeShapeFiles ( String layername ){
        try {
            boolean result = true;
            String path = getShapeFileDiskFolder(layername);
            File dirFile = new File(path);
            if ( dirFile.exists()  )
                FileUtils.forceDelete(dirFile);
            ApsSystemUtils.getLogger().info("files deleted " + path );
            return result;            
        } catch (Throwable t) {
            ApsSystemUtils.logThrowable(t, this, "removeShapeFile"); 
            return false;
        }
    }
    
    /**
     *  FINE GESTIONE SHAPEFILES
     */
    
    /**
     * Restituisce il dao dei work layer.
     * @return Il daodei work layer.
     */
    protected IWorkLayerDAO getWorkLayerDAO() {
        return _workLayerDAO;
    }

    /**
     * Imposta il dao dei work layer.
     * @param layerDAO Il dao dei work layer.
     */
    public void setWorkLayerDAO(IWorkLayerDAO workLayerDAO) {
        this._workLayerDAO = workLayerDAO;
    }
    
    /**
     * restituisce l'istanza del client java di Couchbase per le operazioni di
     * analisi dati.
     * @return l'oggetto CouchbaseClient che permette le operazioni su Couchbase 
     */
    public ICouchbaseDAO getCouchbaseDAO() {
    	return _couchbaseLayerDao;
    }
     
     /**
     * restituisce l'istanza del client java di Couchbase per le operazioni di
     * analisi dati.
     * @return l'oggetto CouchbaseClient che permette le operazioni su Couchbase 
     */
     public void setCouchbaseDAO( ICouchbaseDAO couchbaseLayerDao) {
    	 _couchbaseLayerDao = couchbaseLayerDao;
     }
     
     /**
     * Restituisce il manager della configurazione per l'accesso a Couchbase.
     * @return Il manager della configurazione per l'accesso a Couchbase.
     */
     protected ConfigInterface getConfigManager() {
         return _configManager;
     }

     /**
      * Imposta il manager della configurazione per l'accesso a Couchbase.
      * @param _innoConfigManager Il manager della configurazione per l'accesso a Couchbase.
      */
     public void setConfigManager(ConfigInterface configManager) {
          this._configManager = configManager;
     }
     
     

     private IWorkLayerDAO   _workLayerDAO;
     private ICouchbaseDAO   _couchbaseLayerDao;
     private ConfigInterface _configManager;
     private List<WorkLayerActionThread> _listThreadWorking;
       
}

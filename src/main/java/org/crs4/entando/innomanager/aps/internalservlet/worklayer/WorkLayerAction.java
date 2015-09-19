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
package org.crs4.entando.innomanager.aps.internalservlet.worklayer;

import org.crs4.entando.innomanager.aps.system.services.layer.model.WorkLayer;
import org.crs4.entando.innomanager.aps.system.services.layer.ILayerManager;


import com.agiletec.aps.system.ApsSystemUtils;
import com.agiletec.apsadmin.system.BaseAction;
import static com.agiletec.apsadmin.system.BaseAction.FAILURE;
import static com.agiletec.apsadmin.system.BaseAction.USER_NOT_ALLOWED;
import static com.opensymphony.xwork2.Action.SUCCESS;
import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.crs4.entando.innomanager.aps.internalservlet.layer.helper.IWorkLayerActionHelper;
import org.crs4.entando.innomanager.aps.system.InnoManagerSystemConstants;
import org.crs4.entando.innomanager.aps.system.services.layer.LayerManager;

/**
 * La classe che crea gli oggetti WorkLayer.
 * @author Roberto Demontis (CRS4) 
 * @email demontis@crs4.it
 */
public class WorkLayerAction extends BaseAction {
	
    
    	/**
         * Verifica se deve creare un nuovo layer o modificarne uno 
         * @return il nome dell'azione da eseguire o errore
         */
        public String entryEditAction() {
		this.clearErrorsAndMessages();
                HttpServletRequest request = this.getRequest();
                try {
                    WorkLayer layer = getLayer();
                    if ( layer == null ) {
                        return "editNew";
                    }
                    else if ( layer.getOperator().equals(this.getCurrentUser().getUsername() ) 
                           || this.getCurrentUser().getUsername().equals("admin") ) 
                    {
                        return "edit";
                    }    
                    else return USER_NOT_ALLOWED;
                } catch (Exception e) {
                    this.addActionError("innomanager_WORK_LAYER_ERROR");
                }
                return FAILURE;
        }
        
        /**
         * Determina l'azione da eseguire sul WorkLayer
         * @return il nome dell'azione da eseguire o errore
         */
        public String entryElaborateAction() {
		this.clearErrorsAndMessages();
                try {
                    WorkLayer layer = getLayer();
                    if ( layer == null ) {
                        this.addActionMessage( "Name: " + this.getName() );
                        this.addActionError("innomanager_WORK_LAYER_NOT_PRESENT");
                        return FAILURE;
                    }
                    if ( ! layer.getOperator().equals(this.getCurrentUser().getUsername() ) 
                      && ! this.getCurrentUser().getUsername().equals("admin") ) 
                    {
                        return USER_NOT_ALLOWED;
                    }
                    String action = getStrutAction();
                    if ( action != null ){
                            if (  action.equals("delete") || action.equals("deleteConfirm") 
                               || action.equals("export")
                               || action.equals("import") || action.equals("elaborate")  ) 
                            {
                                 return action;
                            } 
                            
                    }
                    else return "start";        
                } catch (Exception e) {
                    this.addActionError("innomanager_WORK_LAYER_ERROR");
                }
                return FAILURE;
        }
        
        /**
         * Restituisce l'oggetto WorkLayer su cui si deve eseguire l'eleborazione
         * @return un oggetto WorkLayer o null
         */
        public WorkLayer getLayer() {
		try {
                    if ( this.getWorkLayerActionHelper().isUserOnInnoGroup(this.getCurrentUser())
                       || this.getCurrentUser().getUsername().equals("admin") )  
                    {
                        WorkLayer layer = getLayerManager().getWorkLayer(_name);
                        if ( layer!= null && layer.getStatus() == InnoManagerSystemConstants.WORK_LAYER_STATUS_UPLOADED ) 
                            getLayerManager().verifyFile(layer);
                        if ( layer.getOperator().equals(this.getCurrentUser().getUsername() )
                           || this.getCurrentUser().getUsername().equals("admin"))
                             return layer;
                        else return null;
                    }
                } catch (Throwable t) {
                    ApsSystemUtils.logThrowable(t, this, "getLayer");
                }
		return null;
	}       
        
        /**
         * Prepara il form di edit e verifica autorizzazione sull'oggetto WorkLayer 
         * @return l'esito dell'azione
         */
        public String edit() {
		try {
                    this.clearErrorsAndMessages();
                    WorkLayer layer = getLayer();
                    if ( layer == null ) {
                        this.addActionMessage( "Name: " + this.getName() );
                        this.addActionError("innomanager_WORK_LAYER_NOT_PRESENT");
                        return FAILURE;
                    }
                    if ( ! layer.getOperator().equals(this.getCurrentUser().getUsername() ) 
                      && ! this.getCurrentUser().getUsername().equals("admin") ) 
                    {
                        return USER_NOT_ALLOWED;
                    }
                    this.valueForm(layer);
                    this.setStrutAction("edit");
                    return SUCCESS;
                } catch (Throwable t) {
                    ApsSystemUtils.logThrowable(t, this, "edit");
                    this.addActionError("innomanager_WORK_LAYER_ERROR");
		}
		return FAILURE;
	}
        
        /**
         * Prepara il form di edit per la creazione di un nuovo oggetto WorkLayer 
         * e verifica l'autorizzazione dell'utente 
         * @return l'esito dell'azione
         */
        public String editNew() {
		try {
                    this.clearErrorsAndMessages();
                    if ( this.getWorkLayerActionHelper().isUserOnInnoGroup(this.getCurrentUser())
                       || this.getCurrentUser().getUsername().equals("admin") )  
                    {
                        this.setStrutAction("editNew");
                        this.reset();
                        return SUCCESS;
                    }
                    else {
                        return USER_NOT_ALLOWED;
                    } 
                        
                } catch (Throwable t) {
                    ApsSystemUtils.logThrowable(t, this, "editNew");
                    this.addActionError("innomanager_WORK_LAYER_ERROR");
		}
		return FAILURE;
	}
        
        /**
         * Salva le modifiche apportate all'oggetto WorkLayer e verifica  
         * l'autorizzazione dell'utente 
         * @return l'esito dell'azione
         */
        public String save() {
		try {
                    this.clearErrorsAndMessages();
                    if ( this.getWorkLayerActionHelper().isUserOnInnoGroup(this.getCurrentUser())
                       || this.getCurrentUser().getUsername().equals("admin") )  
                    {
                        WorkLayer layer = getLayer();
                        if ( _name == null || _descr == null ){
                            this.addActionError("innomanager_WORK_LAYER_WRONG_PARAMETER");
                            return FAILURE;    
                        }
                        if ( getStrutAction().equals("editNew") ){
                            if ( layer == null ) {
                                if ( _name.trim().length() > 0 && _name.trim().length() < 11 ) {
                            	   char ch;
            			   for ( int i=0; i < _name.length();i++){
                		   ch = _name.charAt(i);
                		   if ( !( (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9') ) )  
                     			this.addActionError("innomanager_WORK_LAYER_WRONG_NAME");
                     			return FAILURE;
                     		   }
                                   getLayerManager().createWorkLayer(_name,_descr,this.getCurrentUser().getUsername());
                                   if ( getLayerManager().getWorkLayer(_name) != null )
                                       return SUCCESS;
                                   else return FAILURE;
                                } 
                                else this.addActionError("innomanager_WORK_LAYER_WRONG_NAME");
                            } 
                            else this.addActionError("innomanager_WORK_LAYER_ALREADY_EXIST");
                        }
                        else if ( getStrutAction().equals("edit") )
                        {
                            if ( layer == null )
                            {
                                this.addActionError("innomanager_WORK_LAYER_NOT_PRESENT");
                                return FAILURE; 
                            } 
                            else if ( layer.getOperator().equals(this.getCurrentUser().getUsername() )
                                   || this.getCurrentUser().equals("admin") ) 
                            {
                                layer.setDescription(_descr);
                                getLayerManager().updateWorkLayer(layer);
                                return SUCCESS;
                            }
                            else return USER_NOT_ALLOWED;
                        }
                    }
                    else return USER_NOT_ALLOWED;
                } catch (Throwable t) {
                    ApsSystemUtils.logThrowable(t, this, "save");
                    this.addActionError("innomanager_WORK_LAYER_SAVE_ERROR");
		}
                return FAILURE;
	}
        
        
        /**
         * Cancella un oggetto WorkLayer e verifica l'autorizzazione dell'utente 
         * 
         * @return l'esito dell'azione
         */
        public String delete() {
		try {
                    this.clearErrorsAndMessages();
                    WorkLayer layer = getLayer();
                    if ( layer == null )
                    {
                        this.addActionMessage("innomanager_WORK_LAYER_NOT_PRESENT");
                    }
                    else if ( ! layer.getOperator().equals(this.getCurrentUser().getUsername() ) 
                      && ! this.getCurrentUser().getUsername().equals("admin") ) 
                    {
                        return USER_NOT_ALLOWED;
                    }
                    else {
                        this.getLayerManager().deleteWorkLayer(layer.getName());
                        return SUCCESS;
                    }
                } catch (Throwable t) {
                    ApsSystemUtils.logThrowable(t, this, "delete");
                }
		return FAILURE;
	}
        
        /**
         * Esegue l'upload di un file e verifica l'autorizzazione dell'utente 
         * 
         * @return l'esito dell'azione
         */
        public String upload() {
		try {
                    this.clearErrorsAndMessages();
                    WorkLayer layer = getLayer();
                    if ( layer == null )
                    {
                        this.addActionMessage("innomanager_WORK_LAYER_NOT_PRESENT");
                    }
                    else if ( ! layer.getOperator().equals(this.getCurrentUser().getUsername() ) 
                           && ! this.getCurrentUser().getUsername().equals("admin") ) 
                    {
                        return USER_NOT_ALLOWED;
                    }
                    else if ( layer.getStatus() == InnoManagerSystemConstants.WORK_LAYER_STATUS_UPLOADING ) 
                    {
                         this.addActionMessage("innomanager_WORK_LAYER_SAVE_FILE_IN_PROGRESS");
                         return SUCCESS;
                    }
                    else if ( layer.getStatus() != InnoManagerSystemConstants.WORK_LAYER_STATUS_CREATE )
                    {
                         this.addActionError("innomanager_WORK_LAYER_WRONG_STATUS");
                         return FAILURE;
                    }
                    else {
                        layer.setStatus(InnoManagerSystemConstants.WORK_LAYER_STATUS_UPLOADING);
                        Date date = new Date();
                        layer.setLog( "["+date+"] Start caricamento shapefile nell'area  locale di lavoro \n" + layer.getLog());
                        getLayerManager().updateWorkLayer(layer);
                        // start unzip
                        File shapefile = getLayerManager().unzipShapeFile(this.getName(),this.getZippedShapefile(),this.getZippedShapefileFileName() ); 
                        if ( shapefile == null ){
                            layer.setStatus(InnoManagerSystemConstants.WORK_LAYER_STATUS_CREATE);
                            getLayerManager().updateWorkLayer(layer);
                            this.addActionError("innomanager_ERROR_READING_SHAPEFILE");
                            return FAILURE;
                        }
                        setZippedShapefile(null);
                        setZippedShapefileFileName(null);
                        // write info
                        String tables = "["+date+"] File importati: \n" ;
                        for ( Iterator it = getLayerManager().getShapeFileInfo(layer.getName()).iterator(); it.hasNext() ;)
                            tables += it.next() + "\n" ; 
                        layer.setLog( tables + layer.getLog() );
                        
                        String attributes = "["+date+"] Attributi trovati nel shapefile: \n" ;
                        for ( Iterator it = getLayerManager().getShapeFilesAttributes(layer.getName()).iterator(); it.hasNext() ;)
                            attributes += it.next() + "\n" ;   
                        layer.setLog( attributes + layer.getLog() );
                        
                        layer.setStatus(InnoManagerSystemConstants.WORK_LAYER_STATUS_UPLOADED);
                        layer.setLog( "["+date+"] Fine caricamento shapefile nell'area  locale di lavoro...\n" + layer.getLog() );
                        getLayerManager().updateWorkLayer(layer);
                        return SUCCESS;
                        //to do: parametrizzare i log per le lingue 
                    }     
                } catch (Throwable t) {
                    ApsSystemUtils.logThrowable(t, this, "upload");
                    this.addActionError("innomanager_ERROR_UPLOADING_SHAPEFILE");
                }
                return FAILURE;
        }
        
        /**
         * Crea ed fa partire il thread che si occupera del caricamento dei dati  
         * dallo shapefile al database 
         * 
         * @return l'esito dell'azione
         */
        public String importToDb() {
		try {
                    this.clearErrorsAndMessages();
                    WorkLayer layer = getLayer();
                    if ( layer == null )
                    {
                        this.addActionMessage("innomanager_WORK_LAYER_NOT_PRESENT");
                    }
                    else if ( ! layer.getOperator().equals(this.getCurrentUser().getUsername() ) 
                           && ! this.getCurrentUser().getUsername().equals("admin") ) 
                    {
                        return USER_NOT_ALLOWED;
                    }
                    else if ( layer.getStatus() == InnoManagerSystemConstants.WORK_LAYER_STATUS_IMPORTING ) {
                         return SUCCESS;
                    }
                    else if ( layer.getStatus() != InnoManagerSystemConstants.WORK_LAYER_STATUS_UPLOADED ) {
                         this.addActionError("innomanager_WORK_LAYER_WRONG_STATUS");
                         return FAILURE;
                    }
                    else {
                        Date date = new Date();
                        layer.setLog( "["+date+"] Inizio scrittura shapefile su database locale di lavoro...\n  "  +  layer.getLog() );
                        layer.setStatus(InnoManagerSystemConstants.WORK_LAYER_STATUS_IMPORTING);
                        LayerManager manager = (LayerManager) getLayerManager(); 
                        manager.updateWorkLayer(layer);
                        ApsSystemUtils.getLogger().info(" status " +  layer.getStatusText()  );
                        if ( manager.startThreadAction(layer.getName(),InnoManagerSystemConstants.WORK_LAYER_ACTION_IMPORT) )
                            return SUCCESS;
                        else {
                            layer.setStatus(InnoManagerSystemConstants.WORK_LAYER_STATUS_UPLOADED);
                            manager.updateWorkLayer(layer);
                            this.addActionError("innomanager_IMPORT_THREAD_NOT_STARTED");
                        }
                    }    
                } catch (Throwable t) {
			ApsSystemUtils.logThrowable(t, this, "write");
                        this.addActionError("innomanager_SYSTEM_ERROR");
		}
                return FAILURE;
	}
        
        
        
        /**
         * Crea e fa partire il thread che si occupera' del tassellamento dei 
         * dati nel database e della creazione dei documenti JSON ma non li 
         * scrive su disco
         * 
         * @return l'esito dell'azione
         */
        public String elaborate() {
		try {
                    this.clearErrorsAndMessages();
                    WorkLayer layer = getLayer();
                    if ( layer == null )
                    {
                        this.addActionMessage("innomanager_WORK_LAYER_NOT_PRESENT");
                    }
                    else if ( ! layer.getOperator().equals(this.getCurrentUser().getUsername() ) 
                           && ! this.getCurrentUser().getUsername().equals("admin") ) 
                    {
                        return USER_NOT_ALLOWED;
                    }
                    else if ( layer.getStatus() == InnoManagerSystemConstants.WORK_LAYER_STATUS_ELABORATING )
                    {    
                         return SUCCESS;
                    }
                    else if ( layer.getStatus() != InnoManagerSystemConstants.WORK_LAYER_STATUS_IMPORTED ) {
                         this.addActionError("innomanager_WORK_LAYER_WRONG_STATUS");
                         return FAILURE;
                    }
                    else {
                        Date date = new Date();
                        layer.setLog( "["+date+"] Inizio elaborazione nel database locale di lavoro...\n  "  +  layer.getLog() );
                        layer.setStatus(InnoManagerSystemConstants.WORK_LAYER_STATUS_ELABORATING);
                        LayerManager manager = (LayerManager) getLayerManager(); 
                        manager.updateWorkLayer(layer);
                        if ( manager.startThreadAction(layer.getName(),InnoManagerSystemConstants.WORK_LAYER_ACTION_ELABORATE) ){
                            return SUCCESS;
                        }
                        else {
                            layer.setStatus(InnoManagerSystemConstants.WORK_LAYER_STATUS_IMPORTED);
                            manager.updateWorkLayer(layer);
                            this.addActionError("innomanager_ELABORATE_THREAD_NOT_STARTED");
                        }
                    }
                } catch (Throwable t) {
                    ApsSystemUtils.logThrowable(t, this, "infos");
                    this.addActionError("innomanager_SYSTEM_ERROR");
                }
		return FAILURE;
	}
        
        /**
         * Crea e fa partire il thread che si occupera della scrittura su disco 
         * ( /tmp ) dei documenti JSON e quindi li invia al nodo couchbase registrato
         *  nella configurazione
         * @return l'esito dell'azione
         */
        public String export() {
		try {
                    this.clearErrorsAndMessages();
                    WorkLayer layer = getLayer();
                    if ( layer == null )
                    {
                        this.addActionMessage("innomanager_WORK_LAYER_NOT_PRESENT");
                    }
                    else if ( ! layer.getOperator().equals(this.getCurrentUser().getUsername() ) 
                           && ! this.getCurrentUser().getUsername().equals("admin") ) 
                    {
                        return USER_NOT_ALLOWED;
                    }
                    else if ( layer.getStatus() == InnoManagerSystemConstants.WORK_LAYER_STATUS_EXPORTING ) {
                         return SUCCESS;
                    }
                    else if (  layer.getStatus()  != InnoManagerSystemConstants.WORK_LAYER_STATUS_READY ) 
                    {
                         this.addActionError("innomanager_WORK_LAYER_WRONG_STATUS");
                         return FAILURE;
                    }
                    else {
                        Date date = new Date();
                        layer.setLog( "["+date+"] Inizio export json su Couchbase \n" + layer.getLog() );
                        layer.setStatus(InnoManagerSystemConstants.WORK_LAYER_STATUS_EXPORTING);
                        LayerManager manager = (LayerManager) getLayerManager(); 
                        manager.updateWorkLayer(layer);
                        if ( manager.startThreadAction(layer.getName(), InnoManagerSystemConstants.WORK_LAYER_ACTION_EXPORT) ) {
                            return SUCCESS;
                        }       
                        else {
                            layer.setStatus(InnoManagerSystemConstants.WORK_LAYER_STATUS_READY);
                            manager.updateWorkLayer(layer);
                            this.addActionError("innomanager_EXPORT_THREAD_NOT_STARTED");
                        }
                   }     
                    
                } catch (Throwable t) {
                    ApsSystemUtils.logThrowable(t, this, "infos");
                    this.addActionError("innomanager_SYSTEM_ERROR");
                }
                return FAILURE;
	}
	
	/**
         * gestione form creazione o modifica informazioni dello strato informativo 
         * 
         * @param layer l'oggetto WorkLayer che si vuole creare e/o modificare
         * @throws Throwable 
         */
        private void valueForm(WorkLayer layer) throws Throwable {
		if ( layer == null ) return;
                this.setName ( layer.getName() );
                this.setDescription ( layer.getDescription() );
        }
        
        /**
         * gestione form creazione o modifica informazioni dello strato informativo 
         * il metodo resetta i valori nel form
         * @throws Throwable 
         */
        private void reset() throws Throwable {
		this.setName ( null );
                this.setDescription ( null );
        }
        
        /**
         * restituisce la dimensione in byte dello shapefile compresso relativo
         *  al WorkLayer su cui si esegue l'azione
         * 
         * @return la dimensione in byte dello shapefile uploadato
         */
        public long getFileLength() {
            if ( getZippedShapefile() != null )
                return getZippedShapefile().length();
            else return -1;
            
        }
        
        /**
         * restituisce le informazioni dello shapefile relativo al WorkLayer 
         * su cui si esegue l'azione
         * @return 
         */
        public List<String> getShapeFilesInfos() {
            return getLayerManager().getShapeFileInfo(_name);
            
        }
        
        /**
         * restituisce gli attributi non geometrici dello shapefile relativo 
         * al WorkLayer su cui si esegue l'azione 
         * 
         * @return la dimensione dello shape file compresso
         */
        public List<String> getShapeFilesAttributes() {
            return getLayerManager().getShapeFilesAttributes(_name);
        }
        
        
        /**
	 * Restituisce il nome o identificativo del layer .
	 * @return L'identificativo della scheda.
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Imposta il nome o identificativo del layer.
	 * @param id L'identificativo della scheda.
	 */
	public void setName(String name) {
		this._name = name;
	}
	
	/**
     	 * Restituisce la descrizione del layer.
     	 * @return La descrizione del layer.
     	 */
    	public String getDescription() {
        	return _descr;
    	}
    
    	/**
     	 * Imposta la descrizione del layer.
     	 * @param descr La descrizione del layer.
     	 */
    	public void setDescription(String descr) {
        	this._descr = descr;
    	}   	
        
        /**
     	 * Imposta l'azione da eseguire.
     	 * @param il nome dell'azione
     	 */
    	public void setStrutAction(String strutAction) {
        	this._strutAction = strutAction;
    	}
        
        /**
     	 * Restituisce  l'azione da eseguire
     	 * @return il nome dell'azione
     	 */
    	public String getStrutAction() {
            return _strutAction ;
        }
        
        
        /**
         * @return the _layerManager
         */
        protected ILayerManager getLayerManager() {
		return _layerManager;
	}
	
        /**
         * @param layerManager the _layerManager to set
         */
        public void setLayerManager(ILayerManager layerManager) {
		this._layerManager = layerManager;
	}
	
        /**
         * @return the _workLayerActionHelper
         */
        public IWorkLayerActionHelper getWorkLayerActionHelper() {
               return _workLayerActionHelper;
        }

        /**
         * @param workLayerActionHelper the _workLayerActionHelper to set
         */
        public void setWorkLayerActionHelper(IWorkLayerActionHelper workLayerActionHelper) {
               this._workLayerActionHelper = workLayerActionHelper;
        }
        
        /**
         * @return the _zippedShapeFileFile
         */
        public File getZippedShapefile() {
              return _zippedShapefile;
        }

        /**
         * @param zippedShapefile the _zippedShapeFile to set
         */
        public void setZippedShapefile(File zippedShapefile) {
              this._zippedShapefile = zippedShapefile;
        }

        /**
         * @return the _zippedShapeFileFileName
         */
        public String getZippedShapefileFileName() {
              return _zippedShapefileFileName;
        }

        /**
         * @param zippedShapeFileFileName the _zippedShapeFileFileName to set
         */
        public void setZippedShapefileFileName(String zippedShapeFileFileName) {
              this._zippedShapefileFileName = zippedShapeFileFileName;
        }
        
        private ILayerManager _layerManager;
	private IWorkLayerActionHelper _workLayerActionHelper;

        private String   _strutAction;
        private String   _name;
    	private String   _descr;
    	private File     _zippedShapefile;
        private String   _zippedShapefileFileName;  
}

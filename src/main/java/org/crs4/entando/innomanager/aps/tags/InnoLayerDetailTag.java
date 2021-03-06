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
package org.crs4.entando.innomanager.aps.tags;

import com.agiletec.aps.system.ApsSystemUtils;
import com.agiletec.aps.util.ApsWebApplicationUtils;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import org.crs4.entando.innomanager.aps.system.InnoManagerSystemConstants;
import org.crs4.entando.innomanager.aps.system.services.layer.ILayerManager;
import  org.crs4.entando.innomanager.aps.system.services.layer.model.InnoLayer;


/**
 * Tag erogatore della lista degli strati informativi di lavoro (worklayer) .
 * @author R.Demontis
 */
public class InnoLayerDetailTag extends TagSupport {
	
	@Override
	public int doStartTag() throws JspException {
		ServletRequest request =  this.pageContext.getRequest();
		try {
                        HttpSession session = this.pageContext.getSession();
                        String name = request.getParameter("name");
                        ILayerManager layerManager = (ILayerManager) ApsWebApplicationUtils.getBean(InnoManagerSystemConstants.INNO_DATAMANAGER, this.pageContext);
			
                        InnoLayer layer = layerManager.getCouchbaseLayer(name);
                        if ( layer != null ){
                             this.pageContext.setAttribute(this.getObjName(), layer);
                        }
                        ApsSystemUtils.getLogger().debug( "INNOLayer found layer for name:" + name );
		} catch (Throwable e) {
			ApsSystemUtils.logThrowable(e, this, "doStartTag");
			throw new JspException("Errore in doStartTag", e);
		}
		return super.doStartTag();
	}
	
        @Override
	public void release() {
		this._objName = null;
	}
	
	/**
	 * Restituisce il nome con il quale viene inserita nel pageContext il layer
	 * @return Il nome della variabile.
	 */
	public String getObjName() {
		return _objName;
	}
	
	/**
	 * Setta il nome con il quale viene inserita nel pageContext il layer
	 * @param listName Il nome della variabile.
	 */
	public void setObjName(String objName) {
		this._objName = objName;
	}
	
	private String _objName;
        
        
        
	
}

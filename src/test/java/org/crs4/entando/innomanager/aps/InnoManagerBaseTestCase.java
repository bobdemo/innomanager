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
package org.crs4.entando.innomanager.aps;

import com.agiletec.ConfigTestUtils;
import com.agiletec.aps.BaseTestCase;
import org.crs4.entando.innomanager.InnoManagerConfigUtils;

/**
 * @author R.Demontis
 */
public class InnoManagerBaseTestCase extends BaseTestCase {
    
    @Override
    protected ConfigTestUtils getConfigUtils() {
            return new InnoManagerConfigUtils();
    }
    
}
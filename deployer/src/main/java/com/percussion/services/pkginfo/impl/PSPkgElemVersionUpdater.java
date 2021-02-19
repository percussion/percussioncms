/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.pkginfo.impl;

import com.percussion.rx.config.IPSConfigService;
import com.percussion.rx.config.data.PSConfigStatus.ConfigStatus;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.pkginfo.IPSPkgUpdater;
import com.percussion.services.pkginfo.utils.PSPkgHelper;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;

/**
 * This class facilitates the updating of package element version information
 * after configuration has been applied as well as the validation of package
 * elements before configuration is applied.  It also listens to the server
 * initialization notification to register itself with the configuration
 * service.
 */
public class PSPkgElemVersionUpdater implements IPSPkgUpdater
{
   public void configChanged(Collection<IPSGuid> ids, ConfigStatus status) throws PSNotFoundException {
      if (ids == null)
         throw new IllegalArgumentException("ids may not be null");
      
      if (status == null)
         throw new IllegalArgumentException("status may not be null");
            
      PSPkgHelper.updatePkgElementVersions(ids);
   }

   public void preConfiguration(String name) throws PSNotFoundException {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be blank");
      
      PSPkgHelper.validatePackage(name);
   }
   
   /**
    * Adds the listener on (applying) configuration changes. This is wired
    * by the spring framework.
    * 
    * @param cfgSvc the configure service, never <code>null</code>.
    */
   public void setConfigService(IPSConfigService cfgSvc)
   {
      if (cfgSvc == null)
         throw new IllegalArgumentException("cfgSvc must not be null");
      
      cfgSvc.addConfigChangeListener(this);
   }
}

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

package com.percussion.install;
import org.w3c.dom.Element;

/**
 * The IPSUpgardePlugin interface is the general interface that a Rhythmyx 
 * plugin must implement. Any plugin that implements this interface can be 
 * registered via the configuration XML file.
 * 
 */

public interface IPSUpgradePlugin
{
   /**
    * This is the method the plugin manager calls for every plugin. process() 
    * shall perform all its upgrade tasks.
    * @param config object of IPSUpgradeModule class
    * @param elemData is data Element of plugin element
    * @return PSPluginResponse object, <code>null</code> if not a pre-upgrade
    * plugin
    */
   PSPluginResponse process(IPSUpgradeModule config, Element elemData);
}

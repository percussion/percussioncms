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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.utils.jexl;

import org.apache.commons.jexl3.JexlException;

import java.util.Map;


public interface IPSScript
{

    /***
     * An optional string indicating the type of system object that owns this script. Never null
     * @return a user friendly string that indicates the owner type: Template, Widget, Snippet, Location Scheme etc. Never null.  May be empty.
     */
    public String getOwnerType();

    /***
     * Sets the type of system object that owns this script.  Should be user friendly and i18N.
     * @param ownerType
     */
    public void setOwnerType(String ownerType);

    /***
     * An optional property that indicates the system object that owns this script.
     * @return name of the system object, never null, may be empty
     */
    public String getOwnerName();

    /***
     * Sets the user friendly name of the system object that owns this script. Should be user friendly.
     * @param ownerName  Never null.
     */
    public void setOwnerName(String ownerName);

    public boolean isCompilable();
    
    public String getScriptText();
    
    public String getParsedText();
    
    public Object eval(Map<String,Object> bindings)  throws JexlException;

    public String getSourceText();

    public boolean getUseStrictMode();

    public void setUseStrictMode(boolean useStrictMode);

    public boolean getUseDebugMode();

    public void setUseDebugMode(boolean useDebugMode);

    public boolean getSilentMode();

    public void setUseSilentMode(boolean useSilentMode);

    /***
     * Reinitialize
     */
    public void reinit(boolean reloadOptionsFromConfig);

}

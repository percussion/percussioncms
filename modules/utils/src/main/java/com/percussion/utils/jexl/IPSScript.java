/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

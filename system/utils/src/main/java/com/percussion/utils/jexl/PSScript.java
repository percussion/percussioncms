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

package com.percussion.utils.jexl;

import com.percussion.install.RxFileManager;
import com.percussion.utils.container.DefaultConfigurationContextImpl;
import com.percussion.utils.container.PSContainerUtilsFactory;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.JexlScript;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

public class PSScript implements IPSScript
{
    /** The logger. NOTE:  jexl requires commons logging */
    private static final Log LOG = LogFactory.getLog(PSScript.class);

    /** The shared expression cache size. */
    public static int CACHE_SIZE = 512;

    private String scriptText;

    //Control JEXL strict mode
    private static boolean jexlUseStrict = false;

    //Control JEXL silent
    private static boolean jexlUseSilent = false;

    //Control JEXL debug
    private static boolean jexlUseDebug = false;


    private boolean compilable = false;

    private JexlScript compiledScript = null;
   
    public PSScript(String scriptText)
    {
        this.scriptText=scriptText;
    }

    private String ownerType = "";

    private String ownerName = "";
    /***
     * An optional string indicating the type of system object that owns this script. Never null
     * @return a user friendly string that indicates the owner type: Template, Widget, Snippet, Location Scheme etc. Never null.  May be empty.
     */
    @Override
    public String getOwnerType() {
        return ownerType;
    }

    /***
     * Sets the type of system object that owns this script.  Should be user friendly and i18N.
     * @param ownerType
     */
    @Override
    public void setOwnerType(String ownerType) {
        if(ownerType==null)
            ownerType = "";
        this.ownerType = ownerType.trim();
    }

    /***
     * An optional property that indicates the system object that owns this script.
     * @return name of the system object, never null, may be empty
     */
    @Override
    public String getOwnerName() {
        return ownerName;
    }

    /***
     * Sets the user friendly name of the system object that owns this script. Should be user friendly.
     * @param ownerName  Never null.
     */
    @Override
    public void setOwnerName(String ownerName) {
        if(ownerName == null){
            ownerName="";
        }
        this.ownerName = ownerName.trim();
    }

    @Override
    public boolean isCompilable()
    {
        return this.compilable;
    }

    @Override
    public String getScriptText()
    {
        return scriptText;
    }


    @Override
    public String getParsedText()
    {
        return scriptText;
    }

    @Override
    public Object eval(Map<String, Object> bindingsMap) throws JexlException 
    {
        JexlContext context = new MapContext(bindingsMap);
        
        if (compiledScript==null)
        {
            String fixedScriptText = JexlScriptFixes.fixScript(scriptText,ownerType,ownerName);

            compiledScript = EngineSingletonHolder.DEFAULT_ENGINE.createScript(fixedScriptText);
        }
        
        return compiledScript.execute(context);
    }

    @Override
    public String getSourceText()
    {
        return scriptText;
    }

    @Override
    public boolean getUseStrictMode() {
        return jexlUseStrict;
    }

    @Override
    public void setUseStrictMode(boolean useStrictMode) {
        jexlUseStrict = useStrictMode;
    }

    @Override
    public boolean getUseDebugMode() {
        return jexlUseDebug;
    }

    @Override
    public void setUseDebugMode(boolean useDebugMode) {
        jexlUseDebug = useDebugMode;
    }

    @Override
    public boolean getSilentMode() {
        return jexlUseSilent;
    }

    @Override
    public void setUseSilentMode(boolean useSilentMode) {
        jexlUseSilent = useSilentMode;
    }

    /***
     * Reinitialize
     * @param reloadOptionsFromConfig
     */
    @Override
    public void reinit(boolean reloadOptionsFromConfig) {
       EngineSingletonHolder.reinit(reloadOptionsFromConfig);
    }


    private static final class EngineSingletonHolder {
        /**
         * non instantiable.
         */
        private EngineSingletonHolder() {
            initConfig();
        }

        /**
         * The JEXL engine singleton instance.
         */
        private static JexlEngine DEFAULT_ENGINE = new JexlBuilder().strict(jexlUseStrict).silent(jexlUseSilent).debug(jexlUseDebug).logger(LOG).cache(CACHE_SIZE).create();


        private static void initConfig() {
            DefaultConfigurationContextImpl config = PSContainerUtilsFactory.getConfigurationContextInstance();

            Path root = config.getRootDir();

            RxFileManager mgr = new RxFileManager(root.toString());

            String serverProps = mgr.getServerPropertiesFile();

            Properties props = null;

            try {
                props = RxFileManager.loadProperties(serverProps);
            } catch (IOException e) {
                LOG.warn("JEXL engine unable to load server.properties, default configuration will be used.");
            }


            if (props != null) {

                //Default to non strict for backward compatibility.
                jexlUseStrict = Boolean.parseBoolean(
                        props.getProperty("jexlUseStrict", "false"));

                jexlUseSilent = Boolean.parseBoolean(
                        props.getProperty("jexlUseSilent", "false"));

                jexlUseDebug = Boolean.parseBoolean(
                        props.getProperty("jexlUseDebug", "false"));

                CACHE_SIZE = Integer.parseInt(
                        props.getProperty("jexlCacheSize", "512"));

            }
        }

        protected static void reinit(boolean reloadOptionsFromConfig) {
            synchronized (DEFAULT_ENGINE)
            {
                //Reload from property files if set.
                if (reloadOptionsFromConfig)
                    initConfig();

                if (DEFAULT_ENGINE != null)
                    DEFAULT_ENGINE.clearCache();

                DEFAULT_ENGINE = new JexlBuilder().strict(jexlUseStrict).silent(jexlUseSilent).debug(jexlUseDebug).logger(LOG).cache(CACHE_SIZE).create();
            }
        }
    }


}

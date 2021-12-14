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
package com.percussion.cms.handlers;

import com.percussion.data.PSContentItemStatusExtractor;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSRuleListEvaluator;
import com.percussion.design.objectstore.PSCloneHandlerConfig;
import com.percussion.design.objectstore.PSCloneHandlerConfigSet;
import com.percussion.design.objectstore.PSConfigurationFactory;
import com.percussion.design.objectstore.PSContentItemStatus;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSObjectException;
import com.percussion.design.objectstore.PSProcessCheck;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.extension.PSExtensionException;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.config.PSConfigManager;
import com.percussion.server.config.PSServerConfigException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * Abstract base class for all clone handlers to maintain generic code
 * possibliy useful for all clone handlers.  
 */
public abstract class PSCloneHandler implements IPSCloneHandler
{
   /**
    * Convenience constructor see {#link PSCLoneHandler(IPSCopyHandler, String)}
    * for description.
    */
   public PSCloneHandler(IPSCopyHandler copyHandler) 
      throws PSServerConfigException, PSUnknownNodeTypeException
   {
      this(copyHandler, null);
   }
   
   /**
    * Constructs a new clone handler for the supplied copy handler.
    * 
    * @param copyHandler the copy handler to use, not <code>null</code>.
    * @param config the clone handler configuration to use, uses the
    *    default ("standard") if <code>null</code> or empty.
    * @throws PSServerConfigException if the clone handler configuration 
    *    could not be loaded successfully.
    * @throws PSUnknownNodeTypeException if the requested clone handler
    *    configuration is invalid.
    * @throws IllegalArgumentException if the supplied copy handler is
    *    <code>null</code>.
    */
   public PSCloneHandler(IPSCopyHandler copyHandler, String config)
      throws PSServerConfigException, PSUnknownNodeTypeException
   {
      if (copyHandler == null)
        throw new IllegalArgumentException("copyHandler cannot be null");
        
      if (config == null || config.trim().length() == 0)
         config = "standard";
        
      m_copyHandler = copyHandler;

      // load clone handler configuration
      PSCloneHandlerConfigSet configs = new PSCloneHandlerConfigSet(
            PSConfigManager.getInstance().getXMLConfig(
                  PSConfigurationFactory.CLONE_HANDLERS_CFG)
                  .getDocumentElement(), null, null);
      m_config = configs.getConfig(config);
      if (m_config == null)
         throw new PSServerConfigException(
            IPSServerErrors.UNKNOWN_CLONEHANDLER_CONFIGURATION, config);
   }

   /** see IPSCloneHandler for description */
   public PSLocator clone(PSLocator source, Iterator relationships,
      PSExecutionData data, PSCommandHandler ch)
      throws SQLException, PSObjectException, IOException
   {
      return clone(source, relationships, data, ch, null);
   }
   
   /**
    * This process check tests whether or not cloning is allowed for the 
    * current object.
    * This defaults to <code>true</code> if no "clone" process check is defined
    * for context "object".
    * 
    * @param data the execution data to operatoe on, not <code>null</code>.
    * @return <code>true</code> if the object can be cloned, <code>false</code>
    *    otherwise.
    * @throws PSNotFoundException if an extension used as part of the process
    *    check cannot be found.
    * @throws PSExtensionException if an extension executed as part of the
    *    process check failed.
    * @throws IllegalArgumentException if the supplied execution data is
    *    <code>null</code>.
    */
   public boolean canClone(PSExecutionData data)
      throws PSNotFoundException, PSExtensionException
   {
      PSProcessCheck check = m_config.getProcessCheck(OBJ_CLONE, "object");
      return doProcessCheck(check, data, true);
   }
   
   /**
    * Executes the supplied process check using the provided execution data.
    * If the supplied process check is <code>null</code>, the provided default 
    * value is returned.
    * 
    * @param check the process check to evaluate, may be <code>null</code> in
    *    which case the default value is returned.
    * @param data the execution data to operate on, not <code>null</code>.
    * @param defaultValue the default value to be returned if the supplied
    *    process check is <code>null</code>.
    * @return the result of the process check or the supplied default value
    *    if <code>null</code> was supplied for the process check.
    * @throws PSNotFoundException if an extension used as part of the process
    *    check cannot be found.
    * @throws PSExtensionException if an extension executed as part of the
    *    process check failed.
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public static boolean doProcessCheck(PSProcessCheck check, 
      PSExecutionData data, boolean defaultValue) 
      throws PSNotFoundException, PSExtensionException
   {
      if (data == null)
        throw new IllegalArgumentException("execution data cannot be null");
        
      if (check == null)
         return defaultValue;
         
      PSRuleListEvaluator evaluator = 
         new PSRuleListEvaluator(check.getConditions());
         
      return evaluator.isMatch(data);
   }
   
   /**
    * The process check name used in the context of objects that
    * specifies whether or not objects can be cloned.
    */
   public static String OBJ_CLONE = "obj_clone";
   
   /**
    * The process check name used in the context of relationships that
    * specifies whether or not relationships are cloned shallow.
    */
   public static String RS_CLONESHALLOW = "rs_cloneshallow";
   
   /**
    * The process check name used in the context of relationships that
    * specifies whether or not relationships are cloned deep.
    */
   public static String RS_CLONEDEEP = "rs_clonedeep";

   /**
    * The copy handler used to clone objects, initialized in ctor, never
    * <code>null</code> or changed after that.
    */   
   protected IPSCopyHandler m_copyHandler = null;
   
   /**
    * The clone handler configuration to use for this cloen handler. 
    * Initialized in ctor, never <code>null</code> or changed after that.
    */
   protected PSCloneHandlerConfig m_config = null;

   /**
    * The data extractor used to get the current revision from the current
    * execution context, initialized in ctor, never <code>null</code> or
    * changed after that.
    */
   protected PSContentItemStatusExtractor m_currentRevisionExtractor = 
      new PSContentItemStatusExtractor(
         new PSContentItemStatus("CONTENTSTATUS", "CURRENTREVISION"));
}

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
package com.percussion.services.assembly;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.percussion.cms.IPSConstants.SYS_PARAM_AA;
import static com.percussion.cms.IPSConstants.SYS_PARAM_ASM;
import static com.percussion.cms.IPSConstants.SYS_PARAM_ASSEMBLY_ITEM;
import static com.percussion.cms.IPSConstants.SYS_PARAM_CHARSET;
import static com.percussion.cms.IPSConstants.SYS_PARAM_INDEX;
import static com.percussion.cms.IPSConstants.SYS_PARAM_MIMETYPE;
import static com.percussion.cms.IPSConstants.SYS_PARAM_PARAMS;
import static com.percussion.cms.IPSConstants.SYS_PARAM_RX;
import static com.percussion.cms.IPSConstants.SYS_PARAM_SITE;
import static com.percussion.cms.IPSConstants.SYS_PARAM_TEMPLATE;
import static com.percussion.cms.IPSConstants.SYS_PARAM_TOOLS;
import static com.percussion.cms.IPSConstants.SYS_PARAM_USER;
import static com.percussion.cms.IPSConstants.SYS_PARAM_VARIABLES;

/**
 * A binding describes the pairing of a variable to bind with an expression
 * to evaluate. 
 * 
 * @author dougrand
 */
public interface IPSTemplateBinding
{
   /**
    * The variable to be bound.
    * @return the variable. Can be <code>null</code> or empty if the expression
    * should be executed and its returned value should be discarded.
    */
   String getVariable();
   
   /**
    * The expression to be evaluated
    * @return the expression, never <code>null</code> or empty
    */
   String getExpression();
   
   /**
    * Names of system variables.
    */
   Set<String> SYSTEM_VARIABLES = Collections.unmodifiableSet(
         new HashSet<String>(Arrays.asList(SYS_PARAM_RX,
                 SYS_PARAM_AA,
                 SYS_PARAM_ASM,
                 SYS_PARAM_ASSEMBLY_ITEM,
                 SYS_PARAM_CHARSET,
                 SYS_PARAM_INDEX,
                 SYS_PARAM_INDEX,
                 SYS_PARAM_MIMETYPE,
                 SYS_PARAM_PARAMS,
                 SYS_PARAM_SITE,
                 SYS_PARAM_TEMPLATE,
                 SYS_PARAM_VARIABLES,
                 SYS_PARAM_TOOLS,
                 SYS_PARAM_USER)));
}

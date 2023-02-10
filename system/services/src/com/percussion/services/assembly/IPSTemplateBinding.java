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

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
package com.percussion.pagemanagement.assembler;

import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.utils.jexl.IPSScript;
import com.percussion.utils.jexl.PSJexlEvaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PSJexlUtils
{

    public static PSJexlEvaluator getBindings(IPSAssemblyItem item) {
        return new PSJexlEvaluator(item.getBindings());
    }
    
    
    @SuppressWarnings("unchecked")
    public static <T> T bindExpression(PSJexlEvaluator eval, IPSScript exp, T value) 
        throws Exception {
        Object original = eval.evaluate(exp);
        T rvalue;
        if (original == null) {
            if (log.isTraceEnabled())
                log.trace("Binding expression: " + exp.getSourceText() + " to " + value);
            eval.bind(exp.getSourceText(), value);
            rvalue = value;
        }
        else {
            log.debug(exp.getSourceText() + " is already set to: " + original);
            if ( value != null && ! original.getClass().isInstance(value)) {
                throw new RuntimeException(exp.getSourceText() + " should be of type: " + value.getClass() +
                        "but is type: " + original.getClass());
            }
            rvalue = (T) original;
        }
        return rvalue;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T evalExpression(PSJexlEvaluator eval, IPSScript exp, @SuppressWarnings("unused") Class<T> k) throws Exception {
        return (T) eval.evaluate(exp);
    }
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(PSJexlUtils.class);
}

/**
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 */
package com.percussion.soln.utilities.rx.jexl;

import java.lang.reflect.Method;

import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.PSJexlUtilBase;

/**
 * Base class for JEXL functions.  
 * 
 * @author DavidBenua
 *
 */
@SuppressWarnings("unchecked")
public class SolnJexlBase extends PSJexlUtilBase {
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
       boolean first = true;
       StringBuilder s = new StringBuilder();
       Method[] methods = this.getClass().getMethods();
       for (int i = 0; i < methods.length; i++)
       {
          Method m = methods[i];
          if (m.isAnnotationPresent(IPSJexlMethod.class))
          {
             if (first)
                first = false;
             else
                s.append(",");
             s.append(m.getName() + "(");
             Class[] params = m.getParameterTypes();
             for (int j = 0; j < params.length; j++) {
             s.append(params[j].getName());
             if (j < (params.length - 1))
                 s.append(",");
             }
             s.append(")");
          }
       }
       return s.toString();
    }
}

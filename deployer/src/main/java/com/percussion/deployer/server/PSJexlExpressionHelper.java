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
package com.percussion.deployer.server;

import com.percussion.deployer.objectstore.PSApplicationIDTypeMapping;
import com.percussion.deployer.objectstore.idtypes.PSBindingIdContext;
import com.percussion.deployer.objectstore.idtypes.PSBindingParamIdContext;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * A helper class for managing the ids found in a jexl binding. This class has
 * a map of a binding index and binding Parameters. This helper is used 
 * when an id is transformed, record it so that next
 * substitution within the binding expression will be known precisely
 * 
 * @author vamsinukala
 */
public class PSJexlExpressionHelper
{
   /**
    * An occurence map keeps track of the occurence of this id and its new value
    * like this: It means for an oldValue=301, there are 3 occurences in a
    * JEXL expression, the first occurence has been replaced with 14, the second
    * occurence was replaced with 1001 and the third one isn't yet replaced.<br>
    *       "301"  --> <li>{0, "14"  } </li>
    *                  <li>{1, "1001"}</li>
    *                  <li>{2, ""    }</li>
    *
    */
   class PSJexlOccurence
   {
      /**
       * map of occurence(Integer) with its value String
       */
      private Map<Integer, String> m_values = new HashMap<Integer, String>();

      /**
       * Based on the occurence number, get its value
       * @see PSJexlOccurence 
       * @param ix
       * @return the new or old value
       */
      protected String getValue(int ix)
      {
         return m_values.get(ix);
      }

      /**
       * check to see if there is such an occurence
       * @param occurence
       * @return true if the occurence exists
       */
      protected boolean hasKey(int occurence)
      {
         return m_values.get(occurence) == null ? false : true;
      }

      /**
       * Add the value with its occurence, if it exists remove and add it back
       * @param occurence the occurence of this value in the jex expression
       * @param newValue may be empty or <code>null</code>
       */
      protected void addValue(int occurence, String newValue)
      {
         m_values.remove(occurence);
         m_values.put(occurence, newValue);
      }
   }

   /**
    * This class holds a map of all the ids with an occurence map.
    * Ex:
    *  <"301", Map<(int)occurence, String newVal>
    *  @see PSJexlOccurence
    */
   class PSJexlBindingParamOccurence
   {
      /**
       * a map to hold the old id value references in an expression
       * @see PSJexlOccurence
       */
      private Map<String, PSJexlOccurence> m_occur = new HashMap<String, PSJexlOccurence>();

      /**
       * with the given id, get all the occurences of that id in an jexl
       * expression
       * @param paramVal the old id value
       * @return the occurence map, may be <code>null</code>
       */
      protected PSJexlOccurence getOccurence(String paramVal)
      {
         return m_occur.get(paramVal);
      }

      /**
       * for a given id, add the occurence map
       * @param pValue old id value never <code>null</code>
       * @param jom the occurence map @see PSJexlOccurence
       */
      protected void addOccurence(String pValue, PSJexlOccurence jom)
      {
         if (StringUtils.isBlank(pValue))
            throw new IllegalArgumentException("id value may not be null");

         PSJexlOccurence curMap = m_occur.get(pValue);
         if (curMap == null)
            m_occur.put(pValue, jom);
         else
         {
            m_occur.remove(pValue);
            m_occur.put(pValue, jom);
         }
      }
   }

   /**
    * Get the jexl binding parameters
    * @param ix the binding index 
    * @return PSJexlBindingParamOccurence may be <code>null</code>
    */
   public PSJexlBindingParamOccurence getParamOccurenceFromIndex(int ix)
   {
      return m_paramMap.get(ix);
   }

   /**
    * This is built during deployment, and useful to update the parameter based
    * on its index, and its occurence that can uniquely identify the id
    * in a binding.
    * @param mapping the application id type mapping never <code>null</code>
    */
   public PSJexlExpressionHelper(PSApplicationIDTypeMapping mapping) {
      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      PSBindingParamIdContext paramCtx = (PSBindingParamIdContext) mapping
            .getContext();
      PSBindingIdContext bindingCtx = (PSBindingIdContext) paramCtx
            .getParentCtx();
      if (bindingCtx == null)
         throw new IllegalStateException("a param context exists without a "
               + "parent binding information");
      String pVal = paramCtx.getParam().getValueText();

      PSJexlBindingParamOccurence pMap = m_paramMap.get(bindingCtx.getIndex());
      if (pMap == null)
      {
         pMap = new PSJexlBindingParamOccurence();
         PSJexlOccurence occurMap = new PSJexlOccurence();
         occurMap.addValue(paramCtx.getOccurence(), "");
         pMap.addOccurence(pVal, occurMap);
         m_paramMap.put(bindingCtx.getIndex(), pMap);
      }
      else
      {
         PSJexlOccurence occurMap = pMap.getOccurence(pVal);
         if (occurMap == null)
         {
            occurMap = new PSJexlOccurence();
            occurMap.addValue(paramCtx.getOccurence(), "");
            pMap.addOccurence(pVal, occurMap);
         }
         else
         {
            if (!occurMap.hasKey(paramCtx.getOccurence()))
               occurMap.addValue(paramCtx.getOccurence(), "");
         }
      }
   }

   /**
    * Method to retrieve the new value in the binding expression
    * @param oldVal the old value that needs to be replaced in the binding
    *        never <code>null</code>
    * @param pCtx the param context that has the binding context, 
    *        never <code>null</code>
    * @param occurNum the nth occurence of this value, if it occurs once in the 
    *        expression, then it has "0" never <code>null</code>.
    * @return the new value that was stored in the jexl expression, may be
    *        <code>null</code>
    */
   public String getJexlBindingParamOccurenceValue(String oldVal,
         PSBindingParamIdContext pCtx, int occurNum)
   {
      if (pCtx == null)
         throw new IllegalArgumentException("param context may not be null");
      if (StringUtils.isBlank(oldVal))
         throw new IllegalArgumentException("old val may not be null or empty");
      if (pCtx.getParentCtx() == null)
         throw new IllegalStateException(
               "jexl param context must have a parent binding context");

      PSJexlBindingParamOccurence pMap = getParamOccurenceFromIndex(((PSBindingIdContext) pCtx
            .getParentCtx()).getIndex());
      if (pMap == null)
         return null;
      PSJexlOccurence occurMap = pMap.getOccurence(oldVal);
      if (occurMap != null && occurMap.hasKey(pCtx.getOccurence()))
         return occurMap.getValue(pCtx.getOccurence());
      return null;
   }

   /**
    * Helper method to update the PSJexlBindingParamOccurence with the new value
    * Locate the occurence  based on the old value and if the occurence exists,
    * update with the new value.
    * @param oldVal the old id never <code>null</code>
    * @param newVal the new id never <code>null</code>
    * @param pCtx the param context never <code>null</code>
    */
   public void updateBindingParam(String oldVal, String newVal,
         PSBindingParamIdContext pCtx)
   {
      if (pCtx == null)
         throw new IllegalArgumentException("param context may not be null");
      if (StringUtils.isBlank(oldVal))
         throw new IllegalArgumentException("old val may not be null or empty");
      if (pCtx.getParentCtx() == null)
         throw new IllegalStateException(
               "jexl param context must have a parent binding context");

      PSJexlBindingParamOccurence pOccur = getParamOccurenceFromIndex(((PSBindingIdContext) pCtx
            .getParentCtx()).getIndex());
      if (pOccur == null)
         return;

      PSJexlOccurence occurMap = pOccur.getOccurence(oldVal);
      if (occurMap != null && occurMap.hasKey(pCtx.getOccurence()))
         occurMap.addValue(pCtx.getOccurence(), newVal);
   }

   /**
    * a parameter map based on the binding index.
    */
   private Map<Integer, PSJexlBindingParamOccurence> m_paramMap = new HashMap<Integer, PSJexlBindingParamOccurence>();
}

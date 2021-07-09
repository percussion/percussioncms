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

package com.percussion.server.cache;

import com.percussion.data.PSExecutionData;
import com.percussion.data.PSRuleListEvaluator;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSRule;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.extension.PSExtensionException;
import com.percussion.server.PSRequest;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

/**
 * This class is used to determine if an optional key is to be included.  The
 * keys and their inclusion rules are defined in PSKeyRules.xml, which is
 * loaded as a resource from the package in which this class is defined.  This 
 * resource file must conform to the following DTD:
 * 
 * <pre><code>
 * &lt;!-- Include the shared definitions -->
 * &lt;!ENTITY % BasicObjects SYSTEM "sys_BasicObjects.dtd">
 * 
 * &lt;!-- 
 *    A set of rules, one for each cache key that is optional.
 * -->
 * <ELEMENT PSXKeyRules (KeyRule*)>
 * 
 * &lt;!-- 
 *    A rule for a single key used to determine if the key value should be used
 *    when an item is cached.  If the rule resolves to <code>true</code>, then
 *    the keys value will be included in the keys when the item is cached.  
 *    Otherwise the item is cache without regard for this keys value.  A list of
 *    rules may be supplied, and they are evaluated in order using each rule's
 *    boolean attribute to determine the logical operand for evaluating the 
 *    following rule.  The boolean attribute of the last rule is ignored.  The 
 *    result of each rule evaluated with the logical operands determines the 
 *    final result.
 *    
 *    Attributes:
 *       keyName - the name of the key to which this rule applies.   May not be
 *          empty.
 * -->
 * &lt;ELEMENT KeyRule (PSXRule+)>
 * &lt;ATTLIST KeyRule 
 *    keyName CDATA #REQUIRED
 * >
 * </code></pre>
 */
public class PSKeyRules
{
   /**
    * Loads the <code>PSKeyRules.xml</code> resource file and intializes
    * all required runtime rule evaluators.
    * 
    * @param sourceNode The element containing the <code>PSXKeyRules</code>
    * defintion.  May not be <code>null</code>.  See class documentation for
    * more information.
    * 
    * @throws IllegalArgumentException if <code>sourceNode</code> is 
    * <code>null</code>.
    * @throws PSUnknownNodeTypeException if the document does not contain the
    * expected format.
    * @throws PSNotFoundException if a specified extension cannot be found.
    * @throws PSExtensionException If any errors occur while preparing a
    * runnable version of an extension.
    */
   public PSKeyRules(Element sourceNode) throws PSUnknownNodeTypeException, 
      PSNotFoundException, PSExtensionException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");
      
      // walk keys and create evaluators
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      // validate the root
      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
      
      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
         
      // walk each KeyRule element found
      tree.setCurrent(sourceNode);
      Element keyRuleEl = tree.getNextElement(KEY_RULE_ELEM, firstFlags);
      while (keyRuleEl != null)
      {
         // key the name of the key
         String keyName = keyRuleEl.getAttribute(KEY_NAME_ATTR);
         if (keyName.trim().length() == 0)
         {
            Object[] args = { XML_NODE_NAME, KEY_NAME_ATTR, keyName};
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
         
         // get the list of the rules
         List ruleList = new ArrayList();   
         Element ruleEl = tree.getNextElement(PSRule.XML_NODE_NAME, 
            firstFlags);
            
         // must have at least one rule
         if (ruleEl == null)
         {
            Object[] args = { XML_NODE_NAME, PSRule.XML_NODE_NAME, "null"};
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         
         // add each rule to the list
         while (ruleEl != null)
         {
            PSRule rule = new PSRule(ruleEl, null, null);
            ruleList.add(rule);
            ruleEl = tree.getNextElement(PSRule.XML_NODE_NAME, nextFlags);
         }
         
         // create the rule list evaluator and add to map under keyname
         PSRuleListEvaluator eval = new PSRuleListEvaluator(
            ruleList.iterator());
         m_evaluators.put(keyName, eval);
         
         // reset current node to the key rule and get the next one
         tree.setCurrent(keyRuleEl);
         keyRuleEl = tree.getNextElement(KEY_RULE_ELEM, nextFlags);
      }
   }
   
   /**
    * Check rules for the supplied key to determine if the key should be 
    * included when caching the item represented by the supplied request.
    * 
    * @param key The key to check rules for, may not be <code>null</code> or 
    * empty.  
    * @param req The request to use to determine if the specified key should be
    * included.  May not be <code>null</code>.
    * 
    * @return <code>true</code> if the rules for the specified <code>key</code>
    * resolve to <code>true</code> or if no rules are supplied for that
    * <code>key</code>, <code>false</code> otherwise.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   boolean isIncluded(String key, PSRequest req)
   {
      if (key == null || key.trim().length() == 0)
         throw new IllegalArgumentException("key may not be null or empty");
      
      if (req == null)
         throw new IllegalArgumentException("req may not be null");
      
      boolean result = true;
      
      PSRuleListEvaluator eval = (PSRuleListEvaluator)m_evaluators.get(key);
      if (eval != null)
      {
         PSExecutionData data = new PSExecutionData(null, null, req);
         result = eval.isMatch(data);
      }
      return result;
   }
 
   /**
    * Map of evaluators for each key name.  Key is the name of a cache key as a
    * <code>String</code>, value is a <code>PSRuleListEvaluator</code> for that
    * key.  Never <code>null</code> or modified after construction.
    */  
   private Map m_evaluators = new HashMap();
   
   // private xml elements and attributes
   private static final String XML_NODE_NAME = "PSXKeyRules";
   private static final String KEY_RULE_ELEM = "KeyRule";
   private static final String KEY_NAME_ATTR = "keyName";
}

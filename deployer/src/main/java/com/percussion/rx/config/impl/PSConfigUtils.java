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
package com.percussion.rx.config.impl;

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionCallSet;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.design.objectstore.PSReplacementValueFactory;
import com.percussion.design.objectstore.PSRule;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionManager;
import com.percussion.extension.PSExtensionRef;
import com.percussion.rx.config.PSConfigException;
import com.percussion.rx.design.IPSDesignModel;
import com.percussion.rx.design.IPSDesignModelFactory;
import com.percussion.rx.design.PSDesignModelFactoryLocator;
import com.percussion.rx.design.impl.PSLocationSchemeModel;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.util.PSCharSets;
import com.percussion.util.PSCollection;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.string.PSPatternMatch;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.CGLIBProxyTypePermission;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PSConfigUtils
{

   private static void initSecurityFramework(XStream stream){
      stream.addPermission(NoTypePermission.NONE);
      stream.addPermission(NullPermission.NULL);
      stream.addPermission(PrimitiveTypePermission.PRIMITIVES);
      stream.addPermission(CGLIBProxyTypePermission.PROXIES);
      stream.allowTypeHierarchy(Collection.class);
      stream.allowTypeHierarchy(Set.class);
      stream.allowTypeHierarchy(List.class);
      stream.allowTypeHierarchy(String.class);
      stream.allowTypesByWildcard(new String[] {
              "com.percussion.**"
      });
   }
   /**
    * Convenient method to execute the supplied url and return the resulting
    * document.
    * 
    * @param url The URL to execute must not be <code>null</code>.
    * @param extraParams Extra parameters to use, may be <code>null</code>.
    * @param applyStyleSheet <code>true</code> if returns the merged result
    * document.
    * @return The resulting xml document. Never <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public static Document getDocument(String url, Map extraParams,
         boolean applyStyleSheet)
   {
      if (url == null)
         throw new IllegalArgumentException("url must not be null");

      PSRequest origReq = (PSRequest) PSRequestInfo
            .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      String origUser = (String) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_USER);
      Document doc = null;
      try
      {
         setRequestToInternalUser(origReq);
         PSRequest req = (PSRequest) PSRequestInfo
               .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);

         IPSRequestContext reqCtxt = new PSRequestContext(req);
         IPSInternalRequest ireq = reqCtxt.getInternalRequest(url,
               extraParams, true);
         if (ireq == null)
         {
            String msg = "Failed to get the internal request for url ({0}).";
            Object[] args = { url };
            throw new PSConfigException(MessageFormat.format(msg, args));
         }
         if (applyStyleSheet)
         {
            byte[] result = ireq.getMergedResult();
            doc = PSXmlDocumentBuilder.createXmlDocument(
                  new ByteArrayInputStream(result), false);
         }
         else
         {
            doc = ireq.getResultDoc();
         }
      }
      catch (Exception e)
      {
         String msg = "Failed to get the document for url ({0}).";
         Object[] args = { url };
         throw new PSConfigException(MessageFormat.format(msg, args), e);
      }
      finally
      {
         resetRequestToOriginal(origReq, origUser);
      }
      return doc;
   }

   /**
    * Helper method to set the request to internal user.
    * 
    * @param origReq The original request used to determine if the request info
    * needs to be reset.
    */
   @SuppressWarnings("unchecked")
   public static void setRequestToInternalUser(PSRequest origReq)
   {
      try
      {
         PSRequestInfo.resetRequestInfo();
      }
      catch (Exception ignore)
      {
         // ignore
      }
      PSRequest req = PSRequest.getContextForRequest();
      PSRequestInfo.initRequestInfo((Map) null);
      PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_PSREQUEST, req);
   }

   /**
    * Helper method to reset the request to original request.
    * 
    * @param origReq The original request to which the request info will be set.
    * @param userName The original user name
    */
   @SuppressWarnings("unchecked")
   public static void resetRequestToOriginal(PSRequest origReq, String userName)
   {
      PSRequestInfo.resetRequestInfo();
      PSRequestInfo.initRequestInfo((Map) null);
      PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_PSREQUEST, origReq);
      PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_USER, userName);
   }

   /**
    * Gets the Design Object names from the given names.
    * 
    * @param objNames the names in question. It must be either a string or a
    * list of strings. It may be a list of actual Design Object names, which
    * will be simply returned; or it may be a pattern of the lookup names, may
    * contains wild card ("*").
    * @param model the model of the Design Object, never <code>null</code>.
    * @param propName the name of the property, never blank.
    * 
    * @return a list of Design Object names. It may be <code>null</code> if
    * there is no matching name of the Design Object.
    * 
    * @see PSPatternMatch
    */
   @SuppressWarnings("unchecked")
   public static Collection<String> getObjectNames(Object objNames,
         IPSDesignModel model, String propName)
   {
      if (StringUtils.isBlank(propName))
         throw new PSConfigException("propName may not be null or empty.");

      if (objNames instanceof String)
      {
         Collection<String> allNames = model.findAllNames();
         Collection<String> names = PSPatternMatch.matchedStrings(
               (String) objNames, allNames);
         if (names.size() == 0)
            return null;

         objNames = names;
      }
      else if (!(objNames instanceof Collection))
      {
         throw new PSConfigException("The design object \"" + propName
               + "\" property must be a \"Collection\" or a String type.");
      }

      return (Collection<String>) objNames;
   }

   /**
    * Gets the Location Scheme model.
    * 
    * @return the model, never <code>null</code>.
    */
   public static PSLocationSchemeModel getSchemeModel()
   {
      IPSDesignModelFactory factory = PSDesignModelFactoryLocator
            .getDesignModelFactory();
      return (PSLocationSchemeModel) factory
            .getDesignModel(PSTypeEnum.LOCATION_SCHEME);
   }

   /**
    * Returns the Context model.
    * 
    * @return the model, never <code>null</code>.
    */
   public static IPSDesignModel getContextModel()
   {
      IPSDesignModelFactory factory = PSDesignModelFactoryLocator
            .getDesignModelFactory();
      return factory.getDesignModel(PSTypeEnum.CONTEXT);
   }

   /**
    * Returns the Template model.
    * 
    * @return the model, never <code>null</code>.
    */
   public static IPSDesignModel getTemplateModel()
   {
      IPSDesignModelFactory factory = PSDesignModelFactoryLocator
            .getDesignModelFactory();
      return factory.getDesignModel(PSTypeEnum.TEMPLATE);
   }

   /**
    * Returns the Content Type model.
    * 
    * @return the model, never <code>null</code>.
    */
   public static IPSDesignModel getContentTypeModel()
   {
      IPSDesignModelFactory factory = PSDesignModelFactoryLocator
            .getDesignModelFactory();
      return factory.getDesignModel(PSTypeEnum.NODEDEF);
   }

   /**
    * Gets the parameter names for the given FQN of the java extension.
    * 
    * @param extFQN the FQN java extension, never <code>null</code> or empty.
    * 
    * @return a list of parameter names, never <code>null</code>, may be
    * empty.
    */
   @SuppressWarnings("unchecked")
   public static List<String> getExtensionParameterNames(String extFQN)
   {
      if (StringUtils.isBlank(extFQN))
         throw new IllegalArgumentException("extFQN may not be null or empty.");

      try
      {
         PSExtensionRef ref = new PSExtensionRef(extFQN);
         IPSExtensionManager emgr = PSServer.getExtensionManager(null);
         IPSExtensionDef def = emgr.getExtensionDef(ref);
         List<String> paramNames = new ArrayList<String>();
         CollectionUtils.addAll(paramNames, def.getRuntimeParameterNames());
         return paramNames;
      }
      catch (Exception e)
      {
         throw new PSConfigException(e); // not possible
      }
   }

   /**
    * Loads an object from the specified file.
    * 
    * @param f the file contains the object. It may not be <code>null</code>
    * or empty.
    * 
    * @return the loaded object. It may be <code>null</code> if the file does
    * not exist.
    */
   public static Object loadObjectFromFile(File f)
   {
      if (!f.exists())
         return null;

      try
      {
         XStream xs = new XStream(new DomDriver());
         initSecurityFramework(xs);

         String str = FileUtils.readFileToString(f, PSCharSets.rxJavaEnc());
         return xs.fromXML(str);
      }
      catch (Exception e)
      {
         String errMsg = "Failed to load: \"" + f.getAbsolutePath() + "\".";
         ms_log.error(errMsg, e);
         throw new PSConfigException(errMsg, e);
      }
   }

   /**
    * Saves the specified object into the given file.
    * 
    * @param obj the to be saved object, never <code>null</code>.
    * @param f the file to save the object to, never <code>null</code>. The
    * file will be created if not exist.
    */
   public static void saveObjectToFile(Object obj, File f)
   {
      // create parent directory if needed
      if (!f.exists())
      {
         File parent = f.getParentFile();
         if (!parent.exists())
         {
            if (!parent.mkdirs())
            {
               String errMsg = "Failed to create dir: \""
                     + parent.getAbsolutePath() + "\".";
               ms_log.error(errMsg);
               throw new PSConfigException(errMsg);
            }
         }
      }

      // save the object to the file
      try
      {
         XStream xs = new XStream(new DomDriver());
         initSecurityFramework(xs);

         String str = xs.toXML(obj);
         FileUtils.writeStringToFile(f, str, PSCharSets.rxJavaEnc());
      }
      catch (Exception e)
      {
         String errMsg = "Failed to save object to: \"" + f.getAbsolutePath()
               + "\".";
         ms_log.error(errMsg, e);
         throw new PSConfigException(errMsg, e);
      }
   }

   /**
    * Prepares a collection of conditional rules represent for the condtion
    * configuration. The condition configuration is list of either Conditional
    * rule or Extension rule.
    * 
    * @param conditonConfig object corresponding to the condition configuration.
    * @return The collection of rules, never <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public static PSCollection prepareConditions(Object conditonConfig)
   {
      if (!(conditonConfig instanceof List))
         throw new PSConfigException("The value of \"" + "conditions"
               + "\" property must be a \"List\" type");
      PSCollection condCol = new PSCollection(PSRule.class);
      List<Map<String, Object>> conditions = (List<Map<String, Object>>) conditonConfig;
      for (Map<String, Object> map : conditions)
      {
         condCol.add(createRule(map));
      }
      return condCol;
   }
   
   public static List<Map<String, Object>> getCondtionsDef(Iterator conditionsIter)
   {
      if (conditionsIter == null)
         throw new IllegalArgumentException("conditionsIter must not be null");
      List<Map<String, Object>> conds = new ArrayList<Map<String, Object>>();
      while(conditionsIter.hasNext())
      {
         conds.add(getRuleDef((PSRule)conditionsIter.next()));
      }
      return conds;
   }

   /**
    * Creates a rule for the supplied data of map of String and Object.
    * 
    * @param map must not be <code>null</code>.
    * @return rule corresponding to the supplied data never <code>null</code>.
    * Throws <code>PSConfigException</code> if the data is not valid.
    */
   @SuppressWarnings("unchecked")
   public static PSRule createRule(Map<String, Object> map)
   {
      if (map == null)
         throw new PSConfigException(
               "map must not be null for creating the rule.");

      PSRule rule = null;
      String type = (String) map.get(PROP_TYPE);
      if (StringUtils.isBlank(type))
      {
         throw new PSConfigException("The required property " + PROP_TYPE
               + " is missing for the supplied condition.");
      }
      // Type must be either Extension or Conditional
      if (type.equalsIgnoreCase(TYPE_EXTENSION))
      {
         String name = (String) map.get(PROP_NAME);
         if (StringUtils.isBlank(name))
            throw new PSConfigException("The required property " + PROP_NAME
                  + " is missing for the supplied condition.");
         List<String> extensionParams = (List<String>) map
               .get(PROP_EXTENSION_PARAMS);
         rule = createExtensionRule(name, extensionParams);
      }
      else if (type.equalsIgnoreCase(TYPE_CONDITIONAL))
      {
         List<Map<String, String>> rules = (List<Map<String, String>>) map
               .get(PROP_RULES);
         if (rules == null)
            throw new PSConfigException("The required property " + PROP_RULES
                  + " is missing for the supplied condition.");
         rule = createConditionalRule(rules);
      }
      else
      {
         String msg = "The supplied type of condition ({0}) is invalid. "
               + "Valid types are \"" + TYPE_CONDITIONAL + "\" and \""
               + TYPE_EXTENSION + "\"";
         Object[] args = { type };
         throw new PSConfigException(MessageFormat.format(msg, args));
      }

      // Boolean is an optional parameter if not supplied in the configuration
      // it is defaulted to "and"
      String op = (String) map.get(PROP_BOOLEAN);
      op = StringUtils.isBlank(op) ? "and" : op;
      if (!(op.equalsIgnoreCase("and") || op.equalsIgnoreCase("or")))
      {
         throw new PSConfigException("The value of \"" + "operators"
               + "\" property must be a either \"and\" or \"or\".");
      }

      int operator = op.equalsIgnoreCase("and") ? PSRule.BOOLEAN_AND
            : PSRule.BOOLEAN_OR;
      rule.setOperator(operator);

      return rule;
   }

   private static Map<String, Object> getRuleDef(PSRule rule)
   {
      Map<String, Object> map = new HashMap<String, Object>();
      if(rule.isExtensionSetRule())
      {
         map.put(PROP_TYPE, TYPE_EXTENSION);
         PSExtensionCallSet callSet = rule.getExtensionRules();
         PSExtensionCall call = (PSExtensionCall) callSet.iterator().next();
         map.putAll(getExtensionCallDef(call, PROP_NAME));
      }
      else
      {
         map.put(PROP_TYPE, TYPE_CONDITIONAL);
         map.put(PROP_RULES, createConditionalRuleDef(rule.getConditionalRules()));
      }
      int op = rule.getOperator();
      String oper = op == PSRule.BOOLEAN_AND?"AND":"OR";
      map.put(PROP_BOOLEAN, oper);
      return map;
   }
   
   private static List<Map<String, String>> createConditionalRuleDef(Iterator rulesIter)
   {
      List<Map<String, String>> rulesDef = new ArrayList<Map<String,String>>();
      while(rulesIter.hasNext())
      {
         Map<String, String> map = new HashMap<String, String>();
         PSConditional condRule = (PSConditional) rulesIter.next();
         String var1 = condRule.getVariable().getValueDisplayText();
         String var2 = condRule.getValue().getValueDisplayText();
         String op = condRule.getOperator();
         if(op.equals(PSConditional.OPTYPE_NOTEQUALS))
            op = "!=";
         String bool = condRule.getBoolean();
         map.put(PROP_VARIABLE1, var1);
         map.put(PROP_VARIABLE2, var2);
         map.put(PROP_OPERATOR, op);
         map.put(PROP_BOOLEAN, bool);
         rulesDef.add(map);
      }
      return rulesDef;
   }
   
   /**
    * Creates a conditional rule for the supplied list of rules. Each object in
    * the supplied list must be a map of name value pairs corresponding to
    * conditional rule.
    * 
    * @param rules List of Map corresponding to conditional rule.
    * @return PSRule
    */
   private static PSRule createConditionalRule(List<Map<String, String>> rules)
   {
      PSCollection conds = new PSCollection(PSConditional.class);
      for (Map<String, String> rule : rules)
      {
         String name = StringUtils.defaultString(rule.get(PROP_VARIABLE1));
         String value = StringUtils.defaultString(rule.get(PROP_VARIABLE2));
         String op = StringUtils.defaultString(rule.get(PROP_OPERATOR));
         if(op.equals("!="))
            op = PSConditional.OPTYPE_NOTEQUALS;
         String bool = StringUtils.defaultString(rule.get(PROP_BOOLEAN));
         IPSReplacementValue nameRv = createReplacementValue(name);
         IPSReplacementValue valueRv = null;
         if (StringUtils.isNotBlank(value))
         {
            valueRv = createReplacementValue(value);
         }
         bool = StringUtils.isBlank(bool) ? PSConditional.OPBOOL_AND : bool
               .toUpperCase();
         if (!(bool.equalsIgnoreCase("AND") || bool.equalsIgnoreCase("OR")))
         {
            throw new PSConfigException("The value of \"" + "boolean"
                  + "\" property must be a either \"AND\" or \"OR\".");
         }
         PSConditional cond = new PSConditional(nameRv, op, valueRv, bool);
         conds.add(cond);
      }
      return new PSRule(conds);
   }

   
   /**
    * Creates the extension rule for the supplied map of fully qualified
    * extension name and optional extension parameter entries.
    * 
    * @param name assumed not blank.
    * @return Extension rule, never <code>null</code>.
    */
   private static PSRule createExtensionRule(String name, List<String> params)
   {
      PSExtensionCallSet callSet = new PSExtensionCallSet();
      PSExtensionCall extCall = createExtensionCall(name, params,
            "com.percussion.extension.IPSUdfProcessor");
      callSet.add(extCall);
      return new PSRule(callSet);
   }

   public static Map<String, Object> getExtensionCallDef(PSExtensionCall call,
         String propName)
   {
      Map<String, Object> map = new HashMap<String, Object>();
      map.put(propName, call.getExtensionRef().getFQN());
      List<String> params = new ArrayList<String>();
      PSExtensionParamValue[] paramVals = call.getParamValues();
      for (PSExtensionParamValue value : paramVals)
      {
         params.add(value.getValue().getValueDisplayText());
      }
      map.put(PROP_EXTENSION_PARAMS, params);
      return map;
   }
   
   /**
    * Creates an extension call for the given name of the extension and list of
    * optional params.
    * 
    * @param name The fully qualified name of the extension, must not be blank.
    * @param params Optional params list, if not <code>null</code> the values
    * must represent a <code>IPSReplacementValue</code>
    * @param interfaceName, may be <code>null</code> or empty. If
    * <code>null</code> or empty then just checks whether an extension exists
    * with the supplied name, otherwise checks the extensions that implements
    * this interface.
    * @return PSExtensionCall, never <code>null</code>.
    */
   public static PSExtensionCall createExtensionCall(String name,
         List<String> params, String interfaceName)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name must not be blank");
            
      PSExtensionRef extRef = null;
      PSExtensionManager mgr = (PSExtensionManager) PSServer
            .getExtensionManager(null);
      try
      {
         PSExtensionRef tempRef = new PSExtensionRef(name);
         if (StringUtils.isNotBlank(interfaceName))
         {
            Iterator iterator = mgr.getExtensionNames(null, null,
                  interfaceName, tempRef.getExtensionName());
            while (iterator.hasNext())
            {
               if (tempRef.equals((PSExtensionRef) iterator.next()))
               {
                  extRef = tempRef;
                  break;
               }
            }
         }
         else
         {
            if (mgr.exists(tempRef))
               extRef = tempRef;
         }
      }
      catch (PSExtensionException e)
      {
         e.printStackTrace();
      }
      if (extRef == null)
      {
         String msg = "The supplied extension name ({0}) does not "
               + "correspond to a valid extension in the system.";
         Object[] args = { name };
         throw new PSConfigException(MessageFormat.format(msg, args));
      }
      List<PSExtensionParamValue> paramValues = new ArrayList<PSExtensionParamValue>();
      if (params != null)
      {
         for (String param : params)
         {
            IPSReplacementValue rv = createReplacementValue(param);
            paramValues.add(new PSExtensionParamValue(rv));
         }
      }
      PSExtensionCall extCall = new PSExtensionCall(extRef, paramValues
            .toArray(new PSExtensionParamValue[paramValues.size()]));

      return extCall;
   }

   /**
    * Creates a replacement value for the given name. If the name is
    * <code>null</code> or empty or doesn't start with "psx" then a
    * <code>PSTextLiteral</code> is returned.
    * 
    * @param name The string representation of the replacement value.
    * @return The replacement value, never <code>null</code>, the underlying
    * creation may throw a <code>RunTimeException</code> if the string is not
    * valid for creating a replacement value.
    */
   private static IPSReplacementValue createReplacementValue(String name)
   {
      name = StringUtils.defaultString(name);
      if (!name.toLowerCase().startsWith("psx"))
      {
         name = PSTextLiteral.ms_NodeType + "/" + name;
      }
      IPSReplacementValue rv = PSReplacementValueFactory
            .getReplacementValueFromString(name);
      return rv;
   }

   /**
    * Creates a boolean conditional based on the supplied flag.
    * 
    * @param flag if <code>true</code> creates a 1=1 condition otherwise 1=2
    * condition
    * @return A <code>true</code> or <code>false</code> conditional never
    * <code>null</code>.
    */
   public static PSConditional createBooleanCondition(boolean flag)
   {
      IPSReplacementValue name = new PSTextLiteral("1");
      IPSReplacementValue val = flag ? new PSTextLiteral("1")
            : new PSTextLiteral("2");
      return new PSConditional(name, "=", val);
   }

   /**
    * Utility method to reverse the key valuse of a map.
    * @param map The input map that needs to be reversed, assumed not <code>null</code>.
    * @return Reversed map, never <code>null</code>, may be empty.
    */
   public static Map getReverseMap(Map<String,String> map)
   {
      Map<String,String> revMap = new HashMap<String,String>();
      Iterator i = map.entrySet().iterator();
      while(i.hasNext())
      {
         Map.Entry entry = (Map.Entry) i.next();
         revMap.put((String)entry.getValue(), (String)entry.getKey());
      }      
      return revMap;
   }

   /**
    * The logger for this class
    */
   static Log ms_log = LogFactory.getLog("PSConfigUtils");

   // Constants for property names
   public static final String PROP_NAME = "name";

   public static final String PROP_EXTENSION_PARAMS = "extensionParams";

   private static final String PROP_TYPE = "type";

   private static final String PROP_RULES = "rules";

   private static final String PROP_BOOLEAN = "boolean";

   private static final String PROP_VARIABLE2 = "variable2";

   private static final String PROP_VARIABLE1 = "variable1";

   private static final String PROP_OPERATOR = "operator";

   private static final String TYPE_CONDITIONAL = "Conditional";

   private static final String TYPE_EXTENSION = "Extension";

}

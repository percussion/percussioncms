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
package com.percussion.services.assembly.jexl;

import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSItemFieldMeta;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.cms.objectstore.server.PSServerItem;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.server.PSRequest;
import com.percussion.services.content.IPSContentService;
import com.percussion.services.content.PSContentServiceLocator;
import com.percussion.services.content.data.PSKeyword;
import com.percussion.services.content.data.PSKeywordChoice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Methods that enable the use of keywords when building assembly output.
 * 
 * @author dougrand
 */
public class PSKeywordUtils extends PSJexlUtilBase
{
   /**
    * Logger for this class
    */
   private static final Logger ms_log = LogManager.getLogger(PSKeywordUtils.class);

   /**
    * Retrieve the named keyword and output the available choices as html
    * selection elements
    * 
    * @param keywordname the name of the keyword, never <code>null</code> or
    *           empty
    * @param currentchoice the current value to mark selected, may be
    *           <code>null</code>
    * @return html select elements
    */
   @IPSJexlMethod(description = "Retrieve the named keyword and output the available choices as html "
         + "selection elements", params =
   {
         @IPSJexlParam(name = "keywordname", type = "String", description = "the name of the keyword"),
         @IPSJexlParam(name = "currentchoice", type = "String", description = "the current chosen value")}, returns = "html select elements")
   public String keywordSelectChoices(String keywordname, String currentchoice)
   {
      if (StringUtils.isBlank(keywordname))
      {
         throw new IllegalArgumentException(
               "keywordname may not be null or empty");
      }
      List<String[]> choices = keywordChoices(keywordname);
      StringBuilder b = new StringBuilder();
      for (String[] kc : choices)
      {
         String label = kc[0];
         String value = kc[1];
         b.append("<OPTION value='");
         b.append(value);
         b.append('\'');
         if (value.equals(currentchoice))
         {
            b.append(" selected='true'");
         }
         b.append(">");
         b.append(label);
         b.append("</OPTION>\n");
      }
      return b.toString();
   }

   /**
    * Get the keyword choices for the named keyword
    * 
    * @param keywordname the named keyword, never <code>null</code> or empty
    * @return a list of results, never <code>null</code>. Each result is a
    *         two element array. The first element of this array is the label of
    *         the keyword choice, the second is the value of the keyword choice.
    */
   @IPSJexlMethod(description = "Get the keyword choices for the named keyword", params =
   {@IPSJexlParam(name = "keywordname", type = "String", description = "the name of the keyword")}, returns = "a list of results, never null. Each result is a two element "
         + "array. The first element of this array is the label of the "
         + "keyword choice, the second is the value of the keyword choice.")
   public List<String[]> keywordChoices(String keywordname)
   {
      List<String[]> rval = new ArrayList<>();
      IPSContentService csvc = PSContentServiceLocator.getContentService();
      List<PSKeyword> keywords = csvc.findKeywordsByLabel(keywordname, "sequence");
      if (keywords == null || keywords.size() == 0)
      {
         throw new RuntimeException("Found no keyword matching " + keywordname);
      }
      if (keywords.size() > 1)
      {
         throw new RuntimeException("Found more than one keyword matching "
               + keywordname);
      }
      PSKeyword k = keywords.get(0);
      for (PSKeywordChoice c : k.getChoices())
      {
         rval.add(new String[]
         {c.getLabel(), c.getValue()});
      }
      return rval;
   }

   /**
    * Retrieve the named keyword and return the label associated with the given
    * value
    * 
    * @param keywordname the name of the keyword, never <code>null</code> or
    *           empty
    * @param keywordvalue the value, never <code>null</code> or empty
    * @return the value, or an empty string if no value is found
    */
   @IPSJexlMethod(description = "Retrieve the named keyword value and returns the label", params =
   {
         @IPSJexlParam(name = "keywordname", type = "String", description = "the name of the keyword"),
         @IPSJexlParam(name = "keywordvalue", type = "String", description = "the value")}, returns = "the label")
   public String getLabel(String keywordname, String keywordvalue)
   {
      if (StringUtils.isBlank(keywordname))
      {
         throw new IllegalArgumentException(
               "keywordname may not be null or empty");
      }
      if (StringUtils.isBlank(keywordvalue))
      {
         throw new IllegalArgumentException(
               "keywordvalue may not be null or empty");
      }
      List<String[]> choices = keywordChoices(keywordname);

      for (String[] kc : choices)
      {
         String label = kc[0];
         String value = kc[1];
         if (value.equals(keywordvalue))
         {
            return label;
         }
      }
      return "";
   }

   /**
    * Retrieve the named keyword and return the label associated with the given
    * value
    * 
    * @param keywordname the name of the keyword, never <code>null</code> or
    *           empty
    * @param keywordvalue the value, never <code>null</code> or empty
    * @param locale the locale, never <code>null</code> or empty
    * @return the value, or an empty string if no value is found
    */
   @IPSJexlMethod(description = "Retrieve the named keyword value for the given locale and returns the label", params =
   {
         @IPSJexlParam(name = "keywordname", type = "String", description = "the name of the keyword"),
         @IPSJexlParam(name = "keywordvalue", type = "String", description = "the value"),
         @IPSJexlParam(name = "locale", type = "String", description = "the locale")}, returns = "the label")
   public String getLabel(String keywordname, String keywordvalue, String locale)
   {
      if (StringUtils.isBlank(keywordname))
      {
         throw new IllegalArgumentException(
               "keywordname may not be null or empty");
      }
      if (StringUtils.isBlank(keywordvalue))
      {
         throw new IllegalArgumentException(
               "keywordvalue may not be null or empty");
      }
      IPSContentService csvc = PSContentServiceLocator.getContentService();
      List<PSKeyword> keywords = csvc.findKeywordsByLabel(keywordname, null);
      if (keywords.size() == 0)
         return "";
      PSKeyword key = keywords.get(0);
      keywords = csvc.findKeywordChoices(key.getValue(), null);
      for (PSKeyword k : keywords)
      {
         String value = k.getValue();
         if (keywordvalue.equals(value))
         {
            StringBuilder b = new StringBuilder();
            b.append("psx.keyword.");
            b.append(k.getGUID().longValue());
            b.append("@");
            b.append(k.getLabel());
            return PSI18nUtils.getString(b.toString(), locale);
         }
      }

      return "";
   }

   /**
    * Retrieve the named field for the given content type and return the label
    * for the value given. If there's a problem, the error will be logged and a
    * {@link RuntimeException} thrown.
    * 
    * @param contenttypename the name of the content type, never
    *           <code>null</code> or empty
    * @param fieldname the field name in the given content type, never
    *           <code>null</code> or empty
    * @param choicevalue the value of the choice for the field, never
    *           <code>null</code>
    * @return the value, or an empty string if the content type is unknown, the
    *         field is unknown or does not contain a choice list, or the value
    *         is not found in the choice list.
    */
   @SuppressWarnings("unchecked")
   @IPSJexlMethod(description = "Retrieve the named field for the given content type and return the label for the value given", params =
   {
         @IPSJexlParam(name = "contenttypename", type = "String", description = "the name of the content type"),
         @IPSJexlParam(name = "fieldname", type = "String", description = "the name of the field within the content type"),
         @IPSJexlParam(name = "choicevalue", type = "String", description = "the choice value to lookup the name for")}, returns = "the label, or empty if the value is not found for the field")
   public String getChoiceLabel(String contenttypename, String fieldname,
         String choicevalue)
   {
      if (StringUtils.isBlank(contenttypename))
      {
         throw new IllegalArgumentException(
               "contenttypename may not be null or empty");
      }
      if (StringUtils.isBlank(fieldname))
      {
         throw new IllegalArgumentException(
               "fieldname may not be null or empty");
      }
      if (choicevalue == null)
      {
         throw new IllegalArgumentException(
               "choicevalue may not be null");
      }
      PSItemDefManager idmgr = PSItemDefManager.getInstance();

      try
      {
         PSItemDefinition def = idmgr.getItemDef(contenttypename, 
               PSItemDefManager.COMMUNITY_ANY);
         PSServerItem item = new PSServerItem(def);
         // Load request, use internal
         PSRequest intreq = PSRequest.getContextForRequest();
         item.load(null, intreq); // Loads defaults
         
         PSItemField field = item.getFieldByName(fieldname);
         if (field == null)
         {
            String message = "Could not find field " + fieldname
                  + " in content type " + contenttypename;
            ms_log.error(message);
            throw new RuntimeException(message);
         }
         PSItemFieldMeta meta = field.getItemFieldMeta();
         Iterator<String> names = meta.getOptionDisplayNames();
         while (names.hasNext())
         {
            String candidate = names.next();
            String value = meta.getOptionValueByDisplayName(candidate);
            if (choicevalue.equals(value))
            {
               return candidate;
            }
         }
         ms_log.warn("Could not find value " + choicevalue + " for field "
               + fieldname + " for content type " + contenttypename);
         return "";
      }
      catch (Exception e)
      {
         ms_log.error(e);
         throw new RuntimeException(e);
      }
   }
}

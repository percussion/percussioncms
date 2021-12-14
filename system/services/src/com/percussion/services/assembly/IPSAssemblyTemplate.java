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

import com.percussion.services.assembly.data.PSTemplateBinding;
import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.utils.guid.IPSGuid;

import java.util.List;
import java.util.Set;

/**
 * A template combines a reference to a specific assembler, with information to
 * be used in doing the assembly. A template is related to the content types
 * and/or the content child types that it can assemble. It may also relate to
 * zero or more slot finders as part of the bindings information, each of which
 * can fill a slot in a template template.
 * 
 * @author dougrand
 */
public interface IPSAssemblyTemplate extends IPSCatalogItem
{
   /**
    * Information about active assembly type, which is used when determining how
    * to display active assembly controls in that mode.
    */
   enum AAType
   {
      /**
       * Normal AA controls provide the ability to add, remove and reorder AA
       * related items.
       */
      Normal,
      /**
       * This is an auto index, which cannot be modified via AA controls
       */
      AutoIndex,
      /**
       * This is not an HTML template and AA controls do not apply
       */
      NonHtml;
      /**
       * Lookup value by ordinal
       * 
       * @param ordinal the ordinal
       * @return the matching enum value, or Normal as a default
       */
      public static AAType valueOf(int ordinal)
      {
         for (AAType t : values())
         {
            if (t.ordinal() == ordinal)
            {
               return t;
            }
         }
         return Normal;
      }
   }

   /**
    * Information used to determine when to publish this template for site folder
    * publishing.
    */
   enum PublishWhen
   {
      /**
       * Unspecified is the backward compatible value for non-SFP templates.
       */
      Unspecified('Z'),
      /**
       * Publish this template in SFP if it is the default template for the
       * content item.
       */
      Default('d'),
      /**
       * Always publish this template for SFP
       */
      Always('a'),
      /**
       * Never publish this template for SFP
       */
      Never('n');

      /**
       * The value of the enumeration
       */
      private char m_val;

      PublishWhen(char val) {
         m_val = val;
      }

      /**
       * Get the value of the enumeration
       * 
       * @return the value
       */
      public char getValue()
      {
         return m_val;
      }

      /**
       * Lookup value by ordinal
       * 
       * @param value the value
       * @return the matching enum value, or Unspecified as a default
       */
      public static PublishWhen valueOf(char value)
      {
         for (PublishWhen t : values())
         {
            if (t.getValue() == value)
            {
               return t;
            }
         }
         return Unspecified;
      }
   }
   
   /**
    * Template type indicates if this template is meant to be used with
    * a single content type or across content types. This is primarily
    * a hint to the workbench.
    */
   enum TemplateType
   {
      /**
       * Local templates are intended for use with a single content type
       */
      Local, 
      /**
       * Shared templates are intended for use with multiple content types
       */
      Shared;
      
      /**
       * Lookup value by ordinal
       * 
       * @param value the value
       * @return the matching enum value, or Unspecified as a default
       */
      public static TemplateType valueOf(int value)
      {
         for (TemplateType t : values())
         {
            if (t.ordinal() == value)
            {
               return t;
            }
         }
         return Shared;
      }      
   }

   /**
    * Output format indicates if this is a page, snippet or binary template
    */
   enum OutputFormat
   {
      /**
       * Defines a template that produces binary output
       */
      Binary,
      /**
       * Defines an HTML page, which will be included in a Global template where
       * appropriate
       */
      Page,
      /**
       * Defines a snippet, i.e. an HTML fragment
       */
      Snippet,
      /**
       * Defines a database publishing template
       */
      Database,
      /**
       * Defines a global template HTML page.
       */
      Global;
      /**
       * Lookup value by ordinal
       * 
       * @param ordinal the ordinal
       * @return the matching enum value, or Page as a default
       */
      public static OutputFormat valueOf(int ordinal)
      {
         for (OutputFormat t : values())
         {
            if (t.ordinal() == ordinal)
            {
               return t;
            }
         }
         return Page;
      }
   }

   /**
    * Defines values that dictate how the given template uses a global template.
    */
   enum GlobalTemplateUsage
   {
      /**
       * None means that no global template will be shown
       */
      None, 
      /**
       * Default means that the folder tree above the item, then the site will
       * be examined to find the name of the global template
       */
      Default, 
      /**
       * Defined means that the global template ref is contained in the global
       * template column
       */
      Defined,
      /**
       * Only valid for variants, means that the xsl variant will handle its
       * own global template.
       */
      Legacy;
      
      /**
       * Lookup value by ordinal
       * 
       * @param ordinal the ordinal
       * @return the matching enum value, or Normal as a default
       */
      public static GlobalTemplateUsage valueOf(int ordinal)
      {
         for (GlobalTemplateUsage t : values())
         {
            if (t.ordinal() == ordinal)
            {
               return t;
            }
         }
         return None;
      }
   }

   /**
    * Clone the template.
    * 
    * @return the cloned template, which has the same ID as the current object.
    */
   Object clone();
   
   /**
    * Get the user readable name of the template
    * 
    * @return a non-<code>null</code>, non-empty string
    */
   String getName();
   
   /**
    * Get the user readable label of the template
    * 
    * @return a non-<code>null</code>, non-empty string
    */
   String getLabel();   

   /**
    * Get a longer description suitable for end users to understand how this
    * template is used
    * 
    * @return a non-<code>null</code>, non-empty string
    */
   String getDescription();

   /**
    * Get the assembler plugin extension name that should be used for 
    * this template. Legacy templates, i.e. variants, will return 
    * {@link com.percussion.extension.IPSExtension#LEGACY_ASSEMBLER} for this
    * value.
    * 
    * @return the assembler, never <code>null</code> or empty.
    */
   String getAssembler();

   /**
    * Legacy assemblers store the url of the assembly request here. This will be
    * deprecated in future releases and removed when the system no longer
    * contains applications and resources.
    * 
    * @return the url of the assembly request, <code>null</code> if undefined
    */
   String getAssemblyUrl();

   /**
    * Get the charset, which provides the character representation for character
    * oriented mime types such as text/plain or text/html.
    * @return the character set, may be <code>null</code>
    */
   String getCharset();
   
   /**
    * Holds the default mimetype for this template.
    * 
    * @return the mimetype may be <code>null</code> or empty
    */
   String getMimeType();

   /**
    * Get the stylesheet for old style templates
    * 
    * @return the stylesheet or <code>null</code> if not defined
    */
   String getStyleSheetPath();

   /**
    * Get the default template text associated with this template. Any template
    * may have text, and it may serve any purpose as input to the assembly
    * process.
    * 
    * @return the template, may be <code>null</code> or empty.
    */
   String getTemplate();

   /**
    * Get the location prefix, which is used when constructing assembly
    * locations.
    * 
    * @return the location prefix, may be <code>null</code> or empty
    */
   String getLocationPrefix();

   /**
    * Get the location suffix, which is used when constructing assembly
    * locations.
    * 
    * @return the location suffix, may be <code>null</code> or empty
    */
   String getLocationSuffix();

   /**
    * Get the active assembly type, which is used when determining whether to
    * display the controls for active assembly in that preview mode.
    * 
    * @return the type, never <code>null</code>
    */
   AAType getActiveAssemblyType();

   /**
    * Gets the output format, a broad categorization of the kind of data this
    * template produces.
    * 
    * @return the output format, never <code>null</code>
    */
   OutputFormat getOutputFormat();

   /**
    * Gets publish when, which indicates how this template participates in the
    * SFP process.
    * 
    * @return the publish when value, never <code>null</code>
    */
   PublishWhen getPublishWhen();
   
   /**
    * Gets the template type, which tells the caller if this is a shared or
    * local template
    * @return the template type, never <code>null</code>
    */
   TemplateType getTemplateType();

   /**
    * Gets the global template usage, which indicates if this template uses a
    * global template and whether it defines the global template or uses the
    * default template.
    * 
    * @return the global template usage, never <code>null</code>
    */
   GlobalTemplateUsage getGlobalTemplateUsage();

   /**
    * Gets the global template guid if defined. The path is ignored if the
    * global template usage is not {@link GlobalTemplateUsage#Defined}.
    * 
    * @return the global template path, or <code>null</code> if not defined.
    */
   IPSGuid getGlobalTemplate();

   /**
    * Each binding represents a single piece of data to be made available to the
    * assembly engine. The bindings are executed in order, with results from one
    * binding available to the next execution through the
    * <code>JEXLContext.</code>
    * <p>
    * More information on JEXL can be found on the Apache site under the Jakarta
    * commons documentation.
    * 
    * @return the bindings, may be <code>null</code> or empty
    */
   List<PSTemplateBinding> getBindings();
   
   /**
    * Templates can be based on either assembly plugins, or assembly 
    * applications. We term the ones based on applications <i>variants</i> for
    * historical reasons. 
    * 
    * @return <code>true</code> if this template is a variant
    */
   boolean isVariant();

   /**
    * Set the assembler for use with this template
    * 
    * @param assembler The assembler to set, never <code>null</code> or empty
    */
   void setAssembler(String assembler);

   /**
    * Set the assembly url, essentially for legacy use only. All templates will
    * simply point to the main assembly servlet.
    * 
    * @param assemblyUrl The assemblyUrl to set, never <code>null</code> or
    *           empty
    */
   void setAssemblyUrl(String assemblyUrl);

   /**
    * @param bindings The bindings to set, may be <code>null</code> if there
    *           are no bindings
    */
   void setBindings(List<PSTemplateBinding> bindings);

   /**
    * Set the character set
    * @param charset the new character set, may be <code>null</code>
    */
   void setCharset(String charset);
   
   /**
    * Add a new binding to the list of bindings
    * 
    * @param binding the binding to add, never <code>null</code>
    */
   void addBinding(PSTemplateBinding binding);

   /**
    * Remove a binding from the list of bindings
    * 
    * @param binding the binding to remove, never <code>null</code>
    */
   void removeBinding(PSTemplateBinding binding);

   /**
    * @param description The description to set, never <code>null</code> or
    *           empty
    */
   void setDescription(String description);

   /**
    * @param locationPrefix The locationPrefix to set, may be <code>null</code>
    */
   void setLocationPrefix(String locationPrefix);

   /**
    * @param locationSuffix The locationSuffix to set, may be <code>null</code>
    */
   void setLocationSuffix(String locationSuffix);

   /**
    * Set a new mimetype
    * 
    * @param mimetype a new mime type value, may be <code>null</code>
    */
   void setMimeType(String mimetype);
   
   /**
    * @param name The name to set, never <code>null</code> or empty
    */
   void setName(String name);

   /**
    * @param label The label to set, never <code>null</code> or empty
    */
   void setLabel(String label);
   
   /**
    * @param outputFormat The outputFormat to set, never <code>null</code>
    */
   void setOutputFormat(OutputFormat outputFormat);

   /**
    * @param publishWhen The publishWhen to set, never <code>null</code>
    */
   void setPublishWhen(PublishWhen publishWhen);

   /**
    * @param newTemplateType the new value to set, never <code>null</code>
    */
   void setTemplateType(TemplateType newTemplateType);
   
   /**
    * Set the stylesheet path
    * 
    * @param path The stylesheet path, used only for legacy assemblers, may be
    *           <code>null</code>
    */
   void setStyleSheetPath(String path);

   /**
    * Set the new template text
    * 
    * @param templatetext the text, may be <code>null</code> or empty
    */
   void setTemplate(String templatetext);

   /**
    * Set the new global template usage
    * 
    * @param usage a global template usage value, never <code>null</code>
    */
   void setGlobalTemplateUsage(GlobalTemplateUsage usage);

   /**
    * Set a new global template id
    * 
    * @param guid the new id, may be <code>null</code> if the template is not
    *           using a defined global template.
    */
   void setGlobalTemplate(IPSGuid guid);

   /**
    * set the active assembly type
    * 
    * @param aaType the active assembly type, never <code>null</code> or empty
    */
   void setActiveAssemblyType(AAType aaType);

   /**
    * Get the slots related to this template
    * 
    * @return the slots, may be an empty set but never <code>null</code>
    */
   Set<IPSTemplateSlot> getSlots();

   /**
    * Add a single slot to the template.
    * 
    * @param slot the slot to add, never <code>null</code>
    */
   void addSlot(IPSTemplateSlot slot);

   /**
    * Remove a single slot from the template
    * 
    * @param slot the slot to remove, never <code>null</code>
    */
   void removeSlot(IPSTemplateSlot slot);

   /**
    * Set the slots related to this template.
    * 
    * @param slots The slots to set, if <code>null</code> will be translated
    *           to an empty set
    */
   void setSlots(Set<IPSTemplateSlot> slots);

}

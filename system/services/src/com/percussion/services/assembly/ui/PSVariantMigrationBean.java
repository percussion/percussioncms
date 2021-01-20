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
package com.percussion.services.assembly.ui;

import com.percussion.rx.ui.jsf.beans.PSHelpTopicMapping;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.IPSAssemblyTemplate.OutputFormat;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.general.IPSRhythmyxInfo;
import com.percussion.services.general.PSRhythmyxInfoLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.security.IPSAcl;
import com.percussion.services.security.IPSAclEntry;
import com.percussion.services.security.IPSAclService;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.PSAclServiceLocator;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.PSSecurityException;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.security.IPSTypedPrincipal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.security.acl.NotOwnerException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JSF bean for performing the migration of variants into templates. This bean
 * presents a list of possible variants to migrate. The user selects one or more
 * of these variants, and the bean will create a template for each group of
 * variants that uses the same resource and stylesheet.
 * <p>
 * Once a variant has been migrated, the source of the legacy variant contains
 * the word "DONE", which will do nothing to the processing of the variant, but
 * will serve to let this tool know that the given variant is no longer
 * available to be migrated - which will avoid dups.
 * <p>
 * Migration process:
 * <ul>
 * <li>Create a single template, associated with all the content types for the
 * group of variants to be migrated
 * <li>Associate the new template with all the slots that the old template was
 * associated with.
 * <li>Associate the new template with all the sites that the old template was
 * associated with.
 * <li>Set the publish when value to "never" as an initial value to avoid
 * errors in publishing.
 * <li>Set the HTML template to the HTML source of the source application's
 * stylesheet if possible
 * <li>Mark the old variant(s) as processed
 * </ul>
 * 
 * @author dougrand
 */
public class PSVariantMigrationBean
{
   /**
    * The suffix to append to the variants that have been processed
    */
   private static final String SUFFIX_OLD = "_old";

   /**
    * The string used in the assembly url to allow existing preview and other
    * apps to "find" the assembly servlet.
    */
   private static final String ASSEMBLER_RENDER = "../" 
      + IPSAssemblyService.ASSEMBLY_URL;

   /**
    * This is the extension "name" for the legacy assembler.
    */
   private static final String LEGACY_ASSEMBLER = "Java/global/percussion/assembly/legacyAssembler";

   /**
    * This is the extension "name" for the velocity assembler.
    */
   private static final String VELOCITY_ASSEMBLER = "Java/global/percussion/assembly/velocityAssembler";

   /**
    * Logger
    */
   private static Log ms_log = LogFactory.getLog(PSVariantMigrationBean.class);

   /**
    * Represent a single variant "set". Each set contains one or more variants,
    * that are gathered together by the resource and stylesheet information. The
    * slots, content types, sites, and communities of the original variant(s)
    * are stored here for the conversion process here:
    * {@link PSVariantMigrationBean#convertVariant(com.percussion.services.assembly.ui.PSVariantMigrationBean.Variant)}
    * <p>
    * This class doesn't implement equals, hashcode, etc. since it is never used
    * in sets or maps, and does not need these methods for the ui.
    */
   public static class Variant implements Comparable<Variant>
   {
      /**
       * The name of the variant (computed common name), never <code>null</code>
       * or empty after construction
       */
      private String mi_name;

      /**
       * <code>true</code> if this variant set has been processed
       */
      private Boolean mi_processed = false;

      /**
       * <code>true</code> if this variant set should be processed. Set when
       * the user submits the UI form.
       */
      private Boolean mi_selected = false;

      /**
       * The resource, i.e. rxs_Shared_cas/xyz, never <code>null</code> or
       * empty after construction.
       */
      private String mi_resource;

      /**
       * The stylesheet, i.e. P_EIGeneric.xsl, never <code>null</code> or
       * empty after construction.
       */
      private String mi_stylesheet;

      /**
       * A collection of associated contenttypes, never <code>null</code>
       * after construction.
       */
      private Set<Integer> mi_contenttypes = new HashSet<Integer>();

      /**
       * Slots associated with this variant set, may be empty, but not
       * <code>null</code>
       */
      private Set<IPSGuid> mi_slots = new HashSet<IPSGuid>();

      /**
       * A set of associated sites, never <code>null</code> after
       * construction.
       */
      private Set<IPSGuid> mi_sites = new HashSet<IPSGuid>();

      /**
       * A set of the associated communities, never <code>null</code> after
       * construction.
       */
      private Set<String> mi_communities = new HashSet<String>();

      /**
       * Problems that occurred during processing, may be empty, but not
       * <code>null</code>
       */
      private List<String> mi_errors = new ArrayList<String>();

      /**
       * The original variants, never <code>null</code> or empty after
       * initialization. Used to mark the variants after processing.
       */
      private Collection<IPSAssemblyTemplate> mi_variants = new ArrayList<IPSAssemblyTemplate>();

      /**
       * The name of the converted template, initialized during the conversion
       * process.
       */
      private String mi_newtemplatename;

      /**
       * Ctor
       * 
       * @param name the name, never <code>null</code> or empty
       * @param resource the resource, never <code>null</code> or empty
       * @param stylesheet the stylesheet, never <code>null</code> or empty
       */
      public Variant(String name, String resource, String stylesheet) {
         if (StringUtils.isBlank(name))
         {
            throw new IllegalArgumentException("name may not be null or empty");
         }
         if (StringUtils.isBlank(resource))
         {
            throw new IllegalArgumentException("app may not be null or empty");
         }
         if (StringUtils.isBlank(stylesheet))
         {
            throw new IllegalArgumentException(
                  "stylesheet may not be null or empty");
         }
         mi_name = name;
         mi_resource = resource;
         mi_stylesheet = stylesheet;
      }

      /**
       * This is the name of the first variant found for the variant item. This
       * is a read only property.
       * 
       * @return Returns the name, never <code>null</code> or empty.
       */
      public String getName()
      {
         return mi_name;
      }

      /**
       * The resource is the path to the application that implements the
       * variant. This will be the same for a group of variants that implement a
       * single logical template.
       * 
       * @return the resource, never <code>null</code> or empty
       */
      public String getResource()
      {
         return mi_resource;
      }

      /**
       * Each variant item is associated with at least one content type. Each
       * source variant must be associated with exactly one content type,
       * although the processing code does not enforce this.
       * 
       * @return the contenttypes, never <code>null</code>
       */
      public Set<Integer> getContenttypes()
      {
         return mi_contenttypes;
      }

      /**
       * This flag indicates that the variant item has been processed. Set to
       * <code>true</code> after processing or if the template source for the
       * variant, which isn't used for any purpose in a variant, has a non-empty
       * value.
       * 
       * @return the processed
       */
      public Boolean getProcessed()
      {
         return mi_processed;
      }

      /**
       * This is the path to the stylesheet of the XSL variant. This plus the
       * resource identify a specific unique set of variants that work together.
       * 
       * @return the stylesheet, never <code>null</code> or empty
       */
      public String getStylesheet()
      {
         return mi_stylesheet;
      }

      /**
       * The slots that are stored for the variant item represent the sum of all
       * associated slots from the variant.
       * 
       * @return the slots never <code>null</code> but may be empty
       */
      public Set<IPSGuid> getSlots()
      {
         return mi_slots;
      }

      /**
       * Set the processing flag, see {@link #getProcessed()}
       * 
       * @param processed the processed to set
       */
      public void setProcessed(Boolean processed)
      {
         mi_processed = processed;
      }

      /**
       * The selected flag is a property used to mark those items that will be
       * processed.
       * 
       * @return the selected
       */
      public Boolean getSelected()
      {
         return mi_selected;
      }

      /**
       * Set the selected flag, see {@link #getProcessed()}
       * 
       * @param selected the selected to set
       */
      public void setSelected(Boolean selected)
      {
         mi_selected = selected;
      }

      /**
       * If there are problems during processing, they are stored in the error
       * list.
       * 
       * @return the errors
       */
      public List<String> getErrors()
      {
         return mi_errors;
      }

      /**
       * This property stores the source variants that this variant item
       * represents.
       * 
       * @return the variants
       */
      public Collection<IPSAssemblyTemplate> getVariants()
      {
         return mi_variants;
      }

      /**
       * This property stores the communities that had runtime access to any of
       * the source variants.
       * 
       * @return the communities may be empty but not <code>null</code>
       */
      public Set<String> getCommunities()
      {
         return mi_communities;
      }

      /**
       * This property stores the sum of all sites associate with the source
       * variants.
       * 
       * @return the sites the associated sites, may be empty but not
       *         <code>null</code>
       */
      public Set<IPSGuid> getSites()
      {
         return mi_sites;
      }

      /**
       * This property holds a new computed unique name for the processed
       * template.
       * 
       * @return the newtemplatename
       */
      public String getNewtemplatename()
      {
         return mi_newtemplatename;
      }

      /**
       * Set the new name, see {@link #getNewtemplatename()}
       * 
       * @param newtemplatename the newtemplatename to set
       */
      public void setNewtemplatename(String newtemplatename)
      {
         if (StringUtils.isBlank(newtemplatename))
         {
            throw new IllegalArgumentException("Template name may not be blank");
         }
         mi_newtemplatename = newtemplatename;
      }

      /**
       * Implementation of comparable simply compares the names of the two
       * variant items.
       * 
       * @param o the second item
       * @return See {@link Comparable}
       */
      public int compareTo(Variant o)
      {
         return mi_name.compareTo(o.mi_name);
      }
   }

   /**
    * The list of variants that are available, might be empty if there are no
    * variants of course.
    */
   private Set<Variant> m_variants = new TreeSet<Variant>();

   /**
    * A list of sites, initialized in the ctor, should never be empty
    */
   private List<IPSSite> m_sites = null;

   /**
    * Ctor
    * 
    * @throws PSAssemblyException if there's a problem retrieving the templates
    */
   public PSVariantMigrationBean() throws PSAssemblyException
   {
      initialize();
   }

   /**
    * Get the variants from the assembly service and gather them together to
    * form the variant objects needed for the UI.
    * 
    * @throws PSAssemblyException if there's a problem retrieving the templates
    */
   private void initialize() throws PSAssemblyException
   {
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      IPSContentMgr cmgr = PSContentMgrLocator.getContentMgr();
      IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
      IPSAclService amgr = PSAclServiceLocator.getAclService();
      IPSBackEndRoleMgr rmgr = PSRoleMgrLocator.getBackEndRoleManager();

      List<PSCommunity> communities = rmgr.findCommunitiesByName("%");
      m_sites = smgr.findAllSites();
      Map<String, Variant> vmap = new HashMap<String, Variant>();

      Set<IPSAssemblyTemplate> templates = asm.findAllTemplates();
      for (IPSAssemblyTemplate t : templates)
      {
         // Skip binaries, and skip all non-variants and skip any variant
         // without a stylesheet
         String assembler = t.getAssembler();
         if (StringUtils.isNotBlank(assembler)
               && !assembler.equals(LEGACY_ASSEMBLER))
            continue;
         if (t.getOutputFormat().equals(OutputFormat.Binary))
            continue;
         if (StringUtils.isBlank(t.getStyleSheetPath()))
            continue;
         String key = makeKey(t);
         Variant v = vmap.get(key);
         if (v == null)
         {
            v = new Variant(t.getName(), t.getAssemblyUrl(), t
                  .getStyleSheetPath());
            m_variants.add(v);
            vmap.put(key, v);
         }
         // Get the associated content types (should be one for a variant)
         try
         {
            List<IPSNodeDefinition> defs = cmgr.findNodeDefinitionsByTemplate(t
                  .getGUID());
            for (IPSNodeDefinition d : defs)
            {
               v.getContenttypes().add((int) d.getGUID().longValue());
            }
         }
         catch (RepositoryException e)
         {
            ms_log.error(e);
         }
         // Get the associated slots
         for (IPSTemplateSlot slot : t.getSlots())
         {
            v.getSlots().add(slot.getGUID());
         }
         v.getSites().addAll(getSitesForTemplate(t));

         // Get the associated communities
         getAssociatedCommunities(amgr, communities, t, v);

         String name = v.getName() + "_v";
         try
         {
            asm.findTemplateByName(name);
            for (int i = 1; i < 100; i++)
            {
               name = v.getName() + "_v" + i;
               asm.findTemplateByName(name);
            }
            v.getErrors().add(
                  "Couldn't create a new template with a unique name");
            v.setNewtemplatename("undefined");
         }
         catch (PSAssemblyException e)
         {
            // Good, the name is unique
         }
         v.setNewtemplatename(name);

         // Keep the association from the variantitem to the source templates
         v.getVariants().add(t);

         // If this variant has any content in the source, then mark it
         // processed.
         if (StringUtils.isNotBlank(t.getTemplate()))
         {
            v.setProcessed(true);
         }
      }
   }

   /**
    * Discover what communities are associated with the given variant. This uses
    * the security service to check each possible community to see if there's
    * access, then checks the "any" community.
    * 
    * @param amgr the acl service, assumed not <code>null</code>
    * @param communities the list of communities, assumed not <code>null</code>
    * @param template the template being created, assumed not <code>null</code>
    * @param variantitem the variant item being processed, assumed not
    *           <code>null</code>
    */
   private void getAssociatedCommunities(IPSAclService amgr,
         List<PSCommunity> communities, IPSAssemblyTemplate template,
         Variant variantitem)
   {
      IPSAcl acl = amgr.loadAclForObject(template.getGUID());
      if (acl != null)
      {
         IPSTypedPrincipal anycommunity = new PSTypedPrincipal(
               PSTypedPrincipal.ANY_COMMUNITY_ENTRY,
               IPSTypedPrincipal.PrincipalTypes.COMMUNITY);
         for (PSCommunity c : communities)
         {
            IPSTypedPrincipal p = new PSTypedPrincipal(c.getName(),
                  IPSTypedPrincipal.PrincipalTypes.COMMUNITY);
            try
            {
               if (acl.checkPermission(p, PSPermissions.RUNTIME_VISIBLE))
               {
                  variantitem.getCommunities().add(c.getName());
               }
            }
            catch (SecurityException e)
            {
               try
               {
                  if (acl.checkPermission(anycommunity,
                        PSPermissions.RUNTIME_VISIBLE))
                  {
                     variantitem.getCommunities().add(c.getName());
                  }
               }
               catch (SecurityException se)
               {
                  // Ignore, no matching entry means no permission
               }
            }
         }
      }
   }

   /**
    * Find the sites that are associated with the given template. This currently
    * just cycles through all the sites as that is fast enough for UI code.
    * 
    * @param template the template being processed, assumed not
    *           <code>null</code>
    * @return a collection of guids, possibly empty, but never <code>null</code>
    */
   private Collection<IPSGuid> getSitesForTemplate(IPSAssemblyTemplate template)
   {
      Set<IPSGuid> siteids = new HashSet<IPSGuid>();

      for (IPSSite s : m_sites)
      {
         for (IPSAssemblyTemplate t : s.getAssociatedTemplates())
         {
            if (t.getGUID().equals(template.getGUID()))
            {
               siteids.add(s.getGUID());
               break;
            }
         }
      }
      return siteids;
   }

   /**
    * Create a key from the template's resource and stylesheet information.
    * 
    * @param t the template, assumed not <code>null</code>
    * @return the string key
    */
   private String makeKey(IPSAssemblyTemplate t)
   {
      String res = t.getAssemblyUrl();
      String ss = t.getStyleSheetPath();
      return res + '/' + ss;
   }

   /**
    * @return the variants
    */
   public Collection<Variant> getVariants()
   {
      return m_variants;
   }

   /**
    * Get only those variants that have been selected.
    * 
    * @return return the filtered collection of variants, never
    *         <code>null</code>
    */
   public Collection<Variant> getProcessedvariants()
   {
      Set<Variant> rval = new TreeSet<Variant>();
      for (Variant v : m_variants)
      {
         if (v.getSelected())
         {
            rval.add(v);
         }
      }
      return rval;
   }

   /**
    * Process the selected variants into new templates. For each variant we
    * create a template with a name derived from the variant's name by adding
    * "_v" after the name. If the name exists, then other names are tried by
    * adding a number to the suffix, i.e. _v1, _v2, etc.
    * 
    * @return the outcome
    */
   public String process()
   {
      for (Variant v : m_variants)
      {
         if (v.getSelected())
         {
            try
            {
               convertVariant(v);
            }
            catch (PSAssemblyException e)
            {
               v.getErrors().add(e.getLocalizedMessage());
            }
         }
      }
      return "admin-convert-variants-result";
   }

   /**
    * Get the help file name for the Convert Variable page.
    * 
    * @return  the help file name, never <code>null</code> or empty.
    */
   public String getHelpFile()
   {
      return PSHelpTopicMapping.getFileName("ConvertVariant");      
   }
   
   /**
    * Do the conversion. This just clones most of the variant's information, and
    * establishes relationships to the slots, sites and content types.
    * 
    * @param v the variant to be converted, assumed not <code>null</code>
    * @throws PSAssemblyException if there's a problem saving the template(s)
    */
   private void convertVariant(Variant v) throws PSAssemblyException
   {
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      IPSContentMgr cmgr = PSContentMgrLocator.getContentMgr();
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      IPSAssemblyTemplate template = createClonedTemplate(v, asm);
      if (template == null)
         return;

      // Get the source from the original
      template.setTemplate(getSource(v));

      // Add the slots
      for (IPSGuid s : v.getSlots())
      {
         IPSTemplateSlot slot = asm.loadSlot(s);
         template.addSlot(slot);
      }

      // Add the communities
      IPSAclService amgr = PSAclServiceLocator.getAclService();
      IPSTypedPrincipal owner = new PSTypedPrincipal(
            PSTypedPrincipal.DEFAULT_USER_ENTRY,
            IPSTypedPrincipal.PrincipalTypes.USER);
      IPSAcl acl = amgr.createAcl(template.getGUID(), owner);
      try
      {
         IPSAclEntry ownerentry = acl.findDefaultEntry(false);
         ownerentry.addPermissions(new PSPermissions[]
         {PSPermissions.READ, PSPermissions.DELETE, PSPermissions.UPDATE});
         for (String community : v.getCommunities())
         {
            IPSTypedPrincipal centry = new PSTypedPrincipal(community,
                  IPSTypedPrincipal.PrincipalTypes.COMMUNITY);

            acl.addEntry(owner, acl.createEntry(centry, new PSPermissions[]
            {PSPermissions.RUNTIME_VISIBLE}));

         }

         List<IPSAcl> alist = new ArrayList<IPSAcl>();
         alist.add(acl);

         amgr.saveAcls(alist);
      }
      catch (PSSecurityException e1)
      {
         v.getErrors().add(
               "Problem persisting acls: " + e1.getLocalizedMessage());
      }
      catch (NotOwnerException e)
      {
         v.getErrors().add(
               "Problem adding acl entry: " + e.getLocalizedMessage());
      }
      // Persist
      asm.saveTemplate(template);

      // Add the content type associations
      List<IPSGuid> defids = new ArrayList<IPSGuid>();
      for (int ct : v.getContenttypes())
      {
         defids.add(gmgr.makeGuid(ct, PSTypeEnum.NODEDEF));
      }
      try
      {
         List<IPSNodeDefinition> defs = cmgr.loadNodeDefinitions(defids);
         for (IPSNodeDefinition def : defs)
         {
            def.addVariantGuid(template.getGUID());
         }
         cmgr.saveNodeDefinitions(defs);
      }
      catch (RepositoryException e)
      {
         v.getErrors().add(
               "One or more content type defs didn't "
                     + "load, no associations formed");
      }

      // Mark variants processed, and change the labels to foo_old
      for (IPSAssemblyTemplate t : v.getVariants())
      {
         String l = t.getLabel();
         if (!l.endsWith(SUFFIX_OLD))
         {
            t.setLabel(l + SUFFIX_OLD);
         }
         t.setTemplate("DONE");
         asm.saveTemplate(t);
      }

      v.setProcessed(true);
      v.getErrors().clear();
   }

   /**
    * Clone the variant into a new template
    * 
    * @param variantitem the item being processed, assumed never
    *           <code>null</code>.
    * @param asm the assembly service, assumed never <code>null</code>
    * @return the cloned template, <code>null</code> if the template's name
    *         couldn't be created
    */
   private IPSAssemblyTemplate createClonedTemplate(Variant variantitem,
         IPSAssemblyService asm)
   {

      IPSAssemblyTemplate template;
      template = asm.createTemplate();
      template.setName(variantitem.getNewtemplatename());
      template.setAssembler(VELOCITY_ASSEMBLER);
      template.setPublishWhen(IPSAssemblyTemplate.PublishWhen.Never);
      IPSAssemblyTemplate variant = variantitem.getVariants().iterator().next();
      template.setActiveAssemblyType(variant.getActiveAssemblyType());
      template.setAssemblyUrl(ASSEMBLER_RENDER);
      template.setDescription(variant.getDescription());
      String label = variant.getLabel();
      if (label.endsWith(SUFFIX_OLD))
      {
         label = label.substring(0, label.length() - 4);
      }
      template.setLabel(label);
      template.setLocationPrefix(variant.getLocationPrefix());
      template.setLocationSuffix(variant.getLocationSuffix());
      template.setOutputFormat(variant.getOutputFormat());
      template.setTemplateType(IPSAssemblyTemplate.TemplateType.Shared);
      return template;
   }

   /**
    * Use the resource and stylesheet information to get the original html
    * source if it still exists under the src directory
    * 
    * @param v the variant info, assumed not <code>null</code>
    * @return the source or empty if there's a problem
    */
   @SuppressWarnings("unchecked")
   private String getSource(Variant v)
   {
      IPSRhythmyxInfo info = PSRhythmyxInfoLocator.getRhythmyxInfo();
      String dir = (String) info
            .getProperty(IPSRhythmyxInfo.Key.ROOT_DIRECTORY);
      String resource = v.getResource();
      // Create a file path using the stylesheet string and resource info
      int last = resource.lastIndexOf('/');
      if (last < 0)
      {
         v.getErrors().add(
               "Couldn't parse resource string, no final slash found");
         return "";
      }
      resource = resource.substring(0, last);
      if (resource.startsWith("../"))
         resource = resource.substring(2);
      String filename = v.getStylesheet().replace(".xsl", ".html");
      String file = dir + resource + "/src/" + filename;
      File ffile = new File(file);
      if (!ffile.exists())
      {
         v.getErrors().add("Couldn't find file, " + file);
         return "";
      }

      StringBuilder b = new StringBuilder();
      try
      {
         Reader r = new InputStreamReader(new FileInputStream(ffile), "UTF-8");
         List<String> lines = IOUtils.readLines(r);
         for (String l : lines)
         {
            b.append(l);
            b.append('\n');
         }
      }
      catch (UnsupportedEncodingException e)
      {
         ms_log.error(e);
         return "";
      }
      catch (FileNotFoundException e)
      {
         ms_log.error(e);
         return "";
      }
      catch (IOException e)
      {
         v.getErrors().add("Problems reading file " + file);
         return "";
      }

      return b.toString();
   }
}

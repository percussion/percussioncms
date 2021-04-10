package com.percussion.soln.rss;

import static org.apache.commons.collections.CollectionUtils.filter;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;
import org.apache.commons.collections.Predicate;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.PSExtensionException;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.IPSAssemblyTemplate.OutputFormat;
import com.percussion.services.assembly.jexl.PSLocationUtils;
import com.percussion.soln.jcr.NodeUtils;


public class RssJexl implements IPSJexlExpression {
    
    private PSLocationUtils locationUtils;
    private IPSAssemblyService assemblyService;
    
    public List<SyndEntry> createEntries() {
        return new ArrayList<>();
    }
    
    public SyndFeed createFeed() {
        return new SyndFeedImpl();
    }
    
    public SyndFeed createFeed(Node node, String titleFields, String bodyFields) {
        SyndFeed feed = createFeed();
        String title = getValue(node, titleFields);
        feed.setTitle(title);
        String body = getValue(node, bodyFields);
        feed.setDescription(body);
        return feed;
    }
    
    public SyndEntry createEntry() {
        return new SyndEntryImpl();
    }
    
    
    public SyndEntry createEntry(Node node, String titleFields, String bodyFields) {
        SyndEntry entry = createEntry();
        String title = getValue(node, titleFields);
        entry.setTitle(title);
        entry.setUpdatedDate(new Date());
        entry.setAuthor(getValue(node, "author"));
        String description = getValue(node, bodyFields);
        SyndContent content = createContent();
        content.setType("text/html");
        content.setValue(description);
        entry.setDescription(content);
        return entry;
    
    }

    protected String getValue(Node node, String titleFields) {
        return locationUtils.getFirstDefined(node, titleFields, "");
    }
    
    public SyndContent createContent() {
        return new SyndContentImpl();
    }
    
    public String getRss(SyndFeed feed) throws IOException, FeedException {
        feed.setFeedType("rss_2.0");
        return feedToString(feed);
    }
    
    public String getAtom(SyndFeed feed) throws IOException, FeedException {
        feed.setFeedType("atom_1.0");
        return feedToString(feed);
    }
    
    
    public String feedToString(SyndFeed feed) throws IOException, FeedException {
        StringWriter writer = new StringWriter();
        SyndFeedOutput output = new SyndFeedOutput();
        output.output(feed,writer);
        writer.close();
        return writer.getBuffer().toString();
    }

    public void init(IPSExtensionDef arg0, File arg1) throws PSExtensionException {
        setLocationUtils(new PSLocationUtils());
        setAssemblyService(PSAssemblyServiceLocator.getAssemblyService());
    }

    
    
    
    public void setAssemblyService(IPSAssemblyService assemblyService) {
        this.assemblyService = assemblyService;
    }

    public void setLocationUtils(PSLocationUtils locationUtils) {
        this.locationUtils = locationUtils;
    }
    
    public String findEntryTemplate(Node node) throws PSAssemblyException {
        String contentType = getContentType(node);
        Collection<IPSAssemblyTemplate> templates = 
            findTemplates("rss.*entry", contentType, "text/xml", OutputFormat.Snippet);
        return pickTemplate(templates);
    }
    
    
    public String findFeedTemplate(Node node) throws PSAssemblyException {
        String contentType = getContentType(node);
        Collection<IPSAssemblyTemplate> templates = 
            findTemplates("rss.*feed", contentType, "text/xml", OutputFormat.Page);
        return pickTemplate(templates);
    }

    protected String getContentType(Node node) {
        return  NodeUtils.getContentType(node);
    }
    
    private String pickTemplate(Collection<IPSAssemblyTemplate> templates) {
        if (templates.isEmpty())
            return null;
        return templates.iterator().next().getName();
    }
    
    public Collection<IPSAssemblyTemplate> findTemplates(
            final String name, 
            final String description, 
            final String mimeType, 
            final OutputFormat format) throws PSAssemblyException {
        
        Collection<IPSAssemblyTemplate> templates = findAllTemplates();
        templates = new ArrayList<>(templates);
        final Pattern np = name == null ? null :  Pattern.compile(name, Pattern.CASE_INSENSITIVE);
        final Pattern dp = description == null ? null : Pattern.compile(description, Pattern.CASE_INSENSITIVE);
        final Pattern mt = mimeType == null ? null : Pattern.compile(mimeType, Pattern.CASE_INSENSITIVE);
        
        filter(templates, new TemplatePredicate() { 
            @Override
            public boolean evalTemplate(IPSAssemblyTemplate t) {
                String name = fix(t.getName());
                String mime = fix(t.getMimeType());
                String description = fix(t.getDescription());
                
                return
                    (format == null || t.getOutputFormat() == format)
                    && (np == null || np.matcher(name).find())
                    && (mt == null || mt.matcher(mime).matches())
                    && (dp == null || dp.matcher(description).find());

            }
        });
        return templates;
        
    }

    protected Collection<IPSAssemblyTemplate> findAllTemplates() throws PSAssemblyException {
        return  assemblyService.findAllTemplates();
    }
    
    private String fix(String input) {
        if (input == null) return "";
        return input;
    }
    
    public abstract static class TemplatePredicate implements Predicate {

        public boolean evaluate(Object t) {
            return evalTemplate((IPSAssemblyTemplate) t);
        }
        
        public abstract boolean evalTemplate(IPSAssemblyTemplate t);
        
    }
    

}

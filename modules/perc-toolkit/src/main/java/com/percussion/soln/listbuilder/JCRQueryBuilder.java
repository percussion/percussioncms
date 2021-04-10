package com.percussion.soln.listbuilder;

import static java.text.MessageFormat.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.join;
import static org.apache.commons.lang.StringUtils.removeEnd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 
 * Builds a JCR query from fields on a node.
 * 
 * @author adamgent
 *
 */
public class JCRQueryBuilder {

    private String startDate = null;
    private String endDate = null;
    private String titleContains = null;
    private String queryStartDateField = "rx:sys_contentstartdate";
    private String queryTitleField = "rx:displaytitle";
    private List<String> selectFields = asList("rx:sys_contentid", "rx:sys_folderid", "jcr:path");
    private Collection<String> fromTypes = asList("nt:base");
    private Collection<String> folderPaths = emptyList();
    private String query;

    
    public void validate() {
        if ( isEmpty(selectFields) || isEmpty(fromTypes)) {
            throw new IllegalStateException("Configuration of ListBuilder invalid");
        }
    }

    protected String buildTextField(String field, String value) {
        return format("( {0} like ''%{1}%'' )", field, value);
    }
    
    public String buildQuery(List<String> fields, Collection<String> types, String cond) {
        String f = join(fields.iterator(),", ");
        String t = join(types.iterator(), ", ");
        if (isNotBlank(cond))
            return format("select {0} from {1} where {2}", f,t,cond);
        return format("select {0} from {1}", f,t);
    }
    

    public String buildDateRange(String field, String startDate, String endDate) {
        if (isNotBlank(field) && isNotBlank(startDate) && isNotBlank(endDate))
            return  format("(''{0}'' < {1} and {1} < ''{2}'')", startDate,field,endDate) ;
        else if (isNotBlank(field) && isNotBlank(startDate))
            return format("(''{0}''  < {1} )", startDate, field) ;
        else
            return "";
    }
    
    protected String buildDateCond() {
        return buildDateRange(queryStartDateField, startDate, endDate);
    
    }
    
    protected String buildTitleCond() {
        if ( isBlank(queryTitleField)) return "";
        if ( isBlank(titleContains) ) return "";
        return format(" {0} like ''%{1}%'' ", queryTitleField, titleContains);
    }
    
    public String buildCond(String sep, List<String> cond) {
        cond = removeBlank(cond);
        if ( isEmpty(cond) ) return "";
        String conds = join(cond.iterator(), " " + sep + " ");
        return "(" + conds + ")";
    }
    
    private boolean isEmpty(Collection<?> c) {
        return (c == null || c.isEmpty());
    }
    
    private List<String> removeBlank(List<String> ss) {
        List<String> rvalue = new ArrayList<String>();
        if (ss == null) return rvalue;
        
        for (String s : ss) {
            if (isNotBlank(s)) {
                rvalue.add(s);
            }
        }
        return rvalue;
    }
    public String buildPathsLikeCond(Collection<String> paths) {
        if (isEmpty(paths))
            return "";
        List<String> conds = new ArrayList<String>();
        for (String p : paths) {
            if (isNotBlank(p)) {
                String n = removeEnd(p, "/") + "/";
                conds.add(" jcr:path like '" + n + "%' ");
            }
        }
        if (isEmpty(conds))
            return "";
        return "(" + join(conds.iterator(), " or ") + ")";
    }
    
    public String buildCond() {
//        String taxCond = buildTaxCond();
//        String tagCond = buildTagCond();
//        String categoryCond = buildCond("or", [tagCond,taxCond]);
        String dateCond = buildDateCond();
        String titleCond = buildTitleCond();
        String pathCond = buildPathsLikeCond(getFolderPaths());
        List<String> conds = asList(
                //categoryCond,
                dateCond,
                titleCond,
                pathCond
               );
        String cond = buildCond("and", conds);
        return cond;
    }

    public String getQuery() {
        if (isNotBlank(this.query)) 
            return this.query;
        validate();
        String cond = buildCond();
        //String paths = this.pathsFromIds(values*.getLong());
        List<String> fields = this.selectFields;
        Collection<String> types = this.fromTypes;
        return buildQuery(fields, types, cond);
    }
    
    public void setQuery(String query) {
        this.query = query;
    }

    public String getQueryStartDateField() {
        return queryStartDateField;
    }

    
    public void setQueryStartDateField(String queryStartDateField) {
        this.queryStartDateField = queryStartDateField;
    }

    
    public String getQueryTitleField() {
        return queryTitleField;
    }

    
    public void setQueryTitleField(String queryTitleField) {
        this.queryTitleField = queryTitleField;
    }

    
    public List<String> getSelectFields() {
        return selectFields;
    }

    
    public void setSelectFields(List<String> selectFields) {
        this.selectFields = selectFields;
    }

    
    public Collection<String> getFromTypes() {
        return fromTypes;
    }

    
    public void setFromTypes(Collection<String> fromTypes) {
        this.fromTypes = fromTypes;
    }

    
    public String getStartDate() {
        return startDate;
    }

    
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    
    public String getEndDate() {
        return endDate;
    }

    
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    
    public String getTitleContains() {
        return titleContains;
    }

    
    public void setTitleContains(String titleContains) {
        this.titleContains = titleContains;
    }

    
    public Collection<String> getFolderPaths() {
        return folderPaths;
    }

    
    public void setFolderPaths(Collection<String> folderPaths) {
        this.folderPaths = folderPaths;
    }
    
    
    
    
//  public Collection<String> pathsFromIds(ids) {
//  def locators = ids.collect { return new PSLocator((int)it, -1) };
//  def guids = locators.collect { return guidManager.makeGuid(it) };
//  def folders = contentService.loadFolders(guids);
//  return folders*.getFolderPath()
//}
    
    
//  public String buildPathsEqual(List paths) {
//      def p = paths.collect { return " jcr:path = '$it' " }.join(' or ');
//      return p ? "($p)" : "";
//  }
//  
//  public String buildPathsLike(List paths) {
//      def p = paths.collect { return " jcr:path like '$it%' " }.join(' or ');
//      return p ? "($p)" : "";
//  }
    
//  public List convertSimpleChildField(String field) {
//  if ( ! field ) return [];
//  def values = node.getProperty(field).getValues().toList().string;
//  return values ? values : [];
//}

//public String buildTagCond() {
//  def values = convertSimpleChildField(tagField);
//  return buildPathsEqual(values);
//}

//public String buildTaxCond() {
//  def values = convertSimpleChildField(taxonomyField);
//  return buildPathsLike(values);
//}
    
}

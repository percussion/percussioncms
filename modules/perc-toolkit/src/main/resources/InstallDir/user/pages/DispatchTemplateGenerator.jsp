<%@page import="com.percussion.services.assembly.IPSAssemblyService"%>
<%@page import="com.percussion.services.assembly.IPSAssemblyTemplate"%>
<%@page import="com.percussion.services.assembly.IPSAssemblyTemplate.PublishWhen"%>
<%@page import="com.percussion.services.assembly.IPSTemplateBinding"%>
<%@page import="com.percussion.services.assembly.IPSTemplateSlot"%>
<%@page import="com.percussion.services.assembly.PSAssemblyServiceLocator"%>
<%@page import="com.percussion.services.assembly.data.PSTemplateBinding"%>
<%@page import="com.percussion.services.catalog.PSTypeEnum"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" 
    import="com.percussion.services.contentmgr.IPSContentMgr"
    import="com.percussion.services.contentmgr.IPSNodeDefinition"
    import="com.percussion.services.contentmgr.PSContentMgrLocator"
    import="com.percussion.services.guidmgr.IPSGuidManager, com.percussion.services.guidmgr.PSGuidManagerLocator"
    import="com.percussion.services.guidmgr.data.PSGuid, com.percussion.services.sitemgr.IPSSite"
    import="com.percussion.services.sitemgr.IPSSiteManager,com.percussion.services.sitemgr.PSSiteManagerLocator"
    import="com.percussion.utils.guid.IPSGuid, com.percussion.utils.types.PSPair, com.percussion.webservices.assembly.IPSAssemblyDesignWs, com.percussion.webservices.assembly.PSAssemblyWsLocator, com.percussion.webservices.content.IPSContentDesignWs, com.percussion.webservices.content.PSContentWsLocator, com.percussion.workbench.ui.editors.form.PSPublishWhenHelper.PublishWhenChoice, org.apache.log4j.Logger"
    import="javax.servlet.jsp.JspWriter, java.io.IOException, java.util.ArrayList"
    import="java.util.Collection, java.util.List"
    import="java.util.Set"
%>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>

<!doctype html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>Dispatch Template Generator</title>
	<script src="http://code.jquery.com/jquery-1.9.0.js"></script>
	<script src="http://code.jquery.com/jquery-migrate-1.0.0.js"></script>
</head>
<body>
<%
Logger log = Logger.getLogger("psoDispatchTemplateGenerator"); 
IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
IPSAssemblyService asvc = PSAssemblyServiceLocator.getAssemblyService();
IPSSiteManager siteSvc = PSSiteManagerLocator.getSiteManager();
IPSAssemblyDesignWs service = PSAssemblyWsLocator.getAssemblyDesignWebservice();
IPSContentDesignWs svcContentDesign = PSContentWsLocator.getContentDesignWebservice();
IPSContentMgr cmgr = PSContentMgrLocator.getContentMgr();
 
if (request.getMethod().equals("POST")){
	IPSSite targetSite = siteSvc.loadSite(new PSGuid(request.getParameter("sitelist")));
	if(targetSite==null){ 
		out.println("<b>Unable to location site: <em>" + request.getParameter("sitelist") + "</em></b>");
 	}else{
		out.println("<br />Generating Dispatch templates for site: " + targetSite.getName());
		
		//Generate a copy of each template
		Set<IPSAssemblyTemplate> templates = targetSite.getAssociatedTemplates();
		for(IPSAssemblyTemplate t : templates){ 
			
			//t.setAssembler("Java/global/percussion/assembly/velocityAssembler");
			if(t.getAssembler().equals("Java/global/percussion/assembly/velocityAssembler")){
				out.println("<br />Processing Template:" + t.getName());
				//This is a velocity template, so generate a copy of it and generate a dispatch
				IPSAssemblyTemplate copy = asvc.createTemplate();
				
				copy.setActiveAssemblyType(t.getActiveAssemblyType());
				copy.setAssembler(t.getAssembler());
				copy.setAssemblyUrl(t.getAssemblyUrl());
				copy.setCharset(t.getCharset());
				copy.setDescription(t.getDescription());
				copy.setGlobalTemplate(t.getGlobalTemplate());
				copy.setGlobalTemplateUsage(t.getGlobalTemplateUsage());
				copy.setLabel(t.getLabel());
				copy.setLocationPrefix(t.getLocationPrefix());
				copy.setLocationSuffix(t.getLocationSuffix());
				copy.setMimeType(t.getMimeType());
				copy.setName(t.getName());
				copy.setOutputFormat(t.getOutputFormat());
 				Set<IPSTemplateSlot> oldSlots = t.getSlots();
				copy.setSlots(t.getSlots());
				copy.setStyleSheetPath(t.getStyleSheetPath());
				copy.setTemplate(t.getTemplate());
				copy.setTemplateType(t.getTemplateType());
				copy.setPublishWhen(PublishWhen.Never);
				
			    //Copy the bindings creating new bindings. 
				List<IPSTemplateBinding> oldBind = t.getBindings();
							
				for(IPSTemplateBinding bind: oldBind){
					out.println("<br />Copying binding:" + bind.getVariable());
					PSTemplateBinding binding = new PSTemplateBinding();
					binding.setId(gmgr.createLongId(PSTypeEnum.INTERNAL));				
					binding.setVariable(bind.getVariable());
					binding.setExpression(bind.getExpression());
					binding.setExecutionOrder(t.getBindings().size()+1);
					copy.addBinding(binding);
				}
				
				
				//Add the new sys.template binding to the dispatch template.
				PSTemplateBinding binding = new PSTemplateBinding();
				
				binding.setId(gmgr.createGuid(PSTypeEnum.INTERNAL).longValue());
				binding.setVariable("$sys.template");
				binding.setExpression("\""+copy.getName()+"\"");
				binding.setExecutionOrder(t.getBindings().size()+1);
				t.addBinding(binding);
				t.setName(t.getName() + "Dispatch");
				t.setAssembler("Java/global/percussion/assembly/dispatchAssembler");
				out.println("<br />Saving dispatch template as:" + t.getName());
				asvc.saveTemplate(t);
		
				out.println("<br />Saving velocity template as:" + copy.getName());
				asvc.saveTemplate(copy);
				copy = asvc.findTemplateByName(copy.getName());
				
				//Now that copy is saved - we need to fix it's slot associations
				IPSGuid copyGuid = copy.getGUID();
				
				
				out.println("<br />Processing Content Types");
				
				//Re-Link Content Type Associations
				List<IPSNodeDefinition> types = cmgr.findAllItemNodeDefinitions();
				
				for(IPSNodeDefinition ct : types){
					out.println("<br />Processing Content Type:" + ct.getName());
					
					Set<IPSGuid> ct_temps = ct.getVariantGuids();
					boolean addTemplate = false;
					for(IPSGuid a : ct_temps){
						if(a.equals(t.getGUID())){
							addTemplate = true;
						}
					}
					if(addTemplate){
						out.println("<br />Relinking to Content Type:" + ct.getName());
						ct.addVariantGuid(copyGuid);
					}
				}
				
				cmgr.saveNodeDefinitions(types);
				
				//Re-Link any Slot Associations
				List<IPSTemplateSlot> allSlots = asvc.findSlotsByName("%");
				for(IPSTemplateSlot s : allSlots){
					out.println("<br />Processing slot:" + s.getName());
					Collection<PSPair<IPSGuid,IPSGuid>> assoc = s.getSlotAssociations();
					
					Collection<PSPair<IPSGuid,IPSGuid>> newAssoc = new ArrayList<PSPair<IPSGuid,IPSGuid>>();
					
					for(PSPair<IPSGuid,IPSGuid> p : assoc){
						newAssoc.add(p);
						if(p.getSecond().equals(t.getGUID())){
							out.println("<br />Re-linking to slot:" + s.getName());							
							PSPair<IPSGuid,IPSGuid> newP = new PSPair<IPSGuid,IPSGuid>(p.getFirst(),copyGuid);
							newAssoc.add(newP);					
						}
					}
					s.setSlotAssociations(newAssoc);
					out.println("<br />Saving slot:" +s.getName());
					asvc.saveSlot(s);
				}
				
			}
			
			
			
		}
 	}
}else{
%>

<script language="javascript" type="text/javascript">  
var siteList = <%= getSiteList(siteSvc,log,out) %>;

addOption = function(selectbox, text, value) {
    var optn = document.createElement("option");
    optn.text = text;
    optn.value = value;
    selectbox.options.add(optn);  
}	

function populateSiteList(){
	var dropdown = document.getElementById("sitelist");
	if (dropdown) {
	    for (var i=0; i < siteList.sites.length;++i){    
	        addOption(dropdown, siteList.sites[i].sitename,siteList.sites[i].siteid);
		}
	}
}
	
	
function populateTemplateList(site){
	
	var d1 = document.getElementById("oldTemplateList");
	var d2 = document.getElementById("newTemplateList");
	var index = site.selectedIndex;
	
	d1.options.length = 0;
	d2.options.length = 0;
	
	for (var i=0; i < siteList.sites[index].templates.length;++i){    
		addOption(d1,siteList.sites[index].templates[i].templatename,siteList.sites[index].templates[i].templateid);
		addOption(d2,siteList.sites[index].templates[i].templatename,siteList.sites[index].templates[i].templateid);
	}
}

$(document).ready(function() {
	populateSiteList();
});
</script>  
<%! 
public void printSiteList(IPSSiteManager siteSvc , Logger logger, JspWriter out){
	List<IPSSite> sites = siteSvc.findAllSites();

	//Print out the Site and Template Lists
	try{
		out.println("<select id='siteid' onchange='showTemplates(this.value)'>");
		for(IPSSite s : sites){ 
	   		out.println("<option value='" + s.getGUID().toStringUntyped() + "' >" +
				s.getName() + "</option>");		   		
		}
		out.println("</select>");
	}catch(IOException e){
		logger.error(e.getLocalizedMessage(),e);		
	}
	
	//Spit out the available templates
	try{
		
		for(IPSSite s : sites){ 
			out.println("<select id='oldTemplate'>");
		 	
			Set<IPSAssemblyTemplate> templates = s.getAssociatedTemplates();
			
			for(IPSAssemblyTemplate t : templates){ 
				out.println("<option value='" + t.getGUID().toStringUntyped() + "' >" +
					t.getGUID().getUUID() + " - " + t.getLabel() + "[" + t.getName() +"]</option>");		   		
			}	
			
			out.println("</select>");
		}
	}catch(IOException e){
		logger.error(e.getLocalizedMessage(),e);		
	}

	//Spit out the new templates.
	try{
		for(IPSSite s : sites){ 
			out.println("<select id='newTemplate'>");
		 	
			Set<IPSAssemblyTemplate> templates = s.getAssociatedTemplates();
			
			for(IPSAssemblyTemplate t : templates){ 
				out.println("<option value='" + t.getGUID().toStringUntyped() + "' >" +
					t.getGUID().getUUID() + " - " + t.getLabel() + "[" + t.getName() +"]</option>");		   		
			}	
			
			out.println("</select>");
		}
	}catch(IOException e){
		logger.error(e.getLocalizedMessage(),e);		
	}
	
	
}

/*
Responsible for returning the JSON array of sites and templates.
*/
private String getSiteList(IPSSiteManager siteSvc , Logger logger, JspWriter out){
	String json = "{\"sites\": [";

	List<IPSSite> sites = siteSvc.findAllSites();
	int sitecount = 0;
	for(IPSSite s : sites){
		
		if(sitecount>0){
			json = json + ",";
		}
		
   		json = json + "{\"siteid\": \"" + s.getGUID().toString() + "\",";
		json = json + "\"sitename\": \"" + s.getName() + "\",";   	
		json = json +  "\"templates\": [";
		//Now build up the site template list 
		Set<IPSAssemblyTemplate> templates = s.getAssociatedTemplates();
		int count=0;
		for(IPSAssemblyTemplate t : templates){ 
			if(count > 0){
				json = json + ","; 
			}
			json = json + "{\"templateid\": \"" + t.getGUID().toString() + "\",";
			json = json + "\"templatename\": \"" + t.getName() + "\",";
			json = json + "\"templatelabel\": \"" + t.getLabel() + "\"}";
			count++;
		}
		json = json +"]}";
		sitecount++;
	}	
	json = json + "]}";
	return json;
} %>
<csrf:form method="post" action="DispatchTemplateGenerator.jsp">
	<label for="sitelist">Select A Site:</label><select name="sitelist" id="sitelist">
	</select>
	<br />
	<input type="submit" />
</csrf:form>
<% } %>
</body>
</html>

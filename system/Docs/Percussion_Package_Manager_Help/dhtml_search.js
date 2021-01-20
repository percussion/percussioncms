/* Generated with AuthorIT version 4.5.610  6/2/2009 1:53:09 PM */
Page=new Array();
Page[0]=new Array("Use the Package Management web application to","review and manage the packages installed on your Percussion CM Server; and","maintain the Community visibility of the packages installed on your Percussion CM Server ","About Package Manager",
"19948.htm");
Page[1]=new Array("Use the Packages tab to manage packages. On this tab you can:","verify package configurations","reapply package configuration settings","reapply package visibility settings","uninstall packages ","Managing Packages",
"19949.htm");
Page[2]=new Array("The Packages tab is the default tab of the Package Manager.","The table on the tab consists of the following columns:","Package Status","Indicates the condition of the package. Packages can be in one of the following states:","State ","Icon ","Description","Installed ","Package was installed successfully.","Installation Error ","Error occurred during package installation; package was not installed successfully and cannot be configured.","Uninstalled ","Package was removed from the system.","Configuration Status","Indicates the configuration condition of the package. Package configuration can be in one of the following states:","State ","Icon ","Description","Configured ","Package was configured successfully.","Configuration Error","An error occurred in package configuration; configurations were not updated.","Unconfigured ","(no icon)","That package was not configured; possible reasons the package was not configured include: the package did not allow configuration, the package has been uninstalled.","Name","Name of the package. The package name takes the format Publisher_Prefix.Solution_Name; for example, perc.Example is one of the example packages shipped to support Percussion's Packaging suite.","Description","Description of the package from the package vendor.","Publisher","Name of the organization that supplies or distributes the package.","Version","The latest version of the package that has been installed.","By Whom","Username of the Percussion CM System user that installed the package.","Last Modified Date","The most recent date the package was changed. Changes include installation, uninstallation, or configuration.","Package Tab",
"19953.htm");
Page[3]=new Array("All columns are included in the table on the Package tab by default, but you can remove columns from the table. To remove a column from the table, click on the drop arrow in any column header and from the drop menu, choose Columns, and select the column you want to remove. You can remove multiple columns at one time","Rows in the table can be sorted on any column. To sort by a column, click on the drop arrow in the column header of the column you want to use to sort, and from the drop menu choose Sort Ascending or Sort Descending. Any new sort you choose overrides an existing sort; you cannot sort within a sort.","The data in a column can be locked to scrolling like the data in a spreadsheet. To lock the data in a column, click on the drop arrow in the column header of the column you want to lock, and from the drop menu choose Freeze. Choose Unfreeze to unlock a locked column. ","Package Tab Options",
"19954.htm");
Page[4]=new Array("Verifying a package allows you to check the following conditions:","whether the package was modified outside of standard configuration (for example in the Percussion Workbench); or","whether a package dependency is missing ","To verify a package, check the box in the row of the package you want to verify, then click the [Verify] button. If any design objects in the package have been modified outside of configuration, the system displays a message with a list of modified design objects.","If any dependencies are missing, the system displays a message with a list of missing dependencies.","Verifying Package Configurations",
"19955.htm");
Page[5]=new Array("Package configurations can be reapplied. For example, you may want to reapply configurations if deisgn objects were modified outside of configuration, such as directly in the Percussion Workbench.","If you create new design objects of the same type as are included in a package, the configurations will be applied to the new design objects when reapplying the configurations. For example, suppose you have installed a package that includes one or more Sites, which all have a set of Context Variables. Later you add a new Site to your implementation. To add the existing Context Variables to your new Site, update the configurations to add Context Variable values for the new Site, then reapply the configuration. The Context Variables will be added to the new Site.","To reapply package configurations, check the box in the row of the package whose configurations you want to reapply, then click on the [Reapply] button and from the drop list, choose Configuration Settings. The server attempts to reapply the configurations and returns a message indicating whether the processing was successful. ","Reapplying Package Configuration Settings",
"19956.htm");
Page[6]=new Array("When you uninstall a package, the design objects are deleted from the system.","Note that if a package includes design objects that are in use, those design objects will not be deleted when you uninstall the package (other design objects will be deleted normally). The system will display a warning message informing you which design objects will not be deleted.","For example, if you want to uninstall a package that includes Workflow, but Content Items have been created and exist any of those Workflows, the Workflows will not be deleted. Similarly, if you want to uninstall a package that includes Content Types, but you have created Content Items of those Content Types, the Content Types will not be deleted.","To uninstall a package, check the box in the row of the package you want to delete, then click the [Uninstall] button. The system displays a confirmation dialog. Click [OK] to uinstall the package or [Cancel] to stop the uninstall action. ","Uninstalling Packages",
"19957.htm");
Page[7]=new Array("When a package is installed, it is not associated with any Communities, so the design objects in the package are not available to business users. You must associate a package with Communities so business users in those Communities can see and use the design objects in the package. Use the Visibility tab of the Package Manager to maintain the Community visibility of packages.","By default, the Visibility tab displays package visibility by Communities. The [Show by Communities] button toggles to an alternative view displaying Communities and the packages visible to that Community. In this view, the [Show by Packages] button toggles to the default Packages by Communities view.","Managing Package Visibility",
"19950.htm");
Page[8]=new Array("To change package associations in the &quot;Show by Packages&quot; view:","Select the package whose associations you want to change.","Click the [Edit Assocations] button","Package Manager displays the Edit Community Associations dialog.","In the Available list, choose the Communities you want to associate with the package. Standard multi-select options are available.","Click the (arrow graphic) to move the Communities to the Selected list. You can also use drag and drop to move Communities.","To remove Community associations, move them from the Selected list to the Available list.","Click the [OK] button.","The system saves your changes. ","Changing Package Association to Communities",
"19958.htm");
Page[9]=new Array("To change package associations in the &quot;Show by Communities&quot; view:","Select the Community whose associations you want to change.","Click the [Edit Associations] button.","Package Manager displays the Edit Package Associations dialog.","In the Available list, choose the packages you want to associate with the Community. Standard multi-select options are available.","Click the (arrow graphic) to move the packages to the Selected list. You can also use drag and drop to move packages.","To remove package associations, move them from the Selected list to the Available list.","Click the [OK] button.","The system saves your changes. ","Changing Community Association to Packages",
"19959.htm");
Page[10]=new Array("Like configurations, visibility settings may be changed outside of configuration, directly in the Percussion Workbench. In that case, you may want to reapply the configured visibility settings.","You can also use this capability to apply visibility settings developed in one server tier to another server tier. For example, if you define a set of visibility settings in your development tier, you can copy the visibility settings to the production tier. Reapplying the visibility settings updates the production tier with the visibility settings you defined in development.","To reapply visibility settings, check the box in the row of the package whose settings you want to reapply, then click on the [Reapply] button and from the drop list, choose Visibility Settings. The server attempts to reapply the visibility settings and returns a message indicating whether the processing was successful. ","Reapplying Package Visibility Settings",
"19960.htm");
var PageCount=11;

function search(SearchWord){
var Result="";
var NrRes=0;
Result='<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">\n';
Result+="<html>\n";
Result+="<head>\n";
Result+="<meta http-equiv='Content-Type' content='text/html; charset=ISO-8859-1'>\n";
Result+="<title>Search Results</title>\n";
Result+='<script language="javascript" charset="ISO-8859-1" src="dhtml_search.js"></script>\n';
Result+='<link rel="stylesheet" type="text/css" href="stylesheet.css">\n';
Result+="<style type='text/css'>\n";
Result+=".searchDetails {font-family:verdana; font-size:8pt; font-weight:bold}\n";
Result+=".searchResults {font-family:verdana; font-size:8pt}\n";
Result+="</style>\n";
Result+="</head>\n";
Result+="<body onload='javascript:document.SearchForm.SearchText.focus()'>\n";
Result+='<table class="searchDetails" border="0" cellspacing="0" cellpadding="2" width="100%">\n';
Result+='<tr><td>Enter a keyword or phrase</td></tr>';
Result+='<tr><td>';
Result+='<form name="SearchForm" action="javascript:search(document.SearchForm.SearchText.value)">';
if(SearchWord.length>=1){
   while(SearchWord.indexOf("<")>-1 || SearchWord.indexOf(">")>-1 || SearchWord.indexOf('"')>-1){
       SearchWord=SearchWord.replace("<","&lt;").replace(">","&gt;").replace('"',"&quot;");
   }
}
Result+='<input type="text" name="SearchText" size="25" value="' + SearchWord + '" />';
Result+='&nbsp;<input type="submit" value="&nbsp;Go&nbsp;"/></form>';
Result+='</td></tr></table>\n';

if(SearchWord.length>=1){
   SearchWord=SearchWord.toLowerCase();
   this.status="Searching, please wait...";
   Result+="<table border='0' cellpadding='5' class='searchResults' width='100%'>";
   for(j=0;j<PageCount;j++){
       k=Page[j].length-1;
       for(i=0;i<k;i++){
           WordPos=Page[j][i].toLowerCase().indexOf(SearchWord);
           if(WordPos>-1){
               FoundWord=Page[j][i].substr(WordPos,SearchWord.length);
               NrRes++;
               Result+="<tr><td>";
               Result+="<a target='BODY' href='"+Page[j][k]+"'>"+Page[j][k-1].replace(FoundWord,FoundWord.bold())+"</a><br/>\n";

               if(i<k-1){
                   if(Page[j][i].length>350){
                       Result+="..."+Page[j][i].substr(WordPos-100,200+FoundWord.length).replace(FoundWord,FoundWord.bold())+"...\n";
                   }
                   else{
                       Result+=Page[j][i].replace(FoundWord,FoundWord.bold())+"\n";
                   }
               }
               Result+="</td></tr>";
               break;
           }
       }
   }
   Result+="</table>";
   Result+="<p class='searchDetails'>&nbsp;" + NrRes + " result(s) found.</p>";
}

Result+="</body></html>";
this.status="";
this.document.open();
this.document.write(Result);
this.document.close();
}

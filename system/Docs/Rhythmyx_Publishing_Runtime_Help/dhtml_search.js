/* Generated with AuthorIT version 4.5.610  6/15/2009 10:41:41 AM */
Page=new Array();
Page[0]=new Array("The dialogs available through the Rhythmyx Publishing Runtime Help are used to run Publishing Editions, to monitor running publishing jobs, and to review logs of previous publishing jobs.","For details about Publishing concepts, see the Rhythmyx Concepts Guide.  For details about implementing Publishing, see the Rhythmyx Implementation Guide, Setting Up the Production Environment, and the Publishing Design Help.  For more information about running, monitoring, and troubleshooting publishing jobs, see the Rhythmyx Administration Manual. For technical information about the Publishing engine, see the Rhythmyx Technical Reference Manual.","About Publishing Runtime",
"17934.htm");
Page[1]=new Array("The Publishing Runtime Interface includes the following dialogs:","Runtime Edition","Publishing Logs","Log View","Published Item Details","Publishing Status","Publishing Runtime Dialogs",
"17939.htm");
Page[2]=new Array("Rhythmyx displays the Runtime Edition dialog when you click on an Edition in the Navigation pane.  Use this dialog to manually start or cancel an Edition, or to review logs for the Edition.","The Runtime Edition dialog always displays the Publishing Logs for the Edition.  When an Edition is running, the dialog displays status data, as in the screenshot below.","If the Edition is not currently running, the dialog displays a [Start] button.  Click this button to start the Edition manually.  If the Edition is currently running, the dialog displays a [Stop] button.  Click this button to stop the Edition.","The list of logs can be sorted on any column.  Click the column header to sort based on that column.  ","Runtime Edition",
"17940.htm");
Page[3]=new Array("Rhythmyx displays the Publishing Logs dialog when you click Publishing Logs under any Edition.  This dialog displays all publishing logs for all Editions published to the Site.  Use this dialog to access publishing logs for the Site.","You can sort the logs based on the data in the following columns:","Job ID (default)","Start Type","Elapsed Time","To sort, click the heading of the column you want to use to sort.  Click once to sort in ascending order.  Click again to sort in descending order.","The Status column indicates the outcome of the publishing run.  This column contains one of the following graphics:","Graphic","Brief description","Full Description","Completed","All Content Lists were processed successfully and all Content Items were published successfully.","Completed with failures","Publishing of one or more Content Items may have failed.  Check the log for the run to identify the Content Items for which publishing failed to determine the causes of the failures.","Publishing of one or more Content Lists may have failed.  Check the console log (&lt;Rhythmyxroot&gt;/console.log) to determine which Content Lists failed and why.","Cancelled","The Content Items were assembled and prepared for delivery but the Edition was cancelled by the user before the Content Items were delivered.","Aborted","The publishing job was aborted.  ","In some cases, the total number of Content Items reported in the log may not equal the total number of Content Items queued for processing.  Logs results are only recorded for Content Items that have been processed by the Assembly Engine.  Content Items that have been queued but not assembled will not have log results. ","Publishing Logs",
"17941.htm");
Page[4]=new Array("Rhythmyx displays the Log View dialog when you click on a specific Log entry in either the Runtime Edition dialog or Publishing Logs dialog and is used to review publishing log data.  The Log View dialog displays a list of the Content Items processed when the Edition was run and summary data for each Content Item.  Summary includes data on whether publishing of the Content Item succeeded or failed. You can access details for each Content Item. ","You can sort the logs based on the data in the following columns:","Content ID","Location (sorts based only on output location)","Elapsed Time","Operation","Status","Delivery Type","To sort, click the heading of the column you want to use to sort.  Click once to sort in ascending order.  Click again to sort in descending order.","Log View",
"17942.htm");
Page[5]=new Array("Rhythmyx displays the Publishing Status dialog when you click Publishing Status in the Navigation pane.  The Publishing Status dialog displays summary information about all Editions that were run in the previous hour.  You can click on any Edition to display the Runtime Edition dialog for that Edition.","Publishing Status",
"17944.htm");
Page[6]=new Array("In a production environment, Rhythmyx usually runs Editions automatically on a defined schedule.  In the development environment, Editions are usually run manually so you can review the results of your modifications as you develop your system.  (Editions are sometimes run manually in the production environment as well.)  To run an Edition:","In the Navigation pane, expand the Sites node, and expand the Site with which the Edition you want to run is associated.","Double-click on the Edition you want to run.","The View and edit pane displays the Runtime Edition dialog.","In the Menu bar, click Start.","Rhythmyx starts running the Edition.  The dialog displays runtime status data, including a progress bar indicating progress in processing the Edition, the start time of the Edition, and the elapsed time of Edition processing.  Note that due to differences in system performance, a total processing time for the Edition cannot be projected.","When processing of the Edition is complete, a new Published Log Entry is added to the dialog and the status data is removed.","Running an Edition",
"17945.htm");
Page[7]=new Array("When you cancel an Edition, all processing of that Edition stops and any output that has not been delivered is discarded.  Delivered output is not changed, however.  ","To stop an Edition:","In the Navigation pane, double-click on Publishing Status.","The View and Edit pane displays the Publishing Status dialog.","Select the Edition you want to cancel.","In the Menu bar, click Stop.","You can also cancel an Edition from the Runtime Edition dialog.  If the Edition is running, the Stop menu option is enabled.  Click Stop to cancel the Edition.","Cancelling an Edition",
"17946.htm");
Page[8]=new Array("Rhythmyx displays the Published Item Detail dialog when you double-click on a Content Item in the Log View dialog and is used to review publishing log data.     This dialog displays detailed publishing information for the Content Item in the Edition job.  If publishing of the Content Item failed, a detailed error message explaining the failure is displayed.  In the screenshot below, for example, the FTP server could not authenticate Rhythmyx when it tried to deliver the Content Item.","Published Item Detail",
"17943.htm");
Page[9]=new Array("Log data can be exported as an XML file.  ","To export data from the Publishing Logs dialog, select the log you want to export and from the Menu bar, choose Edit &gt; Export Selected Log as XML.","To export data from the Log View, in the Menu bar, choose Edit &gt; Export Log as XML.","Exporting Log Data",
"17957.htm");
Page[10]=new Array("Rhythmyx maintains a log of the results of each Edition publishing job.  You can access these logs in two ways:","The Runtime Edition dialog lists all logs for the Edition.","The Publishing Logs dialog lists all logs for all Editions in the Site.","The Status column indicates the outcome of the publishing run.  This column contains one of the following graphics:","Graphic","Brief description","Full Description","Completed","All Content Lists were processed successfully and all Content Items were published successfully.","Completed with failures","Publishing of one or more Content Items may have failed.  Check the log for the run to identify the Content Items for which publishing failed to determine the causes of the failures.","Publishing of one or more Content Lists may have failed.  Check the console log (&lt;Rhythmyxroot&gt;/console.log) to determine which Content Lists failed and why.","Cancelled","The Content Items were assembled and prepared for delivery but the Edition was cancelled by the user before the Content Items were delivered.","Aborted","The publishing job was aborted.  ","In some cases, the total number of Content Items reported in the log may not equal the total number of Content Items queued for processing.  Logs results are only recorded for Content Items that have been processed by the Assembly Engine.  Content Items that have been queued but not assembled will not have log results. ","The Publishing Status page lists all Editions currently running or that were run within the past hour.  ","You can access details of each log.  When you click on the log entry, Rhythmyx displays the Log View in the View and Edit pane.","The Log View displays a list of the Content Items published with summary information about each Content Item.  For details about a Content Item, double-click on the Content Item.  Rhythmyx displays the Published Item Details in the View and Edit pane.","The Published Item Details includes publishing details for the Content Item in the Edition job.  If publishing of the Content Item failed, the dialog displays an detailed error message.","Reviewing Publishing Logs",
"17947.htm");
Page[11]=new Array("Under the default configuration, Rhythmyx automatically purges publishing logs after one month.  You can modify this configuration to change the frequency with which logs are pruned and to archive pruned logs instead of purging them.  For details about configuring these options, see the Rhythmyx Administration Manual.","You can manually purge or archive logs as well.","To prune logs from the Runtime Edition page, select the logs you want to prune and","to delete the logs, in the Menu bar,  Action &gt; Delete Selected Logs;","to archive the logs, in the Menu bar, choose Action &gt; Archive Selected Logs.","To prune logs from the Publishing Logs page, select the logs you want to prune and ","to delete the logs, in the Menu bar,  Action &gt; Delete Selected Logs;","to archive the logs, in the Menu bar, choose Action &gt; Archive Selected Logs.","Archived logs are stored in the directory &lt;Rhythmyxroot&gt;/AppServer/server/rx/deploy/publogx.war as XML files with the name publog_&lt;id&gt;.xml where &lt;id&gt; is the publishing job ID of the archived log; for example, publog_109.xml. ","Pruning Publishing Logs",
"17949.htm");
Page[12]=new Array("For an overview of the status of all Editions currently running or recently run in the system, in the Navigation pane, click Publishing Status.  The View and Edit pane displays the Publishing Status dialog","This dialog displays summaries of all Editions either currently running or that finished processing in the past hour.  For details about a specific Edition, click on that Edition to see the Runtime Edition dialog.","Reviewing Publishing Status",
"17948.htm");
Page[13]=new Array("Use Publishing logs to monitor the publication of localized content.","If your Publishing Model is site-centric (publishes localized content to unique sites or destinations), you will have unique Sites and Editions for each Locale.  Review the log for each Edition to determine whether the content of the Edition published correctly.","If your Publishing Model is content-centric (publishes all content to a single site or destination), you use a single Edition that includes the pages for all Localized versions of your content.  Check the Single Published Item details for each Content Item to see if the different versions within the Edition published correctly.","See the document Internationalizing and Localizing Rhythmyx for more information about localization.","If Rhythmyx publishes your Editions or Edition, but does not publish some content items, republish the failed content.","Monitoring Publication of Localized Content",
"Monitoring_Publication_of_Localized_Content.htm");
Page[14]=new Array("Rhythmxy maintains a record of all Content Items published to a Site.  You may occasionally need to clear this record.  The most common case when a Site record should be cleared is when published files have been deleted from the delivery location, rendering the published Site out of sync with the Site record in the database, but other cases may occur as well.","To clear a Site record:","Expand the Sites node. Expand the Site whose record you want to clear. Click on the Publishing Logs link.","In the Menu bar, choose Action &gt; Delete Site Item Entries.","Note that once a Site record is cleared, an incremental publish will act as a full publish (Rhythmyx will have no record of any Content Items being published to that Site.)  Recommended practice is to run a full publish of a Site immediately after clearing the Site record to update the record with current data.  Incremental publishing runs after this full publish will work correctly.","Clearing a Site Record",
"20126.htm");
var PageCount=15;

function search(SearchWord){
var Result="";
var NrRes=0;
Result='<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">\n';
Result+="<html>\n";
Result+="<head>\n";
Result+="<meta http-equiv='Content-Type' content='text/html; charset=ISO-8859-1'>\n";
Result+="<title>Search Results</title>\n";
Result+='<script language="javascript" type="text/javascript" charset="ISO-8859-1" src="dhtml_search.js"></script>\n';
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

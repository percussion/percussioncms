// These functions are used for Ajax update of the publishing pages for the
// edition status. Make sure to include the yui scripts if you wish to use 
// these functions.


PS = {};

/**
 * Core class for edition runtime pages provides common functionality.
 */
PS.edition = {
   // Lock to take on each async run. As long as the lock is held, a new run won't 
   // start. This is a poor man's mutex based on the notion that the updates aren't 
   // that rapid.
   lock : 0,

   // Take the lock, return false if the lock couldn't be taken
   takeLock:function()
   {
      if (this.lock == 0)
      {
         this.lock = 1;
         return true;
      }
      return false;
   },

   releaseLock:function()
   {
      this.lock = 0;
   },

   // Determine from the status what the run state is for the job. Not
   // active counts as not running.
   isNotRunning:function(val)
   {
      return val == null || val == '' || val == 'INACTIVE'
            || val == 'CANCELLED' || val == 'ABORTED' || val == 'COMPLETED'
            || val == 'COMPLETED_W_FAILURE';
   },
   
   // Update a specific element. The prefix plus the element name locates the
   // element by id.
   updateElement:function(prefix, name, value)
   {
      var el = document.getElementById(prefix + name);
      if (el != null)
      {
         el.innerHTML = value;
      }
   },

   // Update the ui for a given row. The row elements are identified by the
   // prefix
   updateUI:function(row, prefix)
   {
      var progress = document.getElementById(prefix + "_status_progress");
      if (progress != null)
      {
         progress.style.width = '' + row.percent  + '%';
      }
      this.updateElement(prefix, '_status_queued', row.queued);
      this.updateElement(prefix, '_status_prepared', row.prepared);
      this.updateElement(prefix, '_status_delivered', row.delivered);
      this.updateElement(prefix, '_status_failed', row.failed);
      this.updateElement(prefix, '_status_state', row.state);
      this.updateElement(prefix, '_status_statename', row.statename);
      this.updateElement(prefix, '_start_time', row.start_time);
      this.updateElement(prefix, '_elapsed_time', row.elapsed_time);
   },
   
   findInputChild:function(element)
   {
      var list = element.getElementsByTagName('INPUT');
      if (list.length > 0)
      {
         return list[0];
      }
      else
      {
         return null;
      }
   },

   debug:function(output)
   {
      var el = document.getElementById("debugging");
      var text = el.innerHTML;
      if (text == null)
      {
         text = output;
      }
      else
      {
         text = output + "<br>" + text;
      }
      if (text.length > 200)
      {
         text = text.substring(0, 200);
      }
      el.innerHTML = text;
   }
}

/**
 * Single edition runtime functions
 */ 
PS.editionrt = {
   // Track whether there needs to be a refresh of the page to update the logs. 
   // This is used on the edition runtime page
   status_update_needed: 0,

   // How long to wait before the update, this prevents the screen from flashing
   // as soon as the state goes to complete on the edition runtime page.
   wait_count: 4,
   
   // Setup the buttons for starting the job
   jobNotRunning:function()
   {
      var job_panel = document.getElementById("job_panel");
      job_panel.style.display = 'none';
      var start_button = document.getElementById("start_button");
      var stop_button = document.getElementById("stop_button");
      start_button.setAttribute("href","javascript:void(0)");
      stop_button.setAttribute("href","javascript:void(0)");
      this.__enableLink(start_button);
      this.__disableLink(stop_button);
   },

   // Setup the buttons for stopping the job and show the runtime panel
   jobRunning:function()
   {
      var job_panel = document.getElementById("job_panel");
      job_panel.style.display = 'block';
      var start_button = document.getElementById("start_button");
      var stop_button = document.getElementById("stop_button");
      start_button.setAttribute("href","javascript:void(0)");
      stop_button.setAttribute("href","javascript:void(0)");
      this.__enableLink(stop_button);
      this.__disableLink(start_button);
   },
   
   //Enables a link
   __enableLink:function(linkElem)
   {
      if(!linkElem)
         return;
      this.__renameAttribute(linkElem,"onclick_bkp","onclick");
      YAHOO.util.Dom.setStyle(linkElem.getAttribute("id"),"color","black")
   },

   //Disables a link
   __disableLink:function(linkElem)
   {
      if(!linkElem)
         return;
      this.__renameAttribute(linkElem,"onclick","onclick_bkp");
      YAHOO.util.Dom.setStyle(linkElem.getAttribute("id"),"color","gray")
   },

   //Renames the attribute 
   __renameAttribute:function(elem,from,to)
   {
      if(!elem)
         return;
      var attr = elem.getAttribute(from);
      if(attr)
      {
         elem.setAttribute(to,attr);
         elem.removeAttribute(from);
      }
   },

   // Update the UI for the runtime page
   singleUpdateUI:function(o)
   {
      var result = eval('(' + o.responseText + ')');
      var row = result[0];
      if (PS.edition.isNotRunning(row.statename))
      {
         if (PS.editionrt.wait_count > 0)
         {
            PS.editionrt.wait_count--;
         }
         else
         {
            PS.editionrt.jobNotRunning();
            if (PS.editionrt.status_update_needed == 1)
            {
            PS.editionrt.status_update_needed = 0;
               document.getElementById("pubruntime").submit();
               return;
            }
         }
      }
      PS.editionrt.jobRunning();
     PS.editionrt.status_update_needed = 1;
      PS.edition.updateUI(row, '');
   },

   // Failure updater, just try to show something useful.
   showFailureOnUI:function(o)
   {
      PS.editionrt.jobNotRunning();
      PS.edition.updateElement('', '_status_state' + "AJAX failure:" + o.responseText());
   },

   // Callback that sets the initial state of the page from a call to the
   // job status servlet.
   editionRuntimePageInitialize:function(o)
   {
      var result = eval('(' + o.responseText + ')');
      
      if (result.length == 0)
      {
         PS.editionrt.jobNotRunning();
      }
      else
      {
	      var first = result[0];
	   
	      if (PS.edition.isNotRunning(first.statename))
	      {
	         PS.editionrt.jobNotRunning();
	      }
	      else
	      {
	         PS.editionrt.jobRunning();
	         setInterval("PS.editionrt.doEditionUpdate()",3000);
	      }
      }
   },

   startEditionUpdateTimer:function()
   {
   
      PS.editionrt.jobNotRunning();
      var edition = document.getElementById("edition_id");
      YAHOO.util.Connect.asyncRequest('GET', 
         '/Rhythmyx/publisher/status?edition=' + edition.getAttribute("value"),
          { success: this.editionRuntimePageInitialize });
   },

   doEditionUpdate:function()
   {
      if (PS.edition.takeLock())
      {
         var edition = document.getElementById("edition_id");
         YAHOO.util.Connect.asyncRequest('GET', 
            '/Rhythmyx/publisher/status?edition=' + edition.getAttribute("value"),
             { success: this.singleUpdateUI, failure: this.showFailureOnUI });  
         PS.edition.releaseLock();
      }
   }
}

/**
 * Demand publishing page show running demand publish.
 */
PS.demand = {
   // How many milliseconds to wait between updates
   interval: 1000,

   // How many tics to wait before the windows pops down. Each count is 1 seconds
   // given the interval in startEditionUpdateTimer
   wait_count: 80,
   
   startEditionUpdateTimer:function()
   {
      this.doUpdate();   
      setInterval("PS.demand.doUpdate()",this.interval);
   },
   
   doUpdate:function()
   {
      if (PS.edition.takeLock())
      {
         var rid = document.getElementById("requestid");
         YAHOO.util.Connect.asyncRequest('GET', 
               '/Rhythmyx/publisher/status?requestid=' + rid.value,
         { success: this.callback });    
         PS.edition.releaseLock();
      }
   },
   
   // Update the UI for the runtime page
   callback:function(o)
   {
      var result = eval('(' + o.responseText + ')');
      if (result.length == 0)
      {
        return;
      }
      var row = result[0];
      if (PS.edition.isNotRunning(row.statename))
      {
         if (this.wait_count > 0)
         {
            this.wait_count--;
         }
         else
         {
            if (this.status_update_needed == 1)
            {
               window.close();
               return;
            }
         }
      }
      PS.edition.updateUI(row, '');
   }
}
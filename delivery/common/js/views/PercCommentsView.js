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

(function($) {
    // THIS IS USED TO MANAGE THE DELIVERY SERVICES ULRS AS PASSED FROM CM1 TO THE STATIC PAGES
    var deliveryServicesURL = "";
    var currentWidgetCssDefinition = "";

    function getDeliveryServicesDomain(widgetClass) {
        var currentWidget = $(widgetClass);
        var queryString = "";
        var deliveryUrl = "";
        try {
            if ("" === currentWidget.attr("data-query")){ return;}
            queryString = currentWidget.attr("data-query");

            if("undefined" !== typeof(queryString)) {
                queryString = JSON.parse(queryString);
                if ("undefined" !== typeof (queryString.deliveryurl)) {
                    deliveryUrl = queryString.deliveryurl;
                    delete queryString.deliveryurl;
                }
            }
        }
        catch (err) {
            //do nothing
            console.debug(err);
        }
        return deliveryUrl;
    }

    function joinURL(firstPart, secondPart){
        if("undefined" !== typeof (firstPart)){
            if(firstPart.endsWith("/")){
                firstPart = firstPart.substring(0,firstPart.length-1);
            }
        }else{
            firstPart="";
        }

        if("undefined" !== typeof (secondPart)){
            if(secondPart.startsWith("/")){
                secondPart = secondPart.substring(1);
            }
        }else{
            secondPart="";
        }

        return firstPart+ "/" + secondPart;
    }


    $(document).ready(function(){
        //Change the form action url to point to the right server
        deliveryServicesURL = getDeliveryServicesDomain(".perc-comments-view");
        var version = typeof($.getCMSVersion) === "function" ?$.getCMSVersion():"";
        var url =  joinURL(deliveryServicesURL, "/perc-comments-services/comment?perc-version=" + version);
        $("form[name = 'commentForm']").attr("action", url);
    });

    var globals = {
        dateFormatter: null 
    }; 
    
    var settings = {
        serviceurl: null, // The comments service url. Expects no query string.
        site: null, // The sitename
        pagepath: null, 
        tag: null,
        username: null,
        state: 'APPROVED', // either APPROVED or REJECTED
        moderated: null,
        fields: ['createdDate', 'username', {name: 'title', element: 'h3'}, 'text'],
        /*
         The fields array property defines what fields will show and their sort order and containment.
         The following fields are available:
            createdDate,
            email,
            text,
            title,
            username
        
         Fields can be added to the array as a string or an object. The object version gives
         more advanced options as follows:
            {
               name: '', // The field name
               element: '', // The element to use to display field value, defaults to the defaultElement
               posttext: '', // Text to add after the field value
               pretext: ''  // Text to add before the field value
            }
        
         Simple field containment can be defined by adding the desired fields with another array.
         Example:
            [['createdDate', 'username'], [{name: 'title', element: 'h3'}, 'text']],
            
            This will wrap containers around each sets of fields with each having classname
            as comment-container-n  where n is the positional containment index order.
            
            Containment is only supported one level deep and either all fields have a container or
            none should. No mixing is supported.
       */
        defaultElement: 'div', // The default element used to build the comment structure
        defaultClass: "perc-comments", // The default class added to the intial comments wrapper element
        dateformat: 'EEEE, MMMM d, yyyy hh:mm aa', // looks like Tuesday, March 15, 2011 11:11 PM 
        maxResults: 0, // Maximum returned results, 0 or less indicates return maximum possible
        sortby: 'CREATEDDATE', // sortby field, CREATEDATE, EMAIL, USERNAME
        ascending: true,
        lastCommentId: 0
    };
    
    // Publically exposed methods
    var methods = {
        init: function(options) {
            return this.each(function() {
                if(options){ 
                    $.extend( settings, options );
                }
            globals.dateFormatter = new SimpleDateFormat(settings.dateformat);
            var querystring = $.deparam.querystring();
            if ("undefined" !== typeof (querystring.lastCommentId)) {
                settings.lastCommentId = querystring.lastCommentId;
            }
            });
        },
        show: function() {
            var el = this;
            el.addClass(settings.defaultClass);
            getComments(function(success, data) {
                if(success && 'undefined' !== typeof (data.comments) && null !== data.comments)
                {
                    var comments = data.comments; 
                    for(let c = 0; c < comments.length; c++)
                    {
                        el.append(createCommentHtml(comments[c]));
                        el.append($("<div/>").addClass("perc-comment-divider"));
                    }
                }
                else
                {
                    console.error('Error retrieveing comments from DTS service.');
                }
                var lastComment = $('.perc-comment-highlight');
                if (lastComment.position()) {
                    $('html,body').animate({ scrollTop : lastComment.position().top }, 200 );
                }
            });
        }
    };
    // Define the plugin on the jQuery namespace
    $.fn.PercCommentsView = function(method)
    {
        // Method calling logic
        if ( methods[method] ) {
            return methods[ method ].apply( this, Array.prototype.slice.call( arguments, 1 ));
        } else if ( 'object' === typeof method || ! method ) {
            return methods.init.apply( this, arguments );
        } else {
            $.error( 'Method ' +  method + ' does not exist on jQuery.PercCommentsRenderer' );
        }
    };

    // private methods

   /**
    * Calls the service to retrieve comment data. The call is via jsonp so
    * it is cross domain compatible.
    */
    function getComments(callback)
    {
        /*$.jsonp({
            url: getUrl(),
            data: '',
            success: function(data, status){
                callback(true, data);
            },
            error: function(xOptions, error)
            {
                callback(false);
            }
        });
*/
        var fullLink =  getUrl();
        var splittedLink = fullLink.split("?");
        var href = splittedLink[0];
        var qs = splittedLink[1];
        var splittedQs = qs.split('&');
        var body = {};

        for(var key in splittedQs) {
            var t = splittedQs[key].split('=');
            body[t[0]] = t[1];
        }

        $.ajax({
            type: 'POST',
            data: JSON.stringify(body),
            crossDomain: true,
            headers: {  'Access-Control-Allow-Origin': '*' },
            contentType: "application/json; charset=utf-8",
            url: href,
            success: function(data, status){
                callback(true, data);
            },
            error: function(xOptions, error)
            {
                callback(false);
            }
        });
    }
    
   /**
    * Create the url from the specified settings.
    * @returns the url string, never <code>null</code> or empty.
    * @type string
    */
    function getUrl()
    {
        var version = typeof($.getCMSVersion) === "function"?$.getCMSVersion():"";
        
        //Build a valid service URL based on the value stored in the delivery
        var url = deliveryServicesURL + "/perc-comments-services/comment/jsonp";

        url += "?site=" + settings.site;
        if(!isBlank(settings.pagepath))
            url += "&pagepath=" + settings.pagepath;
        if(!isBlank(settings.username))
            url += "&username=" + settings.username;
        if(!isBlank(settings.sortby))
        {
            url += "&sortby=" + settings.sortby;
            url += "&ascending=" + settings.ascending;
        }
        if(!isBlank(settings.tag))
            url += "&tag=" + settings.tag;
        if(!isBlank(settings.state))
            url += "&state=" + settings.state;   
        if(!isBlank(settings.moderated))
            url += "&moderated=" + settings.moderated;
        if(!isBlank(settings.lastCommentId))
            url += "&lastCommentId=" + settings.lastCommentId;
        url += "&callback=?"; //must be last
        return url;
    }
    
   /**
    * Create the comments html from the commentData returned from
    * the server. What fields are shown and their order is based on the 
    * data in the settings.fields array.
    * <pre>
    *  Produces the following HTML:
    *  &lt;div class=&quot;comments&quot;>
    *        &lt;div class=&quot;comment&quot;>
    *           &lt;div class=&quot;comment-createdDate&quot;>Friday, February 18, 2011 01:30 PM ET&lt;/div>
    *           &lt;h4 class=&quot;comment-username&quot;>&lt;a href=&quot;http://www.mysite.com&quot;>Bob&lt;/a>&lt;/h4>
    *            
    *           &lt;h3 class=&quot;comment-title&quot;>The comment Title&lt;/h3> 
    *           &lt;div class=&quot;comment-text&quot;>
    *           The comment Text
    *           &lt;/div>
    *        &lt;/div>
    *        &lt;div class=&quot;comment-divider&quot;>&lt;/div>
    *        &lt;div class=&quot;comment&quot;>
    *           &lt;div class=&quot;comment-createdDate&quot;>Friday, February 18, 2011 01:30 PM ET&lt;/div>
    *           &lt;h4 class=&quot;comment-username&quot;>&lt;a href=&quot;http://www.mysite.com&quot;>Sal&lt;/a>&lt;/h4>
    *           &lt;h3 class=&quot;comment-title&quot;>The comment Title&lt;/h3>
    *           &lt;div class=&quot;comment-text&quot;>
    *           The comment Text
    *           &lt;/div>
    *        &lt;/div>
    *         &lt;div class=&quot;comment-divider&quot;>&lt;/div>
    *     &lt;/div>
    * </pre>
    * @param commentData a single comments data object, assumed not
    * <code>null</code>.
    * @returns a jQuery object that represents the comment HTML. Never
    * <code>null</code> or empty.
    * @type jQuery
    */
    function createCommentHtml(commentData)
    {
        if(typeof commentData["commentCreatedDate"] !== "undefined"){
            commentData["createdDate"] = commentData["commentCreatedDate"];
        }

        var comment = $("<div/>").addClass('perc-comment');
        var field = null;
        var snippet = null;
        if(0 < settings.fields.length)
        {
            //Highlight the recent comment
            if(settings.lastCommentId === commentData["id"]) {
                comment.addClass('perc-comment-highlight');
                if (!commentData["moderated"] && "APPROVED" !== commentData["approvalState"])
                {
                    comment.append(
                            $("<div/>")
                            .addClass('perc-comment-message')
                            .text('Your comment is being held for moderation. It will appear on this page after approval.')
                    )
                }
            }
            if(Array.isArray(settings.fields[0]))
            {
                // Use comment containers
                for(z = 0; z < settings.fields.length; z++)
                {
                   var container = $("<div/>").addClass("comment-container-" + z);
                   comment.append(container);
                   createFieldsHtml(commentData, settings.fields[z], container);
                }
            }
            else
            {
               createFieldsHtml(commentData, settings.fields, comment);
            }
        }
        return comment;
    }
    
   /**
    * Creates the field html.
    * @param commentData {object}, the data for the individual comment brought back
    * from the service. Assumed not <code>null</code>.
    * @param fields {array} the field configuration array. Assumed not
    * <code>null</code>.
    * @param container {jQuery} the container to append the html to,
    * assumed not <code>null</code>.
    */
    function createFieldsHtml(commentData, fields, container)
    {
        for(var i = 0; i < fields.length; i++)
        {
           var field = getFieldObject(fields[i]);
            if(isBlank(commentData[field["name"]]))
                continue;
            var snippet = $("<" + field["element"] + "/>")
                      .addClass("perc-comment-" + field["name"])
                      .html(commentData[field["name"]]);
            if('username' === field["name"] && !(isBlank(commentData.url)))
            {
                var urlSnippet = $("<a/>")
                                .addClass("perc-comment-url")
                                .attr("href", commentData["url"])
                                .text(commentData["url"]);
                snippet.append(' - ', urlSnippet);
            }
            if('createdDate' === field["name"])
            {
                var dt = $.timeago.parse(commentData["createdDate"]);
                snippet.text(globals.dateFormatter.format(dt));
            }
            // Handle pretext
            if(!(isBlank(field.pretext)))
            {
                snippet.html("<span class='comment-pretext'>" + field["pretext"] + "</span>" + snippet.html());
            }
            // Handle posttext
            if(!(isBlank(field.posttext)))
            {
                snippet.html(snippet.html() + "<span class='comment-posttext'>" + field["posttext"] + "</span>");
            }
            container.append(snippet);
        }
    }
    
    /**
     * Helper method to always return a field object with a specified element, even
     * if the string field shortcut is used.
     */ 
    function getFieldObject(rawField)
    {
        if('object' == typeof (rawField))
        {
            if(isBlank(rawField.element)) {
                if (settings.titleFormat) {
                    rawField["element"] = settings.titleFormat;
                }
                else {
                    rawField["element"] = settings.defaultElement;
                }
            }
            return rawField;
        }
        return {name: rawField, element: settings.defaultElement};
    }
    
   /**
    * Helper method to determine if a string object is defined and not empty.
    * @param obj {object} the string object, may be <code>null</code>, empty
    * or <code>undefined</code>.
    * @returns <code>true</code> if the string is considered blank.
    * @type boolean
    */
    function isBlank(obj)
    {
        return 'undefined' === typeof (obj) || null === obj || 0 >= obj.length;
    }
   
   /* Auto bind comments instances for existing elements that have
    * a class of <code>perc-comments-view</code>.
    */
    $(document).ready(function() {
        $('.perc-comments-view').each(function() {
            var $el = $(this);
            var data = $el.attr("data-query");
            if('string' === typeof (data) && 0 < data.length)
            {
                var obj = JSON.parse(data);

                //Look for the special finderpath that only comes from a cm1 call to this plugin
                //from that we set the site and pagepage
                if("undefined" !== typeof (obj.finderpath))
                {
                    var tmp = obj.finderpath.split("/");
                    obj.site = tmp[2];
                    obj.pagepath = "/" + tmp.slice(3).join("/");  
                }
                $el.PercCommentsView(obj);
                $el.PercCommentsView('show');
            }
        });
    });
})(jQuery);

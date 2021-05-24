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

/**
    Miller column widget, by Jason Priestley, 9-14-09 

    defines the following widgets: cml_mcol, cml_direc, cml_diritem.
 **/

(function($) {

/**
   The top-level element containing the Miller column. Individual
   directory levels are held as <td> elements within a single <tr>. An
   extra <td> is kept at the end as a spacer. Another extra <td> is
   kept at the end, before the spacer, to make a smooth removal
   animation. When several columns need to be removed, they are first
   moved into the "remover" <td>, then that <td> is squished to 0
   width, then the elements are removed from the DOM and the remover
   <td> is reset (see: queueRemoval, removeQueued below).


   Usage: 
   <div id="mcol" /> 
   <script> $(document).ready( function () {
   $("#mcol").cml_mcol().cml_mcol('loadURL', 'ajax/call/url'); } ); </script>


   /ajax/call/url should return a JSON list of objects. Current use is
   like this (intended to be customized): 
   [ { 'type': 'file', 'tag': 'filename' }, 
   {'type' : 'dir', 'tag' : 'folder_name', 'url' : 'ajax/call/for/this/dir' } ]

   you can optionally specify a custom icon (current impl will use
   "icons/${icon}.gif" for files, and "icons/${icon}_op.gif" for
   open folders, "icons/${icon}_cl.gif" for closed folders.
 **/
var MillerColumn = {
    _init : function() {
	var thetr = $("<tr/>")
	.append(  $("<td/>").addClass("mcol-remover").append(  $("<div/>").append( $("<table/>").append($("<tr class='remover-row'/>")) ) )  )
	.append(  $("<td/>").addClass("mcol-end-spacer").append( $("<div/>") )  );
	this.element.addClass("ui-widget ui-widget-content mcol").append( $("<table></table>").append(thetr) );
	this._setData("tr", thetr);
	return this.element;
    },
    push : function(col) {
	this._getData("tr").children(".mcol-remover").before(col);
	this.element.stop().animate( {scrollLeft : (col.position().left + this.element.scrollLeft())}, 1000);
    },
    set : function(col, ncol) {
	col.cml_direc('items').cml_diritem('close', true);
	this.push(ncol);
    },

    clear : function() {
	this.removeQueued();
	this.element.find(".mcol-remover").prevAll().remove();
    },

    queueRemoval : function(col) {
	this._getData("tr").children(".mcol-remover").find("tr.remover-row").prepend(col);
    },

    removeQueued : function() {
	this._getData("tr").children(".mcol-remover").children().eq(0)
	.animate( {width: "0px"}, "fast", "linear", function() { $(this).find("tr.remover-row").empty(); $(this).css("width", "auto"); });
	
    },

    loadURL : function(url) {
	var first_col = $('<td/>').cml_direc({'mcol': this.element});
	this.clear();
	fill_dir(url, first_col);
	this.push(first_col);
    }
};



$.widget("ui.cml_mcol", MillerColumn);




/**
   The Directory class represents one level of the directory structure
   within the Miller column. Top-level element is a <td>, below that
   is a <div> so that a min-width can be set, and within the div are
   the directory items.
 **/
var Directory = {
    _init : function() {
	var self = this;
	
	//Set up the HTML structure 
	this.element.addClass("mcol-direc")
		.append( $("<div/>").addClass("mcol-direc-wrapper").addClass("mcol-direc-subdirec") );

	//When a diritem is dropped, it should be added to this directory.
	this._container().droppable({
		drop: function(event, ui) {
		    self.add(ui.draggable);
		    ui.draggable.cml_diritem('option', 'direc', self.element);
		    ui.draggable.hide().fadeIn("slow");
		} });
	return this.element;
    },

    add : function(item) {
	var children = this.items();
	
	//Insert the new item into the correct place in the list according to tag-sorted order.
	var sz = children.length;
	for(let ii = 0; ii <= sz ; ii++) {
	    if( ii === sz ) {
		this._container().append(item);
	    } else {
		if ( item.cml_diritem('tag') < children.eq(ii).cml_diritem('tag') ) {
		    children.eq(ii).before(item);
		    break;
		}
	    }
	}
    },

    _container : function() {
	return this.element.children().eq(0);
    },

    items : function() {
	return this._container().children();
    },

    throb : function() {
	//Hide the real container, and show a loading animation in its place.
	this._container().hide();
	this._container().after( $("<div><img src='/img/throb.gif'/></div>") );
    },

    show : function() {
	//Should be called after "throb". Removes the loading
	//throbber, and shows the container (which should be ready for
	//display).
	this._container().next().remove();
	this._container().show();
    },


    mcol : function () {
	return this._getData('mcol');
    },

    queueRemoval : function () {
	this._getData('mcol').cml_mcol('queueRemoval', this.element);
    }
    
};


$.widget("ui.cml_direc", Directory);

$.ui.cml_direc.getter = "items mcol";


/**
   Make a single DirItem based on a JSON spec retrieved from the
   server. Customize here to use a different JSON format, include more
   information, etc. 
**/
function mk_elem( spec, dr ) {
    if(spec['type'] == 'file') {
	return $("<div/>").cml_diritem({'tag': spec['tag'], 
		    'direc': dr, 
		    'closed_html': file_html(spec) });
    }
    else if (spec['type'] == 'dir') {
	return $("<div/>").cml_diritem({ 'tag': spec['tag'], 
		    'direc': dr, 
		    'opened_html': diropen_html(spec), 
		    'closed_html': dirclosed_html(spec),
		    'opener': url_opener( spec['url'] ) });
    }
    else {
	alert("bad type");
    }
}

/**
   Build an HTML representation of an element based on the JSON spec.
 **/
function file_html(spec) {
    var icn = spec['icon'] || 'file';
    return $("<span/>")
	.append( $("<table/>")
		 .append(  $("<tr />")
			   .append( $("<td/>")
				    .append( $("<img/>").attr("src","icons/" + icn + ".gif")  ))
			   .append( $("<td/>")
				    .append(  $("<span class='mcol-clickable'/>").append(spec['tag'])  ))))
	.html();
}

function diropen_html(spec) {
    var icn = spec['icon'] || 'folder';
    return $("<span/>")
	.append( $("<table/>")
		 .append(  $("<tr />")
			   .append( $("<td/>")
				    .append( $("<img/>").attr("src","icons/" + icn + "_op.gif")  ))
			   .append( $("<td/>")
				    .append(  $("<span class='mcol-clickable'/>").append(spec['tag'])  ))))
	.html();
}

function dirclosed_html(spec) {
    var icn = spec['icon'] || 'folder';
    return $("<span/>")
	.append( $("<table/>")
		 .append(  $("<tr />")
			   .append( $("<td/>")
				    .append( $("<img/>").attr("src","icons/" + icn + "_cl.gif")  ))
			   .append( $("<td/>")
				    .append(  $("<span class='mcol-clickable'/>").append(spec['tag'])  ))))
	.html();
}

function fill_dir(url, dr) {
    dr.cml_direc('throb');
    $.getJSON(url, {}, function( elems ) {
	    for( var ii in elems ) {
		dr.cml_direc('add', mk_elem(elems[ii], dr));
	    }
	    dr.cml_direc('show');
	});
}

/**
   A single diritem. May be a simple item, or may have 'open' and
   'close' behaviors. If it has 'open'/'close' behaviors, then it will
   keep track of the opened directory, and manage closing it.
 **/
var DirItem = {
    _init : function () {
	this._setData('opened', null);
	this.element.html( this._getData('closed_html') );
	this.element.addClass('mcol-listing');
	var self = this;
	this.element.on("click", function() {
		//If the element is being dropped, ignore the click.
		if( ! $(this).is(".ui-draggable-dragging") ) {
		    if( self.element.is('.mcol-opened') ) {
			self.close(false, false);
		    }
		    else {
			self.open();
		    }
		}
	    });
	this.element.draggable({ 'revert': false, 
		    'distance': 15, 
		    'helper' : 'clone',
		    'appendTo' : 'body',
		    //Close the element when it starts being dragged.
		    'start': function() { self.close(); } });
	this.element.each( function () { 
		//Fix IE text selection problem.
		this.ondragstart = function() {return false;};
		this.onselectstart = function() {return false;};
	    } );
		
	return this.element;
    },
    tag : function() {
	return this._getData('tag');
    },
    open : function() {
	var opened = this._getData('opener')(this.element);
	if(opened) { 
	    this.element.addClass("mcol-opened");
	    this.element.html( this._getData('opened_html') );
	}
	this._setData('opened', opened);
	return opened;
    },
    //Close the item. If 'fast' is true, close it without animation or
    //delay. If 'wait' is true, just queue it to be removed (so that
    //the animations can be grouped together by the mcol - this goes
    //more smoothly).
    close : function(fast, wait) {
	val = this._getData('opened');
	if (val) {
	    this._getData('closer')(val,fast,wait);
	    this.element.removeClass("mcol-opened");
	    this.element.html( this._getData('closed_html') );
	    this._setData('opened', null);
	}
    },
    direc : function() {
	return this._getData("direc");
    }
};



$.widget("ui.cml_diritem", DirItem);

$.ui.cml_diritem.getter = "tag open direc";

$.ui.cml_diritem.defaults = {
    'tag': '(nothing)',
    'opener' : function(elem) { return false; },
    'closer' : function(elem, fast, wait) { 
	elem.cml_direc('items').cml_diritem('close', fast, true); 
	if(fast) { 
	    elem.remove(); 
	} else { 
	    elem.cml_direc('queueRemoval');
	    if(!wait) {
		elem.cml_direc('mcol').cml_mcol('removeQueued');
	    }
	} 
    }
};

function url_opener( url ) {
    return function( elem ) {
	var next_col = $('<td/>');
	var mc = elem.cml_diritem('direc').cml_direc('mcol');
	next_col.cml_direc({'mcol': mc});
	fill_dir(url,next_col);
	mc.cml_mcol('set', elem.cml_diritem('direc'), next_col);
	return next_col;
    };
}

})(jQuery);

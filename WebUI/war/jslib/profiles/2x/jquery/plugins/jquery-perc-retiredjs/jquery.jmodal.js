/*
 * jmodal
 * version: 2.0 (05/13/2009)
 * @ jQuery v1.3.*
 *
 * Licensed under the GPL:
 *   http://gplv3.fsf.org
 *
 * Copyright 2008, 2009 Jericho [ thisnamemeansnothing[at]gmail.com ] 
 *  
*/
(function($){
$.extend($.fn, {
    hideJmodal: function() {
        $('#jmodal-overlay').animate({ opacity: 0 }, function() { $(this).css('display', 'none') });
        $('#jquery-jmodal').animate({ opacity: 0 }, function() { $(this).css('display', 'none') });
        $('select').show();
    },
    jmodal: function(setting) {
        var ps = $.fn.extend({
            data: {},
            marginTop: 100,
            buttonText: { button1: '', button2: '', button3: '' },
            button1Event: function(e) { },
            button2Event: function(e) { },
            button3Event: function(e) { },
            initWidth: 400,
            fixed: true,
	    imageType: 'Information',
            title: 'JModal Dialog',
            content: 'This is a jquery plugin!',
            skinId: 'jmodal-main'
        }, setting);

        var allSel = $('select').hide();

        ps.docWidth = $(document).width();
        ps.docHeight = $(document).height();

        var jmodal = $('#jquery-jmodal');
        var overlay = $('#jmodal-overlay');
        if (jmodal.length == 0) {
		var buttonHTML = "";
		var informationHTML = "";
		if(ps.buttonText.button1 !== undefined && ps.buttonText.button1 != '')	{buttonHTML += '<input id="perc_msg_dialogue_button1" type="button" value="' + ps.buttonText.button1 + '" />';} else {buttonHTML += '<input id="perc_msg_dialogue_button1" type="button" style="display:none; visibility:hidden;" value="" />'; ps.buttonText.button1 ="";}
		if(ps.buttonText.button2 !== undefined && ps.buttonText.button2 != '')	{buttonHTML += '<input id="perc_msg_dialogue_button2" type="button" value="' + ps.buttonText.button2 + '" />';} else {buttonHTML += '<input id="perc_msg_dialogue_button2" type="button" style="display:none; visibility:hidden;" value="" />'; ps.buttonText.button2 ="";}
		if(ps.buttonText.button3 !== undefined && ps.buttonText.button3 != '')	{buttonHTML += '<input id="perc_msg_dialogue_button3" type="button" value="' + ps.buttonText.button3 + '" />';} else {buttonHTML += '<input id="perc_msg_dialogue_button3" type="button" style="display:none; visibility:hidden;" value="" />'; ps.buttonText.button3 ="";}
		if(ps.imageType === "Warning")	{
			informationHTML	= "<img src='images/warning.gif' title='Warning'/>" + ps.title; 
		}
		else if(ps.imageType === "Error")	{	
			informationHTML	= "<img src='images/error.gif' title='Error'/>" + ps.title; 
		}
		else	{
			informationHTML	= "<img src='images/information.gif' title='Information'/>" + ps.title; 			
		}
            $('<div id="jmodal-overlay" class="jmodal-overlay"/>' +
		  '<div class="jmodal-main" id="jquery-jmodal" >' +
                    '<div class="jmodal-top">' +
                        '<div class="jmodal-top-left jmodal-png-fiexed">&nbsp;</div>' +
                        '<div class="jmodal-border-top jmodal-png-fiexed">&nbsp;</div>' +
                        '<div class="jmodal-top-right jmodal-png-fiexed">&nbsp;</div>' +
                    '</div>' +
                    '<div class="jmodal-middle">' +
                        '<div class="jmodal-border-left jmodal-png-fiexed">&nbsp;</div>' +
                        '<div class="jmodal-middle-content">' +
                            '<div class="jmodal-title" />' +
                            '<div class="jmodal-content" id="jmodal-container-content" />' +
                            '<div style="text-align:right;">' +
					buttonHTML +
                            '</div>' +
                        '</div>' +
                        '<div class="jmodal-border-right jmodal-png-fiexed">&nbsp;</div>' +
                    '</div>' +
                    '<div class="jmodal-bottom">' +
                        '<div class="jmodal-bottom-left jmodal-png-fiexed">&nbsp;</div>' +
                        '<div class="jmodal-border-bottom jmodal-png-fiexed">&nbsp;</div>' +
                        '<div class="jmodal-bottom-right jmodal-png-fiexed">&nbsp;</div>' +
                    '</div>' +
                '</div>').appendTo($(document.body));
            jmodal = $('#jquery-jmodal');
            overlay = $('#jmodal-overlay');

            //jmodal.append('<div style="position:absolute;top:100px;left:300px;background-color:#ff0000;width:400px;height:300px;z-index:8800">dd</div>');
            //$(document.body).find('form:first-child') || $(document.body)
        }
        else {
            overlay.css({ opacity: 0, 'display': 'block' });
            jmodal.css({ opacity: 0, 'display': 'block' });
	}
        jmodal.attr('class', ps.skinId);
        overlay.css({
            height: ps.docHeight,
            opacity: 0
        }).animate({ opacity: 0.5 });

        jmodal.css({
            position: (ps.fixed ? 'fixed' : 'absolute'),
            width: ps.initWidth,
            left: (ps.docWidth - ps.initWidth) / 2,
            top: (ps.marginTop + document.documentElement.scrollTop)
        }).animate({ opacity: 1 }, function() {
            $(this).css('opacity', '');
        });

        jmodal
            .find('.jmodal-title')
                .html(ps.title)
                    .next()
                        .next()
                            .children('input:first-child')
                                .attr('value', ps.buttonText.button1)
                                    .unbind('click')
                                        .one('click', function(e) {
                                            var args = {
                                                complete: $.fn.hideJmodal
                                            };
                                            allSel.show();
                                            ps.button1Event(ps.data, args);
                                        })
	                                    .next()
                                                .attr('value', ps.buttonText.button2)
		                                    .unbind('click')
                                                    //.one('click', $.fn.hideJmodal);
							.one('click', function(e) {
                                          		  var args = {
	                        	                        complete: $.fn.hideJmodal
        			                          };
	                                            ps.button2Event(ps.data, args);
	                                        })
					    .next()
                                                .attr('value', ps.buttonText.button3)
		                                    .unbind('click')
                                                    //.one('click', $.fn.hideJmodal);
							.one('click', function(e) {
                                          		  var args = {
	                        	                        complete: $.fn.hideJmodal
        			                          };
		                                            ps.button3Event(ps.data, args);
		                                        });
						    
	$('#jmodal-container-content').select();

	$('.jmodal-title').html(informationHTML);
        if (typeof ps.content == 'string') {
            $('#jmodal-container-content').html(ps.content);
        }
        if (typeof ps.content == 'function') {
            var e = $('#jmodal-container-content');
            e.holder = jmodal;
            ps.content(e);
        }
    }
});
})(jQuery);

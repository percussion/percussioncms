window.resizer = {};
(function (rb) {
    //Variables
    var d = document,
        w = window,
        url = d.URL,
        title = d.title,
        wrapper = null,
        devices = null,
        close = null,
        body = null,
        size = null,
        auto = true,
        isResized = false,
        isAnimated = false,
        //The buttons for viewport sizes are defined in the html. The dimensions are defined here
        sizes = {
            smartphonePortrait: [320, 480],
            smartphoneLandscape: [480, 320],
            smallTabletPortrait: [600, 800],
            smallTabletLandscape: [800, 600],
            largeTabletPortrait: [768, 1024],
            largeTabletLandscape: [1024, 768],
            auto: 'auto'
        },
        //Updates viewport information
        resize = function (w, h, f) {
            w = w || wrapper.clientWidth;
            h = h || wrapper.clientHeight;
            size.innerHTML = 'W:' + '<span id="perc-width">' + w +'</span>' + '<br/>' + 'x' + '<br/>' + 'H:' + '<span id="perc-height">'+ h + '</span>'
        },
        //Alters viewport size
        setPosition = function (wh, t, cl) {
            //If viewport selection is auto, adjust to maximum screen dimenions : otherwise use dimensions specified in array
            var width = (wh == 'auto') ? w.innerWidth : wh[0],
                height = (wh == 'auto') ? w.innerHeight : wh[1],
                style = 'width:' + width + 'px;height:' + height + 'px;margin-top:20px;';
            if (typeof (width) == 'undefined' || typeof (height) == 'undefined') return false;
            style += (wh === 'auto') ? 'margin-top:0;' : '';
            wrapper.setAttribute('style', style);
            wrapper.setAttribute('data-device', cl);
            body.setAttribute('style', 'min-height:' + height + 'px;min-width:' + width + 'px;');
            resize(width, height);
            if (wh === 'auto' && !t) {
                isResized = false;
                setTimeout(function () {
                    wrapper.setAttribute('style', '');
                    body.setAttribute('style', '');
                    isAnimated = false
                }, 260)
            } else {
                isAnimated = false
            }
                wrapper.setAttribute('loaded', true);
            //Updates the href of all CM1 links on page (doesn't find links from iframes and javascript based click events) based on the selected preview mode.
            if(cl){
                var alllinks = window.frames['percmobilepreviewframe'].document.links;
                for(var i = 0; i < alllinks.length; i++) {
                    var hrefattr = alllinks[i].href;            
                    var actualhref = alllinks[i].attributes['href'].value;
                    var isAnchor = actualhref.indexOf("#")==0;
                    if(hrefattr.indexOf("/Sites") != -1 && !isAnchor){
                        var h = PSHref2Hash(hrefattr);
                        h["percpreviewmode"]=cl;
                        // replace() removes absolute ref from start of url
                        alllinks[i].href = PSHash2Href(h,hrefattr).replace(/^.*\/\/[^\/]+/, '');
                    }
                }
            }

        }, readyElement = function (id, callback) {
            var interval = setInterval(function () {
                if (d.getElementById(id)) {
                    callback(d.getElementById(id));
                    clearInterval(interval)
                }
            }, 60)
        };

    //Wraps the page in a container
    readyElement('percwrapper', function () {
        wrapper = d.getElementById('percwrapper');
        devices = d.getElementById('percdevices');
        size = d.getElementById('percsize');
        close = d.querySelector('.percclose');
        body = d.querySelector('body');
        wrapper.setAttribute('loaded', true);
        if (window.chrome || (window.getComputedStyle && !window.globalStorage && !window.opera)) {}
        [].forEach.call(document.querySelectorAll('#percdevices a'), function (el) {
            el.addEventListener('click', function (e) {
                [].forEach.call(document.querySelectorAll('#percdevices a'), function (el) {
                    el.className = el.className.replace(' active', '')
                        wrapper.setAttribute('loaded', false);
                });
                e.preventDefault();
                e.stopPropagation();
                var self = this;
                if ((self.className.match('perc-auto') && isResized === false) || isAnimated === true) return false;
                isAnimated = true;
                if (isResized === false) {
                    isResized = true;
                    setPosition(sizes.auto, true)
                }
                setTimeout(function () {
                    self.className = self.className + " " + 'active';
                    if (self.className.match('perc-smartphone-portrait')) {
                        setPosition(sizes.smartphonePortrait, false, 'smartphonePortrait')
                    } else if (self.className.match('perc-smartphone-landscape')) {
                        setPosition(sizes.smartphoneLandscape, false, 'smartphoneLandscape')
                    } else if (self.className.match('perc-small-tablet-portrait')) {
                        setPosition(sizes.smallTabletPortrait, false, 'smallTabletPortrait')
                    } else if (self.className.match('perc-small-tablet-landscape')) {
                        setPosition(sizes.smallTabletLandscape, false, 'smallTabletLandscape')
                    } else if (self.className.match('perc-large-tablet-portrait')) {
                        setPosition(sizes.largeTabletPortrait, false, 'largeTabletPortrait')
                    } else if (self.className.match('perc-large-tablet-landscape')) {
                        setPosition(sizes.largeTabletLandscape, false, 'largeTabletLandscape')
                    } else if (self.className.match('perc-auto')) {
                        setPosition(sizes.auto, false, 'auto')
                    }
                }, 10)
            })
        });
		
        //Code to control the expansion/collapse of the toolbar
        close.addEventListener('click', function (e) {
            if(size.style.display == 'none' || '' && devices.style.display == 'none' || ''){
            e.preventDefault();
            e.stopPropagation();
            size.style.display= 'block';
            devices.style.display= 'block';
            size.parentNode.style.top = '146px';
            size.parentNode.style.width = '351px';
            size.parentNode.style.right= '-136px';
            d.getElementById("percexpand").className = "icon-double-angle-down icon-2x";
            d.getElementById("percexpand").title = "Click to collapse mobile preview toolbar";
            d.getElementById("percexpand").id = "perccollapse";
            }
            else{
            e.preventDefault();
            e.stopPropagation();
            size.style.display= 'none';
            devices.style.display= 'none';
            size.parentNode.style.top = '-14px';
            size.parentNode.style.width = '19px';
            size.parentNode.style.right= '30px';
            d.getElementById("perccollapse").className = "icon-double-angle-up icon-2x";
            d.getElementById("perccollapse").title = "Click to expand mobile preview toolbar";
            d.getElementById("perccollapse").id = "percexpand";
            }            
        }, false);
        w.addEventListener('resize', function () {
            resize()
        }, false);
        
        resize();
        //if percpreviewmode exists call the setPosition to change it to that mode on window open.
        var prurl = window.location.href;
        var prevmode = PSGetParam(prurl,'percpreviewmode');
        if(prevmode){
            setPosition(sizes[prevmode], false, prevmode)
        }

        size.style.minWidth = 0
    })
})(resizer);
//Function to handle mobile preview onload functionality.
//Sets the body overflowX to hidden to avoid horizontal scroll bars.
window.mobilePreviewFrameOnload = function(){
    try{
        window.frames['percmobilepreviewframe'].document.body.style.overflowX='hidden';
        //Get percpreviewmode parameter from url
        var prurl = window.location.href;
        var prevmode = PSGetParam(prurl,'percpreviewmode');
        //process all CM1 links, (doesn't find links from iframes and javascript click events)
        //updates the href attribute, adds the mobile preview parameter and adds preview mode parameter if it exists in the window url,
        var alllinks = window.frames['percmobilepreviewframe'].document.links;
        for(var i = 0; i < alllinks.length; i++) {
            if(alllinks[i].target == '' || (alllinks[i].target).toLowerCase() == '_self')
                alllinks[i].target = '_top';
            var hrefattr = alllinks[i].href; 
            var actualhref = alllinks[i].attributes['href'].value;
            var isAnchor = actualhref.indexOf("#")==0;
            if(hrefattr.indexOf("/Sites") != -1 && !isAnchor){
                var h = PSHref2Hash(hrefattr);
                h["percmobilepreview"]='true';
                if(prevmode)
                    h["percpreviewmode"]=prevmode;
                // replace() removes absolute ref from start of url
                alllinks[i].href = PSHash2Href(h,hrefattr).replace(/^.*\/\/[^\/]+/, '');
            }
        }
    }
    catch(err){
        //this shuld not happen just ignore it.
    }
}

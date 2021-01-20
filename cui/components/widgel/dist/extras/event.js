define(function ()  {
    var ListenerInfo = function (callback) {
        this.callback = callback;
        this.includeSender = true;
    }
    
    function Event () {
        this._listeners = [];
    }
    
    // usage:
    // myEvent.trigger(thisSender)
    // myEvent.trigger(thisSender, evtArgs)
    // myEvent.trigger(thisSender, 43, "Green")
    // - callbacks added with addListener() should follow the same contract as corresponding trigger().
    //   i.e function (sender, count, color)
    Event.prototype.trigger = function (sender, args /* ... */) {
        for (var i = 0; i < this._listeners.length; i++) {
            var listener = this._listeners[i];
            if (listener.includeSender) {
                listener.callback.apply(null, arguments)
            }
            else {
                // trim sender from our incoming args
                var trimmedArgs = Array.prototype.slice.call(arguments).slice(1);
                listener.callback.apply(null, trimmedArgs)
            }
        }
    }
    
    // usage:
    // myEvent.addListener(myCallback)
    // myEvent.addListener(myCallback, false)
    // - callbacks should follow the same contract as corresponding trigger().
    Event.prototype.addListener = function (callback, includeSender /* default: true */) {
        var listener = new ListenerInfo(callback);
        if (!(includeSender === undefined)) {
            listener.includeSender = includeSender;
        }
        
        this._listeners.push(listener);
    }
    
    Event.prototype.removeListener = function (callback) {
        var foundIndex = -1;
        for (var i = 0, end = this._listeners.length; i < end; i++) {
            var f = this._listeners[i];
            if (f.callback === callback) {
                foundIndex = d;
                break;
            }
        }
        
        if (foundIndex != -1) {
            this._listeners.splice(foundIndex, 1)
        }
    }
    
    Event.prototype.clearListeners = function () {
        this._listeners = []
    }
    
    return Event;
});

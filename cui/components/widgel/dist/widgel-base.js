define(['knockout', 'jquery', 'jquery-ui','jquery-ui/widget'], function (ko, $) {
    $.widget("widGEL.baseWidget", {
        //#region Public Widget Members (API)
        options: {
            modelData : null,  // optional - set at creation time to do object-literal based deep/loose initialization of ViewModel.
            view : null
        },

        destroy: function () { },

        // model accessor usage:
        // get value: 
        //            $('selector').widgetName("model", "observableFieldX")()
        // set value: 
        //            $('selector').widgetName("model", "observableFieldX")(5)
        //         or $('selector').widgetName("model", "observableFieldX", 5)
        // call function: 
        //            $('selector').widgetName("model", "functionX")(p1, p2, ...)
        //         or $('selector').widgetName("model", "functionX", p1, p2, ...)
        // get observable: 
        //            $('selector').widgetName("model", "observableFieldX").subscribe(...)
        // get event: 
        //            $('selector').widgetName("model", "eventX").addListener(...)
        model: function (field /* [, ...] */) {
            if (typeof (field) != "string")
                throw ("Invalid ViewModel field name.  Must pass a string.");
            if (field.length == 0)
                throw ("Invalid ViewModel field name.  Empty string is not allowed.");
            if (field.charAt(0) == "_")
                throw ("Invalid ViewModel field name.  ViewModel members that start with an _underscore are for internal widget use only.");

            var myModel = this._viewModel;
            if (!(myModel.hasOwnProperty(field))) {
                throw ("Invalid ViewModel field name.  " + field + " does not exist.");
            }

            // see if we have more args (set/call)
            if (arguments.length > 1) {
                // optimize for single arg (set)
                if (arguments.length == 2) {
                    return myModel[field].call(myModel, arguments[1]);
                }
                //else
                // pass along all but our first argument (field)
                var args = [];
                for (var i = 1, end = arguments.length; i < end; i++) {
                    args.push(arguments[i]);
                }
                return myModel[field].apply(myModel, args);
            }
            else {
                // this is a get
                return myModel[field];
            }
        },

        _createDefaultViewModel: function () {
            // sub-class widgets should override this method to return a view model object with ko observable properties, etc.
            // The returned model can have Public and _Private members.  
            // Public members are get/set and considered part of the widget API.
        },
        //#endregion

        //#region Private Widget Members
        _create: function () {
            // sub-class widgets should override to call _initModelView related functionality and any other needed widget bootstrapping.
        },

        _viewModel: null,

        _initModelView: function () {
            var viewModel = this._viewModel = this._createDefaultViewModel();
            var modelData = this.options.modelData;

            if (modelData != null) {
                this._applyDataToViewModel(modelData);
            }

            $(this.element).html($(this.options.view));

            ko.applyBindingsToDescendants(viewModel, this.element[0]);
        },

        // requiredOptionNames: [string]
        _checkRequiredOptions: function (widgetName, options, requiredOptionNames) {
            for (var i = 0, end = requiredOptionNames.length; i < end; i++) {
                var required = requiredOptionNames[i];
                if ((options[required] == null) || (options[required] == undefined)) {
                    throw widgetName +  " widget could not be created.  " + required + " must be provided at creation.";
                }
            }
        },

        // - called from _initModelView at widget creation.  Fully recursive, allowing structured data init.
        // - observableArrays must implement PushData(arrayItemObj) if they want to be initializable, 
        //   or attempts to init will throw.
        // - plain arrays can optionally implement PushData or rely on standard initialization, which includes 
        //   proper handling of the uncommon case of array items containing observable properties. 
        _applyDataToViewModel: function (data, vModel, allowPrivateProperties /* optional */) {
            if (vModel == undefined) {
                vModel = this._viewModel;
            }

            for (var field in data) {
                if (field.charAt(0) == "_")
                    throw ("Invalid ViewModel field name.  ViewModel members that start with an _underscore are for internal widget use only.");

                if (!(vModel.hasOwnProperty(field)))
                    throw ("Invalid ViewModel field name.  " + field + " does not exist.");

                if ((vModel[field] != null) && (ko.isSubscribable(vModel[field]))) {
                    if (Array.isArray(vModel[field]._latestValue)) {
                        if (!(vModel[field].hasOwnProperty("PushData"))) {
                            throw ("Could not set array data. " + field + "property is observableArray but does not implement PushData() method.");
                        }

                        vModel[field].removeAll();
                        for (var i = 0; i < data[field].length; i++) {
                            vModel[field].PushData(data[field][i]);
                        }
                    }
                    else  {
                        // single-value observable
                        vModel[field](data[field]);
                    }
                }
                else if (Array.isArray(data[field])) {
                    // try pushData method (optional on regular arrays)
                    if (vModel[field].hasOwnProperty("PushData")) {
                        // empty out previous array with unshift so that we preserve any dynamically added properties like PushData
                        var length = vModel[field].length;
                        for (var i = 0; i < length; i++) {
                            vModel[field].pop();
                        }

                        // now push our new values
                        for (var i = 0; i < data[field].length; i++) {
                            vModel[field].PushData(data[field][i]);
                        }
                    }
                    else {
                        // clear and push new values
                        vModel[field] = [];
                        for (var i = 0; i < data[field].length; i++) {
                            //vModel[field].push(data[field][i]);
                            this._applyDataToPlainProperty(data[field], vModel[field], i);
                        }
                    }
                }
                else  {
                    this._applyDataToPlainProperty(data, vModel, field);
                }
            }
        },

        _applyDataToPlainProperty: function (data, vModel, field) {
            if ($.isPlainObject(data[field])) {
                this._applyDataToViewModel(data[field], vModel[field]);
            }
            else {
                vModel[field] = data[field];
            }
        }
        //#endregion
    });

    // public static members
    $.extend($.widGEL.baseWidget, {
        injectCssFile: function (id, url) {
            var hitSelector = 'html head link[css-widget-id="' + id + '"]';
            var hits = $(hitSelector).length;
            if (hits == 0)
            {
                $('html head').append('<link href="' + url + '" css-widget-id="' + id + '" rel="stylesheet" />');
            }
        },

        // dictionary of defined templates
        definedTemplates: { },

        registerDefinedTemplate: function (path, templateString) {
            $.widGEL.baseWidget.definedTemplates[path] = templateString;
        }
    });

    // register requireJS-friendly Knockout template provider
    stringTemplateSource = function (template) {
        this.template = template;
    };

    stringTemplateSource.prototype.text = function () {
        return $.widGEL.baseWidget.definedTemplates[this.template];
    };

    var stringTemplateEngine = new ko.nativeTemplateEngine();
    stringTemplateEngine.originalEngine = ko.templateEngine;
    stringTemplateEngine.makeTemplateSource = function (template) {
        if ((template.length >= 8) && (template.substr(0, 8) == 'defined:')) {
            var path = template.substr(8);
            return new stringTemplateSource(path);
        }
        else {
            return stringTemplateEngine.originalEngine.prototype.makeTemplateSource(template);
        }
    };

    ko.setTemplateEngine(stringTemplateEngine);

    // custom Knockout bindings
    ko.bindingHandlers.ignoreBindings = {
        init: function (elem, valueAccessor) {
            // Let bindings proceed as normal *only if* my value is false
            var shouldIgnoreBindings = ko.utils.unwrapObservable(valueAccessor());
            return { controlsDescendantBindings: shouldIgnoreBindings };
        }
    };

    // extends observable model/object 
    ko.utils.extendObservable = function (target, source) {
        var prop,
            srcVal,
            tgtProp,
            srcProp,
            isObservable = false;

        for (prop in source) {
            if (!source.hasOwnProperty(prop)) {
                continue;
            }

            if (ko.isWriteableObservable(source[prop])) {
                isObservable = true;
                srcVal = source[prop]();
            }
            else if (typeof (source[prop]) !== 'function') {
                srcVal = source[prop];
            }

            if (ko.isWriteableObservable(target[prop])) {
                target[prop](srcVal);
            }
            else if (target[prop] === null || target[prop] === undefined) {
                target[prop] = isObservable ? ko.observable(srcVal) : srcVal;
            }
            else if (typeof (target[prop]) !== 'function') {
                target[prop] = srcVal;
            }

            isObservable = false;
        }
    };

    // the clone function
    ko.utils.clone = function (obj, emptyObj) {
        var json = ko.toJSON(obj);
        var js = JSON.parse(json);

        return ko.utils.extendObservable(emptyObj, js);
    };

    // custom Knockout functions

    // define recursive equality checker
    var primitiveTypes = {
        boolean: true,
        number: true,
        string: true,
        undefined: true
    };

    var recursiveEqualityComparer = function (oldV, newV) {
        var thisRecursive = this;
        var oldIsPrimitive = (oldV === null) || (typeof (oldV) in primitiveTypes);
        var newIsPrimitive = (newV === null) || (typeof (newV) in primitiveTypes);

        if (oldIsPrimitive && newIsPrimitive) {
            return (oldV === newV);
        }

        // both not primitive?
        if ((!oldIsPrimitive) && (!newIsPrimitive)) {
            if (Array.isArray(oldV) && Array.isArray(newV)) {
                if (newV.length != oldV.length) {
                    return false;
                }

                for (var i = 0, end = newV.length; i < end; i++) {
                    if (!recursiveEqualityComparer(oldV[i], newV[i])) {
                        return false;
                    }
                }

                return true;
            }

            // else its an object
            for (var field in oldV) {
                if (!newV.hasOwnProperty(field))
                    return false;

                if (((newV != null && newV[field] != null) &&
                    (oldV != null && oldV[field] != null)) &&
                    (ko.isSubscribable(oldV[field])) &&
                    (ko.isSubscribable(newV[field]))) {

                    if (!recursiveEqualityComparer(oldV[field](), newV[field]()))
                        return false;
                }
                else {
                    if (!recursiveEqualityComparer(oldV[field], newV[field]))
                        return false;
                }
            }

            // all oldV[field]s have matching value in newV
            for (var newField in newV) {
                if (!oldV.hasOwnProperty(newField))
                    return false;
            }
            // and no new fields in newV
            return true;
        }
        // if all else fails...
        return false;
    };

    // twoWayBind 
    //   - binds two observables to eachother so that changes in either one are reflected to the other
    //   - Works for observables that contain primitive values, an array value, or complex object.
    //   - nested object properties or array items can contain observables (full recursive support)
    // KNOWN ISSUES: observableArray().twoWayBind() not yet supported.
    ko.subscribable.fn.twoWayBind = function (otherObservable) {
        var thisObservable = this;
        // define recursive equality checker
        var primitiveTypes = {
            boolean: true,
            number: true,
            string: true,
            undefined: true
        };

        // set equalityComparer to our recursive version.  This is necessary to avoid an infinite loop, 
        // since the default knockout comparer considers any non-primitive value as always changed.
        thisObservable.equalityComparer = recursiveEqualityComparer;
        thisObservable.subscribe(function (newValue) {
            otherObservable(newValue);
        });

        otherObservable.equalityComparer = recursiveEqualityComparer;
        otherObservable.subscribe(function (newValue)
        {
            thisObservable(newValue);
        });

        // sync values
        otherObservable(thisObservable());
    };

    // oneWayBind 
    //   - binds this observable to another one so that changes in this one are reflected to the other
    //   - Works for observables that contain primitive values, an array value, or complex object.
    //   - nested object properties or array items can contain observables (full recursive support)
    // KNOWN ISSUES: observableArray().oneWayBind() not yet supported.
    ko.subscribable.fn.oneWayBind = function (otherObservable) {
        var thisObservable = this;

        // set equalityComparer to our recursive version.  This is necessary to avoid an infinite loop, 
        // since the default knockout comparer considers any non-primitive value as always changed.
        thisObservable.equalityComparer = recursiveEqualityComparer;
        thisObservable.subscribe(function (newValue)
        {
            otherObservable(newValue);
        });

        // sync values
        otherObservable(thisObservable());
    };

    // custom Knockout observable extenders

    // limitValue - limits the value of an observable through a provided limitFunction
    // args:  
    //   - limitFunction(oldValue, newValue) returns validated value
    ko.extenders.limitValue = function (target, limitFunction) {
        //create a writeable computed observable to intercept writes to our observable
        var extender = ko.computed({
            read: target,  //always return the original observables value
            write: function (newValue) {
                var current = target();

                // validate if changed
                if (current !== newValue) {
                    var validatedValue = limitFunction(current, newValue);

                    //only write if it changed
                    if (validatedValue !== current) {
                        target(validatedValue);
                    }
                }
            }
        });

        //initialize with current value to make sure it is validated appropriately
        extender(target());

        //return the new computed observable
        return extender;
    };

    // LimitMinMax - limits the value of an observable to be between a min and a max
    // args:  
    //   - {Min: number or ko.observable(number), Max: number or ko.observable(number)}
    ko.extenders.limitMinMax = function (target, args) {
        //create a writeable computed observable to intercept writes to our observable
        var extender = ko.computed( {
            read: target,  //always return the original observables value
            write: function (newValue) {
                var current = target();

                // validate if changed
                if (current !== newValue) {
                    var limitedValue = newValue;
                    var min = ko.utils.unwrapObservable(args.Min);
                    var max = ko.utils.unwrapObservable(args.Max);
                    if (newValue < min) {
                        limitedValue = min;
                    }
                    else if (newValue > max) {
                        limitedValue = max;
                    }

                    //only write if it changed
                    if (limitedValue !== current) {
                        target(limitedValue);
                    }
                }
            }
        });

        //initialize with current value to make sure it is validated appropriately
        extender(target());

        //return the new computed observable
        return extender;
    };

    ko.extenders.animatedValue = function (target, extenderArgs) {
        var lastTimeoutID = null;
        target.animateTo = function (newValue, duration) {
            if (lastTimeoutID != null) {
                clearTimeout(lastTimeoutID);
            }

            var onAnimate = function (finalValue, timeToEnd) {
                // animation time expired?
                if (timeToEnd <= 0) {
                    target(finalValue);
                    return;
                }

                // else: animate value to next step value
                var currentValue = target();
                var deltaToEndValue = finalValue - currentValue;
                var timeRatio = 50 / timeToEnd;
                var deltaStep = deltaToEndValue * timeRatio;
                target(currentValue + deltaStep);
                lastTimeoutID = setTimeout(function () { onAnimate(finalValue, timeToEnd - 50); }, 50);
            };

            // 20 frames per second
            lastTimeoutID = setTimeout(function () { onAnimate(newValue, duration); }, 50);
        };
        return target;
    };

    // onValueSetting - allows before and after callbacks for when a value is being set.
    // args:  
    // {
    //    beforeSet: onValueSettingCallback(oldValue, newValue),
    //    afterSet: onValueSetCallback(oldValue, newValue)
    // }
    ko.extenders.onValueSetting = function (target, callbacks) {
        //create a writeable computed observable to intercept writes to our observable
        var result = ko.computed({
            read: target,  //always return the original observables value
            write: function (newValue) {
                var current = target();
                if (callbacks.beforeSet) {
                    callbacks.beforeSet(current, newValue);
                }

                target(newValue);

                if (callbacks.afterSet) {
                    callbacks.afterSet(current, newValue);
                }
            }
        });

        //initialize with current value to have callback get hit
        result(target());

        //return the new computed observable
        return result;
    };

    // lockDataUpdates - will set a lock flag before updating, then unlock.  
    //                   Typically used to drive an UpdateBlocked flag to throttle date calls 
    //                   until after multiple param changes
    // args:  
    //  - lockingObservable: the ko.observable(bool) that tracks if updates are blocked
    ko.extenders.lockDataUpdates = function (target, lockingObservable) {
        //create a writeable computed observable to intercept writes to our observable
        var result = ko.computed({
            read: target,  //always return the original observables value
            write: function (newValue) {
                var outerLock = lockingObservable();
                lockingObservable(true);
                target(newValue);
                lockingObservable(outerLock); // set lock to what it was before
            }
        });

        //return the new computed observable
        return result;
    };

    // This is alternate way to subscribe that will include the old value as well
    // as the new value.
    // Use like this: myViewModel.myObservable.subscribeChanged(function (oldValue, newValue) { });
    ko.subscribable.fn.subscribeChanged = function (callback) {
        var _oldValue;
        this.subscribe(function (oldValue) {
            _oldValue = oldValue;
        }, this, 'beforeChange');

        this.subscribe(function (newValue) {
            callback(_oldValue, newValue);
        });
    };

    // convenience function that sets up a .subscribe and immediately calls the
    // callback with the observable's current value
    ko.subscribable.fn.subscribeAndCall = function (callback) {
        this.subscribe(callback);
        callback(this());
    };

    return "SUCCESS: widGEL.baseWidget Registered.";
});

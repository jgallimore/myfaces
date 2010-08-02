/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 * A collection of dom helper routines
 * (which in later incarnations will
 * get browser specific speed optimizations)
 *
 * Since we have to be as tight as possible
 * we will focus with our dom routines to only
 * the parts which our impl uses.
 * A jquery like query API would be nice
 * but this would blow up our codebase significantly
 *
 * TODO selector shortcuts bei chrome abdrehen da knallt es
 *
 */
/** @namespace myfaces._impl._util._Dom */

/** @namespace NodeFilter */
/** @namespace NodeFilter.FILTER_ACCEPT */
/** @namespace NodeFilter.FILTER_SKIP */
/** @namespace NodeFilter.FILTER_REJECT */
/** @namespace NodeFilter.SHOW_ELEMENT */
myfaces._impl.core._Runtime.singletonExtendClass("myfaces._impl._util._Dom", Object, {
    IE_QUIRKS_EVENTS : {
        "onabort": true,
        "onload":true,
        "onunload":true,
        "onchange": true,
        "onsubmit": true,
        "onreset": true,
        "onselect": true,
        "onblur": true,
        "onfocus": true,
        "onkeydown": true,
        "onkeypress": true,
        "onkeyup": true,
        "onclick": true,
        "ondblclick": true,
        "onmousedown": true,
        "onmousemove": true,
        "onmouseout": true,
        "onmouseover": true,
        "onmouseup": true
    },

    _Lang:myfaces._impl._util._Lang,

    constructor_: function() {
        //we have to trigger it upfront because mozilla runs the eval
        //after the dom updates and hence causes a race conditon if used on demand
        //under normal circumstances this works, if there are no normal ones
        //then this also will work at the second time, but the onload handler
        //should cover 99% of all use cases to avoid a loading race condition
        myfaces._impl.core._Runtime.addOnLoad(window, function() {
            myfaces._impl._util._Dom.isManualScriptEval();
        });
        //safety fallback if the window onload handler is overwritten and not chained
        if (document.body) {
            myfaces._impl.core._Runtime.addOnLoad(document.body, function() {
                myfaces._impl._util._Dom.isManualScriptEval();
            });
        }
        //now of the onload handler also is overwritten we have a problem
    },

    /**
     * Run through the given Html item and execute the inline scripts
     * (IE doesn't do this by itself)
     * @param {|Node|} item
     */
    runScripts: function(item, xmlData) {
        var finalScripts = [];
        var execScrpt = this._Lang.hitch(this, function(item) {
            if (item.tagName && this._Lang.equalsIgnoreCase(item.tagName, "script")) {
                var src = item.getAttribute('src');
                if ('undefined' != typeof src
                        && null != src
                        && src.length > 0
                        ) {
                    //we have to move this into an inner if because chrome otherwise chokes
                    //due to changing the and order instead of relying on left to right
                    if ((src.indexOf("ln=scripts") == -1 && src.indexOf("ln=javax.faces") == -1) || (src.indexOf("/jsf.js") == -1
                            && src.indexOf("/jsf-uncompressed.js") == -1))
                        if (finalScripts.length) {
                            //script source means we have to eval the existing
                            //scripts before running the include
                            myfaces._impl.core._Runtime.globalEval(finalScripts.join("\n"));
                            finalScripts = [];
                        }
                    myfaces._impl.core._Runtime.loadScriptEval(src, item.getAttribute('type'), false, "UTF-8");
                } else {
                    // embedded script auto eval
                    var test = (!xmlData) ? item.text : this._Lang.serializeChilds(item);
                    var go = true;
                    while (go) {
                        go = false;
                        if (test.substring(0, 1) == " ") {
                            test = test.substring(1);
                            go = true;
                        }
                        if (test.substring(0, 4) == "<!--") {
                            test = test.substring(4);
                            go = true;
                        }
                        if (test.substring(0, 11) == "//<![CDATA[") {
                            test = test.substring(11);
                            go = true;
                        }
                    }
                    // we have to run the script under a global context
                    //we store the script for less calls to eval
                    finalScripts.push(test);

                }
            }
        });
        try {
            var scriptElements = this.findByTagName(item, "script", true);
            if (scriptElements == null) return;
            for (var cnt = 0; cnt < scriptElements.length; cnt++) {
                execScrpt(scriptElements[cnt]);
            }
            if (finalScripts.length) {
                myfaces._impl.core._Runtime.globalEval(finalScripts.join("\n"));
            }
        } finally {
            //the usual ie6 fix code
            //the IE6 garbage collector is broken
            //nulling closures helps somewhat to reduce
            //mem leaks, which are impossible to avoid
            //at this browser
            execScrpt = null;
        }
    },

    /**
     * Simple delete on an existing item
     */
    deleteItem: function(itemIdToReplace) {
        var item = this.byId(itemIdToReplace);
        if (!item) {
            throw Error("_Dom.deleteItem  Unknown Html-Component-ID: " + itemIdToReplace);
        }

        item.parentNode.removeChild(item);
    },

    /**
     * outerHTML replacement which works cross browserlike
     * but still is speed optimized
     *
     * @param item the item to be replaced
     * @param markup the markup for the replacement
     */
    outerHTML : function(item, markup) {
        if (!item) {
            throw Error("myfaces._impl._util._Dom.outerHTML: item must be passed down");
        }
        if (!markup) {
            throw Error("myfaces._impl._util._Dom.outerHTML: markup must be passed down");
        }

        markup = this._Lang.trim(markup);
        if (markup !== "") {
            var evalNodes = null;

            //w3c compliant browsers with proper contextual fragments
            var parentNode;
            if (window.Range
                    && typeof Range.prototype.createContextualFragment == 'function') {
                evalNodes = this._outerHTMLCompliant(item, markup);
            } else {
                evalNodes = this._outerHTMLNonCompliant(item, markup);
            }

            // and remove the old item
            //first we have to save the node newly insert for easier access in our eval part
            if (this.isManualScriptEval()) {
                var isArr = evalNodes instanceof Array;
                if (isArr && evalNodes.length) {
                    for (var cnt = 0; cnt < evalNodes.length; cnt++) {
                        this.runScripts(evalNodes[cnt]);
                    }
                } else if (!isArr) {
                    this.runScripts(evalNodes);
                }
            }
            return evalNodes;
        }
        // and remove the old item, in case of an empty newtag and do nothing else
        item.parentNode.removeChild(item);
        return null;
    },

    _outerHTMLCompliant: function(item, markup) {

        var b = myfaces._impl.core._Runtime.browser;

        var evalNodes = null;
        var dummyPlaceHolder = document.createElement("div");
        dummyPlaceHolder.innerHTML = markup;
        evalNodes = dummyPlaceHolder.childNodes;
        if ('undefined' == typeof evalNodes.length) {
            item.parentNode.replaceChild(evalNodes, item);
            return evalNodes;
            //return this.replaceElement(item, evalNodes);
        } else if (evalNodes.length == 1) {
            var ret = evalNodes[0];
            item.parentNode.replaceChild(evalNodes[0], item);
            return ret;
            //return this.replaceElement(item, evalNodes[0]);
        } else {
            return this.replaceElements(item, evalNodes)
        }

    },

    _outerHTMLNonCompliant: function(item, markup) {

        var evalNodes = null;
        //now to the non w3c compliant browsers
        //http://blogs.perl.org/users/clinton_gormley/2010/02/forcing-ie-to-accept-script-tags-in-innerhtml.html
        //we have to cope with deficiencies between ie and its simulations in this case
        var probe = document.createElement("div");
        probe.innerHTML = "<table><tbody><tr><td><div></div></td></tr></tbody></table>";
        var depth = 0;
        while (probe) {
            probe = probe.childNodes[0];
            depth++;
        }
        depth--;

        var dummyPlaceHolder = document.createElement("div");

        //fortunately a table element also works which is less critical than form elements regarding
        //the inner content
        dummyPlaceHolder.innerHTML = "<table><tbody><tr><td>" + markup + "</td></tr></tbody></table>";
        evalNodes = dummyPlaceHolder;
        for (var cnt = 0; cnt < depth; cnt++) {
            evalNodes = evalNodes.childNodes[0];
        }
        evalNodes = (evalNodes.parentNode) ? evalNodes.parentNode.childNodes : null;

        if ('undefined' == typeof evalNodes || null == evalNodes) {
            //fallback for htmlunit which should be good enough
            //to run the tests, maybe we have to wrap it as well
            dummyPlaceHolder.innerHTML = "<div>" + markup + "</div>";
            //note this is triggered only in htmlunit no other browser
            //so we are save here
            evalNodes = dummyPlaceHolder.childNodes[0].childNodes;
        }

        //ie throws also an error on length requests
        evalNodes = this._Lang.objToArray(evalNodes);
        if (evalNodes.length == 1) {
            item.parentNode.replaceChild(evalNodes[0], item);
            delete item;
            return evalNodes[0];
            //return this.replaceElement(item, evalNodes[0]);
        } else {
            return this.replaceElements(item, evalNodes)
        }
    },


    /**
     * for performance reasons we work with replaceElement and replaceElements here
     * after measuring performance it has shown that passing down an array instead
     * of a single node makes replaceElement twice as slow, however
     * a single node case is the 95% case
     *
     * @param item
     * @param evalNodes
     */
    replaceElement: function(item, evalNode) {
        evalNode = item.parentNode.replaceChild(evalNode, item);
        return evalNode;
    },


    /**
     * replaces an element with another element or a set of elements
     *
     * @param item the item to be replaced
     *
     * @param evalNodes the elements
     */
    replaceElements: function (item, evalNodes) {
        var parentNode = item.parentNode;
        var evalNodesDefined = 'undefined' != typeof evalNodes.length;
        if (!evalNodesDefined) {
            throw new Error("replaceElements called while evalNodes is not an array");
        }
        var oldNode = item;
        var resultArr = this._Lang.objToArray(evalNodes);

        for (var cnt = 0; cnt < resultArr.length; cnt++) {
            if (cnt == 0) {
                oldNode = parentNode.replaceChild(resultArr[cnt], oldNode);
            } else {
                if (oldNode.nextSibling) {
                    oldNode = parentNode.insertBefore(resultArr[cnt], oldNode.nextSibling);
                } else {
                    oldNode = parentNode.appendChild(resultArr[cnt]);

                }
            }
            evalNodes = resultArr;
        }

        return evalNodes;
    },

    /**
     * finds a corresponding html item from a given identifier and
     * dom fragment
     * @param fragment the dom fragment to find the item for
     * @param itemId the identifier of the item
     */
    findById : function(fragment, itemId) {
        //we have to escape here

        if (fragment.getElementById) {
            return fragment.getElementById(itemId);
        }

        if (fragment.nodeType == 1 && fragment.querySelector) {
            try {
                //we can use the query selector here
                var newItemId = itemId;
                if (fragment.id && fragment.id === itemId) return fragment;
                if (this._Lang.isString(newItemId)) {
                    newItemId = newItemId.replace(/\./g, "\\.").replace(/:/g, "\\:");
                }

                return fragment.querySelector("#" + newItemId);
            } catch(e) {
                //in case the selector bombs we retry manually
            }
        }

        var filter = function(node) {
            return node && node.id && node.id === itemId;
        };
        try {
            return this.findFirst(fragment, filter);
        } finally {
            //ie6 fix code
            filter = null;
        }
    },

    /**
     * findfirst functionality, finds the first element
     * for which the filter can trigger
     *
     * @param fragment the processed fragment/domNode
     * @param filter a filter closure which either returns true or false depending on triggering or not
     */
    findFirst : function(fragment, filter) {
        this._Lang.assertType(filter, "function");

        if (document.createTreeWalker && NodeFilter) {
            return this._iteratorFindFirst(fragment, filter);
        } else {
            return this._recursionFindFirst(fragment, filter);
        }
    },

    /**
     * a simple recusion based find first which iterates over the
     * dom tree the classical way which is also the slowest one
     * but that one will work back to ie6+
     *
     * @param fragment the starting fragment
     * @param filter the filter to be applied to
     */
    _recursionFindFirst: function(fragment, filter) {
        if (filter(fragment)) {
            return fragment;
        }

        if (!fragment.childNodes) {
            return null;
        }

        //sub-fragment usecases
        var child;
        var cnt;
        var childLen = fragment.childNodes.length;
        for (cnt = 0; cnt < childLen; cnt++) {
            child = fragment.childNodes[cnt];
            var item = this._recursionFindFirst(child, filter);
            if (item != null)
                return item;
        }
        return null;
    },

    /**
     * the faster based iterator findFirst which will work
     * on all html5 compliant browsers and a bunch of older ones
     *
     * @param fragment the fragment to be started from
     * @param filter the filter which has to be used
     */
    _iteratorFindFirst:function(fragment, filter) {
        if (filter(fragment)) {
            return fragment;
        }
        //we have a tree walker in place this allows for an optimized deep scan

        var walkerFilter = function (node) {
            return (filter(node)) ? NodeFilter.FILTER_ACCEPT : NodeFilter.FILTER_SKIP;
        };
        var treeWalker = document.createTreeWalker(fragment, NodeFilter.SHOW_ELEMENT, walkerFilter, false);
        if (treeWalker.nextNode()) {
            /** @namespace treeWalker.currentNode */
            return treeWalker.currentNode;
        }
        return null;
    },


    /**
     * optimized search for an array of tag names
     *
     * @param fragment the fragment which should be searched for
     * @param tagNames an map indx of tag names which have to be found
     * @param deepScan if set to true a deep scan is performed otherwise a shallow scan
     */
    findByTagNames: function(fragment, tagNames, deepScan) {


        //shortcut for single components
        if (!deepScan && tagNames[fragment.tagName.toLowerCase()]) {
            return fragment;
        }

        //shortcut elementsByTagName
        if (deepScan && this._Lang.exists(fragment, "getElementsByTagName")) {
            var retArr = [];
            for (var key in tagNames) {
                var foundElems = this.findByTagName(fragment, key, deepScan);
                if (foundElems) {
                    retArr = retArr.concat(foundElems);
                }
            }
            return retArr;
        } else if (deepScan) {
            //no node type with child tags we can handle that without node type checking
            return null;
        }

        //now the filter function checks case insensitively for the tag names needed
        var filter = function(node) {
            return node.tagName && tagNames[node.tagName.toLowerCase()];
        };

        //now we run an optimized find all on it
        try {
            return this.findAll(fragment, filter, deepScan);
        } finally {
            //the usual IE6 is broken, fix code
            filter = null;
        }
    },

    /**
     * determines the number of nodes according to their tagType
     *
     * @param {Node} fragment (Node or fragment) the fragment to be investigated
     * @param {String} tagName the tag name (lowercase)
     * @param {Boolean} deepScan if set to true a found element does not prevent to scan deeper
     * (the normal usecase is false, which means if the element is found only its
     * adjacent elements will be scanned, due to the recursive descension
     * this should work out with elements with different nesting depths but not being
     * parent and child to each other
     *
     * @return the child elements as array or null if nothing is found
     *
     */
    findByTagName : function(fragment, tagName, deepScan) {

        //remapping to save a few bytes
        var _Lang = this._Lang;

        deepScan = !!deepScan;

        //elements by tagname is the fastest, ie throws an error on fragment.getElementsByTagName, the exists type
        //via namespace array checking is safe
        if (deepScan && _Lang.exists(fragment, "getElementsByTagName")) {
            var ret = _Lang.objToArray(fragment.getElementsByTagName(tagName));
            if (fragment.tagName && _Lang.equalsIgnoreCase(fragment.tagName, tagName)) ret.unshift(fragment);
            return ret;
        } else if (deepScan) {
            //no node type with child tags we can handle that without node type checking
            return null;
        }
        //since getElementsByTagName is a standardized dom node function and ie also supports
        //it since 5.5
        //we need no fallback to the query api and the recursive filter
        //also is only needed in case of no deep scan or non dom elements

        var filter = function(node) {
            return node.tagName && _Lang.equalsIgnoreCase(node.tagName, tagName);
        };
        try {
            return this.findAll(fragment, filter, deepScan);
        } finally {
            //the usual IE6 is broken, fix code
            filter = null;
            _Lang = null;
        }

    },

    findByName : function(fragment, name, deepScan) {
        var _Lang = this._Lang;
        var filter = function(node) {
            return  node.name && _Lang.equalsIgnoreCase(node.name, name);
        };
        try {
            deepScan = !!deepScan;

            //elements byName is the fastest
            if (deepScan && _Lang.exists(fragment, "getElementsByName")) {
                var ret = _Lang.objToArray(fragment.getElementsByName(name));
                if (fragment.name == name) ret.unshift(fragment);
                return ret;

            }

            if (deepScan && _Lang.exists(fragment, "querySelectorAll")) {
                try {
                    var newName = name;
                    if (_Lang.isString(newName)) {
                        newName = newName.replace(/\./g, "\\.").replace(/:/g, "\\:");
                    }
                    var result = fragment.querySelectorAll("[name=" + newName + "]");
                    if (fragment.nodeType == 1 && filter(fragment)) {
                        result = (result == null) ? [] : _Lang.objToArray(result);
                        result.push(fragment);
                    }
                    return result;
                } catch(e) {
                    //in case the selector bombs we retry manually
                }
            }

            return this.findAll(fragment, filter, deepScan);
        } finally {
            //the usual IE6 is broken, fix code
            filter = null;
            _Lang = null;
        }
    }
    ,

    /**
     * finds the elements by an attached style class
     *
     * @param fragment the source fragment which is the root of our search (included in the search)
     * @param styleClass the styleclass to search for
     * @param deepScan if set to true a deep scan can be performed
     */
    findByStyleClass : function(fragment, styleClass, deepScan) {
        var filter = this._Lang.hitch(this, function(node) {
            var classes = this.getClasses(node);
            var len = classes.length;
            if (len == 0) return false;
            else {
                for (var cnt = 0; cnt < len; cnt++) {
                    if (classes[cnt] === styleClass) return true;
                }
            }
            return false;
        });
        try {
            deepScan = !!deepScan;

            //html5 getElementsByClassname

            //TODO implement this
            /*if (fragment.getElementsByClassName && deepScan) {
             return fragment.getElementsByClassName(styleClass);
             }

             //html5 speed optimization for browsers which do not ,
             //have the getElementsByClassName implemented
             //but only for deep scan and normal parent nodes
             else */
            if (this._Lang.exists(fragment, "querySelectorAll") && deepScan) {
                try {
                    var result = fragment.querySelectorAll("." + styleClass.replace(/\./g, "\\."));

                    if (fragment.nodeType == 1 && filter(fragment)) {
                        result = (result == null) ? [] : result;
                        result = this._Lang.objToArray(result);
                        result.push(fragment);
                    }
                    return result;
                } catch(e) {
                    //in case the selector bombs we have to retry with a different method
                }
            } else {
                //fallback to the classical filter methods if we cannot use the
                //html 5 selectors for whatever reason
                return this.findAll(fragment, filter, deepScan);
            }

        } finally {
            //the usual IE6 is broken, fix code
            filter = null;

        }
    }
    ,

    /**
     * a filtered findAll for subdom treewalking
     * (which uses browser optimizations wherever possible)
     *
     * @param {|Node|} rootNode the rootNode so start the scan
     * @param filter filter closure with the syntax {boolean} filter({Node} node)
     * @param deepScan if set to true or not set at all a deep scan is performed (for form scans it does not make much sense to deeply scan)
     */
    findAll : function(rootNode, filter, deepScan) {
        this._Lang.assertType(filter, "function");
        deepScan = !!deepScan;

        if (document.createTreeWalker && NodeFilter) {
            return this._iteratorSearchAll(rootNode, filter, deepScan);
        } else {
            return this._recursionSearchAll(rootNode, filter, deepScan);
        }

    }
    ,

    /**
     * classical recursive way which definitely will work on all browsers
     * including the IE6
     *
     * @param rootNode the root node
     * @param filter the filter to be applied to
     * @param deepScan if set to true a deep scan is performed
     */
    _recursionSearchAll: function(rootNode, filter, deepScan) {
        var ret = [];
        //fix the value to prevent undefined errors

        if (filter(rootNode)) {
            ret.push(rootNode);
            if (!deepScan) return ret;
        }

        //
        if (!rootNode.childNodes) {
            return ret;
        }

        //subfragment usecases

        var retLen = ret.length;
        var childLen = rootNode.childNodes.length;
        for (var cnt = 0; (deepScan || retLen == 0) && cnt < childLen; cnt++) {
            ret = ret.concat(this._recursionSearchAll(rootNode.childNodes[cnt], filter, deepScan));
        }
        return ret;
    }
    ,

    /**
     * the faster dom iterator based search, works on all newer browsers
     * except ie8 which already have implemented the dom iterator functions
     * of html 5 (which is pretty all standard compliant browsers)
     *
     * The advantage of this method is a faster tree iteration compared
     * to the normal recursive tree walking.
     *
     * @param rootNode the root node to be iterated over
     * @param filter the iteration filter
     * @param deepScan if set to true a deep scan is performed
     */
    _iteratorSearchAll: function(rootNode, filter, deepScan) {
        var retVal = [];
        //Works on firefox and webkit, opera and ie have to use the slower fallback mechanis
        //we have a tree walker in place this allows for an optimized deep scan
        if (filter(rootNode)) {

            retVal.push(rootNode);
            if (!deepScan) {
                return retVal;
            }
        }
        //we use the reject mechanism to prevent a deep scan reject means any
        //child elements will be omitted from the scan
        var walkerFilter = function (node) {
            var retCode = (filter(node)) ? NodeFilter.FILTER_ACCEPT : NodeFilter.FILTER_SKIP;
            retCode = (!deepScan && retCode == NodeFilter.FILTER_ACCEPT) ? NodeFilter.FILTER_REJECT : retCode;
            if (retCode == NodeFilter.FILTER_ACCEPT || retCode == NodeFilter.FILTER_REJECT) {
                retVal.push(node);
            }
            return retCode;
        };
        var treeWalker = document.createTreeWalker(rootNode, NodeFilter.SHOW_ELEMENT, walkerFilter, false);
        //noinspection StatementWithEmptyBodyJS
        while (treeWalker.nextNode());
        return retVal;
    }
    ,

    /**
     *
     * @param {Node} form
     * @param {String} nameId
     *
     * checks for a a element with the name or identifier of nameOrIdentifier
     * @returns the found node or null otherwise
     */
    findFormElement : function(form, nameId) {
        if (!form) {
            throw Error("_Dom.findFormElement a form node must be given");
        }
        if (!nameId) {
            throw Error("_Dom.findFormElement an element or identifier must be given");
        }
        if (!form.elements) return null;
        return form.elements[nameId] || this.findById(form, nameId);
    }
    ,

    /**
     * bugfixing for ie6 which does not cope properly with setAttribute
     */
    setAttribute : function(node, attr, val) {

        if (!node) {
            throw Error("_Dom.setAttribute a  node must be given");
        }
        if (!attr) {
            throw Error("_Dom.setAttribute an attribute must be given");
        }

        //quirks mode and ie7 mode has the attributes problems ie8 standards mode behaves like
        //a good citizen
        var _Browser = myfaces._impl.core._Runtime.browser;
        if (!_Browser.isIE || _Browser.isIE > 7) {
            if (!node.setAttribute) {
                return;
            }
            node.setAttribute(attr, val);
            return;
        }

        /*
         Now to the broken browsers IE6+.... ie7 and ie8 quirks mode

         we deal mainly with three problems here
         class and for are not handled correctly
         styles are arrays and cannot be set directly
         and javascript events cannot be set via setAttribute as well!

         or in original words of quirksmode.org ... this is a mess!

         Btw. thank you Microsoft for providing all necessary tools for free
         for being able to debug this entire mess in the ie rendering engine out
         (which is the Microsoft ie vms, developers toolbar, Visual Web Developer 2008 express
         and the ie8 8 developers toolset!)

         also thank you http://www.quirksmode.org/
         dojotoolkit.org and   //http://delete.me.uk/2004/09/ieproto.html
         for additional information on this mess!

         The lowest common denominator tested within this code
         is IE6, older browsers for now are legacy!
         */
        attr = attr.toLowerCase();

        if (attr === "class") {
            node.setAttribute("className", val);
        } else if (attr === "for") {

            node.setAttribute("htmlFor", val);
        } else if (attr === "style") {
            //We have to split the styles here and assign them one by one
            var styles = val.split(";");
            var stylesLen = styles.length;
            for (var loop = 0; loop < stylesLen; loop++) {
                var keyVal = styles[loop].split(":");
                if (keyVal[0] != "" && keyVal[0] == "opacity") {
                    //special ie quirks handling for opacity

                    var opacityVal = Math.max(100, Math.round(parseFloat(keyVal[1]) * 10));
                    node.style.setAttribute("arrFilter", "alpha(opacity=" + opacityVal + ")");
                    //if you need more hacks I would recommend
                    //to use the class attribute and conditional ie includes!
                } else if (keyVal[0] != "") {
                    node.style.setAttribute(keyVal[0], keyVal[1]);
                }
            }
        } else {
            //check if the attribute is an event, since this applies only
            //to quirks mode of ie anyway we can live with the standard html4/xhtml
            //ie supported events
            if (this.IE_QUIRKS_EVENTS[attr]) {
                if (this._Lang.isString(attr)) {
                    //event resolves to window.event in ie
                    node.setAttribute(attr, function() {
                        //event implicitly used
                        return this._Lang.globalEval(val);
                    });
                }
            } else {
                //unknown cases we try to catch them via standard setAttributes
                node.setAttribute(attr, val);
            }
        }
    },

    /**
     * gets an element from a form with its id -> sometimes two elements have got
     * the same id but are located in different forms -> MyFaces 1.1.4 two forms ->
     * 2 inputHidden fields with ID jsf_tree_64 & jsf_state_64 ->
     * http://www.arcknowledge.com/gmane.comp.jakarta.myfaces.devel/2005-09/msg01269.html
     *
     * @param {String} nameId - ID of the HTML element located inside the form
     * @param {Node} form - form element containing the element
     * @param {boolean} nameSearch if set to true a search for name is also done
     * @param {boolean} localOnly if set to true a local search is performed only (a full document search is omitted)
     * @return {Object}   the element if found else null
     *
     */
    getElementFromForm : function(nameId, form, nameSearch, localOnly) {
        if (!nameId) {
            throw Error("_Dom.getElementFromForm an item id or name must be given");
        }

        if (!form) {
            return this.byId(nameId);
        }

        var isNameSearch = !!nameSearch;
        var isLocalSearchOnly = !!localOnly;

        //we first check for a name entry!
        if (isNameSearch && this._Lang.exists(form, "elements." + nameId)) {
            return form.elements[nameId];
        }

        //if no name entry is found we check for an Id but only in the form
        var element = this.findById(form, nameId);
        if (element) {
            return element;
        }

        // element not found inside the form -> try document.getElementById
        // (can be null if element doesn't exist)
        if (!isLocalSearchOnly) {
            return this.byId(nameId);
        }

        return null;
    }
    ,

    /**
     * fuzzy form detection which tries to determine the form
     * an item has been detached.
     *
     * The problem is some Javascript libraries simply try to
     * detach controls by reusing the names
     * of the detached input controls. Most of the times,
     * the name is unique in a jsf scenario, due to the inherent form mapping.
     * One way or the other, we will try to fix that by
     * identifying the proper form over the name
     *
     * We do it in several ways, in case of no form null is returned
     * in case of multiple forms we check all elements with a given name (which we determine
     * out of a name or id of the detached element) and then iterate over them
     * to find whether they are in a form or not.
     *
     * If only one element within a form and a given identifier found then we can pull out
     * and move on
     *
     * We cannot do much further because in case of two identical named elements
     * all checks must fail and the first elements form is served.
     *
     * Note, this method is only triggered in case of the issuer or an ajax request
     * is a detached element, otherwise already existing code has served the correct form.
     *
     * This method was added because of
     * https://issues.apache.org/jira/browse/MYFACES-2599
     * to support the integration of existing ajax libraries which do heavy dom manipulation on the
     * controls side (Dojos Dijit library for instance).
     *
     * @param {Node} elem - element as source, can be detached, undefined or null
     *
     * @return either null or a form node if it could be determined
     */
    fuzzyFormDetection : function(elem) {
        if (!document.forms || !document.forms.length) {
            return null;
        }


        // This will not work well on portlet case, because we cannot be sure
        // the returned form is right one.
        //we can cover that case by simply adding one of our config params
        //the default is the weaker, but more correct portlet code
        //you can override it with myfaces_config.no_portlet_env = true globally
        else if (1 == document.forms.length && myfaces._impl.core._Runtime.getGlobalConfig("no_portlet_env", false)) {
            return document.forms[0];
        }
        if (!elem) {
            return null;
        }

        //before going into the more complicated stuff we try the simple approach
        if (!this._Lang.isString(elem)) {
            //element of type form then we are already
            //at form level for the issuing element
            //https://issues.apache.org/jira/browse/MYFACES-2793
            if (this._Lang.equalsIgnoreCase(elem.tagName, "form")) {
                return elem;
            }

            return this.getParent(elem, "form");
        }

        var id = elem.id || null;
        var name = elem.name || null;
        //a framework in a detachment case also can replace an existing identifier element
        // with a name element
        name = name || id;
        var foundForm;

        if (id && '' != id) {
            //we have to assert that the element passed down is detached
            var domElement = this.byId(id);
            if (domElement) {
                foundForm = this.getParent(domElement, "form");
                if (null != foundForm) return foundForm;
            }
        }

        /**
         * name check
         */
        var foundElements = [];

        /**
         * the lesser chance is the elements which have the same name
         * (which is the more likely case in case of a brute dom replacement)
         */
        var nameElems = document.getElementsByName(name);
        if (nameElems) {
            for (var cnt = 0; cnt < nameElems.length && foundElements.length < 2; cnt++) {
                // we already have covered the identifier case hence we only can deal with names,
                foundForm = this.getParent(nameElems[cnt], "form");
                if (null != foundForm) {
                    foundElements.push(foundForm);
                }
            }
        }

        return (1 == foundElements.length ) ? foundElements[0] : null;
    }
    ,

    /**
     * gets a parent of an item with a given tagname
     * @param {Node} item - child element
     * @param {String} tagName - TagName of parent element
     */
    getParent : function(item, tagName) {

        if (!item) {
            throw Error("myfaces._impl._util._Dom.getParent: item must be set");
        }

        var _Lang = this._Lang;
        var searchClosure = function(parentItem) {
            return parentItem && parentItem.tagName
                    && _Lang.equalsIgnoreCase(parentItem.tagName, tagName);
        };

        return this.getFilteredParent(item, searchClosure);
    }
    ,

    /**
     * A parent walker which uses
     * a filter closure for filtering
     *
     * @param {Node} item the root item to ascend from
     * @param {function} filter the filter closure
     */
    getFilteredParent : function(item, filter) {
        if (!item) {
            throw Error("myfaces._impl._util._Dom.getFilteredParent: item must be set");
        }
        if (!filter) {
            throw Error("myfaces._impl._util._Dom.getFilteredParent: filter must be set");
        }

        //search parent tag parentName
        var parentItem = (item.parentNode) ? item.parentNode : null;

        while (parentItem && !filter(parentItem)) {
            parentItem = parentItem.parentNode;
        }
        return (parentItem) ? parentItem : null;
    }
    ,

    /**
     * a closure based child filtering routine
     * which steps one level down the tree and
     * applies the filter closure
     *
     * @param item the node which has to be investigates
     * @param filter the filter closure
     */
    getFilteredChild: function(item, filter) {
        if (!item) {
            throw Error("myfaces._impl._util._Dom.getFilteredChild: item must be set");
        }
        if (!filter) {
            throw Error("myfaces._impl._util._Dom.getFilteredChild: filter must be set");
        }

        var childs = item.childNodes;
        if (!childs) {
            return null;
        }
        for (var c = 0, cLen = childs.length; c < cLen; c++) {
            if (filter(childs[c])) {
                return childs[c];
            }
        }
        return null;
    }
    ,

    /**
     * gets the child of an item with a given tag name
     * @param {Node} item - parent element
     * @param {String} childName - TagName of child element
     * @param {String} itemName - name  attribute the child can have (can be null)
     * @Deprecated
     */
    getChild: function(item, childName, itemName) {
        var _Lang = this._Lang;

        function filter(node) {
            return node.tagName
                    && _Lang.equalsIgnoreCase(node.tagName, childName)
                    && (!itemName || (itemName && itemName == node.getAttribute("name")));

        }

        return this.getFilteredChild(item, filter);
    }
    ,

    /**
     * cross ported from dojo
     * fetches an attribute from a node
     *
     * @param {String} node the node
     * @param {String} attr the attribute
     * @return the attributes value or null
     */
    getAttribute : function(/* HTMLElement */node, /* string */attr) {
        //	summary
        //	Returns the value of attribute attr from node.
        node = this.byId(node);
        // FIXME: need to add support for attr-specific accessors
        if ((!node) || (!node.getAttribute)) {
            // if(attr !== 'nwType'){
            //	alert("getAttr of '" + attr + "' with bad node");
            // }
            return null;
        }
        var ta = typeof attr == 'string' ? attr : new String(attr);

        // first try the approach most likely to succeed
        var v = node.getAttribute(ta.toUpperCase());
        if ((v) && (typeof v == 'string') && (v != "")) {
            return v;	//	string
        }

        // try returning the attributes value, if we couldn't get it as a string
        if (v && v.value) {
            return v.value;	//	string
        }

        // this should work on Opera 7, but it's a little on the crashy side
        if ((node.getAttributeNode) && (node.getAttributeNode(ta))) {
            return (node.getAttributeNode(ta)).value;	//	string
        } else if (node.getAttribute(ta)) {
            return node.getAttribute(ta);	//	string
        } else if (node.getAttribute(ta.toLowerCase())) {
            return node.getAttribute(ta.toLowerCase());	//	string
        }
        return null;	//	string
    }
    ,

    /**
     * checks whether the given node has an attribute attached
     *
     * @param {String|Object} node the node to search for
     * @param {String} attr the attribute to search for
     * @true if the attribute was found
     */
    hasAttribute : function(/* HTMLElement */node, /* string */attr) {
        //	summary
        //	Determines whether or not the specified node carries a value for the attribute in question.
        return this.getAttribute(node, attr) ? true : false;	//	boolean
    }
    ,

    /**
     * fetches the style class for the node
     * cross ported from the dojo toolkit
     * @param {String|Object} node the node to search
     * @returns the className or ""
     */
    getClass : function(node) {
        node = this.byId(node);
        if (!node) {
            return "";
        }
        var cs = "";
        if (node.className) {
            cs = node.className;
        } else {
            if (this.hasAttribute(node, "class")) {
                cs = this.getAttribute(node, "class");
            }
        }
        return cs.replace(/^\s+|\s+$/g, "");
    }
    ,
    /**
     * fetches the class for the node,
     * cross ported from the dojo toolkit
     * @param {String|Object}node the node to search
     */
    getClasses : function(node) {
        var c = this.getClass(node);
        return (c == "") ? [] : c.split(/\s+/g);
    }
    ,

    /**
     * concatenation routine which concats all childnodes of a node which
     * contains a set of CDATA blocks to one big string
     * @param {Node} node the node to concat its blocks for
     */
    concatCDATABlocks : function(/*Node*/ node) {
        var cDataBlock = [];
        // response may contain several blocks
        for (var i = 0; i < node.childNodes.length; i++) {
            cDataBlock.push(node.childNodes[i].data);
        }
        return cDataBlock.join('');
    },

    isManualScriptEval: function() {

        if (!this._Lang.exists(myfaces, "config._autoeval")) {
            var _Browser = myfaces._impl.core._Runtime.browser;
            //now we rely on the document being processed if called for the first time
            var evalDiv = document.createElement("div");
            this._Lang.reserveNamespace("myfaces.config._autoeval");
            //null not swallowed
            myfaces.config._autoeval = false;

            var markup = "<script type='text/javascript'> myfaces.config._autoeval = true; </script>";
            //now we rely on the same replacement mechanisms as outerhtml because
            //some browsers have different behavior of embedded scripts in the contextualfragment
            //or innerhtml case (opera for instance), this way we make sure the
            //eval detection is covered correctly
            this.setAttribute(evalDiv, "style", "display:none");

            //it is less critical in some browsers (old ie versions)
            //to append as first element than as last
            //it should not make any difference layoutwise since we are on display none anyway.
            if (document.body.childNodes.length > 0) {
                document.body.insertBefore(evalDiv, document.body.firstChild);
            } else {
                document.body.appendChild(evalDiv);
            }

            //we remap it into a real boolean value
            if (window.Range
                    && typeof Range.prototype.createContextualFragment == 'function') {
                this._outerHTMLCompliant(evalDiv, markup);
            } else {
                this._outerHTMLNonCompliant(evalDiv, markup);
            }

        }

        return  !myfaces.config._autoeval;
        /* var d = _this.browser;


         return (_this.exists(d, "isIE") &&
         ( d.isIE > 5.5)) ||
         //firefox at version 4 beginning has dropped
         //auto eval to be compliant with the rest
         (_this.exists(d, "isFF") &&
         (d.isFF > 3.9)) ||
         (_this.exists(d, "isKhtml") &&
         (d.isKhtml > 0)) ||
         (_this.exists(d, "isWebKit") &&
         (d.isWebKit > 0)) ||
         (_this.exists(d, "isSafari") &&
         (d.isSafari > 0));
         */
        //another way to determine this without direct user agent parsing probably could
        //be to add an embedded script tag programmatically and check for the script variable
        //set by the script if existing, the add went through an eval if not then we
        //have to deal with it ourselves, this might be dangerous in case of the ie however
        //so in case of ie we have to parse for all other browsers we can make a dynamic
        //check if the browser does auto eval

    },

    isMultipartCandidate: function(executes) {
        if (this._Lang.isString(executes)) {
            executes = this._Lang.strToArray(executes, /\s+/);
        }

        for (var exec in executes) {
            var element = this.byId(executes[exec]);
            var inputs = this.findByTagName(element, "input", true);
            for (var key in inputs) {
                if (this.getAttribute(inputs[key], "type") == "file") return true;
            }
        }
        return false;
    },


    byId: function(id) {
        return this._Lang.byId(id);
    }
});



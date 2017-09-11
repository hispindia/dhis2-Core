!function(e) {
    function t(r) {
        if (n[r]) return n[r].exports;
        var i = n[r] = {
            exports: {},
            id: r,
            loaded: !1
        };
        return e[r].call(i.exports, i, i.exports, t), i.loaded = !0, i.exports;
    }
    var n = {};
    return t.m = e, t.c = n, t.p = "http://localhost:8081/", t(0);
}([ function(e, t, n) {
    (function(e) {
        "use strict";
        function t(e) {
            return e && e.__esModule ? e : {
                default: e
            };
        }
        function r(e) {
            var t = e.keyUiLocale;
            t && "en" !== t && o.config.i18n.sources.add("./i18n/i18n_module_" + t + ".properties"), 
            o.config.i18n.sources.add("./i18n/i18n_module_en.properties");
        }
        function i(e) {
            return new Promise(function(t, n) {
                var r = document.getElementsByTagName("head")[0], i = r.getElementsByTagName("script").length, a = r.getElementsByTagName("script")[i - 1], o = document.createElement("script");
                o.src = e, o.async = !1, o.type = "text/javascript", o.addEventListener("load", function() {
                    t(o);
                }, !1), o.addEventListener("error", function() {
                    n(o);
                }, !1), a.parentNode.insertBefore(o, a.nextSibling);
            });
        }
        function a() {
            (0, o.getInstance)().then(function(e) {
                var t = e.Api.getApi(), n = void 0, r = void 0, i = void 0;
                t.get("/me/dashboard", {}).then(function(t) {
                    n = t.unreadInterpretations, r = t.unreadMessageConversations, r > 0 ? (r > 1 ? jQuery("#messageCount").html(r + " " + e.i18n.getTranslation("unread_messages")) : jQuery("#messageCount").html(r + " " + e.i18n.getTranslation("unread_message")), 
                    jQuery("#messageCont").show()) : jQuery("#messageCont").hide(), n > 0 ? (n > 1 ? jQuery("#interpretationCount").html(n + " " + e.i18n.getTranslation("new_interpretations")) : jQuery("#interpretationCount").html(n + " " + e.i18n.getTranslation("new_interpretation")), 
                    jQuery("#interpretationCount").show()) : jQuery("#interpretationCount").hide();
                }), t.get("me", {}).then(function(e) {
                    i = c(e), i ? jQuery("#updateProfileCont").hide() : jQuery("#updateProfileCont").css("display", "inline");
                });
            });
        }
        var o = n(19), u = n(36), s = t(u), l = n(14);
        t(l);
        (0, o.getManifest)("./manifest.webapp").then(function(e) {
            var t = e.getBaseUrl();
            o.config.baseUrl = t + "/api";
        }).then(o.getUserSettings).then(r).then(o.init).catch(s.default.error.bind(s.default)), 
        Ext.onReady(function() {
            (0, o.getInstance)().then(function(e) {
                f(e);
            }).then(function() {
                return i("javascript/dashboard.js?_rev=2.26");
            });
        }), jQuery(document).ready(function() {
            a();
        });
        var c = function(e) {
            var t = [ e.jobTitle, e.introduction, e.gender, e.birthday, e.nationality, e.employer, e.education, e.interests, e.languages ], n = 0;
            return t.forEach(function(e) {
                n = null != e ? n + 1 : n;
            }), n > 3;
        }, f = function(t) {
            e.i18n_share_your_interpretation_of = t.i18n.getTranslation("share_your_interpretation_of"), 
            e.i18n_interpretation_was_shared = t.i18n.getTranslation("interpretation_was_shared"), 
            e.i18n_viewing = t.i18n.getTranslation("viewing"), e.i18n_click_add_new_to_get_started = t.i18n.getTranslation("click_add_new_to_get_started"), 
            e.i18n_add_stuff_by_searching = t.i18n.getTranslation("add_stuff_by_searching"), 
            e.i18n_arrange_dashboard_by_dragging_and_dropping = t.i18n.getTranslation("arrange_dashboard_by_dragging_and_dropping"), 
            e.i18n_remove = t.i18n.getTranslation("remove"), e.i18n_view_full_size = t.i18n.getTranslation("view_full_size"), 
            e.i18n_click_to_explore_drag_to_new_position = t.i18n.getTranslation("click_to_explore_drag_to_new_position"), 
            e.i18n_drag_to_new_position = t.i18n.getTranslation("drag_to_new_position"), e.i18n_manage = t.i18n.getTranslation("manage"), 
            e.i18n_share = t.i18n.getTranslation("share"), e.i18n_explore = t.i18n.getTranslation("explore"), 
            e.i18n_resize = t.i18n.getTranslation("resize"), e.i18n_share_interpretation = t.i18n.getTranslation("share_interpretation"), 
            e.i18n_see_more_hits = t.i18n.getTranslation("see_more_hits"), e.i18n_see_fewer_hits = t.i18n.getTranslation("see_fewer_hits"), 
            e.i18n_add = t.i18n.getTranslation("add"), e.i18n_click_and_drag_to_new_position = t.i18n.getTranslation("click_and_drag_to_new_position"), 
            e.i18n_get_as_image = t.i18n.getTranslation("get_as_image"), e.i18n_user_org_unit_filter = t.i18n.getTranslation("user_org_unit_filter"), 
            e.i18n_applies_to_favorites_with_user_org_units_only = t.i18n.getTranslation("applies_to_favorites_with_user_org_units_only"), 
            e.i18n_missing_dashboard_app = t.i18n.getTranslation("missing_dashboard_app"), e.i18n_update_profile = t.i18n.getTranslation("update_profile"), 
            e.i18n_add_new_dashboard = t.i18n.getTranslation("add_new_dashboard"), e.i18n_users = t.i18n.getTranslation("users"), 
            e.i18n_event_charts = t.i18n.getTranslation("event_charts"), e.i18n_maps = t.i18n.getTranslation("maps"), 
            e.i18n_event_reports = t.i18n.getTranslation("event_reports"), e.i18n_standard_reports = t.i18n.getTranslation("standard_reports"), 
            e.i18n_resources = t.i18n.getTranslation("resources"), e.i18n_apps = t.i18n.getTranslation("apps"), 
            e.i18n_share_your_data_interpretations = t.i18n.getTranslation("share_your_data_intepretations"), 
            e.i18n_can_edit_and_view = t.i18n.getTranslation("can_edit_and_view"), e.i18n_can_view = t.i18n.getTranslation("can_view"), 
            e.i18n_cancel = t.i18n.getTranslation("cancel"), e.i18n_save = t.i18n.getTranslation("save"), 
            e.i18n_pivot_tables = t.i18n.getTranslation("pivot_tables"), jQuery("#i18n_update_profile").html(t.i18n.getTranslation("update_profile")), 
            jQuery("#i18n_write_feedback").html(t.i18n.getTranslation("write_feedback")), jQuery("#i18n_interpretations").html(t.i18n.getTranslation("interpretations")), 
            jQuery("#searchField").attr({
                placeholder: t.i18n.getTranslation("search_for_users_charts_maps_reports")
            }), jQuery("#i18n_search").html(t.i18n.getTranslation("search")), jQuery("#i18n_rename_current_dashboard").html(t.i18n.getTranslation("rename_current_dashboard")), 
            jQuery("label[name='i18n_from']").html(t.i18n.getTranslation("from")), jQuery("#i18n_pivot_table").html(t.i18n.getTranslation("pivot_table")), 
            jQuery("#i18n_generate_pivot_table_click_share").html(t.i18n.getTranslation("generate_pivot_table_click_share")), 
            jQuery("#i18n_data_visualizer").html(t.i18n.getTranslation("data_visualizer")), 
            jQuery("label[name='i18n_load_favorite_click_share']").html(t.i18n.getTranslation("load_favorite_click_share")), 
            jQuery("#i18n_gis").html(t.i18n.getTranslation("gis")), jQuery("#i18n_data_set_report").html(t.i18n.getTranslation("data_set_report")), 
            jQuery("#i18n_generate_data_set_report_click_share").html(t.i18n.getTranslation("generate_data_set_report_click_share")), 
            jQuery("[name='i18n_name']").html(t.i18n.getTranslation("name")), jQuery("[name='i18n_create']").html(t.i18n.getTranslation("create")), 
            jQuery("[name='i18n_rename']").html(t.i18n.getTranslation("rename")), jQuery("#i18n_add_items_to_current_dashboard").html(t.i18n.getTranslation("add_items_to_current_dashboard")), 
            jQuery("[name='i18n_messages']").html(t.i18n.getTranslation("messages")), jQuery("[name='i18n_delete']").html(t.i18n.getTranslation("delete")), 
            jQuery("[name='i18n_translate']").html(t.i18n.getTranslation("translate")), jQuery("#i18n_applies_to_favorites_with_user_org_units_only").html(t.i18n.getTranslation("applies_to_favorites_with_user_org_units_only")), 
            jQuery("[name='i18n_clear']").html(t.i18n.getTranslation("clear")), jQuery("[name='i18n_update']").html(t.i18n.getTranslation("update")), 
            jQuery("[name='i18n_close']").html(t.i18n.getTranslation("close")), jQuery("[name='i18n_manage']").html(t.i18n.getTranslation("manage")), 
            jQuery("[name='i18n_share']").html(t.i18n.getTranslation("share")), jQuery("#headerBanner").attr({
                title: t.i18n.getTranslation("view_home_page")
            }), jQuery("[name='i18n_show_menu']").html(t.i18n.getTranslation("show_menu")), 
            jQuery("#i18n_add").html(t.i18n.getTranslation("add")), jQuery("#i18n_delete_current_dashboard").html(t.i18n.getTranslation("delete_current_dashboard")), 
            jQuery("#i18n_translate_current_dashboard").html(t.i18n.getTranslation("translate_current_dashboard")), 
            jQuery("#sharingSettings").attr({
                title: t.i18n.getTranslation("sharing_settings")
            }), jQuery("#sharingSearch").attr({
                placeholder: t.i18n.getTranslation("search_for_user_groups")
            }), jQuery("#i18n_created_by").html(t.i18n.getTranslation("created_by") + ":"), 
            jQuery("#i18n_external_access").html(t.i18n.getTranslation("external_access")), 
            jQuery("#i18n_public_access").html(t.i18n.getTranslation("public_access")), jQuery("[name='i18n_can_view']").html(t.i18n.getTranslation("can_view")), 
            jQuery("#i18n_none").html(t.i18n.getTranslation("none")), jQuery("[name='i18n_can_edit_and_view']").html(t.i18n.getTranslation("can_edit_and_view")), 
            jQuery("#i18n_interpretations").html(t.i18n.getTranslation("interpretations")), 
            jQuery("[name='i18n_interpretations']").html(t.i18n.getTranslation("interpretations")), 
            jQuery("#interpretationArea").attr({
                placeholder: t.i18n.getTranslation("write_your_interpretation")
            }), jQuery(".interpretationButton").val(t.i18n.getTranslation("share"));
        };
    }).call(t, function() {
        return this;
    }());
}, function(e, t) {
    "use strict";
    function n(e, t) {
        if (void 0 !== e) return !0;
        throw new Error([ t || "Value", "should be provided" ].join(" "));
    }
    function r(e, t, r) {
        if (n(e, r), n(t, "Type"), "function" == typeof t && e instanceof t || "string" == typeof t && typeof e === t) return !0;
        throw new Error([ "Expected", r || e, "to have type", t ].join(" "));
    }
    function i(e, t) {
        function n() {}
        try {
            return r(e, t), !0;
        } catch (e) {
            n();
        }
        return !1;
    }
    function a(e) {
        return i(e, "string");
    }
    function o(e) {
        return Array.isArray(e);
    }
    function u(e) {
        return i(e, Object);
    }
    function s(e) {
        return void 0 !== e;
    }
    function l(e) {
        return "number" == typeof e && isFinite(e) && e > -9007199254740992 && e < 9007199254740992 && Math.floor(e) === e;
    }
    function c(e) {
        return "number" == typeof e && isFinite(e) && e - parseFloat(e) + 1 >= 0;
    }
    function f(e, t) {
        var n = o(t) && t || [];
        return n.indexOf(e) >= 0;
    }
    function d(e) {
        return e && 11 === e.length;
    }
    Object.defineProperty(t, "__esModule", {
        value: !0
    }), t.checkDefined = n, t.checkType = r, t.isType = i, t.isString = a, t.isArray = o, 
    t.isObject = u, t.isDefined = s, t.isInteger = l, t.isNumeric = c, t.contains = f, 
    t.isValidUid = d, Number.isInteger || (Number.isInteger = l), t.default = {
        checkType: r,
        checkDefined: n,
        isArray: o,
        isDefined: s,
        isInteger: l,
        isNumeric: c,
        isString: a,
        isType: i,
        contains: f,
        isValidUid: d
    };
}, function(e, t, n) {
    "use strict";
    function r(e) {
        return e && e.__esModule ? e : {
            default: e
        };
    }
    function i(e, t) {
        if (!(e instanceof t)) throw new TypeError("Cannot call a class as a function");
    }
    function a() {
        var e = arguments.length <= 0 || void 0 === arguments[0] ? "REPLACE" : arguments[0], t = y.default.getSystem();
        return t.version && Number(t.version.minor) <= 22 ? "mergeStrategy=" + e : "mergeMode=" + e;
    }
    function o(e) {
        return function(t) {
            e(t);
        };
    }
    function u(e) {
        return function(t) {
            return t.responseJSON ? e(t.responseJSON) : (delete t.then, e(t));
        };
    }
    function s(e, t) {
        if (new RegExp("^(:?https?:)?//").test(t)) return t;
        var n = [];
        return e && n.push(e), n.push(t), n.join("/").replace(new RegExp("(.(?:[^:]))//+", "g"), "$1/").replace(new RegExp("/$"), "");
    }
    function l() {
        return l.api ? l.api : l.api = new v(p.default);
    }
    Object.defineProperty(t, "__esModule", {
        value: !0
    });
    var c = function() {
        function e(e, t) {
            for (var n = 0; n < t.length; n++) {
                var r = t[n];
                r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), 
                Object.defineProperty(e, r.key, r);
            }
        }
        return function(t, n, r) {
            return n && e(t.prototype, n), r && e(t, r), t;
        };
    }(), f = n(1), d = n(8), p = r(d), h = n(13), y = r(h), v = function() {
        function e(t) {
            if (i(this, e), !t) throw new Error("D2 requires jQuery");
            this.jquery = t, this.baseUrl = "/api", this.defaultRequestSettings = {
                headers: {
                    "Cache-Control": "no-store"
                },
                data: {},
                contentType: "application/json",
                type: void 0,
                url: void 0
            };
        }
        return c(e, [ {
            key: "get",
            value: function(e, t, n) {
                return this.request("GET", s(this.baseUrl, e), t, n);
            }
        }, {
            key: "post",
            value: function(e, t, n) {
                var r = !n || void 0 === n.contentType || "text/plain" !== n.contentType && n.contentType !== !1 ? JSON.stringify(t) : t;
                return this.request("POST", s(this.baseUrl, e), r, n);
            }
        }, {
            key: "delete",
            value: function(e, t) {
                return this.request("DELETE", s(this.baseUrl, e), void 0, t);
            }
        }, {
            key: "update",
            value: function(e, t) {
                var n = !(arguments.length <= 2 || void 0 === arguments[2]) && arguments[2], r = n === !0 ? e + "?" + a() : e;
                return this.request("PUT", s(this.baseUrl, r), JSON.stringify(t));
            }
        }, {
            key: "request",
            value: function(e, t, n) {
                function r(t, n) {
                    var r = n;
                    void 0 === r ? r = {} : r !== !0 && r !== !1 || (r = r.toString());
                    var o = Object.assign({}, l.defaultRequestSettings, t);
                    return o.type = e, o.url = a, o.data = r, o.dataType = void 0 !== i.dataType ? i.dataType : "json", 
                    o.contentType = void 0 !== i.contentType ? i.contentType : "application/json", ("GET" === e || !n && 0 !== n && n !== !1) && (o.contentType = void 0), 
                    o;
                }
                var i = arguments.length <= 3 || void 0 === arguments[3] ? {} : arguments[3];
                (0, f.checkType)(e, "string", "Request type"), (0, f.checkType)(t, "string", "Url");
                var a = t;
                if (n && n.filter) {
                    var s = n.filter.reduce(function(e, t) {
                        var n = e.length ? "&" : "", r = "filter=" + t;
                        return "" + e + n + r;
                    }, "");
                    delete n.filter, a += "?" + s;
                }
                var l = this;
                return new Promise(function(e, t) {
                    l.jquery.ajax(r(i, n)).then(o(e), u(t));
                });
            }
        }, {
            key: "setBaseUrl",
            value: function(e) {
                return (0, f.checkType)(e, "string", "Base url"), this.baseUrl = e, this;
            }
        } ]), e;
    }();
    v.getApi = l, t.default = v, e.exports = t.default;
}, function(e, t) {
    "use strict";
    function n(e, t) {
        if (!(e instanceof t)) throw new TypeError("Cannot call a class as a function");
    }
    function r(e) {
        throw new Error(e);
    }
    function i(e, t) {
        return function() {
            for (var n = arguments.length, r = Array(n), i = 0; i < n; i++) r[i] = arguments[i];
            return e.apply(this, [ t ].concat(r));
        };
    }
    function a(e, t, n) {
        var r = {
            enumerable: !0,
            configurable: !1,
            writable: !1,
            value: n
        };
        Object.defineProperty(e, t, r);
    }
    function o(e, t) {
        var n = void 0;
        for (n in t) t.hasOwnProperty(n) && (e[n] = t[n]);
        return e;
    }
    function u(e) {
        return function(t) {
            if (t) return t[e];
        };
    }
    Object.defineProperty(t, "__esModule", {
        value: !0
    });
    var s = function() {
        function e(e, t) {
            for (var n = 0; n < t.length; n++) {
                var r = t[n];
                r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), 
                Object.defineProperty(e, r.key, r);
            }
        }
        return function(t, n, r) {
            return n && e(t.prototype, n), r && e(t, r), t;
        };
    }();
    t.throwError = r, t.curry = i, t.addLockedProperty = a, t.copyOwnProperties = o, 
    t.pick = u;
    var l = function() {
        function e() {
            var t = this;
            n(this, e), this.promise = new Promise(function(e, n) {
                t.resolve = e, t.reject = n;
            });
        }
        return s(e, null, [ {
            key: "create",
            value: function() {
                return new e();
            }
        } ]), e;
    }();
    t.Deferred = l;
}, function(e, t, n) {
    (function(r) {
        "use strict";
        function i(e, t) {
            if (!(e instanceof t)) throw new TypeError("Cannot call a class as a function");
        }
        Object.defineProperty(t, "__esModule", {
            value: !0
        });
        var a = function() {
            function e(e, t) {
                for (var n = 0; n < t.length; n++) {
                    var r = t[n];
                    r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), 
                    Object.defineProperty(e, r.key, r);
                }
            }
            return function(t, n, r) {
                return n && e(t.prototype, n), r && e(t, r), t;
            };
        }(), o = n(1), u = function() {
            function e(t) {
                i(this, e), (0, o.checkType)(t, "object", "console"), this.logger = t;
            }
            return a(e, [ {
                key: "canLog",
                value: function(e) {
                    return !!(e && console && (0, o.isType)(this.logger[e], "function"));
                }
            }, {
                key: "debug",
                value: function() {
                    if (this.canLog("debug")) {
                        for (var e = arguments.length, t = Array(e), n = 0; n < e; n++) t[n] = arguments[n];
                        return this.logger.debug.apply(console, t), !0;
                    }
                    return !1;
                }
            }, {
                key: "error",
                value: function() {
                    if (this.canLog("error")) {
                        for (var e = arguments.length, t = Array(e), n = 0; n < e; n++) t[n] = arguments[n];
                        return this.logger.error.apply(console, t), !0;
                    }
                    return !1;
                }
            }, {
                key: "log",
                value: function() {
                    if (this.canLog("log")) {
                        for (var e = arguments.length, t = Array(e), n = 0; n < e; n++) t[n] = arguments[n];
                        return this.logger.log.apply(console, t), !0;
                    }
                    return !1;
                }
            }, {
                key: "warn",
                value: function() {
                    if (this.canLog("warn")) {
                        for (var e = arguments.length, t = Array(e), n = 0; n < e; n++) t[n] = arguments[n];
                        return this.logger.warn.apply(console, t), !0;
                    }
                    return !1;
                }
            } ], [ {
                key: "getLogger",
                value: function() {
                    var t = void 0;
                    try {
                        t = r.console;
                    } catch (e) {
                        t = window.console;
                    }
                    return this.logger ? this.logger : this.logger = new e(t);
                }
            } ]), e;
        }();
        t.default = u, e.exports = t.default;
    }).call(t, function() {
        return this;
    }());
}, function(e, t, n) {
    "use strict";
    function r(e) {
        return e && e.__esModule ? e : {
            default: e
        };
    }
    function i(e, t) {
        if (!(e instanceof t)) throw new TypeError("Cannot call a class as a function");
    }
    Object.defineProperty(t, "__esModule", {
        value: !0
    });
    var a = function() {
        function e(e, t) {
            for (var n = 0; n < t.length; n++) {
                var r = t[n];
                r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), 
                Object.defineProperty(e, r.key, r);
            }
        }
        return function(t, n, r) {
            return n && e(t.prototype, n), r && e(t, r), t;
        };
    }(), o = n(1), u = n(6), s = r(u), l = function() {
        function e(t) {
            var n = this;
            i(this, e), (0, o.checkType)(t, "object", "modelDefinition"), (0, o.checkType)(t.modelProperties, "object", "modelProperties"), 
            Object.defineProperty(this, "modelDefinition", {
                enumerable: !1,
                configurable: !1,
                writable: !1,
                value: t
            }), Object.defineProperty(this, "dirty", {
                enumerable: !1,
                configurable: !1,
                writable: !0,
                value: !1
            }), Object.defineProperty(this, "dataValues", {
                enumerable: !1,
                configurable: !0,
                writable: !0,
                value: {}
            });
            var r = function(e) {
                return e && !!Object.keys(e).length;
            }, a = {}, s = t.attributeProperties;
            r(s) && (Object.defineProperty(this, "attributes", {
                enumerable: !1,
                value: a
            }), Object.keys(s).forEach(function(e) {
                Object.defineProperty(a, e, {
                    enumerable: !0,
                    get: function() {
                        if (Array.isArray(n.attributeValues)) return n.attributeValues.filter(function(t) {
                            return t.attribute.name === e;
                        }).reduce(function(e, t) {
                            return t.value;
                        }, void 0);
                    },
                    set: function(t) {
                        n.attributeValues || (n.attributeValues = []);
                        var r = n.attributeValues.filter(function(t) {
                            return t.attribute.name === e;
                        }).reduce(function(e, t) {
                            return t;
                        }, void 0);
                        if (r) {
                            if (r.value === t) return;
                            r.value = t;
                        } else n.attributeValues.push({
                            value: t,
                            attribute: {
                                id: s[e].id,
                                name: s[e].name
                            }
                        });
                        n.dirty = !0;
                    }
                });
            })), Object.defineProperties(this, t.modelProperties), this[u.DIRTY_PROPERTY_LIST] = new Set([]);
        }
        return a(e, null, [ {
            key: "create",
            value: function(t) {
                return new e(t);
            }
        } ]), e;
    }();
    l.prototype = s.default, t.default = l, e.exports = t.default;
}, function(e, t, n) {
    "use strict";
    function r(e) {
        return e && e.__esModule ? e : {
            default: e
        };
    }
    function i(e, t) {
        if (!(e instanceof t)) throw new TypeError("Cannot call a class as a function");
    }
    function a(e, t) {
        return Boolean(e.modelDefinition && e.modelDefinition.modelValidations && e.modelDefinition.modelValidations[t] && Object.prototype.hasOwnProperty.call(e.modelDefinition.modelValidations, t));
    }
    function o(e) {
        return e && 1 === e.response.importCount.imported && (0, c.isValidUid)(e.response.lastImported) && (this.dataValues.id = e.response.lastImported, 
        this.dataValues.href = [ this.modelDefinition.apiEndpoint, this.dataValues.id ].join("/")), 
        this.dirty = !1, this.getDirtyChildren().forEach(function(e) {
            e.dirty = !1;
        }), this[d].clear(), e;
    }
    Object.defineProperty(t, "__esModule", {
        value: !0
    });
    var u = function() {
        function e(e, t) {
            for (var n = 0; n < t.length; n++) {
                var r = t[n];
                r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), 
                Object.defineProperty(e, r.key, r);
            }
        }
        return function(t, n, r) {
            return n && e(t.prototype, n), r && e(t, r), t;
        };
    }(), s = n(12), l = r(s), c = n(1), f = l.default.getModelValidation(), d = Symbol("List to keep track of dirty properties");
    t.DIRTY_PROPERTY_LIST = d;
    var p = function() {
        function e() {
            i(this, e);
        }
        return u(e, [ {
            key: "create",
            value: function() {
                var e = this;
                return this.validate().then(function(t) {
                    return t.status ? e.modelDefinition.saveNew(e).then(o.bind(e)) : Promise.reject(t);
                });
            }
        }, {
            key: "save",
            value: function(e) {
                var t = this;
                return this.isDirty(e) ? this.validate().then(function(e) {
                    return e.status ? t.modelDefinition.save(t).then(o.bind(t)) : Promise.reject(e);
                }) : Promise.reject("No changes to be saved");
            }
        }, {
            key: "validate",
            value: function() {
                var e = this;
                return new Promise(function(t, n) {
                    function r(e, t) {
                        return t && e.indexOf(t) === -1 && e.push(t), e;
                    }
                    function i(e) {
                        return f.validateAgainstSchema(e);
                    }
                    var a = [];
                    i(e).then(function(e) {
                        a = a.concat(e);
                        var n = {
                            status: 0 === e.length,
                            fields: a.map(function(e) {
                                return e.property;
                            }).reduce(r, []),
                            messages: a
                        };
                        t(n);
                    }).catch(function(e) {
                        return n(e);
                    });
                });
            }
        }, {
            key: "delete",
            value: function() {
                return this.modelDefinition.delete(this);
            }
        }, {
            key: "isDirty",
            value: function() {
                var e = arguments.length <= 0 || void 0 === arguments[0] || arguments[0];
                return !!(this.dirty || e === !0 && this.hasDirtyChildren());
            }
        }, {
            key: "getDirtyPropertyNames",
            value: function() {
                return Array.from(this[d].values());
            }
        }, {
            key: "getCollectionChildren",
            value: function() {
                var e = this;
                return Object.keys(this).filter(function(t) {
                    return e[t] && a(e, t) && e.modelDefinition.modelValidations[t].owner && e[t].size >= 0;
                }).map(function(t) {
                    return e[t];
                });
            }
        }, {
            key: "getCollectionChildrenPropertyNames",
            value: function() {
                var e = this;
                return Object.keys(this).filter(function(t) {
                    return e.modelDefinition && e.modelDefinition.modelValidations && e.modelDefinition.modelValidations[t] && "COLLECTION" === e.modelDefinition.modelValidations[t].type;
                });
            }
        }, {
            key: "getDirtyChildren",
            value: function() {
                return this.getCollectionChildren().filter(function(e) {
                    return e && e.dirty === !0;
                });
            }
        }, {
            key: "hasDirtyChildren",
            value: function() {
                return this.getDirtyChildren().length > 0;
            }
        } ]), e;
    }();
    t.default = new p();
}, function(e, t) {
    "use strict";
    Object.defineProperty(t, "__esModule", {
        value: !0
    });
    var n = new Set([ "categoryOptionCombo" ]);
    t.noCreateAllowedFor = n, t.default = {
        baseUrl: "/api",
        i18n: {
            sources: new Set(),
            strings: new Set()
        }
    };
}, function(e, t, n) {
    "use strict";
    function r(e) {
        return e && e.__esModule ? e : {
            default: e
        };
    }
    Object.defineProperty(t, "__esModule", {
        value: !0
    });
    var i = n(4), a = r(i), o = void 0;
    try {
        o = window.jQuery;
    } catch (e) {
        a.default.getLogger().error("JQuery not found"), o = {};
    }
    t.default = o, e.exports = t.default;
}, function(e, t, n) {
    "use strict";
    function r(e) {
        return e && e.__esModule ? e : {
            default: e
        };
    }
    function i(e) {
        if (Array.isArray(e)) {
            for (var t = 0, n = Array(e.length); t < e.length; t++) n[t] = e[t];
            return n;
        }
        return Array.from(e);
    }
    function a(e, t) {
        if (!(e instanceof t)) throw new TypeError("Cannot call a class as a function");
    }
    function o(e) {
        if (e && e[Symbol.iterator]) {
            var t = [].concat(i(e));
            t.forEach(function(e) {
                e instanceof d.default || (0, c.throwError)("Values of a ModelCollection must be instances of Model");
            });
        }
    }
    function u(e) {
        if (e && e[Symbol.iterator]) {
            var t = [].concat(i(e));
            t.forEach(function(e) {
                (0, l.isValidUid)(e.id) || (0, c.throwError)("Can not add a Model without id to a ModelCollection");
            });
        }
    }
    Object.defineProperty(t, "__esModule", {
        value: !0
    });
    var s = function() {
        function e(e, t) {
            for (var n = 0; n < t.length; n++) {
                var r = t[n];
                r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), 
                Object.defineProperty(e, r.key, r);
            }
        }
        return function(t, n, r) {
            return n && e(t.prototype, n), r && e(t, r), t;
        };
    }(), l = n(1), c = n(3), f = n(5), d = r(f), p = n(10), h = r(p), y = n(33), v = r(y), g = function() {
        function e(t, n, r) {
            var i = this;
            a(this, e), (0, l.checkType)(t, h.default), this.modelDefinition = t, this.pager = new v.default(r, t), 
            this.valuesContainerMap = new Map(), this[Symbol.iterator] = this.valuesContainerMap[Symbol.iterator].bind(this.valuesContainerMap), 
            o(n), u(n), (0, l.isArray)(n) && n.forEach(function(e) {
                return i.valuesContainerMap.set(e.id, e);
            });
        }
        return s(e, [ {
            key: "add",
            value: function(e) {
                return o([ e ]), u([ e ]), this.set(e.id, e), this;
            }
        }, {
            key: "toArray",
            value: function() {
                var e = [];
                return this.forEach(function(t) {
                    e.push(t);
                }), e;
            }
        }, {
            key: "clear",
            value: function() {
                return this.valuesContainerMap.clear.apply(this.valuesContainerMap);
            }
        }, {
            key: "delete",
            value: function() {
                for (var e = arguments.length, t = Array(e), n = 0; n < e; n++) t[n] = arguments[n];
                return this.valuesContainerMap.delete.apply(this.valuesContainerMap, t);
            }
        }, {
            key: "entries",
            value: function() {
                return this.valuesContainerMap.entries.apply(this.valuesContainerMap);
            }
        }, {
            key: "forEach",
            value: function() {
                for (var e = arguments.length, t = Array(e), n = 0; n < e; n++) t[n] = arguments[n];
                return this.valuesContainerMap.forEach.apply(this.valuesContainerMap, t);
            }
        }, {
            key: "get",
            value: function() {
                for (var e = arguments.length, t = Array(e), n = 0; n < e; n++) t[n] = arguments[n];
                return this.valuesContainerMap.get.apply(this.valuesContainerMap, t);
            }
        }, {
            key: "has",
            value: function() {
                for (var e = arguments.length, t = Array(e), n = 0; n < e; n++) t[n] = arguments[n];
                return this.valuesContainerMap.has.apply(this.valuesContainerMap, t);
            }
        }, {
            key: "keys",
            value: function() {
                return this.valuesContainerMap.keys.apply(this.valuesContainerMap);
            }
        }, {
            key: "set",
            value: function() {
                for (var e = arguments.length, t = Array(e), n = 0; n < e; n++) t[n] = arguments[n];
                return this.valuesContainerMap.set.apply(this.valuesContainerMap, t);
            }
        }, {
            key: "values",
            value: function() {
                return this.valuesContainerMap.values.apply(this.valuesContainerMap);
            }
        }, {
            key: "size",
            get: function() {
                return this.valuesContainerMap.size;
            }
        } ], [ {
            key: "create",
            value: function(t, n, r) {
                return new e(t, n, r);
            }
        }, {
            key: "throwIfContainsOtherThanModelObjects",
            value: function(e) {
                return o(e);
            }
        }, {
            key: "throwIfContainsModelWithoutUid",
            value: function(e) {
                return u(e);
            }
        } ]), e;
    }();
    t.default = g, e.exports = t.default;
}, function(e, t, n) {
    "use strict";
    function r(e) {
        return e && e.__esModule ? e : {
            default: e
        };
    }
    function i(e, t) {
        if ("function" != typeof t && null !== t) throw new TypeError("Super expression must either be null or a function, not " + typeof t);
        e.prototype = Object.create(t && t.prototype, {
            constructor: {
                value: e,
                enumerable: !1,
                writable: !0,
                configurable: !0
            }
        }), t && (Object.setPrototypeOf ? Object.setPrototypeOf(e, t) : e.__proto__ = t);
    }
    function a(e, t) {
        if (!(e instanceof t)) throw new TypeError("Cannot call a class as a function");
    }
    function o(e, t) {
        var n = t.collection ? t.collectionName : t.name, r = {
            configurable: !1,
            enumerable: !0,
            get: function() {
                return this.dataValues[n];
            }
        };
        t.writable && (r.set = function(e) {
            (!(0, y.isObject)(e) && e !== this.dataValues[n] || (0, y.isObject)(e)) && (this.dirty = !0, 
            this[A.DIRTY_PROPERTY_LIST].add(n), this.dataValues[n] = e);
        }), n && (e[n] = r);
    }
    function u(e) {
        var t = {}, n = (0, v.curry)(o, t);
        return (e || []).forEach(n), t;
    }
    function s(e, t) {
        function n(e) {
            if (e.href) return e.href.split("/").pop();
        }
        var r = t.collection ? t.collectionName : t.name, i = {
            persisted: t.persisted,
            type: P.default.typeLookup(t.propertyType),
            required: t.required,
            min: t.min,
            max: t.max,
            owner: t.owner,
            unique: t.unique,
            writable: t.writable,
            constants: t.constants,
            ordered: Boolean(t.ordered)
        };
        ("REFERENCE" === i.type || "COLLECTION" === i.type && "REFERENCE" === t.itemPropertyType) && (i.referenceType = n(t)), 
        r && (e[r] = i);
    }
    function l(e) {
        var t = {}, n = (0, v.curry)(s, t);
        return (e || []).forEach(n), t;
    }
    function c(e, t) {
        return function(n) {
            return e && t && e.modelDefinition && e.modelDefinition.modelValidations && e.modelDefinition.modelValidations[n] && "COLLECTION" === e.modelDefinition.modelValidations[n].type && t.hasOwnProperty(e.modelDefinition.modelValidations[n].referenceType);
        };
    }
    function f(e) {
        var t = {}, n = this.getOwnedPropertyNames(), r = e.getCollectionChildrenPropertyNames().filter(function(e) {
            return "attributeValues" !== e;
        });
        return Object.keys(this.modelValidations).forEach(function(i) {
            if (n.indexOf(i) >= 0 && void 0 !== e.dataValues[i] && null !== e.dataValues[i]) if (r.indexOf(i) === -1) t[i] = e.dataValues[i]; else {
                if ("compulsoryDataElementOperands" === i) return void (t[i] = e.dataValues[i]);
                t[i] = Array.from(e.dataValues[i].values()).filter(function(e) {
                    return e.id;
                }).map(function(t) {
                    return e.modelDefinition && "legendSet" === e.modelDefinition.name ? f.call(t.modelDefinition, t) : {
                        id: t.id
                    };
                });
            }
        }), t;
    }
    function d(e) {
        return Boolean(e.id);
    }
    Object.defineProperty(t, "__esModule", {
        value: !0
    });
    var p = function(e, t, n) {
        for (var r = !0; r; ) {
            var i = e, a = t, o = n;
            r = !1, null === i && (i = Function.prototype);
            var u = Object.getOwnPropertyDescriptor(i, a);
            if (void 0 !== u) {
                if ("value" in u) return u.value;
                var s = u.get;
                if (void 0 === s) return;
                return s.call(o);
            }
            var l = Object.getPrototypeOf(i);
            if (null === l) return;
            e = l, t = a, n = o, r = !0, u = l = void 0;
        }
    }, h = function() {
        function e(e, t) {
            for (var n = 0; n < t.length; n++) {
                var r = t[n];
                r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), 
                Object.defineProperty(e, r.key, r);
            }
        }
        return function(t, n, r) {
            return n && e(t.prototype, n), r && e(t, r), t;
        };
    }(), y = n(1), v = n(3), g = n(11), m = r(g), _ = n(5), b = r(_), w = n(9), T = r(w), j = n(24), k = r(j), O = n(21), P = r(O), E = n(23), C = r(E), A = n(6), M = n(25), D = function() {
        function e(t, n, r, i, o, u, s) {
            a(this, e), (0, y.checkType)(t, "string"), (0, y.checkType)(n, "string", "Plural"), 
            (0, v.addLockedProperty)(this, "name", t), (0, v.addLockedProperty)(this, "plural", n), 
            (0, v.addLockedProperty)(this, "isSharable", r && r.shareable || !1), (0, v.addLockedProperty)(this, "isMetaData", r && r.metadata || !1), 
            (0, v.addLockedProperty)(this, "apiEndpoint", r && r.apiEndpoint), (0, v.addLockedProperty)(this, "javaClass", r && r.klass), 
            (0, v.addLockedProperty)(this, "identifiableObject", r && r.identifiableObject), 
            (0, v.addLockedProperty)(this, "modelProperties", i), (0, v.addLockedProperty)(this, "modelValidations", o), 
            (0, v.addLockedProperty)(this, "attributeProperties", u), (0, v.addLockedProperty)(this, "authorities", s), 
            this.filters = C.default.getFilters(this), this.getOwnedPropertyJSON = f.bind(this);
        }
        return h(e, [ {
            key: "filter",
            value: function() {
                return this.clone().filters;
            }
        }, {
            key: "create",
            value: function(e) {
                var t = b.default.create(this), n = t.modelDefinition.modelValidations, r = m.default.getModelDefinitions(), i = Object.assign({}, e);
                return e ? Object.keys(t).forEach(function(a) {
                    var o = n[a].hasOwnProperty("referenceType") && n[a].referenceType || r.hasOwnProperty(a) && a;
                    o && r.hasOwnProperty(o) && Array.isArray(e[a]) && ("translations" === a || "greyedFields" === a ? i[a] = e[a] : i[a] = k.default.create(t, r[o], e[a].map(function(e) {
                        return r[o].create(e);
                    }))), t.dataValues[a] = i[a];
                }) : !function() {
                    Object.keys(t).filter(c(t, r)).forEach(function(e) {
                        var n = t.modelDefinition.modelValidations[e].referenceType;
                        t.dataValues[e] = k.default.create(t, r[n], []);
                    });
                    var e = (0, M.getDefaultValuesForModelType)(t.modelDefinition.name), n = c(t, r);
                    Object.keys(t).filter(function(e) {
                        return !n(e);
                    }).forEach(function(n) {
                        t.dataValues[n] = e[n];
                    });
                }(), t;
            }
        }, {
            key: "clone",
            value: function() {
                var e = Object.getPrototypeOf(this), t = this.filters.filters, n = Object.create(e);
                return n = (0, v.copyOwnProperties)(n, this), n.filters = C.default.getFilters(n), 
                n.filters.filters = t.map(function(e) {
                    return e;
                }), n;
            }
        }, {
            key: "get",
            value: function(e) {
                var t = this, n = arguments.length <= 1 || void 0 === arguments[1] ? {
                    fields: ":all,attributeValues[:all,attribute[id,name,displayName]]"
                } : arguments[1];
                return (0, y.checkDefined)(e, "Identifier"), Array.isArray(e) ? this.list({
                    filter: [ "id:in:[" + e.join(",") + "]" ]
                }) : this.api.get([ this.apiEndpoint, e ].join("/"), n).then(function(e) {
                    return t.create(e);
                }).catch(function(e) {
                    return e.message ? Promise.reject(e.message) : Promise.reject(e);
                });
            }
        }, {
            key: "list",
            value: function() {
                var e = this, t = arguments.length <= 0 || void 0 === arguments[0] ? {} : arguments[0], n = this.filters.getFilters();
                return !(0, y.isDefined)(t.filter) && n.length && (t.filter = n), this.api.get(this.apiEndpoint, Object.assign({
                    fields: ":all"
                }, t)).then(function(t) {
                    return T.default.create(e, t[e.plural].map(function(t) {
                        return e.create(t);
                    }), t.pager);
                });
            }
        }, {
            key: "save",
            value: function(e) {
                if (d(e)) {
                    var t = e.dataValues.href, n = f.bind(this)(e);
                    return this.api.update(t, n, !0);
                }
                return this.saveNew(e);
            }
        }, {
            key: "saveNew",
            value: function(e) {
                var t = f.bind(this)(e);
                return this.api.post(this.apiEndpoint, t);
            }
        }, {
            key: "getOwnedPropertyNames",
            value: function() {
                var e = this;
                return Object.keys(this.modelValidations).filter(function(t) {
                    return e.modelValidations[t].owner;
                });
            }
        }, {
            key: "delete",
            value: function(e) {
                return e.dataValues.href ? this.api.delete(e.dataValues.href) : this.api.delete([ e.modelDefinition.apiEndpoint, e.dataValues.id ].join("/"));
            }
        } ], [ {
            key: "createFromSchema",
            value: function(t) {
                var n = arguments.length <= 1 || void 0 === arguments[1] ? [] : arguments[1], r = void 0;
                return (0, y.checkType)(t, Object, "Schema"), r = "function" == typeof e.specialClasses[t.name] ? e.specialClasses[t.name] : e, 
                Object.freeze(new r(t.name, t.plural, t, Object.freeze(u(t.properties)), Object.freeze(l(t.properties)), n.reduce(function(e, t) {
                    return e[t.name] = t, e;
                }, {}), t.authorities));
            }
        } ]), e;
    }(), S = function(e) {
        function t() {
            a(this, t), p(Object.getPrototypeOf(t.prototype), "constructor", this).apply(this, arguments);
        }
        return i(t, e), h(t, [ {
            key: "get",
            value: function(e) {
                var n = arguments.length <= 1 || void 0 === arguments[1] ? {
                    fields: ":all,userCredentials[:owner]"
                } : arguments[1];
                return p(Object.getPrototypeOf(t.prototype), "get", this).call(this, e, n);
            }
        } ]), t;
    }(D), x = function(e) {
        function t() {
            a(this, t), p(Object.getPrototypeOf(t.prototype), "constructor", this).apply(this, arguments);
        }
        return i(t, e), h(t, [ {
            key: "create",
            value: function() {
                var e = arguments.length <= 0 || void 0 === arguments[0] ? {} : arguments[0], n = Boolean(Object.keys(e).length), r = Object.keys(e).filter(function(e) {
                    return "compulsoryDataElementOperands" !== e;
                }).reduce(function(t, n) {
                    return t[n] = e[n], t;
                }, {}), i = p(Object.getPrototypeOf(t.prototype), "create", this).call(this, n ? r : void 0);
                return i.dataValues.compulsoryDataElementOperands = e.compulsoryDataElementOperands, 
                i;
            }
        } ]), t;
    }(D);
    D.specialClasses = {
        user: S,
        dataSet: x
    }, t.default = D, e.exports = t.default;
}, function(e, t, n) {
    "use strict";
    function r(e, t) {
        if (!(e instanceof t)) throw new TypeError("Cannot call a class as a function");
    }
    function i() {
        return i.modelDefinitions ? i.modelDefinitions : i.modelDefinitions = new u();
    }
    Object.defineProperty(t, "__esModule", {
        value: !0
    });
    var a = function() {
        function e(e, t) {
            for (var n = 0; n < t.length; n++) {
                var r = t[n];
                r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), 
                Object.defineProperty(e, r.key, r);
            }
        }
        return function(t, n, r) {
            return n && e(t.prototype, n), r && e(t, r), t;
        };
    }(), o = n(1), u = function() {
        function e() {
            r(this, e);
        }
        return a(e, [ {
            key: "add",
            value: function(e) {
                try {
                    (0, o.checkType)(e.name, "string");
                } catch (e) {
                    throw new Error("Name should be set on the passed ModelDefinition to add one");
                }
                if (this[e.name]) throw new Error([ "Model", e.name, "already exists" ].join(" "));
                this[e.name] = e, (0, o.isType)(e.plural, "string") && (this[e.plural] = e);
            }
        }, {
            key: "mapThroughDefinitions",
            value: function(e) {
                var t = [], n = void 0;
                (0, o.checkType)(e, "function", "transformer");
                for (n in this) this.hasOwnProperty(n) && this[n].plural !== n && t.push(e(this[n]));
                return t;
            }
        } ]), e;
    }();
    u.getModelDefinitions = i, t.default = u, e.exports = t.default;
}, function(e, t, n) {
    "use strict";
    function r(e) {
        return e && e.__esModule ? e : {
            default: e
        };
    }
    function i(e, t) {
        if (!(e instanceof t)) throw new TypeError("Cannot call a class as a function");
    }
    Object.defineProperty(t, "__esModule", {
        value: !0
    });
    var a = function() {
        function e(e, t) {
            for (var n = 0; n < t.length; n++) {
                var r = t[n];
                r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), 
                Object.defineProperty(e, r.key, r);
            }
        }
        return function(t, n, r) {
            return n && e(t.prototype, n), r && e(t, r), t;
        };
    }(), o = n(1), u = n(4), s = r(u), l = n(2), c = r(l), f = function() {
        function e(t) {
            i(this, e), (0, o.checkType)(t, "object", "logger (Logger)"), this.logger = t;
        }
        return a(e, [ {
            key: "validate",
            value: function() {
                throw this.logger.warn("Client side model validation is deprecated"), new Error("Client side model validation is deprecated");
            }
        }, {
            key: "validateAgainstSchema",
            value: function(e) {
                function t(e) {
                    if (e.response && (e.response.validationViolations || e.response.errorReports)) return e.response.validationViolations || e.response.errorReports;
                    throw new Error("Response was not a WebMessage with the expected format");
                }
                if (!(e && e.modelDefinition && e.modelDefinition.name)) return Promise.reject("model.modelDefinition.name can not be found");
                var n = "schemas/" + e.modelDefinition.name;
                return c.default.getApi().post(n, e.modelDefinition.getOwnedPropertyJSON(e)).then(function(e) {
                    return "OK" === e.status ? [] : Promise.reject(e);
                }).catch(t);
            }
        } ], [ {
            key: "getModelValidation",
            value: function() {
                return this.modelValidation ? this.modelValidation : this.modelValidation = new e(s.default.getLogger(console));
            }
        } ]), e;
    }();
    t.default = f, e.exports = t.default;
}, function(e, t, n) {
    "use strict";
    function r(e) {
        return e && e.__esModule ? e : {
            default: e
        };
    }
    function i(e, t) {
        if (!(e instanceof t)) throw new TypeError("Cannot call a class as a function");
    }
    Object.defineProperty(t, "__esModule", {
        value: !0
    });
    var a = function() {
        function e(e, t) {
            for (var n = 0; n < t.length; n++) {
                var r = t[n];
                r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), 
                Object.defineProperty(e, r.key, r);
            }
        }
        return function(t, n, r) {
            return n && e(t.prototype, n), r && e(t, r), t;
        };
    }(), o = n(2), u = r(o), s = n(35), l = r(s), c = n(34), f = r(c), d = function() {
        function e(t, n) {
            i(this, e), this.settings = t, this.configuration = n, this.systemInfo = void 0, 
            this.version = void 0, this.installedApps = void 0;
        }
        return a(e, [ {
            key: "setSystemInfo",
            value: function(t) {
                this.version = e.parseVersionString(t.version), this.systemInfo = t;
            }
        }, {
            key: "setInstalledApps",
            value: function(e) {
                this.installedApps = e;
            }
        }, {
            key: "loadInstalledApps",
            value: function() {
                var e = this, t = u.default.getApi();
                return t.get("apps").then(function(t) {
                    return e.setInstalledApps(t), t;
                });
            }
        }, {
            key: "uploadApp",
            value: function(e, t) {
                var n = u.default.getApi(), r = new FormData(), i = void 0;
                return r.append("file", e), void 0 !== t && (i = new XMLHttpRequest(), i.upload.onprogress = function(e) {
                    e.lengthComputable && t(e.loaded / e.total);
                }), n.post("apps", r, {
                    contentType: !1,
                    processData: !1,
                    xhr: void 0 !== i ? function() {
                        return i;
                    } : void 0
                });
            }
        }, {
            key: "loadAppStore",
            value: function() {
                var t = this, n = arguments.length <= 0 || void 0 === arguments[0] || arguments[0];
                return new Promise(function(r, i) {
                    var a = u.default.getApi();
                    a.get("appStore").then(function(i) {
                        var a = Object.assign({}, i);
                        a.apps = a.apps.map(function(r) {
                            var i = Object.assign({}, r);
                            return n && (i.versions = i.versions.filter(function(n) {
                                return e.isVersionCompatible(t.version, n);
                            })), i;
                        }).filter(function(e) {
                            return e.versions.length > 0;
                        }), r(a);
                    }).catch(function(e) {
                        i(e);
                    });
                });
            }
        }, {
            key: "installAppVersion",
            value: function(e) {
                var t = u.default.getApi();
                return new Promise(function(n, r) {
                    t.post([ "appStore", e ].join("/"), "", {
                        dataType: "text"
                    }).then(function() {
                        n();
                    }).catch(function(e) {
                        r(e);
                    });
                });
            }
        }, {
            key: "uninstallApp",
            value: function(e) {
                var t = u.default.getApi();
                return t.delete([ "apps", e ].join("/")).catch(function() {});
            }
        }, {
            key: "reloadApps",
            value: function() {
                var e = this, t = u.default.getApi();
                return t.update("apps").then(function() {
                    return e.loadInstalledApps();
                });
            }
        } ], [ {
            key: "parseVersionString",
            value: function(e) {
                return {
                    major: Number.parseInt(e, 10),
                    minor: Number.parseInt(e.substring(e.indexOf(".") + 1), 10),
                    snapshot: e.indexOf("-SNAPSHOT") >= 0
                };
            }
        }, {
            key: "compareVersions",
            value: function(t, n) {
                var r = "string" == typeof t || t instanceof String ? e.parseVersionString(t) : t, i = "string" == typeof n || n instanceof String ? e.parseVersionString(n) : n;
                return r.major !== i.major ? r.major - i.major : r.minor !== i.minor ? r.minor - i.minor : (r.snapshot ? 0 : 1) - (i.snapshot ? 0 : 1);
            }
        }, {
            key: "isVersionCompatible",
            value: function(t, n) {
                var r = !n.min_platform_version || e.compareVersions(t, n.min_platform_version) >= 0, i = !n.max_platform_version || e.compareVersions(t, n.max_platform_version) <= 0;
                return r && i;
            }
        }, {
            key: "getSystem",
            value: function() {
                return e.getSystem.system || (e.getSystem.system = new e(new l.default(), new f.default())), 
                e.getSystem.system;
            }
        } ]), e;
    }();
    t.default = d, e.exports = t.default;
}, function(e, t) {
    "use strict";
    Object.defineProperty(t, "__esModule", {
        value: !0
    }), t.default = {
        translate: {},
        menu: {},
        settings: {}
    };
}, function(e, t, n) {
    "use strict";
    function r(e) {
        return e && e.__esModule ? e : {
            default: e
        };
    }
    function i(e, t) {
        if (!(e instanceof t)) throw new TypeError("Cannot call a class as a function");
    }
    Object.defineProperty(t, "__esModule", {
        value: !0
    });
    var a = function() {
        function e(e, t) {
            for (var n = 0; n < t.length; n++) {
                var r = t[n];
                r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), 
                Object.defineProperty(e, r.key, r);
            }
        }
        return function(t, n, r) {
            return n && e(t.prototype, n), r && e(t, r), t;
        };
    }(), o = n(7), u = r(o), s = n(1), l = function() {
        function e() {
            i(this, e);
        }
        return a(e, null, [ {
            key: "create",
            value: function() {
                for (var e = arguments.length, t = Array(e), n = 0; n < e; n++) t[n] = arguments[n];
                var r = t.filter(function(e) {
                    return e;
                });
                if (!r.every(function(e) {
                    return (0, s.isType)(e, "object");
                })) throw new Error("Expected Config parameter to have type object");
                return Object.assign.apply(Object, [ {}, u.default ].concat(t));
            }
        }, {
            key: "processConfigForD2",
            value: function(e, t) {
                var n = t.Api.getApi();
                t.model.ModelDefinition.prototype.api = n, t.models = t.model.ModelDefinitions.getModelDefinitions(), 
                (0, s.isString)(e.baseUrl) ? n.setBaseUrl(e.baseUrl) : n.setBaseUrl("/api"), e.i18n && e.i18n.sources && Array.from(e.i18n.sources).forEach(function(e) {
                    return t.i18n.addSource(e);
                }), e.i18n && e.i18n.strings && t.i18n.addStrings(Array.from(e.i18n.strings));
            }
        } ]), e;
    }();
    t.default = l, e.exports = t.default;
}, function(e, t, n) {
    "use strict";
    function r(e) {
        return e && e.__esModule ? e : {
            default: e
        };
    }
    function i(e, t) {
        if (!(e instanceof t)) throw new TypeError("Cannot call a class as a function");
    }
    function a(e) {
        var t = void 0;
        return t = e.userCredentials ? Object.assign({}, e.userCredentials, e) : Object.assign({}, e), 
        Object.keys(t).reduce(function(e, n) {
            return h.has(n) ? t[n].map && (e[v[n]] = t[n].map(function(e) {
                return e.id;
            })) : e[n] = t[n], e;
        }, {});
    }
    function o(e) {
        return e && d.noCreateAllowedFor.has(e.name);
    }
    Object.defineProperty(t, "__esModule", {
        value: !0
    });
    var u = function() {
        function e(e, t) {
            for (var n = 0; n < t.length; n++) {
                var r = t[n];
                r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), 
                Object.defineProperty(e, r.key, r);
            }
        }
        return function(t, n, r) {
            return n && e(t.prototype, n), r && e(t, r), t;
        };
    }(), s = n(17), l = r(s), c = n(18), f = r(c), d = n(7), p = Symbol("models"), h = new Set([ "userCredentials", "userGroups", "userRoles", "organisationUnits", "dataViewOrganisationUnits" ]), y = {
        READ: [ "READ" ],
        CREATE: [ "CREATE", "CREATE_PUBLIC", "CREATE_PRIVATE" ],
        CREATE_PUBLIC: [ "CREATE_PUBLIC" ],
        CREATE_PRIVATE: [ "CREATE_PRIVATE" ],
        DELETE: [ "DELETE" ],
        UPDATE: [ "UPDATE" ],
        EXTERNALIZE: [ "EXTERNALIZE" ]
    }, v = Array.from(h).reduce(function(e, t) {
        return e[t] = Symbol(t), e;
    }, {}), g = function() {
        function e(t, n, r, o) {
            i(this, e), Object.assign(this, a(t)), this.authorities = n, this[p] = r, this.userSettings = o;
        }
        return u(e, [ {
            key: "getUserGroups",
            value: function() {
                var e = this[v.userGroups];
                return this[p].userGroup.get({
                    filter: [ "id:in:[" + e.join(",") + "]" ]
                });
            }
        }, {
            key: "getUserRoles",
            value: function() {
                var e = this[v.userRoles];
                return this[p].userRole.get({
                    filter: [ "id:in:[" + e.join(",") + "]" ]
                });
            }
        }, {
            key: "getOrganisationUnits",
            value: function() {
                var e = arguments.length <= 0 || void 0 === arguments[0] ? {
                    fields: ":all,displayName,children[id,displayName,path,children::isNotEmpty]"
                } : arguments[0], t = this[v.organisationUnits];
                return this[p].organisationUnit.list(Object.assign({}, e, {
                    filter: [ "id:in:[" + t.join(",") + "]" ]
                }));
            }
        }, {
            key: "getDataViewOrganisationUnits",
            value: function() {
                var e = arguments.length <= 0 || void 0 === arguments[0] ? {
                    fields: ":all,displayName,children[id,displayName,path,children::isNotEmpty]"
                } : arguments[0], t = this[v.dataViewOrganisationUnits];
                return this[p].organisationUnit.list(Object.assign({}, e, {
                    filter: [ "id:in:[" + t.join(",") + "]" ]
                }));
            }
        }, {
            key: "checkAuthorityForType",
            value: function(e, t) {
                var n = this;
                return !(!t || !Array.isArray(t.authorities)) && t.authorities.filter(function(t) {
                    return e.some(function(e) {
                        return e === t.type;
                    });
                }).some(function(e) {
                    return e.authorities.some(function(e) {
                        return n.authorities.has(e);
                    });
                });
            }
        }, {
            key: "checkCreateAuthorityForType",
            value: function(e, t) {
                return !o(t) && this.checkAuthorityForType(e, t);
            }
        }, {
            key: "canCreate",
            value: function(e) {
                return this.checkCreateAuthorityForType(y.CREATE, e);
            }
        }, {
            key: "canCreatePublic",
            value: function(e) {
                return this.checkCreateAuthorityForType(y.CREATE_PUBLIC, e);
            }
        }, {
            key: "canCreatePrivate",
            value: function(e) {
                return this.checkCreateAuthorityForType(y.CREATE_PRIVATE, e);
            }
        }, {
            key: "canDelete",
            value: function(e) {
                return this.checkAuthorityForType(y.DELETE, e);
            }
        }, {
            key: "canUpdate",
            value: function(e) {
                return !!this.checkAuthorityForType(y.UPDATE, e) || this.checkAuthorityForType(y.CREATE, e);
            }
        }, {
            key: "uiLocale",
            get: function() {
                return this.userSettings && this.userSettings.keyUiLocale ? this.userSettings.keyUiLocale : "en";
            }
        } ], [ {
            key: "create",
            value: function(t, n, r, i) {
                return new e(t, l.default.create(n), r, new f.default(i));
            }
        } ]), e;
    }();
    t.default = g, e.exports = t.default;
}, function(e, t) {
    "use strict";
    function n(e, t) {
        if (!(e instanceof t)) throw new TypeError("Cannot call a class as a function");
    }
    Object.defineProperty(t, "__esModule", {
        value: !0
    });
    var r = function() {
        function e(e, t) {
            for (var n = 0; n < t.length; n++) {
                var r = t[n];
                r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), 
                Object.defineProperty(e, r.key, r);
            }
        }
        return function(t, n, r) {
            return n && e(t.prototype, n), r && e(t, r), t;
        };
    }(), i = Symbol(), a = function() {
        function e() {
            var t = arguments.length <= 0 || void 0 === arguments[0] ? [] : arguments[0];
            n(this, e), this[i] = new Set(t);
        }
        return r(e, [ {
            key: "has",
            value: function(e) {
                return !!this[i].has("ALL") || this[i].has(e);
            }
        } ], [ {
            key: "create",
            value: function(t) {
                return new e(t);
            }
        } ]), e;
    }();
    t.default = a, e.exports = t.default;
}, function(e, t, n) {
    "use strict";
    function r(e) {
        return e && e.__esModule ? e : {
            default: e
        };
    }
    function i(e, t) {
        if (!(e instanceof t)) throw new TypeError("Cannot call a class as a function");
    }
    Object.defineProperty(t, "__esModule", {
        value: !0
    });
    var a = function() {
        function e(e, t) {
            for (var n = 0; n < t.length; n++) {
                var r = t[n];
                r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), 
                Object.defineProperty(e, r.key, r);
            }
        }
        return function(t, n, r) {
            return n && e(t.prototype, n), r && e(t, r), t;
        };
    }(), o = n(2), u = r(o), s = n(1), l = function() {
        function e(t) {
            var n = arguments.length <= 1 || void 0 === arguments[1] ? u.default.getApi() : arguments[1];
            i(this, e), this.api = n, t && Object.assign(this, t);
        }
        return a(e, [ {
            key: "all",
            value: function() {
                return this.api.get("userSettings");
            }
        }, {
            key: "get",
            value: function(e) {
                function t(e) {
                    try {
                        return JSON.parse(e);
                    } catch (t) {
                        return e;
                    }
                }
                var n = this;
                return new Promise(function(r, i) {
                    if (!(0, s.isString)(e)) throw new TypeError('A "key" parameter should be specified when calling get() on userSettings');
                    n.api.get([ "userSettings", e ].join("/"), void 0, {
                        dataType: "text"
                    }).then(function(a) {
                        var o = t(a);
                        n[e] = o, o && r(t(a)), i(new Error("The requested userSetting has no value or does not exist."));
                    });
                });
            }
        }, {
            key: "set",
            value: function(e, t) {
                var n = [ "userSettings", e ].join("/");
                return null === t || 0 === ("" + t).length ? this.api.delete(n, {
                    dataType: "text"
                }).then(this[e] = void 0) : this.api.post(n, t, {
                    dataType: "text",
                    contentType: "text/plain"
                }).then(this[e] = t);
            }
        } ]), e;
    }();
    t.default = l, e.exports = t.default;
}, function(e, t, n) {
    "use strict";
    function r(e) {
        return e && e.__esModule ? e : {
            default: e
        };
    }
    function i(e) {
        var t = new h.default(k.default);
        t.setBaseUrl("");
        var n = {
            getBaseUrl: function() {
                return this.activities.dhis.href;
            }
        };
        return t.get("" + e).then(function(e) {
            return Object.assign({}, e, n);
        });
    }
    function a() {
        var e = h.default.getApi();
        return E.baseUrl && O && e.setBaseUrl(E.baseUrl), e.get("userSettings");
    }
    function o(e) {
        var t = h.default.getApi(), n = c.default.getLogger(), r = b.default.create(E, e), i = {
            models: void 0,
            model: d.default,
            Api: h.default,
            system: v.default.getSystem(),
            i18n: m.default.getI18n()
        };
        return b.default.processConfigForD2(r, i), O ? O = !1 : P = s.Deferred.create(), 
        Promise.all([ t.get("schemas"), t.get("attributes", {
            fields: ":all,optionSet[:all,options[:all]]",
            paging: !1
        }), t.get("me", {
            fields: ":all,organisationUnits[id],userGroups[id],userCredentials[:all,!user,userRoles[id]"
        }), t.get("me/authorization"), a(), t.get("system/info"), t.get("apps"), i.i18n.load() ]).then(function(e) {
            var t = {
                schemas: (0, s.pick)("schemas")(e[0]),
                attributes: (0, s.pick)("attributes")(e[1]),
                currentUser: e[2],
                authorities: e[3],
                userSettings: e[4],
                systemInfo: e[5],
                apps: e[6]
            };
            return t.schemas.map(function(e) {
                var t = r.baseUrl.match(/api\/(2[3-9])/);
                if (t && t[1] && e.apiEndpoint) {
                    var n = t[1];
                    e.apiEndpoint = e.apiEndpoint.replace(/api/, "api/" + n);
                }
                return e;
            }).forEach(function(e) {
                var n = t.attributes.filter(function(t) {
                    var n = [ e.name, "Attribute" ].join("");
                    return t[n] === !0;
                });
                Object.prototype.hasOwnProperty.call(i.models, e.name) || i.models.add(d.default.ModelDefinition.createFromSchema(e, n));
            }), i.currentUser = T.default.create(t.currentUser, t.authorities, i.models, t.userSettings), 
            i.system.setSystemInfo(t.systemInfo), i.system.setInstalledApps(t.apps), P.resolve(i), 
            P.promise;
        }).catch(function(e) {
            return n.error("Unable to get schemas from the api", JSON.stringify(e), e), P.reject("Unable to get schemas from the DHIS2 API"), 
            P.promise;
        });
    }
    function u() {
        return P.promise;
    }
    Object.defineProperty(t, "__esModule", {
        value: !0
    }), t.getManifest = i, t.getUserSettings = a, t.init = o, t.getInstance = u;
    var s = n(3), l = n(4), c = r(l), f = n(32), d = r(f), p = n(2), h = r(p), y = n(13), v = r(y), g = n(20), m = r(g), _ = n(15), b = r(_), w = n(16), T = r(w), j = n(8), k = r(j), O = !0, P = s.Deferred.create(), E = b.default.create(), C = E;
    t.config = C, t.default = {
        init: o,
        config: C,
        getInstance: u,
        getUserSettings: a,
        getManifest: i
    };
}, function(e, t, n) {
    "use strict";
    function r(e) {
        return e && e.__esModule ? e : {
            default: e
        };
    }
    function i(e, t) {
        if (!(e instanceof t)) throw new TypeError("Cannot call a class as a function");
    }
    Object.defineProperty(t, "__esModule", {
        value: !0
    });
    var a = function() {
        function e(e, t) {
            var n = [], r = !0, i = !1, a = void 0;
            try {
                for (var o, u = e[Symbol.iterator](); !(r = (o = u.next()).done) && (n.push(o.value), 
                !t || n.length !== t); r = !0) ;
            } catch (e) {
                i = !0, a = e;
            } finally {
                try {
                    !r && u.return && u.return();
                } finally {
                    if (i) throw a;
                }
            }
            return n;
        }
        return function(t, n) {
            if (Array.isArray(t)) return t;
            if (Symbol.iterator in Object(t)) return e(t, n);
            throw new TypeError("Invalid attempt to destructure non-iterable instance");
        };
    }(), o = function() {
        function e(e, t) {
            for (var n = 0; n < t.length; n++) {
                var r = t[n];
                r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), 
                Object.defineProperty(e, r.key, r);
            }
        }
        return function(t, n, r) {
            return n && e(t.prototype, n), r && e(t, r), t;
        };
    }(), u = n(2), s = r(u), l = function() {
        function e() {
            var t = arguments.length <= 0 || void 0 === arguments[0] ? [] : arguments[0], n = arguments.length <= 1 || void 0 === arguments[1] ? s.default.getApi() : arguments[1];
            i(this, e), this.sources = t, this.api = n, this.strings = new Set(), this.translations = void 0;
        }
        return o(e, [ {
            key: "addSource",
            value: function(e) {
                this.sources.push(e);
            }
        }, {
            key: "addStrings",
            value: function(e) {
                var t = this;
                "string" == typeof e && e.trim().length > 0 ? this.strings.add(e.trim()) : Array.from(e).filter(function(e) {
                    return e && ("" + e).trim().length > 0;
                }).forEach(function(e) {
                    return t.strings.add(e);
                });
            }
        }, {
            key: "load",
            value: function() {
                function e(e) {
                    return e.split("\n").reduce(function(e, t) {
                        var n = t.split("=").map(function(e) {
                            return e.trim();
                        }), r = a(n, 2), i = r[0], o = r[1];
                        return void 0 === i || void 0 === o || e.hasOwnProperty(i) || (e[i] = o.replace(/\\u([0-9a-f]{4})/gi, function(e, t) {
                            return String.fromCharCode(parseInt(t, 16));
                        })), e;
                    }, {});
                }
                var t = this, n = this;
                n.translations = {};
                var r = [];
                return this.sources.forEach(function(t) {
                    r.push(n.api.request("GET", t, void 0, {
                        dataType: "text"
                    }).then(function(t) {
                        return Promise.resolve(e(t));
                    }, function() {
                        return Promise.resolve({});
                    }));
                }), Promise.all(r).then(function(e) {
                    return e.forEach(function(e) {
                        Object.keys(e).forEach(function(r) {
                            n.translations.hasOwnProperty(r) || (n.translations[r] = e[r]), t.strings.delete(r);
                        });
                    }), t.strings.size > 0 ? n.api.post("i18n", Array.from(n.strings)).then(function(e) {
                        return Object.keys(e).filter(function(t) {
                            return t !== e[t];
                        }).forEach(function(t) {
                            n.translations[t] = e[t], n.strings.delete(t);
                        }), Promise.resolve(n.translations);
                    }) : Promise.resolve(n.translations);
                });
            }
        }, {
            key: "getTranslation",
            value: function(e) {
                var t = arguments.length <= 1 || void 0 === arguments[1] ? {} : arguments[1];
                if (void 0 === this.translations) throw new Error("Tried to translate before loading translations!");
                var n = this.translations.hasOwnProperty(e) ? this.translations[e] : "** " + e + " **";
                return Object.keys(t).length ? n.replace(/\$\$(.+?)\$\$/gi, function(e, n) {
                    return t[n] || "";
                }) : n;
            }
        }, {
            key: "isTranslated",
            value: function(e) {
                if (void 0 === this.translations) throw new Error("Tried to translate before loading translations!");
                return this.translations.hasOwnProperty(e);
            }
        }, {
            key: "getUntranslatedStrings",
            value: function() {
                return this.translations ? Array.from(this.strings) : void 0;
            }
        } ], [ {
            key: "getI18n",
            value: function() {
                return new e();
            }
        } ]), e;
    }();
    t.default = l, e.exports = t.default;
}, function(e, t, n) {
    "use strict";
    function r(e, t) {
        if (!(e instanceof t)) throw new TypeError("Cannot call a class as a function");
    }
    Object.defineProperty(t, "__esModule", {
        value: !0
    });
    var i = function() {
        function e(e, t) {
            for (var n = 0; n < t.length; n++) {
                var r = t[n];
                r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), 
                Object.defineProperty(e, r.key, r);
            }
        }
        return function(t, n, r) {
            return n && e(t.prototype, n), r && e(t, r), t;
        };
    }(), a = n(3), o = n(1), u = function() {
        function e() {
            r(this, e);
        }
        return i(e, [ {
            key: "getTypes",
            value: function() {
                return [ "TEXT", "NUMBER", "INTEGER", "BOOLEAN", "EMAIL", "PASSWORD", "URL", "PHONENUMBER", "GEOLOCATION", "COLOR", "COMPLEX", "COLLECTION", "REFERENCE", "DATE", "COMPLEX", "IDENTIFIER", "CONSTANT" ];
            }
        }, {
            key: "typeLookup",
            value: function(e) {
                return this.getTypes().indexOf(e) >= 0 && (0, o.isString)(e) ? e : (0, a.throwError)([ 'Type from schema "', e, '" not found available type list.' ].join(""));
            }
        } ]), e;
    }();
    t.default = new u(), e.exports = t.default;
}, function(e, t, n) {
    "use strict";
    function r(e, t) {
        if (!(e instanceof t)) throw new TypeError("Cannot call a class as a function");
    }
    Object.defineProperty(t, "__esModule", {
        value: !0
    });
    var i = function() {
        function e(e, t) {
            for (var n = 0; n < t.length; n++) {
                var r = t[n];
                r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), 
                Object.defineProperty(e, r.key, r);
            }
        }
        return function(t, n, r) {
            return n && e(t.prototype, n), r && e(t, r), t;
        };
    }(), a = n(1), o = {
        equals: "eq",
        like: "like",
        ilike: "ilike",
        notEqual: "ne"
    }, u = function() {
        function e(t) {
            r(this, e), this.filters = t, this.propertyName = "name", this.comparator = "like", 
            this.filterValue = void 0;
        }
        return i(e, [ {
            key: "on",
            value: function(e) {
                return (0, a.checkDefined)(e, "Property name to filter on"), this.propertyName = e, 
                this;
            }
        }, {
            key: "getQueryParamFormat",
            value: function() {
                return [ this.propertyName, this.comparator, this.filterValue ].join(":");
            }
        } ], [ {
            key: "getFilter",
            value: function(t) {
                return new e(t);
            }
        } ]), e;
    }();
    Object.keys(o).forEach(function(e) {
        Object.defineProperty(u.prototype, e, {
            value: function(t) {
                return (0, a.checkDefined)(t, "filterValue"), this.comparator = o[e], this.filterValue = t, 
                this.filters.add(this), this.filters.getReturn();
            }
        });
    }), t.default = u, e.exports = t.default;
}, function(e, t, n) {
    "use strict";
    function r(e) {
        return e && e.__esModule ? e : {
            default: e
        };
    }
    function i(e, t) {
        if (!(e instanceof t)) throw new TypeError("Cannot call a class as a function");
    }
    Object.defineProperty(t, "__esModule", {
        value: !0
    });
    var a = function() {
        function e(e, t) {
            for (var n = 0; n < t.length; n++) {
                var r = t[n];
                r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), 
                Object.defineProperty(e, r.key, r);
            }
        }
        return function(t, n, r) {
            return n && e(t.prototype, n), r && e(t, r), t;
        };
    }(), o = n(1), u = n(22), s = r(u), l = function() {
        function e(t) {
            i(this, e), this.filters = [], this.modelDefinition = t;
        }
        return a(e, [ {
            key: "on",
            value: function(e) {
                return s.default.getFilter(this).on(e);
            }
        }, {
            key: "add",
            value: function(e) {
                if (!(0, o.isType)(e, s.default)) throw new TypeError("filter should be an instance of Filter");
                this.filters.push(e);
            }
        }, {
            key: "list",
            value: function() {
                return this.modelDefinition.list();
            }
        }, {
            key: "getFilters",
            value: function() {
                return this.filters.map(function(e) {
                    return e.getQueryParamFormat();
                });
            }
        }, {
            key: "getReturn",
            value: function() {
                return this.modelDefinition;
            }
        } ], [ {
            key: "getFilters",
            value: function(t) {
                return new e(t);
            }
        } ]), e;
    }();
    t.default = l, e.exports = t.default;
}, function(e, t, n) {
    "use strict";
    function r(e) {
        return e && e.__esModule ? e : {
            default: e
        };
    }
    function i(e, t) {
        if (!(e instanceof t)) throw new TypeError("Cannot call a class as a function");
    }
    function a(e, t) {
        if ("function" != typeof t && null !== t) throw new TypeError("Super expression must either be null or a function, not " + typeof t);
        e.prototype = Object.create(t && t.prototype, {
            constructor: {
                value: e,
                enumerable: !1,
                writable: !0,
                configurable: !0
            }
        }), t && (Object.setPrototypeOf ? Object.setPrototypeOf(e, t) : e.__proto__ = t);
    }
    Object.defineProperty(t, "__esModule", {
        value: !0
    });
    var o = function() {
        function e(e, t) {
            for (var n = 0; n < t.length; n++) {
                var r = t[n];
                r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), 
                Object.defineProperty(e, r.key, r);
            }
        }
        return function(t, n, r) {
            return n && e(t.prototype, n), r && e(t, r), t;
        };
    }(), u = function(e, t, n) {
        for (var r = !0; r; ) {
            var i = e, a = t, o = n;
            r = !1, null === i && (i = Function.prototype);
            var u = Object.getOwnPropertyDescriptor(i, a);
            if (void 0 !== u) {
                if ("value" in u) return u.value;
                var s = u.get;
                if (void 0 === s) return;
                return s.call(o);
            }
            var l = Object.getPrototypeOf(i);
            if (null === l) return;
            e = l, t = a, n = o, r = !0, u = l = void 0;
        }
    }, s = n(9), l = r(s), c = function(e) {
        function t(e, n, r) {
            i(this, t), u(Object.getPrototypeOf(t.prototype), "constructor", this).call(this, n, r, void 0), 
            this.dirty = !1, this.added = new Set(), this.removed = new Set(), this.parentModel = e;
        }
        return a(t, e), o(t, [ {
            key: "add",
            value: function(e) {
                return this.valuesContainerMap.has(e.id) ? this : (u(Object.getPrototypeOf(t.prototype), "add", this).call(this, e), 
                this.removed.has(e.id) ? this.removed.delete(e.id) : this.added.add(e.id), this.updateDirty(), 
                this);
            }
        }, {
            key: "remove",
            value: function(e) {
                return l.default.throwIfContainsOtherThanModelObjects([ e ]), l.default.throwIfContainsModelWithoutUid([ e ]), 
                this.delete(e.id) && (this.added.has(e.id) ? this.added.delete(e.id) : this.removed.add(e.id)), 
                this.updateDirty(), this;
            }
        }, {
            key: "updateDirty",
            value: function() {
                return this.dirty = this.added.size > 0 || this.removed.size > 0, this.dirty;
            }
        }, {
            key: "save",
            value: function() {
                var e = this;
                if (!this.dirty) return Promise.reject("Nothing to save!");
                var t = this.modelDefinition.api, n = [];
                return this.added.size && Array.from(this.added).forEach(function(r) {
                    n.push(t.post([ e.parentModel.href, e.modelDefinition.plural, r ].join("/")));
                }), this.removed.size && Array.from(this.removed).forEach(function(r) {
                    n.push(t.delete([ e.parentModel.href, e.modelDefinition.plural, r ].join("/")));
                }), Promise.all(n).then(function() {
                    return e.added = new Set(), e.removed = new Set(), e.updateDirty(), Promise.resolve();
                }).catch(function(e) {
                    return Promise.reject("Failed to alter collection:", e);
                });
            }
        } ], [ {
            key: "create",
            value: function(e, n, r) {
                return new t(e, n, r);
            }
        } ]), t;
    }(l.default);
    t.default = c, e.exports = t.default;
}, function(e, t, n) {
    "use strict";
    function r(e) {
        return e && e.__esModule ? e : {
            default: e
        };
    }
    function i(e) {
        return g.has(e) ? g.get(e) : {};
    }
    Object.defineProperty(t, "__esModule", {
        value: !0
    }), t.getDefaultValuesForModelType = i;
    var a = n(31), o = r(a), u = n(26), s = r(u), l = n(27), c = r(l), f = n(28), d = r(f), p = n(29), h = r(p), y = n(30), v = r(y), g = new Map([ [ "organisationUnitGroupSet", o.default ], [ "category", s.default ], [ "categoryOptionGroupSet", c.default ], [ "dataElement", d.default ], [ "dataElementGroupSet", h.default ], [ "dataSet", v.default ] ]);
    t.defaultValues = g;
}, function(e, t) {
    "use strict";
    Object.defineProperty(t, "__esModule", {
        value: !0
    }), t.default = {
        dataDimension: !0
    }, e.exports = t.default;
}, function(e, t) {
    "use strict";
    Object.defineProperty(t, "__esModule", {
        value: !0
    }), t.default = {
        dataDimension: !0
    }, e.exports = t.default;
}, function(e, t) {
    "use strict";
    Object.defineProperty(t, "__esModule", {
        value: !0
    }), t.default = {
        aggregationType: "NONE",
        valueType: "TEXT"
    }, e.exports = t.default;
}, function(e, t) {
    "use strict";
    Object.defineProperty(t, "__esModule", {
        value: !0
    }), t.default = {
        dataDimension: !0
    }, e.exports = t.default;
}, function(e, t) {
    "use strict";
    Object.defineProperty(t, "__esModule", {
        value: !0
    }), t.default = {
        expiryDays: 0,
        openFuturePeriods: 0,
        timelyDays: 15
    }, e.exports = t.default;
}, function(e, t) {
    "use strict";
    Object.defineProperty(t, "__esModule", {
        value: !0
    }), t.default = {
        dataDimension: !0
    }, e.exports = t.default;
}, function(e, t, n) {
    "use strict";
    function r(e) {
        return e && e.__esModule ? e : {
            default: e
        };
    }
    Object.defineProperty(t, "__esModule", {
        value: !0
    });
    var i = n(6), a = r(i), o = n(5), u = r(o), s = n(10), l = r(s), c = n(11), f = r(c), d = n(12), p = r(d);
    t.default = {
        ModelBase: a.default,
        Model: u.default,
        ModelDefinition: l.default,
        ModelDefinitions: f.default,
        ModelValidation: p.default
    }, e.exports = t.default;
}, function(e, t, n) {
    "use strict";
    function r(e, t) {
        if (!(e instanceof t)) throw new TypeError("Cannot call a class as a function");
    }
    Object.defineProperty(t, "__esModule", {
        value: !0
    });
    var i = function() {
        function e(e, t) {
            for (var n = 0; n < t.length; n++) {
                var r = t[n];
                r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), 
                Object.defineProperty(e, r.key, r);
            }
        }
        return function(t, n, r) {
            return n && e(t.prototype, n), r && e(t, r), t;
        };
    }(), a = n(1), o = function() {
        function e() {
            var t = arguments.length <= 0 || void 0 === arguments[0] ? {
                page: 1,
                pageCount: 1
            } : arguments[0], n = arguments.length <= 1 || void 0 === arguments[1] ? {
                list: function() {
                    return Promise.reject("No handler available");
                }
            } : arguments[1];
            r(this, e), this.page = t.page, this.pageCount = t.pageCount, this.total = t.total, 
            this.nextPage = t.nextPage, this.prevPage = t.prevPage, this.pagingHandler = n;
        }
        return i(e, [ {
            key: "hasNextPage",
            value: function() {
                return (0, a.isDefined)(this.nextPage);
            }
        }, {
            key: "hasPreviousPage",
            value: function() {
                return (0, a.isDefined)(this.prevPage);
            }
        }, {
            key: "getNextPage",
            value: function() {
                return this.hasNextPage() ? this.goToPage(this.page + 1) : Promise.reject("There is no next page for this collection");
            }
        }, {
            key: "getPreviousPage",
            value: function() {
                return this.hasPreviousPage() ? this.goToPage(this.page - 1) : Promise.reject("There is no previous page for this collection");
            }
        }, {
            key: "goToPage",
            value: function(e) {
                if (e < 1) throw new Error("PageNr can not be less than 1");
                if (e > this.pageCount) throw new Error("PageNr can not be larger than the total page count of " + this.pageCount);
                return this.pagingHandler.list({
                    page: e
                });
            }
        } ]), e;
    }();
    t.default = o, e.exports = t.default;
}, function(e, t, n) {
    "use strict";
    function r(e) {
        return e && e.__esModule ? e : {
            default: e
        };
    }
    function i(e, t) {
        if (!(e instanceof t)) throw new TypeError("Cannot call a class as a function");
    }
    Object.defineProperty(t, "__esModule", {
        value: !0
    });
    var a = function() {
        function e(e, t) {
            for (var n = 0; n < t.length; n++) {
                var r = t[n];
                r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), 
                Object.defineProperty(e, r.key, r);
            }
        }
        return function(t, n, r) {
            return n && e(t.prototype, n), r && e(t, r), t;
        };
    }(), o = n(2), u = r(o), s = function() {
        function e() {
            var t = arguments.length <= 0 || void 0 === arguments[0] ? u.default.getApi() : arguments[0];
            i(this, e), this.api = t, this._configuration = void 0, this._configPromise = null;
        }
        return a(e, [ {
            key: "all",
            value: function(e) {
                var t = this;
                return null !== this._configPromise && e !== !0 || (this._configPromise = this.api.get("configuration").then(function(e) {
                    return t._configuration = e, t._configuration;
                })), this._configPromise;
            }
        }, {
            key: "get",
            value: function(e, t) {
                return this.all(t).then(function(t) {
                    return t.hasOwnProperty(e) ? Promise.resolve(t[e]) : Promise.reject("Unknown config option: " + e);
                });
            }
        }, {
            key: "set",
            value: function(e, t) {
                var n = this, r = void 0;
                return "systemId" === e ? Promise.reject("The system ID can't be changed") : (r = "feedbackRecipients" !== e && "selfRegistrationOrgUnit" !== e && "selfRegistrationRole" !== e || "null" !== t && null !== t ? "corsWhitelist" === e ? this.api.post([ "configuration", e ].join("/"), t.trim().split("\n"), {
                    dataType: "text"
                }) : this.api.post([ "configuration", e ].join("/"), t, {
                    dataType: "text",
                    contentType: "text/plain"
                }) : this.api.delete([ "configuration", e ].join("/"), {
                    dataType: "text"
                }), r.then(function() {
                    return n._configuration = void 0, Promise.resolve();
                }).catch(function() {
                    return Promise.reject("No configuration found for " + e);
                }));
            }
        } ]), e;
    }();
    t.default = s, e.exports = t.default;
}, function(e, t, n) {
    "use strict";
    function r(e) {
        return e && e.__esModule ? e : {
            default: e
        };
    }
    function i(e, t) {
        if (!(e instanceof t)) throw new TypeError("Cannot call a class as a function");
    }
    Object.defineProperty(t, "__esModule", {
        value: !0
    });
    var a = function() {
        function e(e, t) {
            for (var n = 0; n < t.length; n++) {
                var r = t[n];
                r.enumerable = r.enumerable || !1, r.configurable = !0, "value" in r && (r.writable = !0), 
                Object.defineProperty(e, r.key, r);
            }
        }
        return function(t, n, r) {
            return n && e(t.prototype, n), r && e(t, r), t;
        };
    }(), o = n(1), u = n(2), s = r(u), l = function() {
        function e() {
            var t = arguments.length <= 0 || void 0 === arguments[0] ? s.default.getApi() : arguments[0];
            i(this, e), this.api = t;
        }
        return a(e, [ {
            key: "all",
            value: function() {
                return this.api.get("systemSettings");
            }
        }, {
            key: "get",
            value: function(e) {
                function t(e) {
                    try {
                        return JSON.parse(e);
                    } catch (t) {
                        return e;
                    }
                }
                var n = this;
                return new Promise(function(r, i) {
                    if (!(0, o.isString)(e)) throw new TypeError('A "key" parameter should be specified when calling get() on systemSettings');
                    n.api.get([ "systemSettings", e ].join("/"), void 0, {
                        dataType: "text"
                    }).then(function(e) {
                        var n = t(e);
                        n && r(t(e)), i(new Error("The requested systemSetting has no value or does not exist."));
                    });
                });
            }
        }, {
            key: "set",
            value: function(e, t) {
                var n = [ "systemSettings", e ].join("/");
                return null === t || 0 === ("" + t).length ? this.api.delete(n, {
                    dataType: "text"
                }) : this.api.post(n, t, {
                    dataType: "text",
                    contentType: "text/plain"
                });
            }
        } ]), e;
    }();
    t.default = l, e.exports = t.default;
}, function(e, t, n) {
    var r, i;
    !function(a, o) {
        "use strict";
        "object" == typeof e && e.exports ? e.exports = o() : (r = o, i = "function" == typeof r ? r.call(t, n, t, e) : r, 
        !(void 0 !== i && (e.exports = i)));
    }(this, function() {
        "use strict";
        function e(e) {
            return typeof console !== u && (void 0 !== console[e] ? t(console, e) : void 0 !== console.log ? t(console, "log") : o);
        }
        function t(e, t) {
            var n = e[t];
            if ("function" == typeof n.bind) return n.bind(e);
            try {
                return Function.prototype.bind.call(n, e);
            } catch (t) {
                return function() {
                    return Function.prototype.apply.apply(n, [ e, arguments ]);
                };
            }
        }
        function n(e, t, n) {
            return function() {
                typeof console !== u && (r.call(this, t, n), this[e].apply(this, arguments));
            };
        }
        function r(e, t) {
            for (var n = 0; n < s.length; n++) {
                var r = s[n];
                this[r] = n < e ? o : this.methodFactory(r, e, t);
            }
        }
        function i(t, r, i) {
            return e(t) || n.apply(this, arguments);
        }
        function a(e, t, n) {
            function a(e) {
                var t = (s[e] || "silent").toUpperCase();
                try {
                    return void (window.localStorage[f] = t);
                } catch (e) {}
                try {
                    window.document.cookie = encodeURIComponent(f) + "=" + t + ";";
                } catch (e) {}
            }
            function o() {
                var e;
                try {
                    e = window.localStorage[f];
                } catch (e) {}
                if (typeof e === u) try {
                    var t = window.document.cookie, n = t.indexOf(encodeURIComponent(f) + "=");
                    n && (e = /^([^;]+)/.exec(t.slice(n))[1]);
                } catch (e) {}
                return void 0 === c.levels[e] && (e = void 0), e;
            }
            var l, c = this, f = "loglevel";
            e && (f += ":" + e), c.levels = {
                TRACE: 0,
                DEBUG: 1,
                INFO: 2,
                WARN: 3,
                ERROR: 4,
                SILENT: 5
            }, c.methodFactory = n || i, c.getLevel = function() {
                return l;
            }, c.setLevel = function(t, n) {
                if ("string" == typeof t && void 0 !== c.levels[t.toUpperCase()] && (t = c.levels[t.toUpperCase()]), 
                !("number" == typeof t && t >= 0 && t <= c.levels.SILENT)) throw "log.setLevel() called with invalid level: " + t;
                if (l = t, n !== !1 && a(t), r.call(c, t, e), typeof console === u && t < c.levels.SILENT) return "No console available for logging";
            }, c.setDefaultLevel = function(e) {
                o() || c.setLevel(e, !1);
            }, c.enableAll = function(e) {
                c.setLevel(c.levels.TRACE, e);
            }, c.disableAll = function(e) {
                c.setLevel(c.levels.SILENT, e);
            };
            var d = o();
            null == d && (d = null == t ? "WARN" : t), c.setLevel(d, !1);
        }
        var o = function() {}, u = "undefined", s = [ "trace", "debug", "info", "warn", "error" ], l = new a(), c = {};
        l.getLogger = function(e) {
            if ("string" != typeof e || "" === e) throw new TypeError("You must supply a name when creating a logger.");
            var t = c[e];
            return t || (t = c[e] = new a(e, l.getLevel(), l.methodFactory)), t;
        };
        var f = typeof window !== u ? window.log : void 0;
        return l.noConflict = function() {
            return typeof window !== u && window.log === l && (window.log = f), l;
        }, l;
    });
} ]);
//# sourceMappingURL=app.js.map
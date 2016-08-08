/**
 * Payment page universal handler
 */
(function ($) {
    jQuery.ajaxSettings.traditional = true; // https://forum.jquery.com/topic/jquery-post-1-4-1-is-appending-to-vars-when-posting-from-array-within-array

    var settings = {
        orderIdParam: "mdOrder",
        language: "ru",

        orderId: "orderNumber",
        frameId: "iframe",
        frameError: "error",
        staticUrlFrame: false,

        getSessionStatusAction: "../../rest/getSessionStatus.do",
        getUrlFrameAction: "../../dengimailru/generateurl.do",
        messageAjaxError: "Сервис временно недоступен. Попробуйте позднее.",
        messageGetUrl: "Некорректная ссылка на фрейм",
        messageRedirecting: "Перенаправление на другую страницу",
        errorPage: "error.html",

        statusPayment: false
    };

    var properties = {
        orderId: null
    };

    var methods = {

        init: function (options) {
            if (options) {
                $.extend(settings, options);
            }
            return this.each(function () {
                methods.bindControls();

                var orderId = $.url.param(settings.orderIdParam);
                if (!orderId) {
                    console.log("Error: unknown order");
                    return;
                }
                properties.orderId = orderId;
                if (!settings.staticUrlFrame) {
                   methods.getFrameUrl(); 
                }
                
            });
        },
        bindControls: function () {
            $(window).bind("message", methods.getMessageFromFrame); 
        },
        getStatusLoadFrame: function () {
            $('#' + settings.frameId).load(function() {
                methods.hideProgress();
                methods.showFrame();
            });
        },
        hideProgress: function () {
            $("#indicator").hide();     
        },
        showProgress: function () {
            $("#indicator").show();            
        },
        showFrame: function () {
            $("#" + settings.frameId).show();
        },
        hideFrame: function () {
            $("#" + settings.frameId).hide();
        },
        showError: function (message) {
            methods.hideProgress();
            $('#errorBlock').empty();
            $('#errorBlock').show();
            $('#errorBlock').append(message);
        },
        hideError: function (){
            $('#errorBlock').hide();
        },
        redirect: function (destination, message) {
            if (message) {
                methods.hideFrame;
                $('#infoBlock').empty();
                $('#infoBlock').append('<p>' + message + "</p>");
            }
            document.location = destination;
        },

        getFrameUrl: function () {
            var orderId = properties.orderId;
            $.ajax({
                url:settings.getUrlFrameAction,
                type:'POST',
                cache:false,
                data:({
                    mdOrder:orderId
                }),
                dataType:'json',
                error: function () {
                    methods.showError(settings.messageAjaxError);
                },
                success: function (data) {
                    if ( data['url'] ) {
                        $('#' + settings.frameId).attr('src', data['url']);
                        methods.getStatusLoadFrame();
                    } else if (data['error'] == 2) { //Истекла сессия
                        methods.redirect(settings.errorPage + "?error=payment.errors.session_expired");
                    } else if (data['error'] == 99){ //системная ошибка
                        methods.redirect(settings.errorPage + "?error=payment.errors.session_expired");
                    } else {
                        methods.redirect(settings.errorPage);
                    }
                    return true;
                }
            });
        },

        getPaymentStatus: function () {
            var orderId = properties.orderId;
            $.ajax({
                url:settings.getSessionStatusAction,
                type:'POST',
                cache:false,
                data:({
                    MDORDER:orderId
                }),
                dataType:'json',
                error: function () {
                    methods.showError(settings.messageAjaxError);
                },
                success: function (data) {
                    if (data['redirect']) {
                        methods.redirect(data['redirect'], settings.messageRedirecting);
                        settings.statusPayment = true;

                    } else if (data['returnUrl']) {
                        methods.redirect(data['returnUrl'], settings.messageRedirecting);
                        settings.statusPayment = true;

                    } else if (data['failUrl']) {
                        methods.redirect(data['failUrl'], settings.messageRedirecting);
                        settings.statusPayment = true;

                    } else {
                        settings.statusPayment = false;
                    }
                    return true;
                }
            });
        },

        getMessageFromFrame: function (event) {
            var data = event.originalEvent ? event.originalEvent.data : event.data;
            var origin = event.originalEvent ? event.originalEvent.oe : event.origin;

            if (data['type'] == 'billing') {
                if (data['action'] == '3dsPage'){
                    // console.log("3ds");

                } else if (data['action'] == 'paySuccess') {
                    methods.getPaymentStatus();

                    if (settings.statusPayment == false) {
                        setInterval(methods.getPaymentStatus , 2000);
                    }

                } else if (data['action'] == 'redirect') {
                    methods.redirect(data['back_url'], settings.messageRedirecting);
                }

            }
            return true;
        }

    };


    $.fn.shell = function (method) {
        // Method calling logic
        if (methods[method]) {
            return methods[ method ].apply(this, Array.prototype.slice.call(arguments, 1));
        } else if (typeof method === 'object' || !method) {
            return methods.init.apply(this, arguments);
        } else {
            $.error('Method ' + method + ' does not exist on jQuery.payment');
        }
    };

})(jQuery);


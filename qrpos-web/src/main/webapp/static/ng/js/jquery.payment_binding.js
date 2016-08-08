/**
 * Payment page universal handler
 */
(function ($) {
    var controlIds = {
        // forms
        acs:'acs',

        // input fields
        cvc:'cvc',

        // hidden fields
        paReq:'paReq',
        md:'md',
        termUrl:'termUrl',

        // divs
        maskedPan:'maskedPan',
        orderNumber:'orderNumber',
        amount:'amount',
        description:'description',
        indicator:'indicator',
        errorBlock:'errorBlock',
        countdown:'countdown',
        infoBlock:'infoBlock',
        typeCardIcon: 'typecard-icon',

        // button
        submit:'sendPayment'
    };

    var paramNames = {
        mdOrder:'mdOrder',
        orderNumber:'orderNumber',
        amount:'amount',
        description:'description',
        maskedPan:'maskedPan',
        bonusAmount:'bonusAmount'
    };

    var urls = {
        paymentBinding:"../../rest/processBindingForm.do",
        getSessionStatus:"../../rest/getSessionStatus.do",
        showError:"../../rest/showErrors.do",
        getAvailableLoyalty:"../../rest/getAvailableLoyalty.do"
    };

    var settings = {
        language:'ru',
        pageView:$.url.param('pageView'),
        mdOrder:$.url.param('mdOrder'),
        bindingId:null,
        pointInputId:"spasibo",
        sliderBlock:"spasibo_block",
        pointPenny:"sbrf_spasibo",
        pointSlider:"spasiboSlider",
        integerPoint:true,
        pointPercentOrder:0.99,
        bonusAmount: "bonusAmount",
        remainderAmount: "remainderAmount",
        bonusBlock: "bonusBlock",

        messageAjaxError:"Сервис временно недоступен. Попробуйте позднее.",
        messageTimeRemaining:"До окончания сессии осталось #MIN#:#SEC#",
        messageRedirecting:"Переадресация..."
    };

    var properties = {
        isMaestro: null,
        amount: null
    }

    var methods = {
        init:function (options) {
            if (options) {
                $.extend(settings, options);
            }
            return this.each(function () {
                $(this).ready(methods.initControls);

                methods.getSessionStatus(true);
            });
        },

        updatePage:function (data) {
            if ($("#" + controlIds.orderNumber)) $("#" + controlIds.orderNumber).text(data[paramNames.orderNumber]);
            if ($("#" + controlIds.description))$("#" + controlIds.description).text(data[paramNames.description]);
            if ($("#" + controlIds.maskedPan))$("#" + controlIds.maskedPan).text(formatPan(data[paramNames.maskedPan]));
            if ($("#" + controlIds.amount)) {
                $("#" + controlIds.amount).text(data[paramNames.amount]);
                properties.amount = data[paramNames.amount];
                properties.rawAmount = (data[paramNames.amount]).replace(/[a-zA-Z ]/g, "");
            }

            if (data[paramNames.bonusAmount] > 0) {
                $("#" + settings.bonusBlock).show();
                $("#" + settings.bonusAmount).text(data[paramNames.bonusAmount ] / 100);
            } else {
                $("#" + settings.bonusBlock).hide();
            }

            if (data[controlIds.description] == "") {
                $("#" + controlIds.description).closest('.row').hide();
            } else {
                $("#" + controlIds.description).text(data[controlIds.description]);
            }

            //get payment system for binding
            var bindingId = data['bindingId'],
                bindings = data['bindingItems'],
                paymentSystem;

            bindings.forEach(function(item, i, arr) {
                if (item['id'] == bindingId) {
                    paymentSystem = item['paymentSystem'],
                    properties.isMaestro = item['isMaestro'];
                }
            });

            if (properties.isMaestro == "MAESTRO") {
                $('#' + controlIds.typeCardIcon).removeClass();
                $('#' + controlIds.typeCardIcon).addClass("MAESTRO");
            } else if (paymentSystem == "MASTERCARD") {
                $('#' + controlIds.typeCardIcon).removeClass();
                $('#' + controlIds.typeCardIcon).addClass("MASTERCARD");
            } else if (paymentSystem == "VISA") {
                $('#' + controlIds.typeCardIcon).removeClass();
                $('#' + controlIds.typeCardIcon).addClass("VISA");
            }

        },

        checkControl:function (name) {
            if ($(name).length == 0) {
                alert('Absent ' + name + ' . Please, check documentation or template page');
            }
        },

        checkControls:function () {
            methods.checkControl("#" + controlIds.cvc);
            methods.checkControl('#' + controlIds.submit);
            methods.checkControl('#' + controlIds.errorBlock);
            methods.checkControl('#' + controlIds.countdown);
            methods.checkControl('#' + controlIds.infoBlock);
            methods.checkControl('#' + controlIds.indicator);
        },

        bindControls:function () {
            $("#" + controlIds.cvc).bind('keyup.binding_payment', methods.validate);
            $('#' + controlIds.cvc).bind('keypress.binding_payment', methods.checkNumberInput);
            $('#' + controlIds.cvc).bind('paste.binding_payment', methods.checkNumberInput);

            $('#' + controlIds.submit).bind('click.payment_binding', methods.doSubmitForm);
            $('#' + controlIds.submit).attr('disabled', 'true');

            $('#' + settings.pointInputId).bind('keyup.binding_payment', methods.checkNumberInput);
            $('#' + settings.pointInputId).bind('keyup.binding_payment', methods.updateSpasibo);
        },

        initControls:function () {
            methods.checkControls();
            methods.bindControls();
        },

        checkNumberInput:function (event) {
            setTimeout(function () {
                var elem = $(event.target);
                elem.val(elem.val().replace(/\D/g, ""));
            }, 0);
        },

        sendBindingPayment:function () {
            methods.showProgress();
            paymentData = {
                'orderId':settings.mdOrder,
                'bindingId':settings.bindingId,
                'cvc':$('#' + controlIds.cvc).val(),
                'language':settings.language
            }
            if (($("#" + settings.pointPenny).val() > 0)) {
                paymentData.loyaltyId = $("#" + settings.pointPenny).prop('id');
                paymentData.pointsAmount = $("#" + settings.pointPenny).val();
            }
            $.ajax({
                url:urls.paymentBinding,
                type:'POST',
                cache:false,
                data:paymentData,
                dataType:'json',
                error:function () {
                    methods.showError(settings.messageAjaxError);
                    return true;
                },
                success:function (data) {
                    methods.hideProgress();
                    if (data['acsUrl'] != null) {
                        methods.redirectToAcs(data);
                    } else if ('error' in data) {
                        methods.showError(data['error']);
                    } else if ('redirect' in data) {
                        methods.redirect(data['redirect'], data['info']); // , settings.messageRedirecting
                    }
                    return true;
                }
            });
            return false;
        },

        switchActions:function (isEnabled) {
            $('#' + controlIds.submit).attr('disabled', !isEnabled);
        },

        doSubmitForm:function (event) {
            event.preventDefault();
            if (!methods.validate()) return;

            methods.switchActions(false);
            methods.sendBindingPayment();
        },

        validate:function () {
            $('#' + controlIds.errorBlock).empty();
            var isValid = true;

            if (!/^\d{3,4}$/.test($('#' + controlIds.cvc).val())) {
                isValid = false;
                $('#' + controlIds.cvc + '-validation').attr('class', 'login-invalid');
            } else {
                $('#' + controlIds.cvc + '-validation').attr('class', 'login-valid');
            }

            if (properties.isMaestro) {
                isValid = true;
            }

            methods.switchActions(isValid);
            return isValid;
        },

        showProgress:function () {
            $('#' + controlIds.errorBlock).empty();
            $('#' + controlIds.indicator).show();
        },

        hideProgress:function () {
            $('#' + controlIds.indicator).hide();
        },

        showError:function (message) {
            methods.hideProgress();
            $('#' + controlIds.errorBlock).empty();
            $('#' + controlIds.errorBlock).prepend('<p class="errorField" id="loginError">' + message + "</p>");
        },

        redirect:function (destination, message) {
            if (message) {
                $('#' + controlIds.infoBlock).empty();
                $('#' + controlIds.infoBlock).prepend('<p>' + message + "</p>");
            }
            $('#' + controlIds.countdown).hide();
            $('#' + controlIds.errorBlock).empty();
            methods.switchActions(false);
            document.location = destination;
        },

        redirectToAcs:function (data) {
            $('#' + controlIds.acs).attr('action', data['acsUrl']);
            $('#' + controlIds.paReq).val(data['paReq']);
            $('#' + controlIds.md).val(settings.mdOrder);
            $('#' + controlIds.termUrl).val(data['termUrl']);
            $('#' + controlIds.acs).submit();
        },

        initSlider: function (minAmount, maxAmount) {
            var stepSlider;
            if (settings.integerPoint) {
                stepSlider = 1;
            } else {
                stepSlider = 0.01;
            }
            $('#' + settings.pointSlider).slider({
                range: 'min',
                min: minAmount,
                max: maxAmount,
                value: minAmount,
                step: stepSlider,
                slide: function( event, ui ) {
                  var remAmount = (properties.rawAmount - ui.value).toFixed(2);
                  $('#' + settings.remainderAmount).val(remAmount);
                  $('#' + settings.pointInputId).val(ui.value);

                  $('#' + settings.rawAmount).val(ui.value);
                  $('#' + settings.pointPenny).val( Math.round($('#' + settings.pointInputId).val()*100) );
                  methods.validate();
                }
            });
            $('#' + settings.remainderAmount).val((properties.rawAmount - properties.pointAmountMin).toFixed(2));
            $('#' + settings.pointInputId).val(properties.pointAmountMin);
            $('#' + settings.pointPenny).val(properties.pointAmountMin * 100);
        },

        updateSpasibo:function (event) {
            setTimeout(function () {
                var elem = $(event.target),
                    points = 0,
                    remAmount = 0;
                if (elem.prop('id') == $('#' + settings.pointInputId).prop('id')) {
                    if (+elem.val() > +properties.pointAmountMax) {
                        elem.val(properties.pointAmountMax);
                    }
                }
                remAmount = (properties.rawAmount - elem.val()).toFixed(2);
                $('#' + settings.remainderAmount).val(remAmount);
                $('#' + settings.pointSlider).slider('value', elem.val());
                $('#' + settings.pointPenny).val( $('#' + settings.pointInputId).val()*100);
            }, 0);
        },

        getAvailableLoyaltyForBinding:function (pan, bindingId) {
            methods.showProgress();
            var data = {
                orderId : settings.mdOrder,
                bindingId: settings.bindingId
            };

            $.ajax({
                url: urls.getAvailableLoyalty,
                type:'POST',
                data: data,
                dataType:'json',
                error:function () {
                    methods.showError(settings.messageAjaxError);
                },
                success:function (data) {
                    methods.hideProgress();
                    if (data['errorCode'] != 0) {
                        $('#' + settings.sliderBlock).hide();
                    }
                    if (data.loyaltyOperations.length) {
                        methods.calculatePoints(data);
                        methods.initSlider(properties.pointAmountMin, properties.pointAmountMax);
                        if ((properties.pointAmountMax == 0) ||
                            (properties.pointAmountMax < properties.pointAmountMin)) {
                            $('#' + settings.sliderBlock).hide();
                        } else {
                            $('#' + settings.sliderBlock).show();
                        }
                    } else {
                        $('#' + settings.sliderBlock).hide();
                    }
                }
            });
        },

        calculatePoints:function (data) {
            var pointAmountMax = data['loyaltyOperations'][0]['maxAmount'],
                pointAmountMin = data['loyaltyOperations'][0]['minAmount'],
                amountOrder = properties.amount.replace(/[a-zA-Z ]/g, "");
                amountOrder = Math.round((amountOrder * settings.pointPercentOrder) * 100); //Максимальный балл не может превышать 99% суммы заказа

            if (settings.integerPoint) {
                properties.pointAmountMin = Math.floor(pointAmountMin / 100);
                if (pointAmountMax >= amountOrder) {
                    properties.pointAmountMax = Math.floor(amountOrder / 100);
                } else {
                    properties.pointAmountMax = Math.floor(pointAmountMax / 100);
                }
            } else {
                properties.pointAmountMin = pointAmountMin / 100;
                if (pointAmountMax >= amountOrder) {
                    properties.pointAmountMax = amountOrder / 100;
                } else {
                    properties.pointAmountMax = pointAmountMax / 100;
                }
            }
        },

        startCountdown: function (remainingSecs) {
            $(document).oneTime(remainingSecs * 1000, function () {
                methods.validate();
            });

            $('#countdown').everyTime(1000, function (i) {
                if ( settings.messageTimeRemaining.indexOf("#HOU#") + 1 ){
                    var secondsLeft = remainingSecs - i;
                    var seconds = secondsLeft % 60;
                    var hours = Math.floor(secondsLeft / 3600);
                    var minutes = Math.floor((secondsLeft - hours * 3600) / 60);
                } else {
                    var secondsLeft = remainingSecs - i;
                    var seconds = secondsLeft % 60;
                    var minutes = Math.floor(secondsLeft / 60)
                    var hours = "";
                }
                if (seconds < 10) {
                    seconds = "0" + seconds;
                }
                if (minutes < 10) {
                    minutes = "0" + minutes;
                }
                $(this).text(settings.messageTimeRemaining
                    .replace("#HOU#", new String(hours))
                    .replace("#MIN#", new String(minutes))
                    .replace("#SEC#", new String(seconds)));
                if (secondsLeft <= 0) {
                    methods.getSessionStatus(false);
                }
            }, remainingSecs);
        },

        getSessionStatus:function (informRbsOnLoad) {
            methods.showProgress();
            $.ajax({
                url:urls.getSessionStatus,
                type:'POST',
                cache:false,
                data:({
                    MDORDER:settings.mdOrder,
                    language:settings.language,
                    informRbsOnLoad:informRbsOnLoad,
                    pageView:settings.pageView
                }),
                dataType:'json',
                error:function () {
                    methods.showError(settings.messageAjaxError);
                },
                success:function (data) {
                    methods.hideProgress();
                    if ('redirect' in data) {
                        methods.redirect(data['redirect'], settings.messageRedirecting);
                    } else {
                        if ('error' in data) {
                            methods.showError(data['error']);
                        }
                        methods.updatePage(data);
                        var remainingSecs = data['remainingSecs'];
                        if (remainingSecs > 0) {
                            methods.startCountdown(remainingSecs);
                            settings.bindingId = data['bindingId'];
                            methods.getAvailableLoyaltyForBinding();
                        } else {
                            methods.redirect(urls.showError + '?pageView=' + settings.pageView, settings.messageRedirecting);
                        }
                    }
                    return true;
                }
            });
        }
    };

    $.fn.payment_binding = function (method) {
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

function formatPan(value) {
    var number = value.slice(0,6) + "******" + value.slice(-4);
    var formatValue = number.replace(/([\d\W\*]{4}(?!$))/g,'$1 ');
    return formatValue;
}
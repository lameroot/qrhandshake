/**
 * Payment page universal handler
 */
(function ($) {
    jQuery.ajaxSettings.traditional = true; // https://forum.jquery.com/topic/jquery-post-1-4-1-is-appending-to-vars-when-posting-from-array-within-array

    var settings = {
        // name for orderId parameter
        orderIdParam:"mdOrder",
        language:"ru",

        // orderDetails
        orderId: "orderNumber",
        amount: "amount",
        rawAmount: "rawAmount",
        feeAmount: "feeAmount",
        feeChecked: "feeChecked",
        bonusAmount: "bonusAmount",
        remainderAmount: "remainderAmount",
        bonusBlock: "bonusBlock",
        description: "description",
        feeBlock: "feeBlock",

        paymentFormId: "formPayment",
        acsFormId: "acs",

        pan: "iPAN_sub",
        panInputId: "iPAN",
        pan1InputId: "pan1",
        pan2InputId: "pan2",
        pan3InputId: "pan3",
        pan4InputId: "pan4",
        yearSelectId: "year",
        currentYear:(new Date).getFullYear(),
        monthSelectId:"month",
        monthInput: "input-month",
        yearInput: "input-year",
        cardholderInputId:"iTEXT",
        cvcInputId:"iCVC",
        emailInputId: "email",
        phoneInputId: "phone",

        pointInputId:"spasibo",
        sliderBlock:"spasibo_block",
        pointPenny:"sbrf_spasibo",
        pointSlider:"spasiboSlider",
        integerPoint: true,
        pointPercentOrder: 0.99,

        paramPrefix:"param.",
        pageView:$.url.param('pageView'),

        bindingCheckBoxId: "createBinding",
        bindingPaymentButton: "buttonBindingPayment2",
        deactiveBindingId: "deactiveBinding",
        agreementCheckboxId: "iAgree",
        agreementBlock: "agreeBlock",
        emailCheckboxBlock: "emailCheckboxBlock",
        emailBlock: "emailBlock",
        emailCheckbox: "showEmail",
        phoneBlock: "phoneBlock",
        typeCardIcon: "typecard-icon",

        paymentAction:"../../rest/processform.do",
        paymentBindingAction:"../../rest/processBindingForm.do",
        getSessionStatusAction:"../../rest/getSessionStatus.do",
        isMaestroCardAction:"../../rest/isMaestroCard.do",
        showErrorAction:"../../rest/showErrors.do",
        getFeeAction: "../../rest/getFee.do",
        getAvailableLoyalty:"../../rest/getAvailableLoyalty.do",
        unbindCard: "../../rest/unbindcardanon.do",

        messageAjaxError:"Сервис временно недоступен. Попробуйте позднее.",
        messageTimeRemaining:"До окончания сессии осталось #HOU#:#MIN#:#SEC#",
        messageRedirecting:"Переадресация...",

        getFeeEnabled: false,
        bindingCheckboxEnabled: false,
        agreementCheckboxEnabled: false,
        emailEnabled: false,
        phoneEnabled: false,

        paramNames: [],

        onReady: function () {
        },

        updatePage:function (data) {
            $("#" + settings.orderId).text(data[settings.orderId]);
            $("#" + settings.amount).text(data[settings.amount]);
            properties.amount = data[settings.amount];
            properties.rawAmount = (data[settings.amount]).replace(/[a-zA-Z ]/g, "");
            $("#" + settings.rawAmount).text(properties.rawAmount);

            if (data[settings.description] == "") {
                $("#" + settings.description).closest('.row').hide();
            } else {
                $("#" + settings.description).text(data[settings.description]);
            }
            if (data[settings.bonusAmount] > 0) {
                $("#" + settings.bonusBlock).show();
                $("#" + settings.bonusAmount).text(data[settings.bonusAmount ] / 100);
            } else {
                $("#" + settings.bonusBlock).hide();
            }
            if (settings.emailEnabled) {
                $("#" + settings.emailCheckboxBlock).show();
            }
            if (settings.phoneEnabled) {
                $("#" + settings.phoneBlock).show();
            }
            if (data['feeAmount']) {
                $("#feeBlock").show();
                $("#feeAmount").text(data['feeAmount']);
            } else {
                $("#feeBlock").hide();
            }
            if (data['queriedParams']) {
                $.each(data['queriedParams'], function (name, value) {
                    var el = $('#' + name);
                    if (el && el.is('a')){
                        el.prop('href', value);
                    } else if(el) {
                        if (el.val) el.val(value);
                        if (el.text) el.text(value);
                    }
                });
            }
        }
    };

    var properties = {
        orderId:null,
        amount:null,
        rawAmount:null,
        expired:false,
        fee: 0,
        loyalty:false,
        validCard:false,
        pointAmountMax:null,
        pointAmountMin:null,
        cvcValidationRequired: true,
        isBindingEnabled: false,
        isAgreementEnabled: true,
        validateEmail: false,
        phoneEnabled: false
    };

    var methods = {
        maestroCheck:{
            pan:"",
            result:false
        },

        init:function (options) {
            if (options) {
                $.extend(settings, options);
            }
            return this.each(function () {
                $(this).ready(methods.fillControls);
                //methods.bindControls();
                // init data
                var orderId = $.url.param(settings.orderIdParam);
                if (!orderId) {
                    $(this).log("Unknown order", "error");
                    return;
                }
                properties.orderId = orderId;
                properties.expired = false;
                methods.getSessionStatus(true);
            });
        },
        checkControl:function (name) {
            if ($(name).length == 0) {
                alert('Absent ' + name + ' . Please, check documentation or template page');
            }
        },
        checkControls:function () {
            methods.checkControl('#' + settings.paymentFormId);
            methods.checkControl("#" + settings.panInputId);
            methods.checkControl("#" + settings.cardholderInputId);
            methods.checkControl("#" + settings.cvcInputId);

            methods.checkControl("#" + settings.yearSelectId);
            methods.checkControl("#" + settings.monthSelectId);

            methods.checkControl('#' + settings.orderId);
            methods.checkControl('#' + settings.amount);

            if (settings.bindingCheckboxEnabled) methods.checkControl('#' + settings.bindingCheckBoxId);
            if (settings.agreementCheckboxEnabled) methods.checkControl('#' + settings.agreementCheckboxId);
            if (settings.emailEnabled) methods.checkControl('#' + settings.emailInputId);

            methods.checkControl('#buttonPayment');
            methods.checkControl('#' + settings.orderIdParam);
            methods.checkControl('#' + settings.acsFormId);
            methods.checkControl('#location');
            methods.checkControl('#expiry');
            methods.checkControl('#language');
            methods.checkControl('#errorBlock');
            methods.checkControl('#numberCountdown');
            methods.checkControl('#infoBlock');
        },
        bindControls:function () {
            methods.checkControls();
            $('#' + settings.paymentFormId).bind('submit.payment', methods.onSubmit);
            $("#" + settings.panInputId).bind('keyup.payment', methods.validate);
            $("#" + settings.panInputId).bind('keyup.payment paste.payment', methods.getTypeCard);

            $("#" + settings.cardholderInputId).bind('keyup.payment', methods.validate);
            $("#" + settings.pan).bind('keyup.payment', methods.validate);
            $("#" + settings.pan).bind('keyup.payment paste.payment', methods.getTypeCard);


            $('#' + settings.cvcInputId).bind('keyup.payment', methods.checkNumberInput);
            $('#' + settings.cvcInputId).bind('keyup.payment', methods.validate);

            $("#" + settings.yearSelectId).bind('change.payment', methods.validate);
            $("#" + settings.monthSelectId).bind('change.payment', methods.validate);

            $('#' + settings.panInputId).bind('keypress.payment', methods.checkNumberInput);
            $('#' + settings.pan1InputId).bind('keypress.payment', methods.checkNumberInput);
            $('#' + settings.pan1InputId).bind('paste.payment', methods.checkNumberInput);
            $('#' + settings.pan2InputId).bind('keypress.payment', methods.checkNumberInput);
            $('#' + settings.pan2InputId).bind('paste.payment', methods.checkNumberInput);
            $('#' + settings.pan3InputId).bind('keypress.payment', methods.checkNumberInput);
            $('#' + settings.pan3InputId).bind('paste.payment', methods.checkNumberInput);
            $('#' + settings.pan4InputId).bind('keypress.payment', methods.checkNumberInput)
            $('#' + settings.pan4InputId).bind('paste.payment', methods.checkNumberInput);

            $('#' + settings.pointInputId).bind('keyup.payment', methods.checkNumberInput);
            $('#' + settings.pointInputId).bind('keyup.payment', methods.updateSpasibo);

            $('#' + settings.cvcInputId).bind('keypress.payment', methods.checkNumberInput);
            $('#' + settings.cvcInputId).bind('paste.payment', methods.checkNumberInput);
            $('#' + settings.cardholderInputId).bind('keypress.payment', methods.checkNameInput);

            $('#' + settings.cardholderInputId).bind('paste.payment', methods.checkNameInput);
            $("#" + settings.cardholderInputId).bind('keyup.payment', methods.checkNameInput);

            $("#" + settings.emailInputId).bind('keyup.payment', methods.validate);
            $("#" + settings.phoneInputId).bind('keyup.payment', methods.validate);

            $("#" + settings.phoneInputId).bind('keyup.payment', methods.checkPhoneInput);

            $('#' + settings.deactiveBindingId).bind('click', methods.deactiveBinding);

            $('#buttonPayment').bind('click.payment', methods.doSubmitForm);
            $('#buttonPayment').prop('disabled', 'true');
            $('#buttonPaymentSbrf').bind('click.payment', methods.doSubmitFormSbrf);

            $(document).on('keyup.payment paste.payment', '#' + settings.pan, methods.getTypeCard);
            $('#' + settings.agreementCheckboxId).bind('change', methods.validate);
            $('#' + settings.emailCheckbox).bind('change', methods.validate);
        },
        fillControls: function () {
            methods.bindControls();
            $('#' + settings.yearSelectId).empty();
            var year = settings.currentYear;
            while (year < settings.currentYear + 21) {
                var option = "<option value=" + year + ">" + year + "</option>";
                $('#' + settings.yearSelectId).append($(option));
                year++;
            }
        },
        checkNumberInput:function () {
            var elem = $(this);
            elem.val(elem.val().replace(/\D/g, ""));
        },
        checkPhoneInput:function () {
            var elem = $(this);
            elem.val(elem.val().replace(/[^\d\(\)\+]/g, ""));
        },
        checkNameInput:function () {
            var elem = $(this);
            elem.val(transliterate(elem.val()).replace(/[^a-zA-Z ' \-`.]/g, ""));
        },
        setupAgreementBlock: function (data) {
            if (data['agreementUrl'] != null && data['agreementUrl'] != "null" &&
                data['agreementUrl'] != "" && data['agreementUrl'] != "#") {
                $('#agreeHref').prop("href", data['agreementUrl']);
                properties.validateAgreementCheckbox = true;
            } else {
                $('#agreeHref').closest('.agreeBox').hide();
                properties.isAgreementEnabled = false;
                properties.validateAgreementCheckbox = true;
            }
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
        onSubmit:function (event) {
            event.preventDefault();
            methods.sendPayment();
        },
        doSubmitFormSbrf:function () {
            methods.sendPaymentSbrf();
        },
        switchActions: function (isEnabled) {
            $('#buttonPayment').prop('disabled', !isEnabled);
            $('#buttonBindingPayment2').prop('disabled', !isEnabled);
        },
        doSubmitForm:function () {
            if (!methods.validate()) {
                return;
            }
            if (settings.getFeeEnabled && properties.fee > 0 && !$("#" + settings.feeChecked).val()) {
                return;
            }
            $('#expiry').val($("#" + settings.yearSelectId).val() + $("#" + settings.monthSelectId).val());
            methods.switchActions(false);
            $('#formPayment').submit();
        },

        validate:function () {
            $('#errorBlock').empty();
            var isValid = true;
            var isPanValid = false;

            if (!/(\s*[A-Za-z]+\s*((\.|'|-)|\s+|$)){1,}/.test($('#' + settings.cardholderInputId).val())) {
                isValid = false;
                $('#' + settings.cardholderInputId + '-validation').prop('class', 'login-invalid');
            } else {
                $('#' + settings.cardholderInputId + '-validation').prop('class', 'login-valid');
            }

            if (!/^\d{12,19}$/.test($('#' + settings.panInputId).val()) ||
                !luhn($('#' + settings.panInputId).val()) ||
                $('#' + settings.pan1InputId).length > 0 && $('#' + settings.pan1InputId).val().length != 4 ||
                $('#' + settings.pan2InputId).length > 0 && $('#' + settings.pan2InputId).val().length != 4 ||
                $('#' + settings.pan3InputId).length > 0 && $('#' + settings.pan3InputId).val().length != 4) {
                isValid = false;
                isPanValid = false;
                properties.validCard = false;
                $('#' + settings.panInputId + '-validation').prop('class', 'login-invalid');
            } else {
                $('#' + settings.panInputId + '-validation').prop('class', 'login-valid');
                isPanValid = true;
                properties.validCard = true;
            }

            if (properties.loyalty) {
                if ($('#' + settings.pointInputId).val() < properties.pointAmountMin) {
                    isValid = false;
                    $('#' + settings.pointInputId + '-validation').prop('class', 'login-invalid');
                } else {
                    $('#' + settings.pointInputId + '-validation').prop('class', 'login-valid');
                }
            }

            // check if card expiration date
            var dateNow = new Date();
            var cardDate = new Date();

            cardDate.setYear($('#' + settings.yearSelectId).val());
            cardDate.setMonth($('#' + settings.monthSelectId).val() - 1);
            if (((dateNow.getTime() > cardDate.getTime()) || !$('#' + settings.monthSelectId).val()) ||
                (($('#' + settings.yearInput).is(':visible') && $('#' + settings.yearInput).val().length !== 2) ||
                 ($('#' + settings.monthInput).is(':visible') && $('#' + settings.monthInput).val().length !== 2))) {
                $('#' + settings.yearSelectId + '-validation').prop('class', 'login-invalid');
                isValid = false;
            } else {
                $('#' + settings.yearSelectId + '-validation').prop('class', 'login-valid');
            }

            if (this.expired) {
                isValid = false;
            }

            if (isPanValid) {
                if (/^(50|5[6-8]|6[0-9]).*$/.test($('#' + settings.panInputId).val())) {
                    var isMaestro = methods.isMaestroCard();
                    if (isMaestro == undefined) { // Был вызов AJAX => это не submit и результат, возвращаемый validate(), не нужен, а оставшаяся валидация выполнилась в isMaestroCard().
                        return;
                    }

                    if (isMaestro) { // AJAX вызова не было, вернулось запомненное значение. Продолжим валидацию.
                        $("#cvcMessage").show();
                        if (!($('#' + settings.cvcInputId).val() == "" || /^\d{3,4}$/.test($('#' + settings.cvcInputId).val()))) { // Это Маэстро.
                            isValid = false;
                            $('#' + settings.cvcInputId + '-validation').prop('class', 'login-invalid');
                        } else {
                            $('#' + settings.cvcInputId + '-validation').prop('class', 'login-valid');
                        }
                    } else { // Не Маэстро.
                        $("#cvcMessage").hide();
                        if (properties.cvcValidationRequired) {
                            if (!/^\d{3,4}$/.test($('#' + settings.cvcInputId).val())) {
                                isValid = false;
                                $('#' + settings.cvcInputId + '-validation').prop('class', 'login-invalid');
                            } else {
                                $('#' + settings.cvcInputId + '-validation').prop('class', 'login-valid');
                            }
                        } else if (!($('#' + settings.cvcInputId).val() == "" || /^\d{3,4}$/.test($('#' + settings.cvcInputId).val()))) {
                            isValid = false;
                            $('#' + settings.cvcInputId + '-validation').prop('class', 'login-invalid');
                        } else {
                            $('#' + settings.cvcInputId + '-validation').prop('class', 'login-valid');
                        }
                    }
                } else { // Вообще не Маэстро.
                    $("#cvcMessage").hide();
                    if (properties.cvcValidationRequired) {
                        if (!/^\d{3,4}$/.test($('#' + settings.cvcInputId).val())) {
                            isValid = false;
                            $('#' + settings.cvcInputId + '-validation').prop('class', 'login-invalid');
                        } else {
                            $('#' + settings.cvcInputId + '-validation').prop('class', 'login-valid');
                        }
                    } else if (!($('#' + settings.cvcInputId).val() == "" || /^\d{3,4}$/.test($('#' + settings.cvcInputId).val()))) {
                        isValid = false;
                        $('#' + settings.cvcInputId + '-validation').prop('class', 'login-invalid');
                    } else {
                        $('#' + settings.cvcInputId + '-validation').prop('class', 'login-valid');
                    }
                }
            } else {
                if (properties.cvcValidationRequired) {
                    if (!/^\d{3,4}$/.test($('#' + settings.cvcInputId).val())) {
                        isValid = false;
                        $('#' + settings.cvcInputId + '-validation').prop('class', 'login-invalid');
                    } else {
                        $('#' + settings.cvcInputId + '-validation').prop('class', 'login-valid');
                    }
                } else if (!($('#' + settings.cvcInputId).val() == "" || /^\d{3,4}$/.test($('#' + settings.cvcInputId).val()))) {
                    isValid = false;
                    $('#' + settings.cvcInputId + '-validation').prop('class', 'login-invalid');
                } else {
                    $('#' + settings.cvcInputId + '-validation').prop('class', 'login-valid');
                }
            }
            //validate bindings
            if ($('#' + settings.bindingPaymentButton).is(':visible') &&
                $('#bindingIdSelect [selected="selected"]').data('ismaestro') &&
                ($('#' + settings.cvcInputId).val() == "" || /^\d{3,4}$/.test($('#' + settings.cvcInputId).val()))) { //Validate if bindings
                isValid = true;
                $('#' + settings.panInputId + '-validation').prop('class', 'login-valid');
                $('#' + settings.cvcInputId + '-validation').prop('class', 'login-valid');
            } else if ($('#' + settings.bindingPaymentButton).is(':visible')) {
                $('#' + settings.panInputId + '-validation').prop('class', 'login-valid');
                if (!/^\d{3,4}$/.test($('#' + settings.cvcInputId).val())) {
                    isValid = false;
                    $('#' + settings.cvcInputId + '-validation').prop('class', 'login-invalid');
                } else {
                    isValid = true;
                    $('#' + settings.cvcInputId + '-validation').prop('class', 'login-valid');
                }
            }

            if ($('#' + settings.agreementCheckboxId).is(':visible')) {
                if ($('#' + settings.agreementCheckboxId).prop('checked')) {
                    $('#' + settings.agreementCheckboxId + '-validation').prop('class', 'login-valid');
                } else {
                    isValid = false;
                    $('#' + settings.agreementCheckboxId + '-validation').prop('class', 'login-invalid');
                }
            }

            if (settings.emailEnabled) {
                if (!$('#' + settings.emailInputId).is(':visible') ||
                     $('#' + settings.emailInputId).val().split(" ").join("") == "" ||
                     /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/.test($('#' + settings.emailInputId).val())) {
                    $("#" + settings.emailInputId + '-validation').prop('class', 'login-invalid');
                    $("#" + settings.emailInputId + '-validation').prop('class', 'login-valid');
                } else {
                    isValid = false;
                    $("#" + settings.emailInputId + '-validation').prop('class', 'login-valid');
                    $("#" + settings.emailInputId + '-validation').prop('class', 'login-invalid');
                }
            }

            if (settings.phoneEnabled) {
                if ($('#' + settings.phoneInputId).val().split(" ").join("") == "" ||
                    /^((8|\+7)[\- ]?)?(\(?\d{3}\)?[\- ]?)?[\d\- ]{7}$/.test($('#' + settings.phoneInputId).val())) {
                    $("#" + settings.phoneInputId + '-validation').prop('class', 'login-invalid');
                    $("#" + settings.phoneInputId + '-validation').prop('class', 'login-valid');
                } else {
                    isValid = false;
                    $("#" + settings.phoneInputId + '-validation').prop('class', 'login-valid');
                    $("#" + settings.phoneInputId + '-validation').prop('class', 'login-invalid');
                }
            }

            methods.switchActions(isValid);
            return isValid;
        },
        getTypeCard:function() {
            if (properties.loyalty) {
                if (properties.validCard) {
                    methods.getAvailableLoyaltyForPan();
                } else {
                    $('#' + settings.sliderBlock).hide();
                }
             }
        },
        showProgress:function () {
            $('#errorBlock').empty();
            $('#indicator').show();
        },
        hideProgress:function () {
            $('#indicator').hide();
        },
        showError:function (message) {
            methods.hideProgress();
            $('#errorBlock').empty();
            $('#errorBlock').prepend('<p class="errorField" id="loginError">' + message + "</p>");
        },
        redirect:function (destination, message) {
            if (message) {
                $('#infoBlock').empty();
                $('#infoBlock').prepend('<p>' + message + "</p>");
            }
            $('#numberCountdown').hide();
            $('#errorBlock').empty();
            $('#formPayment').prop('expired', '1');
            methods.switchActions(false);
            document.location = destination;
        },
        startCountdown: function (remainingSecs) {
            $(document).oneTime(remainingSecs * 1000, function () {
                $('#formPayment').prop('expired', '1');
                methods.validate();
            });

            $('#numberCountdown').everyTime(1000, function (i) {
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
        setupBindingForm:function (data) {
            if (data['bindingEnabled'] == true && settings.bindingCheckboxEnabled == true) {
                properties.isBindingEnabled = true;
                $('#bindingBlock').show();
            } else {
                properties.isBindingEnabled = false;
                $('#bindingBlock').hide();
            }

            var bindingForm = $('#formBinding');
            var bindingItems = data['bindingItems'];
            if (bindingForm.length == 0) {
                // Page template does not support bindings
                return;
            }
            if (typeof bindingItems === 'undefined') {
                // No bindings for this order
                bindingForm.hide();
                return;
            }
            methods.checkControl('#buttonBindingPayment');

            // Build binding select control
            var bindingSelect = bindingForm.find('select[name=bindingId]');
            if (bindingSelect.length != 1) {
                alert('Binding selector not found');
            }
            for (var i = 0; i < bindingItems.length; i++) {
                var o = $('<option value="' + bindingItems[i].id + '" data-paymentSystem="' + bindingItems[i].paymentSystem + '" data-ismaestro="' +  bindingItems[i].isMaestro + '">' + bindingItems[i].label + '</option>');
                bindingSelect.append(o);
            }

            var hiddenNodes = bindingForm.find('.rbs_hidden');
            bindingSelect.change(function () {
                hiddenNodes.toggle($(this).val() != '');
                if ($(this).val() != '' ){
                    methods.getAvailableLoyaltyForBinding();
                }
            });
            $('#buttonBindingPayment').bind('click', function () {
                bindingForm.submit();
                return false;
            });
            bindingForm.bind('submit', methods.sendBindingPayment);

            bindingForm.show();
            hiddenNodes.hide();
        },
        sendBindingPayment:function () {
            methods.showProgress();
            var orderId = properties.orderId,
                bindingForm = $('#formBinding'),
                addParams = methods.getAdditionalParams('formBinding'),
                // phoneNum = $('#' + settings.phoneInputId).val().replace(/\D/g, "");
                paymentData = {
                    'orderId':orderId,
                    'bindingId':bindingForm.find('select[name=bindingId]').val(),
                    'cvc':bindingForm.find('input[name=cvc]').val(),
                    'email':$('#' + settings.emailInputId).is(':visible') ? $("#" + settings.emailInputId).val() : null,
                    // 'phone':phoneNum,
                    'jsonParams':addParams
                };
            if (($("#" + settings.pointPenny).val() > 0)) {
                paymentData.loyaltyId = $("#" + settings.pointPenny).prop('id');
                paymentData.pointsAmount = $("#" + settings.pointPenny).val();
            }
            $.ajax({
                url:settings.paymentBindingAction,
                type:'POST',
                cache:false,
                data: paymentData,
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
                        methods.redirect(data['redirect'], data['info'], settings.messageRedirecting);
                    }
                    return true;
                }
            });
            return false;
        },
        getSessionStatus:function (informRbsOnLoad) {
            methods.showProgress();
            var orderId = properties.orderId;
            $.ajax({
                url:settings.getSessionStatusAction,
                type:'POST',
                cache:false,
                data:({
                    MDORDER:orderId,
                    language:settings.language,
                    informRbsOnLoad:informRbsOnLoad,
                    pageView:settings.pageView,
                    paramNames:settings.paramNames
                }),
                dataType:'json',
                error:function () {
                    methods.showError(settings.messageAjaxError);
                },
                success:function (data) {
                    methods.hideProgress();
                    if ('feeEnabled' in data) {
                        settings.getFeeEnabled = data['feeEnabled'];
                        if (settings.getFeeEnabled) {
                            methods.getFee();
                        }
                    }
                    if ('cvcNotRequired' in data && data['cvcNotRequired'] == true) {
                        properties.cvcValidationRequired = false;
                    }
                    if ('otherWayEnabled' in data && data['otherWayEnabled'] == true) {
                        $('#buttonPaymentSbrf').toggle(true);
                    }
                    if ('bindingDeactivationEnabled' in data) {
                        if (data['bindingDeactivationEnabled'] == false){
                            $('#delete-binding').remove();
                        }
                    }
                    if ('redirect' in data) {
                        methods.redirect(data['redirect'], settings.messageRedirecting);
                    } else {
                        if ('error' in data) {
                            methods.showError(data['error']);
                        }
                        settings.updatePage(data);
                        if ('loyaltyServices' in data) {
                            properties.loyalty = true;
                        }
                        var remainingSecs = data['remainingSecs'];
                        if (remainingSecs > 0) {
                            methods.startCountdown(remainingSecs);
                            methods.setupBindingForm(data);
                            methods.setupAgreementBlock(data);
                        } else {
                            methods.redirect(settings.showErrorAction + '?pageView=' + settings.pageView, settings.messageRedirecting);
                        }
                    }
                    settings.onReady();
                    return true;
                }
            });
        },
        getFee: function () {
            if (!settings.getFeeEnabled) {
                return;
            }
            $("#" + settings.feeChecked).val(false);
            methods.showProgress();
            var orderId = properties.orderId;
            $.ajax({
                url: settings.getFeeAction,
                type: 'POST',
                cache: false,
                data: ({
                    mdOrder: orderId,
                    pan: '1'
                }),
                dataType: 'json',
                error: function () {
                    methods.showError(settings.messageAjaxError);
                },
                success: function (data) {
                    methods.hideProgress();
                    if (data['errorCode'] == 0) {
                        $("#" + settings.feeBlock).show();
                        $("#" + settings.feeAmount).text(data['fee']);
                        if (properties.isAgreementEnabled) {
                            $("#" + settings.agreementBlock).show();
                            settings.agreementCheckboxEnabled = true;
                        }
                    } else {
                        $("#" + settings.feeBlock).hide();
                    }
                    return true;
                }
            });
        },
        getAvailableLoyaltyForPan: function(){
            var pan = $("#" + settings.panInputId).val();
            methods.getAvailableLoyalty(pan, null);
        },
        getAvailableLoyaltyForBinding: function(){
            var bindingId = $('#formBinding').find('select[name=bindingId]').val();
            methods.getAvailableLoyalty(null, bindingId);
        },
        getAvailableLoyalty:function (pan, bindingId) {
            methods.showProgress();
            var orderIdNumber = properties.orderId;
            var data = {orderId : orderIdNumber};
            if (pan){
                data.pan = pan;
            } else if (bindingId) {
                data.bindingId = bindingId
            }
            $.ajax({
                url:settings.getAvailableLoyalty,
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
        isMaestroCard:function () {
            if (methods.maestroCheck.pan != $("#" + settings.panInputId).val()) { // Вызов из-за изменения PAN-а, не из-за submit-а.
                $.ajax({
                    url:settings.isMaestroCardAction,
                    type:'POST',
                    cache:false,
                    data:({
                        pan:$("#" + settings.panInputId).val()
                    }),
                    dataType:'json',
                    error:function () {
                        methods.showError(settings.messageAjaxError);
                    },
                    success:function (data) {
                        data = JSON.parse(data);
                        if ('error' in data) {
                            methods.showError(data['error']);
                        } else {
                            methods.maestroCheck.pan = $("#" + settings.panInputId).val(); // Запомним результат вызова для заданного PAN-а, чтобы не дёргать AJAX зря.
                            methods.maestroCheck.result = data["isMaestro"];
                            var isValid = true; // В данном случае isMaestroCard() не вернёт значения, поэтому проведём остаток валидации здесь.
                            if (methods.maestroCheck.result) { // Это Маэстро.
                                $("#cvcMessage").show();
                                if (!($('#' + settings.cvcInputId).val() == "" || /^\d{3,4}$/.test($('#' + settings.cvcInputId).val()))) { // CVC можно не задавать, но если уж задали, то он должен быть правильной длины.
                                    isValid = false;
                                    $('#' + settings.cvcInputId + '-validation').prop('class', 'login-invalid');
                                } else {
                                    $('#' + settings.cvcInputId + '-validation').prop('class', 'login-valid');
                                }
                            } else { // Это не Маэстро - проверим длину CVC как обычно.
                                $("#cvcMessage").hide();
                                    if (properties.cvcValidationRequired) {
                                        if (!/^\d{3,4}$/.test($('#' + settings.cvcInputId).val())) {
                                            isValid = false;
                                            $('#' + settings.cvcInputId + '-validation').prop('class', 'login-invalid');
                                        } else {
                                            $('#' + settings.cvcInputId + '-validation').prop('class', 'login-valid');
                                        }
                                    } else if (!($('#' + settings.cvcInputId).val() == "" || /^\d{3,4}$/.test($('#' + settings.cvcInputId).val()))) {
                                        isValid = false;
                                        $('#' + settings.cvcInputId + '-validation').prop('class', 'login-invalid');
                                    } else {
                                        $('#' + settings.cvcInputId + '-validation').prop('class', 'login-valid');
                                    }
                            }
                            methods.switchActions(isValid); // Включим или выключим кнопку "Оплатить".
                        }
                    }
                });
                return; // Т.к. methods.maestroCheck.result всё равно не успеет обновиться, а результат вызова функции validate() сейчас не нужен, т.к. это не submit.
            }
            return methods.maestroCheck.result; // Вызов не из-за изменения PAN-а - submit или изменение другого поля. Если это submit результат пригодится.
        },
        sendPayment:function () {
            methods.showProgress();
            var orderId = properties.orderId,
                bindingNotNeeded = settings.bindingCheckboxEnabled && !$("#" + settings.bindingCheckBoxId).prop("checked"),
                addParams = methods.getAdditionalParams(settings.paymentFormId),
                // phoneNum = $('#' + settings.phoneInputId).val().replace(/\D/g, ""),
                paymentData = {
                    MDORDER:orderId,
                    $EXPIRY:$("#expiry").prop("value"),
                    $PAN:$("#" + settings.panInputId).val(),
                    MM:$("#" + settings.monthSelectId).val(),
                    YYYY:$("#" + settings.yearSelectId).val(),
                    TEXT:$("#" + settings.cardholderInputId).val(),
                    $CVC:$("#" + settings.cvcInputId).val(),
                    language:settings.language,
                    email: $('#' + settings.emailInputId).is(':visible') ? $("#" + settings.emailInputId).val() : null,
                    // phone: phoneNum,
                    bindingNotNeeded: bindingNotNeeded,
                    jsonParams:addParams
                };
            if (($("#" + settings.pointPenny).val() > 0)) {
                paymentData.loyaltyId = $("#" + settings.pointPenny).prop('id');
                paymentData.pointsAmount = $("#" + settings.pointPenny).val();
            }
            $.ajax({
                url:settings.paymentAction,
                type:'POST',
                cache:false,
                data: paymentData,
                dataType:'json',
                error:function () {
                    methods.showError(settings.messageAjaxError);
                    methods.switchActions(true);
                    return true;
                },
                success:function (data) {
                    methods.hideProgress();
                    methods.switchActions(true);
                    if (data['acsUrl'] != null) {
                        methods.redirectToAcs(data);
                    } else if ('error' in data) {
                        methods.showError(data['error']);
                    } else if ('redirect' in data) {
                        methods.redirect(data['redirect'], data['info'], settings.messageRedirecting);
                    }
                    return true;
                }
            });
        },

        getAdditionalParams:function (paymentFormId) {
            var jsonParams = '{';
            $("#" + paymentFormId + " input[name*='" + settings.paramPrefix + "']").each(function (index, element) {
                jsonParams += '"' + element.id.substring(settings.paramPrefix.length) + '":"' + element.value + '",'
            });
            if (jsonParams.length > 1) {
                jsonParams = jsonParams.substr(0, jsonParams.length - 1);
            }
            jsonParams += "}";
            return jsonParams;
        },

        sendPaymentSbrf:function () {
            methods.showProgress();
            var orderId = properties.orderId;
            $.ajax({
                url:settings.paymentAction,
                type:'POST',
                cache:false,
                data:({
                    MDORDER:orderId,
                    paymentWay:'SBRF_SBOL'
                }),
                dataType:'json',
                error:function () {
                    methods.showError(settings.messageAjaxError);
                    methods.switchActions(true);
                    return true;
                },
                success:function (data) {
                    methods.hideProgress();
                    methods.switchActions(true);
                    if (data['acsUrl'] != null) {
                        methods.redirectToAcs(data);
                    } else if ('error' in data) {
                        methods.showError(data['error']);
                    } else if ('redirect' in data) {
                        methods.redirect(data['redirect'], data['info'], settings.messageRedirecting);
                    }
                    return true;
                }

            });
        },
        deactiveBinding: function() {
            methods.showProgress();
            var orderId = properties.orderId,
                bindingForm = $('#formBinding');
            $.ajax({
                url: settings.unbindCard,
                type: 'POST',
                cache: false,
                data: {
                    'mdOrder': orderId,
                    'bindingId': bindingForm.find('select[name=bindingId]').val(),
                },
                dataType: 'json',
                error: function () {
                    methods.hideProgress();
                    methods.showError(settings.messageAjaxError);
                    return true;
                },
                success: function (data) {
                    methods.hideProgress();
                    if (data['errorCode'] == 0) {
                        $("#bindingIdSelect option:selected").remove();
                        $("#combobox option:selected").remove();
                        $("#combobox").val('other').change();
                        $("#delete-binding").hide();
                        $(document).trigger("deactivedBinding");
                    } else {
                        methods.showError(settings.messageAjaxError);
                    }
                    return true;
                }
            });
        },
        redirectToAcs: function (data) {
            $('#acs').prop('action', data['acsUrl']);
            $('#PaReq').val(data['paReq']);
            $('#MD').val(properties.orderId);
            $('#TermUrl').val(data['termUrl']);
            $('#acs').submit();
        }
    };

    $.fn.getPaymentProperty = function (name) {
        return properties[name];
    }

    $.fn.payment = function (method) {
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

jQuery.fn.log = function (msg, type) {
    if (typeof lastSelector == 'undefined') {
        lastSelector = '--';
    }

    if (lastSelector != this.selector.slice(0, lastSelector.length)) {
        if (lastSelector != '--') {
            console.groupEnd();
            lastSelector = '--';
        }
        else {
            lastSelector = this.selector;
        }
        console.group("%s (%s)", msg, this.selector);
    }

    if (type == undefined) {
        type = "log";
    }
    switch (type) {
        case "log":
            console.log(this);
            break;
        case "warn":
            console.warn(this);
            break;
        case "info":
            console.info(this);
            break;
        case "error":
            console.error(this);
            break;
        case "time":
            console.time(msg);
            break;
        case "timestop":
            console.timeEnd(msg);
            break;
        case "profile":
            console.profile(msg);
            break;
        case "profilestop":
            console.profileEnd(msg);
            break;
    }
    return this;
};

/**
 * Is pressed key a number key
 * @param keyCode
 */
function isNumber(keyCode) {
    return ( keyCode > 47 && keyCode < 58 );
}

function isControl(keyCode) {
    return keyCode in {8:1, 9:1, 13:1, 16:1, 17:1, 18:1, 20:1, 37:1, 38:1, 39:1, 40:1, 45:1, 46:1};
}

function isChar(keyCode) {
    return ( keyCode > 96 && keyCode < 133 ) || (keyCode > 64 && keyCode < 91);
}

function isSpace(keyCode) {
    return keyCode ==
        32;
}

function isDot(keyCode) {
    return keyCode == 46 || keyCode == 39;
}

function luhn(num) {
    num = (num + '').replace(/\D+/g, '').split('').reverse();
    if (!num.length)
        return false;
    var total = 0, i;
    for (i = 0; i < num.length; i++) {
        num[i] = parseInt(num[i]);
        total += i % 2 ? 2 * num[i] - (num[i] > 4 ? 9 : 0) : num[i];
    }
    return (total % 10) == 0;
}
/**
 * Keyboard specific transliteration
 */
var keys = {
    "Й": "Q", "Ц": "W", "У": "E", "К": "R", "Е": "T", "Н": "Y", "Г": "U", "Ш": "I", "Щ": "O", "З": "P",
    "Ф": "A", "Ы": "S", "В": "D", "А": "F", "П": "G", "Р": "H", "О": "J", "Л": "K", "Д": "L", "Я": "Z",
    "Ч": "X", "С": "C", "М": "V", "И": "B", "Т": "N", "Ь": "M", "й": "q", "ц": "w", "у": "e", "к": "r",
    "е": "t", "н": "y", "г": "u", "ш": "i", "щ": "o", "з": "p", "ф": "a", "ы": "s", "в": "d", "а": "f",
    "п": "g", "р": "h", "о": "j", "л": "k", "д": "l", "я": "z", "ч": "x", "с": "c", "м": "v", "и": "b",
    "т": "n", "ь": "m"
};

function transliterate(word) {
    return word.split('').map(function (char) {
        return keys[char] || char;
    }).join("");
}

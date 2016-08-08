$(function() {
    $('#year').val('');
    $('#month').val('');
    var settings = {
        pan: $('#iPAN_sub'),
        panInputId: $('#iPAN'),
        monthInput: $('#input-month'),
        yearInput: $('#input-year'),
        typeCardIcon: $('#typecard-icon')
    };
    var methods = {
        correctNumber: function(event) {
            var elem = $(event.target);
            elem.val(elem.val().replace(/[^0-9]/g, ""));
        },
        correctFocus: function(event) {
            var elem = $(event.target);
            if (elem.val().length == elem.attr('maxlength') && settings.yearInput.val().length == 0) {
                settings.yearInput.focus();
            }
        },
        correctMonth: function(event) {
            var elem = $(event.target);
                firstChar = elem.val().charAt(0);
            if (elem.val() > 1 && firstChar !== '0' && firstChar !== '1') {
                elem.val('0' + elem.val());
            }
            if (elem.val() > 12) {
                elem.val(12);
            }
            elem.focusout(function() {
                if (elem.val() == '1') {
                    elem.val('01');
                }
                if (elem.val() == '') {
                    elem.parent('#year-validation').removeClass('login-valid');
                    elem.parent('#year-validation').addClass('login-invalid');
                }
            });
        },
        syncMonth: function(event) {
            var elem = $(event.target);
            $('#month').val(elem.val());
            $('#month').trigger('change');
        },
        syncYear: function(event) {
            var elem = $(event.target);
            $('#year').val('20' + elem.val());
            $('#year').trigger('change');
        },
        detectTypeCard: function(event) {
            var hidden_elem = settings.panInputId;
            if(/^4[0-9]{12}([0-9]{3})?$/.test(hidden_elem.val())) {
                settings.typeCardIcon.removeClass();
                settings.typeCardIcon.addClass("VISA");
            } else if (/^(5|6)[0-9]{15}([0-9]{3})?/.test(hidden_elem.val())) { // Maestro or MC?
                methods.isMaestroCard();
            } else {
                settings.typeCardIcon.removeClass();
            }
        },
        isMaestroCard: function() {
            $.ajax({
                url: '../../rest/isMaestroCard.do',
                type: 'POST',
                cache: false,
                data: ({
                    pan: settings.panInputId.val()
                }),
                dataType: 'json',
                error: function () {
                    methods.showError(settings.messageAjaxError);
                },
                success: function (data) {
                    data = JSON.parse(data);
                    if ('error' in data) {
                        methods.showError(data['error']);
                    } else {
                        if (data["isMaestro"]) {
                            settings.typeCardIcon.removeClass();
                            settings.typeCardIcon.addClass("MAESTRO");
                        } else {
                            settings.typeCardIcon.removeClass();
                            settings.typeCardIcon.addClass("MASTERCARD");
                        }

                    }
                }
            });
        }
    };
    //bindes
    $(document).on('keyup input paste', settings.pan.selector, function(event) {
        methods.detectTypeCard();
    });
    $(document).on('keyup', settings.monthInput.selector, function(event) {
        setTimeout(function() {
            methods.correctMonth(event);
            methods.correctNumber(event);
            methods.syncMonth(event);
            methods.correctFocus(event);
        }, 0);
    });
    $(document).on('keydown input paste', settings.yearInput.selector, function(event) {
        setTimeout(function() {
            methods.correctNumber(event);
            methods.syncYear(event);
        }, 0);
    });

    // CVV tooltip
    $('.hint').click(function () {
        $(this).next('.hint-baloon').toggleClass('active');
        return false;
    });
    $(document).click(function (event) {
        if ($(event.target).parent('.hint-baloon').length) {
            return false;
        } else {
            $('.hint-baloon').removeClass('active');
        }
        event.stopPropagation();
    });

    // Checkbox email
    $("#showEmail").change(function() {
      if ($('#showEmail').prop('checked')) {
        $('#emailBlock').show();
      } else {
        $('#emailBlock').hide();
      }
    });
});





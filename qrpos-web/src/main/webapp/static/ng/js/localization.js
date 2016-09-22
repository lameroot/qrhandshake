var localizations_internal = {
    "orderNumber":{"ru":"Номер заказа","en":"Order number"},
    "authCode":{"ru":"Код авторизации","en":"Approval Code"},
    "cardholderName":{"ru":"Имя владельца","en":"Cardholder Name"},
    "terminal":{"ru":"Терминал ID","en":"Terminal"},
    "cardNumber":{"ru":"Номер карты","en":"Card number"},
    "total":{"ru":"Сумма платежа","en":"Total"},
    "expiry":{"ru":"Срок действия карты","en":"Card expiry date"},
    "paymentDesc":{"ru": "Описание заказа", "en": "Order description"},
    "paymentFailure":{"ru": "Оплата отклонена", "en": "Payment declined"},
    "fee":{"ru": "Коммиссия за платеж", "en": "Fee"},
    "rejectionReason":{"ru": "Причина отклонения:", "en": "Reason for rejection:"},
    "paymentThanks":{"ru": "Спасибо за оплату!", "en": "Thank you for payment!"},
    "backToShop":{"ru": "Вернуться в магазин", "en": "Back to Shop"},
    "aboutCard":{"ru": "Информация о карте", "en": "Card Information"},
    "_title":{"ru": "Сбербанк - результат платежа", "en": "Sberbank - payment result"}
};
var localizations_external = {};
var localization_language = $.url.param("language") || $.url.param("lang");
if (localization_language == "") {
    var url = document.location.href.toLowerCase();
    if (~url.indexOf('payment_')) {
        var start = url.indexOf(payment);
        localization_language = url.substr(start + payment.length, 2);
    } else {
        localization_language = 'ru';
    }
}


// Translate title
if (localizations_internal['_title']) {
    document.title = localizations_internal['_title'][localization_language];
}

function getLocalizedText(label){
    if (localizations_external[label]){
        if (localizations_external[label][localization_language]){
            return localizations_external[label][localization_language];
        }
    }
    else if (localizations_internal[label]){
        if (localizations_internal[label][localization_language]){
            return localizations_internal[label][localization_language]
        }
    }
    console.log("ERROR! Can`t find localized message!");
    return "";
}

function localizePage(localizations){
    localizations_external = localizations;
    if (localization_language == "ru") {
        return; //don't translate page if choose russian language
    }
    $(document).ready(function(){
        for (var label in localizations_internal) {
            if (localizations_internal[label][localization_language]){
                if ($('[langLbl="'+label+'"]').length != 0) {
                    $('[langLbl="'+label+'"]').html(localizations_internal[label][localization_language]);
                } else if ($('[langPlchdr="'+label+'"]').length) {
                    $('[langPlchdr="'+label+'"]').attr('placeholder', localizations_internal[label][localization_language]);
                }
            }
        }
        for (var label in localizations_external) {
            if (localizations_external[label][localization_language]){
                if ($('[langLbl="'+label+'"]').length != 0) {
                    $('[langLbl="'+label+'"]').html(localizations_external[label][localization_language]);
                } else if ($('[langPlchdr="'+label+'"]').length) {
                    $('[langPlchdr="'+label+'"]').attr('placeholder', localizations_external[label][localization_language]);
                }
            }
        }
        if (typeof $('select').selectric == 'function') {
            $('.selectric').selectric('init');
        }
    });
}
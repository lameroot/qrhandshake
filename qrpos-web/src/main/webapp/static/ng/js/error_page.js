$(document).ready(function () {
    //jquery.url set values from request GET params
    errorCode = $.url.param('error');

    var errors_en = {
        'payment.errors.order_already_processed': 'Order was processed already',
        'payment.errors.session_expired': 'Payment session already expired',
        'payment.errors.internal_error': 'Internal error happened. Please try again later',
        'default': 'Sorry, payment cannot be complete'
    };
    var errors_ru = {
        'payment.errors.order_already_processed': 'Заказ уже обработан',
        'payment.errors.session_expired': 'Сессия оплаты уже истекла',
        'payment.errors.internal_error': 'Ошибка сервиса. Пожалуйста попробуйте позднее',
        'default': 'Извините, оплата не может быть завершена'
    };

    var errors;
    if (lang == 'ru') {
        errors = errors_ru;
    } else {
        errors = errors_en;
    }
    var message;
    if (errorCode in errors) {
        message = errors[errorCode];
    } else {
        message = errors['default'];
    }
    $('#errorBlock').prepend('<p class="errorField">' + message + "</p>");
});
$(document).ready(function(){
    
    var orderId = location.search.split('orderId=')[1];
    $('#orderId').text(orderId);
    
    $.ajax({
        url: "order/sessionStatus",
        type: 'POST',
        data: {
            orderId: orderId
        }
    }).done(function(result) {
        if (result.orderStatus == 'PAID') {
            $('#payBtn').attr("disabled", true);
            $('#state').addClass('successful');
            $('#state').text('Заказ уже оплачен');
        }
        $('#amount').text(result.amount / 100);
    }).fail(function (data) {
        $('#payBtn').attr("disabled", true);
        $('#state').addClass('error');
        $('#state').text('Ошибка связи с сервером');
    });
    
    $("#payBtn").click(function(){
        $('#state').addClass('attention');
        $('#state').text('Ожидание ответа');
        $('#payBtn').attr("disabled", true);
		$.ajax({
			url: "order/payment",
			type: 'POST',
			data: {
				orderId: orderId,
				paymentWay: 'card',
				pan: $('#pan').val(),
				cvc: $('#cvc').val(),
				month: $('#month').val(),
				year: $('#year').val(),
				cardHolderName: $('#cardHolderName').val()
			}
		}).done(function(result) {
            if (result.orderStatus == 'PAID') {
                $('#state').addClass('successful');
                $('#state').text('Успешная оплата');
            } else {
                $('#state').addClass('error');
                $('#state').text('Ошибка оплаты');
            }
        }).fail(function (data) {
            $('#state').addClass('error');
            $('#state').text('Ошибка оплаты');
        });
	});

});
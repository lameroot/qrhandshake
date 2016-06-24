$(document).ready(function(){

	$("#payBtn").click(function(){
		$.ajax({
			url: "qwe.ddns.net/qrapi/order/payment",
			type: 'POST',
			data: {
				orderId: location.search.split('orderId=')[1],
				paymentWay: 'card',
				pan: $('#pan').val(),
				cvc: $('#cvc').val(),
				mont: $('#mont').val(),
				year: $('#year').val(),
				cardHolderName: $('#cardHolderName').val()
			}
		}).done(function(result) {
			debugger;
			console.log(result);
		});

	});

});
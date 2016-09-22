(function ($) {

    $.fn.paymentFinished = function (config) {

        function loadOrderInfo() {

            orderId = $.url.param("orderId");
            language = $.url.param("lang");

            $.ajax({
                url: '../../rest/getFinishedPaymentInfo.do',
                type: 'POST',
                cache: false,
                //processData:false,
                data: {orderId: orderId, language: language},
                dataType: 'json',
                error: function () {
                    console.error("Failed to send trace")
                },
                success: function (data) {
                    if (data.status == "DEPOSITED" || data.status == "APPROVED") {
                        fillElements(data);
                    } else if (data.status == "DECLINED" || data.status == "CREATED"){
                        fillElements(data);
                        showError(data);
                    }
                }
            });
        }

        function fillElements(data) {
            $("#merchantFullName").html(data.merchantFullName);
            $("#merchantUrl").html(data.merchantUrl);
            $("#amount").html(data.formattedAmount);
            $("#orderNumber").html(data.orderNumber);
            $("#approvalCode").html(data.approvalCode);
            $("#refNum").html(data.refNum);
            $("#terminalId").html(data.terminalId);
            $("#panMasked").html(data.panMasked);
            $("#cardholderName").html(data.cardholderName);
            $("#expiry").html(data.expiry);
            $("#merchantLogo").attr("src", "../" + data.merchantShortName + "logo.png");
            $("#mobileMerchantLogo").attr("src", "../" + data.merchantShortName + "logo.png");
            $(".back-btn").attr('href', checkUrl(data.backUrl));
            $(".pdf").attr('href', "../../rest/finishcheck.do?mdOrder="+orderId);

            if ((data.feeAmount == "") || (data.feeAmount == "null") ||
                data.feeAmount == "0") {
                $("#feeAmount").closest(".row").hide();
            } else {
                $("#feeAmount").html(data.formattedFeeAmount);
            }

            if (data.currency !== "810" &&  data.currency !== "643") {
                detectCurrency(data.currency);
            }

            if ((data.orderDescription == "") || (data.orderDescription == "null")) {
                $("#descriptionTitle").hide();
                $("#orderDescription").hide();
            } else {
                $("#orderDescription").html(data.orderDescription);
            }
            var paymentDate = data.paymentDate;
            var posMili = paymentDate.indexOf(".");
                paymentDate = paymentDate.slice(0, posMili);
            $("#paymentDate").html(paymentDate);

            formatAmount($("#amount"));
            formatAmount($("#feeAmount"));
            formatPan($("#panMasked"));
            formatDate($("#expiry"));

            isLogoImg($("#merchantLogo"));
        }

        function detectCurrency(key) {
            switch (key) {
              case "978":
                $("#currencyAmount").html("&#8364;");
                $("#currencyFee").html("&#8364;");
                break;
              case "840":
                $("#currencyAmount").html("$");
                $("#currencyFee").html("$");
                break;
              case "392":
                $("#currencyAmount").html("JPY");
                $("#currencyFee").html("JPY");
                break;
              case "980":
                $("#currencyAmount").html("UAH");
                $("#currencyFee").html("UAH");
                break;
              case "826":
                $("#currencyAmount").html("GBP");
                $("#currencyFee").html("GBP");
                break;
              case "156":
                $("#currencyAmount").html("CNY");
                $("#currencyFee").html("CNY");
                break;
              case "946":
                $("#currencyAmount").html("RON");
                $("#currencyFee").html("RON");
                break;
              case "974":
                $("#currencyAmount").html("BYR");
                $("#currencyFee").html("BYR");
                break;
              case "398":
                $("#currencyAmount").html("KZT");
                $("#currencyFee").html("KZT");
                break;
              case "417":
                $("#currencyAmount").html("KGS");
                $("#currencyFee").html("KGS");
                break;
              case "512":
                $("#currencyAmount").html("OMR");
                $("#currencyFee").html("OMR");
                break;
              default:
                $("#currencyAmount").html("руб.");
                $("#currencyFee").html("руб.");
            }
        }

        function showError(data) {
            $("#thankyou").hide();
            $("#errorTitle").show();
            $("#actionCodeDescription").html(data.actionCodeDescription);
        }

        function isLogoImg(logodiv){
            var img = new Image();
            img = logodiv;
            img.error(function(){
                logodiv.remove();
            });
        }

        function checkUrl(url) {
            if (!/[;<>,]/g.test(url)) {
                return url;
            } else {
                $(".back-btn").hide();
                console.warn("Некорректный backUrl");
                return false;
            }
        }

        function formatAmount(item){
            var value = item.html();
            value += "";
            var x = value.split(".");
            var x1 = x[0];
            var x2 = x.length > 1 ? "." + x[1] : "";
            var rgx = /(\d+)(\d{3})/;
            while (rgx.test(x1)) {
                x1 = x1.replace(rgx, "$1" + " " + "$2");
            }
            item.html(x1 + x2);
        }

        function formatPan(item) {
            var value = item.html();
            var number = new Array(12+1).join('*') + value.slice(-4);
            var formatValue = number.replace(/([\d\W\*]{4}(?!$))/g,'$1 ');
            $("#panMasked").html(formatValue);
        }

        function formatDate(item) {
            var value = item.html();
            var date = value.replace(/\//g, ".");
            $("#expiry").html(date);
        }

        $(document).ready(function(){
            loadOrderInfo();
        });

        window.onbeforeunload = function(e) {
            sendTrace();
        };

    };


}(jQuery));
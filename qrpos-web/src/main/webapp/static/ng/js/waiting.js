function urlParam(param) {
    var pageURL = window.location.search.substring(1);
    var variables = pageURL.split('&');

    for (var i = 0; i < variables.length; i++) {
        var parameterName = variables[i].split('=');
        if (parameterName[0] == param) {
            return parameterName[1];
        }
    }
}

$(function() {
    var timer = setInterval(function() {
         var mdOrder = urlParam('mdOrder');

         $.ajax({
            type: 'POST',
            dataType: 'json',
            url: "../../rest/finish3dsAwait.do",
            data: {mdOrder: mdOrder},
            dataType:'json',
            success: function(data) {
                if (data.ready) {
                    window.location.href = data.url;
                    clearInterval(timer);
                }
            },
            error: function() {

            }
        });
    }, 5000);
});
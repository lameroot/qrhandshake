// include jquery and jquery.payment before here

$(function() {

    var validateDetails = function() {
        // set variables for the expiry date validation, cvc validation and expiry date 'splitter'
        var expiry = $('.cc-exp').payment('cardExpiryVal');
        var validateExpiry = $.payment.validateCardExpiry(expiry["month"], expiry["year"]);
        var validateCVC = $.payment.validateCardCVC($('.cc-cvc').val());
        // if statement on whether the card’s expiry is valid or not
        if (validateExpiry) {
            // if the expiry is valid add the identified class
            $('.cc-exp').addClass('identified');
        } else {
            // remove again if the expiry becomes invalid
            $('.cc-exp').removeClass('identified');
        }
        // if statement on whether the card’s cvc is valid or not
        if (validateCVC) {
            // if the cvc is valid add the identified class
            $('.cc-cvc').addClass('identified');
        } else {
            // remove again if the cvc becomes invalid
            $('.cc-cvc').removeClass('identified');
        }
    }
    // this runs the above function every time stuff is entered into the card inputs
    $('.paymentInput').bind('change paste keyup', function() {
        validateDetails();
    });

});

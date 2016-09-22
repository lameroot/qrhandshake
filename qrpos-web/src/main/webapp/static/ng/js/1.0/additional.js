var bindings = [],
    bindingsEnabled = false,
    OTHER_TEXT = "Другая карта";

$(document)  //Validation pan
    .on('keyup paste', '#iPAN_sub', function(event){
        var key = window.event ? event.keyCode : event.which;
        var inputPAN = $(this);
        if (key == $.ui.keyCode.DOWN || key == $.ui.keyCode.UP || key == $.ui.keyCode.ENTER || ($("#combobox option").length > 0 && $("#combobox").val() !== 'false')) {
            inputPAN.val(inputPAN.val().replace(/[^\d\*]/g, '').replace(/([\d\*]{4}(?!$))/g,'$1 '));
        } else if (key == $.ui.keyCode.BACKSPACE || key == $.ui.keyCode.DELETE || key == $.ui.keyCode.LEFT || key == $.ui.keyCode.RIGHT) {
            return true;
        } else {
            inputPAN.val(inputPAN.val().replace(/[^\d]/g, '').replace(/(\d{4}(?!$))/g,'$1 '));
        }
    })
    .on('autocompletechange', '#iPAN_sub', function(){
        var inputPAN = $(this);
        inputPAN.val(inputPAN.val().replace(/[^\d\*]/g, '').replace(/([\d\*]{4}(?!$))/g,'$1 '));
    })
    .on('deactivedBinding', function() {
        initBindings();
    });

function initMain () { //init combobox
    $('#dummy-pan').hide();
    $("#combobox").combobox();
}

function initBindings(){
    $("#formBinding").hide();
    if (!$.fn.getPaymentProperty("isBindingEnabled") || !$('#bindingIdSelect option').length) { //Are bindings?
        return true;
    } else { // Yes, are bindings
        createComboboxSelect();
        $(".block-input-pan.col-input").addClass('binding');
        $("#combobox").change(function(){
            setEnableBinding($("#combobox").val());
            $("#bindingIdSelect").trigger('change');
        });
        $("#bindingIdSelect [value='" + $("#combobox").val() + "']").attr("selected", "selected");
        $("#buttonBindingPayment2").hide();
        setEnableBinding($("#combobox").val());
    }
}

function createComboboxSelect() {
    if (!$('#combobox option').length) {
        $("#bindingIdSelect option").each(function(index, element){
            if (element.text.length > 0){
                var binding = {value:'',month:'',year:''};
                var len = element.text.length;
                var pan = element.text.substr(0, len-6);
                binding.value = element.value;
                binding.month = element.text.substr(len-5, 2);
                binding.year = element.text.substr(len-2, 2);
                bindings[pan] = binding;
                element.pan = pan;
                element.text = formatPan(pan);
            }
            $('#combobox').append("<option value='"+element.value+"' data-pan='" + element.pan + "'>"+element.text+"</option>");
        });
        $('#combobox').append('<option value="false">'+OTHER_TEXT+'</option>');
    }
}

function formatPan(value) {
    var number,
            formatValue;
    if (value.length > 16) {
            number = value.slice(0,4) + "**********" + value.slice(-4);
    } else {
            number = value.slice(0,4) + "********" + value.slice(-4);
    }
    formatValue = number.replace(/([\d/*]{4}(?!$))/g,'$1 ');
    return formatValue;
}

function setEnableBinding(enable){
    $("#pan_visible").val("");
    var selectItemPan = $('#combobox [selected="selected"]').data('pan');
    if (enable == "false") {
        enable = JSON.parse(enable);
    }
    if (enable){
        //ENABLE bindings
        bindingsEnabled = true;
        clearErrors();
        $("#iPAN-validation").addClass('login-valid');
        $(".basicInfo").hide();
        $("#buttonPayment").hide();
        $("#buttonBindingPayment2").show();
        $("#buttonBindingPayment2").prop('disabled', true);
        $("#delete-binding").show();
        $("#buttonPayment").prop('disabled', false);
        $("#bindingBlock").hide();
        $("#iPAN_sub").val($("#combobox option:selected").text());
        $("#bindingIdSelect option").attr("selected", false);
        $("#bindingIdSelect [value='" + $("#combobox").val() + "']").attr("selected", "selected");
        $("#bindingCvc").val('');
        $('#typecard-icon').removeClass();
        $('#typecard-icon').addClass( detectPaymentSystem() );
        // $("#input-month").val(bindings[selectItemPan].month);
        // $("#input-year").val(bindings[selectItemPan].year);
        $("#iPAN_sub").prop('readonly', true);
        $("#iCVC").val('');
        $('#bindingCvc').val();
        $("#bindingIdSelect").trigger('change');
    } else {
        //DISABLE bindings
        bindingsEnabled = false;
        clearErrors();
        $('#bindingIdSelect').val('');
        $("#iPAN_sub").prop('readonly', false);
        $(".basicInfo").show();
        $('#typecard-icon').removeClass();
        $("#buttonPayment").show();
        $("#buttonPayment").prop('disabled', true);
        $("#bindingBlock").show();
        $("#month").val('01');
        $("#year").val('2016');
        $("#buttonBindingPayment2").hide();
        $("#spasibo_block").hide();
        $("#bindingIdSelect option").attr("selected", false);
        $("#delete-binding").hide();
        $("#iPAN_sub").val('');
        $("#iCVC").val('');
        $("#input-month").val('');
        $("#input-year").val('');
        $("#iTEXT").val('');
    }
}

function detectPaymentSystem() {
    if ($('#bindingIdSelect [selected="selected"]').data('paymentsystem') == 'MASTERCARD' &&
        $('#bindingIdSelect [selected="selected"]').data('ismaestro')) {
        $('#buttonBindingPayment2').prop('disabled', false);
        return 'MAESTRO';
    } else if ($('#bindingIdSelect [selected="selected"]').data('paymentsystem') == 'MASTERCARD' &&
              !$('#bindingIdSelect [selected="selected"]').data('ismaestro')) {
        return 'MASTERCARD'
    } else {
        return $('#bindingIdSelect [selected="selected"]').data('paymentsystem');
    }
}

function clearErrors() {
    $("#iPAN-validation").removeClass();
    $("#iCVC-validation").removeClass();
    $("#iTEXT-validation").removeClass();
    $("#year-validation").removeClass();
    $(".typecard").removeClass();
    $(".typecard").addClass('typecard-icon');
}

function prepareForMobile() {
    var userAgent = navigator.userAgent;
    if (/(Android|webOS|iPhone|iPad|iPod|BlackBerry|Opera Mini)/i.test(userAgent) &&
        !/(Windows Phone|iemobile|WPDesktop)/i.test(userAgent) ) {  //IEMobile doesn't support mask tel type
        $('body').addClass('mobile');
        $('#iCVC').attr('type', 'tel');
    }
}

$(document).ready(function(){
    $("#buttonBindingPayment2").click(function(){
        $("#buttonBindingPayment").click();
    });
    $("#iCVC").keyup(function(value){
        $("#bindingCvc").val($("#iCVC").val());
    });
    $('label[for="iAgree"] a').click(function(e) {
        e.stopPropagation();
    });
    prepareForMobile();
});
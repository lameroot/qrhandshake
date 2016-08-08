var bindingSelector;
var selectdItem;

(function ($) {
    $.widget("custom.combobox", {
        _create: function () {
            this.wrapper = $("<span>")
                .addClass("custom-combobox")
                .insertAfter(this.element);
            this.element.hide();
            this._createAutocomplete();
            if($.fn.getPaymentProperty("isBindingEnabled")){
                this._createShowAllButton();
            }
        },
        _createAutocomplete: function () {
            bindingSelector = this;
            var selected = this.element.children(":selected"),
                value = selected.val() ? selected.text() : "";
            this.input = $("<input  maxlength='23' type='tel'>")
                .appendTo(this.wrapper)
                .val(value)
                .attr("title", "")
                .addClass("custom-combobox-input ui-widget ui-widget-content ui-state-default ui-corner-all")
                .attr('id', 'iPAN_sub')
                .attr('tabindex', "0")
                .attr('name', 'cardNumber')
                .attr('placeholder', "0000 0000 0000 0000")
                .autocomplete({
                    delay: 0,
                    minLength: 0,
                    source: $.proxy(this, "_source"),
                    open: function () {
                        $(this).parent().find('.ui-button').addClass('active');
                    }
                })
                .tooltip({
                    tooltipClass: "ui-state-highlight"
                })
                ;
                $(".ui-helper-hidden-accessible").remove();
            this.input.keyup(function (event){
                $("#iPAN").val(event.target.value.split(" ").join(""));
                $("#iPAN").trigger('keyup.payment');
            });
            this.input.mousedown(function (event){
                event.target.value = event.target.value.replace(/ *$/,'');
            });
            $("#iPAN_sub").val('');
            this._on(this.input, {
                autocompleteselect: function (event, ui) {
                    $(this.input).parent().find('.ui-button').removeClass('active');
                    ui.item.option.selected = true;
                    this._trigger("select", event, {
                        item: ui.item.option
                    });
                    var input = $(this.input);
                    setTimeout(function () {
                        input.blur();
                        input.focus();
                    }, 10);
                    selectdItem = ui.item;
                    $("#combobox").change();
                }
                // ,
                // autocompletechange: function (event, ui) {
                //     $("#pan_visible").trigger('change');
                // }
            });
        },
        _createShowAllButton: function () {
            var input = this.input,
                wasOpen = false,
                wrapp = this.wrapper.parent().parent();

            $("<a>")
                .attr("tabIndex", "0")
                .attr("title", "Show All Items")
                .appendTo(wrapp)
                .button({
                    icons: {
                        primary: "ui-icon-triangle-1-s"
                    },
                    text: false
                })
                .removeClass("ui-corner-all")
                .addClass("arrow")
                .mousedown(function () {
                    wasOpen = input.autocomplete("widget").is(":visible");
                })
                .click(function () {
                    input.focus();
                    // Close if already visible
                    if (wasOpen) {
                        return;
                    }
                    // Pass empty string as value to search for, displaying all results
                    input.autocomplete("search", "");
                });
            input.blur(function () {
                $(this).parent().find('.ui-button').removeClass('active');
            })
        },
        _source: function (request, response) {
            var matcher = new RegExp($.ui.autocomplete.escapeRegex(request.term), "i");
            response(this.element.children("option").map(function () {
                var text = $(this).text();
                if (this.value && ( !request.term || matcher.test(text) ))
                    return {
                        label: text,
                        value: text,
                        option: this
                    };
            }));
        },
        _destroy: function () {
            this.wrapper.remove();
            this.element.show();
        }
    });

})(jQuery);
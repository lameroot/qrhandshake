function isValidData() {
	var isValid = true;
	var fioPattern = /^([А-Яа-я\s\-]+){2,}$/;
	var classPattern = /^([А-Яа-я\s\-]+){1,}$/;
	var phonePattern =/^((8|\+7)[\- ]?)?(\(?\d{3,4}\)?[\- ]?)?[\d\- ]{7,10}$/;
	var urlPattern=/^([a-zA-Z0-9А-Яа-я\-\.]+\.[a-zA-Zрф]{2,3}[\/]{0,1})$/;
	var amountPattern=/^([0-9]){1,6}$/;
	var monthAmountPattern=/^([0-9]){1,10}$/;
	var latinPattern=/^([^А-Яа-я]+)$/;
	var emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;

	$('#errorBlock').empty();

	if($('#org_name').val().replace(/\s/g,"") == ""){
		$('#errorBlock').append('Введите значение поля 1. Юридическое наименование.<br>');
		isValid = false;
	}

	if($('#org_inn').val().replace(/\s/g,"") == ""){
		$('#errorBlock').append('Введите значение поля 2. ИНН.<br>');
		isValid = false;
	}else{
		var innPattern = /^[0-9]{10}$/;
   		if( !innPattern.test($('#org_inn').val())){
			$('#errorBlock').append('Неверное значение поля 2. ИНН.<br>');
			isValid = false;
		}
	}

	if($('#org_address').val().replace(/\s/g,"") == ""){
		$('#errorBlock').append('Введите значение поля 3. Местонахождение.<br>');
		isValid = false;
	}

	if($('#director_fio').val().replace(/\s/g,"") == ""){
		$('#errorBlock').append('Введите значение поля 4. Руководитель.<br>');
		isValid = false;
	}else{
   		if( !fioPattern.test($('#director_fio').val())){
			$('#errorBlock').append('Неверное значение поля 4. Руководитель.<br>');
			isValid = false;
		}
	}

	if($('#director_class').val().replace(/\s/g,"") == ""){
		$('#errorBlock').append('Введите значение поля 4.1. Должность.<br>');
		isValid = false;
	}else{
   		if( !classPattern.test($('#director_class').val())){
			$('#errorBlock').append('Неверное значение поля 4.1. Должность.<br>');
			isValid = false;
		}
	}

	if($('#director_phone').val().replace(/\s/g,"") == ""){
	}else{
   		if( !phonePattern.test($('#director_phone').val())){
			$('#errorBlock').append('Неверное значение поля 4.2. Телефон, Факс.<br>');
			isValid = false;
		}
	}

	if($('#buh_fio').val().replace(/\s/g,"") == ""){
		$('#errorBlock').append('Введите значение поля 5. Главный бухгалтер.<br>');
		isValid = false;
	}else{
   		if( !fioPattern.test($('#buh_fio').val())){
			$('#errorBlock').append('Неверное значение поля 5. Главный бухгалтер.<br>');
			isValid = false;
		}
	}


	if($('#buh_phone').val().replace(/\s/g,"") == ""){
	}else{
   		if( !phonePattern.test($('#buh_phone').val())){
			$('#errorBlock').append('Неверное значение поля 5.1. Телефон, Факс.<br>');
			isValid = false;
		}
	}

	if($('#shop_name').val().replace(/\s/g,"") == ""){
		$('#errorBlock').append('Введите значение поля 6. Наименование интернет-магазина.<br>');
		isValid = false;
	}

	if($('#shop_latin').val().replace(/\s/g,"") == ""){
		$('#errorBlock').append('Введите значение поля 7. Наименование интернет-магазина (латинскими буквами).<br>');
		isValid = false;
	}else{
   		if( !latinPattern.test($('#shop_latin').val())){
			$('#errorBlock').append('Неверное значение поля 7. Наименование интернет-магазина (латинскими буквами).<br>');
			isValid = false;
		}
	}

	if($('#shop_url').val().replace(/\s/g,"") == ""){
		$('#errorBlock').append('Введите значение поля 8. URL-адрес сайта интернет-магазина.<br>');
		isValid = false;
	}else{
   		if( !urlPattern.test($('#shop_url').val())){
			$('#errorBlock').append('Неверное значение поля 8. URL-адрес сайта интернет-магазина.<br>');
			isValid = false;
		}
	}

	if($('#shop_specific').val().replace(/\s/g,"") == ""){
		$('#errorBlock').append('Введите значение поля 9. Род деятельности.<br>');
		isValid = false;
	}


	if($('#shop_amount').val().replace(/\s/g,"") == ""){
		$('#errorBlock').append('Введите значение поля 10. Средняя сумма операции.<br>');
		isValid = false;
	}else{
   		if( !amountPattern.test($('#shop_amount').val())){
			$('#errorBlock').append('Неверное значение поля 10. Средняя сумма операции.<br>');
			isValid = false;
		}
	}

	if($('#shop_monthamount').val().replace(/\s/g,"") == ""){
		$('#errorBlock').append('Введите значение поля 11. Оборот.<br>');
		isValid = false;
	}else{
   		if( !monthAmountPattern.test($('#shop_monthamount').val())){
			$('#errorBlock').append('Неверное значение поля 11. Оборот.<br>');
			isValid = false;
		}
	}


	if($('#region').val().replace(/\s/g,"") == ""){
		$('#errorBlock').append('Введите значение поля 12. Регион.<br>');
		isValid = false;
	}

	if($('#region_town').val().replace(/\s/g,"") == ""){
		$('#errorBlock').append('Введите значение поля 13. Населённый пункт.<br>');
		isValid = false;
	}

	if($('#town_bank').val().replace(/\s/g,"") == ""){
		$('#errorBlock').append('Введите значение поля 14. Подразделение банка.<br>');
		isValid = false;
	}


	if($('#login').val().replace(/\s/g,"") == ""){
		$('#errorBlock').append('Введите значение поля 15. Логин.<br>');
		isValid = false;
	}

	if($('#password1').val().replace(/\s/g,"") == ""){
		$('#errorBlock').append('Введите значение поля 16. Пароль.<br>');
		isValid = false;
	}

	if($('#password2').val().replace(/\s/g,"") == ""){
		$('#errorBlock').append('Введите значение поля 17. Повтор ввода пароля.<br>');
		isValid = false;
	}

	if($('#password1').val() != $('#password2').val()){
		$('#errorBlock').append('Значения поля "16. Пароль" и "17. Повтор ввода пароля" не совпадают.<br>');
		isValid = false;
	}

	if($('#contact_fio').val().replace(/\s/g,"") == ""){
		$('#errorBlock').append('Введите значение поля 18. Контактное лицо.<br>');
		isValid = false;
	}else{
   		if( !fioPattern.test($('#contact_fio').val())){
			$('#errorBlock').append('Неверное значение поля 18. Контактное лицо.<br>');
			isValid = false;
		}
	}

	if($('#contact_specific').val().replace(/\s/g,"") == ""){
	}else{
   		if( !classPattern.test($('#contact_specific').val())){
			$('#errorBlock').append('Неверное значение поля 18.1. Должность.<br>');
			isValid = false;
		}
	}

	if($('#contact_phone').val().replace(/\s/g,"") == ""){
	}else{
   		if( !phonePattern.test($('#contact_phone').val())){
			$('#errorBlock').append('Неверное значение поля 18.2. Телефон, Факс.<br>');
			isValid = false;
		}
	}

	if($('#contact_email').val().replace(/\s/g,"") == ""){
		$('#errorBlock').append('Введите значение поля 19. Электронная почта.<br>');
		isValid = false;
	}else{
   		if( !emailPattern.test($('#contact_email').val())){
			$('#errorBlock').append('Неверное значение поля 19. Электронная почта.<br>');
			isValid = false;
		}
	}

	if($('#captchaText').val().replace(/\s/g,"") == ""){
		$('#errorBlock').append('Введите корректный текст, изображенный на картинке.<br>');
		isValid = false;
	}else{
		if(!jcap()){
			$('#errorBlock').append('Введите корректный текст, изображенный на картинке.<br>');
			isValid = false;
		}
	}

    return isValid;
}

function isNumber(keyCode) {
    return ( keyCode > 47 && keyCode < 58 );
}

function isControl(keyCode) {
    return "8 9 13 16 17 18 20 37 38 39 40 45 46 ".indexOf( keyCode + " " ) != -1;
}

function isChar(keyCode) {
    return ( keyCode > 96 && keyCode < 133 ) || (keyCode > 64 && keyCode < 91);
}

function isSpace(keyCode) {
    return keyCode == 32;
}

function isDot(keyCode) {
    return keyCode == 46;
}

function constructAllInfo(){
	var allInfo = "<table>";
	allInfo = allInfo+"<tr><td>"+$("#label_org_name").text()+"</td><td>"+$("#org_name").val()+"</td></tr>";
	allInfo = allInfo+"<tr><td>"+$("#label_org_inn").text()+"</td>";
	allInfo = allInfo+"<td>"+$("#org_inn").val()+"</td>"+"</tr>";
	allInfo = allInfo+"<tr><td>"+$("#label_org_address").text()+"</td>";
	allInfo = allInfo+"<td>"+$("#org_address").val()+"</td>"+"</tr>";
	allInfo = allInfo+"<tr><td>"+$("#label_director_fio").text()+"</td>";
	allInfo = allInfo+"<td>"+$("#director_fio").val()+"</td>"+"</tr>";
	allInfo = allInfo+"<tr><td>"+$("#label_director_class").text()+"</td>";
	allInfo = allInfo+"<td>"+$("#director_class").val()+"</td>"+"</tr>";
	allInfo = allInfo+"<tr><td>"+$("#label_director_phone").text()+"</td>";
	allInfo = allInfo+"<td>"+$("#director_phone").val()+"</td>"+"</tr>";
	allInfo = allInfo+"<tr><td>"+$("#label_director_fax").text()+"</td>";
	allInfo = allInfo+"<td>"+$("#director_fax").val()+"</td>"+"</tr>";
	allInfo = allInfo+"<tr><td>"+$("#label_buh_fio").text()+"</td>";
	allInfo = allInfo+"<td>"+$("#buh_fio").val()+"</td>"+"</tr>";
	allInfo = allInfo+"<tr><td>"+$("#label_buh_phone").text()+"</td>";
	allInfo = allInfo+"<td>"+$("#buh_phone").val()+"</td>"+"</tr>";
	allInfo = allInfo+"<tr><td>"+$("#label_buh_fax").text()+"</td>";
	allInfo = allInfo+"<td>"+$("#buh_fax").val()+"</td>"+"</tr>";
	allInfo = allInfo+"<tr><td>"+$("#label_shop_name").text()+"</td>";
	allInfo = allInfo+"<td>"+$("#shop_name").val()+"</td>"+"</tr>";
	allInfo = allInfo+"<tr><td>"+$("#label_shop_latin").text()+"</td>";
	allInfo = allInfo+"<td>"+$("#shop_latin").val()+"</td>"+"</tr>";
	allInfo = allInfo+"<tr><td>"+$("#label_shop_url").text()+"</td>";
	allInfo = allInfo+"<td>"+'http://'+$("#shop_url").val()+"</td>"+"</tr>";
	allInfo = allInfo+"<tr><td>"+$("#label_shop_specific").text()+"</td>";
	allInfo = allInfo+"<td>"+$("#shop_specific").val()+"</td>"+"</tr>";
	allInfo = allInfo+"<tr><td>"+$("#label_shop_amount").text()+"</td>";
	allInfo = allInfo+"<td>"+$("#shop_amount").val()+"</td>"+"</tr>";
	allInfo = allInfo+"<tr><td>"+$("#label_shop_monthamount").text()+"</td>";
	allInfo = allInfo+"<td>"+$("#shop_monthamount").val()+"</td>"+"</tr>";
	allInfo = allInfo+"<tr><td>"+$("#label_region").text()+"</td>";
	allInfo = allInfo+"<td>"+$("#region").val()+"</td>"+"</tr>";
	allInfo = allInfo+"<tr><td>"+$("#label_region_town").text()+"</td>";
	allInfo = allInfo+"<td>"+$("#region_town").val()+"</td>"+"</tr>";
	allInfo = allInfo+"<tr><td>"+$("#label_town_bank").text()+"</td>";
	allInfo = allInfo+"<td>"+$("#town_bank").val()+"</td>"+"</tr>";
	allInfo = allInfo+"<tr><td>"+$("#label_login").text()+"</td>";
	allInfo = allInfo+"<td>"+$("#login").val()+"</td>"+"</tr>";
	allInfo = allInfo+"<tr><td>"+$("#label_contact_fio").text()+"</td>";
	allInfo = allInfo+"<td>"+$("#contact_fio").val()+"</td>"+"</tr>";
	allInfo = allInfo+"<tr><td>"+$("#label_contact_specific").text()+"</td>";
	allInfo = allInfo+"<td>"+$("#contact_specific").val()+"</td>"+"</tr>";
	allInfo = allInfo+"<tr><td>"+$("#label_contact_phone").text()+"</td>";
	allInfo = allInfo+"<td>"+$("#contact_phone").val()+"</td>"+"</tr>";
	allInfo = allInfo+"<tr><td>"+$("#label_contact_email").text()+"</td>";
	allInfo = allInfo+"<td>"+$("#contact_email").val()+"</td>"+"</tr>";
	allInfo = allInfo+"</table>";
	allInfo = allInfo.replace("\t","");
	allInfo = allInfo.replace("\n","");
	return allInfo;
}

$(document).ready(function () {
	function initRegionTown(){
		$('#region_town').empty();
		var initOption = "<option value='Выберите город'>Выберите город</option>";
		$('#region_town').append($(initOption));

	}
	function initRegionTownBank(){
		$('#town_bank').empty();
		var initOption = "<option value='Выберите подразделение'>Выберите подразделение</option>";
		$('#town_bank').append($(initOption));

	}

	function getRegions() {
        jQuery.ajax({
            url: $("#location").attr("value")+"getRegions.do",
            type: 'POST',
            cache: false,
            error: function(xhr, status, error) {
			    $('#errorBlock').append('<p class="errorField" id="loginError">' + ajaxError+ "</p>");
                return true;
            },
            success: function (data) {
				var initOption = "<option value='Выберите регион'>Выберите регион</option>";
				$('#region').append($(initOption));
				var myObject = JSON.parse(data);
				$.each(myObject, function() {

					var option = "<option value='"+this+"'>"+this+"</option>";
					$('#region').append($(option));

				});
                return true;
            }

        });
    }
	function getRegionTowns(region) {
        jQuery.ajax({
            url: $("#location").attr("value")+"getRegionTowns.do",
            type: 'POST',
            cache: false,
			data: ({
				region: region
			}),
            error: function(xhr, status, error) {
			    $('#errorBlock').append('<p class="errorField" id="loginError">' + ajaxError+ "</p>");
                return true;
            },
            success: function (data) {
				var myObject = JSON.parse(data);
				$.each(myObject, function() {

					var option = "<option value='"+this+"'>"+this+"</option>";
					$('#region_town').append($(option));

				});
                return true;
            }

        });
    }
	function getRegionTownOffices(region,town) {
        jQuery.ajax({
            url: $("#location").attr("value")+"getRegionTownOffices.do",
            type: 'POST',
            cache: false,
			data: ({
				region: region,
				town: town
			}),
            error: function(xhr, status, error) {
			    $('#errorBlock').append('<p class="errorField" id="loginError">' + ajaxError+ "</p>");
                return true;
            },
            success: function (data) {
				var myObject = JSON.parse(data)
				$.each(myObject, function() {

					var option = "<option value="+this+">"+this+"</option>";
					$('#town_bank').append($(option));

				});
                return true;
            }

        });
    }
    function sendData() {
        $('#errorBlock').empty();
        $('#indicator').show();
		$('#registerButton').attr('disabled', true);
        jQuery.ajax({
            url: $("#location").attr("value")+"register.do",
            type: 'POST',
            cache: false,
            data: ({
                name: $("#contact_fio").attr("value"),
                email: $("#contact_email").attr("value"),
                organization: $("#shop_latin").attr("value").replace(/\W/g,''),
                url: 'http://'+$("#shop_url").attr("value"),
                rbsName: $("#login").attr("value"),
                allInfo: constructAllInfo(),
                password: $("#password1").attr("value"),
                language: language
            }),
            dataType: 'json',
            error: function(xhr, status, error) {
				$('#indicator').hide();
                $('#registerButton').attr('disabled', false);
			    $('#errorBlock').append('<p class="errorField" id="loginError">' + ajaxError+ "</p>");
                return true;
            },
            success: function (data) {
				$('#indicator').hide();
                $('#registerButton').attr('disabled', false);
				if ('error' in data) {
                    var message = data['error'];
                    $('#errorBlock').append('<p class="errorField" id="loginError">' + message + "</p>");
                } else if ('info' in data){
                    $('#errorBlock').empty();
                    $('#numberCountdown').hide();
                    var message = data['info'];
                    $('#infoBlock').append('<p>' + message + "</p>");
                    $('#registerForm').attr('expired', '1');
                    $('#registerButton').attr('disabled', true);
                }
                return true;
            }

        });
    }
	getRegions();
	initRegionTown();
	initRegionTownBank();
	$('#region').change(function(){
		$("#region option[value='Выберите регион']").remove();
		initRegionTown();
        initRegionTownBank();
		getRegionTowns($('#region').val());
    });
	$('#region_town').change(function() {
		$("#region_town option[value='Выберите город']").remove();
        initRegionTownBank();
		getRegionTownOffices($('#region').val(), $('#region_town').val());
    });
	$('#town_bank').change(function() {
		$("#town_bank option[value='Выберите подразделение']").remove();
	});

    $('#registerForm').unbind();

    $('#registerForm').submit(function(event) {
        sendData();
        event.stopPropagation();
        return false;
    });

	$('#registerButton').click(function() {
        if (!isValidData()){
            event.stopPropagation();
			return false;
		}
        $('#registerButton').attr('disabled', true);
        $('#registerForm').submit();
    });
});
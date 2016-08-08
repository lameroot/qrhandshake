// ��� ������� ��������
//  ----------------------------------------------------------------------------
var PARAM_WebSpeaker_vocabularyByAnchors = true;
var PARAM_WebSpeaker_baseUrl = "/payment/js/common/stc-webspeaker/"; //URL to the webspeaker components
var PARAM_WebSpeaker_homePageUrl = "/"; //URL to the site homepage, loaded on user command
var PARAM_WebSpeaker_menuSelectors = ['#menu a', '#menu2 a'];
var PARAM_WebSpeaker_serviceSelectors = ['div.kartinki a', 'div.kartinki_0 a'];
var currentVoiceIndex = 1;
var HANDLER_WebSpeaker_recognitionResult = null;

//  ----------------------------------------------------------------------------

var stcRecognitionFailureTexts = [
	"Пожалуйста, повторите",
	"Команда не распознана, пожалуйста, повторите",
	"Назовите раздел или одну из голосовых команд",
	"Пожалуйста, проверьте настройки микрофона"
];

var stcGeneralHelp = "Для голосовой навигации по интернет-порталу вы можете произнести следующие команды:  "+
					"Главная страница, Обновить страницу, Страница вверх, Страница вниз, Страница вперед,  "+
					"Страница назад, Помощь, Разделы, Услуги. Вы также можете сказать: помощь по разделам и "+
					"помощь по услугам для получения помощи в навигации по разделам и услугам.";

var stcRecognitionFailureCount = 0;
var lastVocabulary = [];
var commonVocabulary = [];
var menuTTSEnabled = 'auto';
var servicesTTSEnabled = 'auto';
var stcSpeakTitle = true;
var stcCheckJava = true;

jQuery('head').append('<link rel="stylesheet" href="'+PARAM_WebSpeaker_baseUrl+
		'stc-webspeaker.css" type="text/css" charset="utf-8"/>')
	.append('<scr'+'ipt src="'+PARAM_WebSpeaker_baseUrl+
		'deployJava.js" type="text/javas'+'cript" ></scr'+'ipt>');

var STC_STATUS_DISABLED = 0;
var STC_STATUS_ENABLED = 1;
var STC_STATUS_ERROR = 2;
var STC_STATUS_INITIALIZING = 3;

// -----------------------------------------------------------------------------

function STCWebSpeakerCreateApplet() {
	if (!stcWebseaketAppletCreated) {
		stcWebseaketAppletCreated = true;
		var s = '';
		s += '	<div id="stc-webspeaker-applet" style="width: 1px; hetight: 1px">';
		s += '    <applet id="STCWebSpeaker" name="STCWebSpeaker" codebase="'+PARAM_WebSpeaker_baseUrl+
			'" code="stc.webspeaker.client.MyApplet" archive="HelloApplet.jar" width="190" height="35">';
		s += '        <PARAM name="WebSpeakerAddress" value="www.sbrf.ru">';
		s += '        <PARAM name="WebSpeakerPort" value="8086">';
		s += '        <PARAM name="NoiseThreshold" value="350">';
		s += '        <PARAM name="WebSpeakerTimeout" value="120">'; // in seconds
		s += '        <PARAM name="asrEncoding" value="gsm">'; //pcm, gsm, u-law
		s += '        <PARAM name="ttsEncoding" value="gsm">';
		s += '        <PARAM name="speedLimit" value="10">'; //KBytes/s
		s += '    </applet>';
		s += '	</div>';
		jQuery("body").prepend(s);
		if (!jQuery.browser.msie) {
			jQuery("#stc-webspeaker-applet").css("position", "fixed");
		}
	}
}

function PAGE_WebSpeaker_create_icon() {
	jQuery('#head2 .table .n3 .div_n3 .d_nav').prepend(
		'<a id="stcVoiceButton" href="#" onclick="stcAppletButtonClick(); return false"><img src="'+PARAM_WebSpeaker_baseUrl+
		'img/ico1.gif" border="0" alt="Включить голосовой интерфейс" '+
		'title="Включить голосовой интерфейс"></a>');
}

// ����� ������� �������������
//  ----------------------------------------------------------------------------
function WebSpeakerCommand(command, action, object) {
    this.command = command;
    this.action = action;
	this.object = object;

	this.toString = function() {
		return "{'"+this.command+"', '"+this.action+"'} ";
	}
}

// Events
//  ----------------------------------------------------------------------------
function STCWebSpeakerReady() {
	setTimeout(STC_WebSpeaker_ready, 1);
}

function STCTurnOn() {
	if (stcWebspeakerStatus == STC_STATUS_ENABLED || stcWebspeakerStatus == STC_STATUS_INITIALIZING) {
		return;
	}
	/*
	if (navigator.javaEnabled) {
		if(!navigator.javaEnabled()) {
			PAGE_WebSpeaker_displayError("no_java");
			return;
		}
	} else if (jQuery.browser.msie) {
		if (new ActiveXObject('JavaWebStart.isInstalled.1.6.0.0') == null) {
			PAGE_WebSpeaker_displayError("no_java");
			return;
		}
	}
	*/

	stcWebspeakerStatus = STC_STATUS_INITIALIZING;

    PAGE_WebSpeaker_displayStatus(false);
	if (stcWebseaketAppletCreated) {
		try {
			document.STCWebSpeaker.startSpeaker();
		} catch (ex) {
			//alert(ex);
		}
	}
	STCShowApplet();
	var cookie = STC_get_cookie('WebSpeakerEnabled');
	if (cookie != '2') {
		STCExpandApplet();
	} else {
		jQuery("#stc-webspeaker-block").show();
		jQuery("#stc-webspeaker-container").hide();
		STCWebSpeakerCreateApplet();
	}
}

function STCTurnOff() {
	STC_set_cookie('WebSpeakerEnabled', '');
	stcWebspeakerStatus = STC_STATUS_DISABLED;

    PAGE_WebSpeaker_displayStatus(false);
	try {
		document.STCWebSpeaker.stopSpeaker();
	} catch (ex) {
		//alert(ex);
	}

	PAGE_WebSpeaker_setHeader("Выключен", 'stc-inactive');
	//jQuery("#stcAppletButton").removeClass().addClass('stcAppletButtonInactive');

	if (stcWebspeakerExpanded){
		STCCollapseApplet();
	} else {
		setTimeout(STCHideApplet, 600);
	}
}

function STCTimeOut() {
	STC_set_cookie('WebSpeakerEnabled', '');
	stcWebspeakerStatus = STC_STATUS_DISABLED;

	try {
		document.STCWebSpeaker.stopSpeaker();
	} catch (ex) {
		//alert(ex);
	}

	if (!stcWebspeakerExpanded){
		STCExpandApplet(true);
	}
}

function STCExpandApplet(isDisabled) {
	if (stcWebspeakerExpanded) {
		return;
	}
	jQuery("#stc-webspeaker-container").queue(function() {
		jQuery("#stc-webspeaker-block").show();
		jQuery(this).css('height', '1px').dequeue();
	}).animate({height: 315}, function() {
		STCWebSpeakerCreateApplet();
		if (!isDisabled) {
			STC_set_cookie('WebSpeakerEnabled', '1');
		}
		jQuery("#stc-webspeaker-applet").css('width', '').css('height', '');
		if (stcWebspeakerStatus == STC_STATUS_ENABLED) {
			jQuery("#stc-webspeaker-top div").html("Готов к работе");
		}
	});
	stcWebspeakerExpanded = true;
}

function STCCollapseApplet() {
	if (!stcWebspeakerExpanded) {
		return;
	}
	jQuery("#stc-webspeaker-container").queue(function() {
		jQuery("#stc-webspeaker-applet").css('width', '1px').css('height', '1px');
		jQuery(this).dequeue();
	}).animate({height: 1}, function() {
		jQuery("#stc-webspeaker-container").hide().css('height', '');
		jQuery("#stc-webspeaker-top div").html("Голосовое управление");
		if (stcWebspeakerStatus != STC_STATUS_ENABLED && stcWebspeakerStatus != STC_STATUS_INITIALIZING) {
			STCHideApplet();
		} else {
			STC_set_cookie('WebSpeakerEnabled', '2');
		}
	});
	stcWebspeakerExpanded = false;
}

function STCShowApplet () {
	jQuery("#stc-webspeaker-top").show();
}

function STCHideApplet () {
	jQuery("#stc-webspeaker-top").hide();
	STC_set_cookie('WebSpeakerEnabled', '');
	stcWebspeakerStatus = STC_STATUS_DISABLED;
}

function STCWebSpeakerSpeechComplete(){
    STC_WebSpeaker_speechComplete();
}

function STCWebSpeakerLoadVocabularyComplete() {
    STC_WebSpeaker_loadVocabulary_complete();
}

function STCWebSpeakerCommandRecognized(cmd) {
    STC_WebSpeaker_recognitionResult(cmd);
}

function STCWebSpeakerSpeakSelection() {
	var sel = "" + PAGE_WebSpeaker_getSelectedText();
	sel = sel.replace(/[\n]/gi, '; ');
	sel = clearToken(sel, true);
	if (sel != "") {
		STCWebSpeakerLoadText(sel, currentVoiceIndex);
	}
	return false;
}

function STCWebSpeakerLoadText(text, currentVoiceIndex) {
	try {
		document.STCWebSpeaker.loadText(text, currentVoiceIndex);
	} catch (ex) {}
}

function STCWebSpeakerSpeakPage() {
	for (var i in contentTTSText) {
		STCWebSpeakerLoadText(contentTTSText[i].text, contentTTSText[i].voiceIndex);
	}
	return true;
}

function STCWebSpeakerStartSpeak() {
	var sel = "" + PAGE_WebSpeaker_getSelectedText();
	sel = sel.replace(/[\n]/gi, '; ');
	sel = clearToken(sel, true);
	if (sel != "") {
		STCWebSpeakerLoadText(sel, currentVoiceIndex);
	}else{
		for (var i in contentTTSText) {
			STCWebSpeakerLoadText(contentTTSText[i].text, contentTTSText[i].voiceIndex);
		}
	}
}

function STCWebSpeakerStopSpeak() {
	if (document.STCWebSpeaker) {
		try {
			document.STCWebSpeaker.stopSpeak();
		} catch (ex) {
			//alert(ex);
		}
	}
}

function STCWebSpeakerCommandNoMatch() {
	var i = stcRecognitionFailureCount % stcRecognitionFailureTexts.length;
	stcRecognitionFailureCount ++;
	PAGE_WebSpeaker_displayResult(stcRecognitionFailureTexts[i]);
}

function STCWebSpeakerErrorEvent(messageId) {
	jQuery('<input type="text" style="position: absolute; bottom: 1px; width: 1px; height: 1px; z-index: 2001" />').
		prependTo('body').focus().remove();
	PAGE_WebSpeaker_displayError(messageId);
	stcWebspeakerStatus = STC_STATUS_ERROR;
}

function STCWebSpeakerAddGrammarComplete(name) {
    // grammar with name "name" was succesfully added
    // call document.STCWebSpeaker.addGrammar(name, text); to add
}

function STCWebSpeakerDeleteGrammarComplete(name) {
    // "name" is name of a deleted grammar
    // call document.STCWebSpeaker.deleteGrammar(name); to delete
}

function STCWebSpeakerDeleteAllGrammarsComplete() {
    // raised when all grammars are deleted
    // call document.STCWebSpeaker.deleteAllGrammars(); to delet all grammars
}

function STCWebSpeakerSetGrammarsNumberComplete(num) {
    // call document.STCWebSpeaker.setGrammarsNumber(number); to set grammar nums
}

function STCWebSpeakerGetGrammarsNumberComplete(num) {
    // call document.STCWebSpeaker.getGrammarsNumber(number); to get grammar nums
}

function STCWebSpeakerGetLoadedGrammarsCountComplete(count) {
    // the count of all loaded grammars
    // call document.STCWebSpeaker.getLoadedGrammarsCount(); to get count
}

function STCWebSpeakerGetLoadedGrammarsComplete(grammars) {
    // list of all loaded grammars
    // call document.STCWebSpeaker.getLoadedGrammars(); to get loaded grammars
}

// dispay status to applet addition,
// load and add text to tts,
// load Vocubulary
function STC_WebSpeaker_ready() {
    //PAGE_WebSpeaker_displayStatus(false);

    PAGE_WebSpeaker_loadText();
	if(initTTSText!=null)
		STCWebSpeakerLoadText(initTTSText, currentVoiceIndex);

    PAGE_WebSpeaker_loadVocabulary();
}

//speech complete event
function STC_WebSpeaker_speechComplete(){
	stcWebspeakerSpeakInProgress = false;
	jQuery('#stc-button-dospeak').html('Озвучить');
}

//load vocabulary complete event
function STC_WebSpeaker_loadVocabulary_complete() {
    PAGE_WebSpeaker_displayStatus(true);
	jQuery('<input type="text" style="position: absolute; bottom: 1px; width: 1px; height: 1px; z-index: 2001" />').
		prependTo('body').focus().remove();
}

jQuery(document).ready(function(){
	//alert('ready');
	PAGE_WebSpeaker_create();
});
if (jQuery.browser.msie) {
	if (window.addEventListener) {
		window.addEventListener('load', PAGE_WebSpeaker_create, false);
	} else {
		window.attachEvent('onload', PAGE_WebSpeaker_create);
	}
}

if (jQuery.browser.msie) {
	jQuery(window).scroll(function() {
		if(parseFloat(jQuery.browser.version) < 7) {
			jQuery("#stc-webspeaker-block").css("bottom", "").css("bottom", "0px");
			jQuery("#stc-webspeaker-applet").css("bottom", "").css("bottom", "49px");
		} else {
			jQuery("#stc-webspeaker-block").css("bottom", - jQuery(document).scrollTop());
			jQuery("#stc-webspeaker-applet").css("bottom", 49 - jQuery(document).scrollTop());
		}
	});
}
//  ----------------------------------------------------------------------------
function trim(s)
{
    var l=0;
    var r=s.length -1;
    while(l < s.length && s.charAt(l) == ' ') l++;
    while(r > l && s.charAt(r) == ' ') r-=1;
    return s.substring(l, r+1);
}

function clearToken(s, isTTS) {
	if (isTTS) {
		s = s.replace(/[^а-яё@\.,\?\!0-9a-z()-*&\^:';%$#№]/gi, ' ');
	} else {
		s = s.replace(/[^а-яё@]/gi, ' ');
	}
    s = s.replace(/\s+/gi, ' ');
	s = trim(s);

	s1 = s.split(' ').join('');

	if (s1.substring(0, s1.length/2) == s1.substring(s1.length/2))
		s = s.substring(0, s.length/2);

    return s;
}
// -----------------------------------------------------------------------------


// search command in lastVocabulary and commonVocabulary
// if find this command, than return action
function getCommandByName(cmd) {
    for (i=0; i<lastVocabulary.length; i++)
        if (cmd == lastVocabulary[i].command)
            return lastVocabulary[i];

    for (i=0; i<commonVocabulary.length; i++)
        if (cmd == commonVocabulary[i].command)
            return commonVocabulary[i];

    return null;
}

var saveAnchor = null;
var saveAnchorBackgroundColor = null;

//change color of selected anchor
function gotoAnchor(anchor) {
	saveAnchor = anchor;
	saveAnchorBackgroundColor = saveAnchor.style.backgroundColor;
	saveAnchor.style.backgroundColor = "red";

	setTimeout(function() {
		if (saveAnchor != null) {
			saveAnchor.style.backgroundColor = saveAnchorBackgroundColor;

			rel = jQuery(saveAnchor).attr("rel");
			if (!rel && rel != "nofollow") {
				try {
					document.STCWebSpeaker.dontCloseConnections();
				} catch (ex) {}
				location.href = saveAnchor.href;
			}

			saveAnchor = null;
		}
	}, 2000);
}

//do it after find current text
function STC_WebSpeaker_recognitionResult(cmd) {
	stcRecognitionFailureCount = 0;
    PAGE_WebSpeaker_displayResult(cmd);

	var command = getCommandByName(cmd);
	var action = command.action;

	if (!action) {
		return;
	}

    if (action == 'ancor') {
		gotoAnchor(command.object);
		return;
	}

	if (action == "action") {
		try {
			document.STCWebSpeaker.dontCloseConnections();
		} catch (ex) {}
		location.href = command.object;
		return;
	}

    // ���������� ����� �������
    if (PAGE_WebSpeaker_processCommonCommand(action)) {
		return;
    }

	// ���� ���� ����������, �� ��������
	if (action && HANDLER_WebSpeaker_recognitionResult) {
		HANDLER_WebSpeaker_recognitionResult(cmd, action);
	}
}

function PAGE_WebSpeaker_processCommonCommand(action) {
	if (action == "close") {
		STCTurnOff();
		return true;
	}
	else if (action == "page_back") {
		history.go(-1);
		return true;
	}
	else if (action == "page_forward") {
		history.go(1);
		return true;
	}
	else if (action == "page_up") {
		pageScrollUp(600);
		return true;
	}
	else if (action == "page_down") {
		pageScrollDown(600);
		return true;
	}
	else if (action == "page_refresh") {
		try {
			document.STCWebSpeaker.dontCloseConnections();
		} catch (ex) {}
		location.reload(true);
	}
	else if (action == "main_page") {
		try {
			document.STCWebSpeaker.dontCloseConnections();
		} catch (ex) {}
		document.location.href = PARAM_WebSpeaker_homePageUrl;
		return true;
	}
    else if (action == "speek_content") {
		for (var i in contentTTSText) {
			STCWebSpeakerLoadText(contentTTSText[i].text, contentTTSText[i].voiceIndex);
		}
		return true;
    }
    else if (action == "next_menu_item" && menuTTSText!=null && menuTTSText.length>0) {
		STCWebSpeakerLoadText(nextArrayItem(menuTTSText), currentVoiceIndex);
		return true;
	}
	else if (action == "first_menu_item" && menuTTSText!=null && menuTTSText.length>0) {
		STCWebSpeakerLoadText(firstArrayItem(menuTTSText), currentVoiceIndex);
		return true;
	}
    else if (action == "previous_menu_item" && menuTTSText!=null && menuTTSText.length>0) {
		STCWebSpeakerLoadText(previousArrayItem(menuTTSText), currentVoiceIndex);
		return true;
	}
    else if (action == "current_menu_item" && menuTTSText!=null && menuTTSText.length>0) {
		STCWebSpeakerLoadText(currentArrayItem (menuTTSText), currentVoiceIndex);
		return true;
	}
    else if (action == "goto_current_menu_item" && menuTTSText!=null && menuTTSText.length>0) {
		try {
			document.STCWebSpeaker.dontCloseConnections();
		} catch (ex) {}
		STC_WebSpeaker_recognitionResult(getCurrentMenuItem(menuTTSText));
		return true;
	}
	else if (action == "help_menu" && menuTTSText!=null && menuTTSText.length>0) {
		STCWebSpeakerLoadText(helpMenu(), currentVoiceIndex);
		return true;
	}
	else if (action == "first_services_item" && servicesTTSText!=null && servicesTTSText.length>0) {
		STCWebSpeakerLoadText(firstArrayItem(servicesTTSText), currentVoiceIndex);
		return true;
	}
	else if (action == "previous_services_item" && servicesTTSText!=null && servicesTTSText.length>0) {
		STCWebSpeakerLoadText(previousArrayItem(servicesTTSText), currentVoiceIndex);
		return true;
	}
    else if (action == "next_services_item" && servicesTTSText!=null && servicesTTSText.length>0) {
		STCWebSpeakerLoadText(nextArrayItem(servicesTTSText), currentVoiceIndex);
		return true;
	}
    else if (action == "current_services_item" && servicesTTSText!=null && servicesTTSText.length>0) {
		STCWebSpeakerLoadText(currentArrayItem (servicesTTSText), currentVoiceIndex);
		return true;
	}
    else if (action == "goto_current_services_item" && servicesTTSText!=null && servicesTTSText.length>0) {
		try {
			document.STCWebSpeaker.dontCloseConnections();
		} catch (ex) {}
		STC_WebSpeaker_recognitionResult(getCurrentMenuItem(servicesTTSText));
		return true;
	}
	else if (action == "help_service" && menuTTSText!=null && menuTTSText.length>0) {
		STCWebSpeakerLoadText(helpService(), currentVoiceIndex);
		return true;
	}
	else if (action == "read_selected") {
		STCWebSpeakerSpeakSelection();
	}
	else if (action == "help_general" && menuTTSText!=null && menuTTSText.length>0) {
		STCWebSpeakerLoadText(helpGeneral(), currentVoiceIndex);
		return true;
	}
	return false;
}

function pageScrollUp(height) {
	window.scrollBy(0, - height);
	// for (i=1; i<height; i+=5) window.scrollBy(0, -5);
}
function pageScrollDown(height) {
	window.scrollBy(0, height);
	// for (i=1; i<height; i+=5) window.scrollBy(0, 5);
}

var commonTitle = "Произнесите название раздела<br>или одну из команд";
var commonCommandsText = "<ul>";
	commonCommandsText	+= "<li>Главная страница</li>";
	commonCommandsText	+= "<li>Обновить страницу</li>";
	commonCommandsText	+= "<li>Страница вверх</li>";
	commonCommandsText	+= "<li>Страница вниз</li>";
	commonCommandsText	+= "<li>Страница вперед</li>";
	commonCommandsText	+= "<li>Страница назад</li>";
	commonCommandsText	+= "<li>Помощь</li>";
	commonCommandsText	+= "</ul>";

// Add addition content to applet
function PAGE_WebSpeaker_displayStatus(ready) {
	if (ready) {
		PAGE_WebSpeaker_setHeader(stcWebspeakerExpanded ? "Готов к работе" : "Голосовое управление", 'stc-active');
		jQuery("#stc-webspeaker #title").html(commonTitle);
		jQuery("#stc-webspeaker #commands").html(commonCommandsText);
		jQuery("#stc-webspeaker-buttons").show();
		//jQuery("#stcAppletButton").removeClass().addClass('stcAppletButtonActive');
		stcWebspeakerStatus = STC_STATUS_ENABLED;
	} else {
		PAGE_WebSpeaker_setHeader("Подготовка ...", 'stc-init');
		jQuery("#stc-webspeaker #title").html("");
		jQuery("#stc-webspeaker #commands").html("");
		jQuery("#stc-webspeaker-buttons").hide();
		//jQuery("#stcAppletButton").removeClass().addClass('stcAppletButtonInit');
	}
}

function PAGE_WebSpeaker_setHeader(label, clazz) {
	jQuery("#stc-webspeaker-top div").html(label);
	jQuery("#stc-webspeaker-top").removeClass().addClass(clazz);
	var stcVoiceButton = jQuery('#stcVoiceButton img');
	if (stcVoiceButton.length > 0) {
		if (clazz != 'stc-active') {
			stcVoiceButton.attr('title', 'Включить голосовой интерфейс').attr('alt', 'Включить голосовой интерфейс');
		} else {
			stcVoiceButton.attr('title', 'Выключить голосовой интерфейс').attr('alt', 'Выключить голосовой интерфейс');
		}
	}
}

function PAGE_WebSpeaker_displayError(messageId) {
	stcWebspeakerStatus = STC_STATUS_ERROR;
	var message = 'Неизвестная ошибка';
	var title = "Ошибка";
	switch (messageId) {
		//case 'e_cant_connect': message='Не удалось подключиться к серверу.'; break;
		case 'e_cant_connect':
		case 'e_no_connections':
			message='Голосовой интерфейс не может быть включен. Пожалуйста, попробуйте позже.';
			break;
		case 'e_server_error':
			message='Во время подключения к серверу возникла ошибка. Попробуйте воспользоваться голосовым управлением позже.';
			break;
		case 'e_microphone':
			message='Ошибка при подключении к микрофону. Пожалуйста, обновите страницу.';
			break;
		case 'e_sound':
			message='Ошибка при выводе звука.';
			break;
		case 'no_java':
			message='На вашем компютере не установлена Java. <a href="http://java.com" target="_blank">Установить</a>';
			break;
		case 'e_busy':
			message='Голосовое управление запущено в другой вкладке.';
			title='Выключен';
			break;
		case 'e_timeout' :
			title='Выключен';
			message='Голосовой интерфейс выключен по неактивности';
			STCTimeOut();
			break;
		case 'e_speed':
			message='Недостаточная скорость интернет канала.';
			break;
	}
	PAGE_WebSpeaker_setHeader(title, 'stc-inactive');
	jQuery("#stc-webspeaker #title").html(message);
	jQuery("#stc-webspeaker #commands").html("");

	//jQuery("#stcAppletButton").removeClass().addClass('stcAppletButtonInactive');
	STCExpandApplet();
}


//Add and clear result in applet addition
var resultTimerId = 0;
function PAGE_WebSpeaker_displayResult(text) {
    jQuery("#stc-webspeaker-result div").html(text);
	clearTimeout(resultTimerId);
    resultTimerId = window.setTimeout("PAGE_WebSpeaker_clearResult()", 3000);
}
function PAGE_WebSpeaker_clearResult() {
    jQuery("#stc-webspeaker-result div").html("");
}

function stcAppletButtonClick() {
	if (jQuery("#stc-webspeaker-container").queue().length > 0) {
		return;
	}
	if (stcWebspeakerStatus != STC_STATUS_DISABLED) {
		STCTurnOff();
	} else {
		STCTurnOn();
	}
}

window.STC_WebSpeaker_created = false;
// Add applet and addition parameters(as status, title, etc) to web page
function PAGE_WebSpeaker_create() {
	if (window.STC_WebSpeaker_created == true) {
		return;
	}
	window.STC_WebSpeaker_created = true;
	if (jQuery('#stc-webspeaker-block').length > 0) {
		return;
	}
	if (stcCheckJava && typeof(deployJava) != 'undefined') {
		try {
			if (!deployJava.versionCheck('1.5+')) {
				//alert('no java');
				return;
			}
		} catch (ex) {
			//alert(ex);
			return;
		}
	}
	var s = '';

    s += '<div id="stc-webspeaker-block">';
    s += '	<div id="stc-webspeaker-top"><div>Подготовка ...</div></div>';
    s += '	<div id="stc-webspeaker-container">';
    s += '		<div id="stc-webspeaker">';
    s += '			<h2 id="title"></h2>';
    s += '			<div id="commands"></div>';
    s += '		</div>';
    s += '		<div id="stc-webspeaker-result"><div></div></div>';
    s += '		<div id="stc-webspeaker-buttons" style="display:none">';
    s += '			<a id="stc-button-play" class="stc-button" href="javascript:void(0)" onclick="STCWebSpeakerStartSpeak()" title="Озвучить выделенный текст или всю страницу"></a>';
    s += '			<a id="stc-button-stop" class="stc-button" href="javascript:void(0)" onclick="STCWebSpeakerStopSpeak()" title="Остановить воспроизведение"></a>';
    s += '		</div>';
    s += '	</div>';
    s += '</div>';

    jQuery("body").prepend(s);
	jQuery("#stc-webspeaker-top").click(function(e) {
		if (!stcWebspeakerExpanded){
			if (stcWebspeakerStatus != STC_STATUS_ENABLED && stcWebspeakerStatus != STC_STATUS_INITIALIZING) {
				STCTurnOff();
			} else {
				STCExpandApplet();
			}
		} else {
			STCCollapseApplet();
		}
	});

	jQuery(document).mousedown(PAGE_WebSpeaker_mouseDown);

	jQuery("#stc-webspeaker-block").hide();
	if (!jQuery.browser.msie) {
		jQuery("#stc-webspeaker-block").css("position", "fixed");
	}
	var cookie = STC_get_cookie('WebSpeakerEnabled');
	//alert(cookie);
	if (cookie == '1' || cookie == '2') {
		setTimeout(STCTurnOn, 50);
	}

	PAGE_WebSpeaker_create_icon();

	//jQuery("#stcAppletButton").click(stcAppletButtonClick).removeClass().
	//	addClass('stcAppletButtonInactive').attr('title', 'Включить голосовой интерфейс');

	var config = jQuery("div.ASRConfig");
	if (config.length > 0) {
		menuTTSEnabled = config.attr("menuTTSEnabled") == '1';
		servicesTTSEnabled = config.attr("servicesTTSenabled") == '1';
	}
}

function PAGE_WebSpeaker_mouseDown(ev) {
	if (stcWebspeakerStatus != STC_STATUS_ENABLED) {
		return;
	}
	if (ev.which == 3) {
		STCWebSpeakerSpeakSelection();
	}
}

// Get text from div with class tts and add content to text variable
// add text variable to applet (load text method)
var menuTTSText;

var servicesTTSText;

var initTTSText;
var contentTTSText;
var stcWebspeakerStatus = STC_STATUS_DISABLED;
var stcWebspeakerExpanded = false;
var stcWebseaketAppletCreated = false;

function PAGE_WebSpeaker_loadText() {

    initTTSText = PAGE_WebSpeaker_getTextByDivName("INITTTS");
    if (initTTSText == "" && stcSpeakTitle) {
		initTTSText = PAGE_WebSpeaker_getTextFromTitle();
	}

    menuTTSText = [];
	PAGE_WebSpeaker_getArrayTextBySelector('div.MENUTTS', menuTTSText);
	if (PARAM_WebSpeaker_menuSelectors && PARAM_WebSpeaker_menuSelectors.length > 0) {
		for (var i = 0; i < PARAM_WebSpeaker_menuSelectors.length; i++) {
			PAGE_WebSpeaker_getArrayTextBySelector(PARAM_WebSpeaker_menuSelectors[i], menuTTSText);
		}
	}
	menuTTSText.index = 0;
	menuTTSText.endMenu = ". Вы находитесь в конце списка разделов. ";
	menuTTSText.startMenu = ". Вы находитесь в начале списка разделов. ";
	menuTTSText.firstMenuItem = "Первый раздел: ";
	menuTTSText.menuHelp = "Помощь по разделам. Для навигации по разделам вы можете сказать: Следующий раздел!, Предыдущий раздел!, Текущий раздел!, Перейти в раздел!";

    servicesTTSText = [];
	PAGE_WebSpeaker_getArrayTextBySelector('div.SERVICETTS', servicesTTSText);
	if (PARAM_WebSpeaker_serviceSelectors && PARAM_WebSpeaker_serviceSelectors.length > 0) {
		for (var i = 0; i < PARAM_WebSpeaker_serviceSelectors.length; i++) {
			PAGE_WebSpeaker_getArrayTextBySelector(PARAM_WebSpeaker_serviceSelectors[i], servicesTTSText);
		}
	}
	servicesTTSText.index = 0;
	servicesTTSText.endMenu = ". Вы находитесь в конце списка услуг. ";
	servicesTTSText.startMenu = ". Вы находитесь в начале списка услуг. ";
	servicesTTSText.firstMenuItem = "Первая услуга: ";
	servicesTTSText.menuHelp = "Помощь по услугам. Для навигации по услугам вы можете сказать: Следующая услуга, Предыдущая услуга, Текущая услуга, Выбрать услугу";

    contentTTSText = [];
    jQuery("div.TTS").each(function() {
		if (jQuery(this).parents("div.NOTTS,div.TTS").length > 0) {
			return;
		}
		var text = clearToken(_readTextAndFromChildren(jQuery(this).get(0)), true);
		var voiceIndex = jQuery(this).attr('voice') == '0' ? 0 : 1;
		if (text.length > 500) {
			var salt = '=0lXIcrFQm6=';
			var rows = text.split('.').join('. '+salt).split('\n').join(' '+salt).split(salt);
			var s = "";
			for (var i = 0; i < rows.length; i++) {
				s += rows[i];
				if (s.length > 400) {
					contentTTSText.push({text: s, voiceIndex: voiceIndex});
					s = "";
				}
			}
			if (s.length > 0) {
				contentTTSText.push({text: s, voiceIndex: voiceIndex});
			}
		} else {
			contentTTSText.push({text: text, voiceIndex: voiceIndex});
		}
    });
}

function _readTextAndFromChildren(element) {
    var text = '';
    switch(element.nodeType){
        case 1 : // element
        {
            if (element.tagName == 'DIV' && jQuery(element).hasClass('NOTTS')) {
                return '';
            }
            var children = element.childNodes;
            for(var index=0;index<children.length;index++) {
                var textfromchild = _readTextAndFromChildren(children[index]);
                text += ' '+textfromchild;
            }
        }
        break;
        case 3 : // text
        {
            text = element.textContent ? element.textContent : element.nodeValue;
        }
        break;
    }
    return text;
}


function PAGE_WebSpeaker_getTextByDivName(divName) {
    var text = "";
    jQuery("DIV." + divName).each(function(){
		text += _readTextAndFromChildren(jQuery(this).get(0));
        text += ".\n";
    });
    return text;
}

function PAGE_WebSpeaker_getArrayTextBySelector(divName, textArray) {
	var text = "";
	jQuery(divName).each(function(){
		text = clearToken(jQuery(this).html(), false);
		textArray.push(text);
	});
    return textArray;

}

function PAGE_WebSpeaker_getTextFromTitle(){
    return text = clearToken(jQuery("title").html(), true);
}

function StcPrepareCommand(command) {
	for (var i = 0; i < 3; i++) {
		command = command.replace(/\s+/m, " ");
	}
	command = trim(command);
	var arr = command.split(" ", 3);
	arr = arr.slice(0,3);
	command = arr.join(" ");
	if (command.charAt(command.length-1) == ' ') {
		command = command.substring(0, command.length-1);
	}
	return command;
}

// analize page, find <a>, <div class="asr"> and class="command"
// and add it to vocabulary pair command and action
function PAGE_WebSpeaker_loadVocabulary() {
	if (menuTTSEnabled == 'auto') {
		menuTTSEnabled = menuTTSText.length > 0;
	}
	if (servicesTTSEnabled == 'auto') {
		servicesTTSEnabled = servicesTTSText.length > 0;
	}
	//browser grammar
	commonVocabulary.push(new WebSpeakerCommand("Страница вниз", "page_down"));
	commonVocabulary.push(new WebSpeakerCommand("Страница вверх", "page_up"));
	commonVocabulary.push(new WebSpeakerCommand("Страница вперед", "page_forward"));
	commonVocabulary.push(new WebSpeakerCommand("Страница назад", "page_back"));
	commonVocabulary.push(new WebSpeakerCommand("Главная страница", "main_page"));
	commonVocabulary.push(new WebSpeakerCommand("Обновить страницу", "page_refresh"));

	//page common grammar
	commonVocabulary.push(new WebSpeakerCommand("Прослушать контент", "speek_content"));
	commonVocabulary.push(new WebSpeakerCommand("Озвучить страницу", "speek_content"));
	commonVocabulary.push(new WebSpeakerCommand("Прочитать страницу", "speek_content"));
	commonVocabulary.push(new WebSpeakerCommand("Прочитать текст страницы", "speek_content"));

	//help command
	commonVocabulary.push(new WebSpeakerCommand("Помощь", "help_general"));
	commonVocabulary.push(new WebSpeakerCommand("Как пользоваться сервисом", "help_general"));

	if (menuTTSEnabled) {
		//main section grammar
		commonVocabulary.push(new WebSpeakerCommand("Разделы", "first_menu_item"));
		commonVocabulary.push(new WebSpeakerCommand("Предыдущий раздел", "previous_menu_item"));
		commonVocabulary.push(new WebSpeakerCommand("Следующий раздел", "next_menu_item"));
		commonVocabulary.push(new WebSpeakerCommand("Текущий раздел", "current_menu_item"));
		commonVocabulary.push(new WebSpeakerCommand("Перейти в раздел", "goto_current_menu_item"));
		commonVocabulary.push(new WebSpeakerCommand("Выбрать раздел", "goto_current_menu_item"));
		commonVocabulary.push(new WebSpeakerCommand("Помощь по разделам", "help_menu"));
	}

	if (servicesTTSEnabled) {
		//main services grammar
		commonVocabulary.push(new WebSpeakerCommand("Услуги", "first_services_item"));
		commonVocabulary.push(new WebSpeakerCommand("Предыдущая услуга", "previous_services_item"));
		commonVocabulary.push(new WebSpeakerCommand("Следующая услуга", "next_services_item"));
		commonVocabulary.push(new WebSpeakerCommand("Текущая услуга", "current_services_item"));
		commonVocabulary.push(new WebSpeakerCommand("Выбрать услугу", "goto_current_services_item"));
		commonVocabulary.push(new WebSpeakerCommand("Помощь по услугам", "help_service"));
	}

	//speak selection grammar
	commonVocabulary.push(new WebSpeakerCommand("Прочитать", "read_selected"));
	commonVocabulary.push(new WebSpeakerCommand("Прочитай", "read_selected"));
	commonVocabulary.push(new WebSpeakerCommand("Прочитать выделенный текст", "read_selected"));
	commonVocabulary.push(new WebSpeakerCommand("Озвучить выделенный текст", "read_selected"));

	//turn off
	commonVocabulary.push(new WebSpeakerCommand("Выключись", "close"));
	commonVocabulary.push(new WebSpeakerCommand("Выключить", "close"));
	commonVocabulary.push(new WebSpeakerCommand("Закройся", "close"));
	commonVocabulary.push(new WebSpeakerCommand("Закрыть", "close"));

	//  ----------------------------------------------------------------------------

    lastVocabulary = [];

    if (PARAM_WebSpeaker_vocabularyByAnchors) {
		jQuery("A").each(function(){
			if (!this.href) {
				return;
			}
			command = StcPrepareCommand(clearToken(jQuery(this).html()));
			action = this.href;
			if (command && command.length > 0 && getCommandByName(command) == null)
			lastVocabulary.push(new WebSpeakerCommand(command, 'ancor', this));
        });
    }
    jQuery("DIV.ASR .command").each(function(){
		if (!jQuery(this).attr("command") || !jQuery(this).attr("action")) {
			return;
		}
        var command = StcPrepareCommand(clearToken(jQuery(this).attr("command")));
        var action = jQuery(this).attr("action");
        if (command && command.length > 0 && getCommandByName(command) == null)
            lastVocabulary.push(new WebSpeakerCommand(command, 'action', action));
    });

    var data = [];

    for (i=0; i<lastVocabulary.length; i++)
        data.push(lastVocabulary[i].command);

    for (i=0; i<commonVocabulary.length; i++)
        data.push(commonVocabulary[i].command);

	if(data!=null) {
		document.STCWebSpeaker.loadVocabulary2(data.join("\n"));
	}
}


function firstArrayItem(array){
	array.index = 0;
	var menuText = array.firstMenuItem;
	menuText += array[array.index];
	return menuText
}

function nextArrayItem(array) {
	var menuText = "";
	if(array.index < array.length - 1) {
        array.index++;
		menuText = array[array.index];
		if ((array.length - 1) - array.index == 0) {
			menuText += array.endMenu;
		}
	} else {
		menuText = array[array.index];
		menuText += array.endMenu;
	}
	return menuText
}

function previousArrayItem(array){
	var menuText = "";
    if (array.index > 0) {
        array.index--;
        menuText = array[array.index];
		if (array.index == 0) {
			menuText += array.startMenu;
		}
    } else {
		menuText = array[array.index];
		menuText += array.startMenu;
	}
    return menuText;
}

function currentArrayItem(array) {
    return array[array.index];
}

function helpMenu(){
    return menuTTSText.menuHelp;
}

function helpService(){
    return servicesTTSText.menuHelp;
}

function helpGeneral(){
    return stcGeneralHelp;
}

function getCurrentMenuItem(array){
	return array[array.index];
}

function PAGE_WebSpeaker_getSelectedText() {
	if (window.getSelection) return window.getSelection();
	if (document.getSelection) return document.getSelection();
	if (document.selection) return document.selection.createRange().text;
	return "";
}

function STC_get_cookie(name) {
  var dcookie = document.cookie;
  var cname = name + "=";
  var clen = dcookie.length;
  var cbegin = 0;
  while (cbegin < clen) {
    var vbegin = cbegin + cname.length;
    if (dcookie.substring(cbegin, vbegin) == cname) {
      var vend = dcookie.indexOf (";", vbegin);
      if (vend == -1) vend = clen;
		return unescape(dcookie.substring(vbegin, vend));
      }
    cbegin = dcookie.indexOf(" ", cbegin) + 1;
    if (cbegin == 0) break;
    }
  return null;
}

function STC_set_cookie(name, value) {
	var d = null;
	if (value) {
		d = new Date(new Date().getTime() + 900000); //15 min
	} else {
		d = new Date(new Date().getTime() - 1000);
	}
	document.cookie = name+"="+value+"; path=/; expires=" + d.toGMTString();
}
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head><title>Redirect to ACS</title></head>
<body onload="document.forms['acs'].submit()">
<form id="acs" method="post" action="<c:out value="${acsUrl}"/>">
  <input type="hidden" id="MD" name="MD" value="<c:out value="${mdOrder}"/>"/>
  <input type="hidden" id="PaReq" name="PaReq" value="${paReq}"/>
  <input type="hidden" id="TermUrl" name="TermUrl" value="${termUrl}"/>
</form>
</body>
</html>
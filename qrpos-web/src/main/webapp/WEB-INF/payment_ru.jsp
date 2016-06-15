<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <title>Оплата банковской картой</title>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">

    <script type="text/javascript" src="resources/js/jquery-1.4.2.min.js"></script>
    <script type="text/javascript" src="resources/js/jquery.url.js"></script>

    <script type="text/javascript" src="resources/js/jquery.shell.js"></script>

</head>
<body>
Оплата заказа на сумму ${merchantOrder.amount} руб. <br/>

<form action="${pageContext.request.contextPath}/order/payment" method="post">
    <table>
        <tr>
            <td>PAN</td>
            <td colspan="2">
                <select name="paymentParams.pan">
                    <option value="5555555555555599">5555555555555599</option>
                    <option value="4111111111111111">4111111111111111</option>
                </select>
            </td>
        </tr>
        <tr>
            <td>Expiry</td>
            <td>
                <select name="paymentParams.month">
                    <option value="01">01</option>
                    <option value="02">02</option>
                    <option value="03">03</option>
                    <option value="04">04</option>
                    <option value="05">05</option>
                    <option value="06">06</option>
                    <option value="07">07</option>
                    <option value="08">08</option>
                    <option value="09">09</option>
                    <option value="10">10</option>
                    <option value="11">11</option>
                    <option selected="true" value="12">12</option>
                </select>
            </td>
            <td>
                <select name="paymentParams.year">
                    <option value="2016">2016</option>
                    <option value="2017">2017</option>
                    <option value="2018">2018</option>
                    <option selected="true" value="2019">2019</option>
                    <option value="2020">2020</option>
                </select>
            </td>
        </tr>
        <tr>
            <td>CARD HOLDER</td>
            <td colspan="2">
                <input name="paymentParams.cardHolderName" value="test test"/>
            </td>
        </tr>
        <tr>
            <td>Cvc</td>
            <td colspan="2">
                <input name="paymentParams.cvc" size="4" value="123"/>
            </td>
        </tr>
        <tr>
            <td colspan="3">
                <input type="submit" value="Оплатить"/>
            </td>
        </tr>
    </table>
    <input type="hidden" name="paymentWay" value="card"/>
    <input type="hidden" name="orderId" value="${merchantOrder.orderId}"/>
</form>

</body>
</html>


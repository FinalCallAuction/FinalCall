<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Payment Confirmation</title>
</head>
<body>
    <h1>Payment Confirmation</h1>
    
    <c:if test="${not empty amount}">
        <p>Payment Amount: ${amount}</p>
        <p>Currency: ${currency}</p>
        <p>Payment Method: ${paymentMethod}</p>
    </c:if>

    <c:if test="${empty amount}">
        <p>Unable to display payment details. Please go back and try again.</p>
    </c:if>

    <a href="${pageContext.request.contextPath}/paymentHome">Back to Payment Home</a>
</body>
</html>

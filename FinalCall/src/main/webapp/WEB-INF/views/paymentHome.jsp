<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Payment Home</title>
</head>
<body>
    <h1>Payment Home</h1>
    <form action="${pageContext.request.contextPath}/processPayment" method="post">
        <label for="amount">Amount:</label>
        <input type="number" id="amount" name="amount" required>
        <br><br>

        <label for="currency">Currency:</label>
        <input type="text" id="currency" name="currency" value="usd" required>
        <br><br>

        <label for="paymentMethod">Payment Method:</label>
        <input type="text" id="paymentMethod" name="paymentMethod" value="card" required>
        <br><br>

        <button type="submit">Make Payment</button>
    </form>

    <c:if test="${not empty error}">
        <div style="color: red;">
            <p>${error}</p>
        </div>
    </c:if>
</body>
</html>

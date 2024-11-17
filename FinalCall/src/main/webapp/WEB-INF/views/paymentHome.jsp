<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Payment Home</title>
</head>
<body>
    <h1>Welcome to Payment Home Page</h1>
    <form action="/processPayment" method="post">
        <label for="amount">Amount:</label>
        <input type="text" id="amount" name="amount" required>
        
        <label for="currency">Currency:</label>
        <input type="text" id="currency" name="currency" required>
        
        <button type="submit">Pay Now</button>
    </form>
</body>
</html>

<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Home</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
    <style>
    </style>
    <script>
        function pop() {
            alert("Please check your email!");
        }
    </script>

</head>
<body>
<div class="container">
    <form class="navbar-form " method="get"  action="Monitor">
        <div class="form-group" >
            <h2>Please input your username : <input type="text" name = "username" class="form-control"></h2><br>
        </div>
        <br>
        <button type="submit" class="btn btn-success" onclick="pop()">Submit</button>

    </form>
</div>
</body>
</html>

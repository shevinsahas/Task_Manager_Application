function LogUser() {
    var username = $('#username').val();
    var password = $('#password').val();

    var loginData = JSON.stringify({
        emailAddress: username,
        password: password
    });

    $.ajax({
        url: "http://localhost:8080/api/auth/login",
        type: 'POST',
        data: loginData,
        contentType: 'application/json',
        success: function(response, status, xhr) {
            var token = xhr.getResponseHeader('Authorization')?.split(' ')[1];
            
            if (token) {
                localStorage.setItem('jwtToken', token);
                localStorage.setItem('username', response.username);
            }

            if (response.role === 'admin') {
                window.location.href = 'admin-dashboard.html';
            } else {
                window.location.href = 'index.html';
            }
        },
        error: function(xhr, status, error) {
            var errorMessage = xhr.responseJSON && xhr.responseJSON.message 
                ? xhr.responseJSON.message 
                : "Login failed. Please try again.";
            swal({
                text: errorMessage,
                icon: "warning",
                button: "OK"
            });
        }
    });
}




function resetForm() {
document.getElementById('username').value = "";
document.getElementById("fname").value = "";
document.getElementById("lname").value = "";
document.getElementById("email").value = "";
document.getElementById("contact").value = "";
document.getElementById("pass").value = "";
document.getElementById("con").value = "";
}


function resetFormLogin() {
document.getElementById("username").value = "";
document.getElementById("password").value = "";
}


 document.getElementById('registrationForm').addEventListener('submit', function (event) {
    event.preventDefault();

    const form = this;
    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }

    registerUser();
});

function registerUser() {
    const fname = document.getElementById('fname').value;
    const lname = document.getElementById('lname').value;
    const email = document.getElementById('email').value;
    const pass = document.getElementById('pass').value;
    const confirmPass = document.getElementById('con').value;
    const contact = document.getElementById('contact').value;
    const username = document.getElementById('username').value;

    if (username.length < 3) {
        swal("Error", "Username must be at least 3 characters long!", "error");
        return;
    }

    if (pass !== confirmPass) {
        swal("Error", "Passwords do not match!", "error");
        return;
    }

    $.ajax({
        method: "POST",
        contentType: "application/json",
        url: "http://localhost:8080/api/auth/register",
        async: true,
        data: JSON.stringify({
            userName: username,
            firstName: fname,
            lastName: lname,
            emailAddress: email,
            contactNumber: contact,
            password: pass
        }),
        success: function () {
            swal("Success", "Registration complete!", "success").then(() => {
                window.location.href = 'login.html';
            });
        },
        error: function (xhr, status, error) {
            if (xhr.responseJSON && xhr.responseJSON.message) {
                swal("Error", xhr.responseJSON.message, "error");
            } else {
                swal("Error", "An unknown error occurred. Please try again.", "error");
            }
        }
    });
}

function resetForm() {
document.getElementById('registrationForm').reset();
}


function saveEmployee() {
    var fname = $('#fname').val();
    var lname = $('#lname').val();
    var email = $('#email').val();
    var pass = $('#pass').val();
    var con = $('#con').val();

    $('.error-text').text('');

    if (fname.trim() === '') {

        swal({
            title: "First name is required",
            button: {
              className: "custom-button-class",
            },
          });
        
       
        return;
    }
    if (lname.trim() === '') {
        swal({
            title: "Last name is required",
            button: {
              className: "custom-button-class",
            },
          });
      
        return;
    }

    if (pass !== con) {
        swal({
            title: "Passwords do not match",
            button: {
              className: "custom-button-class",
            },
          });
      
        return;
    }

    $.ajax({
      method: "POST",
      contentType: "application/json",
      url: "http://localhost:8080/api/auth/register",
      async: true,
      data: JSON.stringify({
          "firstname": fname,
          "lastname": lname,
          "email": email,
          "password": pass
      }),
      success: function (data) {
          swal({
              title: "Good job!",
              text: "Registered!",
              icon: "success",
              button: "OK!",
          }).then(() => {
              window.location.href = "login.html";
          });
          resetForm();
      },
      error: function (xhr, status, error) {
          if (xhr.responseJSON && xhr.responseJSON.message) {
              alert("Error Message: " + xhr.responseJSON.message);
          } else {
              alert("Unknown Error Occurred");
          }
      }
  });
  
}





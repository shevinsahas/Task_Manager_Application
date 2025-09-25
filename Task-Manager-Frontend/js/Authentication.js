
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





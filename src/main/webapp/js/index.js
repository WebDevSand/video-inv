/**
 * Index Page Functions.
 * Manages User Status (Login/Log out), etc.
 *
 * Created by tpaulus on 4/28/16.
 */

var changesMade = false;
var user = null;

var pageHistory = ['index'];

window.onload = function () {
    checkLoggedIn();
    loadView("index");
};

function checkLoggedIn() {
    var userInfo = Cookies.getJSON("user");
    var sessionToken = Cookies.get("session");
    if (sessionToken != null && sessionToken.length > 0 && userInfo != null) {
        var xmlHttp = new XMLHttpRequest();

        xmlHttp.onreadystatechange = function () {
            if (xmlHttp.readyState == 4) {
                var response = xmlHttp;
                console.log("Status: " + response.status);
                console.log("Response:" + response.responseText);

                if (response.status == 200) {
                    console.log("Previous Session token is still valid, auto-logging in");
                    doStaffLogin(userInfo);
                } else {
                    console.log("Previous session token is no longer valid. Please login again.")
                }
            }
        };

        xmlHttp.open('get', "api/session/verify", true);
        xmlHttp.setRequestHeader("session", sessionToken);
        xmlHttp.send();
    }
}

function showHome() {
    if (user != null) showPage("welcome", "menu-home");
    else showPage("index", "menu-home");
}

function loadView(viewName) {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            $('#view-container').html(xmlHttp.responseText);
        }
    };

    xmlHttp.open('get', "views/" + viewName + ".view", true);
    xmlHttp.send();
}

function loadChildView(childName) {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            $('#view-container-child').html(xmlHttp.responseText);
        }
    };

    xmlHttp.open('get', "views/" + pageHistory[pageHistory.length - 1] + "-" + childName + ".view", true);
    xmlHttp.send();
}

function showPage(pageName, navMenuID) {
    // Will only work if the user is logged in.
    if (pageName == "index" || user != null && pageName != pageHistory[pageHistory.length - 1]) {
        // Change the page only if page hasn't change
        if (changesMade) {
            swal({
                title: "Are you sure?",
                text: "Any and all changes you have made will be lost forever!",
                type: "warning",
                showCancelButton: true,
                confirmButtonColor: "#DD6B55",
                confirmButtonText: "Yes, change the page!",
                closeOnConfirm: true,
                html: false
            }, function () {
                changesMade = false;
                showPage(pageName, navMenuID);
            });
        }
        else {
            loadView(pageName);
            pageHistory[pageHistory.length] = pageName;

            if (navMenuID != null) updateNav(navMenuID);
            changesMade = false;
        }
    }
}

function updateNav(newActivePage) {
    $('#navbar').find('.active').removeClass('active');
    $('#' + newActivePage).addClass('active');
}

function showStaffLoginModal() {
    if (user == null) {
        $('#userDropdown').prop("disabled", true);
        $('#staffLoginModal').modal('show');
        $('#inputUsername').focus();
    }
}

function hideStaffLoginModal() {
    $('#staffLoginModal').modal('hide');
    $('#userNotFoundAlert').hide();
}

function loginStaff() {
    $('#userNotFoundAlert').hide();
    var json = '{"username": "' + $('#inputUsername').val() + '",' +
        '"password": "' + btoa($('#inputPassword').val()) + '"' +
        '}';

    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);

            if (response.status == 200) {
                var responseJSON = JSON.parse(xmlHttp.responseText);
                console.log(responseJSON);
                Cookies.set("session", xmlHttp.getResponseHeader("session"), {expires: 1});
                doStaffLogin(responseJSON);
            } else {
                doStaffLogin(null);
            }
        }
    };

    xmlHttp.open('POST', "api/login", true);
    xmlHttp.setRequestHeader("Content-type", "application/json");
    xmlHttp.send(json);
}

function doStaffLogin(userJSON) {
    user = userJSON;
    if (userJSON != null) {
        Cookies.set("user", JSON.stringify(userJSON), {expires: 1});
        $('#header-user-name').html(userJSON.firstName + " " + userJSON.lastName + '<span class="caret"></span>');
        $('#staffLoginModal').modal('hide');
        $('#userDropdown').prop("disabled", false);

        $('#welcomePageName').text(userJSON.firstName + " " + userJSON.lastName);


        $('#link-checkout').removeClass('disabled');
        $('#link-checkin').removeClass('disabled');

        if (userJSON.supervisor) {
            $('#menu-admin').show();
        }

        showPage('welcome');
        setUpdateStaffModalContent(userJSON);
        $('#loginForm')[0].reset();
    } else {
        $('#userNotFoundAlert').show();
    }
}

function setUpdateStaffModalContent(userInfo) {
    $('#updateStaffUsername').text(userInfo.username);
    $('#updateStaffFirstName').val(userInfo.firstName);
    $('#updateStaffLastName').val(userInfo.lastName);
}

function updateStaff() {
    var $password1 = $('#updateStaffPassword1');
    var $password2 = $('#updateStaffPassword2');

    if ($password1.val() != "" && $password1.val() != $password2.val()) {
        $('.passwordInput').addClass("has-error");
        $('.passwordMismatchErr').show();
        return false;
    } else {
        $('.passwordInput').removeClass("has-error");
        $('.passwordMismatchErr').hide();
    }

    var $staffInfoModal = $('#staffInfoModal');
    $staffInfoModal.modal('hide');

    var json = '{' +
        '"username": "' + $('#updateStaffUsername').text() + '",' +
        '"firstName": "' + $('#updateStaffFirstName').val() + '",' +
        '"lastName": "' + $('#updateStaffLastName').val() + '"';

    var updatePassword = $password1.val() != "";

    if (updatePassword) {
        json += ',' +
            '"password": "' + btoa($password1.val()) + '"';
    }

    json += '}';

    if (updatePassword) {
        // Check current password
        swal({
                title: "Quick Question!",
                text: "Before you update your password, what is your current password?",
                type: "input",
                inputType: "password",
                showCancelButton: true,
                closeOnConfirm: false,
                inputPlaceholder: "***********",
                showLoaderOnConfirm: true
            },
            function (inputValue) {
                if (inputValue === false) return false;

                if (inputValue === "") {
                    swal.showInputError("You need to write something!");
                    return false
                }

                var loginJSON = '{"username": "' + user.username + '",' +
                    '"password": "' + btoa(inputValue) + '"' +
                    '}';

                var xmlHttp = new XMLHttpRequest();

                xmlHttp.onreadystatechange = function () {
                    if (xmlHttp.readyState == 4) {
                        var response = xmlHttp;
                        console.log("Status: " + response.status);
                        if (response.status == 200) {
                            doStaffUpdate(json);
                        } else {
                            swal.showInputError("Invalid Password!");
                        }
                    }
                };

                xmlHttp.open('POST', "api/login", true);
                xmlHttp.setRequestHeader("Content-type", "application/json");
                xmlHttp.send(loginJSON);
            });
    } else {
        doStaffUpdate(json);
    }

    setUpdateStaffModalContent(user); // Reset the Modal to the Original Content
}


function doStaffUpdate(json) {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);
            if (response.status == 200) {
                var putJSON = JSON.parse(json);

                user.firstName = putJSON.firstName;
                user.lastName = putJSON.lastName;

                doStaffLogin(user);

                swal("Profile Updated!", "Your changes have been saved!", "success")
            } else {
                swal("Oops...", "Something went wrong.", "error");
                console.log(xmlHttp.responseText);
            }
        }
    };

    xmlHttp.open('PUT', "api/user", true);
    xmlHttp.setRequestHeader("Content-type", "application/json");
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.send(json);

}

function showStaffInfoModal() {
    $('.passwordInput').removeClass("has-error");
    $('.passwordMismatchErr').hide();

    $('#staffInfoModal').modal('show');
}

function logout() {
    user = null;
    Cookies.remove("user");
    Cookies.remove("session");
    showHome();
    pageHistory = ['index']; // Reset Page History for User

    $('#header-user-name').text("Login");
    $('#userDropdown').prop("disabled", true);

    $('#link-checkout').addClass('disabled');
    $('#link-checkin').addClass('disabled');
    $('#menu-admin').hide();

    $('#logoutAlert').show();
    window.setTimeout(function () {
        $('#logoutAlert').alert('close');
    }, 5000)
}
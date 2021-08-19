/**
 * Admin Users View Scripts
 *
 * Created by tpaulus on 1/7/17.
 */

function loadUsers() {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            if (xmlHttp.status == 200) {
                var json = JSON.parse(xmlHttp.responseText);
                var table = $('#user-list')[0];
                for (var i = 0; i < json.length; i++) {
                    var u = json[i];
                    var row = table.insertRow();
                    row.id = "user-" + u.dbID.toString();
                    row.insertCell(0).innerHTML = u.dbID.toString();
                    row.insertCell(1).innerHTML = u.username;
                    row.insertCell(2).innerHTML = u.firstName;
                    row.insertCell(3).innerHTML = u.lastName;
                    if (u.supervisor) row.insertCell(4).innerHTML = '<i class="fa fa-user-o" aria-hidden="true"></i> Supervisor';
                    else row.insertCell(4).innerHTML = '<i class="fa fa-id-badge" aria-hidden="true"></i> User';
                    row.insertCell(5).innerHTML = '<button class="btn btn-default btn-xs" type="button" onclick="showEdit(\'' + u.username + '\');"><i class="fa fa-pencil" aria-hidden="true"></i>&nbsp; Edit</button>';
                }

                sorttable.makeSortable(table);
            }
        }
    };

    xmlHttp.open('GET', "api/user");
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.send();
}

function createUser() {
    var username = $('#createUsername').val();
    var firstName = $('#createFirstName').val();
    var lastName = $('#createLastName').val();
    var password1 = $('#createPassword1').val();
    var password2 = $('#createPassword2').val();
    var supervisor = $('#supervisorUserRadio').is(':checked');

    if (password1 != password2) {
        $('.passwordInput').addClass("has-error");
        $('.passwordMismatchErr').show();
        return false;
    } else {
        $('.passwordInput').removeClass("has-error");
        $('.passwordMismatchErr').hide();
    }

    var json = '  {' +
        '"username": "' + username + '",' +
        '"firstName": "' + firstName + '",' +
        '"lastName": "' + lastName + '",' +
        '"supervisor": ' + supervisor + ',' +
        '"password": "' + btoa(password1) + '"' +
        '}';

    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            if (xmlHttp.status == 201) {
                var $createModal = $('#createModal');
                $createModal.modal('hide');
                $("#user-list").find("tr:gt(0)").remove();
                loadUsers();
                $createModal.find('form').trigger("reset");
            } else {
                swal("Oops...", "Something went wrong!", "error");
                console.log(xmlHttp.responseText);
            }
        }
    };
    xmlHttp.open('POST', "api/user");
    xmlHttp.setRequestHeader("Content-type", "application/json");
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.send(json);
}

function showEdit(username) {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            if (xmlHttp.status == 200) {
                var json = JSON.parse(xmlHttp.responseText);
                var u = json[0];
                // Make sure the password error is hidden
                $('.passwordInput').removeClass("has-error");
                $('.passwordMismatchErr').hide();

                // Set Modal Content
                $('.updateUsername').text(u.username);
                $('#updateFirstName').val(u.firstName);
                $('#updateLastName').val(u.lastName);
                $('#updateUserDBID').val(u.dbID);
                if (u.supervisor) $('#updateSupervisorUserRadio').prop("checked", true);
                else $('#updateRegularUserRadio').prop("checked", true);

                $('#updateModal').modal('show');
            }
        }
    };

    xmlHttp.open('GET', 'api/user?username=' + username);
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.send();
}

function updateUser() {
    var username = $('#updateUsername').text();
    var firstName = $('#updateFirstName').val();
    var lastName = $('#updateLastName').val();
    var password1 = $('#updatePassword1').val();
    var password2 = $('#updatePassword2').val();
    var supervisor = $('#updateSupervisorUserRadio').is(':checked');

    if (password1 != "" && password1 != password2) {
        $('.passwordInput').addClass("has-error");
        $('.passwordMismatchErr').show();
        return false;
    } else {
        $('.passwordInput').removeClass("has-error");
        $('.passwordMismatchErr').hide();
    }

    var json = '  {' +
        '"username": "' + username + '",' +
        '"firstName": "' + firstName + '",' +
        '"lastName": "' + lastName + '",' +
        '"supervisor": ' + supervisor;
    if (password1 != "") json += ', "password": "' + btoa(password1) + '"';
    json += '}';

    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            if (xmlHttp.status == 200) {
                var $updateModal = $('#updateModal');
                $updateModal.modal('hide');
                // Update Table
                var $row = $('#user-' + $('#updateUserDBID').val());
                $row.find("td:nth-child(3)").text(firstName);
                $row.find("td:nth-child(4)").text(lastName);
                if (supervisor) $row.find("td:nth-child(5)").html('<i class="fa fa-user-o" aria-hidden="true"></i> Supervisor');
                else $row.find("td:nth-child(5)").html('<i class="fa fa-id-badge" aria-hidden="true"></i> User');
                sorttable.makeSortable(document.getElementById("user-list"));
                $updateModal.find('form').trigger("reset");
            } else {
                swal("Oops...", JSON.parse(xmlHttp.responseText).message, "error");
                console.log(xmlHttp.responseText);
            }
        }
    };
    xmlHttp.open('PUT', "api/user");
    xmlHttp.setRequestHeader("Content-type", "application/json");
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.send(json);
}

function deleteUser() {
    var username = $("#updateUsername").text();
    var userID = $('#updateUserDBID').val();
    swal({
        title: "Are you sure?",
        text: "This cannot be done is the user has checked out or in ANY items.<br><strong>This cannot be undone!</strong>",
        type: "warning",
        showCancelButton: true,
        confirmButtonColor: "#DD6B55",
        confirmButtonText: "Yes, Delete " + username,
        closeOnConfirm: true,
        html: true
    }, function () {
        $('#updateModal').modal('hide');

        var xmlHttp = new XMLHttpRequest();

        var json = '{' +
            '"username": ' + username +
            '}';

        xmlHttp.onreadystatechange = function () {
            if (xmlHttp.readyState == 4) {
                var response = xmlHttp;
                console.log("Status: " + response.status);

                if (response.status == 200) {
                    swal("User Deleted!", username + " has been deleted!", "success");
                    $('#user-' + userID).remove();
                    sorttable.makeSortable(document.getElementById("user-list"));
                } else {
                    console.log(xmlHttp.responseText);
                    swal("Oops...", JSON.parse(xmlHttp.responseText).message, "error");
                }
            }
        };

        xmlHttp.open('DELETE', "api/user");
        xmlHttp.setRequestHeader("Content-type", "application/json");
        xmlHttp.setRequestHeader("session", Cookies.get("session"));
        xmlHttp.send(json);
    });
}
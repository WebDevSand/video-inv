/**
 * Check in Functions
 *
 * Created by tpaulus on 1/10/17.
 */


var TXComponents = [];
var supervisor = null;

const notifyChime = new Audio("error.ogg");

function loadCheckedOutItems() {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            if (xmlHttp.status == 200) {
                var itemsOut = JSON.parse(xmlHttp.responseText);
                var $table = $('#checkinListContainer').find('table');

                if (itemsOut.length == 0) {
                    $('#noItemsOutPara').show();
                    $table.hide();
                } else {
                    $('#itemsOutPara').show();

                    for (var i = 0; i < itemsOut.length; i++) {
                        var item = itemsOut[i];
                        var row = $table[0].insertRow();
                        row.id = "checked-out-item-" + item.pubID;
                        row.className = "checked-out-items";
                        row.insertCell(0).innerHTML = item.name + " <br> ID: " + item.pubID.toString();
                        row.insertCell(1).innerHTML = item.lastTransactionDate;
                    }
                }
            }
        }
    };

    xmlHttp.open('GET', "api/user/checkedOut");
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.send();
}


function addItem() {
    var $itemID = $('#addItemID');
    if ($itemID.val() == "") return; // Prevent Blank Values

    var $addItemButton = $('#addItemButton');
    var orgBtnValue = $addItemButton.html();
    $addItemButton.html('<i class="fa fa-circle-o-notch fa-spin"></i>');

    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            if (xmlHttp.status == 200) {
                var json = JSON.parse(xmlHttp.responseText);
                var table = $('#checkinItems')[0];
                for (var i = 0; i < json.length; i++) {
                    var item = json[i];
                    if (!item.checked_out) {
                        swal("Oops...", "That item is not currently checked out.", "warning");
                        continue;
                    }
                    if (TXComponents.some(function (c) {
                            return c.id == item.id
                        })) continue;  // Already in component list

                    var row = table.insertRow();
                    row.id = "checkin-item-" + item.pubID;

                    if (item.category != null)
                        row.insertCell(0).innerHTML = '<img src="api/category/icon/' + item.category.id + '" class="categoryIcon" id="cat-ico-' + item.category.id + '" onerror="this.style.visibility = \'hidden\'">';
                    else
                        row.insertCell(0).innerHTML = '<img class="categoryIcon" id="null-cat-ico" style="visibility: hidden">';
                    row.insertCell(1).innerHTML = item.pubID;
                    row.insertCell(2).innerHTML = item.name;
                    row.insertCell(3).innerHTML = '<input type="text" class="form-control" id="item-' + item.pubID + '-comments" value="' + item.comments + '">';
                    var rmBtn = row.insertCell(4);
                    rmBtn.className = 'remove';
                    rmBtn.innerHTML = '<i class="fa fa-trash fa-lg" aria-disabled="true" onclick="removeItem(' + item.pubID + ')"></i>';

                    $('#checked-out-item-' + item.pubID).addClass("success");

                    var component = {
                        "id": item.id,
                        "pubID": item.pubID,
                        "catName": item.category.name,
                        "comments": item.comments
                    };
                    TXComponents.push(component)
                }

                changesMade = true;
            } else {
                notifyChime.play();
                swal({
                    type: "error",
                    title: "Invalid ID!",
                    text: "An Item or Macro was not found. Please try again.",
                    timer: 2500,
                    showConfirmButton: false
                });
            }
            $itemID.val("");
            $addItemButton.html(orgBtnValue);
        }
    };

    xmlHttp.open('GET', 'api/item?id=' + $itemID.val());
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.send();
}

function removeItem(pubID) {
    var item = TXComponents.filter(function (c) {
        return c.pubID == pubID
    })[0];

    if (catCount[item.catName] == 1) {
        $('.cat-' + item.catName).removeClass("fa-check-circle").addClass("fa-times-circle")
    }
    catCount[item.catName] -= 1;

    var index = TXComponents.indexOf(item);
    if (index > -1) {
        TXComponents.splice(index, 1);
    }

    $("#checkin-item-" + pubID).remove();
}

function reset() {
    if (changesMade) {
        swal({
            title: "Are you sure?",
            text: "Any and all changes you have made will be lost forever!",
            type: "warning",
            showCancelButton: true,
            confirmButtonColor: "#DD6B55",
            confirmButtonText: "Yes, reset it!",
            closeOnConfirm: true,
            html: false
        }, function () {
            loadView("checkin");
        });
    }
}

function allItemsCheckedIn() {
    return $('.checked-out-items').not(".success").length == 0;
}


function submit() {
    if (!allItemsCheckedIn()) {
        // Change to confirm dialog
        swal({
            title: "Forgot Anything?",
            text: "You still have some items checked out.",
            type: "warning",
            showCancelButton: true,
            confirmButtonColor: "#1B9061",
            confirmButtonText: "Continue Anyway",
            closeOnConfirm: false,
            html: false
        }, function () {
            startSubmitProcess();
        });
    } else {
        startSubmitProcess();
    }
}

function startSubmitProcess() {
    // Start Checkout Process
    if (!user.supervisor && supervisor == null) {
        swal.close();
        $('#supervisorLoginModal').modal('show');
    } else if (!user.supervisor && supervisor != null) {
        swal("Heads Up!", "Supervisor Mode is still active.", "info");
        $('#editToolbar').hide();
        $('#supervisorToolbar').show();
    } else {
        swal("Heads Up!", "Because you are a designated supervisor, you are allowed to approve your own transaction. " +
            "Please look over the transaction one more time, before clicking approve.", "info");
        supervisor = user;
        $('#editToolbar').hide();
        $('#supervisorToolbar').show();
    }
}

function loginSupervisor() {
    $('#supervisorNotFoundAlert').hide();
    $('#supervisorPermissionsAlert').hide();
    var json = '{"username": "' + $('#supervisorUsername').val() + '",' +
        '"password": "' + btoa($('#supervisorPassword').val()) + '"' +
        '}';

    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            if (xmlHttp.status == 200) {
                var responseJSON = JSON.parse(xmlHttp.responseText);
                var $supervisorLoginModal = $('#supervisorLoginModal');

                if (!responseJSON.supervisor) {
                    $('#supervisorPermissionsAlert').show();
                    $supervisorLoginModal.find('form').trigger("reset");
                } else {
                    supervisor = responseJSON;
                    $supervisorLoginModal.modal('hide');
                    swal("Hi there " + supervisor.firstName + "!", "Supervisor mode has been activated. Please review the " +
                        "following transaction and ensure that it is accurate. Once a transaction is approved, it cannot be " +
                        "changed. Your approval will be included in the transaction record.", "info");
                    $('#editToolbar').hide();
                    $('#supervisorToolbar').show();
                    $supervisorLoginModal.find('form').trigger("reset");
                }
            } else {
                $('#supervisorNotFoundAlert').show();
            }
        }
    };

    xmlHttp.open('POST', "api/login", true);
    xmlHttp.setRequestHeader("Content-type", "application/json");
    xmlHttp.send(json);
}

function backToEdit() {
    $('#supervisorToolbar').hide();
    $('#editToolbar').show();
}

function approveTransaction() {
    swal({
            title: "Are you Sure?",
            text: "Once submitted, transactions cannot be modified or deleted.",
            type: "warning",
            confirmButtonColor: "#1B9061",
            confirmButtonText: "Yes, do it!",
            showCancelButton: true,
            closeOnConfirm: false,
            showLoaderOnConfirm: true
        },
        function () {
            for (var c = 0; c < TXComponents.length; c++) {
                var TXC = TXComponents[c];
                TXC.comments = $('#item-' + TXC.pubID + '-comments').val();
            }

            var json = '{' +
                '"owner": {' +
                '    "dbID": ' + user.dbID +
                '},' +
                '"supervisor": {' +
                '    "dbID": ' + supervisor.dbID +
                '},' +
                '"direction": true,' +
                '    "components":' + JSON.stringify(TXComponents) +
                '}';


            var xmlHttp = new XMLHttpRequest();

            xmlHttp.onreadystatechange = function () {
                if (xmlHttp.readyState == 4) {
                    if (xmlHttp.status == 201) {
                        swal("Transaction Created!", "Your transaction record has been logged.", "success");
                        changesMade = false;
                        showHome();
                    } else {
                        swal("Oops...", "Something went wrong.", "error");
                        console.log(xmlHttp.responseText);
                    }
                }
            };

            xmlHttp.open('POST', 'api/transaction');
            xmlHttp.setRequestHeader("Content-type", "application/json");
            xmlHttp.setRequestHeader("session", Cookies.get("session"));
            xmlHttp.send(json);
        }
    );
}
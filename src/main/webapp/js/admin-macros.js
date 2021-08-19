/**
 * Admin Macros View Scripts
 *
 * Created by tpaulus on 1/5/17.
 */

function loadMacros() {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            console.log("Status: " + xmlHttp.status);
            if (xmlHttp.status == 200) {
                var json = JSON.parse(xmlHttp.responseText);
                var table = document.getElementById("macro-list");
                for (var m = 0; m < json.length; m++) {
                    var macro = json[m];

                    var row = table.insertRow();
                    row.id = "macro-" + macro.id;

                    row.insertCell(0).innerHTML = macro.id;
                    row.insertCell(1).innerHTML = macro.name;
                    if (macro.items.length == 0) row.insertCell(2).innerHTML = "0 items";
                    else if (macro.items.length == 1) row.insertCell(2).innerHTML = "1 item";
                    else row.insertCell(2).innerHTML = macro.items.length.toString() + " items";
                    row.insertCell(3).innerHTML = '<button class="btn btn-default btn-xs" type="button" onclick="showEdit(' + macro.id + ');"><i class="fa fa-pencil" aria-hidden="true"></i>&nbsp; Edit</button>';
                }
                sorttable.makeSortable(table);
            }
        }
    };

    xmlHttp.open('GET', "api/macro");
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.send();
}


function loadItemList() {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            console.log("Status: " + xmlHttp.status);
            if (xmlHttp.status == 200) {
                var json = JSON.parse(xmlHttp.responseText);
                var $tableList = $("table.macroItemsList");
                for (var t = 0; t < $tableList.length; t++) {
                    var table = $tableList[t];

                    for (var i = 0; i < json.length; i++) {
                        var item = json[i];
                        var row = table.insertRow();
                        row.insertCell(0).innerHTML = '<input type="checkbox" title="Include in Macro" class="macro-include item-' + item.id + '">';
                        row.insertCell(1).innerHTML = item.name + ((item.shortName != null && item.shortName.length > 0) ? " <i>[" + item.shortName + "]</i>" : "");
                        row.insertCell(2).innerHTML = item.pubID;
                    }
                }
            }
        }
    };

    xmlHttp.open('GET', "api/item");
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.send();
}

function showEdit(macroID) {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            console.log("Status: " + xmlHttp.status);
            if (xmlHttp.status == 200) {
                var json = JSON.parse(xmlHttp.responseText);
                console.log(json);

                $('.updateMacroID').text(json[0].id);
                $('#updateMacroName').val(json[0].name);
                for (var i = 0; i < json[0].items.length; i++) {
                    var item = json[0].items[i];
                    $('#updateMacroItemsList').find('input.item-' + item).prop('checked', true);
                }

                $('#updateModal').modal('show');
            }
        }
    };

    xmlHttp.open('GET', "api/macro?id=" + macroID);
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.send();

}

function updateMacro() {
    var id = $('#updateMacroID').text();
    var name = $('#updateMacroName').val();
    var items = [];
    var $tr = $('#updateMacroItemsList').find('input.macro-include:checked');

    for (var r = 0; r < $tr.length; r++) {
        var row = $tr[r];
        var myRegexp = /item-(\d+)/g;
        var match = myRegexp.exec(row.classList.toString());
        items.push(match[1]);
    }

    var json = '{' +
        '"id": ' + id + ',' +
        '"name": "' + name + '",' +
        '"items": ' + JSON.stringify(items) +
        "}";

    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);
            if (response.status == 200) {
                $("#macro-" + id + " td:nth-child(2)").text(name);

                if (items.length == 0) $("#macro-" + id + " td:nth-child(3)").text("0 items");
                else if (items.length == 1) $("#macro-" + id + " td:nth-child(3)").text("1 item");
                else $("#macro-" + id + " td:nth-child(3)").text(items.length.toString() + " items");

                var $updateModal = $('#updateModal');
                $updateModal.modal('hide');
                $updateModal.find('form').trigger("reset");
                sorttable.makeSortable(document.getElementById('macro-list'));
            } else {
                console.log(xmlHttp.responseText);
                swal("Oops...", "Something went wrong", "error");
            }
        }
    };

    xmlHttp.open('PUT', "api/macro");
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.setRequestHeader("Content-type", "application/json");
    xmlHttp.send(json);
}

function printMacro(macroID) {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);
            if (response.status == 200) {
                makeLabel(response.responseText);
            }
        }
    };

    xmlHttp.open('GET', "api/macro/label?id=" + macroID);
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.send();
}

function reprintMacro() {
    printMacro($('#updateMacroID').text());
}


function createMacro() {
    var name = $('#macroName').val();
    var items = [];
    var $tr = $('#createMacroItemsList').find('input.macro-include:checked');


    for (var r = 0; r < $tr.length; r++) {
        var row = $tr[r];
        var myRegexp = /item-(\d+)/g;
        var match = myRegexp.exec(row.classList.toString());
        items.push(match[1]);
    }

    var json = '{' +
        '"name": "' + name + '",' +
        '"items": ' + JSON.stringify(items) +
        "}";

    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);
            if (response.status == 201) {
                var json = JSON.parse(response.responseText);
                if ($('#printLabel').is(":checked")) {
                    printMacro(json.id);
                }
                var $createModal = $('#createModal');
                $("#macro-list").find("tr:gt(0)").remove();
                loadMacros();

                $createModal.modal('hide');
            } else {
                console.log(xmlHttp.responseText);
                swal("Oops...", "Something went wrong", "error");
            }
        }
    };

    xmlHttp.open('POST', "api/macro");
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.setRequestHeader("Content-type", "application/json");
    xmlHttp.send(json);
}

function deleteMacro() {
    var macroName = $("#updateMacroName").val();
    swal({
        title: "Are you sure?",
        text: "<strong>This cannot be undone!</strong>",
        type: "warning",
        showCancelButton: true,
        confirmButtonColor: "#DD6B55",
        confirmButtonText: "Yes, Delete " + macroName,
        closeOnConfirm: true,
        html: true
    }, function () {
        $('#updateModal').modal('hide');

        var xmlHttp = new XMLHttpRequest();

        var macroID = $('#updateMacroID').text();
        var json = '{' +
            '"id": ' + macroID +
            '}';

        xmlHttp.onreadystatechange = function () {
            if (xmlHttp.readyState == 4) {
                var response = xmlHttp;
                console.log("Status: " + response.status);

                if (response.status == 200) {
                    swal("Macro Deleted!", macroName + " has been deleted!", "success");
                    $('#macro-' + macroID).remove();
                    sorttable.makeSortable(document.getElementById('macro-list'));
                } else {
                    console.log(xmlHttp.responseText);
                    swal("Oops...", JSON.parse(xmlHttp.responseText).message, "error");
                }
            }
        };

        xmlHttp.open('DELETE', "api/macro");
        xmlHttp.setRequestHeader("Content-type", "application/json");
        xmlHttp.setRequestHeader("session", Cookies.get("session"));
        xmlHttp.send(json);
    });
}
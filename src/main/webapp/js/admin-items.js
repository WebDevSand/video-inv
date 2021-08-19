/**
 * Item View Scripts
 *
 * Created by tpaulus on 12/30/16.
 */

function loadItems() {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            if (xmlHttp.status == 200) {
                var json = JSON.parse(xmlHttp.responseText);
                doLoadItems(json);
            }
        }
    };

    xmlHttp.open('GET', "api/item");
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.send();
}

function createItem() {
    var name = $("#itemName").val();
    var short = $("#itemShortName").val();
    var catID = $("#updateItemCategory").val();
    var assetID = $('#assetID').val();
    var xmlHttp = new XMLHttpRequest();

    var json = '{' +
        '"name": "' + name + '" ,' +
        '"shortName": "' + short + '",' +
        '"category" : {' +
        '   "id" :' + catID +
        '   },' +
        '"assetID": "' + assetID + '"' +
        '}';

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);

            if (response.status == 201) {
                var item = JSON.parse(xmlHttp.responseText);
                if ($('#printLabel').is(":checked")) {
                    getLabel(item.pubID);
                }
                var $createModal = $('#createModal');
                $createModal.modal('hide');
                $("#items-list-list").find("tr:gt(0)").remove();
                loadItems();
                $createModal.find('form').trigger("reset");
            }
        }
    };

    xmlHttp.open('POST', "api/item");
    xmlHttp.setRequestHeader("Content-type", "application/json");
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.send(json);
}
/**
 * Get Dymo Label by ID
 * @param id Public ID
 */
function getLabel(id) {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);

            if (response.status == 200) {
                makeLabel(xmlHttp.responseText);
            }
        }
    };

    xmlHttp.open('GET', "api/item/label?id=" + id);
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.send();
}

function updateItem() {
    var pubID = $("#updateItemID").text();
    var name = $("#updateItemName").val();
    var short = $("#updateItemShortName").val();
    var catID = $("#updateItemCategory").val();
    var assetID = $('#updateAssetID').val();
    var xmlHttp = new XMLHttpRequest();

    var json = '{' +
        '"pubID": ' + pubID + ',' +
        '"name": "' + name + '" ,' +
        '"shortName": "' + short + '",' +
        '"category" : {' +
        '   "id" :' + catID +
        '   },' +
        '"assetID": "' + assetID + '"' +
        '}';

    $('#updateModal').modal('hide');

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            console.log("Status: " + xmlHttp.status);

            if (xmlHttp.status == 200) {
                var itemsList = $('#items-list');
                itemsList.find('#item-' + pubID + ' td:nth-child(2)').text(name);
                itemsList.find('#item-' + pubID + ' td:nth-child(3)').text(short);
                itemsList.find('#item-' + pubID + ' td:nth-child(4)').text(assetID);
                itemsList.find('#item-' + pubID + ' td:nth-child(5)').html(catID != 0 ? $("#itemCategory").find("option[value='" + catID + "']").text() : "<i>None</i>");
                sorttable.makeSortable(document.getElementById('items-list'));
                $('#updateModal').find('form').trigger("reset");
            } else {
                swal("Oops...", "Something went wrong!", "error");
                console.log(xmlHttp.responseText);
            }
        }
    };

    xmlHttp.open('PUT', "api/item");
    xmlHttp.setRequestHeader("Content-type", "application/json");
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.send(json);
}

function reprint() {
    getLabel($('#updateItemID').text());
}

function deleteItem() {
    var itemName = $("#updateItemName").val();
    var itemShortName = $("#updateItemShortName").val();
    swal({
        title: "Are you sure?",
        text: "This cannot be done if this item has been checked out.<br><strong>This cannot be undone!</strong>",
        type: "warning",
        showCancelButton: true,
        confirmButtonColor: "#DD6B55",
        confirmButtonText: "Yes, Delete " + itemShortName != "" ? itemShortName : itemName,
        closeOnConfirm: true,
        html: true
    }, function () {
        $('#updateModal').modal('hide');

        var xmlHttp = new XMLHttpRequest();

        var itemID = $('#updateItemID').text();
        var json = '{' +
            '"pubID": ' + itemID +
            '}';

        xmlHttp.onreadystatechange = function () {
            if (xmlHttp.readyState == 4) {
                var response = xmlHttp;
                console.log("Status: " + response.status);

                if (response.status == 200) {
                    swal("Item Deleted!", itemName + " has been deleted!", "success");
                    $('#item-' + itemID).remove();
                    sorttable.makeSortable(document.getElementById('items-list'));
                } else {
                    console.log(xmlHttp.responseText);
                    swal("Oops...", JSON.parse(xmlHttp.responseText).message, "error");
                }
            }
        };

        xmlHttp.open('DELETE', "api/item");
        xmlHttp.setRequestHeader("Content-type", "application/json");
        xmlHttp.setRequestHeader("session", Cookies.get("session"));
        xmlHttp.send(json);
    });
}

function doLoadItems(json) {
    var table = document.getElementById('items-list');
    for (var i = 0; i < json.length; i++) {
        var item = json[i];
        var row = table.insertRow();
        row.id = "item-" + item.pubID;

        row.insertCell(0).innerHTML = item.pubID;
        row.insertCell(1).innerHTML = item.name;
        var shortName = row.insertCell(2);
        shortName.innerHTML = item.shortName != null ? item.shortName : "";
        shortName.style.fontStyle = "italic";

        row.insertCell(3).innerHTML = item.assetID != null ? item.assetID : "";

        row.insertCell(4).innerHTML = item.category.name != null ? item.category.name : "<i>None</i>";
        row.insertCell(5).innerHTML = item.checked_out == false ? '<i class="fa fa-check"></i>&nbsp; In' : '<i class="fa fa-times"></i>&nbsp; Out';
        var date = row.insertCell(6);
        date.innerHTML = item.lastTransactionDate;
        if (item.lastTransactionDate.toLowerCase() == "none") date.setAttribute("sorttable_customkey", "99999999999999s");
        else date.setAttribute("sorttable_customkey", moment(item.lastTransactionDate, "MMM d, YYYY hh:mm:ss a").format("YYYYMMDDHHmmss"));

        row.insertCell(7).innerHTML = '<button class="btn btn-default btn-xs" type="button" onclick="showHist(' + item.pubID + ');" ' + (item.lastTransactionDate.toLowerCase() == "none" ? "disabled" : "") + '><i class="fa fa-history" aria-hidden="true"></i>&nbsp; History</button>';

        row.insertCell(8).innerHTML = '<button class="btn btn-default btn-xs" type="button" onclick="showEdit(' + item.pubID + ');"><i class="fa fa-pencil" aria-hidden="true"></i>&nbsp; Edit</button>';
    }

    sorttable.makeSortable(table);
}

function showHist(pubID) {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            if (xmlHttp.status == 200) {
                var json = JSON.parse(xmlHttp.responseText);
                $('#historyItemID').text(pubID);
                var $table = $('#transactionHistoryTable');
                $table.find("tr:gt(0)").remove();

                for (var t = 0; t < json.length; t++) {
                    var transaction = json[t];
                    var row = $table[0].insertRow();
                    if (transaction.direction) row.insertCell(0).innerHTML = '<i class="fa fa-sign-in" aria-hidden="true"></i> IN';
                    else row.insertCell(0).innerHTML = '<i class="fa fa-sign-out" aria-hidden="true"></i> OUT';
                    row.insertCell(1).innerHTML = transaction.time;
                    row.insertCell(2).innerHTML = transaction.id;
                    row.insertCell(3).innerHTML = transaction.owner.firstName + " " + transaction.owner.lastName;
                    if (transaction.components.length == 1) row.insertCell(4).innerHTML = transaction.components.length.toString() + " item";
                    else row.insertCell(4).innerHTML = transaction.components.length.toString() + " items";
                    row.insertCell(5).innerHTML = transaction.supervisor.firstName + " " + transaction.supervisor.lastName;
                    row.insertCell(6).innerHTML = '<a href="api/transaction/receipt/' + transaction.id + '" target="_blank" class="btn btn-default btn-xs"><i class="fa fa-file-text" aria-hidden="true"></i> Transaction Receipt</a>';
                }

                $('#historyModal').modal('show');
            }
        }
    };

    xmlHttp.open('GET', "api/item/history?id=" + pubID);
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.send();
}

function showEdit(pubID) {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            if (xmlHttp.status == 200) {
                var json = JSON.parse(xmlHttp.responseText);
                $('.updateItemID').text(json[0].pubID);
                $('#updateItemName').val(json[0].name);
                $('#updateItemShortName').val(json[0].shortName);
                $('#updateItemCategory').val(json[0].category.id);
                $('#updateAssetID').val(json[0].assetID);
                $('button.delete').prop("disabled", $('#items-list').find('#item-' + pubID + ' td:nth-child(6)').text().toLowerCase() != "none");

                $('#updateModal').modal('show');
            }
        }
    };

    xmlHttp.open('GET', "api/item?id=" + pubID);
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.send();
}

function loadCategories() {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            if (xmlHttp.status == 200) {
                var categoryListSelect = $('select.itemCategory');
                for (var c = 0; c < JSON.parse(xmlHttp.responseText).length; c++) {
                    var category = JSON.parse(xmlHttp.responseText)[c];
                    categoryListSelect.append($('<option>', {
                        value: category.id,
                        text: category.name
                    }));
                }
            }
        }
    };

    xmlHttp.open('GET', "api/category");
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.send();
}
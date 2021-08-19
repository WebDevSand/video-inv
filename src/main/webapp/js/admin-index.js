/**
 * Index for Admin View Scripts
 *
 * Created by tpaulus on 1/6/17.
 */

function loadCheckedOut() {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            if (xmlHttp.status == 200) {
                var json = JSON.parse(xmlHttp.responseText);
                var table = $('#itemsOutTable')[0];
                for (var i = 0; i < json.length; i++) {
                    var item = json[i];
                    var row = table.insertRow();
                    row.insertCell(0).innerHTML = item.name;
                    row.insertCell(1).innerHTML = item.lastTransactionDate;
                    row.insertCell(2).innerHTML = '<a href="api/transaction/receipt/' + item.lastTransactionID + '" target="_blank" class="btn btn-default btn-xs"><i class="fa fa-file-text" aria-hidden="true"></i> Transaction Receipt</a>';
                }
                $('#checkedOutJumbo').delay(500).animate({opacity: 1}, 500);
            }
        }
    };

    xmlHttp.open('GET', "api/item/out");
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.send();
}

function loadRecent(count) {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            if (xmlHttp.status == 200) {
                var json = JSON.parse(xmlHttp.responseText);
                var table = $('#transactionsTable')[0];
                for (var t = 0; t < json.length; t++) {
                    var transaction = json[t];
                    var row = table.insertRow();
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

                $('#recentJumbo').delay(500).animate({opacity: 1}, 500);
            }
        }
    };

    xmlHttp.open('GET', "api/transaction?limit=" + count);
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.send();
}

function loadPopular(count) {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            if (xmlHttp.status == 200) {
                var json = JSON.parse(xmlHttp.responseText);
                var table = $('#popularTable')[0];
                for (var i = 0; i < json.length; i++) {
                    var item = json[i];
                    var row = table.insertRow();
                    row.insertCell(0).innerHTML = item.name + (item.shortName != null && item.shortName.length > 0 ? " [" + item.shortName + "]" : "") + " (ID: " + item.pubID + ")";
                    row.insertCell(1).innerHTML = item.frequency.toString();

                }

                $('#popularJumbo').delay(500).animate({opacity: 1}, 500);
            }
        }
    };

    xmlHttp.open('GET', "api/item/popular?count=" + count);
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.send();
}
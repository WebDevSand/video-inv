/**
 * Admin Transactions View Scripts
 *
 * Created by tpaulus on 1/7/17.
 */

function loadTransactions() {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            if (xmlHttp.status == 200) {
                var json = JSON.parse(xmlHttp.responseText);
                var table = document.getElementById('transactionsTable');
                for (var t = 0; t < json.length; t++) {
                    var transaction = json[t];
                    var row = table.insertRow();
                    if (transaction.direction) row.insertCell(0).innerHTML = '<i class="fa fa-sign-in" aria-hidden="true"></i> IN';
                    else row.insertCell(0).innerHTML = '<i class="fa fa-sign-out" aria-hidden="true"></i> OUT';
                    var date = row.insertCell(1);
                    date.setAttribute("sorttable_customkey", moment(transaction.time, "MMM d, YYYY hh:mm:ss a").format("YYYYMMDDHHmmss"));
                    date.innerHTML = transaction.time;
                    row.insertCell(2).innerHTML = transaction.id;
                    row.insertCell(3).innerHTML = transaction.owner.firstName + " " + transaction.owner.lastName;
                    if (transaction.components.length == 1) row.insertCell(4).innerHTML = transaction.components.length.toString() + " item";
                    else row.insertCell(4).innerHTML = transaction.components.length.toString() + " items";
                    row.insertCell(5).innerHTML = transaction.supervisor.firstName + " " + transaction.supervisor.lastName;
                    row.insertCell(6).innerHTML = '<a href="api/transaction/receipt/' + transaction.id + '" target="_blank" class="btn btn-default btn-xs"><i class="fa fa-file-text" aria-hidden="true"></i> Transaction Receipt</a>';
                }

                sorttable.makeSortable(table);
            }
        }
    };

    xmlHttp.open('GET', "api/transaction");
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.send();
}
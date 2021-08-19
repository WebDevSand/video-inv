/**
 * Admin Categories View Scripts
 *
 * Created by tpaulus on 12/31/16.
 */

function loadCategories() {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            if (xmlHttp.status == 200) {
                var table = document.getElementById('category-list');
                for (var c = 0; c < JSON.parse(xmlHttp.responseText).length; c++) {
                    var category = JSON.parse(xmlHttp.responseText)[c];
                    var row = table.insertRow();
                    row.id = "cat-" + category.id;
                    row.insertCell(0).innerHTML = category.id;
                    row.insertCell(1).innerHTML = category.name;
                    row.insertCell(2).innerHTML = '<img src="api/category/icon/' + category.id + '" class="categoryIcon" id="cat-ico-' + category.id + '" onerror="this.style.visibility = \'hidden\'">';
                    row.insertCell(3).innerHTML = '<button class="btn btn-default btn-xs" type="button" onclick="showEdit(' + category.id + ');"><i class="fa fa-pencil" aria-hidden="true"></i>&nbsp; Edit</button>';
                }
                sorttable.makeSortable(table);
            }
        }
    };

    xmlHttp.open('GET', "api/category");
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.send();
}

function addCategory() {
    var xmlHttp = new XMLHttpRequest();

    var json = '{' +
        '"name": "' + $('#categoryName').val() + '"' +
        '}';

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            if (xmlHttp.status == 201) {
                var $createModal = $('#createModal');
                $createModal.modal('hide');
                $("#category-list").find("tr:gt(0)").remove();
                loadCategories();
                $createModal.find('form').trigger("reset");
            }
        }
    };

    xmlHttp.open('POST', "api/category");
    xmlHttp.setRequestHeader("Content-type", "application/json");
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.send(json);

    var $categoryIcon = $('#categoryIcon');
    if ($categoryIcon[0].files.length > 0) {
        updateCategoryIcon(categoryID, $categoryIcon[0].files[0]);
    }
}

function showEdit(id) {
    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            if (xmlHttp.status == 200) {
                var json = JSON.parse(xmlHttp.responseText);
                $('.updateCategoryID').text(json[0].id);
                $('#updateCategoryName').val(json[0].name);
                $('#updateModal').modal('show');
            }
        }
    };

    xmlHttp.open('GET', "api/category?id=" + id);
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.send();
}

function updateCategory() {

    var xmlHttp = new XMLHttpRequest();

    var categoryID = $('#updateCategoryID').text();
    var categoryName = $('#updateCategoryName').val();

    var json = '{' +
        '"id": ' + categoryID + "," +
        '"name" : "' + categoryName + '"' +
        '}';

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);
            var $updateModal = $('#updateModal');
            $updateModal.modal('hide');

            if (response.status == 200) {
                $('#cat-' + categoryID).find("td:nth-child(2)").text(categoryName);
                $updateModal.find('form').trigger("reset");
                sorttable.makeSortable(document.getElementById('category-list'));
            } else {
                console.log(xmlHttp.responseText);
                swal("Oops...", JSON.parse(xmlHttp.responseText).message, "error");
            }
        }
    };

    xmlHttp.open('PUT', "api/category");
    xmlHttp.setRequestHeader("Content-type", "application/json");
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.send(json);

    var $updateCategoryIcon = $('#updateCategoryIcon');
    if ($updateCategoryIcon[0].files.length > 0) {
        updateCategoryIcon(categoryID, $updateCategoryIcon[0].files[0]);
    }
}

function deleteCategory() {
    var $updateCategoryName = $("#updateCategoryName").val();
    swal({
        title: "Are you sure?",
        text: "This cannot be done if this item has been checked out.<br><strong>This cannot be undone!</strong>",
        type: "warning",
        showCancelButton: true,
        confirmButtonColor: "#DD6B55",
        confirmButtonText: "Yes, Delete " + $updateCategoryName,
        closeOnConfirm: true,
        html: true
    }, function () {
        $('#updateModal').modal('hide');

        var xmlHttp = new XMLHttpRequest();

        var categoryID = $('#updateCategoryID').text();
        var json = '{' +
            '"id": ' + categoryID +
            '}';

        xmlHttp.onreadystatechange = function () {
            if (xmlHttp.readyState == 4) {
                var response = xmlHttp;
                console.log("Status: " + response.status);

                if (response.status == 200) {
                    swal("Category Deleted!", $updateCategoryName + " has been deleted!", "success");
                    $('#cat-' + categoryID).remove();
                    sorttable.makeSortable(document.getElementById('category-list'));
                } else {
                    console.log(xmlHttp.responseText);
                    swal("Oops...", JSON.parse(xmlHttp.responseText).message, "error");
                }
            }
        };

        xmlHttp.open('DELETE', "api/category");
        xmlHttp.setRequestHeader("Content-type", "application/json");
        xmlHttp.setRequestHeader("session", Cookies.get("session"));
        xmlHttp.send(json);
    });
}

function updateCategoryIcon(categoryID, icon) {
    // Check the file type.
    if (!icon.type.match('image.*')) {
        swal("Oops...", "That wasn't an a image type", "warning");
        return;
    }

    var formData = new FormData();
    formData.append('icon', icon, icon.name);

    var xmlHttp = new XMLHttpRequest();

    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4) {
            var response = xmlHttp;
            console.log("Status: " + response.status);

            if (response.status != 200) {
                $("#cat-ico-" + categoryID).prop("src", 'api/category/icon/' + categoryID + '?' + new Date().valueOf());
            } else {
                console.log(xmlHttp.responseText);
                swal("Oops...", JSON.parse(xmlHttp.responseText).message, "error");
            }
        }
    };

    xmlHttp.open('POST', "api/category/updateICO/" + categoryID);
    xmlHttp.setRequestHeader("session", Cookies.get("session"));
    xmlHttp.send(formData);
}
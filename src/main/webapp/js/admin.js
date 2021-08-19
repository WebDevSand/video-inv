/**
 * Admin View Scripts
 * The Admin View has several child views, each of which can be loaded into the Admin View
 * and bring with them their own JS content.
 *
 * Created by tpaulus on 12/30/16.
 */

function showSection(sectionName) {
    loadChildView(sectionName);
    updateAdminNav(sectionName);
}

function updateAdminNav(newActiveView) {
    $('#admin-nav').find('.active').removeClass('active');
    $('.admin-menu-' + newActiveView).addClass('active');
}
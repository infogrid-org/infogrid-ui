function toggle_viewlet_alternatives( id ) {
    var div = document.getElementById( id );
    if( div.className == 'org-infogrid-jee-taglib-viewlet-ViewletAlternativesTag' ) {
        div.className = 'org-infogrid-jee-taglib-viewlet-ViewletAlternativesTag-expanded';
    } else {
        div.className = 'org-infogrid-jee-taglib-viewlet-ViewletAlternativesTag';
    }
}

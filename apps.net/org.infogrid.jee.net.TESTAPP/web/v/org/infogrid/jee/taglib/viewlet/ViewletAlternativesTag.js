function toggle_viewlet_alternatives() {
    var div = document.getElementById( 'org-infogrid-jee-taglib-viewlet-ViewletAlternativesTag' );
    if( div.className == 'org-infogrid-jee-taglib-viewlet-ViewletAlternativesTag' ) {
        div.className = 'org-infogrid-jee-taglib-viewlet-ViewletAlternativesTag-expanded';
    } else {
        div.className = 'org-infogrid-jee-taglib-viewlet-ViewletAlternativesTag';
    }
}

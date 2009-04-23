function org_infogrid_jee_taglib_mesh_MeshObjectTag_toggle( nodeName, nullValue, defaultValue ) {
    var input = document.getElementById( nodeName + ".propertyvalue" );

    var div1 = document.getElementById( nodeName + '-no-value' );
    var div2 = document.getElementById( nodeName + '-value' );
    var toggleDiv1 = false;

    if( div1.className != 'org-infogrid-jee-taglib-mesh-MeshObjectTag-hide' ) {
        div1.className = 'org-infogrid-jee-taglib-mesh-MeshObjectTag-hide ';
    } else {
        toggleDiv1  = true;
        input.value = nullValue;
    }

    if( div2.className != 'org-infogrid-jee-taglib-mesh-MeshObjectTag-hide' ) {
        div2.className = 'org-infogrid-jee-taglib-mesh-MeshObjectTag-hide ';
    } else {
        div2.className = 'org-infogrid-jee-taglib-mesh-MeshObjectTag-show';

        input.value = defaultValue;
    }
    
    // if we do this later, things won't flash
    if( toggleDiv1 ) {
        div1.className = 'org-infogrid-jee-taglib-mesh-MeshObjectTag-show';
    }
}

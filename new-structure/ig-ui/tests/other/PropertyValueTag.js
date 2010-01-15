function org_infogrid_jee_taglib_mesh_PropertyValueTag_doRemove( nodeName ) {
    var prefix    = 'org-infogrid-jee-taglib-mesh-PropertyValueTag';
    var input     = document.getElementById( nodeName );
    var nullInput = document.getElementById( nodeName + '.null' );

    var div1 = document.getElementById( nodeName + '-no-value' );
    var div2 = document.getElementById( nodeName + '-value' );

    nullInput.value = 'true';
    div2.className = prefix + '-hide';
    div1.className = prefix + '-show';
}

function org_infogrid_jee_taglib_mesh_PropertyValueTag_doCreate( nodeName, defaultValue ) {
    var prefix    = 'org-infogrid-jee-taglib-mesh-PropertyValueTag';
    var input     = document.getElementById( nodeName );
    var nullInput = document.getElementById( nodeName + '.null' );

    var div1 = document.getElementById( nodeName + '-no-value' );
    var div2 = document.getElementById( nodeName + '-value' );

    nullInput.value = 'false';
    div1.className = prefix + '-hide';
    div2.className = prefix + '-show';
}

function org_infogrid_jee_taglib_mesh_PropertyValueTag_toggleUpload( control, nodeName ) {
    var prefix = 'org-infogrid-jee-taglib-mesh-PropertyValueTag';
    var direct = document.getElementById( nodeName );
    var upload = document.getElementById( nodeName + '.upload' );
    var mime   = document.getElementById( nodeName + '.mime' );

    if( control.checked ) {
        upload.className = prefix + '-show';
        direct.className = prefix + '-hide';
        mime.className   = prefix + '-hide';
    } else {
        upload.className = prefix + '-hide';
        direct.className = prefix + '-show';
        mime.className   = prefix + '-show';
        
        var pickTextMime = false;
        for( var i=0 ; i<mime.options.length ; ++i ) {
            if( mime.options[i].selected ) {
                if( mime.options[i].value.substring( 0, 5 ) != "text/" ) {
                    pickTextMime = true;
                }
                break;
            }
        }
        if( pickTextMime ) {
            for( var i=0 ; i<mime.options.length ; ++i ) {
                if( mime.options[i].value.substring( 0, 5 ) == "text/" ) {
                    mime.options[i].selected = true;
                    break;
                }
            }
        }
    }
}

function org_infogrid_jee_taglib_mesh_PropertyValueTag_mimeChanged( control, nodeName ) {
    var prefix = 'org-infogrid-jee-taglib-mesh-PropertyValueTag';

    var direct = document.getElementById( nodeName );
    var upload = document.getElementById( nodeName + '.upload' );
    var mime   = document.getElementById( nodeName + '.mime' );
    var uploadCb = document.getElementById( nodeName + '.uploadcheckbox' );

    if( mime.value.substring( 0, 5 ) == "text/" ) {
	    uploadCb.checked = false;
        upload.className = prefix + '-hide';
        direct.className = prefix + '-show';
        mime.className   = prefix + '-show';
    } else {
	    uploadCb.checked = true;
        upload.className = prefix + '-show';
        direct.className = prefix + '-hide';
        mime.className   = prefix + '-hide';
    }
}
function org_infogrid_jee_taglib_mesh_PropertyTag_initProperty_value( nodeName, objectName, propertyType, currentValue, defaultValue, isOptional, isReadOnly ) {
    var nullInput  = document.getElementById( nodeName + '.propertyvalue.1.null' );
    var createSpan = document.getElementById( nodeName + '.span.create' );
    var removeSpan = document.getElementById( nodeName + '.span.remove' );
    var valueSpan  = document.getElementById( nodeName + '.span.value' );

    if( currentValue == null ) {
        nullInput.value = 'true';
        createSpan.style.display = 'inline';
        removeSpan.style.display = 'none';
        valueSpan.style.display  = 'none';

    } else if( isOptional ) {
        removeSpan.style.display = 'inline';
    }
}

function org_infogrid_jee_taglib_mesh_PropertyTag_doRemove( nodeName ) {
    var nullInput  = document.getElementById( nodeName + '.propertyvalue.1.null' );
    var createSpan = document.getElementById( nodeName + '.span.create' );
    var removeSpan = document.getElementById( nodeName + '.span.remove' );
    var valueSpan  = document.getElementById( nodeName + '.span.value' );

    nullInput.value = 'true';

    createSpan.style.display = 'inline';
    removeSpan.style.display = 'none';
    valueSpan.style.display  = 'none';
}
function org_infogrid_jee_taglib_mesh_PropertyTag_doCreate( nodeName ) {
    var nullInput  = document.getElementById( nodeName + '.propertyvalue.1.null' );
    var createSpan = document.getElementById( nodeName + '.span.create' );
    var removeSpan = document.getElementById( nodeName + '.span.remove' );
    var valueSpan  = document.getElementById( nodeName + '.span.value' );

    nullInput.value = 'false';

    createSpan.style.display = 'none';
    removeSpan.style.display = 'inline';
    valueSpan.style.display  = 'inline';
}

function org_infogrid_jee_taglib_mesh_PropertyTag_mimeSet( nodeName ) {
    var mime = document.getElementById( nodeName + '.propertyvalue.1.mime' );
    var text = document.getElementById( nodeName + '.propertyvalue.1.text' );
    var data = document.getElementById( nodeName + '.propertyvalue.1.data' );

    if( mime.value.substring( 0, 5 ) == "text/" ) {
        if( text != null ) {
            text.style.display = 'inline';
        }
        if( data != null ) {
            data.style.display = 'none';
        }
    } else {
        if( text != null ) {
            text.style.display = 'none';
        }
        if( data != null ) {
            data.style.display = 'inline';
        }
    }
}

function org_infogrid_jee_taglib_mesh_PropertyTag_mimeChanged( control, nodeName ) {
    var mime = document.getElementById( nodeName + '.propertyvalue.1.mime' );
    var text = document.getElementById( nodeName + '.propertyvalue.1.text' );
    var data = document.getElementById( nodeName + '.propertyvalue.1.data' );

    if( mime.value.substring( 0, 5 ) == "text/" ) {
        if( text != null ) {
            text.style.display = 'inline';
        }
        if( data != null ) {
            data.style.display = 'none';
        }
    } else {
        if( text != null ) {
            text.style.display = 'none';
        }
        if( data != null ) {
            data.style.display = 'inline';
        }
    }
}

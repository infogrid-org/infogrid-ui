function org_infogrid_jee_taglib_mesh_PropertyTag_initProperty_value( nodeName, editIndex, currentValue, isOptional, isReadOnly ) {
    var nullInput  = document.getElementById( nodeName + '.propertyvalue.' + editIndex + '.null' );
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

function org_infogrid_jee_taglib_mesh_PropertyTag_doRemove( nodeName, editIndex ) {
    var nullInput  = document.getElementById( nodeName + '.propertyvalue.' + editIndex + '.null' );
    var createSpan = document.getElementById( nodeName + '.span.create' );
    var removeSpan = document.getElementById( nodeName + '.span.remove' );
    var valueSpan  = document.getElementById( nodeName + '.span.value' );

    nullInput.value = 'true';

    createSpan.style.display = 'inline';
    removeSpan.style.display = 'none';
    valueSpan.style.display  = 'none';
}
function org_infogrid_jee_taglib_mesh_PropertyTag_doCreate( nodeName, editIndex ) {
    var nullInput  = document.getElementById( nodeName + '.propertyvalue.' + editIndex + '.null' );
    var createSpan = document.getElementById( nodeName + '.span.create' );
    var removeSpan = document.getElementById( nodeName + '.span.remove' );
    var valueSpan  = document.getElementById( nodeName + '.span.value' );

    nullInput.value = 'false';

    createSpan.style.display = 'none';
    removeSpan.style.display = 'inline';
    valueSpan.style.display  = 'inline';
}

function org_infogrid_jee_taglib_mesh_PropertyTag_mimeSet( nodeName, editIndex ) {
    var mime = document.getElementById( nodeName + '.propertyvalue.' + editIndex + '.mime' );
    var text = document.getElementById( nodeName + '.propertyvalue.' + editIndex + '.text' );
    var data = document.getElementById( nodeName + '.propertyvalue.' + editIndex + '.data' );

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

function org_infogrid_jee_taglib_mesh_PropertyTag_mimeChanged( nodeName, editIndex ) {
    var mime = document.getElementById( nodeName + '.propertyvalue.' + editIndex + '.mime' );
    var text = document.getElementById( nodeName + '.propertyvalue.' + editIndex + '.text' );
    var data = document.getElementById( nodeName + '.propertyvalue.' + editIndex + '.data' );

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

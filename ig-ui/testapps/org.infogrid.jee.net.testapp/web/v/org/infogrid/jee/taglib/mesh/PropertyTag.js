function org_infogrid_jee_taglib_mesh_PropertyTag_initProperty_value( nodeName, objectName, propertyType, currentValue, defaultValue, isOptional, isReadOnly ) {
    var nullInput = document.getElementById( nodeName + '.propertyvalue.1.null' );
    var createDiv = document.getElementById( nodeName + '.div.create' );
    var removeDiv = document.getElementById( nodeName + '.div.remove' );
    var valueDiv  = document.getElementById( nodeName + '.div.value' );

    if( currentValue == null ) {
        nullInput.value = 'true';
        createDiv.style.display = 'inline';
        removeDiv.style.display = 'none';
        valueDiv.style.display  = 'none';

    } else if( isOptional ) {
        removeDiv.style.display = 'inline';
    }
}

function org_infogrid_jee_taglib_mesh_PropertyTag_doRemove( nodeName ) {
    var nullInput = document.getElementById( nodeName + '.propertyvalue.1.null' );
    var createDiv = document.getElementById( nodeName + '.div.create' );
    var removeDiv = document.getElementById( nodeName + '.div.remove' );
    var valueDiv  = document.getElementById( nodeName + '.div.value' );

    nullInput.value = 'true';

    createDiv.style.display = 'inline';
    removeDiv.style.display = 'none';
    valueDiv.style.display  = 'none';
}
function org_infogrid_jee_taglib_mesh_PropertyTag_doCreate( nodeName ) {
    var nullInput = document.getElementById( nodeName + '.propertyvalue.1.null' );
    var createDiv = document.getElementById( nodeName + '.div.create' );
    var removeDiv = document.getElementById( nodeName + '.div.remove' );
    var valueDiv  = document.getElementById( nodeName + '.div.value' );

    nullInput.value = 'false';

    createDiv.style.display = 'none';
    removeDiv.style.display = 'inline';
    valueDiv.style.display  = 'inline';
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

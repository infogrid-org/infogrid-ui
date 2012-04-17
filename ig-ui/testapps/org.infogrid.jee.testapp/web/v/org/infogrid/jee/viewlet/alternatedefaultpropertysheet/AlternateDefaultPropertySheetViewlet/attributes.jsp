<%@    page contentType="text/html"
 %><%@ taglib prefix="mesh"  uri="/v/org/infogrid/jee/taglib/mesh/mesh.tld"
 %><%@ taglib prefix="candy" uri="/v/org/infogrid/jee/taglib/candy/candy.tld"
 %><%@ taglib prefix="u"     uri="/v/org/infogrid/jee/taglib/util/util.tld"
 %><%@ taglib prefix="v"     uri="/v/org/infogrid/jee/taglib/viewlet/viewlet.tld"
 %><%@ taglib prefix="tmpl"  uri="/v/org/infogrid/jee/taglib/templates/templates.tld"
 %><%@ taglib prefix="c"     uri="http://java.sun.com/jsp/jstl/core"
 %><%@ page import="org.infogrid.model.primitives.BlobDataType"
 %><%@ page import="org.infogrid.model.primitives.BooleanDataType"
 %><%@ page import="org.infogrid.model.primitives.ColorDataType"
 %><%@ page import="org.infogrid.model.primitives.CurrencyDataType"
 %><%@ page import="org.infogrid.model.primitives.DataType"
 %><%@ page import="org.infogrid.model.primitives.EnumeratedDataType"
 %><%@ page import="org.infogrid.model.primitives.ExtentDataType"
 %><%@ page import="org.infogrid.model.primitives.FloatDataType"
 %><%@ page import="org.infogrid.model.primitives.IntegerDataType"
 %><%@ page import="org.infogrid.model.primitives.MultiplicityDataType"
 %><%@ page import="org.infogrid.model.primitives.PointDataType"
 %><%@ page import="org.infogrid.model.primitives.PropertyType"
 %><%@ page import="org.infogrid.model.primitives.StringDataType"
 %><%@ page import="org.infogrid.model.primitives.TimePeriodDataType"
 %><%@ page import="org.infogrid.model.primitives.TimeStampDataType"
 %>

 <table class="attributes">
  <thead>
   <tr>
    <th class="type">
     <v:notIfState viewletState="edit">
      <div class="slide-in-button">
       <u:callJspo page="/o/bless.jspo" action="${Viewlet.postUrl}" linkTitle="Bless this MeshObject" submitLabel="Bless">
        <u:callJspoParam name="toBless" value="${Subject}"/>
        <img src="${CONTEXT}/s/images/medal_bronze_add.png" alt="Bless" />
       </u:callJspo>
      </div>
     </v:notIfState>
     Type
    </th>
    <th>Property</th>
    <th>Value</th>
   </tr>
  </thead>
  <tbody>
   <mesh:blessedByIterate meshObjectName="Subject" blessedByLoopVar="blessedBy">
    <tr>
     <th class="entityType" colspan="3">
      <v:notIfState viewletState="edit">
       <div class="slide-in-button">
        <u:callJspo page="/o/unbless.jspo" action="${Viewlet.postUrl}" linkTitle="Unbless this MeshObject" submitLabel="Unbless">
         <u:callJspoParam name="toUnbless" value="${Subject}"/>
         <u:callJspoParam name="type"      value="${blessedBy}"/>
         <img src="${CONTEXT}/s/images/medal_bronze_delete.png" alt="Unbless" />
        </u:callJspo>
       </div>
      </v:notIfState>
      <mesh:type meshTypeName="blessedBy"/> <mesh:meshTypeId meshTypeName="blessedBy"/>
     </th>
    </tr>
    <mesh:propertyIterate meshObjectName="Subject" meshTypeName="blessedBy" propertyTypeLoopVar="propertyType" propertyValueLoopVar="propertyValue" skipNullProperty="false">
     <tr>
      <th class="type"></th>
      <td class="name"><mesh:type meshTypeName="propertyType" /></td>
      <td class="value">
<%
    String       altDefault = "Hmmm...";
    PropertyType propType = (PropertyType) request.getAttribute( "propertyType" );
    DataType     type     = propType.getDataType();

    if( type instanceof BlobDataType ) {
        altDefault = "UnusualBlobDefault";

    } else if( type instanceof BooleanDataType ) {
        altDefault = "TRUE";

    } else if( type instanceof ColorDataType ) {
        altDefault = "#123456";

    } else if( type instanceof CurrencyDataType ) {
        altDefault = "$99.88";

    } else if( type instanceof EnumeratedDataType ) {
        altDefault = "Value3";

    } else if( type instanceof ExtentDataType ) {
        altDefault = "(99;88)";

    } else if( type instanceof FloatDataType ) {
        altDefault = "99.88";

    } else if( type instanceof IntegerDataType ) {
        altDefault = "99";

    } else if( type instanceof MultiplicityDataType ) {
        altDefault = "88..99";

    } else if( type instanceof PointDataType ) {
        altDefault = "(88;99)";

    } else if( type instanceof StringDataType ) {
        altDefault = "99.88.99.88";

    } else if( type instanceof TimePeriodDataType ) {
        altDefault = "99 years, 88 months, 77 days, 66 hours, 55 minutes, 44.333 seconds";

    } else if( type instanceof TimeStampDataType ) {
        altDefault = "08:08:08.088 1999/11/11 UTC";

    }
%>
          
       <mesh:property meshObjectName="Subject" propertyTypeName="propertyType" ignore="true" state="${Viewlet.viewletState.name}" defaultValue="<%= altDefault %>"/>
      </td>
     </tr>
    </mesh:propertyIterate>
   </mesh:blessedByIterate>
  </tbody>
 </table>

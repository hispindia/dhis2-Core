/**
 * Created by "Sourabh Bhardwaj" on 11/24/2016.
 */


var peid,orgid,cpeid,mpeid,corgid,morgid;
//var base = "http://localhost:8090/dhis/";
var base = "../../";
var orgname;
var deids=[],denames=[];
var arr=[];
var degroupid,degroupname="",dename="",deid="";
var organisationUnits,corganisationUnits,morganisationUnits;
var indicators,mindicators,cindicators;
var mpeid,morgid;
var countries = [];
var ccountries = [];
var morgname;
var mdeids=[],mdenames=[];
var marr;
var mdegroupid,cdegroupid,mdegroupname,mdename,mdeid;var mpeid,morgid;
var organisationUnits;
var corgname;
var cdeids=[],cdenames=[];
var carr=[];
var idarr=[],codearr=[],namearr=[];
var periodarr=[],pearr=[];
var mperiodarr=[],mpearr=[];
var cperiodarr=[],cpearr=[];
var url=base+"dhis-web-commons-security/login.action";
var cdegroupid,cdegroupname,cdename="",cdeid="";
// Login - if OK, call the setLinks function
$(document).ready(function() {
    $("a#meta").fancybox({
        'hideOnContentClick': true
    });




});
Ext.onReady( function() {

    //document.getElementById("drop1").multiple = false;
    Ext.Ajax.request({
        url: base + "dhis-web-commons-security/login.action",
        method: "POST",
        params: {j_username: "publicdashboard", j_password: "D@SHbo@rd20!6"},

    });


    $.post(url, {
        'j_username': 'publicdashboard',
        'j_password': 'D@SHbo@rd20!6'
    }, function (data, status, xhr) {
        //console.log(data);
        //console.log(status);
        //console.log(xhr);
        $.getJSON("../../api/dataElementGroups.json?fields=id,name,dataElementGroups[id,name]", function (data) {

            $.each(data.dataElementGroups, function (index, item) {
                $('#drop3').append(
                    $('<option></option>').val(item.id).html(item).text(item.name)
                );
            });	$.each(data.dataElementGroups, function (index, item) {
                $('#dropm3').append(
                    $('<option></option>').val(item.id).html(item).text(item.name)
                );
            });	$.each(data.dataElementGroups, function (index, item) {
                $('#dropc3').append(
                    $('<option></option>').val(item.id).html(item).text(item.name)
                );
            });
        });


        $('.selectpicker').selectpicker('refresh');
        $.getJSON("../../api/sqlViews/n0UQzBeeixt/data.json", function (data) {

            $.each(data.rows, function (index, item) {
                var res = item[2].slice(0,4);
                periodarr.push(item[2]);
                mperiodarr.push(item[2]);
                cperiodarr.push(item[2]);


                pearr.push(res);
                mpearr.push(res);
                cpearr.push(res);



                pearr.sort(function(a, b) {
                    var nameA=a.toLowerCase(), nameB=b.toLowerCase()
                    if (nameA < nameB) //sort string ascending
                        return -1
                    if (nameA > nameB)
                        return 1
                    return 0 //default return value (no sorting)
                });mpearr.sort(function(a, b) {
                    var nameA=a.toLowerCase(), nameB=b.toLowerCase()
                    if (nameA < nameB) //sort string ascending
                        return -1
                    if (nameA > nameB)
                        return 1
                    return 0 //default return value (no sorting)
                });cpearr.sort(function(a, b) {
                    var nameA=a.toLowerCase(), nameB=b.toLowerCase()
                    if (nameA < nameB) //sort string ascending
                        return -1
                    if (nameA > nameB)
                        return 1
                    return 0 //default return value (no sorting)
                });
            });
            //	console.log(periodarr);
            //console.log(pearr);
            $.each(pearr, function (index, item) {
                $('#drop5').append(
                    $('<option></option>').val(item).html(item).text(item)
                );
            });
            $.each(mpearr, function (index, item) {
                $('#dropm5').append(
                    $('<option></option>').val(item).html(item).text(item)
                );
            });$.each(cpearr, function (index, item) {
                $('#dropc5').append(
                    $('<option></option>').val(item).html(item).text(item)
                );
            });

        });
        $.getJSON("../../api/organisationUnitGroups.json?fields=id,code,name&level=3&order=code:asc", function (data) {
            $.each(data.organisationUnitGroups, function (index, item) {
                $('#drop1').append(
                    $('<option></option>').val(item.id).html(item.name).text(item.name)
                );
//					$('#dropm1').append(
//							$('<option></option>').val(item.id).html(item.name).text(item.name)
//					);

                $('#dropc1').append(
                    $('<option></option>').val(item.id).html(item.name).text(item.name)
                );
            });
            $('.selectpicker').selectpicker('refresh');


            //$('.selectpicker').selectpicker('selectAll');



            //$('.selectpicker').selectpicker('selectAll');

            //$.getJSON("../../api/sqlViews/n0UQzBeeixt/data.json", function (data) {


        });


        $("#drop5").change(function () {
            var selected = $("#drop5 option:selected");


            peid = "";
            selected.each(function () {
                peid += ";" + $(this).val();
            });
        });
        $("#dropm5").change(function () {
            var selected = $("#dropm5 option:selected");


            mpeid = "";
            selected.each(function () {
                mpeid += ";" + $(this).val();
            });
        });$("#dropc5").change(function () {

            var selected = $("#dropc5 option:selected");


            cpeid = "";
            selected.each(function () {
                cpeid += ";" + $(this).val();
            });

        });

        $("#drop3").change(function () {


            degroupid = "";

            degroupname += ","+$(this).find("option:selected").text();

            degroupid = $(this).find("option:selected").val();


            $.each($("#drop4 option:selected"), function(index,item ){
                var country = {};
                country.id =  item.value;
                country.name = item.innerHTML;
                countries.push(country);
                //console.log(country);
                //console.log(countries);
            });

            var element = document.getElementById("drop4");
            for (var i = element.length-1; i >= 0; i--) {

                if(element[i].selected)

                {

                    alert(element[i].selected);
                }
                else {
                    element[i].remove();
                }
            }



            $.each(countries, function(index,item){



                //countries.push(document.getElementById("drop4").selectedIndex);

                $('#drop4').append(
                    $('<option></option>').val(item.id).html(item.name).text(item.name)
                );

            });

            $.getJSON("../../api/dataElementGroups/" + degroupid + ".json?fields=dataElements[id,name]", function (data) {

                indicators = data.dataElements;

                indicators.sort(function(a, b) {
                    var nameA=a.name.toLowerCase(), nameB=b.name.toLowerCase()
                    if (nameA < nameB) //sort string ascending
                        return -1
                    if (nameA > nameB)
                        return 1
                    return 0 //default return value (no sorting)
                });
                $.each(indicators, function (index, item) {
                    $('#drop4').append(
                        $('<option></option>').val(item.id).html(item).text(item.name)
                    );
                });

                $('.selectpicker').selectpicker('refresh');
            });

        });
        $("#dropc3").change(function () {


            cdegroupid = "";

            cdegroupname += ","+$(this).find("option:selected").text();

            cdegroupid = $(this).find("option:selected").val();



            $.each($("#drop4 option:selected"), function(index,item ){
                var ccountry = {};
                ccountry.id =  item.value;
                ccountry.name = item.innerHTML;
                ccountries.push(ccountry);
            });

            var element = document.getElementById("dropc4");
//				for (var i = element.length-1; i >= 0; i--) {
//					element[i].remove()
//				}
            for (var i = element.length-1; i >= 0; i--) {

                if(element[i].selected)

                {

//						alert(element[i].selected);
                }
                else {
                    element[i].remove();
                }
            }


            $.each(ccountries, function(index,item){



                //countries.push(document.getElementById("drop4").selectedIndex);

                $('#dropc4').append(
                    $('<option></option>').val(item.id).html(item.name).text(item.name)
                );

            });

            $.getJSON("../../api/dataElementGroups/" + cdegroupid + ".json?fields=dataElements[id,name]", function (data) {



                cindicators = data.dataElements;

                cindicators.sort(function(a, b) {
                    var nameA=a.name.toLowerCase(), nameB=b.name.toLowerCase()
                    if (nameA < nameB) //sort string ascending
                        return -1
                    if (nameA > nameB)
                        return 1
                    return 0 //default return value (no sorting)
                });
                $.each(cindicators, function (index, item) {
                    $('#dropc4').append(
                        $('<option></option>').val(item.id).html(item).text(item.name)
                    );
                });
                $('.selectpicker').selectpicker('refresh');
            });

        });
        $("#dropm3").change(function () {


            mdegroupid = "";
            mdegroupname = $(this).find("option:selected").text();

            mdegroupid = $(this).find("option:selected").val();


            $.getJSON("../../api/dataElementGroups/" + mdegroupid + ".json?fields=dataElements[id,name]", function (data) {

                mindicators = data.dataElements;

                mindicators.sort(function(a, b) {
                    var nameA=a.name.toLowerCase(), nameB=b.name.toLowerCase()
                    if (nameA < nameB) //sort string ascending
                        return -1
                    if (nameA > nameB)
                        return 1
                    return 0 //default return value (no sorting)
                });
                $.each(mindicators, function (index, item) {
                    $('#dropm4').append(
                        $('<option></option>').val(item.id).html(item).text(item.name)
                    );
                });
                $('.selectpicker').selectpicker('refresh');
            });

        });

        $("#dropc3").change(function () {


            cdegroupid = "";
            cdegroupname = $(this).find("option:selected").text();

            cdegroupid = $(this).find("option:selected").val();


            $.getJSON("../../api/dataElementGroups/" + cdegroupid + ".json?fields=dataElements[id,name]", function (data) {
                $.each(data.dataElements, function (index, item) {
                    // deids.push(item.id);
                    //denames.push(item.name);

                    $('#dropc4').append(
                        $('<option></option>').val(item.id).html(item).text(item.name)
                    );

                });
                $('.selectpicker').selectpicker('refresh');
            });

        });
        $("#drop4").change(function () {
            var selected = $("#drop4 option:selected");
            //dename = "";
            //deid = "";
            selected.each(function () {
                dename = "";
                deid = "";
                deid += ";" + $(this).val();
                dename += ";" + $(this).text();

                //alert(dename);
                arr.push({"id": deid, "name": dename});
            });
            //alert(dename);
            //arr=[];

            //dename = "";
            //deid = "";
            //getFilteredOrgUnitList();
            //console.log("Data Elements" + arr);
        });
        $("#dropm4").change(function () {
            var selected = $("#dropm4 option:selected");
            mdename = "";
            mdeid = "";
            selected.each(function () {
                mdeid += ";" + $(this).val();
                mdename += ";" + $(this).text();

            });
            marr = [];
            marr.push({"id": mdeid, "name": mdename});
            //getFilteredOrgUnitList();

        });
        $("#dropc4").change(function () {
            var selected = $("#dropc4 option:selected");
            cdename = "";
            cdeid = "";
            selected.each(function () {

                //cdeid = "";
                cdeid += ";" + $(this).val();
                cdename += $(this).text()+",";

                carr.push({"id": cdeid, "name": cdename});

            });
            //carr = [];

            //getFilteredOrgUnitList();

        });

        $("#drop1").change(function () {



            if(drop1.value=="MhXp46Y40hA" || drop1.value=="KQKjaCQNIlV")
            {
            var element = document.getElementById("drop2");
            for (var i = element.length-1; i >= 1; i--) {
                element[i].remove()
            }
            }


            var orglevel = $(this).find("option:selected").val();
            $.getJSON("../../api/organisationUnitGroups/" + orglevel + ".json", function (data) {
                organisationUnits = data.organisationUnits;

                organisationUnits.sort(function(a, b) {
                    var nameA=a.name.toLowerCase(), nameB=b.name.toLowerCase()
                    if (nameA < nameB) //sort string ascending
                        return -1
                    if (nameA > nameB)
                        return 1
                    return 0 //default return value (no sorting)
                });

                $.each(organisationUnits, function (index, item) {
                    $('#drop2').append(
                        $('<option></option>').val(item.id).html(item).text(item.name)
                    );
                });
                $('.selectpicker').selectpicker('refresh');

                //$('.selectpicker').selectpicker('selectAll');

            });
            //document.getElementById("drop2").innerHTML = "";
        });
        $("#dropc1").change(function () {

            if(dropc1.value=="MhXp46Y40hA" || dropc1.value=="KQKjaCQNIlV")
            {
            var element = document.getElementById("dropc2");
            for (var i = element.length-1; i >= 0; i--) {
                element[i].remove()
            }

            }
            var corglevel = $(this).find("option:selected").val();
            $.getJSON("../../api/organisationUnitGroups/" + corglevel + ".json", function (data) {
                corganisationUnits = data.organisationUnits;

                corganisationUnits.sort(function(a, b) {
                    var nameA=a.name.toLowerCase(), nameB=b.name.toLowerCase()
                    if (nameA < nameB) //sort string ascending
                        return -1
                    if (nameA > nameB)
                        return 1
                    return 0 //default return value (no sorting)
                });
                $.each(corganisationUnits, function (index, item) {
                    $('#dropc2').append(
                        $('<option></option>').val(item.id).html(item).text(item.name)
                    );
                });
                $('.selectpicker').selectpicker('refresh');

                //$('.selectpicker').selectpicker('selectAll');

            });
        });


        /* Select All property for the Drop down*/

        $('#drop2').selectpicker().change(function(){toggleSelectAll($(this));}).trigger('change');

        function toggleSelectAll(control) {
            var allOptionIsSelected = (control.val() || []).indexOf('all') > -1;
            function valuesOf(elements) {
                return $.map(elements, function(element) {
                    return element.value;
                });
            }

            if (control.data('allOptionIsSelected') != allOptionIsSelected) {
                // User clicked 'All' option
                if (allOptionIsSelected) {
                    // Can't use .selectpicker('selectAll') because multiple "change" events will be triggered
                    control.selectpicker('val', valuesOf(control.find('option')));
                } else {
                    control.selectpicker('val', []);
                }
            } else {
                // User clicked other option
                if (allOptionIsSelected && control.val().length != control.find('option').length) {
                    // All options were selected, user deselected one option
                    // => unselect 'All' option
                    control.selectpicker('val', valuesOf(control.find('option:selected[value!=all]')));
                    allOptionIsSelected = false;
                } else if (!allOptionIsSelected && control.val().length == control.find('option').length - 1) {
                    // Not all options were selected, user selected all options except 'All' option
                    // => select 'All' option too
                    control.selectpicker('val', valuesOf(control.find('option')));
                    allOptionIsSelected = true;
                }
            }
            control.data('allOptionIsSelected', allOptionIsSelected);
        }


        $("#drop2").change(function () {

            var selected = $("#drop2 option:selected");
            orgname="";
            orgid = "";
            selected.each(function () {
                orgname=$(this).val();
                orgid += ";" + $(this).val();

            });

            orgname = $(this).find("option:selected").text();

            // orgid += $(this).find("option:selected").val()+";";
            //	console.log("org id"+orgid);

        });
//			$("#dropm2").change(function () {
//				if(dropm2.value=="all")
//				{
//
//					$('#dropm2').selectpicker('selectAll');
//
//				}
//				var selected = $("#dropm2 option:selected");
//				morgname="";
//				morgid = "";
//				selected.each(function () {
//					morgname=$(this).val();
//					morgid += ";" + $(this).val();
//
//				});
//
//				morgname = $(this).find("option:selected").text();
//
//				// orgid += $(this).find("option:selected").val()+";";
//
//
//			});
        morgid="eGtybcKtVeq";


        /* Select All property for the Drop down of charts*/

        $('#dropc2').selectpicker().change(function(){toggleSelectAll($(this));}).trigger('change');



        $("#dropc2").change(function () {
            /*if(dropc2.value=="all")
             {

             $('#dropc2').selectpicker('selectAll');

             }*/
            var selected = $("#dropc2 option:selected");
            corgname="";
            corgid = "";
            selected.each(function () {
                corgname=$(this).val();
                corgid += ";" + $(this).val();

            });

            corgname = $(this).find("option:selected").text();

            // orgid += $(this).find("option:selected").val()+";";


        });

    });	});





function validatetable() {
    var isValidated = "true";

    //orgUnit Related
    var level = document.getElementById('drop1');
    var levelid = level.options[level.selectedIndex].value;
    //alert( tempOrgUnitUid );

    // indicator related
    var orgf = document.getElementById('drop2');
    var orgfid = orgf.options[orgf.selectedIndex].value;
    //alert( tempIndicatorUid );

    // month related
    var period = document.getElementById('drop3');
    var periodid = period.options[period.selectedIndex].value;
    //alert( tempMonthUid );

    // year related
    var indigroup = document.getElementById('drop4');
    var indigid = indigroup.options[indigroup.selectedIndex].value;
    //alert( tempYearUid );

    // year related
    var indi = document.getElementById('drop5');
    var indiid = indi.options[indi.selectedIndex].value;
    //alert( tempYearUid );


    if (levelid == "base" || levelid == undefined || levelid =="") {
        alert("Please select Level");
        isValidated = "false";
        return;
    }

    else if (orgfid == "" || orgfid == undefined) {
        alert("Please select Organisation unit");
        isValidated = "false";
        return;
    }

    else if (periodid="" || periodid == "Please Select" || periodid == undefined) {
        alert("Please select Period");
        isValidated = "false";
        return;
    }

    else if (indigid="" || indigid == "Please Select" || indigid == undefined) {
        alert("Please select Indicator Group");
        isValidated = "false";
        return;
    }
    else if (indi=""|| indi == "Select Year" || indi == undefined) {
        alert("Please select Indicator");
        isValidated = "false";
        return;
    }

    else ( isValidated === "true")
    {

        window.open('newtab.html?arr='+encodeURIComponent(JSON.stringify(arr))+'&peid='+peid+'&orgid='+orgid+'&orgname='+orgname);

    }

//
//		open = function(verb, url, data, target) {
//			var form = document.createElement("form");
//			form.action = url;
//			form.method = verb;
//			form.target = target || "_self";
//			if (data) {
//				for (var key in data) {
//					var input = document.createElement("textarea");
//					input.name = key;
//					input.value = typeof data[key] === "object" ? JSON.stringify(data[key]) : data[key];
//					form.appendChild(input);
//				}
//			}
//			form.style.display = 'none';
//			document.body.appendChild(form);
//			form.submit();
//		};

}

function validatechart() {
    var isValidated = "true";

    //orgUnit Related
    var level = document.getElementById('dropc1');
    var levelid = level.options[level.selectedIndex].value;
    //alert( tempOrgUnitUid );

    // indicator related
    var orgf = document.getElementById('dropc2');
    var orgfid = orgf.options[orgf.selectedIndex].value;
    //alert( tempIndicatorUid );

    // month related
    var period = document.getElementById('dropc3');
    var periodid = period.options[period.selectedIndex].value;
    //alert( tempMonthUid );

    // year related
    var indigroup = document.getElementById('dropc4');
    var indigid = indigroup.options[indigroup.selectedIndex].value;
    //alert( tempYearUid );

    // year related
    var indi = document.getElementById('dropc5');
    var indiid = indi.options[indi.selectedIndex].value;
    //alert( tempYearUid );


    if (levelid == "base" || levelid == undefined || levelid =="") {
        alert("Please select Level");
        isValidated = "false";
        return;
    }

    else if (orgfid == "" || orgfid == undefined) {
        alert("Please select Organisation unit");
        isValidated = "false";
        return;
    }

    else if (periodid="" || periodid == "Please Select" || periodid == undefined) {
        alert("Please select Period");
        isValidated = "false";
        return;
    }

    else if (indigid="" || indigid == "Please Select" || indigid == undefined) {
        alert("Please select Indicator Group");
        isValidated = "false";
        return;
    }
    else if (indi=""|| indi == "Select Year" || indi == undefined) {
        alert("Please select Indicator");
        isValidated = "false";
        return;
    }
    else ( isValidated === "true")
    {

        window.open('lib/controllers/highcharts1.html?cdegroupid='+cdegroupid+'&cpeid='+cpeid+'&cdegroupname='+cdegroupname+'&cdeid='+cdeid+'&cdename='+encodeURIComponent(cdename)+'&corgid='+corgid+'&corgname='+corgname);
    }

}

function validatemap() {
    var isValidated = "true";

    var period = document.getElementById('dropm5');
    var periodid = period.options[period.selectedIndex].value;

    var indigroup = document.getElementById('dropm4');
    var indigid = indigroup.options[indigroup.selectedIndex].value;

    var indi = document.getElementById('dropm3');
    var indiid = indi.options[indi.selectedIndex].value;

    if (periodid==""|| periodid == "Please Select" || periodid == undefined|| periodid == "false") {
        alert("Please select Period");
        isValidated = "false";
        return;
    }

    else if (indigid=="" || indigid == "Please Select" || indigid == undefined) {
        alert("Please select Indicator Group");
        isValidated = "false";
        return;
    }
    else if (indiid==""|| indiid == "Select Year" || indiid == undefined) {
        alert("Please select Indicator");
        isValidated = "false";
        return;
    }

    else ( isValidated === "true")
    {

        window.open('lib/controllers/map1.html?mdeid='+mdeid+
            '&mpeid='+mpeid+'&morgid='+";sCU6B2ZOTYr;GY9dLYENyOU;AFkkfLBwuHc;sQeCnfgIko4;n5AQFbBKbVA;vMK14bK6V4P;rV0O78pYETp;uu7IpfKO1IA;jqvWOjpUOLE;BYyRgdEoYDd;rY3Q1VZtZpg"+'&morgname='+"Searo Region");

    }


}

function w3_open() {
    document.getElementById("myDropnav").style.display = "block";


}
function w3_open1() {
    document.getElementById("myDropnav1").style.display = "block";
}
function w3_open2() {
    document.getElementById("myDropnav2").style.display = "block";
}

function w3_close() {
    document.getElementById("myDropnav").style.display = "none";
}function w3_close1() {
    document.getElementById("myDropnav1").style.display = "none";
}function w3_close2() {
    document.getElementById("myDropnav2").style.display = "none";
}


var base = "../../";
var url=base+"dhis-web-commons-security/login.action";
var orgid="wSslG6mcGXl";
var ouid=[];
var drop2org=[];
var sel="";
var filterhospital,service,owner,healthfacility;
var hospital;
var defavail="avHST8wLPnX&dimension=jXCd8k2841l&dimension=txl9e6UJFP4";
var defservice="&dimension=BZ0xteKZNid&dimension=OYAHA8Vhc3G&dimension=qNBCYtrkaD7&dimension=Bv5Fu3onViS&dimension=g7tngXzv2Zz&dimension=mKUqJtDn41L&dimension=G6QYTm3JoNo&dimension=DfbzQg5LTlm&dimension=vvzIfRasrJd&dimension=QeEQe0ERs9X&dimension=sFKV5EA9U6t&dimension=x2SMBBm0P7T&dimension=DeWJ6TcLBGn&dimension=MZ8si8FHS0T&dimension=uyyl3x9jwQa&dimension=bfL2zXyQtrA&dimension=XBO6pg9y1m8&dimension=AEQRulqMjQB&dimension=hEZYkang3cp&dimension=lWsun2ZATjI&dimension=ALMaYK2pMhL&dimension=p2MQZL84eNu&dimension=LRb9HlmAbc6&dimension=sCypRhH8brf&dimension=L11XujC9xzh&dimension=szDQ40J4DTm&dimension=KPpV7WAdys5&dimension=tx0G6s6nBiC&dimension=epW5qI95Cno&dimension=lKQPhgCfuvz&dimension=l1f67ipP6mj&dimension=I3jAOh6ZIMk&dimension=bxGjTYnbgcB&dimension=ttLEYvjxCse&dimension=JOOdfW6RCCD&dimension=TaudXwrGaVC&dimension=gJoAOIEKG9M&dimension=snsCxRbqdHP&dimension=eFpqq53Zifj&dimension=U0rv5FWWeeo&dimension=VDigKipZYu1&dimension=Guub32IStl2&dimension=sIeFKRWtZrn&dimension=IYOefLkrEZk&dimension=t035HNWxNZU&dimension=IlBOWfRZyUc&dimension=dO49PmdQpvT&dimension=akM0bMRwfV4";
// var defowner="EwolVkPAKN6&dimension=aI5XEAH8PkC&dimension=VozTuKA0GP1";
var defowner="XEiMcaGi6vv";
var defhealthfacility="&dimension=UmlIjjErp1p&dimension=rD7PJQN4TTe&dimension=nvGzrdrt48l&dimension=UfCxf82vB7J";
var avgRating=[],avgRating1;
var divele;
var servicename,serviceid;
var name=[],address=[],pincode=[],village=[],mobile=[],owner=[],special=[];
var toAdd,spec=[],specialjoin;
var resultcount=0;
var redMarker = L.AwesomeMarkers.icon({
    icon: 'coffee',
    markerColor: 'red'
});
var Subcentregroup=[];
var Subgroup=[];
var blueMarker = L.AwesomeMarkers.icon({
    icon: 'coffee',
    markerColor: 'blue'
});

var cloudmade1 = L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
    maxZoom: 18,
    attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors',
    key: 'BC9A493B41014CAABB9dsfsdfds8F0471D759707'
});
//var cloudmade = L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
//    maxZoom: 18,
//    attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors',
//    key: 'BC9A493B41014CAABB9dsfsdfds8F0471D759707'
//});

var map1 = L.map('map1')
    .setView([31.1471, 75.3412], 8)
    .addLayer(cloudmade1);

//var map = L.map('map')
//    .setView([31.1471, 75.3412], 8)
//    .addLayer(cloudmade);

var markers = new L.FeatureGroup();

var header = {

    "Authorization": "Basic " + btoa( "homepage" + ':' + "Homepage123" )

};
var legend = L.control({position: 'bottomright'});

legend.onAdd = function (map) {

    var div = L.DomUtil.create('div', 'info legend'),
        grades = ["Red","Private","Blue","Public","Green","NGO"],
        labels = [],
        from, to;

    for (var i = 0; i < grades.length;) {
        from = grades[i];
        to = grades[i + 1];


        labels.push(
            '<i style="background:' + getColor(from + 1) + '"></i> ' +
            from + (to ? '&ndash;' + to : '+'));
        i=i+2;
    }

    div.innerHTML = labels.join('<br>');
    return div;
};

legend.addTo(map1);

function getColor(d) {
    return d > 1000 ? '#800026' :
        d > 500  ? '#BD0026' :
            d > 200  ? '#E31A1C' :
                d > 100  ? '#FC4E2A' :
                    d == "Green"   ? '#FD8D3C' :
                        d == "Blue"   ? '#FEB24C' :
                            d == "Red"   ? '#FED976' :
                                '#FFEDA0';
}


Ext.onReady( function() {

    var header = {

        "Authorization": "Basic " + btoa( "homepage" + ':' + "Homepage123" )

    };
    //var base = 'http://localhost:8080/demodhis/';

    Ext.Ajax.request({
        url: "dhis-web-commons-security/login.action?authOnly=true",
        method: 'POST',
        params: { j_username: "homepage", j_password: "Homepage123" },
        success:setLinks()
    });

    function setLinks() {

        document.getElementById("map1").style.display = "none";
        $.ajax({

            async : false,
            type: "GET",
            dataType: "json",
            contentType: "application/json",
            headers : header,
            url: '../../api/optionSets/dGdSkUKNC0I.json?fields=options[id,name]',
            success: function(data){
                $.each(data.options, function (index, item) {
                    $('#drop_ownership').append($('<option></option>').val(item.id).html(item.name).text(item.name)
                    );
                });

            },
            error: function(response){

            }
        });

        $.getJSON("../../api/dataElementGroups/fX1ACo0Ffid.json?fields=dataElements[id,name,code]", function (data) {
            var dataElements=data.dataElements;
            dataElements.sort(function(a, b) {
                var nameA=a.name.toLowerCase(), nameB=b.name.toLowerCase()
                if (nameA < nameB) //sort string ascending
                    return -1
                if (nameA > nameB)
                    return 1
                return 0 //default return value (no sorting)
            });


            $.each(dataElements, function (index, item) {
                $('#drop2').append($('<option></option>').val(item.id).html(item.name).text(item.name)
                );
            });
        });

        $.getJSON("../../api/dataElementGroups/y1WarsbVwrv.json?fields=dataElements[id,name]", function (data) {

            var dataElements=data.dataElements;
            dataElements.sort(function(a, b) {
                var nameA=a.name.toLowerCase(), nameB=b.name.toLowerCase()
                if (nameA < nameB) //sort string ascending
                    return -1
                if (nameA > nameB)
                    return 1
                return 0 //default return value (no sorting)
            });

            $.each(dataElements, function (index, item) {
                $('#droptype').append($('<option></option>').val(item.id).html(item.name).text(item.name)
                );
            });
        });



        $.getJSON("../../api/organisationUnitGroups/mH15ENlpG4v.json?fields=organisationUnits[id,name,code]", function (data) {

            var organisationUnits=data.organisationUnits;
            organisationUnits.sort(function(a, b) {
                var nameA=a.name.toLowerCase(), nameB=b.name.toLowerCase()
                if (nameA < nameB) //sort string ascending
                    return -1
                if (nameA > nameB)
                    return 1
                return 0 //default return value (no sorting)
            });

            $.each(organisationUnits, function (index, item) {
                $('#drop1').append($('<option></option>').val(item.id).html(item.name).text(item.name)
                );
            });
        });

        $.ajax({
            async : false,
            type: "GET",
            dataType: "json",
            contentType: "application/json",
            header : header,
            url: '../../api/organisationUnitGroups/w70fxv91JLT.json?fields=organisationUnits[id,name,code]',
            success: function(response){
                Subcentregroup=response.organisationUnits;
                $.each(Subcentregroup, function (index1, item1) {

                    Subgroup.push(item1.id);

                });
            },
            error: function(response){
            }
        });
    }



    $("#drop1").change(function () {
        drop2org=[];
        $('#drophospital').prop('selectedIndex',0);
        var selected = $("#drop1 option:selected");
        orgid="";
        selected.each(function () {
            drop2org.push($(this).val());
            orgid = orgid+$(this).val()+";";
        });
        var organisationUnits;
        for(var i=0;i<drop2org.length;i++)
        {

            $.getJSON("../../api/organisationUnits/"+drop2org[i]+"?paging=false&fields=children[id,name,children[id,name]]", function (data) {
                var organisationUnits=data.children;


                organisationUnits.sort(function(a, b) {
                    var nameA=a.name.toLowerCase(), nameB=b.name.toLowerCase()
                    if (nameA < nameB) //sort string ascending
                        return -1
                    if (nameA > nameB)
                        return 1
                    return 0 //default return value (no sorting)
                });
                var element = document.getElementById("drophospital");
                for (var i = element.length-1; i >= 0; i--) {

                    if(element[i].selected)

                    {

                    }
                    else {
                        element[i].remove();
                    }
                }


                $.each(organisationUnits, function (index1, item1) {
                    if(item1.children.length>0)
                    {
                        var organisationUnitschildren=item1.children;
                        organisationUnitschildren.sort(function(a, b) {
                            var nameA=a.name.toLowerCase(), nameB=b.name.toLowerCase()
                            if (nameA < nameB) //sort string ascending
                                return -1
                            if (nameA > nameB)
                                return 1
                            return 0 //default return value (no sorting)
                        });
                        $.each(organisationUnitschildren, function (index1, item1) {

                            $('#drophospital').append($('<option></option>').val(item1.id).html(item1.name).text(item1.name));
                        });
                    }


                    $('#drophospital').append($('<option></option>').val(item1.id).html(item1.name).text(item1.name)

                    );
                });
                $('#drophospital').selectpicker('refresh');
            });

        }


        //generatefilterrecord(orgid,defservice,defowner,defhealthfacility);
        //jQuery('#sel').html('');
    });

    $("#drophospital").change(function () {
        hospital="";
        var selected = $("#drophospital option:selected");
        selected.each(function () {
            drop2org.push($(this).val());
            orgid = orgid+$(this).val()+";";
            hospital = hospital+$(this).val()+";";
        });

        orgid=hospital;
        //generatefilterrecord(orgid,defservice,defowner,defhealthfacility);
        //jQuery('#sel').html('');
    });

    $("#drop_ownership").change(function () {

         var singlechar="A";
        defowner="XEiMcaGi6vv:IN";
        var selected = $("#drop_ownership option:selected");
        selected.each(function () {
            defowner = defowner+"%3"+singlechar+$(this).text();
            singlechar="B";
        });
    });

    $("#drop2").change(function () {
        if($(this).val()==""||$(this).val()=="null")
        {
            defservice="&dimension=BZ0xteKZNid&dimension=OYAHA8Vhc3G&dimension=qNBCYtrkaD7&dimension=Bv5Fu3onViS&dimension=g7tngXzv2Zz&dimension=mKUqJtDn41L&dimension=G6QYTm3JoNo&dimension=DfbzQg5LTlm&dimension=vvzIfRasrJd&dimension=QeEQe0ERs9X&dimension=sFKV5EA9U6t&dimension=x2SMBBm0P7T&dimension=DeWJ6TcLBGn&dimension=MZ8si8FHS0T&dimension=uyyl3x9jwQa&dimension=bfL2zXyQtrA&dimension=XBO6pg9y1m8&dimension=AEQRulqMjQB&dimension=hEZYkang3cp&dimension=lWsun2ZATjI&dimension=ALMaYK2pMhL&dimension=p2MQZL84eNu&dimension=LRb9HlmAbc6&dimension=sCypRhH8brf&dimension=L11XujC9xzh&dimension=szDQ40J4DTm&dimension=KPpV7WAdys5&dimension=tx0G6s6nBiC&dimension=epW5qI95Cno&dimension=lKQPhgCfuvz&dimension=l1f67ipP6mj&dimension=I3jAOh6ZIMk&dimension=bxGjTYnbgcB&dimension=ttLEYvjxCse&dimension=JOOdfW6RCCD&dimension=TaudXwrGaVC&dimension=gJoAOIEKG9M&dimension=snsCxRbqdHP&dimension=eFpqq53Zifj&dimension=U0rv5FWWeeo&dimension=VDigKipZYu1&dimension=Guub32IStl2&dimension=sIeFKRWtZrn&dimension=IYOefLkrEZk&dimension=t035HNWxNZU&dimension=IlBOWfRZyUc&dimension=dO49PmdQpvT&dimension=akM0bMRwfV4";

        }
        else
        {
            defservice="";
        }

        var selected = $("#drop2 option:selected");
        selected.each(function () {
            defservice=defservice+"&dimension="+$(this).val()+":IN:1";

        });

    });
    $("#droptype").change(function () {
        if($(this).val()==""||$(this).val()=="null")
        {
            defhealthfacility="&dimension=UmlIjjErp1p&dimension=rD7PJQN4TTe&dimension=nvGzrdrt48l&dimension=UfCxf82vB7J";
        }
        else
        {
            defhealthfacility="";
        }
        var selected = $("#droptype option:selected");
        selected.each(function () {
            defhealthfacility=defhealthfacility+"&dimension="+$(this).val()+":IN:1";

        });

        //defhealthfacility=$(this).find("option:selected").val()+":IN:1";
        //generatefilterrecord(orgid,defservice,defowner,defhealthfacility);
    });


});

function calculateIndex(header,analyticsMap){

    for (var i=0;i<analyticsMap.length;i++){

        var key = analyticsMap[i].key;
        for (var j=0;j<header.length;j++){
            if (header[j].name == key){
                analyticsMap[i].index = j;
            }
        }

    }
    return analyticsMap;
}

function myJoin(array){
    var result = "";
    for (key in array){
        if (array[key]){
            result = result+array[key]+", ";
        }
    }
    return result.substr(0,result.length-2);
}
function generatefilterrecord(orgid,defservice,defavail,defowner,defhealthfacility) {
    map1.removeLayer(markers);
    markers.clearLayers();
    $("#footer").hide();
    $("#visitorcounter").hide();
    document.getElementById("resultcount").style.display = "none";
    var analyticsMap = [

        {
            reference : "Name",
            key: "l8VDWUvIHmv",
            index : 0,
            arrayName:"name"
        },{
            reference : "ouid",
            key: "ou",
            index : 0,
            arrayName:"ouid"
        },
        {
            reference : "address",
            key: "KOhqEw0UKxA",
            index : 0,
            arrayName:"address"
        },{
            reference : "pincode",
            key: "xjJR4dTmn4p",
            index : 0,
            arrayName:"pincode"
        },{
            reference : "Village",
            key: "wcmHow1kcBi",
            index : 0,
            arrayName:"addressjoin"
        },{
            reference : "mobile",
            key: "pqVIj8NyTXb",
            index : 0,
            arrayName:"mobile"
        },{
            reference : "Email",
            key: "g7vyRbNim1K",
            index : 0,
            arrayName:"email"
        },

        {
            reference : "Dental",
            key: "mKUqJtDn41L",
            index : 0,
            arrayName : "special"
        },{
            reference : "Dermatology",
            key: "G6QYTm3JoNo",
            index : 0,
            arrayName : "special"
        },
        {
            reference : "Emergency medicine",
            key: "p2MQZL84eNu",
            index : 0,
            arrayName : "special"
        },
        {
            reference : "Obstetrics & gynaecology",
            key: "lKQPhgCfuvz",
            index : 0,
            arrayName : "special"
        },
        {
            reference : "Paedriatrics & child care",
            key: "L11XujC9xzh",
            index : 0,
            arrayName : "special"
        },
        {
            reference : "General medicine",
            key: "XBO6pg9y1m8",
            index : 0,
            arrayName : "special"
        },{
            reference : "Psychiatry",
            key: "ttLEYvjxCse",
            index : 0,
            arrayName : "special"
        },{
            reference : "Radiology",
            key: "eFpqq53Zifj",
            index : 0,
            arrayName : "special"
        },{
            reference : "General surgery",
            key: "x2SMBBm0P7T",
            index : 0,
            arrayName : "special"
        },{
            reference : "Venereology/venereal diseases/STDs",
            key: "sIeFKRWtZrn",
            index : 0,
            arrayName : "special"
        },{
            reference : "Orthopedics",
            key: "l1f67ipP6mj",
            index : 0,
            arrayName : "special"
        },{
            reference : "Physiotherapy",
            key: "I3jAOh6ZIMk",
            index : 0,
            arrayName : "special"
        },{
            reference : "Ear nose throat (ENT)",
            key: "DfbzQg5LTlm",
            index : 0,
            arrayName : "special"
        },{
            reference : "Leprosy clinic",
            key: "IlBOWfRZyUc",
            index : 0,
            arrayName : "special"
        },{
            reference : "HIV/AIDS clinic",
            key: "t035HNWxNZU",
            index : 0,
            arrayName : "special"
        },{
            reference : "Cancer care",
            key: "tx0G6s6nBiC",
            index : 0,
            arrayName : "special"
        },{
            reference : "Nutrition & dietetics",
            key: "LRb9HlmAbc6",
            index : 0,
            arrayName : "special"
        },{
            reference : "Endocrinology",
            key: "vvzIfRasrJd",
            index : 0,
            arrayName : "special"
        },{
            reference : "Burns and plastic surgery",
            key: "akM0bMRwfV4",
            index : 0,
            arrayName : "special"
        },{
            reference : "Aesthetic & reconstructive surgery",
            key: "dO49PmdQpvT",
            index : 0,
            arrayName : "special"
        },{
            reference : "Anaesthesiology",
            key: "BZ0xteKZNid",
            index : 0,
            arrayName : "special"
        },{
            reference : "Audiology & speech therapy",
            key: "OYAHA8Vhc3G",
            index : 0,
            arrayName : "special"
        },{
            reference : "Bariatric/weightloss surgery",
            key: "qNBCYtrkaD7",
            index : 0,
            arrayName : "special"
        },{
            reference : "Bone marrow transplant",
            key: "Bv5Fu3onViS",
            index : 0,
            arrayName : "special"
        },{
            reference : "Cardiology",
            key: "ALMaYK2pMhL",
            index : 0,
            arrayName : "special"
        },{
            reference : "Critical care",
            key: "g7tngXzv2Zz",
            index : 0,
            arrayName : "special"
        },{
            reference : "Eye care",
            key: "QeEQe0ERs9X",
            index : 0,
            arrayName : "special"
        },{
            reference : "Gastroenterology",
            key: "sFKV5EA9U6t",
            index : 0,
            arrayName : "special"
        },{
            reference : "Health & wellness",
            key: "DeWJ6TcLBGn",
            index : 0,
            arrayName : "special"
        },{
            reference : "Internal medicine",
            key: "MZ8si8FHS0T",
            index : 0,
            arrayName : "special"
        },{
            reference : "IVF",
            key: "uyyl3x9jwQa",
            index : 0,
            arrayName : "special"
        },{
            reference : "Kidney transplant",
            key: "bfL2zXyQtrA",
            index : 0,
            arrayName : "special"
        },{
            reference : "Mental health & behavioral sciences",
            key: "AEQRulqMjQB",
            index : 0,
            arrayName : "special"
        },{
            reference : "Nephrology",
            key: "lWsun2ZATjI",
            index : 0,
            arrayName : "special"
        },
        {
            reference : "Neurosciences",
            key: "hEZYkang3cp",
            index : 0,
            arrayName : "special"
        },
        {
            reference : "Pulmonology",
            key: "szDQ40J4DTm",
            index : 0,
            arrayName : "special"
        },
        {
            reference : "Podiatry",
            key: "bxGjTYnbgcB",
            index : 0,
            arrayName : "special"
        },
        {
            reference : "Rheumatology",
            key: "JOOdfW6RCCD",
            index : 0,
            arrayName : "special"
        },
        {
            reference : "Thoracic surgery",
            key: "TaudXwrGaVC",
            index : 0,
            arrayName : "special"
        }, {
            reference : "Urology",
            key: "KPpV7WAdys5",
            index : 0,
            arrayName : "special"
        }, {
            reference : "Oncology, cancer care surgery and treatment",
            key: "sCypRhH8brf",
            index : 0,
            arrayName : "special"
        }, {
            reference : "Clinical hematology",
            key: "IYOefLkrEZk",
            index : 0,
            arrayName : "special"
        }, {
            reference : "Clinical pharmacology",
            key: "epW5qI95Cno",
            index : 0,
            arrayName : "special"
        }, {
            reference : "Clinical immunology",
            key: "gJoAOIEKG9M",
            index : 0,
            arrayName : "special"
        }, {
            reference : "Medical genetics",
            key: "snsCxRbqdHP",
            index : 0,
            arrayName : "special"
        }, {
            reference : "Infectious diseases",
            key: "Guub32IStl2",
            index : 0,
            arrayName : "special"
        }, {
            reference : "Virology",
            key: "U0rv5FWWeeo",
            index : 0,
            arrayName : "special"
        }, {
            reference : "Contact person-name",
            key: "Gx4VSNet1dC",
            index : 0,
            arrayName : "contactpname"
        },{
            reference : "Contact person-mobile number",
            key: "bUg8a8bAvJs",
            index : 0,
            arrayName : "contactpnumber"
        },{
            reference : "Only OPD",
            key: "rD7PJQN4TTe",
            index : 0,
            arrayName : "hfacilities"
        },{
            reference : "Both IPD and OPD",
            key: "nvGzrdrt48l",
            index : 0,
            arrayName : "hfacilities"
        },{
            reference : "Only lab",
            key: "UmlIjjErp1p",
            index : 0,
            arrayName : "hfacilities"
        },{
            reference : "Only diagnostic",
            key: "UfCxf82vB7J",
            index : 0,
            arrayName : "hfacilities"
        },{
            reference : "Ambulance type",
            key: "tC8Ad3DuScJ",
            index : 0,
            arrayName : "availspecialities"
        },{
            reference : "Blood bank",
            key: "jXCd8k2841l",
            index : 0,
            arrayName : "availspecialities"
        },{
            reference : "Casualty/Emergency",
            key: "RkP5neDLbHv",
            index : 0,
            arrayName : "availspecialities"
        },{
            reference : "Operation theatre",
            key: "avHST8wLPnX",
            index : 0,
            arrayName : "availspecialities"
        },{
            reference : "Ventilator available",
            key: "txl9e6UJFP4",
            index : 0,
            arrayName : "availspecialities"
        },{
            reference : "Health insurance schemes",
            key: "ZUbPsfW6y0C",
            index : 0,
            arrayName : "schemes"
        },{
            reference : "Bhagat puran singh bima yojana",
            key: "CAOM6riDtfU",
            index : 0,
            arrayName : "schemes"
        },{
            reference : "Punjab government employees & pensioners health insurance scheme (PGEPHIS)",
            key: "YL7OJoQCAmF",
            index : 0,
            arrayName : "schemes"
        },{
            reference : "Other health insurance schemes",
            key: "vJO1Jac84Ar",
            index : 0,
            arrayName : "schemes"
        },{
            reference : "Bajaj Alliance Health Insurance Scheme",
            key: "kF8ZJYe9SJZ",
            index : 0,
            arrayName : "schemes"
        },{
            reference : "Max Bupa Health Insurance schemes",
            key: "tNhLX6c7KHp",
            index : 0,
            arrayName : "schemes"
        },{
            reference : "National Health Insurance schemes",
            key: "bVENUe0eDsO",
            index : 0,
            arrayName : "schemes"
        },
        {
            reference : "Ownership details",
            key: "XEiMcaGi6vv",
            index : 0,
            arrayName : "ownership"
        }

    ]
    document.getElementById("loader").style.display = "block";
    $("#content1").hide();
    document.getElementById("map1").style.display = "block";


    //$("#map1").show();


    $("#footer1").hide();
    jQuery('#sel').html('');
    $(".w3-card-4").remove();


    var addressjoin=[],ouid=[],availspecialitiesjoin,availspecialities=[],avaialabilityjoin,name=[],address=[],pincode=[],village=[],mobile=[],special=[],notspecial=[],hfacilities=[],nothfacilities=[],schemes=[],notschemes=[],contactpname=[],contactpnumber=[];
    var toAdd,spec=[],email=[],specialjoin,notspecialjoin,notspec=[],owner=[],notowner=[],hfacilitiesjoin,nothfacilitiesjoin,schemesjoin,notschemesjoin,hfschemes=[],nothfschemes=[];
    var healthfac="",ownership=[],availspecialiti=[];
    var arrayMap = [];latitude=[];longitude=[];


    $.getJSON("../../api/analytics/events/query/tzR46QRZ6FJ.json?stage=o6ps51YxGNb&dimension=pe:LAST_5_YEARS&dimension=ou:"+orgid+"&dimension=l8VDWUvIHmv&dimension=KOhqEw0UKxA&dimension=xjJR4dTmn4p&dimension=wcmHow1kcBi&dimension=pqVIj8NyTXb&dimension=g7vyRbNim1K&dimension=Gx4VSNet1dC&dimension=bUg8a8bAvJs"+defservice+"&dimension="+defavail+"&dimension="+defowner+defhealthfacility+"&dimension=ZUbPsfW6y0C&dimension=CAOM6riDtfU&dimension=YL7OJoQCAmF&dimension=vJO1Jac84Ar&dimension=kF8ZJYe9SJZ&dimension=tNhLX6c7KHp&dimension=bVENUe0eDsO&displayProperty=NAME", function (data) {
        var constants={key:name, value: value}

        analyticsMap = calculateIndex(data.headers,analyticsMap);

        if(data.rows.length==0)
        {
            document.getElementById("noresult").style.display = "block";
            //alert("No result found for above selection");
            document.getElementById("loader").style.display = "none";
        }

        for(var k=0;k<data.rows.length;k++){

            document.getElementById("noresult").style.display = "none";
            arrayMap["special"] = special;
            arrayMap["name"] = name;
            arrayMap["address"] = addressjoin;
            arrayMap["pincode"] = pincode;
            arrayMap["village"] = village;
            arrayMap["mobile"] = mobile;
            arrayMap["notspecial"] = notspecial;
            arrayMap["hfacilities"] = hfacilities;
            arrayMap["nothfacilities"] = nothfacilities;
            arrayMap["schemes"] = schemes;
            arrayMap["notschemes"] = notschemes;
            arrayMap["contactpname"] = contactpname;
            arrayMap["contactpnumber"] = contactpnumber;
            arrayMap["availspecialities"] = availspecialities;
            arrayMap["ownership"] = ownership;
            arrayMap["ouid"] = ouid;

            for (var j=0;j<analyticsMap.length;j++){

                if (analyticsMap[j].index > 0){
                    var value = data.rows[k][analyticsMap[j].index];
                    if (value == 1){
                        value = data.headers[analyticsMap[j].index].column;
                    }

                    if (!value || value == 0){
                        value = "";
                    }
                    if(arrayMap[analyticsMap[j].arrayName]){
                        arrayMap[analyticsMap[j].arrayName].push(value);
                    }
                }
            }
            specialjoin = myJoin(special);
            availspecialitiesjoin = myJoin(availspecialities);
            notspecialjoin = myJoin(notspecial);
            hfacilitiesjoin = myJoin(hfacilities);
            nothfacilitiesjoin = myJoin(nothfacilities);
            schemesjoin = myJoin(schemes);
            notschemesjoin = myJoin(notschemes);

            spec.push(specialjoin);
            notspec.push(notspecialjoin);
            owner.push(hfacilitiesjoin);
            availspecialiti.push(availspecialitiesjoin);
            notowner.push(nothfacilitiesjoin);
            hfschemes.push(schemesjoin);
            nothfschemes.push(notschemesjoin);


            availspecialities=[];
            special = [];
            notspecial = [];
            hfacilities = [];
            nothfacilities = [];
            schemes = [];
            notschemes = [];




        }
        var header = {
            "Authorization": "Basic " + btoa( "homepage" + ':' + "Homepage123@123" )
        };
        for (var i = 0; i < name.length; i++) {
            $.ajax({
                async: false,
                type: "GET",
                dataType: "json",
                contentType: "application/json",
                header: header,
                url: '../../api/organisationUnits/' + ouid[i] + '.json?fields=[id,name,coordinates]',
                success: function (response) {
                    var coordinates = JSON.parse(response.coordinates);
                    latitude.push(coordinates[0]);
                    longitude.push(coordinates[1]);


                },
                error: function (response) {

                }
            });
        }




        for (var i = 0; i < ouid.length; i++) {


            if(Subgroup.includes(ouid[i]))
            {

            }
            else
            {

                if(ownership[i]=="Public")
                {
                    var marker = L.marker([longitude[i], latitude[i]], {icon: blueMarker}).bindPopup(name[i]+","+" </br><strong>Contact:</strong>"+  mobile[i]+ "</br><strong>Schemes:</strong>"+hfschemes[i]+"</br><strong>Availabilities: </strong>"+availspecialiti[i]+"</br>GoTo List View for more details").openPopup();
                    markers.addLayer(marker);
                    map1.addLayer(markers);
                }
                else if(ownership[i]=="Private")
                {
                    var marker=L.marker([longitude[i], latitude[i]], {icon: redMarker}).bindPopup(name[i]).openPopup();
                    markers.addLayer(marker);
                    map1.addLayer(markers);
                }

            }

        }


    });

    document.getElementById("loader").style.display = "none";
}

function generatefilterrecordlist(orgid,defservice,defavail,defowner,defhealthfacility) {
    $("#footer").hide();
    $("#visitorcounter").hide();
    var analyticsMap = [

        {
            reference : "Name",
            key: "l8VDWUvIHmv",
            index : 0,
            arrayName:"name"
        },{
            reference : "ouid",
            key: "ou",
            index : 0,
            arrayName:"ouid"
        },
        {
            reference : "address",
            key: "KOhqEw0UKxA",
            index : 0,
            arrayName:"address"
        },{
            reference : "pincode",
            key: "xjJR4dTmn4p",
            index : 0,
            arrayName:"pincode"
        },{
            reference : "Village",
            key: "wcmHow1kcBi",
            index : 0,
            arrayName:"addressjoin"
        },{
            reference : "mobile",
            key: "pqVIj8NyTXb",
            index : 0,
            arrayName:"mobile"
        },{
            reference : "Email",
            key: "g7vyRbNim1K",
            index : 0,
            arrayName:"email"
        },

        {
            reference : "Dental",
            key: "mKUqJtDn41L",
            index : 0,
            arrayName : "special"
        },{
            reference : "Dermatology",
            key: "G6QYTm3JoNo",
            index : 0,
            arrayName : "special"
        },
        {
            reference : "Emergency medicine",
            key: "p2MQZL84eNu",
            index : 0,
            arrayName : "special"
        },
        {
            reference : "Obstetrics & gynaecology",
            key: "lKQPhgCfuvz",
            index : 0,
            arrayName : "special"
        },
        {
            reference : "Paedriatrics & child care",
            key: "L11XujC9xzh",
            index : 0,
            arrayName : "special"
        },
        {
            reference : "General medicine",
            key: "XBO6pg9y1m8",
            index : 0,
            arrayName : "special"
        },{
            reference : "Psychiatry",
            key: "ttLEYvjxCse",
            index : 0,
            arrayName : "special"
        },{
            reference : "Radiology",
            key: "eFpqq53Zifj",
            index : 0,
            arrayName : "special"
        },{
            reference : "General surgery",
            key: "x2SMBBm0P7T",
            index : 0,
            arrayName : "special"
        },{
            reference : "Venereology/venereal diseases/STDs",
            key: "sIeFKRWtZrn",
            index : 0,
            arrayName : "special"
        },{
            reference : "Orthopedics",
            key: "l1f67ipP6mj",
            index : 0,
            arrayName : "special"
        },{
            reference : "Physiotherapy",
            key: "I3jAOh6ZIMk",
            index : 0,
            arrayName : "special"
        },{
            reference : "Ear nose throat (ENT)",
            key: "DfbzQg5LTlm",
            index : 0,
            arrayName : "special"
        },{
            reference : "Leprosy clinic",
            key: "IlBOWfRZyUc",
            index : 0,
            arrayName : "special"
        },{
            reference : "HIV/AIDS clinic",
            key: "t035HNWxNZU",
            index : 0,
            arrayName : "special"
        },{
            reference : "Cancer care",
            key: "tx0G6s6nBiC",
            index : 0,
            arrayName : "special"
        },{
            reference : "Nutrition & dietetics",
            key: "LRb9HlmAbc6",
            index : 0,
            arrayName : "special"
        },{
            reference : "Endocrinology",
            key: "vvzIfRasrJd",
            index : 0,
            arrayName : "special"
        },{
            reference : "Burns and plastic surgery",
            key: "akM0bMRwfV4",
            index : 0,
            arrayName : "special"
        },{
            reference : "Aesthetic & reconstructive surgery",
            key: "dO49PmdQpvT",
            index : 0,
            arrayName : "special"
        },{
            reference : "Anaesthesiology",
            key: "BZ0xteKZNid",
            index : 0,
            arrayName : "special"
        },{
            reference : "Audiology & speech therapy",
            key: "OYAHA8Vhc3G",
            index : 0,
            arrayName : "special"
        },{
            reference : "Bariatric/weightloss surgery",
            key: "qNBCYtrkaD7",
            index : 0,
            arrayName : "special"
        },{
            reference : "Bone marrow transplant",
            key: "Bv5Fu3onViS",
            index : 0,
            arrayName : "special"
        },{
            reference : "Cardiology",
            key: "ALMaYK2pMhL",
            index : 0,
            arrayName : "special"
        },{
            reference : "Critical care",
            key: "g7tngXzv2Zz",
            index : 0,
            arrayName : "special"
        },{
            reference : "Eye care",
            key: "QeEQe0ERs9X",
            index : 0,
            arrayName : "special"
        },{
            reference : "Gastroenterology",
            key: "sFKV5EA9U6t",
            index : 0,
            arrayName : "special"
        },{
            reference : "Health & wellness",
            key: "DeWJ6TcLBGn",
            index : 0,
            arrayName : "special"
        },{
            reference : "Internal medicine",
            key: "MZ8si8FHS0T",
            index : 0,
            arrayName : "special"
        },{
            reference : "IVF",
            key: "uyyl3x9jwQa",
            index : 0,
            arrayName : "special"
        },{
            reference : "Kidney transplant",
            key: "bfL2zXyQtrA",
            index : 0,
            arrayName : "special"
        },{
            reference : "Mental health & behavioral sciences",
            key: "AEQRulqMjQB",
            index : 0,
            arrayName : "special"
        },{
            reference : "Nephrology",
            key: "lWsun2ZATjI",
            index : 0,
            arrayName : "special"
        },
        {
            reference : "Neurosciences",
            key: "hEZYkang3cp",
            index : 0,
            arrayName : "special"
        },
        {
            reference : "Pulmonology",
            key: "szDQ40J4DTm",
            index : 0,
            arrayName : "special"
        },
        {
            reference : "Podiatry",
            key: "bxGjTYnbgcB",
            index : 0,
            arrayName : "special"
        },
        {
            reference : "Rheumatology",
            key: "JOOdfW6RCCD",
            index : 0,
            arrayName : "special"
        },
        {
            reference : "Thoracic surgery",
            key: "TaudXwrGaVC",
            index : 0,
            arrayName : "special"
        }, {
            reference : "Urology",
            key: "KPpV7WAdys5",
            index : 0,
            arrayName : "special"
        }, {
            reference : "Oncology, cancer care surgery and treatment",
            key: "sCypRhH8brf",
            index : 0,
            arrayName : "special"
        }, {
            reference : "Clinical hematology",
            key: "IYOefLkrEZk",
            index : 0,
            arrayName : "special"
        }, {
            reference : "Clinical pharmacology",
            key: "epW5qI95Cno",
            index : 0,
            arrayName : "special"
        }, {
            reference : "Clinical immunology",
            key: "gJoAOIEKG9M",
            index : 0,
            arrayName : "special"
        }, {
            reference : "Medical genetics",
            key: "snsCxRbqdHP",
            index : 0,
            arrayName : "special"
        }, {
            reference : "Infectious diseases",
            key: "Guub32IStl2",
            index : 0,
            arrayName : "special"
        }, {
            reference : "Virology",
            key: "U0rv5FWWeeo",
            index : 0,
            arrayName : "special"
        }, {
            reference : "Contact person-name",
            key: "Gx4VSNet1dC",
            index : 0,
            arrayName : "contactpname"
        },{
            reference : "Contact person-mobile number",
            key: "bUg8a8bAvJs",
            index : 0,
            arrayName : "contactpnumber"
        },{
            reference : "Only OPD",
            key: "rD7PJQN4TTe",
            index : 0,
            arrayName : "hfacilities"
        },{
            reference : "Both IPD and OPD",
            key: "nvGzrdrt48l",
            index : 0,
            arrayName : "hfacilities"
        },{
            reference : "Only lab",
            key: "UmlIjjErp1p",
            index : 0,
            arrayName : "hfacilities"
        },{
            reference : "Only diagnostic",
            key: "UfCxf82vB7J",
            index : 0,
            arrayName : "hfacilities"
        },{
            reference : "Ambulance type",
            key: "tC8Ad3DuScJ",
            index : 0,
            arrayName : "availspecialities"
        },{
            reference : "Blood bank",
            key: "jXCd8k2841l",
            index : 0,
            arrayName : "availspecialities"
        },{
            reference : "Casualty/Emergency",
            key: "RkP5neDLbHv",
            index : 0,
            arrayName : "availspecialities"
        },{
            reference : "Operation theatre",
            key: "avHST8wLPnX",
            index : 0,
            arrayName : "availspecialities"
        },{
            reference : "Ventilator available",
            key: "txl9e6UJFP4",
            index : 0,
            arrayName : "availspecialities"
        },{
            reference : "Health insurance schemes",
            key: "ZUbPsfW6y0C",
            index : 0,
            arrayName : "schemes"
        },{
            reference : "Bhagat puran singh bima yojana",
            key: "CAOM6riDtfU",
            index : 0,
            arrayName : "schemes"
        },{
            reference : "Punjab government employees & pensioners health insurance scheme (PGEPHIS)",
            key: "YL7OJoQCAmF",
            index : 0,
            arrayName : "schemes"
        },{
            reference : "Other health insurance schemes",
            key: "vJO1Jac84Ar",
            index : 0,
            arrayName : "schemes"
        },{
            reference : "Bajaj Alliance Health Insurance Scheme",
            key: "kF8ZJYe9SJZ",
            index : 0,
            arrayName : "schemes"
        },{
            reference : "Max Bupa Health Insurance schemes",
            key: "tNhLX6c7KHp",
            index : 0,
            arrayName : "schemes"
        },{
            reference : "National Health Insurance schemes",
            key: "bVENUe0eDsO",
            index : 0,
            arrayName : "schemes"
        },
        {
            reference : "Ownership details",
            key: "XEiMcaGi6vv",
            index : 0,
            arrayName : "ownership"
        }

    ]

    document.getElementById("resultcount").style.display = "block";
    document.getElementById("loader").style.display = "block";
    resultcount=0;
    $("#content1").hide();
    //$("#map1").hide();
    document.getElementById("map1").style.display = "none";

    $("#footer1").hide();



    jQuery('#sel').html('');
    $(".w3-card-4").remove();

    var addressjoin=[],ouid=[],availspecialitiesjoin,availspecialities=[],avaialabilityjoin,name=[],address=[],pincode=[],village=[],mobile=[],special=[],notspecial=[],hfacilities=[],nothfacilities=[],schemes=[],notschemes=[],contactpname=[],contactpnumber=[];
    var toAdd,spec=[],email=[],specialjoin,notspecialjoin,notspec=[],owner=[],notowner=[],hfacilitiesjoin,nothfacilitiesjoin,schemesjoin,notschemesjoin,hfschemes=[],nothfschemes=[];
    var healthfac="",ownership=[],availspecialiti=[];
    var arrayMap = [];latitude=[];longitude=[];


    $.getJSON("../../api/analytics/events/query/tzR46QRZ6FJ.json?stage=o6ps51YxGNb&dimension=pe:LAST_5_YEARS&dimension=ou:"+orgid+"&dimension=l8VDWUvIHmv&dimension=KOhqEw0UKxA&dimension=xjJR4dTmn4p&dimension=wcmHow1kcBi&dimension=pqVIj8NyTXb&dimension=g7vyRbNim1K&dimension=Gx4VSNet1dC&dimension=bUg8a8bAvJs"+defservice+"&dimension="+defavail+"&dimension="+defowner+defhealthfacility+"&dimension=ZUbPsfW6y0C&dimension=CAOM6riDtfU&dimension=YL7OJoQCAmF&dimension=vJO1Jac84Ar&dimension=kF8ZJYe9SJZ&dimension=tNhLX6c7KHp&dimension=bVENUe0eDsO&displayProperty=NAME", function (data) {
        var constants={key:name, value: value}

        analyticsMap = calculateIndex(data.headers,analyticsMap);

        if(data.rows.length==0)
        {
            document.getElementById("noresult").style.display = "block";
            //alert("No result found for above selection");
            document.getElementById("loader").style.display = "none";
        }
        for(var k=0;k<data.rows.length;k++){

            document.getElementById("noresult").style.display = "none";
            arrayMap["special"] = special;
            arrayMap["name"] = name;
            arrayMap["address"] = addressjoin;
            arrayMap["pincode"] = pincode;
            arrayMap["village"] = village;
            arrayMap["mobile"] = mobile;
            arrayMap["notspecial"] = notspecial;
            arrayMap["hfacilities"] = hfacilities;
            arrayMap["nothfacilities"] = nothfacilities;
            arrayMap["schemes"] = schemes;
            arrayMap["notschemes"] = notschemes;
            arrayMap["contactpname"] = contactpname;
            arrayMap["contactpnumber"] = contactpnumber;
            arrayMap["availspecialities"] = availspecialities;
            arrayMap["ownership"] = ownership;
            arrayMap["ouid"] = ouid;

            for (var j=0;j<analyticsMap.length;j++){

                if (analyticsMap[j].index > 0){
                    var value = data.rows[k][analyticsMap[j].index];
                    if (value == 1){
                        value = data.headers[analyticsMap[j].index].column;
                    }

                    if (!value || value == 0){
                        value = "";
                    }
                    if(arrayMap[analyticsMap[j].arrayName]){
                        arrayMap[analyticsMap[j].arrayName].push(value);
                    }
                }
            }
            specialjoin = myJoin(special);
            availspecialitiesjoin = myJoin(availspecialities);
            notspecialjoin = myJoin(notspecial);
            hfacilitiesjoin = myJoin(hfacilities);
            nothfacilitiesjoin = myJoin(nothfacilities);
            schemesjoin = myJoin(schemes);
            notschemesjoin = myJoin(notschemes);

            spec.push(specialjoin);
            notspec.push(notspecialjoin);
            owner.push(hfacilitiesjoin);
            availspecialiti.push(availspecialitiesjoin);
            notowner.push(nothfacilitiesjoin);
            hfschemes.push(schemesjoin);
            nothfschemes.push(notschemesjoin);


            availspecialities=[];
            special = [];
            notspecial = [];
            hfacilities = [];
            nothfacilities = [];
            schemes = [];
            notschemes = [];

        }

        for (var i = 0; i < ouid.length; i++) {


            if(Subgroup.includes(ouid[i]))
            {

            }
            else
            {
                obj = new constructor_obj(document.body, name[i], addressjoin[i], pincode[i], mobile[i], spec[i], owner[i],availspecialiti[i], contactpname[i], contactpnumber[i], email[i], hfschemes[i], nothfschemes[i], ouid[i],ownership[i]);
                resultcount++;
            }

        }

        document.getElementById('resultcount').innerHTML = '<strong>Total number of Results:</strong>'+ resultcount;
    });

}



function constructor_obj(parent, title, address,pincode,mobile,spec,owner,avaialability,contactpname,contactpnumber, email,hfschemes,nothfschemes,ouid,ownership) {
    var procedures=[],costs=[];
    this.parent = parent;
    this.title = title.toUpperCase();
    this.address = address;
    this.pincode = pincode;
    this.mobile = mobile;
    this.spec = spec;
    this.owner = owner;
    this.avaialability = avaialability;
    this.contactpname = contactpname;
    this.contactpnumber = contactpnumber;
    this.email = email;
    this.hfschemes = hfschemes;
    this.nothfschemes = nothfschemes;
    this.ouid = ouid;
    this.ownership = ownership;

//this.avgRating = avgRating;

    var div = document.createElement('div');
    div.id = 'sel';
    div.style = "width:auto;"

    $.getJSON("../../api/analytics/events/query/tzR46QRZ6FJ.json?stage=o6ps51YxGNb&dimension=pe:LAST_5_YEARS&dimension=ou:"+ouid+"&dimension=eH2F2xuLmoY&dimension=kfDoQ3V1RQK&dimension=csjh8jewk7x&dimension=WXnr5Qk8Qgo&dimension=UwGx1EmJyIf&dimension=rzNUVjOm5ZJ&dimension=tpm9TIh7IeQ&dimension=pUYrErSv4Kf&dimension=hq5P29o8auc&dimension=KBgdegZWYDc&dimension=Mpo7Zm6z9WL&dimension=xtGxtg2I1SH&dimension=EhBs6eq4ebt&dimension=ThPPuHVPTsZ&dimension=hK82b1FNhui&dimension=aLY8DrZ88WN&dimension=XfstNyl31ca&dimension=fOLmSsSSlVI&dimension=aLnsQJ6De8p&dimension=LFDq2RYMt88&displayProperty=NAME", function (cdata) {


        for(var p=0;p<cdata.rows.length;p++)
        {
            for(var k=8;k<27;k++)
            {
                if(cdata.rows[p][k]!=""&&cdata.rows[p][k]!=0)
                {
                    procedures.push(cdata.rows[p][k]);
                    costs.push(cdata.rows[p][k+1]);
                }
                k++;
            }


        }

        var htmlstring="";
        htmlstring='<table><tr><th style="width:30%">Procedure</th><th style="width:30%">Cost</th></tr>'
        for(var i =0;i<=procedures.length-1;i++)
        {
            htmlstring+='<tr><td>'+procedures[i]+'</td><td>'+costs[i]+'</td></tr>'

        }
        htmlstring+='</table>';
        $("#procedureid").empty();
        $("#procedureid").append(htmlstring);

    });


    div.innerHTML='\
    <div class="w3-card-4" style="margin-left:15%;margin-right:15%;" id="test1">\
    <div >\
     <header  style="background-color:#2978B3;height: 3%" >\
      <table width="100px">\
      <tr>\
       <td width="8%"><i class="fa fa-h-square" style="font-size:36px; color:White;"></i></td>\
       <td width="72%"><h3 style="color:white;" class="title"></h3></td>\
        <td width="20%"><p> <a id="createAccountLink" style="color: white" target="_blank" href="lib/controllers/feedback.html?ouid='+ouid+'">Rate This Facility</a></p></td>\
       </tr>\
       </table>\
        </header>\
        </br>\
        <div class="w3-container">\
        <table width="100%" style="background-color: aliceblue">\
         <tr>\
        <td width="20%"><p><strong>Address</strong></p></td>\
        <td width="50%"><p class="address"></p></td>\
        <td width="10%"><p><strong>Pincode</strong></p></td>\
        <td width="18%" class="pincode"><p></p></td></td>\
        </tr>\
        </table>\
        <table width="100%">\
        <tr>\
            <td width="20%" ><strong>Contact Number</strong></td>\
            <td width="20%"><p class="mobile"></p></td>\
            <td width="20%"><p> <a id="createAccountLink" target="_blank" href="lib/controllers/procedures.html?ouid='+ouid+'">Procedure List</a></p></td>\
            <td width="20%"><p> <a id="createAccountLink" target="_blank" href="lib/controllers/doctors.html?ouid='+ouid+'">Doctor List</a></p></td>\
        </tr>\
    </table>\
</div>\
     <button class="w3-btn-block w3-dark-grey" onclick="myfunc(\''+ouid+'\')">More Details</button>\
    </br>\
        <div class="noDisplay" id="'+ouid+'">\
          </br>\
        <table width="100%" class="w3-table w3-striped w3-bordered w3-hoverable">\
        <tr>\
        <td width="20%"><p><strong style="margin-left: 10%">Specialities</strong></p></td>\
        <td width="80%"><p style="text-align: justify;" class="specialty"></p></td>\
    </tr>\
    <tr>\
    <td width="20%"><p><strong style="margin-left: 10%">Health Facilites</strong></p></td>\
    <td><p class="owner"></p></td>\
    </tr>\
    <tr>\
    <td width="20%"><p><strong style="margin-left: 10%">Services</strong></p></td>\
        <td width="80%"><p class="availability"></p></td>\
    </tr>\
    <tr>\
    <td width="20%"><p><strong style="margin-left: 10%">Contact Person</strong></p></td>\
    <td width="80%"><p class="contactperson"></p></td>\
    </tr>\
        <tr>\
    <td width="20%"><p><strong style="margin-left: 10%">Email</p></strong></td>\
    <td width="80%"><p class="email"></p></td>\
    </tr>\
     <tr>\
    <td width="20%"><p><strong style="margin-left: 10%">Phone:</strong></p></td>\
    <td width="80%"><p class="contactpnumber"></p></td>\
    </tr>\
    <tr>\
    <td width="20%"><strong style="margin-left: 10%">Schemes</strong></td>\
        <td width="80%"><p class="hfschemes"></p></td>\
    </tr>\
    <tr>\
    <td width="20%"><strong style="margin-left:10%">Ownership</strong></td>\
        <td width="80%"><p class="ownership"></p></td>\
        </tr>\
        </table>\
        </br>\
        </div>\
        </div>\
        </div>\
    ';

    this.parent.appendChild(div);
    div.getElementsByClassName('title')[0].innerHTML = this.title?this.title:"";
    div.getElementsByClassName('address')[0].innerHTML = this.address?this.address:"";
    div.getElementsByClassName('mobile')[0].innerHTML = this.mobile?this.mobile:"";
    div.getElementsByClassName('pincode')[0].innerHTML = this.pincode?this.pincode.substring(0,this.pincode.length -2):"";
    div.getElementsByClassName('specialty')[0].innerHTML = this.spec?this.spec:"";
    div.getElementsByClassName('contactperson')[0].innerHTML = this.contactpname?this.contactpname:"";
    div.getElementsByClassName('contactpnumber')[0].innerHTML = this.contactpnumber?this.contactpnumber:"";
    div.getElementsByClassName('email')[0].innerHTML = this.email?this.email:"";
    div.getElementsByClassName('owner')[0].innerHTML = this.owner?this.owner:"";
    div.getElementsByClassName('availability')[0].innerHTML = this.avaialability?this.avaialability:"";
    div.getElementsByClassName('hfschemes')[0].innerHTML = this.hfschemes?this.hfschemes:"";
    div.getElementsByClassName('ownership')[0].innerHTML = this.ownership?this.ownership:"";

//        div.getElementsByClassName('rating')[0].innerHTML = "3.0";
//        div.getElementsByClassName('rating')[0].innerHTML = this.avgRating;
    this.parent="";
    document.getElementById("loader").style.display = "none";
    document.getElementById("resultcount")== "asdsadsa";
}


function myfunc(ouid){
//      var div = $("#"+thiz)[0];
//              div.className="";
    $("#"+ouid).slideToggle(1000);
}
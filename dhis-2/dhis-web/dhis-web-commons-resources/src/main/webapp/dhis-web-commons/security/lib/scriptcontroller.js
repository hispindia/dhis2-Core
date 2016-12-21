

var base = "../../";
var url=base+"dhis-web-commons-security/login.action";
var orgid="wSslG6mcGXl";
var ouid=[];
var filterhospital,service,owner,healthfacility;
var hospital;
var defavail="avHST8wLPnX&dimension=jXCd8k2841l&dimension=txl9e6UJFP4";
var defservice="BZ0xteKZNid&dimension=OYAHA8Vhc3G&dimension=qNBCYtrkaD7&dimension=Bv5Fu3onViS&dimension=g7tngXzv2Zz&dimension=mKUqJtDn41L&dimension=G6QYTm3JoNo&dimension=DfbzQg5LTlm&dimension=vvzIfRasrJd&dimension=QeEQe0ERs9X&dimension=sFKV5EA9U6t&dimension=x2SMBBm0P7T&dimension=DeWJ6TcLBGn&dimension=MZ8si8FHS0T&dimension=uyyl3x9jwQa&dimension=bfL2zXyQtrA&dimension=XBO6pg9y1m8&dimension=AEQRulqMjQB&dimension=hEZYkang3cp&dimension=lWsun2ZATjI&dimension=ALMaYK2pMhL&dimension=p2MQZL84eNu&dimension=LRb9HlmAbc6&dimension=sCypRhH8brf&dimension=L11XujC9xzh&dimension=szDQ40J4DTm&dimension=KPpV7WAdys5&dimension=tx0G6s6nBiC&dimension=epW5qI95Cno&dimension=lKQPhgCfuvz&dimension=l1f67ipP6mj&dimension=I3jAOh6ZIMk&dimension=bxGjTYnbgcB&dimension=ttLEYvjxCse&dimension=JOOdfW6RCCD&dimension=TaudXwrGaVC&dimension=gJoAOIEKG9M&dimension=snsCxRbqdHP&dimension=eFpqq53Zifj&dimension=U0rv5FWWeeo&dimension=VDigKipZYu1&dimension=Guub32IStl2&dimension=sIeFKRWtZrn&dimension=IYOefLkrEZk&dimension=t035HNWxNZU&dimension=IlBOWfRZyUc&dimension=dO49PmdQpvT&dimension=akM0bMRwfV4";
// var defowner="EwolVkPAKN6&dimension=aI5XEAH8PkC&dimension=VozTuKA0GP1";
var defowner="XEiMcaGi6vv";
var defhealthfacility="UmlIjjErp1p&dimension=rD7PJQN4TTe&dimension=nvGzrdrt48l&dimension=UfCxf82vB7J";
var avgRating=[],avgRating1;
var divele;
var servicename,serviceid;
var name=[],address=[],pincode=[],village=[],mobile=[],owner=[],special=[];
var toAdd,spec=[],specialjoin;
var redMarker = L.AwesomeMarkers.icon({
    icon: 'coffee',
    markerColor: 'red'
});

var blueMarker = L.AwesomeMarkers.icon({
    icon: 'coffee',
    markerColor: 'blue'
});

var map1 =L.map('map1').setView([31.1471, 75.3412], 10);

L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
}).addTo(map1);


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


        //var greenIcon = L.icon({
        //    iconUrl: 'hospital.png',
        //
        //    iconSize:     [10, 20], // size of the icon
        //    shadowSize:   [50, 64], // size of the shadow
        //    iconAnchor:   [22, 94], // point of the icon which will correspond to marker's location
        //    shadowAnchor: [4, 62],  // the same for the shadow
        //    popupAnchor:  [-3, -76] // point from which the popup should open relative to the iconAnchor
        //});

        ////
        //L.marker([51.941196,4.512291], {icon: redMarker}).addTo(map);
        //
        var map =L.map('map').setView([31.1471, 75.3412], 8);

        L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
            attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
        }).addTo(map);



        L.marker([31.23911142,76.49470665], {icon: blueMarker}).addTo(map).bindPopup('Anandpur Sahib SDH').openPopup();
        L.marker([31.55747995,75.26604862], {icon: blueMarker}).addTo(map).bindPopup('Baba Bakala SDH').openPopup();
        L.marker([30.0667157,74.65523599], {icon: blueMarker}).addTo(map).bindPopup('Badal SDH').openPopup();

        L.marker([31.0759360441769,76.2937775969546], {icon: blueMarker}).addTo(map).bindPopup('Balachaur SDH').openPopup();
        L.marker([30.37550547,75.54520121], {icon: blueMarker}).addTo(map).bindPopup('Barnala DH').openPopup();
        L.marker([31.80871582,75.21504738], {icon: blueMarker}).addTo(map).bindPopup('Batala SDH').openPopup();
        L.marker([30.195269,74.948875], {icon: blueMarker}).addTo(map).bindPopup('Bathinda DH').openPopup();
        L.marker([31.80801415,75.66609161], {icon: blueMarker}).addTo(map).bindPopup('Dasuya SDH').openPopup();
        L.marker([30.58493912,76.84537522], {icon: blueMarker}).addTo(map).bindPopup('Dera Bassi SDH').openPopup();
        L.marker([30.36701341,75.86064861], {icon: blueMarker}).addTo(map).bindPopup('Dhuri SDH').openPopup();
        L.marker([30.67916,74.76056], {icon: blueMarker}).addTo(map).bindPopup('Faridkot DH').openPopup();
        L.marker([30.64654063,76.39065742], {icon: blueMarker}).addTo(map).bindPopup('Fatehgarh Sahib DH').openPopup();
        L.marker([30.40692,74.02465], {icon: blueMarker}).addTo(map).bindPopup('Fazilka DH').openPopup();
        L.marker([30.95201,74.60811], {icon: blueMarker}).addTo(map).bindPopup('Ferozpur DH').openPopup();
        L.marker([31.2133338,76.15022645], {icon: blueMarker}).addTo(map).bindPopup('Garhshankar SDH').openPopup();
        L.marker([30.21606572,74.64781269], {icon: blueMarker}).addTo(map).bindPopup('Gidderbaha SDH').openPopup();
        L.marker([32.04073,75.40306], {icon: blueMarker}).addTo(map).bindPopup('Gurdaspur DH').openPopup();
        L.marker([31.52922237,75.89993167], {icon: blueMarker}).addTo(map).bindPopup('Hoshiarpur DH').openPopup();
        L.marker([30.81121602,75.477995], {icon: blueMarker}).addTo(map).bindPopup('Jagraon SDH').openPopup();
        L.marker([31.3262145,75.57351424], {icon: blueMarker}).addTo(map).bindPopup('Jalandhar DH').openPopup();
        L.marker([31.37516702,75.38252558], {icon: blueMarker}).addTo(map).bindPopup('Kapurthala DH').openPopup();
        L.marker([31.42239039,75.10375804], {icon: blueMarker}).addTo(map).bindPopup('Khadoor Sahib SDH').openPopup();
        L.marker([30.70351313,76.22276782], {icon: blueMarker}).addTo(map).bindPopup('Khanna SDH').openPopup();
        L.marker([30.75275677,76.63298919], {icon: blueMarker}).addTo(map).bindPopup('Kharar SDH').openPopup();
        L.marker([30.584712,74.819477], {icon: blueMarker}).addTo(map).bindPopup('Kot Kapura SDH').openPopup();
        L.marker([30.90672481,75.86081254], {icon: blueMarker}).addTo(map).bindPopup('Ludhiana DH').openPopup();
        L.marker([30.51947437,75.87631838], {icon: blueMarker}).addTo(map).bindPopup('Malerkotla SDH').openPopup();
        L.marker([30.18659814,74.4952944], {icon: blueMarker}).addTo(map).bindPopup('Malout SDH').openPopup();
        L.marker([30.66815731,76.29482029], {icon: blueMarker}).addTo(map).bindPopup('Mandi Gobindgarh SDH').openPopup();
        L.marker([29.9854766666666,75.4028283333333], {icon: blueMarker}).addTo(map).bindPopup('Mansa DH').openPopup();
        L.marker([30.8115504023731,75.1687670480491], {icon: blueMarker}).addTo(map).bindPopup('Moga DH').openPopup();
        L.marker([31.95013172,75.61338068], {icon: blueMarker}).addTo(map).bindPopup('Mukerian SDH').openPopup();
        L.marker([31.21798862,75.19219881], {icon: blueMarker}).addTo(map).bindPopup('Sultanpur Lodhi SDH').openPopup();
        L.marker([30.1379447,75.80376698], {icon: blueMarker}).addTo(map).bindPopup('Sunam SDH').openPopup();
        L.marker([29.98855225,75.08340907], {icon: blueMarker}).addTo(map).bindPopup('Talwandi Sabo SDH').openPopup();
        L.marker([30.30710418,75.37256897], {icon: blueMarker}).addTo(map).bindPopup('Tappa SDH').openPopup();
        L.marker([31.45811,74.92324], {icon: blueMarker}).addTo(map).bindPopup('Tarn Taran DH').openPopup();
        L.marker([30.9689826,74.99135592], {icon: blueMarker}).addTo(map).bindPopup('Zira SDH').openPopup();
        L.marker([29.9252016666666,75.5579733333333], {icon: blueMarker}).addTo(map).bindPopup('Bhudlada SDH').openPopup();
        L.marker([30.12685385,74.79407916], {icon: blueMarker}).addTo(map).bindPopup('Ghudda SDH').openPopup();
        L.marker([29.82178179,75.87980863], {icon: blueMarker}).addTo(map).bindPopup('Moonak SDH').openPopup();
        L.marker([30.46327,74.53723], {icon: blueMarker}).addTo(map).bindPopup('Muktsar DH').openPopup();
        L.marker([30.37233541,76.14592193], {icon: blueMarker}).addTo(map).bindPopup('Nabha SDH').openPopup();
        L.marker([31.12822406,75.47563157], {icon: blueMarker}).addTo(map).bindPopup('Nakodar SDH').openPopup();
        L.marker([32.28217178,75.65333879], {icon: blueMarker}).addTo(map).bindPopup('Pathankot DH').openPopup();
        L.marker([30.33780603,76.39953685], {icon: blueMarker}).addTo(map).bindPopup('Patiala DH').openPopup();
        L.marker([31.27465805,74.85364882], {icon: blueMarker}).addTo(map).bindPopup('Patti SDH').openPopup();
        L.marker([31.230178,75.76264578], {icon: blueMarker}).addTo(map).bindPopup('Phagwara SDH').openPopup();
        L.marker([31.02171817,75.79010622], {icon: blueMarker}).addTo(map).bindPopup('Phillaur SDH').openPopup();
        L.marker([30.64373869,75.58089011], {icon: blueMarker}).addTo(map).bindPopup('Raikot SDH').openPopup();
        L.marker([30.47829002,76.58439128], {icon: blueMarker}).addTo(map).bindPopup('Rajpura SDH').openPopup();
        L.marker([30.32280885,75.24355099], {icon: blueMarker}).addTo(map).bindPopup('Rampura Phul SDH').openPopup();
        L.marker([30.96655,76.52503], {icon: blueMarker}).addTo(map).bindPopup('Ropar DH').openPopup();
        L.marker([30.15946692,76.20282606], {icon: blueMarker}).addTo(map).bindPopup('Samana SDH').openPopup();
        L.marker([30.83910716,76.18747948], {icon: blueMarker}).addTo(map).bindPopup('Samrala SDH').openPopup();
        L.marker([30.25216211,75.83589949], {icon: blueMarker}).addTo(map).bindPopup('Sangrur DH').openPopup();
        L.marker([29.6910283333333,75.2380716666666], {icon: blueMarker}).addTo(map).bindPopup('Sardulgarh SDH').openPopup();
        L.marker([31.5391409,75.50770497], {icon: blueMarker}).addTo(map).bindPopup('Bhulath SDH').openPopup();
        L.marker([30.73882964,76.71369736], {icon: blueMarker}).addTo(map).bindPopup('Mohali DH').openPopup();
        L.marker([31.11652967,76.15569411], {icon: blueMarker}).addTo(map).bindPopup('Nawanshahr DH').openPopup();

        //
        L.marker([30.90926,75.86416], {icon: redMarker}).addTo(map).bindPopup('Christian medical college').openPopup();
        L.marker([30.69541,76.73423], {icon: redMarker}).addTo(map).bindPopup('Fortis Mohali').openPopup();
        L.marker([30.58577,76.84173], {icon: redMarker}).addTo(map).bindPopup('SRI RAM HOSPITAL').openPopup();
        L.marker([30.7406,76.67444], {icon: redMarker}).addTo(map).bindPopup('Kamboj Multispeciality hospital').openPopup();
        L.marker([30.6970005,75.7683411], {icon: redMarker}).addTo(map).bindPopup('Ranipur PHC').openPopup();
        L.marker([30.97322,76.52601], {icon: redMarker}).addTo(map).bindPopup('Sangha Hospital').openPopup();
        L.marker([30.9625,76.51973], {icon: redMarker}).addTo(map).bindPopup('Max City Hospital').openPopup();
        L.marker([30.76232,76.63054], {icon: redMarker}).addTo(map).bindPopup('Rajeev Eye hospital').openPopup();
        L.marker([30.21728,74.94581], {icon: redMarker}).addTo(map).bindPopup('Chhabra Hospital').openPopup();
        L.marker([30.8365993,75.4187164], {icon: redMarker}).addTo(map).bindPopup('Care & Cure Medicity Hospital').openPopup();
        L.marker([30.63024,76.81883], {icon: redMarker}).addTo(map).bindPopup('Chaudhary Hospital').openPopup();
        L.marker([31.65915,74.8725], {icon: redMarker}).addTo(map).bindPopup('Preet Hospital').openPopup();
        L.marker([31.61746,74.8621], {icon: redMarker}).addTo(map).bindPopup('Choudhary ENT & Maternity Hospital').openPopup();
        L.marker([31.6339,74.8555499], {icon: redMarker}).addTo(map).bindPopup('Lifeline Hospital').openPopup();
        L.marker([30.47754,76.58428], {icon: redMarker}).addTo(map).bindPopup('Baweja Multispeciality Hospital').openPopup();
        L.marker([30.709,76.80246], {icon: redMarker}).addTo(map).bindPopup('Eden Critical Care hospital Ltd').openPopup();
        L.marker([30.82948,75.16471], {icon: redMarker}).addTo(map).bindPopup('Garg Hospital').openPopup();
        L.marker([30.67542,76.22782], {icon: redMarker}).addTo(map).bindPopup('Rani Eye hospital').openPopup();
        L.marker([30.33193,76.37918], {icon: redMarker}).addTo(map).bindPopup('Prime Multispeciality Hospital (A Unit Of Hope Health Ways)').openPopup();
        L.marker([30.34231,76.45401], {icon: redMarker}).addTo(map).bindPopup('Rama Atray Memorial eye Hospital').openPopup();
        L.marker([30.34867,76.39142], {icon: redMarker}).addTo(map).bindPopup('Garg Eye Hospital').openPopup();
        L.marker([30.14313,74.2122], {icon: redMarker}).addTo(map).bindPopup('Loona Nursing Home').openPopup();
        L.marker([30.37965,76.36246], {icon: redMarker}).addTo(map).bindPopup('Randhawa Eye Hospital& Lasik Centre').openPopup();
        L.marker([30.47562,76.58569], {icon: redMarker}).addTo(map).bindPopup('Neelam Hospital').openPopup();
        L.marker([31.64268,74.89598], {icon: redMarker}).addTo(map).bindPopup('Apex Hospital').openPopup();
        L.marker([30.22191,74.95694], {icon: redMarker}).addTo(map).bindPopup('Badyal Multispeciality hospital & Trauma Centre').openPopup();
        L.marker([30.74119,76.74019], {icon: redMarker}).addTo(map).bindPopup('SANTOKH HOSPITAL').openPopup();
        L.marker([31.63285,74.84357], {icon: redMarker}).addTo(map).bindPopup('Gumber Eye & Dental Care Centre').openPopup();
        L.marker([31.64287,74.8962899], {icon: redMarker}).addTo(map).bindPopup('The corporate hospital').openPopup();
        L.marker([30.35258,76.374], {icon: redMarker}).addTo(map).bindPopup('A.P.Healthcare & Trauma Centre').openPopup();
        L.marker([31.33428,75.56743], {icon: redMarker}).addTo(map).bindPopup('Shakuntala devi. VIG Hospital').openPopup();
        L.marker([31.82191,75.19866], {icon: redMarker}).addTo(map).bindPopup('Satsar Hospital').openPopup();
        L.marker([31.33443,75.56227], {icon: redMarker}).addTo(map).bindPopup('Makkar Hospital').openPopup();
        L.marker([30.8877,75.80504], {icon: redMarker}).addTo(map).bindPopup('N.K. Aggarwal Joints & Spine Centre').openPopup();
        L.marker([30.68029,76.7382499], {icon: redMarker}).addTo(map).bindPopup('I Med Hospital').openPopup();
        L.marker([30.4777,74.50962], {icon: redMarker}).addTo(map).bindPopup('New Delhi Hospital & Nursing Home').openPopup();
        L.marker([31.23289,75.7862899], {icon: redMarker}).addTo(map).bindPopup('Mitra Eye Hospital').openPopup();
        L.marker([30.20886,74.94574], {icon: redMarker}).addTo(map).bindPopup('Aman Hospital').openPopup();
        L.marker([30.77698,76.55861], {icon: redMarker}).addTo(map).bindPopup('Healthsure Multispeciality Hospital').openPopup();
        L.marker([31.35661,76.3833], {icon: redMarker}).addTo(map).bindPopup('Shri Krishna Netralaya and Dantalaya').openPopup();
        L.marker([31.62106,74.9021999], {icon: redMarker}).addTo(map).bindPopup('Gupta Hospital').openPopup();
        L.marker([30.8365993,75.4187164], {icon: redMarker}).addTo(map).bindPopup('Rajpal Hospital').openPopup();
        L.marker([31.64395,74.80026], {icon: redMarker}).addTo(map).bindPopup('Jai Kamal Eye Hospital').openPopup();
        L.marker([30.21231,74.94462], {icon: redMarker}).addTo(map).bindPopup('Nagpal Superspeciality Hospital').openPopup();
        L.marker([30.47272,74.5193], {icon: redMarker}).addTo(map).bindPopup('Dr. Arpanas U/S Maternity Centre').openPopup();
        L.marker([30.35258,76.374], {icon: redMarker}).addTo(map).bindPopup('Aggarwal Health Care & Eye Hospital').openPopup();
        L.marker([30.33493,76.42367], {icon: redMarker}).addTo(map).bindPopup('Sodhi Eye Hospital').openPopup();
        L.marker([31.51631,75.87824], {icon: redMarker}).addTo(map).bindPopup('Nova Hospital').openPopup();
        L.marker([30.3789,75.54185], {icon: redMarker}).addTo(map).bindPopup('Lifeline Multispeciality Hospital').openPopup();
        L.marker([31.30991,75.57359], {icon: redMarker}).addTo(map).bindPopup('Thind Eye Hospital').openPopup();
        L.marker([30.81682,75.1676899], {icon: redMarker}).addTo(map).bindPopup('Dr. Satya Pal Mittal Hospital').openPopup();
        L.marker([30.33148,76.41767], {icon: redMarker}).addTo(map).bindPopup('Guru nanak hospital.').openPopup();
        L.marker([30.26362,76.0377], {icon: redMarker}).addTo(map).bindPopup('Ravi Eye Hospital and Lasik Laser Centre').openPopup();
        L.marker([30.73746,76.7757], {icon: redMarker}).addTo(map).bindPopup('Laser Eye Clinic').openPopup();
        L.marker([30.48278,74.54282], {icon: redMarker}).addTo(map).bindPopup('Platinum Nursing Home').openPopup();
        L.marker([30.22213,74.94327], {icon: redMarker}).addTo(map).bindPopup('Gem Hospital').openPopup();
        L.marker([30.36972,75.54191], {icon: redMarker}).addTo(map).bindPopup('Dr.Naresh Hospital Heart Center').openPopup();
        L.marker([30.22213,74.94327], {icon: redMarker}).addTo(map).bindPopup('Major Gupta Bone & Joint Hospital').openPopup();
        L.marker([30.5232,75.87734], {icon: redMarker}).addTo(map).bindPopup('Ekjyot Eye hospital.').openPopup();
        L.marker([30.20871,74.96707], {icon: redMarker}).addTo(map).bindPopup('Deol Bone and Joint Hospital').openPopup();
        L.marker([31.22006,75.78059], {icon: redMarker}).addTo(map).bindPopup('Ekjyot shrama eye hospital').openPopup();
        L.marker([30.36596,75.53904], {icon: redMarker}).addTo(map).bindPopup('sood nursing home').openPopup();
        L.marker([31.70114,74.82171], {icon: redMarker}).addTo(map).bindPopup('Ivy Hospital (Ivy Health & Life Science Pvt Ltd) Amritsar').openPopup();
        L.marker([30.33047,76.38458], {icon: redMarker}).addTo(map).bindPopup('Saronwala Hospital').openPopup();
        L.marker([31.34613,75.56153], {icon: redMarker}).addTo(map).bindPopup('Oxford hospital Pvt. Ltd').openPopup();
        L.marker([31.68149,74.83413], {icon: redMarker}).addTo(map).bindPopup('Surjit Hospital').openPopup();
        L.marker([30.9259,75.84564], {icon: redMarker}).addTo(map).bindPopup('Dr. Shamsher Singh Health Care').openPopup();
        L.marker([30.45306,74.53483], {icon: redMarker}).addTo(map).bindPopup('Kavita Maternity Hospital').openPopup();
        L.marker([31.33443,75.56227], {icon: redMarker}).addTo(map).bindPopup('Joshi hospital multi superspeciality hospital').openPopup();
        L.marker([30.22213,74.94327], {icon: redMarker}).addTo(map).bindPopup('Pardeep Memorial Goyal Multispeciality Hospital').openPopup();
        L.marker([30.81792,75.19998], {icon: redMarker}).addTo(map).bindPopup('Gill Eye hospital').openPopup();
        L.marker([30.47272,74.5193], {icon: redMarker}).addTo(map).bindPopup('Gawri Nursing Home').openPopup();
        L.marker([31.33428,75.56743], {icon: redMarker}).addTo(map).bindPopup('Karan Hospital').openPopup();
        L.marker([32.02774,75.39031], {icon: redMarker}).addTo(map).bindPopup('Abrol Medical Centre').openPopup();
        L.marker([30.62618,76.39361], {icon: redMarker}).addTo(map).bindPopup('Rana Nursing Home').openPopup();
        L.marker([30.33699,76.38368], {icon: redMarker}).addTo(map).bindPopup('Preet Surgical Centre & Maternity Hospital').openPopup();

    }

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

    $("#drop1").change(function () {
        $('#drophospital').prop('selectedIndex',0);
        orgid = $(this).find("option:selected").val();
        var organisationUnits;

        $.getJSON("../../api/organisationUnits/"+orgid+"?paging=false&fields=children[id,name]", function (data) {
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
                $('#drophospital').append($('<option></option>').val(item1.id).html(item1.name).text(item1.name)

                );
            });
            $('#drophospital').selectpicker('refresh');
        });
        //generatefilterrecord(orgid,defservice,defowner,defhealthfacility);
        //jQuery('#sel').html('');
    });

    $("#drophospital").change(function () {
        hospital = $(this).find("option:selected").val();
        orgid=hospital;
        //generatefilterrecord(orgid,defservice,defowner,defhealthfacility);
        //jQuery('#sel').html('');
    });

    $("#drop_ownership").change(function () {
        defowner="XEiMcaGi6vv";
        defowner = defowner+":IN:"+$(this).find("option:selected").text();

    });

    $("#drop2").change(function () {
        //jQuery('#sel').html('');
        defservice=$(this).find("option:selected").val()+":IN:1";

        //generatefilterrecord(orgid,defservice,defowner,defhealthfacility);


    })
    $("#droptype").change(function () {
        defhealthfacility=$(this).find("option:selected").val()+":IN:1";
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

    $("#content1").hide();
    $("#map1").show();

    $("#mapA1").hide();
    $("#footer").hide();
    jQuery('#sel').html('');
    $(".w3-card-4").remove();


    var addressjoin=[],ouid=[],availspecialitiesjoin,availspecialities=[],avaialabilityjoin,name=[],address=[],pincode=[],village=[],mobile=[],special=[],notspecial=[],hfacilities=[],nothfacilities=[],schemes=[],notschemes=[],contactpname=[],contactpnumber=[];
    var toAdd,spec=[],email=[],specialjoin,notspecialjoin,notspec=[],owner=[],notowner=[],hfacilitiesjoin,nothfacilitiesjoin,schemesjoin,notschemesjoin,hfschemes=[],nothfschemes=[];
    var healthfac="",ownership=[],availspecialiti=[];
    var arrayMap = [];latitude=[];longitude=[];


    $.getJSON("../../api/analytics/events/query/tzR46QRZ6FJ.json?stage=o6ps51YxGNb&dimension=pe:THIS_YEAR&dimension=ou:"+orgid+"&dimension=l8VDWUvIHmv&dimension=KOhqEw0UKxA&dimension=xjJR4dTmn4p&dimension=wcmHow1kcBi&dimension=pqVIj8NyTXb&dimension=g7vyRbNim1K&dimension=Gx4VSNet1dC&dimension=bUg8a8bAvJs&dimension="+defservice+"&dimension="+defavail+"&dimension="+defowner+"&dimension="+defhealthfacility+"&dimension=ZUbPsfW6y0C&dimension=CAOM6riDtfU&dimension=YL7OJoQCAmF&dimension=vJO1Jac84Ar&dimension=kF8ZJYe9SJZ&dimension=tNhLX6c7KHp&dimension=bVENUe0eDsO&displayProperty=NAME", function (data) {
        console.log("../../api/analytics/events/query/tzR46QRZ6FJ.json?stage=o6ps51YxGNb&dimension=pe:THIS_YEAR&dimension=ou:"+orgid+"&dimension=l8VDWUvIHmv&dimension=KOhqEw0UKxA&dimension=xjJR4dTmn4p&dimension=wcmHow1kcBi&dimension=pqVIj8NyTXb&dimension=g7vyRbNim1K&dimension=Gx4VSNet1dC&dimension=bUg8a8bAvJs&dimension="+defservice+"&dimension="+defowner+"&dimension="+defhealthfacility+"&dimension=jXCd8k2841l&dimension=RkP5neDLbHv&dimension=avHST8wLPnX&dimension=txl9e6UJFP4&dimension=ZUbPsfW6y0C&dimension=CAOM6riDtfU&dimension=YL7OJoQCAmF&dimension=vJO1Jac84Ar&dimension=kF8ZJYe9SJZ&dimension=tNhLX6c7KHp&dimension=bVENUe0eDsO&displayProperty=NAME");
        var constants={key:name, value: value}

        analyticsMap = calculateIndex(data.headers,analyticsMap);

        if(data.rows.length==0)
        {
            alert("No result found for above selection");
        }
        for(var k=0;k<data.rows.length;k++){

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
                    if (value == "1"){
                        value = data.headers[analyticsMap[j].index].column;
                    }

                    if (!value || value == "0"){
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

        for (var i = 0; i < name.length; i++) {
            if(ownership[i]=="Public")
            {
                L.marker([longitude[i], latitude[i]], {icon: blueMarker}).addTo(map1).bindPopup(name[i]+","+"</br><strong>Contact:</strong> "+  mobile[i]+ ",</br> <strong>Schemes:</strong>"+hfschemes[i]+", </br><strong>Availabilities:</strong> "+availspecialiti[i]).openPopup();
            }
            else if(ownership[i]=="Private")
            {
                L.marker([longitude[i], latitude[i]], {icon: redMarker}).addTo(map1).bindPopup(name[i]).openPopup();
            }


        }

    });

}

function generatefilterrecordlist(orgid,defservice,defavail,defowner,defhealthfacility) {
    $("#content1").hide();
    $("#map1").hide();

    $("#mapA1").hide();
    $("#footer").hide();



    jQuery('#sel').html('');
    $(".w3-card-4").remove();

    var addressjoin=[],ouid=[],availspecialitiesjoin,availspecialities=[],avaialabilityjoin,name=[],address=[],pincode=[],village=[],mobile=[],special=[],notspecial=[],hfacilities=[],nothfacilities=[],schemes=[],notschemes=[],contactpname=[],contactpnumber=[];
    var toAdd,spec=[],email=[],specialjoin,notspecialjoin,notspec=[],owner=[],notowner=[],hfacilitiesjoin,nothfacilitiesjoin,schemesjoin,notschemesjoin,hfschemes=[],nothfschemes=[];
    var healthfac="",ownership=[],availspecialiti=[];
    var arrayMap = [];latitude=[];longitude=[];


    $.getJSON("../../api/analytics/events/query/tzR46QRZ6FJ.json?stage=o6ps51YxGNb&dimension=pe:THIS_YEAR&dimension=ou:"+orgid+"&dimension=l8VDWUvIHmv&dimension=KOhqEw0UKxA&dimension=xjJR4dTmn4p&dimension=wcmHow1kcBi&dimension=pqVIj8NyTXb&dimension=g7vyRbNim1K&dimension=Gx4VSNet1dC&dimension=bUg8a8bAvJs&dimension="+defservice+"&dimension="+defavail+"&dimension="+defowner+"&dimension="+defhealthfacility+"&dimension=ZUbPsfW6y0C&dimension=CAOM6riDtfU&dimension=YL7OJoQCAmF&dimension=vJO1Jac84Ar&dimension=kF8ZJYe9SJZ&dimension=tNhLX6c7KHp&dimension=bVENUe0eDsO&displayProperty=NAME", function (data) {
        console.log("../../api/analytics/events/query/tzR46QRZ6FJ.json?stage=o6ps51YxGNb&dimension=pe:THIS_YEAR&dimension=ou:"+orgid+"&dimension=l8VDWUvIHmv&dimension=KOhqEw0UKxA&dimension=xjJR4dTmn4p&dimension=wcmHow1kcBi&dimension=pqVIj8NyTXb&dimension=g7vyRbNim1K&dimension=Gx4VSNet1dC&dimension=bUg8a8bAvJs&dimension="+defservice+"&dimension="+defowner+"&dimension="+defhealthfacility+"&dimension=jXCd8k2841l&dimension=RkP5neDLbHv&dimension=avHST8wLPnX&dimension=txl9e6UJFP4&dimension=ZUbPsfW6y0C&dimension=CAOM6riDtfU&dimension=YL7OJoQCAmF&dimension=vJO1Jac84Ar&dimension=kF8ZJYe9SJZ&dimension=tNhLX6c7KHp&dimension=bVENUe0eDsO&displayProperty=NAME");
        var constants={key:name, value: value}

        analyticsMap = calculateIndex(data.headers,analyticsMap);

        if(data.rows.length==0)
        {
            alert("No result found for above selection");
        }
        for(var k=0;k<data.rows.length;k++){

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
                    if (value == "1"){
                        value = data.headers[analyticsMap[j].index].column;
                    }

                    if (!value || value == "0"){
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

        for (var i = 0; i < name.length; i++) {
            obj = new constructor_obj(document.body, name[i], addressjoin[i], pincode[i], mobile[i], spec[i], owner[i],availspecialiti[i], contactpname[i], contactpnumber[i], email[i], hfschemes[i], nothfschemes[i], ouid[i],ownership[i]);
        }
    });

}



function constructor_obj(parent, title, address,pincode,mobile,spec,owner,avaialability,contactpname,contactpnumber, email,hfschemes,nothfschemes,ouid,ownership) {
    var procedures=[],costs=[];
    this.parent = parent;
    this.title = title;
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

    $.getJSON("../../api/analytics/events/query/tzR46QRZ6FJ.json?stage=o6ps51YxGNb&dimension=pe:THIS_YEAR&dimension=ou:"+ouid+"&dimension=eH2F2xuLmoY&dimension=kfDoQ3V1RQK&dimension=csjh8jewk7x&dimension=WXnr5Qk8Qgo&dimension=UwGx1EmJyIf&dimension=rzNUVjOm5ZJ&dimension=tpm9TIh7IeQ&dimension=pUYrErSv4Kf&dimension=hq5P29o8auc&dimension=KBgdegZWYDc&dimension=Mpo7Zm6z9WL&dimension=xtGxtg2I1SH&dimension=EhBs6eq4ebt&dimension=ThPPuHVPTsZ&dimension=hK82b1FNhui&dimension=aLY8DrZ88WN&dimension=XfstNyl31ca&dimension=fOLmSsSSlVI&dimension=aLnsQJ6De8p&dimension=LFDq2RYMt88&displayProperty=NAME", function (cdata) {

        console.log("ouid:"+ouid);
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

}


function myfunc(ouid){
//      var div = $("#"+thiz)[0];
//              div.className="";
    $("#"+ouid).slideToggle(1000);
}
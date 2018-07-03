// var arr=[];
// var peid,orgid,orgname;
//
//
// function showtable(arr,peid,orgid,orgname){
//   console.log("values: "+ peid+orgid+orgname);
//   console.log("values arr "+ arr);
//
//   DHIS.getTable({
//     "columns": [
//       {
//         "dimension": "dx",
//         "items": arr
//       }
//     ],
//     "rows": [
//       {
//         "dimension": "pe",
//         "items": [
//           {
//             "id": peid
//           }
//         ]
//       },
//       {
//         "dimension": "ou",
//         "items": [
//           {
//             "id": orgid,
//             "name": orgname
//           }
//         ]
//       }
//
//     ],
//     "filters": null,
//     "showColTotals": false,
//     "showRowTotals": false,
//     "showColSubTotals": false,
//     "showRowSubTotals": false,
//     "url": "../..",
//     "el": "table1"
//   });
// }

var arr=[];
var peid,orgid,orgname;


function showtable(arr,peid,orgid,orgname){
var r2 = {
el: "table1",
columns: [
{
"dimension": "dx",
"items": arr
}
],
rows: [
{
"dimension": "pe",
"items": [
{
"id": peid
}
]
},
{
"dimension": "ou",
"items": [
{
"id": orgid,
"name": orgname
}
]
}

],
filters: [
null
],

// All following properties are optional
title: "My custom title"
};
reportTablePlugin.load([r2]);
}

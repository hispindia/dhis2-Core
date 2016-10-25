var arr=[];
var peid,orgid,orgname;


function showtable(arr,peid,orgid,orgname){
console.log("values: "+ peid+orgid+orgname);
console.log("values arr "+ arr);

DHIS.getTable({
  "columns": [
    {
      "dimension": "dx",
      "items": arr
    }
  ],
  "rows": [
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
  "filters": null,
  "url": "http://hospdev.hispindia.org/dhis_searo",
  "el": "table1"
});
}
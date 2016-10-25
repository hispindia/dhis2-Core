var arr=[];
var peid,orgid,orgname;


function showchart(arr,peid,orgid,orgname){
console.log("values: "+ peid+orgid+orgname);
console.log("values arr "+ arr);

  DHIS.getChart({
  "type": "column",
  "columns": [
    {
      "dimension": "dx",
      "items": arr
    }
  ],
  "rows": [
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
  "filters": [
    {
      "dimension": "pe",
       "items": [
                        {
                            "id": peid
                        }
                    ]
    }
  ],
  "sortOrder": 0,
  "url": "../..",
  "el": "chart1"
});
}
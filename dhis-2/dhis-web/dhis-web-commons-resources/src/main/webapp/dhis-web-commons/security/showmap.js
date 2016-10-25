
var deid,peid,orgid,orgname;


function showmap(deid,peid,orgid,orgname){


  DHIS.getMap({
    "mapViews": [
      {
        "columns": [
          {
            "dimension": "dx",
            "items": [
              {
                "id": "VG82v0dFGeP"
              }
            ]
          }
        ],
        "rows": [
          {
            "dimension": "ou",
            "items": [
              {
                "id": "GY9dLYENyOU"
              }
            ]
          }
        ],
        "filters": [
          {
            "dimension": "pe",
            "items": [
              {
                "id": "2012"
              }
            ]
          }
        ],
        "layer": "thematic1",
        "method": 3,
        "hidden": false
      }
    ],
    "url": "../..",
    "el": "map"
  });

}
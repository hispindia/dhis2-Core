window.Loader = (function () {
    selected=[];



    increment=function(ths,value){


        if(selected[0]!=value && selected.length!=0)
        {
            selected=[];
            head=document.getElementById("heading").innerHTML="";
            head1=document.getElementById("heading").innerHTML=value+"%";

        }else{
            this.heading=document.createElement('h3');
            this.heading.id="heading";
            this.heading.innerHTML=value+"%";
            ths.innerDiv.append(this.heading);
        }

        selected.push(value);

    }

    var Loader = {
        showLoader: function () {
            this.iDiv = document.createElement('div');
            this.iDiv.id =this.selector='loader-wrapper';
            this.iDiv.className =this.selector_load='loader-wrapper-load';
            document.getElementsByTagName('body')[0].appendChild(this.iDiv);

            this.innerDiv = document.createElement('div');
            this.innerDiv.id = 'loader';

            this.iDiv.appendChild(this.innerDiv);
            document.getElementById(this.selector).style.visibility = 'visible';



        },
        hideLoader: function () {
            var load=document.getElementsByClassName (this.selector_load);
            // load.style.visibility = 'hidden';
            for (var k = 0; k < load.length; k++) {
                load[k].style.visibility = 'hidden';
            }
        },
        increment:function(val){

            return new increment(this,val);


        }

    };


    return Loader;
}());
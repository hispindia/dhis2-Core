var Imtech3 = {};
Imtech3.Pager3 = function() {
    this.paragraphsPerPage = 5;
    this.currentPage = 1;
    this.pagingControlsContainer = "#pagingControls3";
    this.pagingContainerPath = "#alternatecolor4";
    
    this.numPages = function() {
        var numPages3 = 0;
        if (this.paragraphs != null && this.paragraphsPerPage != null) {
            numPages3 = Math.ceil(this.paragraphs.length / this.paragraphsPerPage);
        }
       
        return numPages3;
    };
    
    this.showPage = function(page) {
        this.currentPage = page;
        
        var html3 = "";
        for (var i = (page-1)*this.paragraphsPerPage; i < ((page-1)*this.paragraphsPerPage) + this.paragraphsPerPage; i++) {
            if (i < this.paragraphs.length) {
                var elem = this.paragraphs.get(i);
                html3 += "<" + elem.tagName + ">" + elem.innerHTML + "</" + elem.tagName + ">";
            }
        }
        
        $(this.pagingContainerPath).html(html3);
        altRows('alternatecolor4');
        renderControls(this.pagingControlsContainer, this.currentPage, this.numPages());
        
    }
    
    var renderControls = function(container, currentPage, numPages) {
        var pagingControls3 = "<table class='altrowstable'><tr>";
        for (var i = 1; i <= numPages; i++) {
            if (i != currentPage) {
                pagingControls3 += "<th><a href='#' onclick='pager2.showPage(" + i + "); return false;'>" + i + "</a></th>";
            } else {
                pagingControls3 += "<th style='background-color:#E0ECF8;'>" + i + "</th>";
            }
        }
        
        pagingControls3 += "</tr></table>";
        
        $(container).html(pagingControls3);
    }    
}
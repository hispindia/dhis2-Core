var Imtech2 = {};
Imtech2.Pager2 = function() {
    this.paragraphsPerPage = 5;
    this.currentPage = 1;
    this.pagingControlsContainer = "#pagingControls2";
    this.pagingContainerPath = "#alternatecolor3";
    
    this.numPages = function() {
        var numPages2 = 0;
        if (this.paragraphs != null && this.paragraphsPerPage != null) {
            numPages2 = Math.ceil(this.paragraphs.length / this.paragraphsPerPage);
        }
       
        return numPages2;
    };
    
    this.showPage = function(page) {
        this.currentPage = page;
        
        var html2 = "";
        for (var i = (page-1)*this.paragraphsPerPage; i < ((page-1)*this.paragraphsPerPage) + this.paragraphsPerPage; i++) {
            if (i < this.paragraphs.length) {
                var elem = this.paragraphs.get(i);
                html2 += "<" + elem.tagName + ">" + elem.innerHTML + "</" + elem.tagName + ">";
            }
        }
        
        $(this.pagingContainerPath).html(html2);
        altRows('alternatecolor3');
        renderControls(this.pagingControlsContainer, this.currentPage, this.numPages());
        
    }
    
    var renderControls = function(container, currentPage, numPages) {
        var pagingControls2 = "<table class='altrowstable'><tr>";
        for (var i = 1; i <= numPages; i++) {
            if (i != currentPage) {
                pagingControls2 += "<th><a href='#' onclick='pager1.showPage(" + i + "); return false;'>" + i + "</a></th>";
            } else {
                pagingControls2 += "<th style='background-color:#E0ECF8;'>" + i + "</th>";
            }
        }
        
        pagingControls2 += "</tr></table>";
        
        $(container).html(pagingControls2);
    }    
}
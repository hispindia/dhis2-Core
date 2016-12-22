package org.hisp.dhis.ivb.util;

import java.util.List;

public class CSVImportSatus
{
    Integer facilityCount = 0;

    Integer importFacilityCount = 0;
    
    Integer updateCount = 0;
    
    Integer insertCount = 0;
    
    Integer missingCount = 0;
    
    Integer totalCount = 0;
    
    String missingFacilities = "";
    
    List<String> statusMsg;
    
    public CSVImportSatus()
    {
        
    }

    public Integer getFacilityCount()
    {
        return facilityCount;
    }

    public void setFacilityCount( Integer facilityCount )
    {
        this.facilityCount = facilityCount;
    }

    public Integer getImportFacilityCount()
    {
        return importFacilityCount;
    }

    public void setImportFacilityCount( Integer importFacilityCount )
    {
        this.importFacilityCount = importFacilityCount;
    }

    public Integer getUpdateCount()
    {
        return updateCount;
    }

    public void setUpdateCount( Integer updateCount )
    {
        this.updateCount = updateCount;
    }

    public Integer getInsertCount()
    {
        return insertCount;
    }

    public void setInsertCount( Integer insertCount )
    {
        this.insertCount = insertCount;
    }

    public String getMissingFacilities()
    {
        return missingFacilities;
    }

    public void setMissingFacilities( String missingFacilities )
    {
        this.missingFacilities = missingFacilities;
    }

    public List<String> getStatusMsg()
    {
        return statusMsg;
    }

    public void setStatusMsg( List<String> statusMsg )
    {
        this.statusMsg = statusMsg;
    }

    public Integer getMissingCount()
    {
        return missingCount;
    }

    public void setMissingCount( Integer missingCount )
    {
        this.missingCount = missingCount;
    }

    public Integer getTotalCount()
    {
        return totalCount;
    }

    public void setTotalCount( Integer totalCount )
    {
        this.totalCount = totalCount;
    }

    
}

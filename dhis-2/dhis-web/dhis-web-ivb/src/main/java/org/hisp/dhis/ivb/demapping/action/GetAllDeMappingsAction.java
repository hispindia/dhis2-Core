package org.hisp.dhis.ivb.demapping.action;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hisp.dhis.ivb.demapping.DeMapping;
import org.hisp.dhis.ivb.demapping.DeMappingService;
import org.hisp.dhis.paging.ActionPagingSupport;

public class GetAllDeMappingsAction
    extends ActionPagingSupport<DeMapping>
{

    // -------------------------------------------------------------------------
    // Dependency
    // -------------------------------------------------------------------------

    private DeMappingService demappingService;

    public void setDemappingService(DeMappingService demappingService) {
        this.demappingService = demappingService;
    }

    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------
    private List<DeMapping> demappings = new ArrayList<DeMapping>();

    public List<DeMapping> getDemappings() {
        return demappings;
    }

    public void setDemappings(List<DeMapping> demappings) {
        this.demappings = demappings;
    }

    private String key;

    public String getKey()
    {
        return key;
    }

    public void setKey( String key )
    {
        this.key = key;
    }

    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------

    @Override
    public String execute()
        throws Exception
    {
        demappings = new ArrayList<DeMapping>( demappingService.getAllDeMappings() );
/*
        if ( isNotBlank( key ) )
        {
            lookupService.searchLookupByName( lookups, key );
        }

        this.paging = createPaging( lookups.size() );
        lookups = getBlockElement( lookups, paging.getStartPos(), paging.getPageSize() );

        Collections.sort( lookups );
*/
        return SUCCESS;

    }
}

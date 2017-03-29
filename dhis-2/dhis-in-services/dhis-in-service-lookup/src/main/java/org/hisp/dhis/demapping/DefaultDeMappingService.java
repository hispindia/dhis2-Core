package org.hisp.dhis.demapping;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.hisp.dhis.ivb.demapping.DeMapping;
import org.hisp.dhis.ivb.demapping.DeMappingService;
import org.hisp.dhis.ivb.demapping.DeMappingStore;

public class DefaultDeMappingService
implements DeMappingService
{
// -------------------------------------------------------------------------
// Dependencies
// -------------------------------------------------------------------------

private DeMappingStore demappingStore;

public void setDemappingStore(DeMappingStore demappingStore) {
    this.demappingStore = demappingStore;
}

// -------------------------------------------------------------------------
// DeMapping
// -------------------------------------------------------------------------

    @Override
    public void addDeMapping( DeMapping demapping )
    {
        demappingStore.addDeMapping( demapping );
    }

    @Override
    public void updateDeMapping( DeMapping demapping )
    {
        demappingStore.updateDeMapping( demapping );
    }

    @Override
    public void deleteDeMapping( DeMapping demapping )
    {
        demappingStore.deleteDeMapping( demapping );
    }

    @Override
    public DeMapping getDeMapping( String deid )
    {
        return demappingStore.getDeMapping( deid );
    }

    @Override
    public Collection<DeMapping> getAllDeMappings()
    {
        return demappingStore.getAllDeMappings();
    }


}

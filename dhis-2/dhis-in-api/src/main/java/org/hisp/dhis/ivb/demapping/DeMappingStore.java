package org.hisp.dhis.ivb.demapping;

import java.util.Collection;

public interface DeMappingStore
{
    String ID = DeMappingStore.class.getName();
    
    // -------------------------------------------------------------------------
    // Lookup
    // -------------------------------------------------------------------------

    void addDeMapping( DeMapping demapping );

    void updateDeMapping( DeMapping demapping );

    void deleteDeMapping( DeMapping demapping );

    DeMapping getDeMapping( String deid );

    Collection<DeMapping> getAllDeMappings();

}

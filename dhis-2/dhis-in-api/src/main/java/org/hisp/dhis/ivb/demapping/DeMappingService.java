package org.hisp.dhis.ivb.demapping;

import java.util.Collection;
import java.util.List;

public interface DeMappingService
{
    String ID = DeMappingService.class.getName();

    // -------------------------------------------------------------------------
    // DeMapping
    // -------------------------------------------------------------------------

    void addDeMapping( DeMapping demapping );

    void updateDeMapping( DeMapping demapping );

    void deleteDeMapping( DeMapping demapping );

    DeMapping getDeMapping( String deid );

    Collection<DeMapping> getAllDeMappings();
}

package org.hisp.dhis.datavalue.comparator;

import java.util.Comparator;

import org.hisp.dhis.datavalue.DataValueAudit;

/**
 * @author BHARATH
 */
public class DataValueAuditLastUpdatedComparator implements Comparator<DataValueAudit>
{
    @Override
    public int compare( DataValueAudit dataValueAudit0, DataValueAudit dataValueAudit1 )
    {
        return dataValueAudit1.getTimestamp().compareTo( dataValueAudit0.getTimestamp() );
    }
}

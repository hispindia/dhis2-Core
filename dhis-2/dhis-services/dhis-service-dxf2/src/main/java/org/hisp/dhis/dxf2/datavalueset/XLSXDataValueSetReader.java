package org.hisp.dhis.dxf2.datavalueset;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class XLSXDataValueSetReader implements DataValueSetReader
{
    private final DataValueSet dataValueSet;

    @Override
    public DataValueSet readHeader()
    {
        return dataValueSet;
    }

    @Override
    public DataValueEntry readNext()
    {
        return null; // header contains the values
    }

    @Override
    public void close()
    {
        //dataValueSet..close();
    }
}

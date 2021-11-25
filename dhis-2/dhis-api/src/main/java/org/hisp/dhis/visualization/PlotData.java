/*
 * Copyright (c) 2004-2021, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.visualization;

import static java.util.Collections.emptyList;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.common.AnalyticsType;
import org.hisp.dhis.common.DimensionalItemObject;
import org.hisp.dhis.common.Grid;
import org.hisp.dhis.eventchart.EventChart;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.user.User;

/**
 * A simple wrapper that holds "plotable" objects and expose useful methods that
 * are required during the creation of a chart.
 *
 * @author maikel arabori
 */
public class PlotData
{
    private EventChart eventChart;

    private Visualization visualization;

    public PlotData( final EventChart eventChart )
    {
        this.eventChart = eventChart;
    }

    public PlotData( final Visualization visualization )
    {
        this.visualization = visualization;
    }

    public EventChart getEventChart()
    {
        return eventChart;
    }

    public Visualization getVisualization()
    {
        return visualization;
    }

    public boolean hasOrganisationUnitLevels()
    {
        if ( eventChart != null )
        {
            return eventChart.hasOrganisationUnitLevels();
        }
        else if ( visualization != null )
        {
            return visualization.hasOrganisationUnitLevels();
        }

        return false;
    }

    public boolean hasItemOrganisationUnitGroups()
    {
        if ( eventChart != null )
        {
            return eventChart.hasItemOrganisationUnitGroups();
        }
        else if ( visualization != null )
        {
            return visualization.hasItemOrganisationUnitGroups();
        }

        return false;
    }

    public void init( User user, Date date, OrganisationUnit organisationUnit, List<OrganisationUnit> atLevels,
        List<OrganisationUnit> inGroups, I18nFormat format )
    {
        if ( eventChart != null )
        {
            eventChart.init( user, date, organisationUnit, atLevels, inGroups, format );
        }
        else if ( visualization != null )
        {
            visualization.init( user, date, organisationUnit, atLevels, inGroups, format );
        }
    }

    public void clearTransientState()
    {
        if ( eventChart != null )
        {
            eventChart.clearTransientState();
        }
        else if ( visualization != null )
        {
            visualization.clearTransientState();
        }
    }

    public List<Integer> getOrganisationUnitLevels()
    {
        if ( eventChart != null )
        {
            return eventChart.getOrganisationUnitLevels();
        }
        else if ( visualization != null )
        {
            return visualization.getOrganisationUnitLevels();
        }

        return emptyList();
    }

    public List<OrganisationUnit> getOrganisationUnits()
    {
        if ( eventChart != null )
        {
            return eventChart.getOrganisationUnits();
        }
        else if ( visualization != null )
        {
            return visualization.getOrganisationUnits();
        }

        return emptyList();
    }

    public List<OrganisationUnitGroup> getItemOrganisationUnitGroups()
    {
        if ( eventChart != null )
        {
            return eventChart.getItemOrganisationUnitGroups();
        }
        else if ( visualization != null )
        {
            return visualization.getItemOrganisationUnitGroups();
        }

        return emptyList();
    }

    public boolean isAggregate()
    {
        if ( visualization != null )
        {
            return true;
        }

        return false;
    }

    public void setGrid( final Grid grid )
    {
        if ( eventChart != null )
        {
            eventChart.setDataItemGrid( grid );
        }
    }

    public AnalyticsType getAnalyticsType()
    {
        if ( visualization != null )
        {
            return AnalyticsType.AGGREGATE;
        }

        return AnalyticsType.EVENT;
    }

    public boolean isRegression()
    {
        if ( visualization != null )
        {
            return visualization.isRegression();
        }
        else if ( eventChart != null )
        {
            return eventChart.isRegression();
        }

        return false;
    }

    public boolean hasSortOrder()
    {
        if ( visualization != null )
        {
            return visualization.hasSortOrder();
        }
        else if ( eventChart != null )
        {
            return eventChart.hasSortOrder();
        }

        return false;
    }

    public List<DimensionalItemObject> category()
    {
        if ( visualization != null )
        {
            return visualization.chartCategory();
        }
        else if ( eventChart != null )
        {
            return eventChart.category();
        }

        return emptyList();
    }

    public List<DimensionalItemObject> series()
    {
        if ( visualization != null )
        {
            return visualization.chartSeries();
        }
        else if ( eventChart != null )
        {
            return eventChart.series();
        }

        return emptyList();
    }

    public boolean isType( String type )
    {
        if ( visualization != null )
        {
            return StringUtils.trimToEmpty( type ).equals( visualization.getType().name() );
        }
        else if ( eventChart != null )
        {
            return StringUtils.trimToEmpty( type ).equals( eventChart.getType().name() );
        }

        return false;
    }

    public String getType()
    {
        if ( visualization != null )
        {
            return visualization.getType().name();
        }
        else if ( eventChart != null )
        {
            return eventChart.getType().name();
        }

        return StringUtils.EMPTY;
    }

    public boolean hasTitle()
    {
        if ( visualization != null )
        {
            return visualization.hasTitle();
        }
        else if ( eventChart != null )
        {
            return eventChart.hasTitle();
        }

        return false;
    }

    public String getDisplayTitle()
    {
        if ( visualization != null )
        {
            return visualization.getDisplayTitle();
        }
        else if ( eventChart != null )
        {
            return eventChart.getDisplayTitle();
        }

        return StringUtils.EMPTY;
    }

    public String generateTitle()
    {
        if ( visualization != null )
        {
            return visualization.generateTitle();
        }
        else if ( eventChart != null )
        {
            return eventChart.generateTitle();
        }

        return StringUtils.EMPTY;
    }

    public boolean isHideTitle()
    {
        if ( visualization != null )
        {
            return visualization.isHideTitle();
        }
        else if ( eventChart != null )
        {
            return eventChart.isHideTitle();
        }

        return false;
    }

    public String getName()
    {
        if ( visualization != null )
        {
            return visualization.getName();
        }
        else if ( eventChart != null )
        {
            return eventChart.getName();
        }

        return StringUtils.EMPTY;
    }

    public boolean isHideLegend()
    {
        if ( visualization != null )
        {
            return visualization.isHideLegend();
        }
        else if ( eventChart != null )
        {
            return eventChart.isHideLegend();
        }

        return false;
    }

    public String getDomainAxisLabel()
    {
        if ( visualization != null )
        {
            return visualization.getDomainAxisLabel();
        }
        else if ( eventChart != null )
        {
            return eventChart.getDomainAxisLabel();
        }

        return StringUtils.EMPTY;
    }

    public String getRangeAxisLabel()
    {
        if ( visualization != null )
        {
            return visualization.getRangeAxisLabel();
        }
        else if ( eventChart != null )
        {
            return eventChart.getRangeAxisLabel();
        }

        return StringUtils.EMPTY;
    }

    public boolean isTargetLine()
    {
        if ( visualization != null )
        {
            return visualization.isTargetLine();
        }
        else if ( eventChart != null )
        {
            return eventChart.isTargetLine();
        }

        return false;
    }

    public boolean isBaseLine()
    {
        if ( visualization != null )
        {
            return visualization.isBaseLine();
        }
        else if ( eventChart != null )
        {
            return eventChart.isBaseLine();
        }

        return false;
    }

    public boolean isHideSubtitle()
    {
        if ( visualization != null )
        {
            return visualization.isHideSubtitle();
        }
        else if ( eventChart != null )
        {
            return eventChart.isHideSubtitle();
        }

        return false;
    }

    public int getSortOrder()
    {
        if ( visualization != null )
        {
            return visualization.getSortOrder();
        }
        else if ( eventChart != null )
        {
            return eventChart.getSortOrder();
        }

        return 0;
    }

    public Double getTargetLineValue()
    {
        if ( visualization != null )
        {
            return visualization.getTargetLineValue();
        }
        else if ( eventChart != null )
        {
            return eventChart.getTargetLineValue();
        }

        return 0d;
    }

    public Double getBaseLineValue()
    {
        if ( visualization != null )
        {
            return visualization.getBaseLineValue();
        }
        else if ( eventChart != null )
        {
            return eventChart.getBaseLineValue();
        }

        return 0d;
    }

    public String getTargetLineLabel()
    {
        if ( visualization != null )
        {
            return visualization.getTargetLineLabel();
        }
        else if ( eventChart != null )
        {
            return eventChart.getTargetLineLabel();
        }

        return StringUtils.EMPTY;
    }

    public String getBaseLineLabel()
    {
        if ( visualization != null )
        {
            return visualization.getBaseLineLabel();
        }
        else if ( eventChart != null )
        {
            return eventChart.getBaseLineLabel();
        }

        return StringUtils.EMPTY;
    }
}

package org.hisp.dhis.ivb.report.action;

/*
 * Copyright (c) 2004-2012, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the HISP project nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
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

import static org.hisp.dhis.commons.util.TextUtils.getCommaDelimitedString;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.lookup.LookupService;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserAuthorityGroup;
import org.hisp.dhis.user.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author BHARATH
 */
public class CountryActivitiesResultAction
    implements Action
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    @Autowired
    private LookupService lookupService;

    @Autowired
    private OrganisationUnitService organisationUnitService;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private I18nService i18nService;

    @Autowired
    private IndicatorService indicatorService;

    @Autowired
    private DataElementService dataElementService;

    @Autowired
    private DataValueService dataValueService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private SelectionTreeManager selectionTreeManager;

    @Autowired
    private IVBUtil ivbUtil;

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    private String includeTabularOutput;

    public String getIncludeTabularOutput()
    {
        return includeTabularOutput;
    }

    public void setIncludeTabularOutput( String includeTabularOutput )
    {
        this.includeTabularOutput = includeTabularOutput;
    }

    private Integer nextXmonths;

    public Integer getNextXmonths()
    {
        return nextXmonths;
    }

    public void setNextXmonths( Integer nextXmonths )
    {
        this.nextXmonths = nextXmonths;
    }

    private String orgUnitId;

    public void setOrgUnitId( String orgUnitId )
    {
        this.orgUnitId = orgUnitId;
    }

    public String getOrgUnitId()
    {
        return orgUnitId;
    }

    private List<Integer> selectedListDe = new ArrayList<Integer>();

    public void setSelectedListDe( List<Integer> selectedListDe )
    {
        this.selectedListDe = selectedListDe;
    }

    public OrganisationUnit organisationUnit;

    public OrganisationUnit getOrganisationUnit()
    {
        return organisationUnit;
    }

    private String language;

    public String getLanguage()
    {
        return language;
    }

    private String userName;

    public String getUserName()
    {
        return userName;
    }

    private SimpleDateFormat simpleDateFormat1;

    public SimpleDateFormat getSimpleDateFormat1()
    {
        return simpleDateFormat1;
    }

    private SimpleDateFormat simpleDateFormat2;

    public SimpleDateFormat getSimpleDateFormat2()
    {
        return simpleDateFormat2;
    }

    private int messageCount;

    public int getMessageCount()
    {
        return messageCount;
    }

    private String adminStatus;

    public String getAdminStatus()
    {
        return adminStatus;
    }

    private List<Integer> treeSelectedId = new ArrayList<Integer>();

    public void setTreeSelectedId( List<Integer> treeSelectedId )
    {
        this.treeSelectedId = treeSelectedId;
    }

    private Map<String, DataValue> dataValueMap = new HashMap<String, DataValue>();

    public Map<String, DataValue> getDataValueMap()
    {
        return dataValueMap;
    }

    private List<DataElement> dataElements = new ArrayList<DataElement>();

    public List<DataElement> getDataElements()
    {
        return dataElements;
    }

    private List<DataElement> chartDes = new ArrayList<DataElement>();

    public List<DataElement> getChartDes()
    {
        return chartDes;
    }

    private List<String> next12Months = new ArrayList<String>();

    public List<String> getNext12Months()
    {
        return next12Months;
    }

    private Map<DataElement, String> deMonths = new HashMap<DataElement, String>();

    public Map<DataElement, String> getDeMonths()
    {
        return deMonths;
    }

    private Map<DataElement, DataSet> deDatasets = new HashMap<DataElement, DataSet>();

    public Map<DataElement, DataSet> getDeDatasets()
    {
        return deDatasets;
    }

    // ---------------------------------------------------------------------------------------------
    // Action implementation
    // ----------------------------------------------------------------------------------------------
    public String execute()
    {
        simpleDateFormat1 = new SimpleDateFormat( "yyyy-MM-dd" );

        User curUser = currentUserService.getCurrentUser();
        Set<DataElement> userDes = new HashSet<DataElement>();
        List<UserAuthorityGroup> userAuthorityGroups = new ArrayList<UserAuthorityGroup>( curUser.getUserCredentials()
            .getUserAuthorityGroups() );
        for ( UserAuthorityGroup userAuthorityGroup : userAuthorityGroups )
        {
            userDes.addAll( userAuthorityGroup.getDataElements() );
           // System.out.println("userAuthorityGroups"+userAuthorityGroup.getDataElements());
        }

        
        userName = curUser.getUsername();
        if ( i18nService.getCurrentLocale() == null )
        {
            language = "en";
        }
        else
        {
            language = i18nService.getCurrentLocale().getLanguage();
        }

        if ( treeSelectedId.size() > 0 )
        {
            organisationUnit = organisationUnitService.getOrganisationUnit( orgUnitId );
        }
        else if ( selectionTreeManager.getReloadedSelectedOrganisationUnits() != null )
        {
            organisationUnit = selectionTreeManager.getSelectedOrganisationUnit();
        }
        else
        {
            organisationUnit = new OrganisationUnit();
        }

        if ( includeTabularOutput == null )
        {
            includeTabularOutput = "no";
        }
        else
        {
            includeTabularOutput = "yes";
        }

        SimpleDateFormat monthFormat = new SimpleDateFormat( "MMM-yyyy" );
        Date sDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime( sDate );
        for ( int i = 1; i <= nextXmonths; i++ )
        {
            next12Months.add( monthFormat.format( cal.getTime() ) );
            // cal.roll(Calendar.MONTH, 1 );
            cal.add( Calendar.MONTH, 1 );
        }
        cal.add( Calendar.MONTH, -1 );
        Date eDate = cal.getTime();

        Set<DataElement> hiddenDes = new HashSet<DataElement>( ivbUtil.getHiddenDataElementList( organisationUnit
            .getUid() ) );

        String dataElementIdsByComma = "-1";

        if ( selectedListDe.size() > 0 )
        {
            dataElementIdsByComma = getCommaDelimitedString( selectedListDe );
        }

        dataValueMap = ivbUtil.getLatestDataValuesForTabularReport( dataElementIdsByComma, organisationUnit.getId()
            + "" );

        for ( Integer deId : selectedListDe )
        {
            DataValue dv = dataValueMap.get( organisationUnit.getId() + ":" + deId );
            if ( dv != null )
            {
                String dateValue = "";
                try
                {
                    Integer month = 0;
                    String[] dateParts = dv.getValue().split( "-" );

                    if ( dateParts.length == 1 )
                    {
                        dateValue = dateParts[0] + "-01";
                        month = 1;
                    }
                    else
                    {
                        if ( dateParts[1].contains( "Q1" ) )
                        {
                            dateParts[1] = "03";

                        }

                        if ( dateParts[1].contains( "Q2" ) )
                        {
                            dateParts[1] = "06";

                        }
                        if ( dateParts[1].contains( "Q3" ) )
                        {
                            dateParts[1] = "09";

                        }
                        if ( dateParts[1].contains( "Q4" ) )
                        {
                            dateParts[1] = "12";

                        }
                        dateValue = dateParts[0] + "-" + dateParts[1];
                        month = Integer.parseInt( dateParts[1] );
                    }

                    dateValue += "-01";
                    Date d = simpleDateFormat1.parse( dateValue );

                    DataElement de = dataElementService.getDataElement( deId );
                    

                    dataElements.add( de );

                    for ( DataSet ds : de.getDataSets() )
                    {
                        if ( ds.getSources() != null && ds.getSources().size() >= 0 )
                        {
                            deDatasets.put( de, ds );
                        }
                    }
                
                    sDate=d;
                    if ( d != null && sDate.getTime() <= d.getTime() && d.getTime() <= eDate.getTime() )
                    {
                        chartDes.add( de );
                        deMonths.put( de, monthFormat.format( d ) );
                        
                    }

                }
                catch ( Exception e )
                {

                }
            }
        }

        messageCount = (int) messageService.getUnreadMessageConversationCount();
        List<UserGroup> userGrps = new ArrayList<UserGroup>( currentUserService.getCurrentUser().getGroups() );
        if ( userGrps.contains( configurationService.getConfiguration().getFeedbackRecipients() ) )
        {
            adminStatus = "Yes";
        }
        else
        {
            adminStatus = "No";
        }

        return SUCCESS;
    }

}

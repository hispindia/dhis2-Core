package org.hisp.dhis.ivb.dataentry.action;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.attribute.AttributeValue;
import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueAudit;
import org.hisp.dhis.datavalue.DataValueAuditService;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.period.QuarterlyPeriodType;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserAuthorityGroup;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author Samta bajpai
 */
public class GetHistoriesAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    private DataValueAuditService dataValueAuditService;

    public void setDataValueAuditService( DataValueAuditService dataValueAuditService )
    {
        this.dataValueAuditService = dataValueAuditService;
    }

    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    private DataValueService dataValueService;

    public void setDataValueService( DataValueService dataValueService )
    {
        this.dataValueService = dataValueService;
    }

    private DataElementCategoryService categoryService;

    public void setCategoryService( DataElementCategoryService categoryService )
    {
        this.categoryService = categoryService;
    }

    private ConstantService constantService;

    public void setConstantService( ConstantService constantService )
    {
        this.constantService = constantService;
    }

    @Autowired
    private IVBUtil ivbUtil;
    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------
    private String dataElementId;

    public void setDataElementId( String dataElementId )
    {
        this.dataElementId = dataElementId;
    }

    private boolean checkNumberType;
    
    public boolean isCheckNumberType() {
		return checkNumberType;
	}

	public void setCheckNumberType(boolean checkNumberType) {
		this.checkNumberType = checkNumberType;
	}

	private int organisationUnitId;

    public void setOrganisationUnitId( int organisationUnitId )
    {
        this.organisationUnitId = organisationUnitId;
    }

    private String periodId;

    public void setPeriodId( String periodId )
    {
        this.periodId = periodId;
    }

    private String updateStatus;

    public void setUpdateStatus( String updateStatus )
    {
        this.updateStatus = updateStatus;
    }

    public String getUpdateStatus()
    {
        return updateStatus;
    }

    // -------------------------------------------------------------------------
    // Output
    // -------------------------------------------------------------------------

    private int statusCode = 0;

    public int getStatusCode()
    {
        return statusCode;
    }

    private List<DataValueAudit> historyList = new ArrayList<DataValueAudit>();

    public List<DataValueAudit> getHistoryList()
    {
        return historyList;
    }

    private String dataElementName;

    public String getDataElementName()
    {
        return dataElementName;
    }

    private SimpleDateFormat starndardDateFormat;
    
    public SimpleDateFormat getStarndardDateFormat()
    {
        return starndardDateFormat;
    }

    private SimpleDateFormat simpleDateFormat1;

    public SimpleDateFormat getSimpleDateFormat1()
    {
        return simpleDateFormat1;
    }

    private SimpleDateFormat yearFormat;
    
    public SimpleDateFormat getYearFormat()
    {
        return yearFormat;
    }

    private SimpleDateFormat simpleDateFormat2;

    public SimpleDateFormat getSimpleDateFormat2()
    {
        return simpleDateFormat2;
    }

    private String periodTypeName;

    public String getPeriodTypeName()
    {
        return periodTypeName;
    }

    private String deleteAuthority = "";

    public String getDeleteAuthority()
    {
        return deleteAuthority;
    }

    private String hideAuthority = "";

    public String getHideAuthority()
    {
        return hideAuthority;
    }

    private String editAuthority = "";

    public String getEditAuthority()
    {
        return editAuthority;
    }

    private String conflictAuthority = "";

    public String getConflictAuthority()
    {
        return conflictAuthority;
    }

    private String closeDiscussionAuthority = "";

    public String getCloseDiscussionAuthority()
    {
        return closeDiscussionAuthority;
    }

    private DataValue dataValue;

    public DataValue getDataValue()
    {
        return dataValue;
    }

    private String currentPeriod;

    public String getCurrentPeriod()
    {
        return currentPeriod;
    }

    private String reportingPeriod;

    public String getReportingPeriod()
    {
        return reportingPeriod;
    }

    private String hideUnhideAuthority;

    public String getHideUnhideAuthority()
    {
        return hideUnhideAuthority;
    }

    private String selectedPeriod;

    public String getSelectedPeriod()
    {
        return selectedPeriod;
    }

    public void setSelectedPeriod( String selectedPeriod )
    {
        this.selectedPeriod = selectedPeriod;
    }

    private String userName;

    public String getUserName()
    {
        return userName;
    }

    private String conflict;

    public String getConflict()
    {
        return conflict;
    }

    public void setConflict( String conflict )
    {
        this.conflict = conflict;
    }

    private String yearlyDataElement = "NO";
    
    public String getYearlyDataElement()
    {
        return yearlyDataElement;
    }

    public String execute()
    {
        starndardDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        
        yearFormat = new SimpleDateFormat( "yyyy" );
        
        if ( conflict == null || conflict == "" )
        {
            conflict = "";
        }
        
        Period period = PeriodType.getPeriodFromIsoString( periodId );

        userName = currentUserService.getCurrentUser().getUsername();
        User curUser = currentUserService.getCurrentUser();
        List<UserAuthorityGroup> userAuthorityGroups = new ArrayList<UserAuthorityGroup>( curUser.getUserCredentials().getUserAuthorityGroups() );

        if ( period.getPeriodType().getName().equalsIgnoreCase( "Quarterly" ) )
        {
            simpleDateFormat1 = new SimpleDateFormat( "MMM" );
            simpleDateFormat2 = new SimpleDateFormat( "MMM yyyy" );
            periodTypeName = "Quarterly";
        }
        else
        {
            simpleDateFormat1 = new SimpleDateFormat( "yyyy-MM-dd" );
            periodTypeName = "Other";
        }

        DataElement dataElement = dataElementService.getDataElement( Integer.parseInt( dataElementId ) );
        dataElementName = dataElement.getName();

        Constant yearlyDEConst = constantService.getConstantByName( "YEARLY_DATAELMENT_ATTRIBUTE_ID" );
        Set<AttributeValue> dataElementAttributeValues = dataElement.getAttributeValues();
        if ( dataElementAttributeValues != null && dataElementAttributeValues.size() > 0 )
        {
            for ( AttributeValue deAttributeValue : dataElementAttributeValues )
            {
                if ( deAttributeValue.getAttribute().getId() == yearlyDEConst.getValue() &&  deAttributeValue.getValue().equalsIgnoreCase( "true" ) )
                {
                    yearlyDataElement = "YES";
                    break;
                }
            }
        }
        
        OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( organisationUnitId );
        DataElementCategoryOptionCombo optionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();

        dataValue = dataValueService.getLatestDataValue( dataElement, optionCombo, organisationUnit );

        if ( dataValue != null )
        {
            SimpleDateFormat standardDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
            PeriodType periodType = dataValue.getPeriod().getPeriodType();
            period = ivbUtil.getCurrentPeriod( periodType, new Date() );
            currentPeriod = period.getName();
            String dvPeriodStartDate = standardDateFormat.format( dataValue.getPeriod().getStartDate() );
            int tempMonth = Integer.parseInt( dvPeriodStartDate.split( "-" )[1] );
            if ( tempMonth >= 1 && tempMonth <= 3 )
            {
                reportingPeriod = dvPeriodStartDate.split( "-" )[0] + " Q1";
            }
            else if ( tempMonth >= 4 && tempMonth <= 6 )
            {
                reportingPeriod = dvPeriodStartDate.split( "-" )[0] + " Q2";
            }
            else if ( tempMonth >= 7 && tempMonth <= 9 )
            {
                reportingPeriod = dvPeriodStartDate.split( "-" )[0] + " Q3";
            }
            else if ( tempMonth >= 10 && tempMonth <= 12 )
            {
                reportingPeriod = dvPeriodStartDate.split( "-" )[0] + " Q4";
            }
        }
        else
        {
            System.out.println( period.getId() );
            PeriodType periodType = new QuarterlyPeriodType();
            period = ivbUtil.getCurrentPeriod( periodType, new Date() );
            dataValue = new DataValue();
            dataValue.setDataElement( dataElement );
            dataValue.setSource( organisationUnit );
            dataValue.setCategoryOptionCombo( optionCombo );
            dataValue.setValue( "" );
            dataValue.setLastUpdated( new Date() );
            dataValue.setStoredBy( currentUserService.getCurrentUsername() );
            dataValue.setPeriod( period );
            dataValueService.addDataValue( dataValue );
        }


        Set<String> userAutorities = new HashSet<String>();
        for ( UserAuthorityGroup userAuthorityGroup : userAuthorityGroups )
        {
        	userAutorities.addAll( userAuthorityGroup.getAuthorities() );
        }
        
        if( userAutorities.contains( "F_HIDE_UNHIDE_DATAVALUEAUDIT" ) )
        {
        	historyList.addAll(  dataValueAuditService.getDataValueAuditByOrgUnit_DataElement( dataElement, organisationUnit ) );
        	hideUnhideAuthority = "Yes";
        }
        else
        {
        	historyList.addAll(  dataValueAuditService.getActiveDataValueAuditByOrgUnit_DataElement( dataElement, organisationUnit ) );
        	hideUnhideAuthority = "No";
        }
        
        if( userAutorities.contains( "F_DATAVALUE_AUDIT_DELETE" ) )
        {
            deleteAuthority = "Yes";
        }
        else
        {
            deleteAuthority = "No";
        }
        
        if ( userAutorities.contains( "F_DATAVALUE_AUDIT_EDIT" ) )
        {
            editAuthority = "Yes";
        }
        else
        {
            editAuthority = "No";
        }
        
        if ( userAutorities.contains( "F_DATAVALUE_AUDIT_HIDE" ) )
        {
            hideAuthority = "Yes";
        }
        else
        {
            hideAuthority = "No";
        }
        
        if ( userAutorities.contains( "F_DATAVALUE_CONFLICT" ) )
        {
            conflictAuthority = "Yes";
        }
        else
        {
            conflictAuthority = "No";
        }
        
        if ( userAutorities.contains( "F_OPEN_CLOSE_DISCUSSION" ) )
        {
            closeDiscussionAuthority = "Yes";
        }
        else
        {
            closeDiscussionAuthority = "No";
        }
        
        /*
        for ( UserAuthorityGroup userAuthorityGroup : userAuthorityGroups )
        {
            userAuthorityGroup.getUserGroupAccesses();
            if ( userAuthorityGroup.getAuthorities().contains( "F_HIDE_UNHIDE_DATAVALUEAUDIT" ) )
            {
                dataValueAuditService.getDataValueAuditByDataValue( dataValue );
                List<DataValueAudit> dataValueAudits = new ArrayList<DataValueAudit>( dataValueAuditService.getDataValueAuditByOrgUnit_DataElement( dataElement, organisationUnit ) );
                for( DataValueAudit da : dataValueAudits )
                {
                    if( !historyList.contains( da ) )
                    {
                        historyList.add( da );
                    }
                }                
                hideUnhideAuthority = "Yes";
            }
            else
            {
                List<DataValueAudit> dataValueAudits = new ArrayList<DataValueAudit>( dataValueAuditService.getActiveDataValueAuditByOrgUnit_DataElement( dataElement, organisationUnit ) );
                for( DataValueAudit da : dataValueAudits )
                {
                    if( !historyList.contains( da ) )
                    {
                        historyList.add( da );
                    }
                }
                hideUnhideAuthority = "No";
            }
            
            if( userAuthorityGroup.getAuthorities().contains( "F_DATAVALUE_AUDIT_DELETE" ) )
            {
                deleteAuthority = "Yes";
            }
            else
            {
                deleteAuthority = "No";
            }
            
            if ( userAuthorityGroup.getAuthorities().contains( "F_DATAVALUE_AUDIT_EDIT" ) )
            {
                editAuthority = "Yes";
            }
            else
            {
                editAuthority = "No";
            }
            
            if ( userAuthorityGroup.getAuthorities().contains( "F_DATAVALUE_AUDIT_HIDE" ) )
            {
                hideAuthority = "Yes";
            }
            else
            {
                hideAuthority = "No";
            }
            
            if ( userAuthorityGroup.getAuthorities().contains( "F_DATAVALUE_CONFLICT" ) )
            {
                conflictAuthority = "Yes";
            }
            else
            {
                conflictAuthority = "No";
            }
            
            if ( userAuthorityGroup.getAuthorities().contains( "F_OPEN_CLOSE_DISCUSSION" ) )
            {
                closeDiscussionAuthority = "Yes";
            }
            else
            {
                closeDiscussionAuthority = "No";
            }
        }
         */
        
        DataValueAudit currentHistoryDVA = null;

        if ( dataValue != null )
        {
            currentHistoryDVA = dataValueAuditService.getLatestDataValueAudit( dataElement, organisationUnit, DataValueAudit.DVA_CT_HISOTRY, dataValue.getValue(), dataValue.getComment() );
        }
        else
        {
            currentHistoryDVA = dataValueAuditService.getLatestDataValueAudit( dataElement, organisationUnit, DataValueAudit.DVA_CT_HISOTRY );
        }
        //System.out.println( "historyList.size() : "+historyList.size() );
		if( currentHistoryDVA != null )
		{
		    Iterator<DataValueAudit> dvaIterator = historyList.iterator();
		    while ( dvaIterator.hasNext() )
		    {
		    	DataValueAudit dva = dvaIterator.next();
		    	System.out.println( dva.getId() + " : " +  currentHistoryDVA.getId() );
		    	if( dva.getId() == currentHistoryDVA.getId() )
		    	{
		    		dvaIterator.remove();
		    		break;
		    	}				
		    }
		}
		//System.out.println( "Next historyList.size() : "+historyList.size() );	
	
        return SUCCESS;
    }
}
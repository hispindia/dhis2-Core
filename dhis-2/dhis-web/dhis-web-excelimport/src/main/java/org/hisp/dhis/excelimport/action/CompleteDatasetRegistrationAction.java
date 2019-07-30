package org.hisp.dhis.excelimport.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataset.CompleteDataSetRegistration;
import org.hisp.dhis.dataset.CompleteDataSetRegistrationService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.i18n.I18nManager;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.period.WeeklyPeriodType;
import org.hisp.dhis.user.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 */
public class CompleteDatasetRegistrationAction implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private CompleteDataSetRegistrationService registrationService;

    @Autowired
    private DataSetService dataSetService;

    @Autowired
    private PeriodService periodService;

    @Autowired
    private IdentifiableObjectManager manager;

    @Autowired
    private OrganisationUnitService organisationUnitService;

    @Autowired
    private CurrentUserService currentUserService;
    
    @Autowired
    private I18nManager i18nManager;
    
    @Autowired
    private DataElementCategoryService dataElementCategoryService;

    
    private I18nFormat format;

    public void setFormat( I18nFormat format )
    {
        this.format = format;
    }

    
    // -------------------------------------------------------------------------
    // Input/Output
    // -------------------------------------------------------------------------
    
    private String organisationUnitUid;
    
    public void setOrganisationUnitUid( String organisationUnitUid )
    {
        this.organisationUnitUid = organisationUnitUid;
    }

    private String startDate;

    public void setStartDate( String startDate )
    {
        this.startDate = startDate;
    }

    private String endDate;

    public void setEndDate( String endDate )
    {
        this.endDate = endDate;
    }
    
    private boolean dataSetCompleteRegistration;
    
    public void setDataSetCompleteRegistration( boolean dataSetCompleteRegistration )
    {
        this.dataSetCompleteRegistration = dataSetCompleteRegistration;
    }

    private Date sDate;

    private Date eDate;
    
    private String weeklyPeriodTypeName;
    
    private String message;

    public String getMessage()
    {
        return message;
    }
    
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute() throws Exception
    {
        
        // ---------------------------------------------------------------------
        // Register as completed data set
        // ---------------------------------------------------------------------
        
        System.out.println( organisationUnitUid +  " : " + startDate +  " : " + endDate );
        
        System.out.println(   " dataSetCompleteRegistration : " + dataSetCompleteRegistration );
        
        message = "";
        // Period Info
        sDate = format.parseDate( startDate );
        eDate = format.parseDate( endDate );
        
        List<Period> periodList = new ArrayList<>();
        
        weeklyPeriodTypeName = WeeklyPeriodType.NAME;
        PeriodType periodType = periodService.getPeriodTypeByName( weeklyPeriodTypeName );
        
        //periodList = new ArrayList<Period>( periodService.getPeriodsBetweenDates( periodType, sDate, eDate ) );
        periodList = new ArrayList<Period>( periodService.getIntersectingPeriodsByPeriodType( periodType, sDate, eDate ) );
        
        // OrganisationUnit Info
        OrganisationUnit selectedOrgUnit = organisationUnitService.getOrganisationUnit( organisationUnitUid );
        
        //Set<OrganisationUnit> children = selectedOrgUnit.getChildren();
        
        List<OrganisationUnit> children = new ArrayList<OrganisationUnit>( getOrganisationUnitWithChildren( selectedOrgUnit.getId() ) );
        System.out.println( " OrganisationUnit Size -- " + children.size() );
        
        String storedBy = currentUserService.getCurrentUsername();
        System.out.println( "Total No of OrgUnit : " + children.size() + " : DataSet Complete/Incomplete Registration Start Time is : " + new Date() );
        
        if( dataSetCompleteRegistration )
        {
            if( selectedOrgUnit != null )
            {
                if( children != null && children.size() > 0 )
                {
                    for ( OrganisationUnit unit : children )
                    {
                        Set<DataSet> dataSets = unit.getDataSets();
                        
                        if( dataSets != null && dataSets.size() > 0  )
                        {
                            for ( DataSet ds : dataSets )
                            {
                                if( periodList != null && periodList.size() > 0 )
                                {
                                    for ( Period period : periodList )
                                    {
                                        //System.out.println( "Complete Registration for Children -- dataSetID" + ds.getId() +  " orgUnitId : " + unit.getId() +  " periodID : " + period.getId() );
                                        registerCompleteDataSet( ds, period, unit, storedBy );
                                    }
                                }
                            }
                        }
                    }
                }
                else
                {
                    Set<DataSet> dataSets = selectedOrgUnit.getDataSets();
                    
                    if( dataSets != null && dataSets.size() > 0  )
                    {
                        for ( DataSet ds : dataSets )
                        {
                            if( periodList != null && periodList.size() > 0 )
                            {
                                for ( Period period : periodList )
                                {
                                    //System.out.println( "Complete Registration for Children -- dataSetID" + ds.getId() +  " orgUnitId : " + selectedOrgUnit.getId() +  " periodID : " + period.getId() );
                                    registerCompleteDataSet( ds, period, selectedOrgUnit, storedBy );
                                }
                            }
                        }
                    }
                }
                message = "Dataset Complete Successfully";
            }
        }
        
        else
        {
            if( selectedOrgUnit != null )
            {
                if( children != null && children.size() > 0 )
                {
                    for ( OrganisationUnit unit : children )
                    {
                        Set<DataSet> dataSets = unit.getDataSets();
                        
                        if( dataSets != null && dataSets.size() > 0  )
                        {
                            for ( DataSet ds : dataSets )
                            {
                                if( periodList != null && periodList.size() > 0 )
                                {
                                    for ( Period period : periodList )
                                    {
                                        //System.out.println( "InComplete Registration for Children -- dataSetId " + ds.getId() +  " orgUnit ID : " + unit.getId() +  " periodID : " + period.getId() );
                                        unRegisterCompleteDataSet( ds, period, unit );
                                    }
                                }
                            }
                        }
                    }
                }
                else
                {
                    Set<DataSet> dataSets = selectedOrgUnit.getDataSets();
                    
                    if( dataSets != null && dataSets.size() > 0  )
                    {
                        for ( DataSet ds : dataSets )
                        {
                            if( periodList != null && periodList.size() > 0 )
                            {
                                for ( Period period : periodList )
                                {
                                    //System.out.println( "InComplete Registration for selected OrgUnit dataSet Id-- " + ds.getId() +  " orgUnitId : " + selectedOrgUnit.getId() +  " period ID: " + period.getId() );
                                    unRegisterCompleteDataSet( ds, period, selectedOrgUnit );
                                }
                            }
                        }
                    }
                }
                message = "Dataset incomplete Successfully";
            }
        }
        
        System.out.println( "Total No of OrgUnit : " + children.size() + " : DataSet Complete/Incomplete Registration End Time is : " + new Date() );
        
        return SUCCESS;
    }

    private void registerCompleteDataSet( DataSet dataSet, Period period, OrganisationUnit organisationUnit, String storedBy )
    {
        I18nFormat format = i18nManager.getI18nFormat();
        
        CompleteDataSetRegistration registration = new CompleteDataSetRegistration();
        
        DataElementCategoryOptionCombo defaultAttributeOptionCombo = dataElementCategoryService.getDefaultDataElementCategoryOptionCombo();
        
        if ( registrationService.getCompleteDataSetRegistration( dataSet, period, organisationUnit, defaultAttributeOptionCombo ) == null )
        {
            registration.setDataSet( dataSet );
            registration.setPeriod( period );
            registration.setSource( organisationUnit );
            registration.setDate( new Date() );
            registration.setStoredBy( storedBy );

            registration.setPeriodName( format.formatPeriod( registration.getPeriod() ) );

            registrationService.saveCompleteDataSetRegistration( registration, true );
        }
    }
    
    private void unRegisterCompleteDataSet( DataSet dataSet, Period period, OrganisationUnit organisationUnit )
    {   
        DataElementCategoryOptionCombo defaultAttributeOptionCombo = dataElementCategoryService.getDefaultDataElementCategoryOptionCombo();
        CompleteDataSetRegistration registration = registrationService.getCompleteDataSetRegistration( dataSet, period, organisationUnit, defaultAttributeOptionCombo );
        if ( registration != null )
        {
            registrationService.deleteCompleteDataSetRegistration( registration );
        }
    }
    

    /**
     * Support method for getOrganisationUnitWithChildren(). Adds all
     * OrganisationUnit children to a result collection.
     */
    public Collection<OrganisationUnit> getOrganisationUnitWithChildren( int id )
    {
        OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( id );

        if ( organisationUnit == null )
        {
            return Collections.emptySet();
        }

        List<OrganisationUnit> result = new ArrayList<OrganisationUnit>();

        int rootLevel = 1;

        organisationUnit.setHierarchyLevel( rootLevel );

        result.add( organisationUnit );

        addOrganisationUnitChildren( organisationUnit, result, rootLevel );

        return result;
    }


    private void addOrganisationUnitChildren( OrganisationUnit parent, List<OrganisationUnit> result, int level )
    {
        if ( parent.getChildren() != null && parent.getChildren().size() > 0 )
        {
            level++;
        }

        List<OrganisationUnit> childList = parent.getSortedChildren();

        for ( OrganisationUnit child : childList )
        {
            child.setHierarchyLevel( level );

            result.add( child );

            addOrganisationUnitChildren( child, result, level );
        }

        level--;
    }    
    
    
}

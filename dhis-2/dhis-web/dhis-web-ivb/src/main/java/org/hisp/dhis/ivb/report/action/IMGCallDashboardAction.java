package org.hisp.dhis.ivb.report.action;

import static org.hisp.dhis.common.IdentifiableObjectUtils.getIdentifiers;
import static org.hisp.dhis.commons.util.TextUtils.getCommaDelimitedString;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.lookup.Lookup;
import org.hisp.dhis.lookup.LookupService;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupSet;
import org.hisp.dhis.user.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

public class IMGCallDashboardAction implements Action
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private OrganisationUnitGroupService organisationUnitGroupService;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private I18nService i18nService;

    @Autowired
    private MessageService messageService;

    @Autowired 
    private LookupService lookupService;

    @Autowired
    private IVBUtil ivbUtil;

    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------
    private String language;

    private String userName;

    public String getLanguage()
    {
        return language;
    }

    public String getUserName()
    {
        return userName;
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


    List<String> introQuarters = new ArrayList<String>();
    
    public List<String> getIntroQuarters()
    {
        return introQuarters;
    }

    Map<OrganisationUnitGroup, Integer> regionMap = new HashMap<OrganisationUnitGroup, Integer>();

    public Map<OrganisationUnitGroup, Integer> getRegionMap()
    {
        return regionMap;
    }

    Map<OrganisationUnitGroup, Integer> gaviRegionMap = new HashMap<OrganisationUnitGroup, Integer>();
    
    public Map<OrganisationUnitGroup, Integer> getGaviRegionMap()
    {
        return gaviRegionMap;
    }

    List<OrganisationUnitGroup> regions = new ArrayList<OrganisationUnitGroup>();
    
    public List<OrganisationUnitGroup> getRegions()
    {
        return regions;
    }

    Map<String, Integer> ivpIntroDateMap = new HashMap<String, Integer>();

    public Map<String, Integer> getIvpIntroDateMap()
    {
        return ivpIntroDateMap;
    }

    Map<String, Integer> ipvDecisionMap = new HashMap<String, Integer>();
    
    public Map<String, Integer> getIpvDecisionMap()
    {
        return ipvDecisionMap;
    }

    Map<String, Integer> ipvAppStatusMap = new HashMap<String, Integer>();
    
    public Map<String, Integer> getIpvAppStatusMap()
    {
        return ipvAppStatusMap;
    }

    List<String> ipvAppStatusList = new ArrayList<String>();
    
    public List<String> getIpvAppStatusList()
    {
        return ipvAppStatusList;
    }

    private OrganisationUnitGroup totalOU;
    
    public OrganisationUnitGroup getTotalOU()
    {
        return totalOU;
    }

    private Map<String, DataValue> dataValueMap = new HashMap<String, DataValue>();

    private OrganisationUnitGroupSet whoRegionsGroupSet;
    
    public OrganisationUnitGroupSet getWhoRegionsGroupSet()
    {
        return whoRegionsGroupSet;
    }

    private Map<String, String> ipvDateMap = new HashMap<String, String>();
    
    public Map<String, String> getIpvDateMap()
    {
        return ipvDateMap;
    }
    
    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------
    @Override
    public String execute() throws Exception
    {
        userName = currentUserService.getCurrentUser().getUsername();
        
        if ( i18nService.getCurrentLocale() == null )
        {
            language = "en";
        }
        else
        {
            language = i18nService.getCurrentLocale().getLanguage();
        }

        Lookup lookup = lookupService.getLookupByName( Lookup.TIER_GROUPSET );
        
        OrganisationUnitGroupSet tierGroupSet = organisationUnitGroupService.getOrganisationUnitGroupSet( Integer.parseInt( lookup.getValue() ) );

        lookup = lookupService.getLookupByName( Lookup.WHO_REGIONS_GROUPSET );
        
        whoRegionsGroupSet = organisationUnitGroupService.getOrganisationUnitGroupSet( Integer.parseInt( lookup.getValue() ) );

        List<OrganisationUnit> tierOrgUnitList = new ArrayList<OrganisationUnit>( tierGroupSet.getOrganisationUnits() );
        List<Integer> orgunitIds = new ArrayList<Integer>( getIdentifiers( tierOrgUnitList ) );
        String orgUnitIdsByComma = "-1";
        if ( orgunitIds.size() > 0 )
        {
            orgUnitIdsByComma = getCommaDelimitedString( orgunitIds );
        }

        String dataElementIdsByComma = "359,360,4,493";
        Integer ipvIntroDateDeid = 359;
        Integer ipvIntroStatusDeid = 360;
        Integer ipvGAVIEligStatusDeid = 4;
        Integer ipvStatusDeid = 493;
        
        dataValueMap = ivbUtil.getLatestDataValuesForTabularReport( dataElementIdsByComma, orgUnitIdsByComma );
        
        totalOU = new OrganisationUnitGroup();
        totalOU.setId( -1 );
        totalOU.setName( "Total" );
        
        //IPV Introduction Date
        Set<String> introQset = new HashSet<String>();
        Set<String> ipvAppStatusSet = new HashSet<String>();
        
        for( OrganisationUnit ou : tierOrgUnitList )
        {
            //OrganisationUnit region = ou.getParent();
            
            OrganisationUnitGroup region = ou.getGroupInGroupSet( whoRegionsGroupSet );
            
            Integer count = regionMap.get( region );
            if( count == null )
            {
                count = 0;
            }
            regionMap.put( region, ++count );
            
            count = regionMap.get( totalOU );
            if( count == null )
            {
                count = 0;
            }
            regionMap.put( totalOU, ++count );
            
            DataValue dv = dataValueMap.get( ou.getId()+":"+ipvIntroDateDeid );
            String ipvIntroDate = "";
            if( dv != null && dv.getValue() != null )
            {
                ipvIntroDate = dv.getValue();
            }
            
            if( ipvIntroDate != null && !ipvIntroDate.trim().equals( "" ) && !ipvIntroDate.trim().equals( "2015" ) )
            {
                ipvIntroDate = ivbUtil.getQuarterFormatByString( ipvIntroDate );
            }
            
            //System.out.println( region.getName() + " : " + ipvIntroDate );
            
            if( ipvIntroDate != null && !ipvIntroDate.trim().equals( "" ) && !ipvIntroDate.trim().equals( "2015" ) )
            {
                Integer count1 = ivpIntroDateMap.get( region.getId()+":"+ipvIntroDate );
                if( count1 == null )
                {
                    count1 = 0;
                }
                ivpIntroDateMap.put( region.getId()+":"+ipvIntroDate, ++count1 );
                
                count1 = ivpIntroDateMap.get( totalOU.getId()+":"+ipvIntroDate );
                if( count1 == null )
                {
                    count1 = 0;
                }
                ivpIntroDateMap.put( totalOU.getId()+":"+ipvIntroDate, ++count1 );

                introQset.add( ipvIntroDate );
            }
            else
            {
                Integer count1 = ivpIntroDateMap.get( region.getId()+":Not available" );
                if( count1 == null )
                {
                    count1 = 0;
                }
                ivpIntroDateMap.put( region.getId()+":Not available", ++count1 );

                count1 = ivpIntroDateMap.get( totalOU.getId()+":Not available" );
                if( count1 == null )
                {
                    count1 = 0;
                }
                ivpIntroDateMap.put( totalOU.getId()+":Not available", ++count1 );

                introQset.add( "Not available" );                
            }

            dv = dataValueMap.get( ou.getId()+":"+ipvIntroStatusDeid );
            String ipvIntroStatus = "";
            if( dv != null && dv.getValue() != null )
            {
                ipvIntroStatus = dv.getValue();
            }
            
            DataValue dv1= dataValueMap.get( ou.getId()+":"+ipvGAVIEligStatusDeid );
            String gaviEligStatus = "";
            if( dv1 != null && dv1.getValue() != null )
            {
                gaviEligStatus = dv1.getValue();
            }
            
            if( ipvIntroStatus.trim().equalsIgnoreCase( "Formal commitment" ) || ipvIntroStatus.trim().equalsIgnoreCase( "Introduced partially" ) || ipvIntroStatus.trim().equalsIgnoreCase( "Introduced sequentially" )  || ipvIntroStatus.trim().equalsIgnoreCase( "Introduced into Routine" ) )
            {
                if( gaviEligStatus.trim().equalsIgnoreCase( "eligible" ) || gaviEligStatus.trim().equalsIgnoreCase( "graduate" ) )
                {
                    Integer count1 = ipvDecisionMap.get( region.getId()+":Formal Commitment:gavi" );
                    if( count1 == null )
                    {
                        count1 = 0;
                    }
                    ipvDecisionMap.put( region.getId()+":Formal Commitment:gavi", ++count1 );

                    count1 = ipvDecisionMap.get( totalOU.getId()+":Formal Commitment:gavi" );
                    if( count1 == null )
                    {
                        count1 = 0;
                    }
                    ipvDecisionMap.put( totalOU.getId()+":Formal Commitment:gavi", ++count1 );
                }
                else
                {
                    Integer count1 = ipvDecisionMap.get( region.getId()+":Formal Commitment:non gavi" );
                    if( count1 == null )
                    {
                        count1 = 0;
                    }
                    ipvDecisionMap.put( region.getId()+":Formal Commitment:non gavi", ++count1 );

                    count1 = ipvDecisionMap.get( totalOU.getId()+":Formal Commitment:non gavi" );
                    if( count1 == null )
                    {
                        count1 = 0;
                    }
                    ipvDecisionMap.put( totalOU.getId()+":Formal Commitment:non gavi", ++count1 );
                }
            }
            else if( ipvIntroStatus.trim().equalsIgnoreCase( "Intent to introduce" ) )
            {
                if( gaviEligStatus.trim().equalsIgnoreCase( "eligible" ) || gaviEligStatus.trim().equalsIgnoreCase( "graduate" ) )
                {
                    Integer count1 = ipvDecisionMap.get( region.getId()+":Intent to Introduce:gavi" );
                    if( count1 == null )
                    {
                        count1 = 0;
                    }
                    ipvDecisionMap.put( region.getId()+":Intent to Introduce:gavi", ++count1 );

                    count1 = ipvDecisionMap.get( totalOU.getId()+":Intent to Introduce:gavi" );
                    if( count1 == null )
                    {
                        count1 = 0;
                    }
                    ipvDecisionMap.put( totalOU.getId()+":Intent to Introduce:gavi", ++count1 );
                }
                else
                {
                    Integer count1 = ipvDecisionMap.get( region.getId()+":Intent to Introduce:non gavi" );
                    if( count1 == null )
                    {
                        count1 = 0;
                    }
                    ipvDecisionMap.put( region.getId()+":Intent to Introduce:non gavi", ++count1 );

                    count1 = ipvDecisionMap.get( totalOU.getId()+":Intent to Introduce:non gavi" );
                    if( count1 == null )
                    {
                        count1 = 0;
                    }
                    ipvDecisionMap.put( totalOU.getId()+":Intent to Introduce:non gavi", ++count1 );
                }
            }
            else
            {
                if( gaviEligStatus.trim().equalsIgnoreCase( "eligible" ) || gaviEligStatus.trim().equalsIgnoreCase( "graduate" ) )
                {
                    Integer count1 = ipvDecisionMap.get( region.getId()+":Not Available:gavi" );
                    if( count1 == null )
                    {
                        count1 = 0;
                    }
                    ipvDecisionMap.put( region.getId()+":Not Available:gavi", ++count1 );
                    
                    count1 = ipvDecisionMap.get( totalOU.getId()+":Not Available:gavi" );
                    if( count1 == null )
                    {
                        count1 = 0;
                    }
                    ipvDecisionMap.put( totalOU.getId()+":Not Available:gavi", ++count1 );
                }
                else
                {
                    Integer count1 = ipvDecisionMap.get( region.getId()+":Not Available:non gavi" );
                    if( count1 == null )
                    {
                        count1 = 0;
                    }
                    ipvDecisionMap.put( region.getId()+":Not Available:non gavi", ++count1 );

                    count1 = ipvDecisionMap.get( totalOU.getId()+":Not Available:non gavi" );
                    if( count1 == null )
                    {
                        count1 = 0;
                    }
                    ipvDecisionMap.put( totalOU.getId()+":Not Available:non gavi", ++count1 );
                }
            }
            
            if( gaviEligStatus.trim().equalsIgnoreCase( "eligible" ) || gaviEligStatus.trim().equalsIgnoreCase( "graduate" ) )
            {
                count = gaviRegionMap.get( region );
                if( count == null )
                {
                    count = 0;
                }
                gaviRegionMap.put( region, ++count );

                count = gaviRegionMap.get( totalOU );
                if( count == null )
                {
                    count = 0;
                }
                gaviRegionMap.put( totalOU, ++count );
                

                DataValue dv2= dataValueMap.get( ou.getId()+":"+ipvStatusDeid );
                String ipvAppStatus = "";
                if( dv2 != null && dv2.getValue() != null )
                {
                    ipvAppStatus = dv2.getValue();
                    String tempIPVFormat = "";
                    try
                    {
                        //Date tempDate = ivbUtil.getStartDateByString( ipvAppStatus );
                        tempIPVFormat = getDateFormat( ipvAppStatus );
                        //SimpleDateFormat monthFormat = new SimpleDateFormat( "MMM-yyyy" );
                        //ipvAppStatus = monthFormat.format( tempDate );                    
                    }
                    catch( Exception e )
                    {
                        tempIPVFormat = ipvAppStatus;
                    }
                    ipvDateMap.put( ipvAppStatus, tempIPVFormat );
                }
            
                if( ipvAppStatus.trim().replace( " ", "" ).equalsIgnoreCase( "notapplicable" ) )
                {
                    ipvAppStatus = "Not Applicable";
                    ipvDateMap.put( ipvAppStatus, ipvAppStatus );
                }
            
                ipvAppStatusSet.add( ipvAppStatus );
            
                count = ipvAppStatusMap.get( region.getId()+":"+ipvAppStatus  );
                if( count == null )
                {
                    count = 0;
                }
                ipvAppStatusMap.put( region.getId()+":"+ipvAppStatus, ++count );

                count = ipvAppStatusMap.get( totalOU.getId()+":"+ipvAppStatus  );
                if( count == null )
                {
                    count = 0;
                }
                ipvAppStatusMap.put( totalOU.getId()+":"+ipvAppStatus, ++count );
            }
        }
        
        regions.addAll( regionMap.keySet() );
        regions.remove( totalOU );
        Collections.sort( regions, new IdentifiableObjectNameComparator() );
        regions.add( totalOU );
        
        introQuarters.addAll( introQset );
        Collections.sort( introQuarters );
        
        ipvAppStatusList.addAll( ipvAppStatusSet );
        //Collections.sort( ipvAppStatusList );
        int navailFlag = 0;
        int nappFlag = 0;
        if( ipvAppStatusList.contains( "Not Available" ) )
        {
            ipvAppStatusList.remove( "Not Available" );
            navailFlag = 1; 
        }
        if( ipvAppStatusList.contains( "Not Applicable" ) )
        {
            ipvAppStatusList.remove( "Not Applicable" );
            nappFlag = 1;
        }
        Collections.sort( ipvAppStatusList );
        if( navailFlag != 0 )
        {
            ipvAppStatusList.add( "Not Available" );
        }
        if( nappFlag != 0 )
        {
            ipvAppStatusList.add( "Not Applicable" );
        }

        for( String introQuarter : introQuarters )
        {
            Double tempVal = 0.0;
            tempVal = (double) ivpIntroDateMap.get( totalOU.getId()+":"+introQuarter ) / regionMap.get( totalOU ) * 100.0;
            Long tempP = Math.round( tempVal );
            ivpIntroDateMap.put( totalOU.getId()+":P:"+introQuarter, (Integer) tempP.intValue() );
        }
        
        Double tempVal = 0.0;
        try
        {
        	tempVal = (double) ipvDecisionMap.get( totalOU.getId()+":Formal Commitment:gavi" ) / regionMap.get( totalOU ) * 100.0;
        }
        catch( Exception e )
        {
        	tempVal = 0.0;
        }
        Long tempP = Math.round( tempVal );
        ipvDecisionMap.put( totalOU.getId()+":P:Formal Commitment:gavi", (Integer) tempP.intValue() );
        
        try
        {
        	tempVal = (double) ipvDecisionMap.get( totalOU.getId()+":Formal Commitment:non gavi" ) / regionMap.get( totalOU ) * 100.0;
        }
        catch( Exception e )
        {
        	tempVal = 0.0;
        }
        tempP = Math.round( tempVal );
        ipvDecisionMap.put( totalOU.getId()+":P:Formal Commitment:non gavi", (Integer) tempP.intValue() );

        try
        {
        	tempVal = (double) ipvDecisionMap.get( totalOU.getId()+":Intent to Introduce:gavi" ) / regionMap.get( totalOU ) * 100.0;
        }
        catch( Exception e )
        {
        	tempVal = 0.0;
        }
        tempP = Math.round( tempVal );
        ipvDecisionMap.put( totalOU.getId()+":P:Intent to Introduce:gavi", (Integer) tempP.intValue() );

        try
        {
        	tempVal = (double) ipvDecisionMap.get( totalOU.getId()+":Intent to Introduce:non gavi" ) / regionMap.get( totalOU ) * 100.0;
        }
        catch( Exception e )
        {
        	tempVal = 0.0;
        }
        tempP = Math.round( tempVal );
        ipvDecisionMap.put( totalOU.getId()+":P:Intent to Introduce:non gavi", (Integer) tempP.intValue() );

        try
        {
        	tempVal = (double) ipvDecisionMap.get( totalOU.getId()+":Not Available:gavi" ) / regionMap.get( totalOU ) * 100.0;
        }
        catch( Exception e )
        {
        	tempVal = 0.0;
        }
        
        tempP = Math.round( tempVal );
        ipvDecisionMap.put( totalOU.getId()+":P:Not Available:gavi", (Integer) tempP.intValue() );

        try
        {
        	tempVal = (double) ipvDecisionMap.get( totalOU.getId()+":Not Available:non gavi" ) / regionMap.get( totalOU ) * 100.0;
        }
        catch( Exception e )
        {
        	tempVal = 0.0;
        }
        tempP = Math.round( tempVal );
        ipvDecisionMap.put( totalOU.getId()+":P:Not Available:non gavi", (Integer) tempP.intValue() );

        for( String ipvAppStatus : ipvAppStatusList )
        {
            tempVal = 0.0;
            tempVal = (double) ipvAppStatusMap.get( totalOU.getId()+":"+ipvAppStatus ) / gaviRegionMap.get( totalOU ) * 100.0;
            tempP = Math.round( tempVal );
            ipvAppStatusMap.put( totalOU.getId()+":P:"+ipvAppStatus, (Integer) tempP.intValue() );
        }

        return SUCCESS;
    }
    
    
    public String getDateFormat( String dateStr )
    {
        String returnDateFormat = "";

        try
        {
            String startDate = "";
            String[] startDateParts = dateStr.split( "-" );

            if ( startDateParts.length <= 1 )
            {
                returnDateFormat = dateStr;
            }
            else if ( startDateParts[1].equalsIgnoreCase( "Q1" ) || startDateParts[1].equalsIgnoreCase( "Q2" ) || 
                        startDateParts[1].equalsIgnoreCase( "Q3" ) || startDateParts[1].equalsIgnoreCase( "Q4" ) )
            {
                returnDateFormat = dateStr;
            }
            else
            {
                startDate = startDateParts[0] + "-" + startDateParts[1] + "-01";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
                Date sDate = simpleDateFormat.parse( startDate );
                SimpleDateFormat monthFormat = new SimpleDateFormat( "MMM-yyyy" );
                returnDateFormat = monthFormat.format( sDate );
            }
        }        
        catch( Exception e )
        {
            System.out.println( "Exception in getStartDateByString : "+ e.getMessage() );
        }
        
        return returnDateFormat;
    }

}

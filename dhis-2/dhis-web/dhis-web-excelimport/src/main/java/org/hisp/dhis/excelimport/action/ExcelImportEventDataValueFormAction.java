package org.hisp.dhis.excelimport.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hisp.dhis.category.Category;
import org.hisp.dhis.category.CategoryOption;
import org.hisp.dhis.category.CategoryOptionCombo;
import org.hisp.dhis.category.CategoryService;
import org.hisp.dhis.excelimport.api.AttributeCategoryOptionCombo;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 */
public class ExcelImportEventDataValueFormAction implements Action
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private PeriodService periodService;

    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }

    @Autowired
    private OrganisationUnitService organisationUnitService;

    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private final int ALL = 0;

    public int getALL()
    {
        return ALL;
    }

    // -------------------------------------------------------------------------
    // Properties
    // -------------------------------------------------------------------------

    private String message;

    public void setMessage( String message )
    {
        this.message = message;
    }
    
    private Collection<OrganisationUnit> organisationUnits;

    public Collection<OrganisationUnit> getOrganisationUnits()
    {
        return organisationUnits;
    }
        
    private Collection<PeriodType> periodTypes;

    public Collection<PeriodType> getPeriodTypes()
    {
        return periodTypes;
    }
    
    private List<Period> periods;

    public List<Period> getPeriods()
    {
        return periods;
    }
    
    private List<String> yearList;
    
    public List<String> getYearList()
    {
        return yearList;
    }

    private List<AttributeCategoryOptionCombo> attributeTypeCOCList = new ArrayList<AttributeCategoryOptionCombo>();
    
    public List<AttributeCategoryOptionCombo> getAttributeTypeCOCList()
    {
        return attributeTypeCOCList;
    }
        
    private SimpleDateFormat simpleDateFormat;
    
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
        //raFolderName = reportService.getRAFolderName();

        /* Period Info */
       
        /*
        periodTypes = periodService.getAllPeriodTypes();

        Iterator<PeriodType> alldeIterator = periodTypes.iterator();
        while ( alldeIterator.hasNext() )
        {
            PeriodType type = alldeIterator.next();
            
            if ( type.getName().equalsIgnoreCase("Weekly") || type.getName().equalsIgnoreCase("Monthly") || type.getName().equalsIgnoreCase("quarterly") || type.getName().equalsIgnoreCase("yearly"))
            {
                //periods.addAll( periodService.getPeriodsByPeriodType(type) );
            }
            
            else
            {
               alldeIterator.remove();
            }
        }
        */
        
        PeriodType periodType = periodService.getPeriodTypeByName( "Yearly" );
        
        periods = new ArrayList<Period>( periodService.getPeriodsByPeriodType( periodType ) );
        
        Iterator<Period> periodIterator = periods.iterator();
        while ( periodIterator.hasNext() )
        {
            Period p1 = periodIterator.next();

            if ( p1.getStartDate().compareTo( new Date() ) > 0 )
            {
                periodIterator.remove();
            }

        }
        
        Collections.sort( periods, new PeriodsComparator() );
        
        yearList = new ArrayList<String>();
        
        simpleDateFormat = new SimpleDateFormat( "yyyy" );
        for ( Period p1 : periods )
        {
            yearList.add( simpleDateFormat.format( p1.getStartDate() ) );
        }
        
        /*
        CategoryOptionCombo defaultAttributeOptionCombo = categoryService.getDefaultCategoryOptionCombo();
        
        List<Category> dataElementAttributeCategoryList = new ArrayList<>( categoryService.getAttributeCategories() );
        
        Category dataElementAttributeCategory = dataElementAttributeCategoryList.get( 0 );
        
        List<CategoryOption> categoryOption = new ArrayList<>( dataElementAttributeCategory.getCategoryOptions() );
        
        attributeTypeCOCList = new ArrayList<AttributeCategoryOptionCombo>( attributeTypeCOCList() );
        */
        
        System.out.println(message);
        
        
        
        
        return SUCCESS;
    }
    

    // Supportive methods
    public List<AttributeCategoryOptionCombo> attributeTypeCOCList()
    {
        List<AttributeCategoryOptionCombo> attributeTypeCOCList = new ArrayList<AttributeCategoryOptionCombo>();

        String query = "";

        try
        {
           
            query = "SELECT coc.categoryoptioncomboid, coc.name, coc.uid from categoryoptioncombo coc " +
                    " INNER JOIN categorycombos_optioncombos coc_co ON coc_co.categoryoptioncomboid = coc.categoryoptioncomboid " +
                    " INNER JOIN categorycombo co ON co.categorycomboid = coc_co.categorycomboid " +
                    " WHERE co.datadimensiontype = 'ATTRIBUTE';";
            
            SqlRowSet rs1 = jdbcTemplate.queryForRowSet( query );

            while ( rs1.next() )
            {
                AttributeCategoryOptionCombo attributeCategoryOptionCombo = new AttributeCategoryOptionCombo();
                attributeCategoryOptionCombo.setCocId( rs1.getInt( 1 ) );
                attributeCategoryOptionCombo.setCocName( rs1.getString( 2 ) );
                attributeCategoryOptionCombo.setCocUid( rs1.getString( 3 ) );
                attributeTypeCOCList.add( attributeCategoryOptionCombo );
            }
        }
        catch ( Exception e )
        {
            System.out.println( "SQL Exception : " + e.getMessage() );
            return null;
        }
 
        return attributeTypeCOCList;
    }
}

package org.hisp.dhis.dataanalyser.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryCombo;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementGroup;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.Section;
import org.hisp.dhis.dataset.SectionService;
import org.hisp.dhis.option.OptionService;
import org.hisp.dhis.option.OptionSet;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

public class GetDataElementsForTabularAnalysisAction implements Action
{

    private final static int ALL = 0;

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    private DataElementCategoryService dataElementCategoryService;

    public void setDataElementCategoryService( DataElementCategoryService dataElementCategoryService )
    {
        this.dataElementCategoryService = dataElementCategoryService;
    }
    
    private SectionService sectionService;

    public void setSectionService( SectionService sectionService )
    {
        this.sectionService = sectionService;
    }
    
    @Autowired
    private OptionService optionService;
    
    // -------------------------------------------------------------------------
    // Comparator
    // -------------------------------------------------------------------------
    private Comparator<DataElement> dataElementComparator;

    public void setDataElementComparator( Comparator<DataElement> dataElementComparator )
    {
        this.dataElementComparator = dataElementComparator;
    }
    

    // -------------------------------------------------------------------------
    // DisplayPropertyHandler
    // -------------------------------------------------------------------------
    /*
    private DisplayPropertyHandler displayPropertyHandler;

    public void setDisplayPropertyHandler( DisplayPropertyHandler displayPropertyHandler )
    {
        this.displayPropertyHandler = displayPropertyHandler;
    }
    */
    // -------------------------------------------------------------------------
    // Input & output
    // -------------------------------------------------------------------------
    private Integer id;

    public void setId( Integer id )
    {
        this.id = id;
    }

    private String deOptionValue;

    public void setDeOptionValue( String deOptionValue )
    {
        this.deOptionValue = deOptionValue;
    }

    public String getDeOptionValue()
    {
        return deOptionValue;
    }

    private List<DataElement> dataElements;

    public List<DataElement> getDataElements()
    {
        return dataElements;
    }

    private List<String> optionComboNames;

    public List<String> getOptionComboNames()
    {
        return optionComboNames;
    }

    private List<String> optionComboIds;

    public List<String> getOptionComboIds()
    {
        return optionComboIds;
    }

    private String chkValue;

    public void setChkValue( String chkValue )
    {
        this.chkValue = chkValue;
    }
    
    private String dataElementGroupCode= "";
    
    private Set<String> nonPrivateOptionsCode;
    
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------
    public String execute()
        throws Exception
    {
        optionComboIds = new ArrayList<String>();
        optionComboNames = new ArrayList<String>();
        nonPrivateOptionsCode = new HashSet<String>();
        
        OptionSet optionSet = optionService.getOptionSetByCode( "non_private_coc" );
        if( optionSet != null )
        {
            nonPrivateOptionsCode = new HashSet<String>( optionSet.getOptionCodesAsSet() );
        }
        
        System.out.println( optionSet.getName() + " optionSet size = " + nonPrivateOptionsCode.size() );
        
        if ( id == null || id == ALL )
        {
            System.out.println("The id is null");
            dataElements = new ArrayList<DataElement>( dataElementService.getAllDataElements() );
            System.out.println( " DataElements size 11 = "+ dataElements.size() );
        } 
        else
        {
            if ( chkValue.equals( "true" ) )
            {
                DataElementGroup dataElementGroup = dataElementService.getDataElementGroup( id );
                if ( dataElementGroup != null )
                {
                    dataElementGroupCode = dataElementGroup.getCode();
                    dataElements = new ArrayList<DataElement>( dataElementGroup.getMembers() );
                    System.out.println( "dataElementGroup id = " + id + " dataElements size = " + dataElements.size() );
                }
                else
                {
                    dataElements = new ArrayList<DataElement>();
                }
            }
            if ( chkValue.equals( "false" ) )
            {
                Section section = sectionService.getSection( id );
                if ( section != null )
                {
                    dataElements = new ArrayList<DataElement>( section.getDataElements() );
                    //System.out.println( "section id = " + id + " dataElements size = " + dataElements.size() );
                }
                else
                {
                    dataElements = new ArrayList<DataElement>();
                }
            }
        }
        //System.out.println( " dataElements size 22 = " + dataElements.size() );
        Iterator<DataElement> alldeIterator = dataElements.iterator();
        while ( alldeIterator.hasNext() )
        {
            DataElement de1 = alldeIterator.next();
            /*
            if ( !de1.getValueType().isInteger() || !de1.getDomainType().getValue().equals( "AGGREGATE" )  )
            {
                alldeIterator.remove();
            }
            */
            if ( !de1.getDomainType().getValue().equalsIgnoreCase( "AGGREGATE" ) )
            {
                alldeIterator.remove();
	    }
        }
        
        //System.out.println( " dataElements size 33 = " + dataElements.size() );
        Collections.sort( dataElements, dataElementComparator );
        

        //displayPropertyHandler.handle( dataElements );

        if ( deOptionValue != null )
        {
            if ( deOptionValue.equalsIgnoreCase( "optioncombo" ) )
            {
                if( dataElementGroupCode.equalsIgnoreCase( "private" ) )
                {
                    //System.out.println( id + " 1 dataElementGroupName = " + dataElementGroupCode );
                    Iterator<DataElement> deIterator = dataElements.iterator();
                    while ( deIterator.hasNext() )
                    {
                        DataElement de = deIterator.next();

                        //DataElementCategoryCombo dataElementCategoryCombo = de.getCategoryCombo();
                        DataElementCategoryCombo dataElementCategoryCombo = de.getDataElementCategoryCombo();
                        List<DataElementCategoryOptionCombo> optionCombos = new ArrayList<DataElementCategoryOptionCombo>( dataElementCategoryCombo.getOptionCombos() );

                        Iterator<DataElementCategoryOptionCombo> optionComboIterator = optionCombos.iterator();
                        while ( optionComboIterator.hasNext() )
                        {
                            //Public Institution
                            // 35718 Accredited Private Institutions MIES Dataset Group New
                            // eliminate Public Institution while select Accredited Private Institutions DE Group
                            DataElementCategoryOptionCombo decoc = optionComboIterator.next();
                            
                            if ( !nonPrivateOptionsCode.contains( decoc.getUid() ))
                            {
                                optionComboIds.add( de.getId() + ":" + decoc.getId() );
                                optionComboNames.add( de.getName() + ":" + dataElementCategoryService.getDataElementCategoryOptionCombo( decoc.getUid() ).getName() );
                            }
                            
                            /*
                            if ( !dataElementCategoryService.getDataElementCategoryOptionCombo( decoc.getUid() ).getName().equalsIgnoreCase( "Public Institution" ) )
                            {
                                optionComboIds.add( de.getId() + ":" + decoc.getId() );
                                optionComboNames.add( de.getName() + ":" + dataElementCategoryService.getDataElementCategoryOptionCombo( decoc.getUid() ).getName() );
                            }
                            */
                        }
                    }
                }
                else
                {
                    //System.out.println( id + " 2 dataElementGroupName = " + dataElementGroupCode );
                    Iterator<DataElement> deIterator = dataElements.iterator();
                    while ( deIterator.hasNext() )
                    {
                        DataElement de = deIterator.next();

                        //DataElementCategoryCombo dataElementCategoryCombo = de.getCategoryCombo();
                        DataElementCategoryCombo dataElementCategoryCombo = de.getDataElementCategoryCombo();
                        List<DataElementCategoryOptionCombo> optionCombos = new ArrayList<DataElementCategoryOptionCombo>( dataElementCategoryCombo.getOptionCombos() );

                        Iterator<DataElementCategoryOptionCombo> optionComboIterator = optionCombos.iterator();
                        while ( optionComboIterator.hasNext() )
                        {
                            DataElementCategoryOptionCombo decoc = optionComboIterator.next();
                            optionComboIds.add( de.getId() + ":" + decoc.getId() );
                            optionComboNames.add( de.getName() + ":" + dataElementCategoryService.getDataElementCategoryOptionCombo( decoc.getUid() ).getName() );
                        }

                    }
                }
                
            }
        }
        
        return SUCCESS;
    }
}
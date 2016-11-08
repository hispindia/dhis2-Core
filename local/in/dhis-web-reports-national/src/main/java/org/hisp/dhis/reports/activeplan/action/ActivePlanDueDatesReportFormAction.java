package org.hisp.dhis.reports.activeplan.action;

import java.util.List;

import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.reports.ReportService;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 *
 * @version ActivePlanDueDatesReportFormAction.javaAug 28, 2012 12:51:11 PM	
 */

public class ActivePlanDueDatesReportFormAction implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private SelectionTreeManager selectionTreeManager;

    public void setSelectionTreeManager( SelectionTreeManager selectionTreeManager )
    {
        this.selectionTreeManager = selectionTreeManager;
    }
    /*
    private ProgramService programService;
    
    public void setProgramService( ProgramService programService )
    {
        this.programService = programService;
    }
    */
    private ReportService reportService;

    public void setReportService( ReportService reportService )
    {
        this.reportService = reportService;
    }
    
    // -------------------------------------------------------------------------
    // Getter & Setter
    // -------------------------------------------------------------------------

    private List<Program> programList;
    
    public List<Program> getProgramList()
    {
        return programList;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------
    public String execute() throws Exception
    {
        //programList = new ArrayList<Program>( programService.getAllPrograms() );
        
        //programList = ( List<Program> ) getProgramsByOrgUnit( organisationUnit );
        
        /*
        OrganisationUnit organisationUnit = selectionTreeManager.getSelectedOrganisationUnit();
        
        if( organisationUnit == null )
        {
            System.out.println("Organisationunit is null");
        }
        
        programList = new ArrayList<Program>( reportService.getProgramsByOrgUnit( organisationUnit ) );
        
        System.out.println( "----OrganisationUnit is : --" + organisationUnit.getName() );
        
        System.out.println( " --Size of Program List is : ---" + programList.size() );
        
        */
       /* 
       Iterator<Program> iterator = programList.iterator();
        while( iterator.hasNext() )
        {
            Program program = iterator.next();
           
            if( program.getOrganisationUnits() == null || program.getOrganisationUnits().size() <= 0 )
            {
                iterator.remove();
            }
        }
      */  
        return SUCCESS;
    }
    
    /*
    public Collection<Program> getProgramsByOrgUnit( OrganisationUnit organisationUnit )
    {
        List<Program> tempProgramList = new ArrayList<Program>();

        for ( Program program : programService.getAllPrograms() )
        {
            if ( program.getOrganisationUnits().contains( organisationUnit ) )
            {
                tempProgramList.add( program );
            }
        }

        return tempProgramList;
    }
    */
    
}

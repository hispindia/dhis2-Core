package org.hisp.dhis.inspectionmanagement.schedulinginspections.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.jexl2.UnifiedJEXL.Exception;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.program.ProgramStage;
import org.hisp.dhis.program.ProgramStageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 */
public class GetProgramStageListAction implements Action
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    @Autowired
    private ProgramService programService;

    @Autowired
    private ProgramStageService programStageService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    // -------------------------------------------------------------------------
    // Input & output
    // -------------------------------------------------------------------------

    private Integer programId;
    
    public void setProgramId( Integer programId )
    {
        this.programId = programId;
    }

    private List<ProgramStage> programStages = new ArrayList<ProgramStage>();
    
    public List<ProgramStage> getProgramStages()
    {
        return programStages;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------
    public String execute()
        throws Exception
    {
        programStages = new ArrayList<ProgramStage>();
        String programStageName = "Inspection";
        
        if ( programId != null && programId != 0)
        {
            Program program = programService.getProgram( programId );
            
            //programStages = new ArrayList<ProgramStage>( program.getProgramStages() );
            
            //programStages = new ArrayList<ProgramStage>( program.getProgramStages() );
            programStages = new ArrayList<ProgramStage>( getProgramsByProgramStageName( programId, programStageName ) );
            
        }

        return SUCCESS;
    }

    // --------------------------------------------------------------------------------
    // Get Program List By Program ProgramStage Name
    // --------------------------------------------------------------------------------
    public List<ProgramStage> getProgramsByProgramStageName( Integer programId, String programStageName )
    {
        List<ProgramStage> programStageList = new ArrayList<ProgramStage>();

        try
        {
            String query = "SELECT programstageid from programstage " + " WHERE programid = " + programId + 
                            " and name ILIKE '%" + programStageName + "%' ";

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );

            while ( rs.next() )
            {
                Integer psId = rs.getInt( 1 );

                if ( psId != null )
                {
                    // System.out.println( " psiId -- " + psiId );
                    ProgramStage programStage = programStageService.getProgramStage( psId );
                    programStageList.add( programStage );
                }
            }

            return programStageList;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Illegal ProgramStage id", e );
        }
    }    
    
    
}

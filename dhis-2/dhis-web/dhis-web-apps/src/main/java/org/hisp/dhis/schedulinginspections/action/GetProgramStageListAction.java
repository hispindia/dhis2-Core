package org.hisp.dhis.schedulinginspections.action;

import java.util.ArrayList;
import java.util.List;

import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.program.ProgramStage;
import org.hisp.dhis.program.ProgramStageService;
import org.springframework.beans.factory.annotation.Autowired;

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
        
        if ( programId != null && programId != 0)
        {
            Program program = programService.getProgram( programId );
            
            programStages = new ArrayList<ProgramStage>( program.getProgramStages() );
            
        }

        return SUCCESS;
    }

}

package org.hisp.dhis.reports.activeplan.action;

import java.util.Date;

import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 * 
 * @version ActivePlanDueDatesReportResultAction.javaAug 28, 2012 2:34:11 PM
 */

public class ActivePlanDueDatesReportResultAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    /*
    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    private ProgramService programService;
     
    public void setProgramService( ProgramService programService ) 
    {
        this.programService = programService; 
    }
 
    private ReportService reportService;

    public void setReportService( ReportService reportService )
    {
        this.reportService = reportService;
    }



    private PatientAttributeValueService patientAttributeValueService;

    public void setPatientAttributeValueService( PatientAttributeValueService patientAttributeValueService )
    {
        this.patientAttributeValueService = patientAttributeValueService;
    }

    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate( JdbcTemplate jdbcTemplate )
    {
        this.jdbcTemplate = jdbcTemplate;
    }
    */
    
    // -------------------------------------------------------------------------
    // input output Getter & Setter
    // -------------------------------------------------------------------------
    /*
    private int ouIDTB;

    public void setOuIDTB( int ouIDTB )
    {
        this.ouIDTB = ouIDTB;
    }
    
    
    private String ouIDTB;
    
    public void setOuIDTB( String ouIDTB )
    {
        this.ouIDTB = ouIDTB;
    }

    private OrganisationUnit orgUnit;

    public OrganisationUnit getOrgUnit()
    {
        return orgUnit;
    }

    private String programList;
     
    public void setProgramList( String programList ) 
    { 
        this.programList =      programList; 
    }
     
    private List<Patient> patientList;

    public List<Patient> getPatientList()
    {
        return patientList;
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

    private I18nFormat format;

    public void setFormat( I18nFormat format )
    {
        this.format = format;
    }

    private Date sDate;

    private Date eDate;

    private String excelTemplateName;

    public void setExcelTemplateName( String excelTemplateName )
    {
        this.excelTemplateName = excelTemplateName;
    }

    private String xmlTemplateName;

    public void setXmlTemplateName( String xmlTemplateName )
    {
        this.xmlTemplateName = xmlTemplateName;
    }

    private String inputTemplatePath;

    private String outputReportPath;

    private String raFolderName;

    private String fileName;

    public String getFileName()
    {
        return fileName;
    }

    private InputStream inputStream;

    public InputStream getInputStream()
    {
        return inputStream;
    }

    private List<Patient> finalPatientList;

    public List<Patient> getFinalPatientList()
    {
        return finalPatientList;
    }
    
    private Map<Integer, String> patientSystemIdentifierValueMap;
    
    private Map<String, String> patientAttributeValueMap;
    
    private Map<String, String> programStageDataElementValueMap;
    
    //private List<String> followUpDoneList;
    
    //private Map<Integer, List<String>> patientfollowUpDoneListMap;
    
    private Map<Integer, Integer> patientfollowUpDoneListCountMap;
    
    int dischargeDE;
    int followUpDE;
    int dischargeStage;
    int followUpstage;
    */
    
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public String execute()
        throws Exception
    {

/*       
         Initialization 
        finalPatientList = new ArrayList<Patient>();
        patientSystemIdentifierValueMap = new HashMap<Integer, String>();
        patientAttributeValueMap = new HashMap<String, String>();
        programStageDataElementValueMap = new HashMap<String, String>();
        
        //followUpDoneList = new ArrayList<String>();
        //patientfollowUpDoneListMap = new HashMap<Integer, List<String>>();
        patientfollowUpDoneListCountMap = new HashMap<Integer, Integer>();
        
        orgUnit = organisationUnitService.getOrganisationUnit( ouIDTB );

        Program program = programService.getProgram( Integer.parseInt(  programList ) );

        // patientList = new ArrayList<Patient>(
        // reportService.getPatientByOrgUnit( orgUnit ) );

        // System.out.println(
        // "---Size of Patient List in selected orgUnit is   : " +
        // patientList.size() );
        
        // Period Info
        // SimpleDateFormat dayFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "dd-MM-yyyy" );
        
        sDate = format.parseDate( startDate );
        eDate = format.parseDate( endDate );
        
        String deCodesXMLFileName = xmlTemplateName;
        
        List<Report_inDesign> reportDesignHeaderList = reportService.getReportDesignForHeader( deCodesXMLFileName );
        
        Iterator<Report_inDesign> reportDesignHeaderIterator = reportDesignHeaderList.iterator();
        while ( reportDesignHeaderIterator.hasNext() )
        {
            Report_inDesign report_inDesign = (Report_inDesign) reportDesignHeaderIterator.next();
            String deCodeString = report_inDesign.getExpression();
            String sType = report_inDesign.getStype();
            
            if ( sType.equalsIgnoreCase( "dischargeStageDataElement" ) )
            {
                String[] tempDischargeStageDe = deCodeString.split( ":" );
                dischargeStage = Integer.parseInt( tempDischargeStageDe[0] );
                dischargeDE = Integer.parseInt( tempDischargeStageDe[1] );
                
            }
            
            if ( sType.equalsIgnoreCase( "followUpStageDataElement" ) )
            {
                String[] tempFollowUpStageDe = deCodeString.split( ":" );
                followUpstage = Integer.parseInt( tempFollowUpStageDe[0] );
                followUpDE = Integer.parseInt( tempFollowUpStageDe[1] );
            }
        }
        //System.out.println( " dischargeDE id is :" + dischargeDE  + " dischargeStage is :" + dischargeStage  + " followUpDE id is :" + followUpDE  + " followUpstage is :" + followUpstage );     

        patientList = new ArrayList<Patient>( reportService.getPatientByOrgUnitAndProgram( orgUnit, program ) );
        
        System.out.println( "Report Generation Start Time is : \t" + new Date() );
        
        //System.out.println( "Initial Patient List Size is : \t" + patientList.size() ); 
        
        for( Patient tempPatient : patientList)
        {
            String systemIdentifier = "";

            PatientIdentifierType idType = null;

            for ( PatientIdentifier identifier : tempPatient.getIdentifiers() )
            {
                idType = identifier.getIdentifierType();

                if ( idType != null )
                {
                }
                else
                {
                    systemIdentifier = identifier.getIdentifier();
                }
                patientSystemIdentifierValueMap.put( tempPatient.getId(), systemIdentifier );
            }
            
            
            for ( PatientAttribute patientAttribute : tempPatient.getAttributes() )
            {
                
                patientAttributeValueMap.put( tempPatient.getId() + ":" + patientAttribute.getId(), PatientAttributeValue.UNKNOWN );
            }
             
            
            Collection<PatientAttributeValue> patientAttributeValues = patientAttributeValueService
                .getPatientAttributeValues( tempPatient );

            for ( PatientAttributeValue patientAttributeValue : patientAttributeValues )
            {
                if ( PatientAttribute.TYPE_COMBO.equalsIgnoreCase( patientAttributeValue.getPatientAttribute()
                    .getValueType() ) )
                {
                   
                    patientAttributeValueMap.put( tempPatient.getId() + ":" + patientAttributeValue.getPatientAttribute().getId(),
                        patientAttributeValue.getPatientAttributeOption().getName() );
                }
                else
                {
                    patientAttributeValueMap.put( tempPatient.getId() + ":" + patientAttributeValue.getPatientAttribute().getId(),
                        patientAttributeValue.getValue() );
                }
            }
            
            String query = " SELECT patientdatavalue.programstageinstanceid,programstageinstance.programstageid ,dataelementid,value from patientdatavalue " 
                + " INNER JOIN programstageinstance on patientdatavalue.programstageinstanceid = programstageinstance.programstageinstanceid "
                + "INNER JOIN programinstance on programstageinstance.programinstanceid = programinstance.programinstanceid "
                 + "WHERE programstageinstance.programstageid = "+ dischargeStage + " and dataelementid = "+ dischargeDE +" and programinstance.patientid = " + tempPatient.getId() + " ORDER BY executiondate ";
            
            String query = "SELECT patientdatavalue.programstageinstanceid,programstageinstance.programstageid,dataelementid,value,programstageinstance.executiondate from patientdatavalue "
                + " INNER JOIN programstageinstance on patientdatavalue.programstageinstanceid = programstageinstance.programstageinstanceid "
                + " INNER JOIN programinstance on programstageinstance.programinstanceid = programinstance.programinstanceid "
                + " WHERE programinstance.patientid = " + tempPatient.getId() + " ORDER BY executiondate";
             
            SqlRowSet sqlResultSet = jdbcTemplate.queryForRowSet( query );

            if ( sqlResultSet != null )
            {
                
                
                sqlResultSet.beforeFirst();
                while ( sqlResultSet.next() )
                {
                    String programStageDataElement = "";

                    // int programStageInstanceId = sqlResultSet.getInt( 1 );
                    int programStageId = sqlResultSet.getInt( 2 );
                    int dataElementId = sqlResultSet.getInt( 3 );
                    String deValue = sqlResultSet.getString( 4 );

                    programStageDataElement = tempPatient.getId() + ":" + programStageId + ":" + dataElementId;

                    if ( deValue != null && !deValue.trim().equals( "" ) )
                    {
                        Date tempDate = format.parseDate( deValue );
                        Calendar tempSDate = Calendar.getInstance();
                        tempSDate.setTime( tempDate );
                        tempSDate.add( Calendar.DATE, 15 );
                        Date followUpDate = tempSDate.getTime();
                        int flag = 0;
                        //System.out.println( " Patient id is : \t" + tempPatient.getId()  + " deValue is : \t" + deValue  +  " followUp Date is : \t" + followUpDate ); 
                        if ( (followUpDate.compareTo( sDate ) >= 0) && (followUpDate.compareTo( eDate ) <= 0) )
                        {
                            flag = 1;
                            tempSDate = Calendar.getInstance();
                            tempSDate.setTime( tempDate );
                            tempSDate.add( Calendar.DATE, 15 );
                            followUpDate = tempSDate.getTime();
                        }
                        if ( (followUpDate.compareTo( sDate ) >= 0) && (followUpDate.compareTo( eDate ) <= 0) )
                        {
                            flag = 1;
                            tempSDate = Calendar.getInstance();
                            tempSDate.setTime( tempDate );
                            tempSDate.add( Calendar.MONTH, 1 );
                            followUpDate = tempSDate.getTime();
                        }
                        if ( (followUpDate.compareTo( sDate ) >= 0) && (followUpDate.compareTo( eDate ) <= 0) )
                        {
                            flag = 1;
                            tempSDate = Calendar.getInstance();
                            tempSDate.setTime( tempDate );
                            tempSDate.add( Calendar.DATE, 45 );
                            followUpDate = tempSDate.getTime();
                        }
                        if ( (followUpDate.compareTo( sDate ) >= 0) && (followUpDate.compareTo( eDate ) <= 0) )
                        {
                            flag = 1;
                            tempSDate = Calendar.getInstance();
                            tempSDate.setTime( tempDate );
                            tempSDate.add( Calendar.MONTH, 3 );
                            followUpDate = tempSDate.getTime();
                        }
                        if ( (followUpDate.compareTo( sDate ) >= 0) && (followUpDate.compareTo( eDate ) <= 0) )
                        {
                            flag = 1;
                            tempSDate = Calendar.getInstance();
                            tempSDate.setTime( tempDate );
                            tempSDate.add( Calendar.YEAR, 1 );
                            followUpDate = tempSDate.getTime();
                        }

                        if( flag == 1 )
                        {
                            finalPatientList.add( tempPatient );
                        }
                        //finalPatientList.add( tempPatient );
                        programStageDataElementValueMap.put( programStageDataElement, deValue );
                    }
                    
                }
            }
            
           
            String query2 = "SELECT programstageinstance.executiondate from programstageinstance "
                + "INNER JOIN programinstance on programstageinstance.programinstanceid = programinstance.programinstanceid "
                  + " WHERE programstageinstance.programstageid = 9 and programinstance.patientid = " + tempPatient.getId() + " ORDER BY executiondate" ;
            
            
            SqlRowSet sqlResultSet2 = jdbcTemplate.queryForRowSet( query2 );

            if ( sqlResultSet2 != null )
            {
                List<String> followUpDoneList  = new ArrayList<String>();
                sqlResultSet2.beforeFirst();
                while ( sqlResultSet2.next() )
                {
                    String executiondate = sqlResultSet2.getString( 1 );
                    if ( executiondate != null && executiondate != "" )
                    {
                        followUpDoneList.add( executiondate );
                    }
                }
                patientfollowUpDoneListMap.put( tempPatient.getId(), followUpDoneList );
            }
            
            
            
            
            String query2 = "SELECT count( executiondate ) from programstageinstance "
                + "INNER JOIN programinstance on programstageinstance.programinstanceid = programinstance.programinstanceid "
                  + " WHERE programstageinstance.programstageid = "+ followUpstage + " and programinstance.patientid = " + tempPatient.getId() + " ORDER BY executiondate" ;
            
            SqlRowSet sqlResultSet2 = jdbcTemplate.queryForRowSet( query2 );
            
            if ( sqlResultSet2 != null )
            {
                int executionDateCount;
                sqlResultSet2.beforeFirst();
                while ( sqlResultSet2.next() )
                {
                    Integer tempExecutionDateCount = sqlResultSet2.getInt( 1 );
                    if ( tempExecutionDateCount != null && tempExecutionDateCount != 0 )
                    {
                        executionDateCount = tempExecutionDateCount;
                    }
                    else
                    {
                        executionDateCount = 0;
                    }
                    patientfollowUpDoneListCountMap.put( tempPatient.getId(), executionDateCount );
                }
            }
        }
        //System.out.println( "final Patient List Size is : \t" + finalPatientList.size() ); 
        
        // System.out.println(
        // "---Size of Patient List in selected orgUnit and Program is   : " +
        // patientList.size() );

        raFolderName = reportService.getRAFolderName();

        String reportFileNameTB = excelTemplateName;

        //System.out.println( "Report Generation Start Time is : \t" + new Date() );

        inputTemplatePath = System.getenv( "DHIS2_HOME" ) + File.separator + raFolderName + File.separator + "template"
            + File.separator + reportFileNameTB;
        outputReportPath = System.getenv( "DHIS2_HOME" ) + File.separator + Configuration_IN.DEFAULT_TEMPFOLDER;

        File newdir = new File( outputReportPath );
        if ( !newdir.exists() )
        {
            newdir.mkdirs();
        }
        outputReportPath += File.separator + UUID.randomUUID().toString() + ".xls";

        //String deCodesXMLFileName = xmlTemplateName;

        List<Report_inDesign> reportDesignList = reportService.getReportDesignForTracker( deCodesXMLFileName );

        
         * for ( Patient patient : patientList ) { System.out.println(
         * " patient Name is  : " + patient.getFullName() + "--patient ID is " +
         * patient.getId() ); }
         * 
         * patientList = new ArrayList<Patient>(
         * reportService.getPatientByOrgUnitAndProgram( orgUnit, program ) );
         * 
         * System.out.println(
         * "---Size of Patient List in selected orgUnit and Program is   : " +
         * patientList.size() );
         * 
         * for ( Patient patient : patientList ) { System.out.println(
         * " patient Name is  : " + patient.getFullName() + "--patient ID is " +
         * patient.getId() ); }
         
        // SimpleDateFormat dayFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        
        // System.out.println(" Start Date is  --- " + startDate +
        // "-- End date is ---- " + endDate );

        // System.out.println(" Start Date is in date   --- " + sDate +
        // "-- End date is in date ---- " + eDate );

        // if ( sDate > eDate )
        
         * if ( ( sDate.compareTo( eDate ) >= 0 ) ) {
         * System.out.println(" Start Date is greter or equal"); } else if ((
         * sDate.compareTo( eDate ) < 0 )) {
         * System.out.println(" End Date is greter"); } else {
         * System.out.println("Both dates are equal"); }
         

        Workbook templateWorkbook = Workbook.getWorkbook( new File( inputTemplatePath ) );
        WritableWorkbook outputReportWorkbook = Workbook
            .createWorkbook( new File( outputReportPath ), templateWorkbook );

        // Cell formatting
        WritableCellFormat wCellformat = new WritableCellFormat();
        wCellformat.setBorder( Border.ALL, BorderLineStyle.THIN );
        wCellformat.setAlignment( Alignment.CENTRE );
        wCellformat.setVerticalAlignment( VerticalAlignment.CENTRE );
        wCellformat.setWrap( false );

        WritableCellFormat deWCellformat = new WritableCellFormat();
        deWCellformat.setBorder( Border.ALL, BorderLineStyle.THIN );
        deWCellformat.setAlignment( Alignment.CENTRE );
        deWCellformat.setVerticalAlignment( VerticalAlignment.JUSTIFY );
        deWCellformat.setWrap( true );

        int patientCount = 1;
        int followUpListSize = 0;
        int rowNo = 0;
        //Iterator<Patient> patient = patientList.iterator();
        Iterator<Patient> patient = finalPatientList.iterator();
        while ( patient.hasNext() )
        {
            Patient currentPatient = (Patient) patient.next();
            
            //List<String> tempfollowUpList = new ArrayList< String >( patientfollowUpDoneListCountMap.get( currentPatient.getId() ));
            followUpListSize = patientfollowUpDoneListCountMap.get( currentPatient.getId() );
            
           // System.out.println( "Patient Id is : " + currentPatient.getId() + "--Patient Name is : " + currentPatient.getFullName()  + "--follow Up List Size is :" + followUpListSize ); 
            
            
            String systemIdentifier = "";

            PatientIdentifierType idType = null;

            for ( PatientIdentifier identifier : currentPatient.getIdentifiers() )
            {
                idType = identifier.getIdentifierType();

                if ( idType != null )
                {
                }
                else
                {
                    systemIdentifier = identifier.getIdentifier();
                }
            }
            
            
            Map<Integer, String> patientAttributeValueMap = new HashMap<Integer, String>();

            for ( PatientAttribute patientAttribute : currentPatient.getAttributes() )
            {
                patientAttributeValueMap.put( patientAttribute.getId(), PatientAttributeValue.UNKNOWN );
            }

            Collection<PatientAttributeValue> patientAttributeValues = patientAttributeValueService
                .getPatientAttributeValues( currentPatient );

            for ( PatientAttributeValue patientAttributeValue : patientAttributeValues )
            {
                if ( PatientAttribute.TYPE_COMBO.equalsIgnoreCase( patientAttributeValue.getPatientAttribute()
                    .getValueType() ) )
                {
                    patientAttributeValueMap.put( patientAttributeValue.getPatientAttribute().getId(),
                        patientAttributeValue.getPatientAttributeOption().getName() );
                }
                else
                {
                    patientAttributeValueMap.put( patientAttributeValue.getPatientAttribute().getId(),
                        patientAttributeValue.getValue() );
                }
            }

             
           
            Map<String, String> programStageDataElementValueMap = new HashMap<String, String>();

            String query = "SELECT patientdatavalue.programstageinstanceid,programstageinstance.programstageid,dataelementid,value,programstageinstance.executiondate from patientdatavalue "
                + " INNER JOIN programstageinstance on patientdatavalue.programstageinstanceid = programstageinstance.programstageinstanceid "
                + " INNER JOIN programinstance on programstageinstance.programinstanceid = programinstance.programinstanceid "
                + " WHERE programinstance.patientid = " + currentPatient.getId() + " ORDER BY executiondate";

            SqlRowSet sqlResultSet = jdbcTemplate.queryForRowSet( query );

            if ( sqlResultSet != null )
            {
                sqlResultSet.beforeFirst();
                while ( sqlResultSet.next() )
                {
                    String programStageDataElement = "";

                    // int programStageInstanceId = sqlResultSet.getInt( 1 );
                    int programStageId = sqlResultSet.getInt( 2 );
                    int dataElementId = sqlResultSet.getInt( 3 );
                    String deValue = sqlResultSet.getString( 4 );

                    programStageDataElement = programStageId + ":" + dataElementId;

                    programStageDataElementValueMap.put( programStageDataElement, deValue );

                }
            }
            
            int count1 = 0;
            //int followUpListSize = 0;
            Iterator<Report_inDesign> reportDesignIterator = reportDesignList.iterator();
            while ( reportDesignIterator.hasNext() )
            {
                Report_inDesign report_inDesign = (Report_inDesign) reportDesignIterator.next();

                String sType = report_inDesign.getStype();
                String deCodeString = report_inDesign.getExpression();

                int tempRowNo = report_inDesign.getRowno();
                int tempColNo = report_inDesign.getColno();
                int sheetNo = report_inDesign.getSheetno();
                // WritableSheet sheet0 = outputReportWorkbook.getSheet( sheetNo
                // );
                
                WritableSheet sheet = outputReportWorkbook.getSheet( sheetNo );

                int flag = 0;
                int followUpFlag = 0;
                
                //followUpListSize = patientfollowUpDoneListCountMap.get( currentPatient.getId() );

                String tempStr = "";
                String dischargeDate = "";

                if ( deCodeString.equalsIgnoreCase( "slno" ) )
                {
                    tempStr = "" + patientCount;
                }
                else if ( deCodeString.equalsIgnoreCase( "FACILITY" ) )
                {
                    tempStr = orgUnit.getName();
                }

                else if ( deCodeString.equalsIgnoreCase( "FACILITYP" ) )
                {
                    tempStr = orgUnit.getParent().getName();
                }
                else if ( deCodeString.equalsIgnoreCase( "FACILITYPP" ) )
                {
                    tempStr = orgUnit.getParent().getParent().getName();
                }

                else if ( deCodeString.equalsIgnoreCase( "PERIOD-FROM" ) )
                {
                    tempStr = simpleDateFormat.format( sDate );
                }
                else if ( deCodeString.equalsIgnoreCase( "PERIOD-TO" ) )
                {
                    tempStr = simpleDateFormat.format( eDate );
                }
                else if ( deCodeString.equalsIgnoreCase( "PATIENTNAME" ) )
                {
                    tempStr = "";
                }
                else if ( deCodeString.equalsIgnoreCase( "GENDER" ) )
                {
                    tempStr = "";
                }
                else if ( deCodeString.equalsIgnoreCase( "AGE" ) )
                {
                    
                    if ( currentPatient.getDobType() == 'V' || currentPatient.getDobType() == 'D' )
                    {
                        tempStr = simpleDateFormat.format( currentPatient.getBirthDate() );
                    }
                    else
                    {
                        tempStr = currentPatient.getAge();
                    }
                    
                    
                }

                else if ( deCodeString.equalsIgnoreCase( "PHONENO" ) )
                {
                    tempStr = "";
                }

                else if ( deCodeString.equalsIgnoreCase( "SYSTEMIDENTIFIER" ) )
                {
                    //tempStr = systemIdentifier;
                    tempStr = patientSystemIdentifierValueMap.get( currentPatient.getId() ); 
                    
                }

                else if ( deCodeString.equalsIgnoreCase( "NA" ) )
                {
                    tempStr = "";
                }

                else if ( sType.equalsIgnoreCase( "attributes" ) )
                {
                    tempStr = " ";
                    
                    for ( PatientAttribute patientAttribute : currentPatient.getAttributes() )
                    {
                        if ( patientAttribute.getId() == Integer.parseInt( deCodeString ) )
                        {
                            try
                            {
                                tempStr = patientAttributeValueMap.get( currentPatient.getId() + ":" + patientAttribute.getId() );
                            }
                            catch ( Exception e )
                            {
                            }
                            break;
                        }
                    }
                    
                    
                }

                else if ( sType.equalsIgnoreCase( "1stfollowup" ) )
                {
                    tempStr = programStageDataElementValueMap.get( currentPatient.getId() + ":" + deCodeString );
                    dischargeDate = programStageDataElementValueMap.get( currentPatient.getId() + ":" + deCodeString );
                    if ( tempStr != null )
                    {
                        Date tempDate = format.parseDate( tempStr );
                        Calendar tempSDate = Calendar.getInstance();
                        tempSDate.setTime( tempDate );
                        tempSDate.add( Calendar.DATE, 15 );
                        // tempStr = "" + tempSDate.get(Calendar.YEAR) + "-" + (
                        // tempSDate.get(Calendar.MONTH) + 1) + "-"
                        // +tempSDate.get(Calendar.DATE);
                        Date firstFollowUpDate = tempSDate.getTime();

                        // Date date = format.parseDate( dayFormat.format(
                        // firstFollowUpDate ) );

                        // System.out.println(" Inside First followup -- " +
                        // simpleDateFormat.format( firstFollowUpDate.getTime())
                        // );
                        
                        if( followUpListSize == 1 )
                        {
                            followUpFlag = 1;
                        }
                         
                        if( ( followUpListSize - 1 ) >= 0 )
                        {
                            followUpFlag = 1;
                        }
                        
                        if ( (firstFollowUpDate.compareTo( sDate ) >= 0) && (firstFollowUpDate.compareTo( eDate ) <= 0) )
                        {
                            // System.out.println(" Start Date is greter or equal");
                            flag = 1;
                           
                            tempStr = simpleDateFormat.format( tempSDate.getTime() );
                        }
                        else
                        {
                            tempStr = simpleDateFormat.format( tempSDate.getTime() );
                        }
                    }
                    else
                    {
                        tempStr = "";
                        dischargeDate = " ";
                    }
                    //System.out.println( "Id is : " + currentPatient.getId() + "-Name is : " + currentPatient.getFullName()  + "-followUpFlag :" + followUpFlag + "-flag :" + flag ); 
                }
                else if ( sType.equalsIgnoreCase( "2ndfollowup" ) )
                {
                    tempStr = programStageDataElementValueMap.get( currentPatient.getId() + ":" + deCodeString );
                    dischargeDate = programStageDataElementValueMap.get( currentPatient.getId() + ":" + deCodeString );

                    if ( tempStr != null )
                    {
                        Date tempDate = format.parseDate( tempStr );
                        Calendar tempSDate = Calendar.getInstance();
                        tempSDate.setTime( tempDate );
                        tempSDate.add( Calendar.MONTH, 1 );
                        // tempStr = "" + tempSDate.get(Calendar.YEAR) + "-" + (
                        // tempSDate.get(Calendar.MONTH) + 1) + "-"
                        // +tempSDate.get(Calendar.DATE);
                        Date secondFollowUpDate = tempSDate.getTime();
                        // System.out.println(" Inside Second followup -- " +
                        // simpleDateFormat.format(
                        // secondFollowUpDate.getTime()) );
                        
                        
                        if( ( followUpFlag == 0 ) && ( followUpListSize == 2 ) )
                        {
                            followUpFlag = 1;
                        }
                        
                        if( ( followUpListSize-2 ) >= 0  )
                        {
                            followUpFlag = 1;
                        }
                        
                        
                        if ( (flag == 0) && (secondFollowUpDate.compareTo( sDate ) >= 0)
                            && (secondFollowUpDate.compareTo( eDate ) <= 0) )
                        {
                            // System.out.println(" Start Date is greter or equal");
                            flag = 1;
                           
                            tempStr = simpleDateFormat.format( tempSDate.getTime() );
                        }
                        else
                        {
                            tempStr = simpleDateFormat.format( tempSDate.getTime() );
                        }

                        // tempStr = simpleDateFormat.format(
                        // tempSDate.getTime() );
                    }
                    else
                    {
                        tempStr = " ";
                        dischargeDate = " ";
                    }
                    //System.out.println( "Id is : " + currentPatient.getId() + "-Name is : " + currentPatient.getFullName()  + "-followUpFlag :" + followUpFlag + "-flag :" + flag ); 
                }
                else if ( sType.equalsIgnoreCase( "3rdfollowup" ) )
                {

                    tempStr = programStageDataElementValueMap.get( currentPatient.getId() + ":" + deCodeString );
                    dischargeDate = programStageDataElementValueMap.get( currentPatient.getId() + ":" + deCodeString );

                    if ( tempStr != null )
                    {
                        Date tempDate = format.parseDate( tempStr );
                        Calendar tempSDate = Calendar.getInstance();
                        tempSDate.setTime( tempDate );
                        tempSDate.add( Calendar.DATE, 45 );

                        Date thirdFollowUpDate = tempSDate.getTime();
                        // System.out.println(" Inside third followup -- " +
                        // simpleDateFormat.format( thirdFollowUpDate.getTime())
                        // );
                        
                        if( ( followUpFlag == 0) && ( followUpListSize == 3 ) )
                        {
                            followUpFlag = 1;
                        }
                        
                        if(  ( followUpListSize-3 ) >= 0 )
                        {
                            followUpFlag = 1;
                        }
                        if ( (flag == 0) && (thirdFollowUpDate.compareTo( sDate ) >= 0)
                            && (thirdFollowUpDate.compareTo( eDate ) <= 0) )
                        {
                            // System.out.println(" Start Date is greter or equal");
                            flag = 1;
                           
                            tempStr = simpleDateFormat.format( tempSDate.getTime() );
                        }
                        else
                        {
                            tempStr = simpleDateFormat.format( tempSDate.getTime() );
                        }
                        // tempStr = "" + tempSDate.get(Calendar.YEAR) + "-" + (
                        // tempSDate.get(Calendar.MONTH) + 1) + "-"
                        // +tempSDate.get(Calendar.DATE);
                        // tempStr = simpleDateFormat.format(
                        // tempSDate.getTime() );
                    }
                    else
                    {
                        tempStr = " ";
                        dischargeDate = " ";
                    }
                    //System.out.println( "Id is : " + currentPatient.getId() + "-Name is : " + currentPatient.getFullName()  + "-followUpFlag :" + followUpFlag + "-flag :" + flag ); 
                }

                else if ( sType.equalsIgnoreCase( "4thfollowup" ) )
                {
                    tempStr = programStageDataElementValueMap.get( currentPatient.getId() + ":" + deCodeString );
                    dischargeDate = programStageDataElementValueMap.get( currentPatient.getId() + ":" + deCodeString );

                    if ( tempStr != null )
                    {
                        Date tempDate = format.parseDate( tempStr );
                        Calendar tempSDate = Calendar.getInstance();
                        tempSDate.setTime( tempDate );
                        tempSDate.add( Calendar.MONTH, 3 );
                        // tempStr = "" + tempSDate.get(Calendar.DATE);
                        // tempStr = "" + tempSDate.get(Calendar.YEAR) + "-" + (
                        // tempSDate.get(Calendar.MONTH) + 1) + "-"
                        // +tempSDate.get(Calendar.DATE);

                        Date fourthFollowUpDate = tempSDate.getTime();
                        // System.out.println(" Inside fourth followup -- " +
                        // simpleDateFormat.format(
                        // fourthFollowUpDate.getTime()) );
                        
                        
                        if( ( followUpFlag == 0) && ( followUpListSize == 4 ) )
                        {
                            followUpFlag = 1;
                        }
                        
                        if( ( followUpListSize-4 ) >= 0 ) 
                        {
                            followUpFlag = 1;
                        }
                        
                        if ( (flag == 0) && (fourthFollowUpDate.compareTo( sDate ) >= 0)
                            && (fourthFollowUpDate.compareTo( eDate ) <= 0) )
                        {
                            // System.out.println(" Start Date is greter or equal");
                            flag = 1;

                            tempStr = simpleDateFormat.format( tempSDate.getTime() );
                        }
                        else
                        {
                            tempStr = simpleDateFormat.format( tempSDate.getTime() );
                        }

                        // tempStr = simpleDateFormat.format(
                        // tempSDate.getTime() );
                    }
                    else
                    {
                        tempStr = " ";
                        dischargeDate = " ";
                    }
                    //System.out.println( "Id is : " + currentPatient.getId() + "-Name is : " + currentPatient.getFullName()  + "-followUpFlag :" + followUpFlag + "-flag :" + flag ); 
                }

                else if ( sType.equalsIgnoreCase( "5thfollowup" ) )
                {

                    tempStr = programStageDataElementValueMap.get( currentPatient.getId() + ":" + deCodeString );
                    dischargeDate = programStageDataElementValueMap.get( currentPatient.getId() + ":" + deCodeString );

                    if ( tempStr != null )
                    {
                        Date tempDate = format.parseDate( tempStr );
                        Calendar tempSDate = Calendar.getInstance();
                        tempSDate.setTime( tempDate );
                        tempSDate.add( Calendar.YEAR, 1 );
                        // tempStr = "" + tempSDate.get(Calendar.DATE);
                        // tempStr = "" + tempSDate.get(Calendar.YEAR) + "-"
                        // +tempSDate.get(Calendar.MONTH) + "-"
                        // +tempSDate.get(Calendar.DATE);

                        Date fifthFollowUpDate = tempSDate.getTime();
                        // System.out.println(" Inside fifth followup -- " +
                        // simpleDateFormat.format( fifthFollowUpDate.getTime())
                        // );
                        
                        if( ( followUpFlag == 0) && ( followUpListSize == 5 ) )
                        {
                            followUpFlag = 1;
                        }
                        
                        
                        if( (  ( followUpListSize-5 ) >= 0 ) )
                        {
                            followUpFlag = 1;
                        }
                        
                        if ( (flag == 0) && (fifthFollowUpDate.compareTo( sDate ) >= 0)
                            && (fifthFollowUpDate.compareTo( eDate ) <= 0) )
                        {
                            // System.out.println(" Start Date is greter or equal");
                            flag = 1;
                           
                            tempStr = simpleDateFormat.format( tempSDate.getTime() );
                        }
                        else
                        {
                            tempStr = simpleDateFormat.format( tempSDate.getTime() );
                        }

                        // tempStr = simpleDateFormat.format(
                        // tempSDate.getTime() );
                    }
                    else
                    {
                        tempStr = " ";
                        dischargeDate = " ";
                    }
                    //System.out.println( "Id is : " + currentPatient.getId() + "-Name is : " + currentPatient.getFullName()  + "-followUpFlag :" + followUpFlag + "-flag :" + flag ); 
                    
                }

                // System.out.println( sType + " : " + deCodeString + " : " + tempStr + " -- FLAG -- : " + flag );
                // tempRowNo = patientCount+1;
                // WritableSheet sheet = outputReportWorkbook.getSheet( sheetNo
                // );
                
                
                if ( deCodeString.equalsIgnoreCase( "FACILITY" ) || deCodeString.equalsIgnoreCase( "FACILITYP" )
                    || deCodeString.equalsIgnoreCase( "FACILITYPP" ) || deCodeString.equalsIgnoreCase( "PERIOD-FROM" )
                    || deCodeString.equalsIgnoreCase( "PERIOD-TO" ) )
                {
                    
                }
                else
                {
                    tempRowNo += rowNo;
                }
                
                
                //System.out.println( "Patient Id is : " + currentPatient.getId() + "-Patient Name is : " + currentPatient.getFullName()  + "-followUpFlag is :" + followUpFlag + "-flag is :" + flag ); 
                
                if (  dischargeDate == null  || dischargeDate.equals( " " )  )
                {
                    
                    if ( deCodeString.equalsIgnoreCase( "slno" ) || sType.equalsIgnoreCase( "attributes" ) || deCodeString.equalsIgnoreCase( "SYSTEMIDENTIFIER" )
                        || deCodeString.equalsIgnoreCase( "PHONENO" ) || deCodeString.equalsIgnoreCase( "AGE" ) || deCodeString.equalsIgnoreCase( "GENDER" ) 
                        || deCodeString.equalsIgnoreCase( "PATIENTNAME" ) )
                    {
                        
                    }
                    else
                    {
                        tempRowNo += rowNo;
                    }
                    
                    //sheet.addCell( new Blank( tempColNo, tempRowNo, wCellformat ) );
                    
                }
                else
                {
                    
                    if ( deCodeString.equalsIgnoreCase( "FACILITY" ) || deCodeString.equalsIgnoreCase( "FACILITYP" )
                        || deCodeString.equalsIgnoreCase( "FACILITYPP" ) || deCodeString.equalsIgnoreCase( "PERIOD-FROM" )
                        || deCodeString.equalsIgnoreCase( "PERIOD-TO" ) )
                    {
                        
                    }
                    else
                    {
                        tempRowNo += rowNo;
                    }
                    
                    //WritableSheet sheet = outputReportWorkbook.getSheet( sheetNo );
                    if ( followUpFlag == 1 )
                    {
                        try
                        {
                            sheet.addCell( new Number( tempColNo, tempRowNo, Double.parseDouble( tempStr ),getCellFormat2() ) );
                        }
                        catch ( Exception e )
                        {
                            sheet.addCell( new Label( tempColNo, tempRowNo, tempStr, getCellFormat2() ) );
                        }
                    }
                    
                    else if ( flag == 1 )
                    {
                        try
                        {
                            sheet.addCell( new Number( tempColNo, tempRowNo, Double.parseDouble( tempStr ),getCellFormat1() ) );
                        }
                        catch ( Exception e )
                        {
                            sheet.addCell( new Label( tempColNo, tempRowNo, tempStr, getCellFormat1() ) );
                        }
                    }
                    
                   else
                   {
                        if ( deCodeString.equalsIgnoreCase( "SYSTEMIDENTIFIER" ) )
                        {
                            sheet.addCell( new Label( tempColNo, tempRowNo, tempStr, wCellformat ) );
                        }
    
                        else
                        {
                            try
                            {
                                sheet.addCell( new Number( tempColNo, tempRowNo, Double.parseDouble( tempStr ),
                                    wCellformat ) );
                            }
                            catch ( Exception e )
                            {
                                sheet.addCell( new Label( tempColNo, tempRowNo, tempStr, wCellformat ) );
                            }
                        }
                   }
                }

                count1++;
            }

            rowNo++;
            patientCount++;
        }
        outputReportWorkbook.write();
        outputReportWorkbook.close();
        fileName = reportFileNameTB.replace( ".xls", "" );
        fileName += "_" + orgUnit.getShortName() + ".xls";
        //fileName = "SNCUDueDateReport.xls";
        File outputReportFile = new File( outputReportPath );
        inputStream = new BufferedInputStream( new FileInputStream( outputReportFile ) );
        outputReportFile.deleteOnExit();*/

        System.out.println( "Report Generation End Time is : \t" + new Date() );

        return SUCCESS;
    }

    public WritableCellFormat getCellFormat1()
        throws Exception
    {
        WritableFont arialBold = new WritableFont( WritableFont.ARIAL, 10, WritableFont.NO_BOLD );
        WritableCellFormat wCellformat = new WritableCellFormat( arialBold );

        wCellformat.setBorder( Border.ALL, BorderLineStyle.THIN );
        wCellformat.setAlignment( Alignment.CENTRE );
        wCellformat.setBackground( Colour.YELLOW );
        wCellformat.setVerticalAlignment( VerticalAlignment.CENTRE );
        wCellformat.setWrap( true );
        return wCellformat;
    }
    
    public WritableCellFormat getCellFormat2()
        throws Exception
    {
        WritableFont arialBold = new WritableFont( WritableFont.ARIAL, 10, WritableFont.NO_BOLD );
        WritableCellFormat wCellformat = new WritableCellFormat( arialBold );
    
        wCellformat.setBorder( Border.ALL, BorderLineStyle.THIN );
        wCellformat.setAlignment( Alignment.CENTRE );
        wCellformat.setBackground( Colour.GREEN );
        wCellformat.setVerticalAlignment( VerticalAlignment.CENTRE );
        wCellformat.setWrap( true );
        return wCellformat;
    }    
    
    
    
}

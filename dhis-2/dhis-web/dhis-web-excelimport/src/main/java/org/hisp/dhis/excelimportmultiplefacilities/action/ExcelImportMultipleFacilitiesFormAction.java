package org.hisp.dhis.excelimportmultiplefacilities.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

public class ExcelImportMultipleFacilitiesFormAction implements Action {

	// -------------------------------------------------------------------------
	// Dependencies
	// -------------------------------------------------------------------------

	@Autowired
	private PeriodService periodService;

	@Autowired
	private OrganisationUnitService organisationUnitService;

	@Autowired
	private DataSetService dataSetService;
	/*
	 * private ReportService reportService;
	 * 
	 * public void setReportService( ReportService reportService ) {
	 * this.reportService = reportService; }
	 */
	// -------------------------------------------------------------------------
	// Constants
	// -------------------------------------------------------------------------

	private final int ALL = 0;

	public int getALL() 
	{
		return ALL;
	}

	// private String raFolderName;

	// -------------------------------------------------------------------------
	// Properties
	// -------------------------------------------------------------------------

	private String message;

	public void setMessage(String message) 
	{
		this.message = message;
	}

	private Collection<OrganisationUnit> organisationUnits;

	public Collection<OrganisationUnit> getOrganisationUnits() 
	{
		return organisationUnits;
	}

	private Collection<Period> periods = new ArrayList<Period>();

	public Collection<Period> getPeriods() 
	{
		return periods;
	}

	private Collection<PeriodType> periodTypes;

	public Collection<PeriodType> getPeriodTypes() 
	{
		return periodTypes;
	}

	

	
	private Collection<DataSet> dataSetList = new ArrayList<DataSet>();

	public Collection<DataSet> getDataSetList()
	{
		return dataSetList;
	}

	// -------------------------------------------------------------------------
	// Action implementation
	// -------------------------------------------------------------------------

	public String execute() throws Exception 
	{

		dataSetList = new ArrayList<DataSet>( dataSetService.getAllDataSets() );


		Iterator<DataSet> allDsIterator = dataSetList.iterator();
		while ( allDsIterator.hasNext() )
		{
			DataSet ds = allDsIterator.next();
			//System.out.println(ds.getCode());

			//if( ds.getCode() != null )
			//{
			if(ds.getName().equalsIgnoreCase("SC HMIS Dataset") || ds.getName().equalsIgnoreCase("PHC HMIS Dataset") || ds.getName().equalsIgnoreCase("CHC HMIS Dataset") || ds.getName().equalsIgnoreCase("DH HMIS Dataset") )
			{

			}
			else
			{
				allDsIterator.remove();
			}
			//}

		}


		for( DataSet ds : dataSetList)
		{
			ds.getPeriodType().getName();
		}
		// raFolderName = reportService.getRAFolderName();

		/* Period Info */
//		periodTypes = periodService.getAllPeriodTypes();
//
//		Iterator<PeriodType> alldeIterator = periodTypes.iterator();
//		while ( alldeIterator.hasNext() )
//		{
//			PeriodType type = alldeIterator.next();
//			if (type.getName().equalsIgnoreCase("Monthly") ||
//					type.getName().equalsIgnoreCase("quarterly") ||
//					type.getName().equalsIgnoreCase("yearly"))
//			{
//				periods.addAll(periodService.getPeriodsByPeriodType(type));
//			}
//			else
//			{
//				alldeIterator.remove();
//			}
//		}

		System.out.println(message);
		return SUCCESS;
	}

}

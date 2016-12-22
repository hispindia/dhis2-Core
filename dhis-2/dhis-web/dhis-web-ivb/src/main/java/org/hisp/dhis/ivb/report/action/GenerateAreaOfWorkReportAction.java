package org.hisp.dhis.ivb.report.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hisp.dhis.attribute.AttributeValue;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.dataset.Section;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.lookup.Lookup;
import org.hisp.dhis.lookup.LookupService;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author Samta Bajpai
 */
public class GenerateAreaOfWorkReportAction
implements Action
{
	// -------------------------------------------------------------------------
	// Dependencies
	// -------------------------------------------------------------------------

	@Autowired
	private LookupService lookupService;


	private DataSetService dataSetService;

	public void setDataSetService( DataSetService dataSetService )
	{
		this.dataSetService = dataSetService;
	}

	private OrganisationUnitService organisationUnitService;

	public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
	{
		this.organisationUnitService = organisationUnitService;
	}

	private CurrentUserService currentUserService;

	public void setCurrentUserService( CurrentUserService currentUserService )
	{
		this.currentUserService = currentUserService;
	}

	private I18nService i18nService;

	public void setI18nService( I18nService service )
	{
		i18nService = service;
	}

	private MessageService messageService;

	public void setMessageService( MessageService messageService )
	{
		this.messageService = messageService;
	}

	private ConfigurationService configurationService;

	public void setConfigurationService( ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	private DataElementCategoryService categoryService;

	public void setCategoryService( DataElementCategoryService categoryService )
	{
		this.categoryService = categoryService;
	}

	private DataValueService dataValueService;

	public void setDataValueService( DataValueService dataValueService )
	{
		this.dataValueService = dataValueService;
	}
	@Autowired
	private SelectionTreeManager selectionTreeManager;

	// -------------------------------------------------------------------------
	// Getters / Setters
	// -------------------------------------------------------------------------

	private String userSource;

	public String getUserSource()
	{
		return userSource;
	}

	public void setUserSource( String userSource )
	{
		this.userSource = userSource;
	}
	private String orgUnitId;

	public void setOrgUnitId( String orgUnitId )
	{
		this.orgUnitId = orgUnitId;
	}

	public String getOrgUnitId()
	{
		return orgUnitId;
	}

	private String dataSetId;

	public String getDataSetId()
	{
		return dataSetId;
	}

	public void setDataSetId( String dataSetId )
	{
		this.dataSetId = dataSetId;
	}

	private List<Section> dataSetSections;

	public List<Section> getDataSetSections()
	{
		return dataSetSections;
	}
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
	private Map<String, DataValue> dataValueMap = new HashMap<String, DataValue>();

	public Map<String, DataValue> getDataValueMap()
	{
		return dataValueMap;
	}
	private DataSet datset;

	public DataSet getDatset()
	{
		return datset;
	}

	public void setDatset( DataSet datset )
	{
		this.datset = datset;
	}
	private OrganisationUnit organisationUnit;

	public OrganisationUnit getOrganisationUnit()
	{
		return organisationUnit;
	}

	private List<Integer> treeSelectedId = new ArrayList<Integer>();

	public void setTreeSelectedId(List<Integer> treeSelectedId) {
		this.treeSelectedId = treeSelectedId;
	}

	Set<Integer> percentageRequiredDe = new HashSet<Integer>();

	public Set<Integer> getPercentageRequiredDe() {
		return percentageRequiredDe;
	}

	// --------------------------------------------------------------------------
	// Action implementation
	// -------------------------------------------------------------------------- 


	public String execute()
	{  
		System.out.println( userSource );
		userName = currentUserService.getCurrentUser().getUsername();

		Lookup lookup1 = lookupService.getLookupByName(Lookup.IS_PERCENTAGE);
		int percentage_attribute_id = Integer.parseInt(lookup1.getValue());

		if ( i18nService.getCurrentLocale() == null )
		{
			language = "en";
		}
		else
		{
			language = i18nService.getCurrentLocale().getLanguage();
		}
		DataElementCategoryOptionCombo optionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();

		if(treeSelectedId.size() > 0)
		{
			organisationUnit = organisationUnitService.getOrganisationUnit(  orgUnitId  );
		}
		else if (selectionTreeManager.getReloadedSelectedOrganisationUnits() != null)
		{
			organisationUnit = selectionTreeManager.getSelectedOrganisationUnit();
		}
		else
		{
			return SUCCESS;
		}
		datset = dataSetService.getDataSet( Integer.parseInt( dataSetId ) );

		Collection<DataElement> dataElementList = new ArrayList<DataElement>();
		if(datset.getSections().size() > 0)
		{
			for(Section section : datset.getSections())
			{   
				dataElementList.addAll( section.getDataElements() ) ;
			}
		}
		else
		{
			dataElementList.addAll(  datset.getDataElements() ) ;
		}

		for( DataElement de : dataElementList )
		{
			DataValue dataValue = dataValueService.getLatestDataValue( de, optionCombo, organisationUnit ); 
			Set<AttributeValue> attrValueSet = new HashSet<AttributeValue>( de.getAttributeValues() );

			if( dataValue == null )
			{
				dataValue = new DataValue();            	
				dataValue.setSource( organisationUnit );
				dataValue.setDataElement( de );
				dataValue.setCategoryOptionCombo( optionCombo );
				dataValue.setValue( "" );
				dataValue.setComment( "" );                
			}


			for ( AttributeValue attValue : attrValueSet )
			{
				if ( attValue.getAttribute().getId() == percentage_attribute_id && attValue.getValue().equalsIgnoreCase( "true" ))
				{
					percentageRequiredDe.add(de.getId());
					System.out.println("Percentage required De :"+ de.getId());
				}
			}

			if( dataValue.getValue() == null )
			{
				dataValue.setValue("");
			}

			if( dataValue.getComment() == null )
			{
				dataValue.setComment("");
			}

			dataValueMap.put( de.getUid(), dataValue );
		}

		messageCount = (int) messageService.getUnreadMessageConversationCount();
		List<UserGroup> userGrps = new ArrayList<UserGroup>( currentUserService.getCurrentUser().getGroups() );
		if ( userGrps.contains( configurationService.getConfiguration().getFeedbackRecipients() ) )
		{
			adminStatus = "Yes";
		}
		else
		{
			adminStatus = "No";
		}

		return SUCCESS;

	}

}

package org.hisp.dhis.ivb.report.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.attribute.AttributeValue;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementGroup;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.dataset.Section;
import org.hisp.dhis.dataset.SectionService;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.lookup.Lookup;
import org.hisp.dhis.lookup.LookupService;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.hisp.dhis.ouwt.manager.OrganisationUnitSelectionManager;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserAuthorityGroup;
import org.hisp.dhis.user.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

public class GaviReportFormAction implements Action
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
	
	@Autowired
    private SelectionTreeManager selectionTreeManager;

    private OrganisationUnitSelectionManager selectionManager;

    public void setSelectionManager( OrganisationUnitSelectionManager selectionManager )
    {
        this.selectionManager = selectionManager;
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
    private DataSetService dataSetService;
    
    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }
    
    private DataElementService dataElementService;
    
    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }
    private OrganisationUnitGroupService organisationUnitGroupService;

    public void setOrganisationUnitGroupService( OrganisationUnitGroupService organisationUnitGroupService )
    {
        this.organisationUnitGroupService = organisationUnitGroupService;
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
    private ConstantService constantService;

    public void setConstantService( ConstantService constantService )
    {
        this.constantService = constantService;
    }

    @Autowired
    private SectionService sectionService;
    
    public void setSectionService( SectionService sectionService )
    {
        this.sectionService = sectionService;
    }
    
    @Autowired
    private DataElementService dataElementGroupService;
    
    @Autowired
    private LookupService lookupService;
    
    @Autowired
    private IVBUtil ivbUtil;
    
    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    private String language;

    public String getLanguage()
    {
        return language;
    }

    private String userName;

    public String getUserName()
    {
        return userName;
    }

    private List<Section> vaccineList = new ArrayList<Section>();
    
    public List<Section> getVaccineList()
    {
        return vaccineList;
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
    
    private String displayRestrictedDesCheckBox = "Y";
    
    public String getDisplayRestrictedDesCheckBox() 
    {
		return displayRestrictedDesCheckBox;
	}

	public String execute()
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
        selectionManager.clearSelectedOrganisationUnits();

        Set<Section> sections = new HashSet<Section>();
        Constant vaccineAttributeConstant = constantService.getConstantByName( "VACCINE_ATTRIBUTE" );
        DataSet vaccineIntroDataSet = dataSetService.getDataSet( "UezSPDbJYdG" );
        DataElementGroup gaviAppYearMonthDeGroup = dataElementService.getDataElementGroup( "TGexTg38NCs" );
        for( DataElement de : gaviAppYearMonthDeGroup.getMembers() )
        {
            Set<AttributeValue> dataElementAttributeValues = de.getAttributeValues();
            for ( AttributeValue deAttributeValue : dataElementAttributeValues )
            {
                if ( deAttributeValue.getAttribute().getId() == vaccineAttributeConstant.getValue() && deAttributeValue.getValue() != null )
                {
                    Section section = sectionService.getSectionByName( deAttributeValue.getValue(), vaccineIntroDataSet.getId() );
                    if( section != null )
                    {
                        sections.add( section );
                    }
                    break;
                }
            }
        }

        Set<OrganisationUnit> currentUserOrgUnits = new HashSet<OrganisationUnit>( currentUserService.getCurrentUser().getDataViewOrganisationUnits() );
        selectionTreeManager.setRootOrganisationUnits( currentUserOrgUnits );

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
        
        vaccineList.addAll( sections );
        
        Collections.sort(vaccineList , new Comparator<Section>() {
            public int compare(Section one, Section other) {
                return one.getDisplayName().trim() .compareTo(other.getDisplayName().trim());
            }
        });
        
        
        Lookup lookup = lookupService.getLookupByName( Lookup.RESTRICTED_DE_ATTRIBUTE_ID );
        int restrictedDeAttributeId = Integer.parseInt( lookup.getValue() );
        Set<DataElement> restrictedDes = ivbUtil.getRestrictedDataElements( restrictedDeAttributeId );

        User curUser = currentUserService.getCurrentUser();
        List<UserAuthorityGroup> userAuthorityGroups = new ArrayList<UserAuthorityGroup>( curUser.getUserCredentials().getUserAuthorityGroups() );
        Set<DataElement> userDataElements = new HashSet<DataElement>();
        for ( UserAuthorityGroup userAuthorityGroup : userAuthorityGroups )
        {
            userDataElements.addAll( userAuthorityGroup.getDataElements() );
        }
        
        restrictedDes.removeAll( userDataElements );
        
        if( restrictedDes != null && restrictedDes.size() > 0 )
        {
        	displayRestrictedDesCheckBox = "N";
        }
        
        return SUCCESS;
    }
    
}

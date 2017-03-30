package org.hisp.dhis.ivb.hiddendeorgunit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.attribute.AttributeValue;
import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserAuthorityGroup;
import org.hisp.dhis.user.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;

public class HideDataElementActionForm implements Action {

	// -------------------------------------------------------------------------
	// Dependencies
	// -------------------------------------------------------------------------

	private DataElementService dataElementService;

	public void setDataElementService(DataElementService dataElementService) {
		this.dataElementService = dataElementService;
	}

	@Autowired
	private SelectionTreeManager selectionTreeManager;

	@Autowired
	private MessageService messageService;

	@Autowired
	private CurrentUserService currentUserService;

	@Autowired
	private I18nService i18nService;

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private ConstantService constantService;

	// -------------------------------------------------------------------------
	// Input & output
	// -------------------------------------------------------------------------

	private List<DataElement> allDataElementList = new ArrayList<DataElement>();

	public List<DataElement> getAllDataElementList() {
		return allDataElementList;
	}

	private String language;

	public String getLanguage() {
		return language;
	}

	private String userName;

	public String getUserName() {
		return userName;
	}

	private String message;

	public String getMessage() {
		return message;
	}

	private int messageCount;

	public int getMessageCount() {
		return messageCount;
	}

	private String adminStatus;

	public String getAdminStatus() {
		return adminStatus;
	}

	private Integer dataElementId;

	public void setDataElementId(Integer dataElementId) {
		this.dataElementId = dataElementId;
	}

	// -------------------------------------------------------------------------
	// Action
	// -------------------------------------------------------------------------

	public String execute() throws Exception {
		FilterOrganisationUnitsAction FOA = new FilterOrganisationUnitsAction();

		Boolean DESElected = FOA.failed;
		
		
		userName = currentUserService.getCurrentUser().getUsername();

		if (i18nService.getCurrentLocale() == null) {
			language = "en";
		} else {
			language = i18nService.getCurrentLocale().getLanguage();
		}

		if (ActionContext.getContext().getSession().get("SessionMsg") == null) {
			message = "Not Exist";
		} else {
			message = "Exist";
		}
		messageCount = (int) messageService.getUnreadMessageConversationCount();
		List<UserGroup> userGrps = new ArrayList<UserGroup>(currentUserService.getCurrentUser().getGroups());
		if (userGrps.contains(configurationService.getConfiguration().getFeedbackRecipients())) {
			adminStatus = "Yes";
		} else {
			adminStatus = "No";
		}

		allDataElementList = (List<DataElement>) dataElementService.getAllDataElements();

		User curUser = currentUserService.getCurrentUser();

		List<UserAuthorityGroup> userAuthorityGroups = new ArrayList<UserAuthorityGroup>(
				curUser.getUserCredentials().getUserAuthorityGroups());
		Set<DataElement> userDataElements = new HashSet<DataElement>();
		for (UserAuthorityGroup userAuthorityGroup : userAuthorityGroups) {
			userDataElements.addAll(userAuthorityGroup.getDataElements());
		}

		Constant ivbAggDEConst = constantService.getConstantByName("IS_IVB_AGGREGATED_DE_ATTRIBUTE_ID");
		Constant ivbRestrictedDEConst = constantService.getConstantByName("RESTRICTED_DE_ATTRIBUTE_ID");
		Set<DataElement> restrictedDes = new HashSet<DataElement>();

		Iterator<DataElement> iterator = allDataElementList.iterator();
		while (iterator.hasNext()) {
			DataElement dataElement = iterator.next();

			if (dataElement.getPublicAccess() != null && dataElement.getPublicAccess().equals("--------")) {
				iterator.remove();
				continue;
			}

			// System.out.println( "DE : " + dataElement.getName() );
			Set<AttributeValue> dataElementAttributeValues = dataElement.getAttributeValues();
			if (dataElementAttributeValues != null && dataElementAttributeValues.size() > 0) {
				for (AttributeValue deAttributeValue : dataElementAttributeValues) {
					// System.out.println( "DE : " + dataElement.getName() + " :
					// " + deAttributeValue.getAttribute().getId() + " : " +
					// deAttributeValue.getValue().equalsIgnoreCase( "true" ) +
					// " : " + ivbRestrictedDEConst.getValue() );

					if (deAttributeValue.getAttribute().getId() == ivbAggDEConst.getValue()
							&& deAttributeValue.getValue().equalsIgnoreCase("true")) {
						iterator.remove();
					} else if (deAttributeValue.getAttribute().getId() == ivbRestrictedDEConst.getValue()
							&& deAttributeValue.getValue().equalsIgnoreCase("true")) {
						restrictedDes.add(dataElement);
						// System.out.println( "Restricted DE : " +
						// dataElement.getName() );
					}
				}
			}

		}

		restrictedDes.removeAll(userDataElements);

		allDataElementList.removeAll(restrictedDes);

		// Set<OrganisationUnit> currentUserOrgUnits =new
		// HashSet<OrganisationUnit>()
		// Set<OrganisationUnit> currentUserOrgUnits = new
		// HashSet<OrganisationUnit>(
		// currentUserService.getCurrentUser().getDataViewOrganisationUnits() );
		// selectionTreeManager.setRootOrganisationUnits( currentUserOrgUnits );
		if (DESElected) {
			
			
			Collection<OrganisationUnit> units = selectionTreeManager.getReloadedSelectedOrganisationUnits();
		} else {
			
			selectionTreeManager.clearSelectedOrganisationUnits();
		}
		FOA.failed=false;
		//DESElected = false;

		ActionContext.getContext().getSession().put("adminStatus", adminStatus);
		ActionContext.getContext().getSession().put("messageCount", messageCount);

		return SUCCESS;
	}
}

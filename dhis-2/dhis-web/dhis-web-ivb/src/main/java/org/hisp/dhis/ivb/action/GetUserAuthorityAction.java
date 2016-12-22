package org.hisp.dhis.ivb.action;

/*
 * Copyright (c) 2004-2012, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the HISP project nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserAuthorityGroup;

import com.opensymphony.xwork2.Action;

/**
 * @author Samta bajpai
 */
public class GetUserAuthorityAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    // -------------------------------------------------------------------------
    // Output
    // -------------------------------------------------------------------------

    private String maintenanceModuleAuthority;

    public String getMaintenanceModuleAuthority()
    {
        return maintenanceModuleAuthority;
    }

    public void setMaintenanceModuleAuthority( String maintenanceModuleAuthority )
    {
        this.maintenanceModuleAuthority = maintenanceModuleAuthority;
    }

    private String userConflictAuthority;

    public String getUserConflictAuthority()
    {
        return userConflictAuthority;
    }

    public void setUserConflictAuthority( String userConflictAuthority )
    {
        this.userConflictAuthority = userConflictAuthority;
    }

    private String userActivityReportAuthority;

    public String getUserActivityReportAuthority()
    {
        return userActivityReportAuthority;
    }

    public void setUserActivityReportAuthority( String userActivityReportAuthority )
    {
        this.userActivityReportAuthority = userActivityReportAuthority;
    }

    private String seeUserDashboardAuthority;

    public String getSeeUserDashboardAuthority()
    {
        return seeUserDashboardAuthority;
    }

    public void setSeeUserDashboardAuthority( String seeUserDashboardAuthority )
    {
        this.seeUserDashboardAuthority = seeUserDashboardAuthority;
    }

    private String aggEngineAuthority;

    public String getAggEngineAuthority()
    {
        return aggEngineAuthority;
    }

    public void setAggEngineAuthority( String aggEngineAuthority )
    {
        this.aggEngineAuthority = aggEngineAuthority;
    }

    private String lookupAuthority;

    public String getLookupAuthority()
    {
        return lookupAuthority;
    }

    public void setLookupAuthority( String lookupAuthority )
    {
        this.lookupAuthority = lookupAuthority;
    }

    private String progReviewTAReportAuthority;

    public String getProgReviewTAReportAuthority()
    {
        return progReviewTAReportAuthority;
    }

    public void setProgReviewTAReportAuthority( String progReviewTAReportAuthority )
    {
        this.progReviewTAReportAuthority = progReviewTAReportAuthority;
    }

    private String cmypDevTrackingReportAuthority;
    
    public String getCmypDevTrackingReportAuthority()
    {
        return cmypDevTrackingReportAuthority;
    }

    public void setCmypDevTrackingReportAuthority( String cmypDevTrackingReportAuthority )
    {
        this.cmypDevTrackingReportAuthority = cmypDevTrackingReportAuthority;
    }

    private String alertReportAuthority;
    
    public String getAlertReportAuthority() 
    {
		return alertReportAuthority;
	}

	public void setAlertReportAuthority(String alertReportAuthority) 
	{
		this.alertReportAuthority = alertReportAuthority;
	}

	private String userActivityLoginReportAuthority;
	
	public String getUserActivityLoginReportAuthority() 
	{
		return userActivityLoginReportAuthority;
	}

	public void setUserActivityLoginReportAuthority(String userActivityLoginReportAuthority) 
	{
		this.userActivityLoginReportAuthority = userActivityLoginReportAuthority;
	}
	private String userActivityLoginMonitoringAuthority;

   

	public String getUserActivityLoginMonitoringAuthority() {
		return userActivityLoginMonitoringAuthority;
	}

	public void setUserActivityLoginMonitoringAuthority(String userActivityLoginMonitoringAuthority) {
		this.userActivityLoginMonitoringAuthority = userActivityLoginMonitoringAuthority;
	}

	public String execute()
    {
        User curUser = currentUserService.getCurrentUser();

        Set<String> userAuthorities = new HashSet<String>();

        List<UserAuthorityGroup> userAuthorityGroups = new ArrayList<UserAuthorityGroup>( curUser.getUserCredentials().getUserAuthorityGroups() );

        for ( UserAuthorityGroup userAuthorityGroup : userAuthorityGroups )
        {
            if ( userAuthorityGroup.getAuthorities() != null )
            {
                userAuthorities.addAll( userAuthorityGroup.getAuthorities() );
            }
        }

        if ( userAuthorities.contains( "F_DATAVALUE_CONFLICT" ) )
        {
            userConflictAuthority = "Yes";
        }
        else
        {
            userConflictAuthority = "No";
        }

        if ( userAuthorities.contains( "F_USER_REPORT_VIEW" ) )
        {
            userActivityReportAuthority = "Yes";
        }
        else
        {
            userActivityReportAuthority = "No";
        }

        if ( userAuthorities.contains( "M_dhis-web-dashboard-integration" ) )
        {
            seeUserDashboardAuthority = "Yes";
        }
        else
        {
            seeUserDashboardAuthority = "No";
        }

        if ( userAuthorities.contains( "F_AGG_ENGINE_VIEW" ) )
        {
            aggEngineAuthority = "Yes";
        }
        else
        {
            aggEngineAuthority = "No";
        }

        if ( userAuthorities.contains( "F_LOOKUP_VIEW" ) )
        {
            lookupAuthority = "Yes";
        }
        else
        {
            lookupAuthority = "No";
        }

        if ( userAuthorities.contains( "M_dhis-web-maintenance-user" ) )
        {
            maintenanceModuleAuthority = "Yes";
        }
        else
        {
            maintenanceModuleAuthority = "No";
        }

        if ( userAuthorities.contains( "F_PROG_REVIEW_TECH_ASSISTANCE_REPORT" ) )
        {
            progReviewTAReportAuthority = "Yes";
        }
        else
        {
            progReviewTAReportAuthority = "No";
        }
        
        if ( userAuthorities.contains( "F_CMYP_DEV_TRACKING_REPORT" ) )
        {
            cmypDevTrackingReportAuthority = "Yes";
        }
        else
        {
            cmypDevTrackingReportAuthority = "No";
        }

        if ( userAuthorities.contains( "F_ALERT_REPORT" ) )
        {
            alertReportAuthority = "Yes";
        }
        else
        {
        	alertReportAuthority = "No";
        }

        if ( userAuthorities.contains( "F_USERACTIVITY_LOGIN_REPORT" ) )
        {
        	userActivityLoginReportAuthority = "Yes";
        }
        else
        {
        	userActivityLoginReportAuthority = "No";
        }
        
        if ( userAuthorities.contains( "ACTIVITY_MONITORING" ) )
        {
        	userActivityLoginMonitoringAuthority = "Yes";
        }
        else
        {
        	userActivityLoginMonitoringAuthority = "No";
        }
        return SUCCESS;
    }
}
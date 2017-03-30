package org.hisp.dhis.ivb.report.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hisp.dhis.configuration.ConfigurationService;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.message.MessageService;
import org.hisp.dhis.ivb.useractivity.UserActivity;
import org.hisp.dhis.ivb.useractivity.UserActivityService;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserCredentials;
import org.hisp.dhis.user.UserGroup;
import org.hisp.dhis.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author Priyanka Bawa
 */
public class GenerateUserAcitivityLoginMonitoringtAction
    implements Action
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private UserService userService;

    private UserCredentials userCredentials;

    @Autowired
    private UserActivityService useractivityservice;

    public UserActivityService getUseractivityservice()
    {
        return useractivityservice;
    }

    public void setUseractivityservice( UserActivityService useractivityservice )
    {
        this.useractivityservice = useractivityservice;
    }

    public UserCredentials getUserCredentials()
    {
        return userCredentials;
    }

    @Autowired
    private MessageService messageService;

    @Autowired
    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    @Autowired
    private I18nService i18nService;

    @Autowired
    private ConfigurationService configurationService;

    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------
    private String startDate;

    public String getStartDate()
    {
        return startDate;
    }

    public void setStartDate( String startDate )
    {
        this.startDate = startDate;
    }

    private String endDate;

    public String getEndDate()
    {
        return endDate;
    }

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

    // private String selectedUser;

    private List<String> selectedUser;

    public List<String> getSelectedUser()
    {
        return selectedUser;
    }

    public void setSelectedUser( List<String> selectedUser )
    {
        this.selectedUser = selectedUser;
    }

    private String alluserList;

    public String getAlluserList()
    {
        return alluserList;
    }

    public void setAlluserList( String alluserList )
    {
        this.alluserList = alluserList;
    }

    private String userByName;

    public String getUserByName()
    {
        return userByName;
    }

    public void setUserByName( String userByName )
    {
        this.userByName = userByName;
    }

    private String language;

    private String userName;

    public String getUserName()
    {
        return userName;
    }

    public void setUserName( String userName )
    {
        this.userName = userName;
    }

    private List<String> userInformation = new ArrayList<String>();

    public List<String> getUserInformation()
    {
        return userInformation;
    }

    public void setUserInformation( List<String> userInformation )
    {
        this.userInformation = userInformation;
    }

    private List<Date> userLoginTime = new ArrayList<Date>();

    public List<Date> getUserLoginTime()
    {
        return userLoginTime;
    }

    public void setUserLoginTime( List<Date> userLoginTime )
    {
        this.userLoginTime = userLoginTime;
    }

    private SimpleDateFormat simpleDateFormat;

    public SimpleDateFormat getSimpleDateFormat()
    {
        return simpleDateFormat;
    }

    public void setSimpleDateFormat( SimpleDateFormat simpleDateFormat )
    {
        this.simpleDateFormat = simpleDateFormat;
    }

    private SimpleDateFormat simpleDateFormat1;

    public SimpleDateFormat getSimpleDateFormat1()
    {
        return simpleDateFormat1;
    }

    public void setSimpleDateFormat1( SimpleDateFormat simpleDateFormat1 )
    {
        this.simpleDateFormat1 = simpleDateFormat1;
    }

    private Map<String, String> userNameMap = new HashMap<String, String>();

    public Map<String, String> getUserNameMap()
    {
        return userNameMap;
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

    // --------------------------------------------------------------------------
    // Action implementation
    // --------------------------------------------------------------------------
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

        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
        // System.out.println( "selectedUser--" + selectedUser );

        // System.out.println( "selectedUser size--" + selectedUser.size() );
        // System.out.println( " userByName--" + userByName );
        sDate = getStartDateByString( startDate );
        eDate = getEndDateByString( endDate );

        if ( selectedUser.size() > 0 )
        {
            for ( int i = 0; i < selectedUser.size(); i++ )
            {
                String userUid = selectedUser.get( i );

                User tempUser = userService.getUser( userUid );

                String allLoginTimes = " ";

                if ( tempUser != null )
                {
                    List<UserActivity> useractivityList = new ArrayList<UserActivity>(
                        useractivityservice.getUserActivityByUserAndDate( tempUser, sDate, eDate ) );

                    // System.out.println( "tempUser-----" +
                    // tempUser.getUsername() );

                    // System.out.println( "useractivity-----" + useractivity);
                    //
                    // System.out.println( "size-----" + count );

                    if ( useractivityList != null )
                    {
                        for ( UserActivity uactivity : useractivityList )
                        {
                            Date str = uactivity.getLoginTime();
                            String strDate = sdf.format( str );
                            // System.out.println("str"+str);
                            allLoginTimes += strDate + " || ";

                        }
                        if ( allLoginTimes.length() > 2 )
                        {
                            allLoginTimes = allLoginTimes.substring( 0, allLoginTimes.length() - 3 );
                        }
                        userNameMap.put( userUid, tempUser.getUsername() + "@@@" + useractivityList.size() + "@@@"
                            + allLoginTimes );

                        // System.out.println( tempUser.getUsername() + "---" +
                        // allLoginTimes );
                    }

                }
            }

        }

        return SUCCESS;
    }

    /**
     * Get Start Date from String date foramt (format could be YYYY / YYYY-Qn /
     * YYYY-MM )
     * 
     * @param dateStr
     * @return
     */
    private Date getStartDateByString( String dateStr )
    {
        String startDate1 = "";
        String[] startDateParts = dateStr.split( "-" );

        if ( startDateParts.length <= 1 )
        {
            startDate1 = startDateParts[0] + "-01-01";
        }
        else if ( startDateParts[1].equalsIgnoreCase( "Q1" ) )
        {
            startDate1 = startDateParts[0] + "-01-01";
        }
        else if ( startDateParts[1].equalsIgnoreCase( "Q2" ) )
        {
            startDate1 = startDateParts[0] + "-04-01";
        }
        else if ( startDateParts[1].equalsIgnoreCase( "Q3" ) )
        {
            startDate1 = startDateParts[0] + "-07-01";
        }
        else if ( startDateParts[1].equalsIgnoreCase( "Q4" ) )
        {
            startDate1 = startDateParts[0] + "-10-01";
        }
        else if ( startDateParts.length == 3 )
        {

            startDate1 = startDateParts[0] + "-" + startDateParts[1] + "-" + startDateParts[2];
        }

        else
        {

            startDate1 = startDateParts[0] + "-" + startDateParts[1] + "-" + "01";

        }

        Date sDate = format.parseDate( startDate1 );

        return sDate;
    }

    /**
     * Get End Date from String date format (format could be YYYY / YYYY-Qn /
     * YYYY-MM )
     * 
     * @param dateStr
     * @return
     */
    private Date getEndDateByString( String dateStr )
    {

        String endDate1 = "";
        int monthDays[] = { 0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
        String[] endDateParts = dateStr.split( "-" );

        if ( endDateParts.length <= 1 )
        {
            endDate1 = endDateParts[0] + "-12-31";
        }
        else if ( endDateParts[1].equalsIgnoreCase( "Q1" ) )
        {
            endDate1 = endDateParts[0] + "-03-31";
        }
        else if ( endDateParts[1].equalsIgnoreCase( "Q2" ) )
        {
            endDate1 = endDateParts[0] + "-06-30";
        }
        else if ( endDateParts[1].equalsIgnoreCase( "Q3" ) )
        {
            endDate1 = endDateParts[0] + "-09-30";
        }
        else if ( endDateParts[1].equalsIgnoreCase( "Q4" ) )
        {
            endDate1 = endDateParts[0] + "-12-31";
        }
        else if ( endDateParts.length == 3 )
        {

            endDate1 = endDateParts[0] + "-" + endDateParts[1] + "-" + endDateParts[2];
        }

        else
        {
            if ( Integer.parseInt( endDateParts[0] ) % 400 == 0 )
            {
                endDate1 = endDateParts[0] + "-" + endDateParts[1] + "-"
                    + (monthDays[Integer.parseInt( endDateParts[1] )] + 1);
            }
            else
            {
                endDate1 = endDateParts[0] + "-" + endDateParts[1] + "-"
                    + (monthDays[Integer.parseInt( endDateParts[1] )]);
            }
        }

        Date eDate = format.parseDate( endDate1 );

        return eDate;
    }
}

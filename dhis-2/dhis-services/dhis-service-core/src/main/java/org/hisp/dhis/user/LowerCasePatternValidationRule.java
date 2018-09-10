package org.hisp.dhis.user;

import java.util.regex.Pattern;

/**
 * @author Mithilesh Kumar Thakur
 */
//custom change for security audit
public class LowerCasePatternValidationRule implements PasswordValidationRule
{
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile( ".*[a-z].*" );
    
    @Override
    public PasswordValidationResult validate( CredentialsInfo credentialsInfo )
    {
        if ( !LOWERCASE_PATTERN.matcher( credentialsInfo.getPassword() ).matches() )
        {
            return new PasswordValidationResult( "Password must have at least one lower case", "password_lowercase_validation",false );
        }

        return new PasswordValidationResult( true );
    }

    @Override
    public boolean isRuleApplicable( CredentialsInfo credentialsInfo )
    {
        return true;
    }
}

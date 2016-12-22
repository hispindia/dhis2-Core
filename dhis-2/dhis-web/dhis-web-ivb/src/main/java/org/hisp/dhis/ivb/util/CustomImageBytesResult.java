package org.hisp.dhis.ivb.util;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.hisp.dhis.ivb.action.ImageAction;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.Result;
 
public class CustomImageBytesResult implements Result 
{
    private static final long serialVersionUID = 8867536777060138224L;

    public void execute(ActionInvocation invocation) throws Exception 
    {
	ImageAction action = (ImageAction) invocation.getAction();
	HttpServletResponse response = ServletActionContext.getResponse();
 
	response.setContentType( action.getCustomContentType());
	response.getOutputStream().write(action.getCustomImageInBytes());
	response.getOutputStream().flush();
    } 
}

package org.hisp.dhis.dataimport.action;
import com.opensymphony.xwork2.Action;


public class NoAction implements Action {

	@Override
	public String execute() throws Exception {
		// TODO Auto-generated method stub
        System.out.println("inside Noaction class");
		return SUCCESS;
	}

}

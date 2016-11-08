
/* **************************************************************************************
	Some common needed funtions
   ************************************************************************************ */
   
   	function isNull(inputId)
	{
		if($('#'+inputId).val()=="")
			return true;
		else
			return false;
	}
	
	function hasMinCharacters(inputId,numOfMinChars)
	{
		if($('#'+inputId).val().length >= numOfMinChars)
			return true;
		else
			return false;
	}
	
	/*
	isUrlValid(inputId)
	{
		var url = $("#"+inputId).val();
		var pattern = new RegExp('^(https?:\/\/)?'+ // protocol
					'((([a-z\d]([a-z\d-]*[a-z\d])*)\.)+[a-z]{2,}|'+ // domain name
					'((\d{1,3}\.){3}\d{1,3}))'+ // OR ip (v4) address
					'(\:\d+)?(\/[-a-z\d%_.~+]*)*'+ // port and path
					'(\?[;&a-z\d%_.~+=-]*)?'+ // query string
					'(\#[-a-z\d_]*)?$','i'); // fragment locater
			
		if(!pattern.test(url)) 
			return false;
		else 
			return true;
	}
	*/
	
/* **************************************************************************************
	Validating Add Instance Form
   ************************************************************************************ */
   
	function validateAddForm()
	{
		if(isNull("title"))
		{
			$("#alertMsg").html("Instance name is empty");
			$("#title").focus();
			return false;
		}
		else if(!hasMinCharacters("title",3))
		{
			$("#alertMsg").html("Instance name should be at least of 3 characters");
			$("#title").focus();
			return false;
		}
		else if(isNull("url"))
		{
			$("#alertMsg").html("Instance URL is empty");
			$("#url").focus();
			return false;
		}
		/*else if(!isUrlValid("url"))
		{
			$("#alertMsg").html("Invalid URL");
			return false;
		}*/
		else if(isNull("uname"))
		{
			$("#alertMsg").html("User name is empty");
			$("#uname").focus();
			return false;
		}
		else if(isNull("pword"))
		{
			$("#alertMsg").html("Password is empty");
			$("#pword").focus();
			return false;
		}
		else
		{
			$("#instanceModal").modal("hide");
			return true;
		}
	}
	
/* **************************************************************************************
	Validating Edit Instance Form
   ************************************************************************************ */
   
	function validateEditForm()
	{
		if(isNull("ename"))
		{
			$("#alertMsg2").html("Instance name is empty");
			$("#ename").focus();
			return false;
		}
		else if(!hasMinCharacters("ename",3))
		{
			$("#alertMsg").html("Instance name should be at least of 3 characters");
			$("#ename").focus();
			return false;
		}
		else if(isNull("eurl"))
		{
			$("#alertMsg").html("Instance URL is empty");
			$("#eurl").focus();
			return false;
		}
		/*else if(!isUrlValid("url"))
		{
			$("#alertMsg").html("Invalid URL");
			return false;
		}*/
		else if(isNull("euname"))
		{
			$("#alertMsg").html("User name is empty");
			$("#euname").focus();
			return false;
		}
		else if(isNull("epword"))
		{
			$("#alertMsg").html("Password is empty");
			$("#epword").focus();
			return false;
		}
		else
		{
			$("#editInstanceModal").modal("hide");
			return true;
		}
	}

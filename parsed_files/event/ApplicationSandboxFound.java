package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ApplicationSandboxFound implements Event{

	private List<ApplicationSandbox> applicationSandboxs;

	public ApplicationSandboxFound(List<ApplicationSandbox> applicationSandboxs) {
		this.setApplicationSandboxs(applicationSandboxs);
	}

	public List<ApplicationSandbox> getApplicationSandboxs()	{
		return applicationSandboxs;
	}

	public void setApplicationSandboxs(List<ApplicationSandbox> applicationSandboxs)	{
		this.applicationSandboxs = applicationSandboxs;
	}
}

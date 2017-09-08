package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class DataResourceRoleFound implements Event{

	private List<DataResourceRole> dataResourceRoles;

	public DataResourceRoleFound(List<DataResourceRole> dataResourceRoles) {
		this.setDataResourceRoles(dataResourceRoles);
	}

	public List<DataResourceRole> getDataResourceRoles()	{
		return dataResourceRoles;
	}

	public void setDataResourceRoles(List<DataResourceRole> dataResourceRoles)	{
		this.dataResourceRoles = dataResourceRoles;
	}
}

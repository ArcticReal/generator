package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class UserLoginSecurityGroupFound implements Event{

	private List<UserLoginSecurityGroup> userLoginSecurityGroups;

	public UserLoginSecurityGroupFound(List<UserLoginSecurityGroup> userLoginSecurityGroups) {
		this.setUserLoginSecurityGroups(userLoginSecurityGroups);
	}

	public List<UserLoginSecurityGroup> getUserLoginSecurityGroups()	{
		return userLoginSecurityGroups;
	}

	public void setUserLoginSecurityGroups(List<UserLoginSecurityGroup> userLoginSecurityGroups)	{
		this.userLoginSecurityGroups = userLoginSecurityGroups;
	}
}

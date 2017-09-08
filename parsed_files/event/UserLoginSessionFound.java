package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class UserLoginSessionFound implements Event{

	private List<UserLoginSession> userLoginSessions;

	public UserLoginSessionFound(List<UserLoginSession> userLoginSessions) {
		this.setUserLoginSessions(userLoginSessions);
	}

	public List<UserLoginSession> getUserLoginSessions()	{
		return userLoginSessions;
	}

	public void setUserLoginSessions(List<UserLoginSession> userLoginSessions)	{
		this.userLoginSessions = userLoginSessions;
	}
}

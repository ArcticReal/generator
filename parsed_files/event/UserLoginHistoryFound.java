package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class UserLoginHistoryFound implements Event{

	private List<UserLoginHistory> userLoginHistorys;

	public UserLoginHistoryFound(List<UserLoginHistory> userLoginHistorys) {
		this.setUserLoginHistorys(userLoginHistorys);
	}

	public List<UserLoginHistory> getUserLoginHistorys()	{
		return userLoginHistorys;
	}

	public void setUserLoginHistorys(List<UserLoginHistory> userLoginHistorys)	{
		this.userLoginHistorys = userLoginHistorys;
	}
}

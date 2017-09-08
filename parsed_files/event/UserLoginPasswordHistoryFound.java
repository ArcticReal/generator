package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class UserLoginPasswordHistoryFound implements Event{

	private List<UserLoginPasswordHistory> userLoginPasswordHistorys;

	public UserLoginPasswordHistoryFound(List<UserLoginPasswordHistory> userLoginPasswordHistorys) {
		this.setUserLoginPasswordHistorys(userLoginPasswordHistorys);
	}

	public List<UserLoginPasswordHistory> getUserLoginPasswordHistorys()	{
		return userLoginPasswordHistorys;
	}

	public void setUserLoginPasswordHistorys(List<UserLoginPasswordHistory> userLoginPasswordHistorys)	{
		this.userLoginPasswordHistorys = userLoginPasswordHistorys;
	}
}

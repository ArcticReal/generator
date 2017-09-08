package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PicklistStatusHistoryFound implements Event{

	private List<PicklistStatusHistory> picklistStatusHistorys;

	public PicklistStatusHistoryFound(List<PicklistStatusHistory> picklistStatusHistorys) {
		this.setPicklistStatusHistorys(picklistStatusHistorys);
	}

	public List<PicklistStatusHistory> getPicklistStatusHistorys()	{
		return picklistStatusHistorys;
	}

	public void setPicklistStatusHistorys(List<PicklistStatusHistory> picklistStatusHistorys)	{
		this.picklistStatusHistorys = picklistStatusHistorys;
	}
}

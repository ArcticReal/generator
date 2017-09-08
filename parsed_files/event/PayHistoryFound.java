package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PayHistoryFound implements Event{

	private List<PayHistory> payHistorys;

	public PayHistoryFound(List<PayHistory> payHistorys) {
		this.setPayHistorys(payHistorys);
	}

	public List<PayHistory> getPayHistorys()	{
		return payHistorys;
	}

	public void setPayHistorys(List<PayHistory> payHistorys)	{
		this.payHistorys = payHistorys;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class GlAccountHistoryFound implements Event{

	private List<GlAccountHistory> glAccountHistorys;

	public GlAccountHistoryFound(List<GlAccountHistory> glAccountHistorys) {
		this.setGlAccountHistorys(glAccountHistorys);
	}

	public List<GlAccountHistory> getGlAccountHistorys()	{
		return glAccountHistorys;
	}

	public void setGlAccountHistorys(List<GlAccountHistory> glAccountHistorys)	{
		this.glAccountHistorys = glAccountHistorys;
	}
}

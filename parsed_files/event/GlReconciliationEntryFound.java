package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class GlReconciliationEntryFound implements Event{

	private List<GlReconciliationEntry> glReconciliationEntrys;

	public GlReconciliationEntryFound(List<GlReconciliationEntry> glReconciliationEntrys) {
		this.setGlReconciliationEntrys(glReconciliationEntrys);
	}

	public List<GlReconciliationEntry> getGlReconciliationEntrys()	{
		return glReconciliationEntrys;
	}

	public void setGlReconciliationEntrys(List<GlReconciliationEntry> glReconciliationEntrys)	{
		this.glReconciliationEntrys = glReconciliationEntrys;
	}
}

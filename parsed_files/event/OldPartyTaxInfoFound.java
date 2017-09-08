package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OldPartyTaxInfoFound implements Event{

	private List<OldPartyTaxInfo> oldPartyTaxInfos;

	public OldPartyTaxInfoFound(List<OldPartyTaxInfo> oldPartyTaxInfos) {
		this.setOldPartyTaxInfos(oldPartyTaxInfos);
	}

	public List<OldPartyTaxInfo> getOldPartyTaxInfos()	{
		return oldPartyTaxInfos;
	}

	public void setOldPartyTaxInfos(List<OldPartyTaxInfo> oldPartyTaxInfos)	{
		this.oldPartyTaxInfos = oldPartyTaxInfos;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyTypeAttrFound implements Event{

	private List<PartyTypeAttr> partyTypeAttrs;

	public PartyTypeAttrFound(List<PartyTypeAttr> partyTypeAttrs) {
		this.setPartyTypeAttrs(partyTypeAttrs);
	}

	public List<PartyTypeAttr> getPartyTypeAttrs()	{
		return partyTypeAttrs;
	}

	public void setPartyTypeAttrs(List<PartyTypeAttr> partyTypeAttrs)	{
		this.partyTypeAttrs = partyTypeAttrs;
	}
}

package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class TermTypeAttrFound implements Event{

	private List<TermTypeAttr> termTypeAttrs;

	public TermTypeAttrFound(List<TermTypeAttr> termTypeAttrs) {
		this.setTermTypeAttrs(termTypeAttrs);
	}

	public List<TermTypeAttr> getTermTypeAttrs()	{
		return termTypeAttrs;
	}

	public void setTermTypeAttrs(List<TermTypeAttr> termTypeAttrs)	{
		this.termTypeAttrs = termTypeAttrs;
	}
}

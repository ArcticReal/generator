package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CustRequestTypeAttrFound implements Event{

	private List<CustRequestTypeAttr> custRequestTypeAttrs;

	public CustRequestTypeAttrFound(List<CustRequestTypeAttr> custRequestTypeAttrs) {
		this.setCustRequestTypeAttrs(custRequestTypeAttrs);
	}

	public List<CustRequestTypeAttr> getCustRequestTypeAttrs()	{
		return custRequestTypeAttrs;
	}

	public void setCustRequestTypeAttrs(List<CustRequestTypeAttr> custRequestTypeAttrs)	{
		this.custRequestTypeAttrs = custRequestTypeAttrs;
	}
}

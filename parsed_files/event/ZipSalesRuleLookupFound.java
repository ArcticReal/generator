package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ZipSalesRuleLookupFound implements Event{

	private List<ZipSalesRuleLookup> zipSalesRuleLookups;

	public ZipSalesRuleLookupFound(List<ZipSalesRuleLookup> zipSalesRuleLookups) {
		this.setZipSalesRuleLookups(zipSalesRuleLookups);
	}

	public List<ZipSalesRuleLookup> getZipSalesRuleLookups()	{
		return zipSalesRuleLookups;
	}

	public void setZipSalesRuleLookups(List<ZipSalesRuleLookup> zipSalesRuleLookups)	{
		this.zipSalesRuleLookups = zipSalesRuleLookups;
	}
}

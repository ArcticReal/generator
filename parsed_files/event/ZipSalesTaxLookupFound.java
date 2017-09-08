package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ZipSalesTaxLookupFound implements Event{

	private List<ZipSalesTaxLookup> zipSalesTaxLookups;

	public ZipSalesTaxLookupFound(List<ZipSalesTaxLookup> zipSalesTaxLookups) {
		this.setZipSalesTaxLookups(zipSalesTaxLookups);
	}

	public List<ZipSalesTaxLookup> getZipSalesTaxLookups()	{
		return zipSalesTaxLookups;
	}

	public void setZipSalesTaxLookups(List<ZipSalesTaxLookup> zipSalesTaxLookups)	{
		this.zipSalesTaxLookups = zipSalesTaxLookups;
	}
}

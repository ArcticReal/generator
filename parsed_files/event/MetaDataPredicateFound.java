package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class MetaDataPredicateFound implements Event{

	private List<MetaDataPredicate> metaDataPredicates;

	public MetaDataPredicateFound(List<MetaDataPredicate> metaDataPredicates) {
		this.setMetaDataPredicates(metaDataPredicates);
	}

	public List<MetaDataPredicate> getMetaDataPredicates()	{
		return metaDataPredicates;
	}

	public void setMetaDataPredicates(List<MetaDataPredicate> metaDataPredicates)	{
		this.metaDataPredicates = metaDataPredicates;
	}
}

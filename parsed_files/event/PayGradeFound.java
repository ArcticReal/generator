package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PayGradeFound implements Event{

	private List<PayGrade> payGrades;

	public PayGradeFound(List<PayGrade> payGrades) {
		this.setPayGrades(payGrades);
	}

	public List<PayGrade> getPayGrades()	{
		return payGrades;
	}

	public void setPayGrades(List<PayGrade> payGrades)	{
		this.payGrades = payGrades;
	}
}

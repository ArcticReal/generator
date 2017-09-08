package com.skytala.eCommerce.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Splitter;
import com.skytala.eCommerce.command.AddPayGrade;
import com.skytala.eCommerce.command.DeletePayGrade;
import com.skytala.eCommerce.command.UpdatePayGrade;
import com.skytala.eCommerce.entity.PayGrade;
import com.skytala.eCommerce.entity.PayGradeMapper;
import com.skytala.eCommerce.event.PayGradeAdded;
import com.skytala.eCommerce.event.PayGradeDeleted;
import com.skytala.eCommerce.event.PayGradeFound;
import com.skytala.eCommerce.event.PayGradeUpdated;
import com.skytala.eCommerce.query.FindPayGradesBy;

@RestController
@RequestMapping("/api/payGrade")
public class PayGradeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PayGrade>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PayGradeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PayGrade
	 * @return a List with the PayGrades
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PayGrade> findPayGradesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPayGradesBy query = new FindPayGradesBy(allRequestParams);

		int usedTicketId;

		synchronized (PayGradeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PayGradeFound.class,
				event -> sendPayGradesFoundMessage(((PayGradeFound) event).getPayGrades(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPayGradesFoundMessage(List<PayGrade> payGrades, int usedTicketId) {
		queryReturnVal.put(usedTicketId, payGrades);
	}

	/**
	 * 
	 * this method will only be called by Springs DispatcherServlet
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @return true on success; false on fail
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/add", consumes = "application/x-www-form-urlencoded")
	public boolean createPayGrade(HttpServletRequest request) {

		PayGrade payGradeToBeAdded = new PayGrade();
		try {
			payGradeToBeAdded = PayGradeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPayGrade(payGradeToBeAdded);

	}

	/**
	 * creates a new PayGrade entry in the ofbiz database
	 * 
	 * @param payGradeToBeAdded
	 *            the PayGrade thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPayGrade(PayGrade payGradeToBeAdded) {

		AddPayGrade com = new AddPayGrade(payGradeToBeAdded);
		int usedTicketId;

		synchronized (PayGradeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PayGradeAdded.class,
				event -> sendPayGradeChangedMessage(((PayGradeAdded) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);

	}

	/**
	 * this method will only be called by Springs DispatcherServlet
	 * 
	 * @param request HttpServletRequest object
	 * @return true on success, false on fail
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/update", consumes = "application/x-www-form-urlencoded")
	public boolean updatePayGrade(HttpServletRequest request) {

		BufferedReader br;
		String data = null;
		Map<String, String> dataMap = null;

		try {
			br = new BufferedReader(new InputStreamReader(request.getInputStream()));
			if (br != null) {
				data = java.net.URLDecoder.decode(br.readLine(), "UTF-8");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}

		dataMap = Splitter.on('&').trimResults().withKeyValueSeparator(Splitter.on('=').limit(2).trimResults())
				.split(data);

		PayGrade payGradeToBeUpdated = new PayGrade();

		try {
			payGradeToBeUpdated = PayGradeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePayGrade(payGradeToBeUpdated);

	}

	/**
	 * Updates the PayGrade with the specific Id
	 * 
	 * @param payGradeToBeUpdated the PayGrade thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePayGrade(PayGrade payGradeToBeUpdated) {

		UpdatePayGrade com = new UpdatePayGrade(payGradeToBeUpdated);

		int usedTicketId;

		synchronized (PayGradeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PayGradeUpdated.class,
				event -> sendPayGradeChangedMessage(((PayGradeUpdated) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);
	}

	/**
	 * removes a PayGrade from the database
	 * 
	 * @param payGradeId:
	 *            the id of the PayGrade thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepayGradeById(@RequestParam(value = "payGradeId") String payGradeId) {

		DeletePayGrade com = new DeletePayGrade(payGradeId);

		int usedTicketId;

		synchronized (PayGradeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PayGradeDeleted.class,
				event -> sendPayGradeChangedMessage(((PayGradeDeleted) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);
	}

	public void sendPayGradeChangedMessage(boolean success, int usedTicketId) {
		commandReturnVal.put(usedTicketId, success);
	}

	@RequestMapping(value = (" * "))
	public String returnErrorPage(HttpServletRequest request) {

		String usedUri = request.getRequestURI();
		String[] splittedString = usedUri.split("/");

		String usedRequest = splittedString[splittedString.length - 1];

		if (validRequests.containsKey(usedRequest)) {
			return "Error: request method " + request.getMethod() + " not allowed for \"" + usedUri + "\"!\n"
					+ "Please use " + validRequests.get(usedRequest) + "!";

		}

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/payGrade/\" plus one of the following: "
				+ "";

		Set<String> keySet = validRequests.keySet();
		Iterator<String> it = keySet.iterator();

		while (it.hasNext()) {
			returnVal += "\"" + it.next() + "\"";
			if (it.hasNext())
				returnVal += ", ";
		}

		returnVal += "!";

		return returnVal;

	}
}

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
import com.skytala.eCommerce.command.AddBillingAccountTerm;
import com.skytala.eCommerce.command.DeleteBillingAccountTerm;
import com.skytala.eCommerce.command.UpdateBillingAccountTerm;
import com.skytala.eCommerce.entity.BillingAccountTerm;
import com.skytala.eCommerce.entity.BillingAccountTermMapper;
import com.skytala.eCommerce.event.BillingAccountTermAdded;
import com.skytala.eCommerce.event.BillingAccountTermDeleted;
import com.skytala.eCommerce.event.BillingAccountTermFound;
import com.skytala.eCommerce.event.BillingAccountTermUpdated;
import com.skytala.eCommerce.query.FindBillingAccountTermsBy;

@RestController
@RequestMapping("/api/billingAccountTerm")
public class BillingAccountTermController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<BillingAccountTerm>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public BillingAccountTermController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a BillingAccountTerm
	 * @return a List with the BillingAccountTerms
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<BillingAccountTerm> findBillingAccountTermsBy(@RequestParam Map<String, String> allRequestParams) {

		FindBillingAccountTermsBy query = new FindBillingAccountTermsBy(allRequestParams);

		int usedTicketId;

		synchronized (BillingAccountTermController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BillingAccountTermFound.class,
				event -> sendBillingAccountTermsFoundMessage(((BillingAccountTermFound) event).getBillingAccountTerms(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendBillingAccountTermsFoundMessage(List<BillingAccountTerm> billingAccountTerms, int usedTicketId) {
		queryReturnVal.put(usedTicketId, billingAccountTerms);
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
	public boolean createBillingAccountTerm(HttpServletRequest request) {

		BillingAccountTerm billingAccountTermToBeAdded = new BillingAccountTerm();
		try {
			billingAccountTermToBeAdded = BillingAccountTermMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createBillingAccountTerm(billingAccountTermToBeAdded);

	}

	/**
	 * creates a new BillingAccountTerm entry in the ofbiz database
	 * 
	 * @param billingAccountTermToBeAdded
	 *            the BillingAccountTerm thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createBillingAccountTerm(BillingAccountTerm billingAccountTermToBeAdded) {

		AddBillingAccountTerm com = new AddBillingAccountTerm(billingAccountTermToBeAdded);
		int usedTicketId;

		synchronized (BillingAccountTermController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BillingAccountTermAdded.class,
				event -> sendBillingAccountTermChangedMessage(((BillingAccountTermAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateBillingAccountTerm(HttpServletRequest request) {

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

		BillingAccountTerm billingAccountTermToBeUpdated = new BillingAccountTerm();

		try {
			billingAccountTermToBeUpdated = BillingAccountTermMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateBillingAccountTerm(billingAccountTermToBeUpdated);

	}

	/**
	 * Updates the BillingAccountTerm with the specific Id
	 * 
	 * @param billingAccountTermToBeUpdated the BillingAccountTerm thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateBillingAccountTerm(BillingAccountTerm billingAccountTermToBeUpdated) {

		UpdateBillingAccountTerm com = new UpdateBillingAccountTerm(billingAccountTermToBeUpdated);

		int usedTicketId;

		synchronized (BillingAccountTermController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BillingAccountTermUpdated.class,
				event -> sendBillingAccountTermChangedMessage(((BillingAccountTermUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a BillingAccountTerm from the database
	 * 
	 * @param billingAccountTermId:
	 *            the id of the BillingAccountTerm thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletebillingAccountTermById(@RequestParam(value = "billingAccountTermId") String billingAccountTermId) {

		DeleteBillingAccountTerm com = new DeleteBillingAccountTerm(billingAccountTermId);

		int usedTicketId;

		synchronized (BillingAccountTermController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BillingAccountTermDeleted.class,
				event -> sendBillingAccountTermChangedMessage(((BillingAccountTermDeleted) event).isSuccess(), usedTicketId));

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

	public void sendBillingAccountTermChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/billingAccountTerm/\" plus one of the following: "
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

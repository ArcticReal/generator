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
import com.skytala.eCommerce.command.AddBillingAccountTermAttr;
import com.skytala.eCommerce.command.DeleteBillingAccountTermAttr;
import com.skytala.eCommerce.command.UpdateBillingAccountTermAttr;
import com.skytala.eCommerce.entity.BillingAccountTermAttr;
import com.skytala.eCommerce.entity.BillingAccountTermAttrMapper;
import com.skytala.eCommerce.event.BillingAccountTermAttrAdded;
import com.skytala.eCommerce.event.BillingAccountTermAttrDeleted;
import com.skytala.eCommerce.event.BillingAccountTermAttrFound;
import com.skytala.eCommerce.event.BillingAccountTermAttrUpdated;
import com.skytala.eCommerce.query.FindBillingAccountTermAttrsBy;

@RestController
@RequestMapping("/api/billingAccountTermAttr")
public class BillingAccountTermAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<BillingAccountTermAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public BillingAccountTermAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a BillingAccountTermAttr
	 * @return a List with the BillingAccountTermAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<BillingAccountTermAttr> findBillingAccountTermAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindBillingAccountTermAttrsBy query = new FindBillingAccountTermAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (BillingAccountTermAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BillingAccountTermAttrFound.class,
				event -> sendBillingAccountTermAttrsFoundMessage(((BillingAccountTermAttrFound) event).getBillingAccountTermAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendBillingAccountTermAttrsFoundMessage(List<BillingAccountTermAttr> billingAccountTermAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, billingAccountTermAttrs);
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
	public boolean createBillingAccountTermAttr(HttpServletRequest request) {

		BillingAccountTermAttr billingAccountTermAttrToBeAdded = new BillingAccountTermAttr();
		try {
			billingAccountTermAttrToBeAdded = BillingAccountTermAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createBillingAccountTermAttr(billingAccountTermAttrToBeAdded);

	}

	/**
	 * creates a new BillingAccountTermAttr entry in the ofbiz database
	 * 
	 * @param billingAccountTermAttrToBeAdded
	 *            the BillingAccountTermAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createBillingAccountTermAttr(BillingAccountTermAttr billingAccountTermAttrToBeAdded) {

		AddBillingAccountTermAttr com = new AddBillingAccountTermAttr(billingAccountTermAttrToBeAdded);
		int usedTicketId;

		synchronized (BillingAccountTermAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BillingAccountTermAttrAdded.class,
				event -> sendBillingAccountTermAttrChangedMessage(((BillingAccountTermAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateBillingAccountTermAttr(HttpServletRequest request) {

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

		BillingAccountTermAttr billingAccountTermAttrToBeUpdated = new BillingAccountTermAttr();

		try {
			billingAccountTermAttrToBeUpdated = BillingAccountTermAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateBillingAccountTermAttr(billingAccountTermAttrToBeUpdated);

	}

	/**
	 * Updates the BillingAccountTermAttr with the specific Id
	 * 
	 * @param billingAccountTermAttrToBeUpdated the BillingAccountTermAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateBillingAccountTermAttr(BillingAccountTermAttr billingAccountTermAttrToBeUpdated) {

		UpdateBillingAccountTermAttr com = new UpdateBillingAccountTermAttr(billingAccountTermAttrToBeUpdated);

		int usedTicketId;

		synchronized (BillingAccountTermAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BillingAccountTermAttrUpdated.class,
				event -> sendBillingAccountTermAttrChangedMessage(((BillingAccountTermAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a BillingAccountTermAttr from the database
	 * 
	 * @param billingAccountTermAttrId:
	 *            the id of the BillingAccountTermAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletebillingAccountTermAttrById(@RequestParam(value = "billingAccountTermAttrId") String billingAccountTermAttrId) {

		DeleteBillingAccountTermAttr com = new DeleteBillingAccountTermAttr(billingAccountTermAttrId);

		int usedTicketId;

		synchronized (BillingAccountTermAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(BillingAccountTermAttrDeleted.class,
				event -> sendBillingAccountTermAttrChangedMessage(((BillingAccountTermAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendBillingAccountTermAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/billingAccountTermAttr/\" plus one of the following: "
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

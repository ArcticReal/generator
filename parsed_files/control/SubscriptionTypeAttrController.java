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
import com.skytala.eCommerce.command.AddSubscriptionTypeAttr;
import com.skytala.eCommerce.command.DeleteSubscriptionTypeAttr;
import com.skytala.eCommerce.command.UpdateSubscriptionTypeAttr;
import com.skytala.eCommerce.entity.SubscriptionTypeAttr;
import com.skytala.eCommerce.entity.SubscriptionTypeAttrMapper;
import com.skytala.eCommerce.event.SubscriptionTypeAttrAdded;
import com.skytala.eCommerce.event.SubscriptionTypeAttrDeleted;
import com.skytala.eCommerce.event.SubscriptionTypeAttrFound;
import com.skytala.eCommerce.event.SubscriptionTypeAttrUpdated;
import com.skytala.eCommerce.query.FindSubscriptionTypeAttrsBy;

@RestController
@RequestMapping("/api/subscriptionTypeAttr")
public class SubscriptionTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SubscriptionTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SubscriptionTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SubscriptionTypeAttr
	 * @return a List with the SubscriptionTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SubscriptionTypeAttr> findSubscriptionTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindSubscriptionTypeAttrsBy query = new FindSubscriptionTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (SubscriptionTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SubscriptionTypeAttrFound.class,
				event -> sendSubscriptionTypeAttrsFoundMessage(((SubscriptionTypeAttrFound) event).getSubscriptionTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSubscriptionTypeAttrsFoundMessage(List<SubscriptionTypeAttr> subscriptionTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, subscriptionTypeAttrs);
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
	public boolean createSubscriptionTypeAttr(HttpServletRequest request) {

		SubscriptionTypeAttr subscriptionTypeAttrToBeAdded = new SubscriptionTypeAttr();
		try {
			subscriptionTypeAttrToBeAdded = SubscriptionTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSubscriptionTypeAttr(subscriptionTypeAttrToBeAdded);

	}

	/**
	 * creates a new SubscriptionTypeAttr entry in the ofbiz database
	 * 
	 * @param subscriptionTypeAttrToBeAdded
	 *            the SubscriptionTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSubscriptionTypeAttr(SubscriptionTypeAttr subscriptionTypeAttrToBeAdded) {

		AddSubscriptionTypeAttr com = new AddSubscriptionTypeAttr(subscriptionTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (SubscriptionTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SubscriptionTypeAttrAdded.class,
				event -> sendSubscriptionTypeAttrChangedMessage(((SubscriptionTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSubscriptionTypeAttr(HttpServletRequest request) {

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

		SubscriptionTypeAttr subscriptionTypeAttrToBeUpdated = new SubscriptionTypeAttr();

		try {
			subscriptionTypeAttrToBeUpdated = SubscriptionTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSubscriptionTypeAttr(subscriptionTypeAttrToBeUpdated);

	}

	/**
	 * Updates the SubscriptionTypeAttr with the specific Id
	 * 
	 * @param subscriptionTypeAttrToBeUpdated the SubscriptionTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSubscriptionTypeAttr(SubscriptionTypeAttr subscriptionTypeAttrToBeUpdated) {

		UpdateSubscriptionTypeAttr com = new UpdateSubscriptionTypeAttr(subscriptionTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (SubscriptionTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SubscriptionTypeAttrUpdated.class,
				event -> sendSubscriptionTypeAttrChangedMessage(((SubscriptionTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SubscriptionTypeAttr from the database
	 * 
	 * @param subscriptionTypeAttrId:
	 *            the id of the SubscriptionTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesubscriptionTypeAttrById(@RequestParam(value = "subscriptionTypeAttrId") String subscriptionTypeAttrId) {

		DeleteSubscriptionTypeAttr com = new DeleteSubscriptionTypeAttr(subscriptionTypeAttrId);

		int usedTicketId;

		synchronized (SubscriptionTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SubscriptionTypeAttrDeleted.class,
				event -> sendSubscriptionTypeAttrChangedMessage(((SubscriptionTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSubscriptionTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/subscriptionTypeAttr/\" plus one of the following: "
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

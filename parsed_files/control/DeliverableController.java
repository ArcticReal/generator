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
import com.skytala.eCommerce.command.AddDeliverable;
import com.skytala.eCommerce.command.DeleteDeliverable;
import com.skytala.eCommerce.command.UpdateDeliverable;
import com.skytala.eCommerce.entity.Deliverable;
import com.skytala.eCommerce.entity.DeliverableMapper;
import com.skytala.eCommerce.event.DeliverableAdded;
import com.skytala.eCommerce.event.DeliverableDeleted;
import com.skytala.eCommerce.event.DeliverableFound;
import com.skytala.eCommerce.event.DeliverableUpdated;
import com.skytala.eCommerce.query.FindDeliverablesBy;

@RestController
@RequestMapping("/api/deliverable")
public class DeliverableController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<Deliverable>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public DeliverableController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a Deliverable
	 * @return a List with the Deliverables
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<Deliverable> findDeliverablesBy(@RequestParam Map<String, String> allRequestParams) {

		FindDeliverablesBy query = new FindDeliverablesBy(allRequestParams);

		int usedTicketId;

		synchronized (DeliverableController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DeliverableFound.class,
				event -> sendDeliverablesFoundMessage(((DeliverableFound) event).getDeliverables(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendDeliverablesFoundMessage(List<Deliverable> deliverables, int usedTicketId) {
		queryReturnVal.put(usedTicketId, deliverables);
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
	public boolean createDeliverable(HttpServletRequest request) {

		Deliverable deliverableToBeAdded = new Deliverable();
		try {
			deliverableToBeAdded = DeliverableMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createDeliverable(deliverableToBeAdded);

	}

	/**
	 * creates a new Deliverable entry in the ofbiz database
	 * 
	 * @param deliverableToBeAdded
	 *            the Deliverable thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createDeliverable(Deliverable deliverableToBeAdded) {

		AddDeliverable com = new AddDeliverable(deliverableToBeAdded);
		int usedTicketId;

		synchronized (DeliverableController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DeliverableAdded.class,
				event -> sendDeliverableChangedMessage(((DeliverableAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateDeliverable(HttpServletRequest request) {

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

		Deliverable deliverableToBeUpdated = new Deliverable();

		try {
			deliverableToBeUpdated = DeliverableMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateDeliverable(deliverableToBeUpdated);

	}

	/**
	 * Updates the Deliverable with the specific Id
	 * 
	 * @param deliverableToBeUpdated the Deliverable thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateDeliverable(Deliverable deliverableToBeUpdated) {

		UpdateDeliverable com = new UpdateDeliverable(deliverableToBeUpdated);

		int usedTicketId;

		synchronized (DeliverableController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DeliverableUpdated.class,
				event -> sendDeliverableChangedMessage(((DeliverableUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a Deliverable from the database
	 * 
	 * @param deliverableId:
	 *            the id of the Deliverable thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletedeliverableById(@RequestParam(value = "deliverableId") String deliverableId) {

		DeleteDeliverable com = new DeleteDeliverable(deliverableId);

		int usedTicketId;

		synchronized (DeliverableController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DeliverableDeleted.class,
				event -> sendDeliverableChangedMessage(((DeliverableDeleted) event).isSuccess(), usedTicketId));

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

	public void sendDeliverableChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/deliverable/\" plus one of the following: "
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

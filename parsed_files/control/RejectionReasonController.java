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
import com.skytala.eCommerce.command.AddRejectionReason;
import com.skytala.eCommerce.command.DeleteRejectionReason;
import com.skytala.eCommerce.command.UpdateRejectionReason;
import com.skytala.eCommerce.entity.RejectionReason;
import com.skytala.eCommerce.entity.RejectionReasonMapper;
import com.skytala.eCommerce.event.RejectionReasonAdded;
import com.skytala.eCommerce.event.RejectionReasonDeleted;
import com.skytala.eCommerce.event.RejectionReasonFound;
import com.skytala.eCommerce.event.RejectionReasonUpdated;
import com.skytala.eCommerce.query.FindRejectionReasonsBy;

@RestController
@RequestMapping("/api/rejectionReason")
public class RejectionReasonController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<RejectionReason>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public RejectionReasonController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a RejectionReason
	 * @return a List with the RejectionReasons
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<RejectionReason> findRejectionReasonsBy(@RequestParam Map<String, String> allRequestParams) {

		FindRejectionReasonsBy query = new FindRejectionReasonsBy(allRequestParams);

		int usedTicketId;

		synchronized (RejectionReasonController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RejectionReasonFound.class,
				event -> sendRejectionReasonsFoundMessage(((RejectionReasonFound) event).getRejectionReasons(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendRejectionReasonsFoundMessage(List<RejectionReason> rejectionReasons, int usedTicketId) {
		queryReturnVal.put(usedTicketId, rejectionReasons);
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
	public boolean createRejectionReason(HttpServletRequest request) {

		RejectionReason rejectionReasonToBeAdded = new RejectionReason();
		try {
			rejectionReasonToBeAdded = RejectionReasonMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createRejectionReason(rejectionReasonToBeAdded);

	}

	/**
	 * creates a new RejectionReason entry in the ofbiz database
	 * 
	 * @param rejectionReasonToBeAdded
	 *            the RejectionReason thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createRejectionReason(RejectionReason rejectionReasonToBeAdded) {

		AddRejectionReason com = new AddRejectionReason(rejectionReasonToBeAdded);
		int usedTicketId;

		synchronized (RejectionReasonController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RejectionReasonAdded.class,
				event -> sendRejectionReasonChangedMessage(((RejectionReasonAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateRejectionReason(HttpServletRequest request) {

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

		RejectionReason rejectionReasonToBeUpdated = new RejectionReason();

		try {
			rejectionReasonToBeUpdated = RejectionReasonMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateRejectionReason(rejectionReasonToBeUpdated);

	}

	/**
	 * Updates the RejectionReason with the specific Id
	 * 
	 * @param rejectionReasonToBeUpdated the RejectionReason thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateRejectionReason(RejectionReason rejectionReasonToBeUpdated) {

		UpdateRejectionReason com = new UpdateRejectionReason(rejectionReasonToBeUpdated);

		int usedTicketId;

		synchronized (RejectionReasonController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RejectionReasonUpdated.class,
				event -> sendRejectionReasonChangedMessage(((RejectionReasonUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a RejectionReason from the database
	 * 
	 * @param rejectionReasonId:
	 *            the id of the RejectionReason thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleterejectionReasonById(@RequestParam(value = "rejectionReasonId") String rejectionReasonId) {

		DeleteRejectionReason com = new DeleteRejectionReason(rejectionReasonId);

		int usedTicketId;

		synchronized (RejectionReasonController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(RejectionReasonDeleted.class,
				event -> sendRejectionReasonChangedMessage(((RejectionReasonDeleted) event).isSuccess(), usedTicketId));

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

	public void sendRejectionReasonChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/rejectionReason/\" plus one of the following: "
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

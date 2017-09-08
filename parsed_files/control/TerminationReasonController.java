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
import com.skytala.eCommerce.command.AddTerminationReason;
import com.skytala.eCommerce.command.DeleteTerminationReason;
import com.skytala.eCommerce.command.UpdateTerminationReason;
import com.skytala.eCommerce.entity.TerminationReason;
import com.skytala.eCommerce.entity.TerminationReasonMapper;
import com.skytala.eCommerce.event.TerminationReasonAdded;
import com.skytala.eCommerce.event.TerminationReasonDeleted;
import com.skytala.eCommerce.event.TerminationReasonFound;
import com.skytala.eCommerce.event.TerminationReasonUpdated;
import com.skytala.eCommerce.query.FindTerminationReasonsBy;

@RestController
@RequestMapping("/api/terminationReason")
public class TerminationReasonController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<TerminationReason>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public TerminationReasonController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a TerminationReason
	 * @return a List with the TerminationReasons
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<TerminationReason> findTerminationReasonsBy(@RequestParam Map<String, String> allRequestParams) {

		FindTerminationReasonsBy query = new FindTerminationReasonsBy(allRequestParams);

		int usedTicketId;

		synchronized (TerminationReasonController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TerminationReasonFound.class,
				event -> sendTerminationReasonsFoundMessage(((TerminationReasonFound) event).getTerminationReasons(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendTerminationReasonsFoundMessage(List<TerminationReason> terminationReasons, int usedTicketId) {
		queryReturnVal.put(usedTicketId, terminationReasons);
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
	public boolean createTerminationReason(HttpServletRequest request) {

		TerminationReason terminationReasonToBeAdded = new TerminationReason();
		try {
			terminationReasonToBeAdded = TerminationReasonMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createTerminationReason(terminationReasonToBeAdded);

	}

	/**
	 * creates a new TerminationReason entry in the ofbiz database
	 * 
	 * @param terminationReasonToBeAdded
	 *            the TerminationReason thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createTerminationReason(TerminationReason terminationReasonToBeAdded) {

		AddTerminationReason com = new AddTerminationReason(terminationReasonToBeAdded);
		int usedTicketId;

		synchronized (TerminationReasonController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TerminationReasonAdded.class,
				event -> sendTerminationReasonChangedMessage(((TerminationReasonAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateTerminationReason(HttpServletRequest request) {

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

		TerminationReason terminationReasonToBeUpdated = new TerminationReason();

		try {
			terminationReasonToBeUpdated = TerminationReasonMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateTerminationReason(terminationReasonToBeUpdated);

	}

	/**
	 * Updates the TerminationReason with the specific Id
	 * 
	 * @param terminationReasonToBeUpdated the TerminationReason thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateTerminationReason(TerminationReason terminationReasonToBeUpdated) {

		UpdateTerminationReason com = new UpdateTerminationReason(terminationReasonToBeUpdated);

		int usedTicketId;

		synchronized (TerminationReasonController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TerminationReasonUpdated.class,
				event -> sendTerminationReasonChangedMessage(((TerminationReasonUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a TerminationReason from the database
	 * 
	 * @param terminationReasonId:
	 *            the id of the TerminationReason thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteterminationReasonById(@RequestParam(value = "terminationReasonId") String terminationReasonId) {

		DeleteTerminationReason com = new DeleteTerminationReason(terminationReasonId);

		int usedTicketId;

		synchronized (TerminationReasonController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TerminationReasonDeleted.class,
				event -> sendTerminationReasonChangedMessage(((TerminationReasonDeleted) event).isSuccess(), usedTicketId));

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

	public void sendTerminationReasonChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/terminationReason/\" plus one of the following: "
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

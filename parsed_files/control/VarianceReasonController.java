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
import com.skytala.eCommerce.command.AddVarianceReason;
import com.skytala.eCommerce.command.DeleteVarianceReason;
import com.skytala.eCommerce.command.UpdateVarianceReason;
import com.skytala.eCommerce.entity.VarianceReason;
import com.skytala.eCommerce.entity.VarianceReasonMapper;
import com.skytala.eCommerce.event.VarianceReasonAdded;
import com.skytala.eCommerce.event.VarianceReasonDeleted;
import com.skytala.eCommerce.event.VarianceReasonFound;
import com.skytala.eCommerce.event.VarianceReasonUpdated;
import com.skytala.eCommerce.query.FindVarianceReasonsBy;

@RestController
@RequestMapping("/api/varianceReason")
public class VarianceReasonController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<VarianceReason>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public VarianceReasonController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a VarianceReason
	 * @return a List with the VarianceReasons
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<VarianceReason> findVarianceReasonsBy(@RequestParam Map<String, String> allRequestParams) {

		FindVarianceReasonsBy query = new FindVarianceReasonsBy(allRequestParams);

		int usedTicketId;

		synchronized (VarianceReasonController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(VarianceReasonFound.class,
				event -> sendVarianceReasonsFoundMessage(((VarianceReasonFound) event).getVarianceReasons(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendVarianceReasonsFoundMessage(List<VarianceReason> varianceReasons, int usedTicketId) {
		queryReturnVal.put(usedTicketId, varianceReasons);
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
	public boolean createVarianceReason(HttpServletRequest request) {

		VarianceReason varianceReasonToBeAdded = new VarianceReason();
		try {
			varianceReasonToBeAdded = VarianceReasonMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createVarianceReason(varianceReasonToBeAdded);

	}

	/**
	 * creates a new VarianceReason entry in the ofbiz database
	 * 
	 * @param varianceReasonToBeAdded
	 *            the VarianceReason thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createVarianceReason(VarianceReason varianceReasonToBeAdded) {

		AddVarianceReason com = new AddVarianceReason(varianceReasonToBeAdded);
		int usedTicketId;

		synchronized (VarianceReasonController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(VarianceReasonAdded.class,
				event -> sendVarianceReasonChangedMessage(((VarianceReasonAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateVarianceReason(HttpServletRequest request) {

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

		VarianceReason varianceReasonToBeUpdated = new VarianceReason();

		try {
			varianceReasonToBeUpdated = VarianceReasonMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateVarianceReason(varianceReasonToBeUpdated);

	}

	/**
	 * Updates the VarianceReason with the specific Id
	 * 
	 * @param varianceReasonToBeUpdated the VarianceReason thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateVarianceReason(VarianceReason varianceReasonToBeUpdated) {

		UpdateVarianceReason com = new UpdateVarianceReason(varianceReasonToBeUpdated);

		int usedTicketId;

		synchronized (VarianceReasonController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(VarianceReasonUpdated.class,
				event -> sendVarianceReasonChangedMessage(((VarianceReasonUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a VarianceReason from the database
	 * 
	 * @param varianceReasonId:
	 *            the id of the VarianceReason thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletevarianceReasonById(@RequestParam(value = "varianceReasonId") String varianceReasonId) {

		DeleteVarianceReason com = new DeleteVarianceReason(varianceReasonId);

		int usedTicketId;

		synchronized (VarianceReasonController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(VarianceReasonDeleted.class,
				event -> sendVarianceReasonChangedMessage(((VarianceReasonDeleted) event).isSuccess(), usedTicketId));

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

	public void sendVarianceReasonChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/varianceReason/\" plus one of the following: "
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

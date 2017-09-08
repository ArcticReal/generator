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
import com.skytala.eCommerce.command.AddPerfReviewItemType;
import com.skytala.eCommerce.command.DeletePerfReviewItemType;
import com.skytala.eCommerce.command.UpdatePerfReviewItemType;
import com.skytala.eCommerce.entity.PerfReviewItemType;
import com.skytala.eCommerce.entity.PerfReviewItemTypeMapper;
import com.skytala.eCommerce.event.PerfReviewItemTypeAdded;
import com.skytala.eCommerce.event.PerfReviewItemTypeDeleted;
import com.skytala.eCommerce.event.PerfReviewItemTypeFound;
import com.skytala.eCommerce.event.PerfReviewItemTypeUpdated;
import com.skytala.eCommerce.query.FindPerfReviewItemTypesBy;

@RestController
@RequestMapping("/api/perfReviewItemType")
public class PerfReviewItemTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PerfReviewItemType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PerfReviewItemTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PerfReviewItemType
	 * @return a List with the PerfReviewItemTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PerfReviewItemType> findPerfReviewItemTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPerfReviewItemTypesBy query = new FindPerfReviewItemTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (PerfReviewItemTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PerfReviewItemTypeFound.class,
				event -> sendPerfReviewItemTypesFoundMessage(((PerfReviewItemTypeFound) event).getPerfReviewItemTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPerfReviewItemTypesFoundMessage(List<PerfReviewItemType> perfReviewItemTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, perfReviewItemTypes);
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
	public boolean createPerfReviewItemType(HttpServletRequest request) {

		PerfReviewItemType perfReviewItemTypeToBeAdded = new PerfReviewItemType();
		try {
			perfReviewItemTypeToBeAdded = PerfReviewItemTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPerfReviewItemType(perfReviewItemTypeToBeAdded);

	}

	/**
	 * creates a new PerfReviewItemType entry in the ofbiz database
	 * 
	 * @param perfReviewItemTypeToBeAdded
	 *            the PerfReviewItemType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPerfReviewItemType(PerfReviewItemType perfReviewItemTypeToBeAdded) {

		AddPerfReviewItemType com = new AddPerfReviewItemType(perfReviewItemTypeToBeAdded);
		int usedTicketId;

		synchronized (PerfReviewItemTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PerfReviewItemTypeAdded.class,
				event -> sendPerfReviewItemTypeChangedMessage(((PerfReviewItemTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePerfReviewItemType(HttpServletRequest request) {

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

		PerfReviewItemType perfReviewItemTypeToBeUpdated = new PerfReviewItemType();

		try {
			perfReviewItemTypeToBeUpdated = PerfReviewItemTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePerfReviewItemType(perfReviewItemTypeToBeUpdated);

	}

	/**
	 * Updates the PerfReviewItemType with the specific Id
	 * 
	 * @param perfReviewItemTypeToBeUpdated the PerfReviewItemType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePerfReviewItemType(PerfReviewItemType perfReviewItemTypeToBeUpdated) {

		UpdatePerfReviewItemType com = new UpdatePerfReviewItemType(perfReviewItemTypeToBeUpdated);

		int usedTicketId;

		synchronized (PerfReviewItemTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PerfReviewItemTypeUpdated.class,
				event -> sendPerfReviewItemTypeChangedMessage(((PerfReviewItemTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PerfReviewItemType from the database
	 * 
	 * @param perfReviewItemTypeId:
	 *            the id of the PerfReviewItemType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteperfReviewItemTypeById(@RequestParam(value = "perfReviewItemTypeId") String perfReviewItemTypeId) {

		DeletePerfReviewItemType com = new DeletePerfReviewItemType(perfReviewItemTypeId);

		int usedTicketId;

		synchronized (PerfReviewItemTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PerfReviewItemTypeDeleted.class,
				event -> sendPerfReviewItemTypeChangedMessage(((PerfReviewItemTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPerfReviewItemTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/perfReviewItemType/\" plus one of the following: "
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

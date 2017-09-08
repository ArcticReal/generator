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
import com.skytala.eCommerce.command.AddPerfRatingType;
import com.skytala.eCommerce.command.DeletePerfRatingType;
import com.skytala.eCommerce.command.UpdatePerfRatingType;
import com.skytala.eCommerce.entity.PerfRatingType;
import com.skytala.eCommerce.entity.PerfRatingTypeMapper;
import com.skytala.eCommerce.event.PerfRatingTypeAdded;
import com.skytala.eCommerce.event.PerfRatingTypeDeleted;
import com.skytala.eCommerce.event.PerfRatingTypeFound;
import com.skytala.eCommerce.event.PerfRatingTypeUpdated;
import com.skytala.eCommerce.query.FindPerfRatingTypesBy;

@RestController
@RequestMapping("/api/perfRatingType")
public class PerfRatingTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PerfRatingType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PerfRatingTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PerfRatingType
	 * @return a List with the PerfRatingTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PerfRatingType> findPerfRatingTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindPerfRatingTypesBy query = new FindPerfRatingTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (PerfRatingTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PerfRatingTypeFound.class,
				event -> sendPerfRatingTypesFoundMessage(((PerfRatingTypeFound) event).getPerfRatingTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPerfRatingTypesFoundMessage(List<PerfRatingType> perfRatingTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, perfRatingTypes);
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
	public boolean createPerfRatingType(HttpServletRequest request) {

		PerfRatingType perfRatingTypeToBeAdded = new PerfRatingType();
		try {
			perfRatingTypeToBeAdded = PerfRatingTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPerfRatingType(perfRatingTypeToBeAdded);

	}

	/**
	 * creates a new PerfRatingType entry in the ofbiz database
	 * 
	 * @param perfRatingTypeToBeAdded
	 *            the PerfRatingType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPerfRatingType(PerfRatingType perfRatingTypeToBeAdded) {

		AddPerfRatingType com = new AddPerfRatingType(perfRatingTypeToBeAdded);
		int usedTicketId;

		synchronized (PerfRatingTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PerfRatingTypeAdded.class,
				event -> sendPerfRatingTypeChangedMessage(((PerfRatingTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePerfRatingType(HttpServletRequest request) {

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

		PerfRatingType perfRatingTypeToBeUpdated = new PerfRatingType();

		try {
			perfRatingTypeToBeUpdated = PerfRatingTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePerfRatingType(perfRatingTypeToBeUpdated);

	}

	/**
	 * Updates the PerfRatingType with the specific Id
	 * 
	 * @param perfRatingTypeToBeUpdated the PerfRatingType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePerfRatingType(PerfRatingType perfRatingTypeToBeUpdated) {

		UpdatePerfRatingType com = new UpdatePerfRatingType(perfRatingTypeToBeUpdated);

		int usedTicketId;

		synchronized (PerfRatingTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PerfRatingTypeUpdated.class,
				event -> sendPerfRatingTypeChangedMessage(((PerfRatingTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PerfRatingType from the database
	 * 
	 * @param perfRatingTypeId:
	 *            the id of the PerfRatingType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteperfRatingTypeById(@RequestParam(value = "perfRatingTypeId") String perfRatingTypeId) {

		DeletePerfRatingType com = new DeletePerfRatingType(perfRatingTypeId);

		int usedTicketId;

		synchronized (PerfRatingTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PerfRatingTypeDeleted.class,
				event -> sendPerfRatingTypeChangedMessage(((PerfRatingTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPerfRatingTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/perfRatingType/\" plus one of the following: "
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

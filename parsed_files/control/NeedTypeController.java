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
import com.skytala.eCommerce.command.AddNeedType;
import com.skytala.eCommerce.command.DeleteNeedType;
import com.skytala.eCommerce.command.UpdateNeedType;
import com.skytala.eCommerce.entity.NeedType;
import com.skytala.eCommerce.entity.NeedTypeMapper;
import com.skytala.eCommerce.event.NeedTypeAdded;
import com.skytala.eCommerce.event.NeedTypeDeleted;
import com.skytala.eCommerce.event.NeedTypeFound;
import com.skytala.eCommerce.event.NeedTypeUpdated;
import com.skytala.eCommerce.query.FindNeedTypesBy;

@RestController
@RequestMapping("/api/needType")
public class NeedTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<NeedType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public NeedTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a NeedType
	 * @return a List with the NeedTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<NeedType> findNeedTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindNeedTypesBy query = new FindNeedTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (NeedTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(NeedTypeFound.class,
				event -> sendNeedTypesFoundMessage(((NeedTypeFound) event).getNeedTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendNeedTypesFoundMessage(List<NeedType> needTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, needTypes);
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
	public boolean createNeedType(HttpServletRequest request) {

		NeedType needTypeToBeAdded = new NeedType();
		try {
			needTypeToBeAdded = NeedTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createNeedType(needTypeToBeAdded);

	}

	/**
	 * creates a new NeedType entry in the ofbiz database
	 * 
	 * @param needTypeToBeAdded
	 *            the NeedType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createNeedType(NeedType needTypeToBeAdded) {

		AddNeedType com = new AddNeedType(needTypeToBeAdded);
		int usedTicketId;

		synchronized (NeedTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(NeedTypeAdded.class,
				event -> sendNeedTypeChangedMessage(((NeedTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateNeedType(HttpServletRequest request) {

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

		NeedType needTypeToBeUpdated = new NeedType();

		try {
			needTypeToBeUpdated = NeedTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateNeedType(needTypeToBeUpdated);

	}

	/**
	 * Updates the NeedType with the specific Id
	 * 
	 * @param needTypeToBeUpdated the NeedType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateNeedType(NeedType needTypeToBeUpdated) {

		UpdateNeedType com = new UpdateNeedType(needTypeToBeUpdated);

		int usedTicketId;

		synchronized (NeedTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(NeedTypeUpdated.class,
				event -> sendNeedTypeChangedMessage(((NeedTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a NeedType from the database
	 * 
	 * @param needTypeId:
	 *            the id of the NeedType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteneedTypeById(@RequestParam(value = "needTypeId") String needTypeId) {

		DeleteNeedType com = new DeleteNeedType(needTypeId);

		int usedTicketId;

		synchronized (NeedTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(NeedTypeDeleted.class,
				event -> sendNeedTypeChangedMessage(((NeedTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendNeedTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/needType/\" plus one of the following: "
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

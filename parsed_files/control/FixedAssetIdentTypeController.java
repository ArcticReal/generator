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
import com.skytala.eCommerce.command.AddFixedAssetIdentType;
import com.skytala.eCommerce.command.DeleteFixedAssetIdentType;
import com.skytala.eCommerce.command.UpdateFixedAssetIdentType;
import com.skytala.eCommerce.entity.FixedAssetIdentType;
import com.skytala.eCommerce.entity.FixedAssetIdentTypeMapper;
import com.skytala.eCommerce.event.FixedAssetIdentTypeAdded;
import com.skytala.eCommerce.event.FixedAssetIdentTypeDeleted;
import com.skytala.eCommerce.event.FixedAssetIdentTypeFound;
import com.skytala.eCommerce.event.FixedAssetIdentTypeUpdated;
import com.skytala.eCommerce.query.FindFixedAssetIdentTypesBy;

@RestController
@RequestMapping("/api/fixedAssetIdentType")
public class FixedAssetIdentTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FixedAssetIdentType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FixedAssetIdentTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FixedAssetIdentType
	 * @return a List with the FixedAssetIdentTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FixedAssetIdentType> findFixedAssetIdentTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindFixedAssetIdentTypesBy query = new FindFixedAssetIdentTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (FixedAssetIdentTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetIdentTypeFound.class,
				event -> sendFixedAssetIdentTypesFoundMessage(((FixedAssetIdentTypeFound) event).getFixedAssetIdentTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFixedAssetIdentTypesFoundMessage(List<FixedAssetIdentType> fixedAssetIdentTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, fixedAssetIdentTypes);
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
	public boolean createFixedAssetIdentType(HttpServletRequest request) {

		FixedAssetIdentType fixedAssetIdentTypeToBeAdded = new FixedAssetIdentType();
		try {
			fixedAssetIdentTypeToBeAdded = FixedAssetIdentTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFixedAssetIdentType(fixedAssetIdentTypeToBeAdded);

	}

	/**
	 * creates a new FixedAssetIdentType entry in the ofbiz database
	 * 
	 * @param fixedAssetIdentTypeToBeAdded
	 *            the FixedAssetIdentType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFixedAssetIdentType(FixedAssetIdentType fixedAssetIdentTypeToBeAdded) {

		AddFixedAssetIdentType com = new AddFixedAssetIdentType(fixedAssetIdentTypeToBeAdded);
		int usedTicketId;

		synchronized (FixedAssetIdentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetIdentTypeAdded.class,
				event -> sendFixedAssetIdentTypeChangedMessage(((FixedAssetIdentTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFixedAssetIdentType(HttpServletRequest request) {

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

		FixedAssetIdentType fixedAssetIdentTypeToBeUpdated = new FixedAssetIdentType();

		try {
			fixedAssetIdentTypeToBeUpdated = FixedAssetIdentTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFixedAssetIdentType(fixedAssetIdentTypeToBeUpdated);

	}

	/**
	 * Updates the FixedAssetIdentType with the specific Id
	 * 
	 * @param fixedAssetIdentTypeToBeUpdated the FixedAssetIdentType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFixedAssetIdentType(FixedAssetIdentType fixedAssetIdentTypeToBeUpdated) {

		UpdateFixedAssetIdentType com = new UpdateFixedAssetIdentType(fixedAssetIdentTypeToBeUpdated);

		int usedTicketId;

		synchronized (FixedAssetIdentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetIdentTypeUpdated.class,
				event -> sendFixedAssetIdentTypeChangedMessage(((FixedAssetIdentTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FixedAssetIdentType from the database
	 * 
	 * @param fixedAssetIdentTypeId:
	 *            the id of the FixedAssetIdentType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefixedAssetIdentTypeById(@RequestParam(value = "fixedAssetIdentTypeId") String fixedAssetIdentTypeId) {

		DeleteFixedAssetIdentType com = new DeleteFixedAssetIdentType(fixedAssetIdentTypeId);

		int usedTicketId;

		synchronized (FixedAssetIdentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetIdentTypeDeleted.class,
				event -> sendFixedAssetIdentTypeChangedMessage(((FixedAssetIdentTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFixedAssetIdentTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/fixedAssetIdentType/\" plus one of the following: "
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

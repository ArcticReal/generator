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
import com.skytala.eCommerce.command.AddFixedAssetStdCostType;
import com.skytala.eCommerce.command.DeleteFixedAssetStdCostType;
import com.skytala.eCommerce.command.UpdateFixedAssetStdCostType;
import com.skytala.eCommerce.entity.FixedAssetStdCostType;
import com.skytala.eCommerce.entity.FixedAssetStdCostTypeMapper;
import com.skytala.eCommerce.event.FixedAssetStdCostTypeAdded;
import com.skytala.eCommerce.event.FixedAssetStdCostTypeDeleted;
import com.skytala.eCommerce.event.FixedAssetStdCostTypeFound;
import com.skytala.eCommerce.event.FixedAssetStdCostTypeUpdated;
import com.skytala.eCommerce.query.FindFixedAssetStdCostTypesBy;

@RestController
@RequestMapping("/api/fixedAssetStdCostType")
public class FixedAssetStdCostTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FixedAssetStdCostType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FixedAssetStdCostTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FixedAssetStdCostType
	 * @return a List with the FixedAssetStdCostTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FixedAssetStdCostType> findFixedAssetStdCostTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindFixedAssetStdCostTypesBy query = new FindFixedAssetStdCostTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (FixedAssetStdCostTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetStdCostTypeFound.class,
				event -> sendFixedAssetStdCostTypesFoundMessage(((FixedAssetStdCostTypeFound) event).getFixedAssetStdCostTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFixedAssetStdCostTypesFoundMessage(List<FixedAssetStdCostType> fixedAssetStdCostTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, fixedAssetStdCostTypes);
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
	public boolean createFixedAssetStdCostType(HttpServletRequest request) {

		FixedAssetStdCostType fixedAssetStdCostTypeToBeAdded = new FixedAssetStdCostType();
		try {
			fixedAssetStdCostTypeToBeAdded = FixedAssetStdCostTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFixedAssetStdCostType(fixedAssetStdCostTypeToBeAdded);

	}

	/**
	 * creates a new FixedAssetStdCostType entry in the ofbiz database
	 * 
	 * @param fixedAssetStdCostTypeToBeAdded
	 *            the FixedAssetStdCostType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFixedAssetStdCostType(FixedAssetStdCostType fixedAssetStdCostTypeToBeAdded) {

		AddFixedAssetStdCostType com = new AddFixedAssetStdCostType(fixedAssetStdCostTypeToBeAdded);
		int usedTicketId;

		synchronized (FixedAssetStdCostTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetStdCostTypeAdded.class,
				event -> sendFixedAssetStdCostTypeChangedMessage(((FixedAssetStdCostTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFixedAssetStdCostType(HttpServletRequest request) {

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

		FixedAssetStdCostType fixedAssetStdCostTypeToBeUpdated = new FixedAssetStdCostType();

		try {
			fixedAssetStdCostTypeToBeUpdated = FixedAssetStdCostTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFixedAssetStdCostType(fixedAssetStdCostTypeToBeUpdated);

	}

	/**
	 * Updates the FixedAssetStdCostType with the specific Id
	 * 
	 * @param fixedAssetStdCostTypeToBeUpdated the FixedAssetStdCostType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFixedAssetStdCostType(FixedAssetStdCostType fixedAssetStdCostTypeToBeUpdated) {

		UpdateFixedAssetStdCostType com = new UpdateFixedAssetStdCostType(fixedAssetStdCostTypeToBeUpdated);

		int usedTicketId;

		synchronized (FixedAssetStdCostTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetStdCostTypeUpdated.class,
				event -> sendFixedAssetStdCostTypeChangedMessage(((FixedAssetStdCostTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FixedAssetStdCostType from the database
	 * 
	 * @param fixedAssetStdCostTypeId:
	 *            the id of the FixedAssetStdCostType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefixedAssetStdCostTypeById(@RequestParam(value = "fixedAssetStdCostTypeId") String fixedAssetStdCostTypeId) {

		DeleteFixedAssetStdCostType com = new DeleteFixedAssetStdCostType(fixedAssetStdCostTypeId);

		int usedTicketId;

		synchronized (FixedAssetStdCostTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetStdCostTypeDeleted.class,
				event -> sendFixedAssetStdCostTypeChangedMessage(((FixedAssetStdCostTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFixedAssetStdCostTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/fixedAssetStdCostType/\" plus one of the following: "
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

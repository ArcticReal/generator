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
import com.skytala.eCommerce.command.AddFixedAssetProductType;
import com.skytala.eCommerce.command.DeleteFixedAssetProductType;
import com.skytala.eCommerce.command.UpdateFixedAssetProductType;
import com.skytala.eCommerce.entity.FixedAssetProductType;
import com.skytala.eCommerce.entity.FixedAssetProductTypeMapper;
import com.skytala.eCommerce.event.FixedAssetProductTypeAdded;
import com.skytala.eCommerce.event.FixedAssetProductTypeDeleted;
import com.skytala.eCommerce.event.FixedAssetProductTypeFound;
import com.skytala.eCommerce.event.FixedAssetProductTypeUpdated;
import com.skytala.eCommerce.query.FindFixedAssetProductTypesBy;

@RestController
@RequestMapping("/api/fixedAssetProductType")
public class FixedAssetProductTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FixedAssetProductType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FixedAssetProductTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FixedAssetProductType
	 * @return a List with the FixedAssetProductTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FixedAssetProductType> findFixedAssetProductTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindFixedAssetProductTypesBy query = new FindFixedAssetProductTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (FixedAssetProductTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetProductTypeFound.class,
				event -> sendFixedAssetProductTypesFoundMessage(((FixedAssetProductTypeFound) event).getFixedAssetProductTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFixedAssetProductTypesFoundMessage(List<FixedAssetProductType> fixedAssetProductTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, fixedAssetProductTypes);
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
	public boolean createFixedAssetProductType(HttpServletRequest request) {

		FixedAssetProductType fixedAssetProductTypeToBeAdded = new FixedAssetProductType();
		try {
			fixedAssetProductTypeToBeAdded = FixedAssetProductTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFixedAssetProductType(fixedAssetProductTypeToBeAdded);

	}

	/**
	 * creates a new FixedAssetProductType entry in the ofbiz database
	 * 
	 * @param fixedAssetProductTypeToBeAdded
	 *            the FixedAssetProductType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFixedAssetProductType(FixedAssetProductType fixedAssetProductTypeToBeAdded) {

		AddFixedAssetProductType com = new AddFixedAssetProductType(fixedAssetProductTypeToBeAdded);
		int usedTicketId;

		synchronized (FixedAssetProductTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetProductTypeAdded.class,
				event -> sendFixedAssetProductTypeChangedMessage(((FixedAssetProductTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFixedAssetProductType(HttpServletRequest request) {

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

		FixedAssetProductType fixedAssetProductTypeToBeUpdated = new FixedAssetProductType();

		try {
			fixedAssetProductTypeToBeUpdated = FixedAssetProductTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFixedAssetProductType(fixedAssetProductTypeToBeUpdated);

	}

	/**
	 * Updates the FixedAssetProductType with the specific Id
	 * 
	 * @param fixedAssetProductTypeToBeUpdated the FixedAssetProductType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFixedAssetProductType(FixedAssetProductType fixedAssetProductTypeToBeUpdated) {

		UpdateFixedAssetProductType com = new UpdateFixedAssetProductType(fixedAssetProductTypeToBeUpdated);

		int usedTicketId;

		synchronized (FixedAssetProductTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetProductTypeUpdated.class,
				event -> sendFixedAssetProductTypeChangedMessage(((FixedAssetProductTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FixedAssetProductType from the database
	 * 
	 * @param fixedAssetProductTypeId:
	 *            the id of the FixedAssetProductType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefixedAssetProductTypeById(@RequestParam(value = "fixedAssetProductTypeId") String fixedAssetProductTypeId) {

		DeleteFixedAssetProductType com = new DeleteFixedAssetProductType(fixedAssetProductTypeId);

		int usedTicketId;

		synchronized (FixedAssetProductTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetProductTypeDeleted.class,
				event -> sendFixedAssetProductTypeChangedMessage(((FixedAssetProductTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFixedAssetProductTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/fixedAssetProductType/\" plus one of the following: "
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

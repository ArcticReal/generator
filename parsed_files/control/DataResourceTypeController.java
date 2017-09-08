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
import com.skytala.eCommerce.command.AddDataResourceType;
import com.skytala.eCommerce.command.DeleteDataResourceType;
import com.skytala.eCommerce.command.UpdateDataResourceType;
import com.skytala.eCommerce.entity.DataResourceType;
import com.skytala.eCommerce.entity.DataResourceTypeMapper;
import com.skytala.eCommerce.event.DataResourceTypeAdded;
import com.skytala.eCommerce.event.DataResourceTypeDeleted;
import com.skytala.eCommerce.event.DataResourceTypeFound;
import com.skytala.eCommerce.event.DataResourceTypeUpdated;
import com.skytala.eCommerce.query.FindDataResourceTypesBy;

@RestController
@RequestMapping("/api/dataResourceType")
public class DataResourceTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<DataResourceType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public DataResourceTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a DataResourceType
	 * @return a List with the DataResourceTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<DataResourceType> findDataResourceTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindDataResourceTypesBy query = new FindDataResourceTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (DataResourceTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataResourceTypeFound.class,
				event -> sendDataResourceTypesFoundMessage(((DataResourceTypeFound) event).getDataResourceTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendDataResourceTypesFoundMessage(List<DataResourceType> dataResourceTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, dataResourceTypes);
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
	public boolean createDataResourceType(HttpServletRequest request) {

		DataResourceType dataResourceTypeToBeAdded = new DataResourceType();
		try {
			dataResourceTypeToBeAdded = DataResourceTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createDataResourceType(dataResourceTypeToBeAdded);

	}

	/**
	 * creates a new DataResourceType entry in the ofbiz database
	 * 
	 * @param dataResourceTypeToBeAdded
	 *            the DataResourceType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createDataResourceType(DataResourceType dataResourceTypeToBeAdded) {

		AddDataResourceType com = new AddDataResourceType(dataResourceTypeToBeAdded);
		int usedTicketId;

		synchronized (DataResourceTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataResourceTypeAdded.class,
				event -> sendDataResourceTypeChangedMessage(((DataResourceTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateDataResourceType(HttpServletRequest request) {

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

		DataResourceType dataResourceTypeToBeUpdated = new DataResourceType();

		try {
			dataResourceTypeToBeUpdated = DataResourceTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateDataResourceType(dataResourceTypeToBeUpdated);

	}

	/**
	 * Updates the DataResourceType with the specific Id
	 * 
	 * @param dataResourceTypeToBeUpdated the DataResourceType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateDataResourceType(DataResourceType dataResourceTypeToBeUpdated) {

		UpdateDataResourceType com = new UpdateDataResourceType(dataResourceTypeToBeUpdated);

		int usedTicketId;

		synchronized (DataResourceTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataResourceTypeUpdated.class,
				event -> sendDataResourceTypeChangedMessage(((DataResourceTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a DataResourceType from the database
	 * 
	 * @param dataResourceTypeId:
	 *            the id of the DataResourceType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletedataResourceTypeById(@RequestParam(value = "dataResourceTypeId") String dataResourceTypeId) {

		DeleteDataResourceType com = new DeleteDataResourceType(dataResourceTypeId);

		int usedTicketId;

		synchronized (DataResourceTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataResourceTypeDeleted.class,
				event -> sendDataResourceTypeChangedMessage(((DataResourceTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendDataResourceTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/dataResourceType/\" plus one of the following: "
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

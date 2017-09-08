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
import com.skytala.eCommerce.command.AddDataTemplateType;
import com.skytala.eCommerce.command.DeleteDataTemplateType;
import com.skytala.eCommerce.command.UpdateDataTemplateType;
import com.skytala.eCommerce.entity.DataTemplateType;
import com.skytala.eCommerce.entity.DataTemplateTypeMapper;
import com.skytala.eCommerce.event.DataTemplateTypeAdded;
import com.skytala.eCommerce.event.DataTemplateTypeDeleted;
import com.skytala.eCommerce.event.DataTemplateTypeFound;
import com.skytala.eCommerce.event.DataTemplateTypeUpdated;
import com.skytala.eCommerce.query.FindDataTemplateTypesBy;

@RestController
@RequestMapping("/api/dataTemplateType")
public class DataTemplateTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<DataTemplateType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public DataTemplateTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a DataTemplateType
	 * @return a List with the DataTemplateTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<DataTemplateType> findDataTemplateTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindDataTemplateTypesBy query = new FindDataTemplateTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (DataTemplateTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataTemplateTypeFound.class,
				event -> sendDataTemplateTypesFoundMessage(((DataTemplateTypeFound) event).getDataTemplateTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendDataTemplateTypesFoundMessage(List<DataTemplateType> dataTemplateTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, dataTemplateTypes);
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
	public boolean createDataTemplateType(HttpServletRequest request) {

		DataTemplateType dataTemplateTypeToBeAdded = new DataTemplateType();
		try {
			dataTemplateTypeToBeAdded = DataTemplateTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createDataTemplateType(dataTemplateTypeToBeAdded);

	}

	/**
	 * creates a new DataTemplateType entry in the ofbiz database
	 * 
	 * @param dataTemplateTypeToBeAdded
	 *            the DataTemplateType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createDataTemplateType(DataTemplateType dataTemplateTypeToBeAdded) {

		AddDataTemplateType com = new AddDataTemplateType(dataTemplateTypeToBeAdded);
		int usedTicketId;

		synchronized (DataTemplateTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataTemplateTypeAdded.class,
				event -> sendDataTemplateTypeChangedMessage(((DataTemplateTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateDataTemplateType(HttpServletRequest request) {

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

		DataTemplateType dataTemplateTypeToBeUpdated = new DataTemplateType();

		try {
			dataTemplateTypeToBeUpdated = DataTemplateTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateDataTemplateType(dataTemplateTypeToBeUpdated);

	}

	/**
	 * Updates the DataTemplateType with the specific Id
	 * 
	 * @param dataTemplateTypeToBeUpdated the DataTemplateType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateDataTemplateType(DataTemplateType dataTemplateTypeToBeUpdated) {

		UpdateDataTemplateType com = new UpdateDataTemplateType(dataTemplateTypeToBeUpdated);

		int usedTicketId;

		synchronized (DataTemplateTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataTemplateTypeUpdated.class,
				event -> sendDataTemplateTypeChangedMessage(((DataTemplateTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a DataTemplateType from the database
	 * 
	 * @param dataTemplateTypeId:
	 *            the id of the DataTemplateType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletedataTemplateTypeById(@RequestParam(value = "dataTemplateTypeId") String dataTemplateTypeId) {

		DeleteDataTemplateType com = new DeleteDataTemplateType(dataTemplateTypeId);

		int usedTicketId;

		synchronized (DataTemplateTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataTemplateTypeDeleted.class,
				event -> sendDataTemplateTypeChangedMessage(((DataTemplateTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendDataTemplateTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/dataTemplateType/\" plus one of the following: "
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

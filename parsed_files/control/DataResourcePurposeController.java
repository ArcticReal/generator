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
import com.skytala.eCommerce.command.AddDataResourcePurpose;
import com.skytala.eCommerce.command.DeleteDataResourcePurpose;
import com.skytala.eCommerce.command.UpdateDataResourcePurpose;
import com.skytala.eCommerce.entity.DataResourcePurpose;
import com.skytala.eCommerce.entity.DataResourcePurposeMapper;
import com.skytala.eCommerce.event.DataResourcePurposeAdded;
import com.skytala.eCommerce.event.DataResourcePurposeDeleted;
import com.skytala.eCommerce.event.DataResourcePurposeFound;
import com.skytala.eCommerce.event.DataResourcePurposeUpdated;
import com.skytala.eCommerce.query.FindDataResourcePurposesBy;

@RestController
@RequestMapping("/api/dataResourcePurpose")
public class DataResourcePurposeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<DataResourcePurpose>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public DataResourcePurposeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a DataResourcePurpose
	 * @return a List with the DataResourcePurposes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<DataResourcePurpose> findDataResourcePurposesBy(@RequestParam Map<String, String> allRequestParams) {

		FindDataResourcePurposesBy query = new FindDataResourcePurposesBy(allRequestParams);

		int usedTicketId;

		synchronized (DataResourcePurposeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataResourcePurposeFound.class,
				event -> sendDataResourcePurposesFoundMessage(((DataResourcePurposeFound) event).getDataResourcePurposes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendDataResourcePurposesFoundMessage(List<DataResourcePurpose> dataResourcePurposes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, dataResourcePurposes);
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
	public boolean createDataResourcePurpose(HttpServletRequest request) {

		DataResourcePurpose dataResourcePurposeToBeAdded = new DataResourcePurpose();
		try {
			dataResourcePurposeToBeAdded = DataResourcePurposeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createDataResourcePurpose(dataResourcePurposeToBeAdded);

	}

	/**
	 * creates a new DataResourcePurpose entry in the ofbiz database
	 * 
	 * @param dataResourcePurposeToBeAdded
	 *            the DataResourcePurpose thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createDataResourcePurpose(DataResourcePurpose dataResourcePurposeToBeAdded) {

		AddDataResourcePurpose com = new AddDataResourcePurpose(dataResourcePurposeToBeAdded);
		int usedTicketId;

		synchronized (DataResourcePurposeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataResourcePurposeAdded.class,
				event -> sendDataResourcePurposeChangedMessage(((DataResourcePurposeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateDataResourcePurpose(HttpServletRequest request) {

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

		DataResourcePurpose dataResourcePurposeToBeUpdated = new DataResourcePurpose();

		try {
			dataResourcePurposeToBeUpdated = DataResourcePurposeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateDataResourcePurpose(dataResourcePurposeToBeUpdated);

	}

	/**
	 * Updates the DataResourcePurpose with the specific Id
	 * 
	 * @param dataResourcePurposeToBeUpdated the DataResourcePurpose thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateDataResourcePurpose(DataResourcePurpose dataResourcePurposeToBeUpdated) {

		UpdateDataResourcePurpose com = new UpdateDataResourcePurpose(dataResourcePurposeToBeUpdated);

		int usedTicketId;

		synchronized (DataResourcePurposeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataResourcePurposeUpdated.class,
				event -> sendDataResourcePurposeChangedMessage(((DataResourcePurposeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a DataResourcePurpose from the database
	 * 
	 * @param dataResourcePurposeId:
	 *            the id of the DataResourcePurpose thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletedataResourcePurposeById(@RequestParam(value = "dataResourcePurposeId") String dataResourcePurposeId) {

		DeleteDataResourcePurpose com = new DeleteDataResourcePurpose(dataResourcePurposeId);

		int usedTicketId;

		synchronized (DataResourcePurposeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataResourcePurposeDeleted.class,
				event -> sendDataResourcePurposeChangedMessage(((DataResourcePurposeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendDataResourcePurposeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/dataResourcePurpose/\" plus one of the following: "
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

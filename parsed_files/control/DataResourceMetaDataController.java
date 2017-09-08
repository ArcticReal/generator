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
import com.skytala.eCommerce.command.AddDataResourceMetaData;
import com.skytala.eCommerce.command.DeleteDataResourceMetaData;
import com.skytala.eCommerce.command.UpdateDataResourceMetaData;
import com.skytala.eCommerce.entity.DataResourceMetaData;
import com.skytala.eCommerce.entity.DataResourceMetaDataMapper;
import com.skytala.eCommerce.event.DataResourceMetaDataAdded;
import com.skytala.eCommerce.event.DataResourceMetaDataDeleted;
import com.skytala.eCommerce.event.DataResourceMetaDataFound;
import com.skytala.eCommerce.event.DataResourceMetaDataUpdated;
import com.skytala.eCommerce.query.FindDataResourceMetaDatasBy;

@RestController
@RequestMapping("/api/dataResourceMetaData")
public class DataResourceMetaDataController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<DataResourceMetaData>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public DataResourceMetaDataController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a DataResourceMetaData
	 * @return a List with the DataResourceMetaDatas
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<DataResourceMetaData> findDataResourceMetaDatasBy(@RequestParam Map<String, String> allRequestParams) {

		FindDataResourceMetaDatasBy query = new FindDataResourceMetaDatasBy(allRequestParams);

		int usedTicketId;

		synchronized (DataResourceMetaDataController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataResourceMetaDataFound.class,
				event -> sendDataResourceMetaDatasFoundMessage(((DataResourceMetaDataFound) event).getDataResourceMetaDatas(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendDataResourceMetaDatasFoundMessage(List<DataResourceMetaData> dataResourceMetaDatas, int usedTicketId) {
		queryReturnVal.put(usedTicketId, dataResourceMetaDatas);
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
	public boolean createDataResourceMetaData(HttpServletRequest request) {

		DataResourceMetaData dataResourceMetaDataToBeAdded = new DataResourceMetaData();
		try {
			dataResourceMetaDataToBeAdded = DataResourceMetaDataMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createDataResourceMetaData(dataResourceMetaDataToBeAdded);

	}

	/**
	 * creates a new DataResourceMetaData entry in the ofbiz database
	 * 
	 * @param dataResourceMetaDataToBeAdded
	 *            the DataResourceMetaData thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createDataResourceMetaData(DataResourceMetaData dataResourceMetaDataToBeAdded) {

		AddDataResourceMetaData com = new AddDataResourceMetaData(dataResourceMetaDataToBeAdded);
		int usedTicketId;

		synchronized (DataResourceMetaDataController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataResourceMetaDataAdded.class,
				event -> sendDataResourceMetaDataChangedMessage(((DataResourceMetaDataAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateDataResourceMetaData(HttpServletRequest request) {

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

		DataResourceMetaData dataResourceMetaDataToBeUpdated = new DataResourceMetaData();

		try {
			dataResourceMetaDataToBeUpdated = DataResourceMetaDataMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateDataResourceMetaData(dataResourceMetaDataToBeUpdated);

	}

	/**
	 * Updates the DataResourceMetaData with the specific Id
	 * 
	 * @param dataResourceMetaDataToBeUpdated the DataResourceMetaData thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateDataResourceMetaData(DataResourceMetaData dataResourceMetaDataToBeUpdated) {

		UpdateDataResourceMetaData com = new UpdateDataResourceMetaData(dataResourceMetaDataToBeUpdated);

		int usedTicketId;

		synchronized (DataResourceMetaDataController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataResourceMetaDataUpdated.class,
				event -> sendDataResourceMetaDataChangedMessage(((DataResourceMetaDataUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a DataResourceMetaData from the database
	 * 
	 * @param dataResourceMetaDataId:
	 *            the id of the DataResourceMetaData thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletedataResourceMetaDataById(@RequestParam(value = "dataResourceMetaDataId") String dataResourceMetaDataId) {

		DeleteDataResourceMetaData com = new DeleteDataResourceMetaData(dataResourceMetaDataId);

		int usedTicketId;

		synchronized (DataResourceMetaDataController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataResourceMetaDataDeleted.class,
				event -> sendDataResourceMetaDataChangedMessage(((DataResourceMetaDataDeleted) event).isSuccess(), usedTicketId));

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

	public void sendDataResourceMetaDataChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/dataResourceMetaData/\" plus one of the following: "
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

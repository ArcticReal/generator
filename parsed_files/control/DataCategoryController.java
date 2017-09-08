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
import com.skytala.eCommerce.command.AddDataCategory;
import com.skytala.eCommerce.command.DeleteDataCategory;
import com.skytala.eCommerce.command.UpdateDataCategory;
import com.skytala.eCommerce.entity.DataCategory;
import com.skytala.eCommerce.entity.DataCategoryMapper;
import com.skytala.eCommerce.event.DataCategoryAdded;
import com.skytala.eCommerce.event.DataCategoryDeleted;
import com.skytala.eCommerce.event.DataCategoryFound;
import com.skytala.eCommerce.event.DataCategoryUpdated;
import com.skytala.eCommerce.query.FindDataCategorysBy;

@RestController
@RequestMapping("/api/dataCategory")
public class DataCategoryController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<DataCategory>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public DataCategoryController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a DataCategory
	 * @return a List with the DataCategorys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<DataCategory> findDataCategorysBy(@RequestParam Map<String, String> allRequestParams) {

		FindDataCategorysBy query = new FindDataCategorysBy(allRequestParams);

		int usedTicketId;

		synchronized (DataCategoryController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataCategoryFound.class,
				event -> sendDataCategorysFoundMessage(((DataCategoryFound) event).getDataCategorys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendDataCategorysFoundMessage(List<DataCategory> dataCategorys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, dataCategorys);
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
	public boolean createDataCategory(HttpServletRequest request) {

		DataCategory dataCategoryToBeAdded = new DataCategory();
		try {
			dataCategoryToBeAdded = DataCategoryMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createDataCategory(dataCategoryToBeAdded);

	}

	/**
	 * creates a new DataCategory entry in the ofbiz database
	 * 
	 * @param dataCategoryToBeAdded
	 *            the DataCategory thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createDataCategory(DataCategory dataCategoryToBeAdded) {

		AddDataCategory com = new AddDataCategory(dataCategoryToBeAdded);
		int usedTicketId;

		synchronized (DataCategoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataCategoryAdded.class,
				event -> sendDataCategoryChangedMessage(((DataCategoryAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateDataCategory(HttpServletRequest request) {

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

		DataCategory dataCategoryToBeUpdated = new DataCategory();

		try {
			dataCategoryToBeUpdated = DataCategoryMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateDataCategory(dataCategoryToBeUpdated);

	}

	/**
	 * Updates the DataCategory with the specific Id
	 * 
	 * @param dataCategoryToBeUpdated the DataCategory thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateDataCategory(DataCategory dataCategoryToBeUpdated) {

		UpdateDataCategory com = new UpdateDataCategory(dataCategoryToBeUpdated);

		int usedTicketId;

		synchronized (DataCategoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataCategoryUpdated.class,
				event -> sendDataCategoryChangedMessage(((DataCategoryUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a DataCategory from the database
	 * 
	 * @param dataCategoryId:
	 *            the id of the DataCategory thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletedataCategoryById(@RequestParam(value = "dataCategoryId") String dataCategoryId) {

		DeleteDataCategory com = new DeleteDataCategory(dataCategoryId);

		int usedTicketId;

		synchronized (DataCategoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataCategoryDeleted.class,
				event -> sendDataCategoryChangedMessage(((DataCategoryDeleted) event).isSuccess(), usedTicketId));

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

	public void sendDataCategoryChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/dataCategory/\" plus one of the following: "
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

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
import com.skytala.eCommerce.command.AddDataResource;
import com.skytala.eCommerce.command.DeleteDataResource;
import com.skytala.eCommerce.command.UpdateDataResource;
import com.skytala.eCommerce.entity.DataResource;
import com.skytala.eCommerce.entity.DataResourceMapper;
import com.skytala.eCommerce.event.DataResourceAdded;
import com.skytala.eCommerce.event.DataResourceDeleted;
import com.skytala.eCommerce.event.DataResourceFound;
import com.skytala.eCommerce.event.DataResourceUpdated;
import com.skytala.eCommerce.query.FindDataResourcesBy;

@RestController
@RequestMapping("/api/dataResource")
public class DataResourceController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<DataResource>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public DataResourceController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a DataResource
	 * @return a List with the DataResources
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<DataResource> findDataResourcesBy(@RequestParam Map<String, String> allRequestParams) {

		FindDataResourcesBy query = new FindDataResourcesBy(allRequestParams);

		int usedTicketId;

		synchronized (DataResourceController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataResourceFound.class,
				event -> sendDataResourcesFoundMessage(((DataResourceFound) event).getDataResources(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendDataResourcesFoundMessage(List<DataResource> dataResources, int usedTicketId) {
		queryReturnVal.put(usedTicketId, dataResources);
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
	public boolean createDataResource(HttpServletRequest request) {

		DataResource dataResourceToBeAdded = new DataResource();
		try {
			dataResourceToBeAdded = DataResourceMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createDataResource(dataResourceToBeAdded);

	}

	/**
	 * creates a new DataResource entry in the ofbiz database
	 * 
	 * @param dataResourceToBeAdded
	 *            the DataResource thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createDataResource(DataResource dataResourceToBeAdded) {

		AddDataResource com = new AddDataResource(dataResourceToBeAdded);
		int usedTicketId;

		synchronized (DataResourceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataResourceAdded.class,
				event -> sendDataResourceChangedMessage(((DataResourceAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateDataResource(HttpServletRequest request) {

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

		DataResource dataResourceToBeUpdated = new DataResource();

		try {
			dataResourceToBeUpdated = DataResourceMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateDataResource(dataResourceToBeUpdated);

	}

	/**
	 * Updates the DataResource with the specific Id
	 * 
	 * @param dataResourceToBeUpdated the DataResource thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateDataResource(DataResource dataResourceToBeUpdated) {

		UpdateDataResource com = new UpdateDataResource(dataResourceToBeUpdated);

		int usedTicketId;

		synchronized (DataResourceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataResourceUpdated.class,
				event -> sendDataResourceChangedMessage(((DataResourceUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a DataResource from the database
	 * 
	 * @param dataResourceId:
	 *            the id of the DataResource thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletedataResourceById(@RequestParam(value = "dataResourceId") String dataResourceId) {

		DeleteDataResource com = new DeleteDataResource(dataResourceId);

		int usedTicketId;

		synchronized (DataResourceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataResourceDeleted.class,
				event -> sendDataResourceChangedMessage(((DataResourceDeleted) event).isSuccess(), usedTicketId));

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

	public void sendDataResourceChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/dataResource/\" plus one of the following: "
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

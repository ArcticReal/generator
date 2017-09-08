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
import com.skytala.eCommerce.command.AddDataResourceAttribute;
import com.skytala.eCommerce.command.DeleteDataResourceAttribute;
import com.skytala.eCommerce.command.UpdateDataResourceAttribute;
import com.skytala.eCommerce.entity.DataResourceAttribute;
import com.skytala.eCommerce.entity.DataResourceAttributeMapper;
import com.skytala.eCommerce.event.DataResourceAttributeAdded;
import com.skytala.eCommerce.event.DataResourceAttributeDeleted;
import com.skytala.eCommerce.event.DataResourceAttributeFound;
import com.skytala.eCommerce.event.DataResourceAttributeUpdated;
import com.skytala.eCommerce.query.FindDataResourceAttributesBy;

@RestController
@RequestMapping("/api/dataResourceAttribute")
public class DataResourceAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<DataResourceAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public DataResourceAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a DataResourceAttribute
	 * @return a List with the DataResourceAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<DataResourceAttribute> findDataResourceAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindDataResourceAttributesBy query = new FindDataResourceAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (DataResourceAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataResourceAttributeFound.class,
				event -> sendDataResourceAttributesFoundMessage(((DataResourceAttributeFound) event).getDataResourceAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendDataResourceAttributesFoundMessage(List<DataResourceAttribute> dataResourceAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, dataResourceAttributes);
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
	public boolean createDataResourceAttribute(HttpServletRequest request) {

		DataResourceAttribute dataResourceAttributeToBeAdded = new DataResourceAttribute();
		try {
			dataResourceAttributeToBeAdded = DataResourceAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createDataResourceAttribute(dataResourceAttributeToBeAdded);

	}

	/**
	 * creates a new DataResourceAttribute entry in the ofbiz database
	 * 
	 * @param dataResourceAttributeToBeAdded
	 *            the DataResourceAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createDataResourceAttribute(DataResourceAttribute dataResourceAttributeToBeAdded) {

		AddDataResourceAttribute com = new AddDataResourceAttribute(dataResourceAttributeToBeAdded);
		int usedTicketId;

		synchronized (DataResourceAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataResourceAttributeAdded.class,
				event -> sendDataResourceAttributeChangedMessage(((DataResourceAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateDataResourceAttribute(HttpServletRequest request) {

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

		DataResourceAttribute dataResourceAttributeToBeUpdated = new DataResourceAttribute();

		try {
			dataResourceAttributeToBeUpdated = DataResourceAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateDataResourceAttribute(dataResourceAttributeToBeUpdated);

	}

	/**
	 * Updates the DataResourceAttribute with the specific Id
	 * 
	 * @param dataResourceAttributeToBeUpdated the DataResourceAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateDataResourceAttribute(DataResourceAttribute dataResourceAttributeToBeUpdated) {

		UpdateDataResourceAttribute com = new UpdateDataResourceAttribute(dataResourceAttributeToBeUpdated);

		int usedTicketId;

		synchronized (DataResourceAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataResourceAttributeUpdated.class,
				event -> sendDataResourceAttributeChangedMessage(((DataResourceAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a DataResourceAttribute from the database
	 * 
	 * @param dataResourceAttributeId:
	 *            the id of the DataResourceAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletedataResourceAttributeById(@RequestParam(value = "dataResourceAttributeId") String dataResourceAttributeId) {

		DeleteDataResourceAttribute com = new DeleteDataResourceAttribute(dataResourceAttributeId);

		int usedTicketId;

		synchronized (DataResourceAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataResourceAttributeDeleted.class,
				event -> sendDataResourceAttributeChangedMessage(((DataResourceAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendDataResourceAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/dataResourceAttribute/\" plus one of the following: "
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

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
import com.skytala.eCommerce.command.AddDataResourceTypeAttr;
import com.skytala.eCommerce.command.DeleteDataResourceTypeAttr;
import com.skytala.eCommerce.command.UpdateDataResourceTypeAttr;
import com.skytala.eCommerce.entity.DataResourceTypeAttr;
import com.skytala.eCommerce.entity.DataResourceTypeAttrMapper;
import com.skytala.eCommerce.event.DataResourceTypeAttrAdded;
import com.skytala.eCommerce.event.DataResourceTypeAttrDeleted;
import com.skytala.eCommerce.event.DataResourceTypeAttrFound;
import com.skytala.eCommerce.event.DataResourceTypeAttrUpdated;
import com.skytala.eCommerce.query.FindDataResourceTypeAttrsBy;

@RestController
@RequestMapping("/api/dataResourceTypeAttr")
public class DataResourceTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<DataResourceTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public DataResourceTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a DataResourceTypeAttr
	 * @return a List with the DataResourceTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<DataResourceTypeAttr> findDataResourceTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindDataResourceTypeAttrsBy query = new FindDataResourceTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (DataResourceTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataResourceTypeAttrFound.class,
				event -> sendDataResourceTypeAttrsFoundMessage(((DataResourceTypeAttrFound) event).getDataResourceTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendDataResourceTypeAttrsFoundMessage(List<DataResourceTypeAttr> dataResourceTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, dataResourceTypeAttrs);
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
	public boolean createDataResourceTypeAttr(HttpServletRequest request) {

		DataResourceTypeAttr dataResourceTypeAttrToBeAdded = new DataResourceTypeAttr();
		try {
			dataResourceTypeAttrToBeAdded = DataResourceTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createDataResourceTypeAttr(dataResourceTypeAttrToBeAdded);

	}

	/**
	 * creates a new DataResourceTypeAttr entry in the ofbiz database
	 * 
	 * @param dataResourceTypeAttrToBeAdded
	 *            the DataResourceTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createDataResourceTypeAttr(DataResourceTypeAttr dataResourceTypeAttrToBeAdded) {

		AddDataResourceTypeAttr com = new AddDataResourceTypeAttr(dataResourceTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (DataResourceTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataResourceTypeAttrAdded.class,
				event -> sendDataResourceTypeAttrChangedMessage(((DataResourceTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateDataResourceTypeAttr(HttpServletRequest request) {

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

		DataResourceTypeAttr dataResourceTypeAttrToBeUpdated = new DataResourceTypeAttr();

		try {
			dataResourceTypeAttrToBeUpdated = DataResourceTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateDataResourceTypeAttr(dataResourceTypeAttrToBeUpdated);

	}

	/**
	 * Updates the DataResourceTypeAttr with the specific Id
	 * 
	 * @param dataResourceTypeAttrToBeUpdated the DataResourceTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateDataResourceTypeAttr(DataResourceTypeAttr dataResourceTypeAttrToBeUpdated) {

		UpdateDataResourceTypeAttr com = new UpdateDataResourceTypeAttr(dataResourceTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (DataResourceTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataResourceTypeAttrUpdated.class,
				event -> sendDataResourceTypeAttrChangedMessage(((DataResourceTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a DataResourceTypeAttr from the database
	 * 
	 * @param dataResourceTypeAttrId:
	 *            the id of the DataResourceTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletedataResourceTypeAttrById(@RequestParam(value = "dataResourceTypeAttrId") String dataResourceTypeAttrId) {

		DeleteDataResourceTypeAttr com = new DeleteDataResourceTypeAttr(dataResourceTypeAttrId);

		int usedTicketId;

		synchronized (DataResourceTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DataResourceTypeAttrDeleted.class,
				event -> sendDataResourceTypeAttrChangedMessage(((DataResourceTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendDataResourceTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/dataResourceTypeAttr/\" plus one of the following: "
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

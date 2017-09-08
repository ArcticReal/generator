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
import com.skytala.eCommerce.command.AddContentMetaData;
import com.skytala.eCommerce.command.DeleteContentMetaData;
import com.skytala.eCommerce.command.UpdateContentMetaData;
import com.skytala.eCommerce.entity.ContentMetaData;
import com.skytala.eCommerce.entity.ContentMetaDataMapper;
import com.skytala.eCommerce.event.ContentMetaDataAdded;
import com.skytala.eCommerce.event.ContentMetaDataDeleted;
import com.skytala.eCommerce.event.ContentMetaDataFound;
import com.skytala.eCommerce.event.ContentMetaDataUpdated;
import com.skytala.eCommerce.query.FindContentMetaDatasBy;

@RestController
@RequestMapping("/api/contentMetaData")
public class ContentMetaDataController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContentMetaData>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContentMetaDataController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContentMetaData
	 * @return a List with the ContentMetaDatas
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContentMetaData> findContentMetaDatasBy(@RequestParam Map<String, String> allRequestParams) {

		FindContentMetaDatasBy query = new FindContentMetaDatasBy(allRequestParams);

		int usedTicketId;

		synchronized (ContentMetaDataController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentMetaDataFound.class,
				event -> sendContentMetaDatasFoundMessage(((ContentMetaDataFound) event).getContentMetaDatas(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContentMetaDatasFoundMessage(List<ContentMetaData> contentMetaDatas, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contentMetaDatas);
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
	public boolean createContentMetaData(HttpServletRequest request) {

		ContentMetaData contentMetaDataToBeAdded = new ContentMetaData();
		try {
			contentMetaDataToBeAdded = ContentMetaDataMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContentMetaData(contentMetaDataToBeAdded);

	}

	/**
	 * creates a new ContentMetaData entry in the ofbiz database
	 * 
	 * @param contentMetaDataToBeAdded
	 *            the ContentMetaData thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContentMetaData(ContentMetaData contentMetaDataToBeAdded) {

		AddContentMetaData com = new AddContentMetaData(contentMetaDataToBeAdded);
		int usedTicketId;

		synchronized (ContentMetaDataController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentMetaDataAdded.class,
				event -> sendContentMetaDataChangedMessage(((ContentMetaDataAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContentMetaData(HttpServletRequest request) {

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

		ContentMetaData contentMetaDataToBeUpdated = new ContentMetaData();

		try {
			contentMetaDataToBeUpdated = ContentMetaDataMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContentMetaData(contentMetaDataToBeUpdated);

	}

	/**
	 * Updates the ContentMetaData with the specific Id
	 * 
	 * @param contentMetaDataToBeUpdated the ContentMetaData thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContentMetaData(ContentMetaData contentMetaDataToBeUpdated) {

		UpdateContentMetaData com = new UpdateContentMetaData(contentMetaDataToBeUpdated);

		int usedTicketId;

		synchronized (ContentMetaDataController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentMetaDataUpdated.class,
				event -> sendContentMetaDataChangedMessage(((ContentMetaDataUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContentMetaData from the database
	 * 
	 * @param contentMetaDataId:
	 *            the id of the ContentMetaData thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontentMetaDataById(@RequestParam(value = "contentMetaDataId") String contentMetaDataId) {

		DeleteContentMetaData com = new DeleteContentMetaData(contentMetaDataId);

		int usedTicketId;

		synchronized (ContentMetaDataController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentMetaDataDeleted.class,
				event -> sendContentMetaDataChangedMessage(((ContentMetaDataDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContentMetaDataChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contentMetaData/\" plus one of the following: "
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

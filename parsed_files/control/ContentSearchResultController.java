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
import com.skytala.eCommerce.command.AddContentSearchResult;
import com.skytala.eCommerce.command.DeleteContentSearchResult;
import com.skytala.eCommerce.command.UpdateContentSearchResult;
import com.skytala.eCommerce.entity.ContentSearchResult;
import com.skytala.eCommerce.entity.ContentSearchResultMapper;
import com.skytala.eCommerce.event.ContentSearchResultAdded;
import com.skytala.eCommerce.event.ContentSearchResultDeleted;
import com.skytala.eCommerce.event.ContentSearchResultFound;
import com.skytala.eCommerce.event.ContentSearchResultUpdated;
import com.skytala.eCommerce.query.FindContentSearchResultsBy;

@RestController
@RequestMapping("/api/contentSearchResult")
public class ContentSearchResultController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContentSearchResult>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContentSearchResultController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContentSearchResult
	 * @return a List with the ContentSearchResults
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContentSearchResult> findContentSearchResultsBy(@RequestParam Map<String, String> allRequestParams) {

		FindContentSearchResultsBy query = new FindContentSearchResultsBy(allRequestParams);

		int usedTicketId;

		synchronized (ContentSearchResultController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentSearchResultFound.class,
				event -> sendContentSearchResultsFoundMessage(((ContentSearchResultFound) event).getContentSearchResults(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContentSearchResultsFoundMessage(List<ContentSearchResult> contentSearchResults, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contentSearchResults);
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
	public boolean createContentSearchResult(HttpServletRequest request) {

		ContentSearchResult contentSearchResultToBeAdded = new ContentSearchResult();
		try {
			contentSearchResultToBeAdded = ContentSearchResultMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContentSearchResult(contentSearchResultToBeAdded);

	}

	/**
	 * creates a new ContentSearchResult entry in the ofbiz database
	 * 
	 * @param contentSearchResultToBeAdded
	 *            the ContentSearchResult thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContentSearchResult(ContentSearchResult contentSearchResultToBeAdded) {

		AddContentSearchResult com = new AddContentSearchResult(contentSearchResultToBeAdded);
		int usedTicketId;

		synchronized (ContentSearchResultController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentSearchResultAdded.class,
				event -> sendContentSearchResultChangedMessage(((ContentSearchResultAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContentSearchResult(HttpServletRequest request) {

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

		ContentSearchResult contentSearchResultToBeUpdated = new ContentSearchResult();

		try {
			contentSearchResultToBeUpdated = ContentSearchResultMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContentSearchResult(contentSearchResultToBeUpdated);

	}

	/**
	 * Updates the ContentSearchResult with the specific Id
	 * 
	 * @param contentSearchResultToBeUpdated the ContentSearchResult thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContentSearchResult(ContentSearchResult contentSearchResultToBeUpdated) {

		UpdateContentSearchResult com = new UpdateContentSearchResult(contentSearchResultToBeUpdated);

		int usedTicketId;

		synchronized (ContentSearchResultController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentSearchResultUpdated.class,
				event -> sendContentSearchResultChangedMessage(((ContentSearchResultUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContentSearchResult from the database
	 * 
	 * @param contentSearchResultId:
	 *            the id of the ContentSearchResult thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontentSearchResultById(@RequestParam(value = "contentSearchResultId") String contentSearchResultId) {

		DeleteContentSearchResult com = new DeleteContentSearchResult(contentSearchResultId);

		int usedTicketId;

		synchronized (ContentSearchResultController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentSearchResultDeleted.class,
				event -> sendContentSearchResultChangedMessage(((ContentSearchResultDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContentSearchResultChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contentSearchResult/\" plus one of the following: "
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

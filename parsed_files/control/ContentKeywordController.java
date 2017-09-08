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
import com.skytala.eCommerce.command.AddContentKeyword;
import com.skytala.eCommerce.command.DeleteContentKeyword;
import com.skytala.eCommerce.command.UpdateContentKeyword;
import com.skytala.eCommerce.entity.ContentKeyword;
import com.skytala.eCommerce.entity.ContentKeywordMapper;
import com.skytala.eCommerce.event.ContentKeywordAdded;
import com.skytala.eCommerce.event.ContentKeywordDeleted;
import com.skytala.eCommerce.event.ContentKeywordFound;
import com.skytala.eCommerce.event.ContentKeywordUpdated;
import com.skytala.eCommerce.query.FindContentKeywordsBy;

@RestController
@RequestMapping("/api/contentKeyword")
public class ContentKeywordController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContentKeyword>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContentKeywordController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContentKeyword
	 * @return a List with the ContentKeywords
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContentKeyword> findContentKeywordsBy(@RequestParam Map<String, String> allRequestParams) {

		FindContentKeywordsBy query = new FindContentKeywordsBy(allRequestParams);

		int usedTicketId;

		synchronized (ContentKeywordController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentKeywordFound.class,
				event -> sendContentKeywordsFoundMessage(((ContentKeywordFound) event).getContentKeywords(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContentKeywordsFoundMessage(List<ContentKeyword> contentKeywords, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contentKeywords);
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
	public boolean createContentKeyword(HttpServletRequest request) {

		ContentKeyword contentKeywordToBeAdded = new ContentKeyword();
		try {
			contentKeywordToBeAdded = ContentKeywordMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContentKeyword(contentKeywordToBeAdded);

	}

	/**
	 * creates a new ContentKeyword entry in the ofbiz database
	 * 
	 * @param contentKeywordToBeAdded
	 *            the ContentKeyword thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContentKeyword(ContentKeyword contentKeywordToBeAdded) {

		AddContentKeyword com = new AddContentKeyword(contentKeywordToBeAdded);
		int usedTicketId;

		synchronized (ContentKeywordController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentKeywordAdded.class,
				event -> sendContentKeywordChangedMessage(((ContentKeywordAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContentKeyword(HttpServletRequest request) {

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

		ContentKeyword contentKeywordToBeUpdated = new ContentKeyword();

		try {
			contentKeywordToBeUpdated = ContentKeywordMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContentKeyword(contentKeywordToBeUpdated);

	}

	/**
	 * Updates the ContentKeyword with the specific Id
	 * 
	 * @param contentKeywordToBeUpdated the ContentKeyword thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContentKeyword(ContentKeyword contentKeywordToBeUpdated) {

		UpdateContentKeyword com = new UpdateContentKeyword(contentKeywordToBeUpdated);

		int usedTicketId;

		synchronized (ContentKeywordController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentKeywordUpdated.class,
				event -> sendContentKeywordChangedMessage(((ContentKeywordUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContentKeyword from the database
	 * 
	 * @param contentKeywordId:
	 *            the id of the ContentKeyword thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontentKeywordById(@RequestParam(value = "contentKeywordId") String contentKeywordId) {

		DeleteContentKeyword com = new DeleteContentKeyword(contentKeywordId);

		int usedTicketId;

		synchronized (ContentKeywordController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentKeywordDeleted.class,
				event -> sendContentKeywordChangedMessage(((ContentKeywordDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContentKeywordChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contentKeyword/\" plus one of the following: "
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

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
import com.skytala.eCommerce.command.AddContentAssoc;
import com.skytala.eCommerce.command.DeleteContentAssoc;
import com.skytala.eCommerce.command.UpdateContentAssoc;
import com.skytala.eCommerce.entity.ContentAssoc;
import com.skytala.eCommerce.entity.ContentAssocMapper;
import com.skytala.eCommerce.event.ContentAssocAdded;
import com.skytala.eCommerce.event.ContentAssocDeleted;
import com.skytala.eCommerce.event.ContentAssocFound;
import com.skytala.eCommerce.event.ContentAssocUpdated;
import com.skytala.eCommerce.query.FindContentAssocsBy;

@RestController
@RequestMapping("/api/contentAssoc")
public class ContentAssocController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContentAssoc>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContentAssocController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContentAssoc
	 * @return a List with the ContentAssocs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContentAssoc> findContentAssocsBy(@RequestParam Map<String, String> allRequestParams) {

		FindContentAssocsBy query = new FindContentAssocsBy(allRequestParams);

		int usedTicketId;

		synchronized (ContentAssocController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentAssocFound.class,
				event -> sendContentAssocsFoundMessage(((ContentAssocFound) event).getContentAssocs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContentAssocsFoundMessage(List<ContentAssoc> contentAssocs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contentAssocs);
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
	public boolean createContentAssoc(HttpServletRequest request) {

		ContentAssoc contentAssocToBeAdded = new ContentAssoc();
		try {
			contentAssocToBeAdded = ContentAssocMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContentAssoc(contentAssocToBeAdded);

	}

	/**
	 * creates a new ContentAssoc entry in the ofbiz database
	 * 
	 * @param contentAssocToBeAdded
	 *            the ContentAssoc thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContentAssoc(ContentAssoc contentAssocToBeAdded) {

		AddContentAssoc com = new AddContentAssoc(contentAssocToBeAdded);
		int usedTicketId;

		synchronized (ContentAssocController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentAssocAdded.class,
				event -> sendContentAssocChangedMessage(((ContentAssocAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContentAssoc(HttpServletRequest request) {

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

		ContentAssoc contentAssocToBeUpdated = new ContentAssoc();

		try {
			contentAssocToBeUpdated = ContentAssocMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContentAssoc(contentAssocToBeUpdated);

	}

	/**
	 * Updates the ContentAssoc with the specific Id
	 * 
	 * @param contentAssocToBeUpdated the ContentAssoc thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContentAssoc(ContentAssoc contentAssocToBeUpdated) {

		UpdateContentAssoc com = new UpdateContentAssoc(contentAssocToBeUpdated);

		int usedTicketId;

		synchronized (ContentAssocController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentAssocUpdated.class,
				event -> sendContentAssocChangedMessage(((ContentAssocUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContentAssoc from the database
	 * 
	 * @param contentAssocId:
	 *            the id of the ContentAssoc thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontentAssocById(@RequestParam(value = "contentAssocId") String contentAssocId) {

		DeleteContentAssoc com = new DeleteContentAssoc(contentAssocId);

		int usedTicketId;

		synchronized (ContentAssocController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentAssocDeleted.class,
				event -> sendContentAssocChangedMessage(((ContentAssocDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContentAssocChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contentAssoc/\" plus one of the following: "
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

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
import com.skytala.eCommerce.command.AddContentAttribute;
import com.skytala.eCommerce.command.DeleteContentAttribute;
import com.skytala.eCommerce.command.UpdateContentAttribute;
import com.skytala.eCommerce.entity.ContentAttribute;
import com.skytala.eCommerce.entity.ContentAttributeMapper;
import com.skytala.eCommerce.event.ContentAttributeAdded;
import com.skytala.eCommerce.event.ContentAttributeDeleted;
import com.skytala.eCommerce.event.ContentAttributeFound;
import com.skytala.eCommerce.event.ContentAttributeUpdated;
import com.skytala.eCommerce.query.FindContentAttributesBy;

@RestController
@RequestMapping("/api/contentAttribute")
public class ContentAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContentAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContentAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContentAttribute
	 * @return a List with the ContentAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContentAttribute> findContentAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindContentAttributesBy query = new FindContentAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (ContentAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentAttributeFound.class,
				event -> sendContentAttributesFoundMessage(((ContentAttributeFound) event).getContentAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContentAttributesFoundMessage(List<ContentAttribute> contentAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contentAttributes);
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
	public boolean createContentAttribute(HttpServletRequest request) {

		ContentAttribute contentAttributeToBeAdded = new ContentAttribute();
		try {
			contentAttributeToBeAdded = ContentAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContentAttribute(contentAttributeToBeAdded);

	}

	/**
	 * creates a new ContentAttribute entry in the ofbiz database
	 * 
	 * @param contentAttributeToBeAdded
	 *            the ContentAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContentAttribute(ContentAttribute contentAttributeToBeAdded) {

		AddContentAttribute com = new AddContentAttribute(contentAttributeToBeAdded);
		int usedTicketId;

		synchronized (ContentAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentAttributeAdded.class,
				event -> sendContentAttributeChangedMessage(((ContentAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContentAttribute(HttpServletRequest request) {

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

		ContentAttribute contentAttributeToBeUpdated = new ContentAttribute();

		try {
			contentAttributeToBeUpdated = ContentAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContentAttribute(contentAttributeToBeUpdated);

	}

	/**
	 * Updates the ContentAttribute with the specific Id
	 * 
	 * @param contentAttributeToBeUpdated the ContentAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContentAttribute(ContentAttribute contentAttributeToBeUpdated) {

		UpdateContentAttribute com = new UpdateContentAttribute(contentAttributeToBeUpdated);

		int usedTicketId;

		synchronized (ContentAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentAttributeUpdated.class,
				event -> sendContentAttributeChangedMessage(((ContentAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContentAttribute from the database
	 * 
	 * @param contentAttributeId:
	 *            the id of the ContentAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontentAttributeById(@RequestParam(value = "contentAttributeId") String contentAttributeId) {

		DeleteContentAttribute com = new DeleteContentAttribute(contentAttributeId);

		int usedTicketId;

		synchronized (ContentAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentAttributeDeleted.class,
				event -> sendContentAttributeChangedMessage(((ContentAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContentAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contentAttribute/\" plus one of the following: "
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

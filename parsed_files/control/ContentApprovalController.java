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
import com.skytala.eCommerce.command.AddContentApproval;
import com.skytala.eCommerce.command.DeleteContentApproval;
import com.skytala.eCommerce.command.UpdateContentApproval;
import com.skytala.eCommerce.entity.ContentApproval;
import com.skytala.eCommerce.entity.ContentApprovalMapper;
import com.skytala.eCommerce.event.ContentApprovalAdded;
import com.skytala.eCommerce.event.ContentApprovalDeleted;
import com.skytala.eCommerce.event.ContentApprovalFound;
import com.skytala.eCommerce.event.ContentApprovalUpdated;
import com.skytala.eCommerce.query.FindContentApprovalsBy;

@RestController
@RequestMapping("/api/contentApproval")
public class ContentApprovalController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ContentApproval>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ContentApprovalController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ContentApproval
	 * @return a List with the ContentApprovals
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ContentApproval> findContentApprovalsBy(@RequestParam Map<String, String> allRequestParams) {

		FindContentApprovalsBy query = new FindContentApprovalsBy(allRequestParams);

		int usedTicketId;

		synchronized (ContentApprovalController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentApprovalFound.class,
				event -> sendContentApprovalsFoundMessage(((ContentApprovalFound) event).getContentApprovals(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendContentApprovalsFoundMessage(List<ContentApproval> contentApprovals, int usedTicketId) {
		queryReturnVal.put(usedTicketId, contentApprovals);
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
	public boolean createContentApproval(HttpServletRequest request) {

		ContentApproval contentApprovalToBeAdded = new ContentApproval();
		try {
			contentApprovalToBeAdded = ContentApprovalMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createContentApproval(contentApprovalToBeAdded);

	}

	/**
	 * creates a new ContentApproval entry in the ofbiz database
	 * 
	 * @param contentApprovalToBeAdded
	 *            the ContentApproval thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createContentApproval(ContentApproval contentApprovalToBeAdded) {

		AddContentApproval com = new AddContentApproval(contentApprovalToBeAdded);
		int usedTicketId;

		synchronized (ContentApprovalController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentApprovalAdded.class,
				event -> sendContentApprovalChangedMessage(((ContentApprovalAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateContentApproval(HttpServletRequest request) {

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

		ContentApproval contentApprovalToBeUpdated = new ContentApproval();

		try {
			contentApprovalToBeUpdated = ContentApprovalMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateContentApproval(contentApprovalToBeUpdated);

	}

	/**
	 * Updates the ContentApproval with the specific Id
	 * 
	 * @param contentApprovalToBeUpdated the ContentApproval thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateContentApproval(ContentApproval contentApprovalToBeUpdated) {

		UpdateContentApproval com = new UpdateContentApproval(contentApprovalToBeUpdated);

		int usedTicketId;

		synchronized (ContentApprovalController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentApprovalUpdated.class,
				event -> sendContentApprovalChangedMessage(((ContentApprovalUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ContentApproval from the database
	 * 
	 * @param contentApprovalId:
	 *            the id of the ContentApproval thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecontentApprovalById(@RequestParam(value = "contentApprovalId") String contentApprovalId) {

		DeleteContentApproval com = new DeleteContentApproval(contentApprovalId);

		int usedTicketId;

		synchronized (ContentApprovalController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ContentApprovalDeleted.class,
				event -> sendContentApprovalChangedMessage(((ContentApprovalDeleted) event).isSuccess(), usedTicketId));

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

	public void sendContentApprovalChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/contentApproval/\" plus one of the following: "
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

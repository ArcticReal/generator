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
import com.skytala.eCommerce.command.AddPostalAddressBoundary;
import com.skytala.eCommerce.command.DeletePostalAddressBoundary;
import com.skytala.eCommerce.command.UpdatePostalAddressBoundary;
import com.skytala.eCommerce.entity.PostalAddressBoundary;
import com.skytala.eCommerce.entity.PostalAddressBoundaryMapper;
import com.skytala.eCommerce.event.PostalAddressBoundaryAdded;
import com.skytala.eCommerce.event.PostalAddressBoundaryDeleted;
import com.skytala.eCommerce.event.PostalAddressBoundaryFound;
import com.skytala.eCommerce.event.PostalAddressBoundaryUpdated;
import com.skytala.eCommerce.query.FindPostalAddressBoundarysBy;

@RestController
@RequestMapping("/api/postalAddressBoundary")
public class PostalAddressBoundaryController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PostalAddressBoundary>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PostalAddressBoundaryController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PostalAddressBoundary
	 * @return a List with the PostalAddressBoundarys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PostalAddressBoundary> findPostalAddressBoundarysBy(@RequestParam Map<String, String> allRequestParams) {

		FindPostalAddressBoundarysBy query = new FindPostalAddressBoundarysBy(allRequestParams);

		int usedTicketId;

		synchronized (PostalAddressBoundaryController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PostalAddressBoundaryFound.class,
				event -> sendPostalAddressBoundarysFoundMessage(((PostalAddressBoundaryFound) event).getPostalAddressBoundarys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPostalAddressBoundarysFoundMessage(List<PostalAddressBoundary> postalAddressBoundarys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, postalAddressBoundarys);
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
	public boolean createPostalAddressBoundary(HttpServletRequest request) {

		PostalAddressBoundary postalAddressBoundaryToBeAdded = new PostalAddressBoundary();
		try {
			postalAddressBoundaryToBeAdded = PostalAddressBoundaryMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPostalAddressBoundary(postalAddressBoundaryToBeAdded);

	}

	/**
	 * creates a new PostalAddressBoundary entry in the ofbiz database
	 * 
	 * @param postalAddressBoundaryToBeAdded
	 *            the PostalAddressBoundary thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPostalAddressBoundary(PostalAddressBoundary postalAddressBoundaryToBeAdded) {

		AddPostalAddressBoundary com = new AddPostalAddressBoundary(postalAddressBoundaryToBeAdded);
		int usedTicketId;

		synchronized (PostalAddressBoundaryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PostalAddressBoundaryAdded.class,
				event -> sendPostalAddressBoundaryChangedMessage(((PostalAddressBoundaryAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePostalAddressBoundary(HttpServletRequest request) {

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

		PostalAddressBoundary postalAddressBoundaryToBeUpdated = new PostalAddressBoundary();

		try {
			postalAddressBoundaryToBeUpdated = PostalAddressBoundaryMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePostalAddressBoundary(postalAddressBoundaryToBeUpdated);

	}

	/**
	 * Updates the PostalAddressBoundary with the specific Id
	 * 
	 * @param postalAddressBoundaryToBeUpdated the PostalAddressBoundary thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePostalAddressBoundary(PostalAddressBoundary postalAddressBoundaryToBeUpdated) {

		UpdatePostalAddressBoundary com = new UpdatePostalAddressBoundary(postalAddressBoundaryToBeUpdated);

		int usedTicketId;

		synchronized (PostalAddressBoundaryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PostalAddressBoundaryUpdated.class,
				event -> sendPostalAddressBoundaryChangedMessage(((PostalAddressBoundaryUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PostalAddressBoundary from the database
	 * 
	 * @param postalAddressBoundaryId:
	 *            the id of the PostalAddressBoundary thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepostalAddressBoundaryById(@RequestParam(value = "postalAddressBoundaryId") String postalAddressBoundaryId) {

		DeletePostalAddressBoundary com = new DeletePostalAddressBoundary(postalAddressBoundaryId);

		int usedTicketId;

		synchronized (PostalAddressBoundaryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PostalAddressBoundaryDeleted.class,
				event -> sendPostalAddressBoundaryChangedMessage(((PostalAddressBoundaryDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPostalAddressBoundaryChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/postalAddressBoundary/\" plus one of the following: "
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

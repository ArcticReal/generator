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
import com.skytala.eCommerce.command.AddPostalAddress;
import com.skytala.eCommerce.command.DeletePostalAddress;
import com.skytala.eCommerce.command.UpdatePostalAddress;
import com.skytala.eCommerce.entity.PostalAddress;
import com.skytala.eCommerce.entity.PostalAddressMapper;
import com.skytala.eCommerce.event.PostalAddressAdded;
import com.skytala.eCommerce.event.PostalAddressDeleted;
import com.skytala.eCommerce.event.PostalAddressFound;
import com.skytala.eCommerce.event.PostalAddressUpdated;
import com.skytala.eCommerce.query.FindPostalAddresssBy;

@RestController
@RequestMapping("/api/postalAddress")
public class PostalAddressController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PostalAddress>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PostalAddressController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PostalAddress
	 * @return a List with the PostalAddresss
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PostalAddress> findPostalAddresssBy(@RequestParam Map<String, String> allRequestParams) {

		FindPostalAddresssBy query = new FindPostalAddresssBy(allRequestParams);

		int usedTicketId;

		synchronized (PostalAddressController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PostalAddressFound.class,
				event -> sendPostalAddresssFoundMessage(((PostalAddressFound) event).getPostalAddresss(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPostalAddresssFoundMessage(List<PostalAddress> postalAddresss, int usedTicketId) {
		queryReturnVal.put(usedTicketId, postalAddresss);
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
	public boolean createPostalAddress(HttpServletRequest request) {

		PostalAddress postalAddressToBeAdded = new PostalAddress();
		try {
			postalAddressToBeAdded = PostalAddressMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPostalAddress(postalAddressToBeAdded);

	}

	/**
	 * creates a new PostalAddress entry in the ofbiz database
	 * 
	 * @param postalAddressToBeAdded
	 *            the PostalAddress thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPostalAddress(PostalAddress postalAddressToBeAdded) {

		AddPostalAddress com = new AddPostalAddress(postalAddressToBeAdded);
		int usedTicketId;

		synchronized (PostalAddressController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PostalAddressAdded.class,
				event -> sendPostalAddressChangedMessage(((PostalAddressAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePostalAddress(HttpServletRequest request) {

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

		PostalAddress postalAddressToBeUpdated = new PostalAddress();

		try {
			postalAddressToBeUpdated = PostalAddressMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePostalAddress(postalAddressToBeUpdated);

	}

	/**
	 * Updates the PostalAddress with the specific Id
	 * 
	 * @param postalAddressToBeUpdated the PostalAddress thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePostalAddress(PostalAddress postalAddressToBeUpdated) {

		UpdatePostalAddress com = new UpdatePostalAddress(postalAddressToBeUpdated);

		int usedTicketId;

		synchronized (PostalAddressController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PostalAddressUpdated.class,
				event -> sendPostalAddressChangedMessage(((PostalAddressUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PostalAddress from the database
	 * 
	 * @param postalAddressId:
	 *            the id of the PostalAddress thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletepostalAddressById(@RequestParam(value = "postalAddressId") String postalAddressId) {

		DeletePostalAddress com = new DeletePostalAddress(postalAddressId);

		int usedTicketId;

		synchronized (PostalAddressController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PostalAddressDeleted.class,
				event -> sendPostalAddressChangedMessage(((PostalAddressDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPostalAddressChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/postalAddress/\" plus one of the following: "
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

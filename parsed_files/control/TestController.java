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
import com.skytala.eCommerce.command.AddTest;
import com.skytala.eCommerce.command.DeleteTest;
import com.skytala.eCommerce.command.UpdateTest;
import com.skytala.eCommerce.entity.Test;
import com.skytala.eCommerce.entity.TestMapper;
import com.skytala.eCommerce.event.TestAdded;
import com.skytala.eCommerce.event.TestDeleted;
import com.skytala.eCommerce.event.TestFound;
import com.skytala.eCommerce.event.TestUpdated;
import com.skytala.eCommerce.query.FindTestsBy;

@RestController
@RequestMapping("/api/test")
public class TestController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<Test>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public TestController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a Test
	 * @return a List with the Tests
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<Test> findTestsBy(@RequestParam Map<String, String> allRequestParams) {

		FindTestsBy query = new FindTestsBy(allRequestParams);

		int usedTicketId;

		synchronized (TestController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TestFound.class,
				event -> sendTestsFoundMessage(((TestFound) event).getTests(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendTestsFoundMessage(List<Test> tests, int usedTicketId) {
		queryReturnVal.put(usedTicketId, tests);
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
	public boolean createTest(HttpServletRequest request) {

		Test testToBeAdded = new Test();
		try {
			testToBeAdded = TestMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createTest(testToBeAdded);

	}

	/**
	 * creates a new Test entry in the ofbiz database
	 * 
	 * @param testToBeAdded
	 *            the Test thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createTest(Test testToBeAdded) {

		AddTest com = new AddTest(testToBeAdded);
		int usedTicketId;

		synchronized (TestController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TestAdded.class,
				event -> sendTestChangedMessage(((TestAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateTest(HttpServletRequest request) {

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

		Test testToBeUpdated = new Test();

		try {
			testToBeUpdated = TestMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateTest(testToBeUpdated);

	}

	/**
	 * Updates the Test with the specific Id
	 * 
	 * @param testToBeUpdated the Test thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateTest(Test testToBeUpdated) {

		UpdateTest com = new UpdateTest(testToBeUpdated);

		int usedTicketId;

		synchronized (TestController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TestUpdated.class,
				event -> sendTestChangedMessage(((TestUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a Test from the database
	 * 
	 * @param testId:
	 *            the id of the Test thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletetestById(@RequestParam(value = "testId") String testId) {

		DeleteTest com = new DeleteTest(testId);

		int usedTicketId;

		synchronized (TestController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TestDeleted.class,
				event -> sendTestChangedMessage(((TestDeleted) event).isSuccess(), usedTicketId));

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

	public void sendTestChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/test/\" plus one of the following: "
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

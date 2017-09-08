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
import com.skytala.eCommerce.command.AddProductSearchConstraint;
import com.skytala.eCommerce.command.DeleteProductSearchConstraint;
import com.skytala.eCommerce.command.UpdateProductSearchConstraint;
import com.skytala.eCommerce.entity.ProductSearchConstraint;
import com.skytala.eCommerce.entity.ProductSearchConstraintMapper;
import com.skytala.eCommerce.event.ProductSearchConstraintAdded;
import com.skytala.eCommerce.event.ProductSearchConstraintDeleted;
import com.skytala.eCommerce.event.ProductSearchConstraintFound;
import com.skytala.eCommerce.event.ProductSearchConstraintUpdated;
import com.skytala.eCommerce.query.FindProductSearchConstraintsBy;

@RestController
@RequestMapping("/api/productSearchConstraint")
public class ProductSearchConstraintController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductSearchConstraint>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductSearchConstraintController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductSearchConstraint
	 * @return a List with the ProductSearchConstraints
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductSearchConstraint> findProductSearchConstraintsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductSearchConstraintsBy query = new FindProductSearchConstraintsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductSearchConstraintController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductSearchConstraintFound.class,
				event -> sendProductSearchConstraintsFoundMessage(((ProductSearchConstraintFound) event).getProductSearchConstraints(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductSearchConstraintsFoundMessage(List<ProductSearchConstraint> productSearchConstraints, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productSearchConstraints);
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
	public boolean createProductSearchConstraint(HttpServletRequest request) {

		ProductSearchConstraint productSearchConstraintToBeAdded = new ProductSearchConstraint();
		try {
			productSearchConstraintToBeAdded = ProductSearchConstraintMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductSearchConstraint(productSearchConstraintToBeAdded);

	}

	/**
	 * creates a new ProductSearchConstraint entry in the ofbiz database
	 * 
	 * @param productSearchConstraintToBeAdded
	 *            the ProductSearchConstraint thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductSearchConstraint(ProductSearchConstraint productSearchConstraintToBeAdded) {

		AddProductSearchConstraint com = new AddProductSearchConstraint(productSearchConstraintToBeAdded);
		int usedTicketId;

		synchronized (ProductSearchConstraintController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductSearchConstraintAdded.class,
				event -> sendProductSearchConstraintChangedMessage(((ProductSearchConstraintAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductSearchConstraint(HttpServletRequest request) {

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

		ProductSearchConstraint productSearchConstraintToBeUpdated = new ProductSearchConstraint();

		try {
			productSearchConstraintToBeUpdated = ProductSearchConstraintMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductSearchConstraint(productSearchConstraintToBeUpdated);

	}

	/**
	 * Updates the ProductSearchConstraint with the specific Id
	 * 
	 * @param productSearchConstraintToBeUpdated the ProductSearchConstraint thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductSearchConstraint(ProductSearchConstraint productSearchConstraintToBeUpdated) {

		UpdateProductSearchConstraint com = new UpdateProductSearchConstraint(productSearchConstraintToBeUpdated);

		int usedTicketId;

		synchronized (ProductSearchConstraintController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductSearchConstraintUpdated.class,
				event -> sendProductSearchConstraintChangedMessage(((ProductSearchConstraintUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductSearchConstraint from the database
	 * 
	 * @param productSearchConstraintId:
	 *            the id of the ProductSearchConstraint thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductSearchConstraintById(@RequestParam(value = "productSearchConstraintId") String productSearchConstraintId) {

		DeleteProductSearchConstraint com = new DeleteProductSearchConstraint(productSearchConstraintId);

		int usedTicketId;

		synchronized (ProductSearchConstraintController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductSearchConstraintDeleted.class,
				event -> sendProductSearchConstraintChangedMessage(((ProductSearchConstraintDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductSearchConstraintChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productSearchConstraint/\" plus one of the following: "
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

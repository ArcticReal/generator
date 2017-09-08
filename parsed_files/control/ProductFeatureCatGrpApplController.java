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
import com.skytala.eCommerce.command.AddProductFeatureCatGrpAppl;
import com.skytala.eCommerce.command.DeleteProductFeatureCatGrpAppl;
import com.skytala.eCommerce.command.UpdateProductFeatureCatGrpAppl;
import com.skytala.eCommerce.entity.ProductFeatureCatGrpAppl;
import com.skytala.eCommerce.entity.ProductFeatureCatGrpApplMapper;
import com.skytala.eCommerce.event.ProductFeatureCatGrpApplAdded;
import com.skytala.eCommerce.event.ProductFeatureCatGrpApplDeleted;
import com.skytala.eCommerce.event.ProductFeatureCatGrpApplFound;
import com.skytala.eCommerce.event.ProductFeatureCatGrpApplUpdated;
import com.skytala.eCommerce.query.FindProductFeatureCatGrpApplsBy;

@RestController
@RequestMapping("/api/productFeatureCatGrpAppl")
public class ProductFeatureCatGrpApplController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductFeatureCatGrpAppl>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductFeatureCatGrpApplController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductFeatureCatGrpAppl
	 * @return a List with the ProductFeatureCatGrpAppls
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductFeatureCatGrpAppl> findProductFeatureCatGrpApplsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductFeatureCatGrpApplsBy query = new FindProductFeatureCatGrpApplsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductFeatureCatGrpApplController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureCatGrpApplFound.class,
				event -> sendProductFeatureCatGrpApplsFoundMessage(((ProductFeatureCatGrpApplFound) event).getProductFeatureCatGrpAppls(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductFeatureCatGrpApplsFoundMessage(List<ProductFeatureCatGrpAppl> productFeatureCatGrpAppls, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productFeatureCatGrpAppls);
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
	public boolean createProductFeatureCatGrpAppl(HttpServletRequest request) {

		ProductFeatureCatGrpAppl productFeatureCatGrpApplToBeAdded = new ProductFeatureCatGrpAppl();
		try {
			productFeatureCatGrpApplToBeAdded = ProductFeatureCatGrpApplMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductFeatureCatGrpAppl(productFeatureCatGrpApplToBeAdded);

	}

	/**
	 * creates a new ProductFeatureCatGrpAppl entry in the ofbiz database
	 * 
	 * @param productFeatureCatGrpApplToBeAdded
	 *            the ProductFeatureCatGrpAppl thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductFeatureCatGrpAppl(ProductFeatureCatGrpAppl productFeatureCatGrpApplToBeAdded) {

		AddProductFeatureCatGrpAppl com = new AddProductFeatureCatGrpAppl(productFeatureCatGrpApplToBeAdded);
		int usedTicketId;

		synchronized (ProductFeatureCatGrpApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureCatGrpApplAdded.class,
				event -> sendProductFeatureCatGrpApplChangedMessage(((ProductFeatureCatGrpApplAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductFeatureCatGrpAppl(HttpServletRequest request) {

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

		ProductFeatureCatGrpAppl productFeatureCatGrpApplToBeUpdated = new ProductFeatureCatGrpAppl();

		try {
			productFeatureCatGrpApplToBeUpdated = ProductFeatureCatGrpApplMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductFeatureCatGrpAppl(productFeatureCatGrpApplToBeUpdated);

	}

	/**
	 * Updates the ProductFeatureCatGrpAppl with the specific Id
	 * 
	 * @param productFeatureCatGrpApplToBeUpdated the ProductFeatureCatGrpAppl thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductFeatureCatGrpAppl(ProductFeatureCatGrpAppl productFeatureCatGrpApplToBeUpdated) {

		UpdateProductFeatureCatGrpAppl com = new UpdateProductFeatureCatGrpAppl(productFeatureCatGrpApplToBeUpdated);

		int usedTicketId;

		synchronized (ProductFeatureCatGrpApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureCatGrpApplUpdated.class,
				event -> sendProductFeatureCatGrpApplChangedMessage(((ProductFeatureCatGrpApplUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductFeatureCatGrpAppl from the database
	 * 
	 * @param productFeatureCatGrpApplId:
	 *            the id of the ProductFeatureCatGrpAppl thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductFeatureCatGrpApplById(@RequestParam(value = "productFeatureCatGrpApplId") String productFeatureCatGrpApplId) {

		DeleteProductFeatureCatGrpAppl com = new DeleteProductFeatureCatGrpAppl(productFeatureCatGrpApplId);

		int usedTicketId;

		synchronized (ProductFeatureCatGrpApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureCatGrpApplDeleted.class,
				event -> sendProductFeatureCatGrpApplChangedMessage(((ProductFeatureCatGrpApplDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductFeatureCatGrpApplChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productFeatureCatGrpAppl/\" plus one of the following: "
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

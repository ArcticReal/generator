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
import com.skytala.eCommerce.command.AddProductPriceAutoNotice;
import com.skytala.eCommerce.command.DeleteProductPriceAutoNotice;
import com.skytala.eCommerce.command.UpdateProductPriceAutoNotice;
import com.skytala.eCommerce.entity.ProductPriceAutoNotice;
import com.skytala.eCommerce.entity.ProductPriceAutoNoticeMapper;
import com.skytala.eCommerce.event.ProductPriceAutoNoticeAdded;
import com.skytala.eCommerce.event.ProductPriceAutoNoticeDeleted;
import com.skytala.eCommerce.event.ProductPriceAutoNoticeFound;
import com.skytala.eCommerce.event.ProductPriceAutoNoticeUpdated;
import com.skytala.eCommerce.query.FindProductPriceAutoNoticesBy;

@RestController
@RequestMapping("/api/productPriceAutoNotice")
public class ProductPriceAutoNoticeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductPriceAutoNotice>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductPriceAutoNoticeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductPriceAutoNotice
	 * @return a List with the ProductPriceAutoNotices
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductPriceAutoNotice> findProductPriceAutoNoticesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductPriceAutoNoticesBy query = new FindProductPriceAutoNoticesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductPriceAutoNoticeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPriceAutoNoticeFound.class,
				event -> sendProductPriceAutoNoticesFoundMessage(((ProductPriceAutoNoticeFound) event).getProductPriceAutoNotices(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductPriceAutoNoticesFoundMessage(List<ProductPriceAutoNotice> productPriceAutoNotices, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productPriceAutoNotices);
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
	public boolean createProductPriceAutoNotice(HttpServletRequest request) {

		ProductPriceAutoNotice productPriceAutoNoticeToBeAdded = new ProductPriceAutoNotice();
		try {
			productPriceAutoNoticeToBeAdded = ProductPriceAutoNoticeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductPriceAutoNotice(productPriceAutoNoticeToBeAdded);

	}

	/**
	 * creates a new ProductPriceAutoNotice entry in the ofbiz database
	 * 
	 * @param productPriceAutoNoticeToBeAdded
	 *            the ProductPriceAutoNotice thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductPriceAutoNotice(ProductPriceAutoNotice productPriceAutoNoticeToBeAdded) {

		AddProductPriceAutoNotice com = new AddProductPriceAutoNotice(productPriceAutoNoticeToBeAdded);
		int usedTicketId;

		synchronized (ProductPriceAutoNoticeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPriceAutoNoticeAdded.class,
				event -> sendProductPriceAutoNoticeChangedMessage(((ProductPriceAutoNoticeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductPriceAutoNotice(HttpServletRequest request) {

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

		ProductPriceAutoNotice productPriceAutoNoticeToBeUpdated = new ProductPriceAutoNotice();

		try {
			productPriceAutoNoticeToBeUpdated = ProductPriceAutoNoticeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductPriceAutoNotice(productPriceAutoNoticeToBeUpdated);

	}

	/**
	 * Updates the ProductPriceAutoNotice with the specific Id
	 * 
	 * @param productPriceAutoNoticeToBeUpdated the ProductPriceAutoNotice thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductPriceAutoNotice(ProductPriceAutoNotice productPriceAutoNoticeToBeUpdated) {

		UpdateProductPriceAutoNotice com = new UpdateProductPriceAutoNotice(productPriceAutoNoticeToBeUpdated);

		int usedTicketId;

		synchronized (ProductPriceAutoNoticeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPriceAutoNoticeUpdated.class,
				event -> sendProductPriceAutoNoticeChangedMessage(((ProductPriceAutoNoticeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductPriceAutoNotice from the database
	 * 
	 * @param productPriceAutoNoticeId:
	 *            the id of the ProductPriceAutoNotice thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductPriceAutoNoticeById(@RequestParam(value = "productPriceAutoNoticeId") String productPriceAutoNoticeId) {

		DeleteProductPriceAutoNotice com = new DeleteProductPriceAutoNotice(productPriceAutoNoticeId);

		int usedTicketId;

		synchronized (ProductPriceAutoNoticeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductPriceAutoNoticeDeleted.class,
				event -> sendProductPriceAutoNoticeChangedMessage(((ProductPriceAutoNoticeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductPriceAutoNoticeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productPriceAutoNotice/\" plus one of the following: "
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

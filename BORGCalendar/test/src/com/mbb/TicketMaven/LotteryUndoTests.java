package com.mbb.TicketMaven;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mbb.TicketMaven.common.util.Errmsg;
import com.mbb.TicketMaven.model.CustomerModel;
import com.mbb.TicketMaven.model.LayoutModel;
import com.mbb.TicketMaven.model.LotteryManager;
import com.mbb.TicketMaven.model.SeatModel;
import com.mbb.TicketMaven.model.ShowModel;
import com.mbb.TicketMaven.model.TicketModel;
import com.mbb.TicketMaven.model.TicketRequestModel;
import com.mbb.TicketMaven.model.ZoneModel;
import com.mbb.TicketMaven.model.bean.Customer;
import com.mbb.TicketMaven.model.bean.Show;
import com.mbb.TicketMaven.model.bean.TicketRequest;
import com.mbb.TicketMaven.model.jdbc.JdbcDB;

public class LotteryUndoTests {

	@BeforeClass
	public static void setUp() throws Exception {

		Errmsg.console(true);

		CustomerModel.getReference().open_db("jdbc:hsqldb:mem:whatever", "tm");
		TicketModel.getReference().open_db("jdbc:hsqldb:mem:whatever", "tm");
		ShowModel.getReference().open_db("jdbc:hsqldb:mem:whatever", "tm");
		SeatModel.getReference().open_db("jdbc:hsqldb:mem:whatever", "tm");
		LayoutModel.getReference().open_db("jdbc:hsqldb:mem:whatever", "tm");
		ZoneModel.getReference().open_db("jdbc:hsqldb:mem:whatever", "tm");
		TicketRequestModel.getReference().open_db("jdbc:hsqldb:mem:whatever", "tm");

		// import test data
		FileInputStream is = new FileInputStream("test/data/data.sql");
		StringBuffer sb = new StringBuffer();
		InputStreamReader r = new InputStreamReader(is);
		while (true) {
			int ch = r.read();
			if (ch == -1)
				break;
			sb.append((char) ch);
		}
		JdbcDB.execSQL(sb.toString());
		is.close();
	}

	@Test
	public void testLotteryUndo() throws Exception {
		Collection<Customer> customers = CustomerModel.getReference()
				.getRecords();
		System.out.println("num custs=" + customers.size());

		// make a copy of all customers
		Collection<Customer> orig_custs = new ArrayList<Customer>();
		for (Customer c : customers) {
			Customer oc = (Customer) c.copy();
			orig_custs.add(oc);
		}

		// make a copy of all requests
		Collection<TicketRequest> reqs = TicketRequestModel.getReference()
				.getRecords();
		Collection<TicketRequest> orig_reqs = new ArrayList<TicketRequest>();
		for (TicketRequest c : reqs) {
			TicketRequest oc = (TicketRequest) c.copy();
			orig_reqs.add(oc);
		}

		// save number fo requests and tickets
		int num_req = TicketRequestModel.getReference().numRows();
		int num_tkt = TicketModel.getReference().numRows();

		System.out.println("Before Lotteries: requests=" + num_req
				+ "  tickets=" + num_tkt);

		// run all possible lotteries
		Collection<Show> shows = ShowModel.getReference().getRecords();
		for (Show s : shows) {
			new LotteryManager(s.getKey()).runLottery();
		}

		int num_req_a = TicketRequestModel.getReference().numRows();
		int num_tkt_a = TicketModel.getReference().numRows();
		System.out.println("After Lotteries: requests=" + num_req_a
				+ "  tickets=" + num_tkt_a);

		// undo all lotteries
		for (Show s : shows) {
			new LotteryManager(s.getKey()).undoLottery();
		}

		num_req_a = TicketRequestModel.getReference().numRows();
		num_tkt_a = TicketModel.getReference().numRows();

		Assert.assertTrue("Number of requests don't match: " + num_req + "!="
				+ num_req_a, num_req == num_req_a);
		Assert.assertTrue("Number of tickets don't match: " + num_tkt + "!="
				+ num_tkt_a, num_tkt == num_tkt_a);

		// verify that every customer has been reset properly
		for (Customer oc : orig_custs) {
			Customer c = CustomerModel.getReference().getCustomer(oc.getKey());
			Assert.assertTrue("total tickets does not match for customer: "
					+ c.getKey() + " " + c.getTotalTickets().intValue() + "!="
					+ oc.getTotalTickets().intValue(), c.getTotalTickets()
					.intValue() == oc.getTotalTickets().intValue());
			Assert.assertTrue("total quality does not match for customer: "
					+ c.getKey() + " " + c.getTotalQuality().intValue() + "!="
					+ oc.getTotalQuality().intValue(), c.getTotalQuality()
					.intValue() == oc.getTotalQuality().intValue());
		}

		// verify equivalent requests
		for (TicketRequest oc : orig_reqs) {
			Collection<TicketRequest> crs = TicketRequestModel.getReference()
					.getRequestsForCustomer(oc.getCustomerId().intValue());
			TicketRequest c = null;
			for (TicketRequest tr : crs) {
				if (tr.getCustomerId().intValue() == oc.getCustomerId()
						.intValue()
						&& tr.getShowId().intValue() == oc.getShowId()
								.intValue()) {
					// equivalent request
					c = tr;
					break;
				}

				Assert.assertNotNull("Didn't find matching request for tr: " + oc.getKey(), c);
				Assert.assertTrue("total tickets does not match for request: "
						+ c.getKey() + " " + c.getTickets().intValue()
						+ "!=" + oc.getTickets().intValue(), c
						.getTickets().intValue() == oc.getTickets()
						.intValue());
			}
		}
	}

	@AfterClass
	public static void tearDown() {
		try {
			JdbcDB.cleanup();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}

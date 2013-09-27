package net.sf.borg.test;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.List;

import net.sf.borg.model.CheckListModel;
import net.sf.borg.model.db.DBHelper;
import net.sf.borg.model.db.jdbc.JdbcDBHelper;
import net.sf.borg.model.entity.CheckList;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class CheckListTest {

	@BeforeClass
	public static void setUp() throws Exception {
		// open the borg dbs - in memory
		DBHelper.setFactory(new JdbcDBHelper());
		DBHelper.setController(new JdbcDBHelper());
		DBHelper.getController().connect("jdbc:hsqldb:mem:whatever");
		
	}
	
	@Test
	public void testCRUD() throws Exception
	{
		
		CheckList cl1 = new CheckList();
		cl1.setCheckListName("clname1");
		CheckList.Item item1 = new CheckList.Item();
		item1.setText("itemtext1");
		item1.setChecked(Boolean.FALSE);
		cl1.getItems().add(item1);
		CheckList.Item item2 = new CheckList.Item();
		item2.setText("itemtext2");
		item2.setChecked(Boolean.TRUE);
		cl1.getItems().add(item2);
		
		CheckListModel.getReference().saveCheckList(cl1);
		
		Collection<String> names = CheckListModel.getReference().getNames();
		assertEquals(names.size(), 1);
		
		CheckList read1 = CheckListModel.getReference().getCheckList("clname1");
		assertEquals(read1.getCheckListName(), "clname1");
		List<CheckList.Item> items = read1.getItems();
		assertEquals(2, items.size());
		for( CheckList.Item item : items )
		{
			if( item.getText().equals("itemtext1") )
				assertEquals(item.getChecked(), Boolean.FALSE);
			else if( item.getText().equals("itemtext2") )
				assertEquals(item.getChecked(), Boolean.TRUE);
			else
				throw new Exception("Unknown Item Found");
				
		}
		assertEquals(items.get(0).getText(), "itemtext1");
		assertEquals(items.get(1).getText(), "itemtext2");
		assertEquals(items.get(1).getChecked(), Boolean.TRUE);
		
		CheckList.Item item3 = new CheckList.Item();
		item1.setText("itemtext3");
		item1.setChecked(Boolean.TRUE);
		read1.getItems().add(item3);
		
		CheckListModel.getReference().saveCheckList(read1);
		CheckList read2 = CheckListModel.getReference().getCheckList("clname1");
		items = read2.getItems();
		assertEquals(3, items.size());
		
		CheckListModel.getReference().delete("clname1", false);
		
		assertEquals( CheckListModel.getReference().getCheckLists().size(), 0);

		

	}
	
	
	@AfterClass
	public static void tearDown()
	{
		// empty
	}

}

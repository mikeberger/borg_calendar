package net.sf.borg.todoconduit;

import palm.conduit.*;
import java.io.*;
import java.util.*;

//"Portions copyright (c) 1996-2002 PalmSource, Inc. or its affiliates.  All rights reserved."
public class CategoryManager {

    private int db;
    SyncProperties props;
    byte[] bytes; //the array of bytes from the hand-held device which contains the AppInfoBlock

    public CategoryManager(SyncProperties props, int db) {
        this.props = props;
        this.db = db;
    }

	/** Accesses the AppInfoBlock from the hand-held device and parses it into a Vector
	 *	of category objects.	
	 *	@return A Vector of category objects
	 */
    public Vector getHHCategories() throws IOException {
        //use the javasync api to read the AppInfoBlock from the AddressDB
        bytes = SyncManager.readDBAppInfoBlock(db, props.remoteNames[0]);
        return Category.parseCategories(bytes);
    }


	/** 
	 * Writes a Vector of category objects back to the device.
	 * @param hhCategories A Vector of category objects to be written to the device
	 */
    public void writeHHCategories(Vector hhCategories) throws IOException {
        byte []tmp = Category.toBytes(hhCategories);
        System.arraycopy(tmp, 0, bytes, 0, tmp.length);

        //write the synchronized categories to the device
        SyncManager.writeDBAppInfoBlock(db, props.remoteNames[0], bytes);
    }


	//this method searches for a particular category name within a vector of categories
    public Category matchName(String name, Vector categories) {

        Category category;
        String categoryName;

        for (int i = 0; i < categories.size(); i++) {

            category = (Category)categories.elementAt(i);

            categoryName = category.getName();

            if (categoryName != "" && name.equals(categoryName)) {
                return category;
            }
        }
        return null;
    }
    
	//this method searches for a particular category id within a vector of categories
    public Category matchId(int id, Vector categories) {

      /*
        Category category;
        System.out.println(id);
        for (int i = 0; i < categories.size(); i++) {

            category = (Category)categories.elementAt(i);
            System.out.println( category.getId() + " " + category.getName());
            if (id == category.getId() && id != 0) {
                return category;
            }
        }
        return null;
        
    }
    */
        
        return( (Category)categories.elementAt(id));
    
    }
    
    public int getNextIndex(Vector categories) {

        int nextIndex = -1;
        Category tempCategory;

        for (int i = 0; i < categories.size(); i++) {

            tempCategory = (Category)categories.elementAt(i);

            if (tempCategory.getName().equals("")){
                nextIndex = tempCategory.getIndex();
				return nextIndex;
            }
        }
        return nextIndex;
    }
}








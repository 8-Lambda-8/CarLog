package com.a8lambda8.carlog;

import java.util.List;
import java.util.Vector;

/**
 * Created by Jakob Wasle on 16.09.2017.
 */

public class list_Item_list {

    //private int itemcount = 0;
    private List<list_Item> itemlist;

    list_Item_list () {
        itemlist = new Vector<>();
    }

    void addItem(list_Item item) {
        itemlist.add(item);
        //itemcount++;
    }

    list_Item getItem(int location) {
        return itemlist.get(location);
    }

    List<list_Item> getAllItems() {
        return itemlist;
    }

    /*int getItemCount() {
        return itemcount;
    }*/

    void clear(){
        itemlist.clear();
    }

}

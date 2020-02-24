package com.a8lambda8.carlog;

import java.util.List;
import java.util.Vector;

/**
 * Created by Jakob Wasle on 16.09.2017.
 */

class trip_Item_list {

    //private int itemcount = 0;
    private List<trip_Item> itemlist;

    trip_Item_list() {
        itemlist = new Vector<>();
    }

    void addItem(trip_Item item) {
        itemlist.add(item);
        //itemcount++;
    }

    trip_Item getItem(int location) {
        return itemlist.get(location);
    }

    List<trip_Item> getAllItems() {
        return itemlist;
    }

    int size() {
        return itemlist.size();
    }

    void clear(){
        itemlist.clear();
    }

}

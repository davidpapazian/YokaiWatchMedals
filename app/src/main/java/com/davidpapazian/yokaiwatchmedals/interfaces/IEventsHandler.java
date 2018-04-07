package com.davidpapazian.yokaiwatchmedals.interfaces;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.davidpapazian.yokaiwatchmedals.medalLibrary.Item.Item;

public interface IEventsHandler {
    void onClick(View v, int position, Item item);
    boolean onLongClick(View v, int position, Item item);
    void onCtxClick(View v, int position, Item item);
    void onUpdateFinished(RecyclerView.Adapter adapter);
}
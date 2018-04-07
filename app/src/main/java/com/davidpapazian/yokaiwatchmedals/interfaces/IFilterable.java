package com.davidpapazian.yokaiwatchmedals.interfaces;

import android.widget.Filter;

public interface IFilterable {
    boolean enableSearchOption();
    Filter getFilter();
    void restoreList();
    void setTopPanelVisibility(boolean visible);
}
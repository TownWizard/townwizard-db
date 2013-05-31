package com.townwizard.db.model.paging;

import java.util.Collections;
import java.util.List;

public class Page <T> {

    private int page;
    private boolean more;
    private List<T> objects;
    
    public Page(List<T> objects, int page, boolean more) {
        this.objects = objects;
        this.page = page;
        this.more = more;
    }

    public List<T> getObjects() {
        return objects;
    }

    public int getPage() {
        return page;
    }

    public boolean isMore() {
        return more;
    }
    
    public static <T> Page<T> empty() {
        return new Page<>(Collections.<T>emptyList(), 0, false);
    }
    
}

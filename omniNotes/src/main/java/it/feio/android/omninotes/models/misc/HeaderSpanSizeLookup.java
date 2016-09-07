package it.feio.android.omninotes.models.misc;

import android.support.v7.widget.GridLayoutManager;
import it.feio.android.omninotes.models.adapters.NoteAdapter;


public class HeaderSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {

    private NoteAdapter adapter;
    private int mSpanSize = 1;

    public HeaderSpanSizeLookup(NoteAdapter adapter, int spanSize) {
        this.adapter = adapter;
        this.mSpanSize = spanSize;
    }

    @Override
    public int getSpanSize(int position) {
        boolean isHeaderOrFooter = adapter.isHeader(position) || adapter.isFooter(position);
        return isHeaderOrFooter ? mSpanSize : 1;
    }
}
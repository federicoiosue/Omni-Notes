package it.feio.android.omninotes.models.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import it.feio.android.omninotes.models.adapters.NoteAdapter;


public class ExRecyclerView extends RecyclerView {
    public ExRecyclerView(Context context) {
        super(context);
    }

    public ExRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ExRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setHeaderView(View view) {
        RecyclerView.Adapter outerAdapter = getAdapter();
        if (outerAdapter != null) {
            NoteAdapter headerAndFooterAdapter = (NoteAdapter) outerAdapter;
            if (headerAndFooterAdapter.getHeaderViewsCount() == 0) {
                headerAndFooterAdapter.addHeaderView(view);
            }
        }
    }

    public void setFooterView(View view) {
        RecyclerView.Adapter outerAdapter = getAdapter();
        if (outerAdapter != null) {
            NoteAdapter headerAndFooterAdapter = (NoteAdapter) outerAdapter;
            if (headerAndFooterAdapter.getFooterViewsCount() == 0) {
                headerAndFooterAdapter.addFooterView(view);
            }
        }
    }

    public void removeFooterView() {
        RecyclerView.Adapter outerAdapter = getAdapter();
        if (outerAdapter != null) {
            int footerViewCounter = ((NoteAdapter) outerAdapter).getFooterViewsCount();
            if (footerViewCounter > 0) {
                View footerView = ((NoteAdapter) outerAdapter).getFooterView();
                ((NoteAdapter) outerAdapter).removeFooterView(footerView);
            }
        }
    }

    public void removeHeaderView() {
        RecyclerView.Adapter outerAdapter = getAdapter();
        if (outerAdapter != null) {
            int headerViewCounter = ((NoteAdapter) outerAdapter).getHeaderViewsCount();
            if (headerViewCounter > 0) {
                View headerView = ((NoteAdapter) outerAdapter).getHeaderView();
                ((NoteAdapter) outerAdapter).removeFooterView(headerView);
            }
        }
    }

    public int getLayoutPosition(RecyclerView.ViewHolder holder) {
        RecyclerView.Adapter outerAdapter = getAdapter();
        if (outerAdapter != null) {
            int headerViewCounter = ((NoteAdapter) outerAdapter).getHeaderViewsCount();
            if (headerViewCounter > 0) {
                return holder.getLayoutPosition() - headerViewCounter;
            }
        }

        return holder.getLayoutPosition();
    }

    public int getAdapterPosition(RecyclerView.ViewHolder holder) {
        RecyclerView.Adapter outerAdapter = getAdapter();
        if (outerAdapter != null) {
            int headerViewCounter = ((NoteAdapter) outerAdapter).getHeaderViewsCount();
            if (headerViewCounter > 0) {
                return holder.getAdapterPosition() - headerViewCounter;
            }
        }

        return holder.getAdapterPosition();
    }
}

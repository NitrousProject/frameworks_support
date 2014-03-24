/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package android.support.v17.leanback.widget;

import android.database.Cursor;
import android.support.v17.leanback.database.CursorMapper;
import android.util.LruCache;

/**
 * Adapter implemented with a {@link Cursor}.
 */
public class CursorObjectAdapter extends ObjectAdapter {
    private static final int CACHE_SIZE = 100;
    private Cursor mCursor;
    private CursorMapper mMapper;
    private final LruCache<Integer, Object> mItemCache = new LruCache<Integer, Object>(CACHE_SIZE);

    public CursorObjectAdapter(PresenterSelector presenterSelector) {
        super(presenterSelector);
    }

    public CursorObjectAdapter(Presenter presenter) {
        super(presenter);
    }

    public CursorObjectAdapter() {
        super();
    }

    /**
     * Change the underlying cursor to a new cursor. If there is
     * an existing cursor it will be closed.
     *
     * @param cursor The new cursor to be used.
     */
    public void changeCursor(Cursor cursor) {
        if (cursor == mCursor) {
            return;
        }
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = cursor;
        mItemCache.trimToSize(0);
        onCursorChanged();
    }

    /**
     * Swap in a new Cursor, returning the old Cursor. Unlike changeCursor(Cursor),
     * the returned old Cursor is not closed.
     *
     * @param cursor The new cursor to be used.
     */
    public Cursor swapCursor(Cursor cursor) {
        if (cursor == mCursor) {
            return mCursor;
        }
        Cursor oldCursor = mCursor;
        mCursor = cursor;
        mItemCache.trimToSize(0);
        onCursorChanged();
        return oldCursor;
    }

    /**
     * Called whenever the cursor changes.
     */
    protected void onCursorChanged() {
        notifyChanged();
    }

    /**
     * Gets the {@link Cursor} backing the adapter.
     */
     public final Cursor getCursor() {
        return mCursor;
    }

    /**
     * Sets the {@link CursorMapper} used to convert {@link Cursor} rows into
     * Objects.
     */
    public final void setMapper(CursorMapper mapper) {
        mMapper = mapper;
    }

    /**
     * Gets the {@link CursorMapper} used to convert {@link Cursor} rows into
     * Objects.
     */
    public final CursorMapper getMapper() {
        return mMapper;
    }

    @Override
    public int size() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    @Override
    public Object get(int index) {
        if (mCursor == null) {
            return null;
        }
        if (!mCursor.moveToPosition(index)) {
            throw new ArrayIndexOutOfBoundsException();
        }
        Object item = mItemCache.get(index);
        if (item != null) {
            return item;
        }
        item = mMapper.convert(mCursor);
        mItemCache.put(index, item);
        return item;
    }

    /**
     * Closes this adapter, closing the backing {@link Cursor} as well.
     */
    public void close() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
    }

    /**
     * Checks whether the adapter, and hence the backing {@link Cursor}, is closed.
     */
    public boolean isClosed() {
        return mCursor == null || mCursor.isClosed();
    }

    /**
     * Remove an item from the cache. This will force the item to be re-read
     * from the data source the next time (@link #get(int)} is called.
     */
    protected final void invalidateCache(int index) {
        mItemCache.remove(index);
    }

    /**
     * Remove {@code count} items starting at {@code index}.
     */
    protected final void invalidateCache(int index, int count) {
        for (int limit = count + index; index < limit; index++) {
            invalidateCache(index);
        }
    }
}

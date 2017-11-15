package com.cleanonfire.api.data.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by heitorgianastasio on 03/10/17.
 */

public abstract class BaseCleanDAO<T, I extends BaseCleanDAO.Identification> {
    protected SQLiteCleanHelper dbHelper;

    public BaseCleanDAO(SQLiteCleanHelper dbHelper) {
        this.dbHelper = dbHelper;

    }

    protected abstract String getIdentificationCondition();

    protected abstract String getTableName();

    protected abstract T parseFromCursorReader(CleanCursorReader cursor);

    protected abstract ContentValues parseToContentValues(T t);

    protected abstract I getId(T t);

    public List<T> getAll() {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            Cursor cursor = queryFromCriteria(QueryCriteria.EMPTY, db);
            List<T> result = new ArrayList<>();
            CleanCursorReader reader = new CleanCursorReader(cursor);
            if (cursor.moveToFirst()) {
                do {
                    result.add(parseFromCursorReader(reader));
                } while (cursor.moveToNext());
            }
            return result;
        }
    }

    protected T getById(I identification) {
        QueryCriteria criteria = QueryCriteria
                .builder()
                .setSelection(getIdentificationCondition())
                .setSelectionArgs(identification.identificationArgs())
                .build();
        Cursor cursor = queryFromCriteria(criteria, dbHelper.getReadableDatabase());
        CleanCursorReader reader = new CleanCursorReader(cursor);
        if (cursor.moveToFirst()) {
            return parseFromCursorReader(reader);
        } else {
            return null;
        }
    }

    public List<T> query(QueryCriteria queryCriteria) {
        Cursor cursor = queryFromCriteria(queryCriteria, dbHelper.getReadableDatabase());
        List<T> result = new ArrayList<>();
        CleanCursorReader reader = new CleanCursorReader(cursor);
        if (cursor.moveToFirst()) {
            do {
                result.add(parseFromCursorReader(reader));
            } while (cursor.moveToNext());

        }

        return result;
    }

    protected abstract I insertOrThrow(SQLiteDatabase db, T t) throws SQLException;


    public I save(T t) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            return insertOrThrow(db, t);
        } catch (SQLException e) {
            I id = getId(t);
            db.update(getTableName(), parseToContentValues(t), getIdentificationCondition(), id.identificationArgs());
            return id;
        } catch (Exception e) {
            throw e;
        } finally {
            db.close();
        }
    }

    public List<I> save(List<T> ts) {
        List<I> ids = new ArrayList<>();
        for (T t : ts) {
            ids.add(save(t));
        }
        return ids;
    }


    public List<I> save(T... ts) {
        return save(Arrays.asList(ts));

    }

    public int update(ContentValues values, ModificationCriteria criteria) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            return db.update(getTableName(), values, criteria.getSelection(), criteria.selectionArgs);
        }
    }

    public List<I> delete(List<T> elements) {
        List<I> idList = new ArrayList<>();
        for (T element : elements) {
            idList.add(deleteById(getId(element)));
        }
        return idList;
    }

    public List<I> delete(T... elements) {
        return delete(Arrays.asList(elements));
    }

    public I delete(T t) {
        return deleteById(getId(t));
    }

    protected I deleteById(I id) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            int rows = db.delete(getTableName(), getIdentificationCondition(), id.identificationArgs());
            if (rows > 0)
                return id;
            else
                return null;
        }
    }

    public int delete(ModificationCriteria criteria) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            return db.delete(getTableName(), criteria.getSelection(), criteria.getSelectionArgs());
        }
    }

    public List<T> rawQuery(String sql, String... args) {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sql, args);
        List<T> result = new ArrayList<>();
        CleanCursorReader reader = new CleanCursorReader(cursor);
        if (cursor.moveToFirst()) {
            do {
                result.add(parseFromCursorReader(reader));
            } while (cursor.moveToNext());
        }
        return result;
    }


    private Cursor queryFromCriteria(QueryCriteria criteria, SQLiteDatabase db) {
        return db.query(
                criteria.isDistinct(),
                getTableName(),
                criteria.getColumns(),
                criteria.getSelection(),
                criteria.getSelectionArgs(),
                criteria.getGroupBy(),
                criteria.getHaving(),
                criteria.getOrderBy(),
                criteria.getLimit()
        );
    }


    public interface Identification {
        String[] identificationArgs();
    }
}

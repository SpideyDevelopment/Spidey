/*
 * This file is generated by jOOQ.
 */
package dev.mlnr.spidey.jooq.tables;


import dev.mlnr.spidey.jooq.Keys;
import dev.mlnr.spidey.jooq.Public;
import dev.mlnr.spidey.jooq.tables.records.SettingsFiltersRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import java.util.Arrays;
import java.util.List;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class SettingsFilters extends TableImpl<SettingsFiltersRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.settings_filters</code>
     */
    public static final SettingsFilters SETTINGS_FILTERS = new SettingsFilters();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<SettingsFiltersRecord> getRecordType() {
        return SettingsFiltersRecord.class;
    }

    /**
     * The column <code>public.settings_filters.guild_id</code>.
     */
    public final TableField<SettingsFiltersRecord, Long> GUILD_ID = createField(DSL.name("guild_id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.settings_filters.pinned_deleting_enabled</code>.
     */
    public final TableField<SettingsFiltersRecord, Boolean> PINNED_DELETING_ENABLED = createField(DSL.name("pinned_deleting_enabled"), SQLDataType.BOOLEAN.defaultValue(DSL.field("false", SQLDataType.BOOLEAN)), this, "");

    /**
     * The column <code>public.settings_filters.invite_deleting_enabled</code>.
     */
    public final TableField<SettingsFiltersRecord, Boolean> INVITE_DELETING_ENABLED = createField(DSL.name("invite_deleting_enabled"), SQLDataType.BOOLEAN.defaultValue(DSL.field("false", SQLDataType.BOOLEAN)), this, "");

    private SettingsFilters(Name alias, Table<SettingsFiltersRecord> aliased) {
        this(alias, aliased, null);
    }

    private SettingsFilters(Name alias, Table<SettingsFiltersRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.settings_filters</code> table reference
     */
    public SettingsFilters(String alias) {
        this(DSL.name(alias), SETTINGS_FILTERS);
    }

    /**
     * Create an aliased <code>public.settings_filters</code> table reference
     */
    public SettingsFilters(Name alias) {
        this(alias, SETTINGS_FILTERS);
    }

    /**
     * Create a <code>public.settings_filters</code> table reference
     */
    public SettingsFilters() {
        this(DSL.name("settings_filters"), null);
    }

    public <O extends Record> SettingsFilters(Table<O> child, ForeignKey<O, SettingsFiltersRecord> key) {
        super(child, key, SETTINGS_FILTERS);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public UniqueKey<SettingsFiltersRecord> getPrimaryKey() {
        return Keys.SETTINGS_FILTERS_PKEY;
    }

    @Override
    public List<UniqueKey<SettingsFiltersRecord>> getKeys() {
        return Arrays.<UniqueKey<SettingsFiltersRecord>>asList(Keys.SETTINGS_FILTERS_PKEY);
    }

    @Override
    public List<ForeignKey<SettingsFiltersRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<SettingsFiltersRecord, ?>>asList(Keys.SETTINGS_FILTERS__SETTINGS_FILTERS_GUILD_ID_FKEY);
    }

    private transient Guilds _guilds;

    public Guilds guilds() {
        if (_guilds == null)
            _guilds = new Guilds(this, Keys.SETTINGS_FILTERS__SETTINGS_FILTERS_GUILD_ID_FKEY);

        return _guilds;
    }

    @Override
    public SettingsFilters as(String alias) {
        return new SettingsFilters(DSL.name(alias), this);
    }

    @Override
    public SettingsFilters as(Name alias) {
        return new SettingsFilters(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public SettingsFilters rename(String name) {
        return new SettingsFilters(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public SettingsFilters rename(Name name) {
        return new SettingsFilters(name, null);
    }

    // -------------------------------------------------------------------------
    // Row3 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row3<Long, Boolean, Boolean> fieldsRow() {
        return (Row3) super.fieldsRow();
    }
}

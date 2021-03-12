/*
 * This file is generated by jOOQ.
 */
package dev.mlnr.spidey.jooq.tables.records;


import dev.mlnr.spidey.jooq.tables.SettingsFilters;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class SettingsFiltersRecord extends UpdatableRecordImpl<SettingsFiltersRecord> implements Record3<Long, Boolean, Boolean> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.settings_filters.guild_id</code>.
     */
    public SettingsFiltersRecord setGuildId(Long value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>public.settings_filters.guild_id</code>.
     */
    public Long getGuildId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>public.settings_filters.pinned_deleting_enabled</code>.
     */
    public SettingsFiltersRecord setPinnedDeletingEnabled(Boolean value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>public.settings_filters.pinned_deleting_enabled</code>.
     */
    public Boolean getPinnedDeletingEnabled() {
        return (Boolean) get(1);
    }

    /**
     * Setter for <code>public.settings_filters.invite_deleting_enabled</code>.
     */
    public SettingsFiltersRecord setInviteDeletingEnabled(Boolean value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>public.settings_filters.invite_deleting_enabled</code>.
     */
    public Boolean getInviteDeletingEnabled() {
        return (Boolean) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<Long, Boolean, Boolean> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<Long, Boolean, Boolean> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<Long> field1() {
        return SettingsFilters.SETTINGS_FILTERS.GUILD_ID;
    }

    @Override
    public Field<Boolean> field2() {
        return SettingsFilters.SETTINGS_FILTERS.PINNED_DELETING_ENABLED;
    }

    @Override
    public Field<Boolean> field3() {
        return SettingsFilters.SETTINGS_FILTERS.INVITE_DELETING_ENABLED;
    }

    @Override
    public Long component1() {
        return getGuildId();
    }

    @Override
    public Boolean component2() {
        return getPinnedDeletingEnabled();
    }

    @Override
    public Boolean component3() {
        return getInviteDeletingEnabled();
    }

    @Override
    public Long value1() {
        return getGuildId();
    }

    @Override
    public Boolean value2() {
        return getPinnedDeletingEnabled();
    }

    @Override
    public Boolean value3() {
        return getInviteDeletingEnabled();
    }

    @Override
    public SettingsFiltersRecord value1(Long value) {
        setGuildId(value);
        return this;
    }

    @Override
    public SettingsFiltersRecord value2(Boolean value) {
        setPinnedDeletingEnabled(value);
        return this;
    }

    @Override
    public SettingsFiltersRecord value3(Boolean value) {
        setInviteDeletingEnabled(value);
        return this;
    }

    @Override
    public SettingsFiltersRecord values(Long value1, Boolean value2, Boolean value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached SettingsFiltersRecord
     */
    public SettingsFiltersRecord() {
        super(SettingsFilters.SETTINGS_FILTERS);
    }

    /**
     * Create a detached, initialised SettingsFiltersRecord
     */
    public SettingsFiltersRecord(Long guildId, Boolean pinnedDeletingEnabled, Boolean inviteDeletingEnabled) {
        super(SettingsFilters.SETTINGS_FILTERS);

        setGuildId(guildId);
        setPinnedDeletingEnabled(pinnedDeletingEnabled);
        setInviteDeletingEnabled(inviteDeletingEnabled);
    }
}
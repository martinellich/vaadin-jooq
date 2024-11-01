package ch.martinelli.oss.vaadinjooq.ui;

import ch.martinelli.oss.vaadinjooq.repository.JooqRepository;
import ch.martinelli.oss.vaadinjooq.util.JooqUtil;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.SortOrder;
import org.jooq.Record;
import org.jooq.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The RecordGrid uses the table fields generated by jOOQ to create Vaadin Grid.
 * It contains a Builder that can be used to create the Grid.
 *
 * @param <R> The type of the jOOQ Record to use
 */
public class RecordGrid<R extends Record> extends Grid<R> {

    private ConfigurableFilterDataProvider<R, Void, Condition> filterDataProvider;

    private RecordGrid() {
        super();
    }

    private void createColumns(TableField<?, ?>[] columns, String[] headers) {
        if (headers != null && headers.length != columns.length) {
            throw new IllegalStateException("Number of headers must match number of cloumns!");
        }

        for (int i = 0; i < columns.length; i++) {
            TableField<?, ?> column = columns[i];
            String header;
            if (headers != null) {
                header = headers[i];
            } else {
                header = JooqUtil.getHeaderText(column);
            }
            addColumn(r -> r.getValue(column))
                    .setHeader(header)
                    .setKey(column.getName())
                    .setSortProperty(column.getName())
                    .setSortable(true);
        }
    }

    private void setFilterDataProvider(ConfigurableFilterDataProvider<R, Void, Condition> filterDataProvider) {
        this.filterDataProvider = filterDataProvider;
        super.setItems(filterDataProvider);
    }

    public void filter(Condition condition) {
        filterDataProvider.setFilter(condition);
        filterDataProvider.refreshAll();
    }

    public void refreshAll() {
        filterDataProvider.refreshAll();
    }

    public void refreshItem(R item) {
        filterDataProvider.refreshItem(item);
    }

    /**
     * Builder to create the {@link RecordGrid}
     *
     * @param <R> The type of the jOOQ Record to use
     */
    public static class Builder<R extends Record> {

        private final JooqRepository repository;
        private final Table<R> table;
        private RecordGrid<R> grid;
        private TableField<?, ?>[] columns;
        private TableField<?, ?>[] idColumns;
        private String[] headers;
        private Condition condition;
        private Map<Field<?>, SortDirection> sort;

        /**
         * Creates the builder
         *
         * @param table      The jOOQ {@link Table}
         * @param dslContext The jOOQ {@link DSLContext}
         */
        public Builder(Table<R> table, DSLContext dslContext) {
            this.table = table;
            repository = new JooqRepository(dslContext);
        }

        /**
         * Specify the columns to display in the grid
         *
         * @param columns Grid columns
         * @return The builder
         */
        public Builder<R> withColumns(TableField<?, ?>... columns) {
            this.columns = columns;

            return this;
        }

        /**
         * Set the header texts
         *
         * @param headers Header texts
         * @return The builder
         */
        public Builder<R> withHeaders(String... headers) {
            this.headers = headers;

            return this;
        }

        /**
         * Optional. Set the columns that form the primary key.
         * This is necessary if the {@link Table} is a view or has no primary key
         *
         * @param idColumns Id columns
         * @return The builder
         */
        public Builder<R> withIdColumns(TableField<?, ?>... idColumns) {
            this.idColumns = idColumns;

            return this;
        }

        /**
         * Define a initial filter condition
         *
         * @param condition Initial {@link Condition}
         * @return The builder
         */
        public Builder<R> withCondition(Condition condition) {
            this.condition = condition;

            return this;
        }

        /**
         * Set the initial sort order
         *
         * @param sort Initial sort order
         * @return The builder
         */
        public Builder<R> withSort(Map<Field<?>, SortDirection> sort) {
            this.sort = sort;

            return this;
        }

        /**
         * Builds the {@link RecordGrid}
         *
         * @return RecordGrid
         */
        public RecordGrid<R> build() {
            grid = new RecordGrid<>();

            if (columns != null && columns.length > 0) {
                grid.createColumns(columns, headers);
            }

            if (table.getPrimaryKey() == null && (idColumns == null || idColumns.length == 0)) {
                throw new IllegalStateException("The table has no primary key or unique key and no idColumns are provided!");
            }

            grid.setFilterDataProvider(createDataProvider());

            return grid;
        }

        private ConfigurableFilterDataProvider<R, Void, Condition> createDataProvider() {
            CallbackDataProvider<R, Condition> dataProvider = new CallbackDataProvider<>(
                    query -> {
                        List<R> rows = repository.findAll(table, createFilter(query), createOrderBy(query), query.getOffset(), query.getLimit());
                        if (!rows.isEmpty() && grid.getSelectedItems().isEmpty()) {
                            grid.select(rows.get(0));
                        }
                        return rows.stream();
                    },
                    query -> repository.count(table, createFilter(query)),
                    r -> {
                        StringBuilder sb = new StringBuilder();
                        if (idColumns != null && idColumns.length > 0) {
                            // If idColumns are set these are used to generate the id
                            for (TableField<?, ?> idColumn : idColumns) {
                                Object value = r.getValue(idColumn);
                                sb.append(value);
                            }
                        } else {
                            UniqueKey<R> primaryKey = table.getPrimaryKey();
                            if (primaryKey != null) {
                                // If there is a primary key we use this
                                for (TableField<R, ?> field : primaryKey.getFields()) {
                                    Object value = r.getValue(field);
                                    sb.append(value);
                                }
                            } else if (!table.getUniqueKeys().isEmpty()) {
                                // If there is are unique keys we use the first
                                UniqueKey<R> uniqueKey = table.getUniqueKeys().get(0);
                                for (TableField<R, ?> field : uniqueKey.getFields()) {
                                    Object value = r.getValue(field);
                                    sb.append(value);
                                }
                            }
                        }
                        return sb.toString();
                    }
            );
            return dataProvider.withConfigurableFilter();
        }

        private Condition createFilter(com.vaadin.flow.data.provider.Query<R, Condition> query) {
            Condition filterCondition;
            Optional<Condition> filter = query.getFilter();
            if (filter.isPresent()) {
                if (condition == null) {
                    filterCondition = filter.get();
                } else {
                    filterCondition = condition.and(filter.get());
                }
            } else {
                filterCondition = condition;
            }
            return filterCondition;
        }

        private Map<Field<?>, SortDirection> createOrderBy(com.vaadin.flow.data.provider.Query<R, Condition> query) {
            Map<Field<?>, SortDirection> orderBy = new HashMap<>();
            for (SortOrder<String> sortOrder : query.getSortOrders()) {
                orderBy.put(table.field(sortOrder.getSorted()), sortOrder.getDirection());
            }
            if (orderBy.isEmpty()) {
                orderBy = sort;
            }
            return orderBy;
        }
    }
}

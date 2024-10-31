/*
 * This file is generated by jOOQ.
 */
package ch.martinelli.oss.vaadinjooq.db;


import ch.martinelli.oss.vaadinjooq.db.tables.Customer;
import ch.martinelli.oss.vaadinjooq.db.tables.records.CustomerRecord;
import jakarta.annotation.Generated;
import org.jooq.Identity;
import org.jooq.UniqueKey;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables of 
 * the <code></code> schema.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // IDENTITY definitions
    // -------------------------------------------------------------------------

    public static final Identity<CustomerRecord, Integer> IDENTITY_CUSTOMER = Identities0.IDENTITY_CUSTOMER;

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<CustomerRecord> CONSTRAINT_5 = UniqueKeys0.CONSTRAINT_5;
    public static final UniqueKey<CustomerRecord> CONSTRAINT_52 = UniqueKeys0.CONSTRAINT_52;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------


    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Identities0 {
        public static Identity<CustomerRecord, Integer> IDENTITY_CUSTOMER = Internal.createIdentity(Customer.CUSTOMER, Customer.CUSTOMER.ID);
    }

    private static class UniqueKeys0 {
        public static final UniqueKey<CustomerRecord> CONSTRAINT_5 = Internal.createUniqueKey(Customer.CUSTOMER, "CONSTRAINT_5", Customer.CUSTOMER.ID);
        public static final UniqueKey<CustomerRecord> CONSTRAINT_52 = Internal.createUniqueKey(Customer.CUSTOMER, "CONSTRAINT_52", Customer.CUSTOMER.EMAIL);
    }
}
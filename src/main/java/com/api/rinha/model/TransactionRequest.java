package com.api.rinha.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TransactionRequest {

    public String id;
    public Transaction transaction;
    public Customer customer;
    public Merchant merchant;
    public Terminal terminal;

    @JsonProperty("last_transaction")
    public LastTransaction lastTransaction;

    public static class Transaction {
        public double amount;
        public int installments;

        @JsonProperty("requested_at")
        public String requestedAt;
    }

    public static class Customer {
        @JsonProperty("avg_amount")
        public double avgAmount;

        @JsonProperty("tx_count_24h")
        public int txCount24h;

        @JsonProperty("known_merchants")
        public List<String> knownMerchants;
    }

    public static class Merchant {
        public String id;
        public String mcc;

        @JsonProperty("avg_amount")
        public double avgAmount;
    }

    public static class Terminal {
        @JsonProperty("is_online")
        public boolean isOnline;

        @JsonProperty("card_present")
        public boolean cardPresent;

        @JsonProperty("km_from_home")
        public double kmFromHome;
    }

    public static class LastTransaction {
        public String timestamp;

        @JsonProperty("km_from_current")
        public double kmFromCurrent;
    }
}
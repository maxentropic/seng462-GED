package com.teamged.txserver.database;

import com.teamged.ServerConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by DanielF on 2016-02-02.
 */
public class UserDatabaseObject {
    private static final int CENT_CAP = 100;

    private final Object lock = new Object();
    private final List<String> history = new ArrayList<>();
    private final Deque<StockRequest> sellList = new ArrayDeque<>();
    private final Deque<StockRequest> buyList = new ArrayDeque<>();
    private final Deque<StockTrigger> buyTriggers = new ArrayDeque<>();
    private final Deque<StockTrigger> sellTriggers = new ArrayDeque<>();
    private final Map<String, Integer> stocksOwned = new HashMap<>();
    private final String userName;
    private StockRequest sellAmount = null;
    private StockRequest buyAmount = null;
    private int dollars = 0;
    private int cents = 0;

    public UserDatabaseObject(String user) {
        userName = user;
    }

    public String add(int dollars, int cents) {
        synchronized (lock) {
            this.dollars += dollars;
            this.cents += cents;
            if (this.cents >= CENT_CAP) {
                this.cents -= CENT_CAP;
                this.dollars++;
            }
            history.add("ADD," + userName + "," + dollars + "." + cents);
            // TODO: Update database
        }

        return userName + ", " + this.dollars + "." + this.cents;
    }

    /**
     * Gets a raw quote string from the request server.
     *
     * @param stock
     * @return
     */
    public String quote(String stock) {
        String quote;
        synchronized (lock) {
            try (
                    Socket quoteSocket = new Socket(ServerConstants.QUOTE_SERVER, ServerConstants.QUOTE_PORT);
                    PrintWriter out = new PrintWriter(quoteSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(quoteSocket.getInputStream()))
            ) {
                out.println(stock + "," + userName);
                quote = in.readLine();
                history.add("QUOTE," + userName + "," + stock);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                quote = "QUOTE ERROR";
            } catch (IOException e) {
                e.printStackTrace();
                quote = "QUOTE ERROR";
            }
        }

        return quote;
    }

    public String buy(String stock, int dollars, int cents) {
        String buyRes;
        synchronized (lock) {
            if (dollars > this.dollars || (dollars == this.dollars && cents >= this.cents)) {
                String quote = quote(stock);
                String[] quoteParams = quote.split(",");
                try {
                    if (quoteParams.length == 5 && quoteParams[1].equals(stock) && quoteParams[2].equals(userName)) {
                        String[] stockPriceString = quoteParams[0].split("\\.");
                        int stockDollars = Integer.parseInt(stockPriceString[0]);
                        int stockCents = Integer.parseInt(stockPriceString[1]);
                        long stockPrice = (long) stockDollars * CENT_CAP + stockCents;
                        long stockPurchaseMoney = (long) dollars * CENT_CAP + cents;
                        int stockCount = (int) (stockPurchaseMoney / stockPrice);
                        long remainingMoney = stockPurchaseMoney % stockPrice;
                        long spentMoney = stockPurchaseMoney - remainingMoney;

                        StockRequest sr = new StockRequest(
                                stock,
                                stockCount,
                                (int) (spentMoney / CENT_CAP),
                                (int) (spentMoney % CENT_CAP),
                                Calendar.getInstance().getTimeInMillis());
                        buyList.push(sr);
                        this.dollars -= (remainingMoney / CENT_CAP);
                        this.cents -= (remainingMoney % CENT_CAP);
                        if (this.cents < 0) {
                            this.dollars--;
                            this.cents += CENT_CAP;
                        }

                        // TODO: Start a timer to expire the stored buy request in 60 seconds
                        history.add("BUY," + userName + "," + stock + "," + dollars + "." + cents);
                        buyRes = quote;
                    } else {
                        buyRes = "BUY ERROR," + userName + "," + this.dollars + "." + this.cents + "," + quote;
                    }
                } catch (Exception e) {
                    buyRes = "BUY ERROR," + userName + "," + this.dollars + "." + this.cents + "," + quote;
                }
            } else {
                buyRes = "BUY ERROR," + userName + "," + this.dollars + "." + this.cents;
            }
        }

        return buyRes;
    }

    public String commitBuy() {
        String commitRes;
        synchronized (lock) {
            if (!buyList.isEmpty()) {
                StockRequest buyReq = buyList.removeLast();
                // TODO: Confirm time has not expired on this buy request

                String stock = buyReq.getStock();
                int stockAmt = buyReq.getShares();
                if (stocksOwned.containsKey(stock)) {
                    stockAmt += stocksOwned.get(stock);
                }

                stocksOwned.put(stock, stockAmt);
                history.add("COMMIT_BUY," + userName);

                commitRes = "COMMIT_BUY," + userName + "," + stock + "," + buyReq.getDollars() + "." + buyReq.getCents();
            } else {
                commitRes = "COMMIT_BUY ERROR";
            }
        }

        return commitRes;
    }

    public String cancelBuy() {
        String cancelRes;
        synchronized (lock) {
            if (!buyList.isEmpty()) {
                StockRequest buyReq = buyList.removeLast();

                this.dollars += buyReq.getDollars();
                this.cents += buyReq.getCents();
                if (this.cents >= CENT_CAP) {
                    this.cents -= CENT_CAP;
                    this.dollars++;
                }

                history.add("CANCEL_BUY," + userName);
                cancelRes = "CANCEL_BUY," + userName + "," + buyReq.getStock() + "," + this.dollars + "." + this.cents;
            } else {
                cancelRes = "CANCEL_BUY ERROR";
            }
        }

        return cancelRes;
    }

    public String sell(String stock, int dollars, int cents) {
        String sellRes;
        synchronized (lock) {
            String quote = quote(stock);
            String[] quoteParams = quote.split(",");
            try {
                if (quoteParams.length == 5 && quoteParams[1].equals(stock) && quoteParams[2].equals(userName)) {
                    long sellMoney = (long) dollars * CENT_CAP + cents;

                    // Gets the current value of the stock on the market
                    String[] stockPriceString = quoteParams[0].split("\\.");
                    int stockDollars = Integer.parseInt(stockPriceString[0]);
                    int stockCents = Integer.parseInt(stockPriceString[1]);
                    long stockPrice = (long) stockDollars * CENT_CAP + stockCents;

                    // Gets the current value of the owned stocks of this type
                    int ownedCount = 0;
                    if (stocksOwned.containsKey(stock)) {
                        ownedCount = stocksOwned.get(stock);
                    }
                    long ownedValue = stockPrice * ownedCount;

                    // Gets the value of the rounded stocks to sell
                    int actualSellAmount = (int) (sellMoney / stockPrice);
                    long actualValue = actualSellAmount * stockPrice;

                    // Removes the number of stocks as would be sold by this command
                    if (ownedValue >= actualValue) {
                        StockRequest sr = new StockRequest(
                                stock,
                                actualSellAmount,
                                (int) (actualValue / CENT_CAP),
                                (int) (actualValue % CENT_CAP),
                                Calendar.getInstance().getTimeInMillis()
                        );

                        sellList.push(sr);
                        stocksOwned.put(stock, ownedCount - actualSellAmount);
                        // TODO: Start a timer to expire the stored buy request in 60 seconds
                        history.add("SELL," + userName + "," + stock + "," + dollars + "." + cents);
                        sellRes = quote;
                    } else {
                        sellRes = "SELL ERROR," + userName + "," + stock + "," + ownedCount + "," + quote;
                    }
                } else {
                    sellRes = "SELL ERROR," + userName + "," + this.dollars + "." + this.cents + "," + quote;
                }
            } catch (Exception e) {
                sellRes = "SELL ERROR," + userName + "," + this.dollars + "." + this.cents + "," + quote;
            }
        }

        return sellRes;
    }

    public String commitSell() {
        String commitRes;
        synchronized (lock) {
            if (!sellList.isEmpty()) {
                StockRequest sellReq = sellList.removeLast();
                // TODO: Confirm time has not expired on this sell request

                // Releases the money into the account
                this.dollars += sellReq.getDollars();
                this.cents += sellReq.getCents();
                if (this.cents >= CENT_CAP) {
                    this.cents -= CENT_CAP;
                    this.dollars++;
                }

                history.add("COMMIT_SELL," + userName);

                commitRes = "COMMIT_SELL," + userName + "," + sellReq.getStock() + "," + sellReq.getDollars() + "." + sellReq.getCents();
            } else {
                commitRes = "COMMIT_SELL ERROR";
            }
        }

        return commitRes;
    }

    public String cancelSell() {
        String cancelRes;
        synchronized (lock) {
            if (!sellList.isEmpty()) {
                StockRequest sellReq = sellList.removeLast();
                String stock = sellReq.getStock();

                // Returns stocks to holdings
                int stockCount = sellReq.getShares();
                if (stocksOwned.containsKey(stock)) {
                    stockCount = stocksOwned.get(stock);
                }

                stocksOwned.put(stock, stockCount);

                history.add("CANCEL_SELL," + userName);
                cancelRes = "CANCEL_SELL," + userName + "," + stock + "," + this.dollars + "." + this.cents;
            } else {
                cancelRes = "CANCEL_SELL ERROR";
            }
        }

        return cancelRes;
    }

    public String setBuyAmount(String stock, int dollars, int cents) {
        String setRes;
        synchronized (lock) {
            // TODO: Confirm the business logic that should be followed if a previous set buy is present
            if (buyAmount == null && (dollars > this.dollars || (dollars == this.dollars && cents >= this.cents))) {
                buyAmount = new StockRequest(stock, 0, dollars, cents, 0);

                history.add("SET_BUY_AMOUNT," + userName + "," + stock + "," + dollars + "." + cents);
                setRes = "SET_BUY_AMOUNT," + userName + "," + stock + "," + dollars + "." + cents;
            } else {
                setRes = "SET_BUY_AMOUNT ERROR," + userName + "," + this.dollars + "." + this.cents;
            }
        }

        return setRes;
    }

    public String cancelSetBuy(String stock) {
        String cancelSet;
        synchronized (lock) {
            StockRequest buyReq = null;

            if (buyAmount != null && buyAmount.getStock().equals(stock)) {
                buyReq = buyAmount;
                buyAmount = null;
            } else {
                Iterator<StockTrigger> iter = buyTriggers.descendingIterator();
                do {
                    StockTrigger trigger = iter.next();
                    if (trigger.getSetAmount().getStock().equals(stock)) {
                        buyTriggers.remove(trigger);
                        buyReq = trigger.getSetAmount();
                        break;
                    }
                } while (iter.hasNext());
            }

            if (buyReq != null) {
                this.dollars += buyReq.getDollars();
                this.cents += buyReq.getCents();
                if (this.cents >= CENT_CAP) {
                    this.cents -= CENT_CAP;
                    this.dollars++;
                }

                history.add("CANCEL_SET_BUY," + userName + "," + stock);
                cancelSet = "CANCEL_SET_BUY," + userName + "," + stock + "," + this.dollars + "." + this.cents;
            } else {
                cancelSet = "CANCEL_SET_BUY ERROR";
            }
        }

        return cancelSet;
    }

    public String setBuyTrigger(String stock, int dollars, int cents) {
        String triggerSet;
        synchronized (lock) {
            if (buyAmount != null && buyAmount.getStock().equals(stock)) {
                StockRequest buy = buyAmount;
                buyAmount = null;

                String quote = quote(stock);    // Get a quote first to see if the current price is good
                String[] quoteParams = quote.split(",");
                try {
                    if (quoteParams.length == 5 && quoteParams[1].equals(stock) && quoteParams[2].equals(userName)) {
                        String[] stockPriceString = quoteParams[0].split("\\.");
                        int stockDollars = Integer.parseInt(stockPriceString[0]);
                        int stockCents = Integer.parseInt(stockPriceString[1]);

                        if (stockDollars < dollars || (stockDollars == dollars && stockCents <= cents)) {
                            long stockPrice = (long) stockDollars * CENT_CAP + stockCents;
                            long stockPurchaseMoney = (long) buy.getDollars() * CENT_CAP + buy.getCents();
                            int stockCount = (int) (stockPurchaseMoney / stockPrice);
                            long remainingMoney = stockPurchaseMoney % stockPrice;

                            this.dollars += (remainingMoney / CENT_CAP);
                            this.cents += (remainingMoney % CENT_CAP);
                            if (this.cents >= CENT_CAP) {
                                this.cents -= CENT_CAP;
                                this.dollars++;
                            }

                            int newStockCount = stockCount;
                            if (stocksOwned.containsKey(stock)) {
                                newStockCount = stocksOwned.get(stock);
                            }
                            stocksOwned.put(stock, newStockCount);

                            history.add("BUY," + userName + "," + stock + "," + buy.getDollars() + "." + buy.getCents());
                            history.add("COMMIT_BUY," + userName);

                            triggerSet = "SET_BUY_TRIGGER,BUY,COMMIT_BUY," + quote;
                        } else {
                            // TODO: Update expiry of buy request amount
                            StockTrigger trigger = new StockTrigger(buy, dollars, cents);
                            buyTriggers.add(trigger);

                            // TODO: Start a trigger timer
                            triggerSet = "SET_BUY_TRIGGER," + quote;
                        }

                    } else {
                        triggerSet = "SET_BUY_TRIGGER ERROR," + userName + "," + this.dollars + "." + this.cents + "," + quote;
                    }
                } catch (Exception e) {
                    triggerSet = "SET_BUY_TRIGGER ERROR," + userName + "," + this.dollars + "." + this.cents + "," + quote;
                }
            } else {
                triggerSet = "SET_BUY_TRIGGER ERROR";
            }
        }

        return triggerSet;
    }

    public String setSellAmount(String stock, int dollars, int cents) {
        // TODO
        return "";
    }

    public String cancelSetSell(String stock) {
        // TODO
        return "";
    }

    public String setSellTrigger(String stock, int dollars, int cents) {
        // TODO
        return "";
    }

    public String dumplog(String filename) {
        // TODO: May have to eventually handle this (or just handle elsewhere)
        history.add("DUMPLOG," + userName + "," + filename);
        return "";
    }

    public String dumplog() {
        // TODO: May have to eventually handle this (or just handle elsewhere)
        return "";
    }

    public String displaySummary() {
        StringBuilder summary = new StringBuilder();
        for (String event : history) {
            summary.append(event);
            summary.append(";");
        }

        history.add("DISPLAY_SUMMARY," + userName);

        return summary.toString();
    }
}
